package com.cheng.line.service.impl;

import com.cheng.line.domain.LineUserTagRelation;
import com.cheng.line.dto.TagPreviewDTO;
import com.cheng.line.mapper.LineUserTagRelationMapper;
import com.cheng.line.service.ILineTagResolveService;
import com.cheng.system.domain.SysTag;
import com.cheng.system.domain.SysTagGroup;
import com.cheng.system.domain.SysTagGroupDetail;
import com.cheng.system.mapper.SysTagGroupDetailMapper;
import com.cheng.system.mapper.SysTagGroupMapper;
import com.cheng.system.mapper.SysTagMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * LINE 標籤目標解析 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineTagResolveServiceImpl implements ILineTagResolveService {

    private final LineUserTagRelationMapper lineUserTagRelationMapper;
    private final SysTagGroupMapper sysTagGroupMapper;
    private final SysTagGroupDetailMapper sysTagGroupDetailMapper;
    private final SysTagMapper sysTagMapper;

    @Override
    public Set<String> resolveTargets(List<Long> tagIds, List<Long> tagGroupIds) {
        Set<String> result = new HashSet<>();

        // 解析個別標籤
        if (tagIds != null && !tagIds.isEmpty()) {
            result.addAll(resolveByTagIds(tagIds));
        }

        // 解析標籤群組
        if (tagGroupIds != null && !tagGroupIds.isEmpty()) {
            result.addAll(resolveByTagGroupIds(tagGroupIds));
        }

        log.info("標籤目標解析完成：tagIds={}, tagGroupIds={}, 解析人數={}", tagIds, tagGroupIds, result.size());
        return result;
    }

    @Override
    public TagPreviewDTO previewCount(List<Long> tagIds, List<Long> tagGroupIds) {
        TagPreviewDTO preview = new TagPreviewDTO();
        List<TagPreviewDTO.TagDetailDTO> tagDetails = new ArrayList<>();

        // 各標籤明細
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                SysTag tag = sysTagMapper.selectSysTagByTagId(tagId);
                if (tag == null) {
                    continue;
                }
                int userCount = lineUserTagRelationMapper.countUsersByTagId(tagId);

                TagPreviewDTO.TagDetailDTO detail = new TagPreviewDTO.TagDetailDTO();
                detail.setTagId(tagId);
                detail.setTagName(tag.getTagName());
                detail.setTagColor(tag.getTagColor());
                detail.setUserCount(userCount);
                tagDetails.add(detail);
            }
        }

        // 標籤群組中的標籤明細
        if (tagGroupIds != null) {
            for (Long groupId : tagGroupIds) {
                List<SysTagGroupDetail> details = sysTagGroupDetailMapper.selectByGroupId(groupId);
                for (SysTagGroupDetail groupDetail : details) {
                    // 避免重複加入已存在的標籤
                    boolean alreadyExists = tagDetails.stream()
                            .anyMatch(d -> d.getTagId().equals(groupDetail.getTagId()));
                    if (alreadyExists) {
                        continue;
                    }

                    SysTag tag = sysTagMapper.selectSysTagByTagId(groupDetail.getTagId());
                    if (tag == null) {
                        continue;
                    }
                    int userCount = lineUserTagRelationMapper.countUsersByTagId(groupDetail.getTagId());

                    TagPreviewDTO.TagDetailDTO detail = new TagPreviewDTO.TagDetailDTO();
                    detail.setTagId(groupDetail.getTagId());
                    detail.setTagName(tag.getTagName());
                    detail.setTagColor(tag.getTagColor());
                    detail.setUserCount(userCount);
                    tagDetails.add(detail);
                }
            }
        }

        // 解析去重後的總人數
        Set<String> allTargets = resolveTargets(tagIds, tagGroupIds);
        preview.setCount(allTargets.size());
        preview.setTagDetails(tagDetails);

        return preview;
    }

    /**
     * 根據個別標籤 ID 解析目標使用者
     * 對每個 tagId 查詢 line_user_tag_relation，合併為聯集
     */
    private Set<String> resolveByTagIds(List<Long> tagIds) {
        Set<String> result = new HashSet<>();
        for (Long tagId : tagIds) {
            List<LineUserTagRelation> relations = lineUserTagRelationMapper.selectByTagId(tagId);
            for (LineUserTagRelation relation : relations) {
                result.add(relation.getLineUserId());
            }
        }
        return result;
    }

    /**
     * 根據標籤群組 ID 解析目標使用者
     * 依據群組的 calcMode 和明細的 operator 做交集/聯集運算
     */
    private Set<String> resolveByTagGroupIds(List<Long> tagGroupIds) {
        Set<String> result = new HashSet<>();

        for (Long groupId : tagGroupIds) {
            SysTagGroup group = sysTagGroupMapper.selectSysTagGroupById(groupId);
            if (group == null) {
                log.warn("標籤群組不存在：groupId={}", groupId);
                continue;
            }

            List<SysTagGroupDetail> details = sysTagGroupDetailMapper.selectByGroupId(groupId);
            if (details == null || details.isEmpty()) {
                continue;
            }

            // 依序號排序
            details.sort(Comparator.comparingInt(SysTagGroupDetail::getGroupIndex));

            Set<String> groupResult = resolveGroupDetails(details, group.getCalcMode());
            result.addAll(groupResult);
        }

        return result;
    }

    /**
     * 根據群組明細和運算模式解析使用者
     *
     * @param details  排序後的群組明細列表
     * @param calcMode 運算模式：LEFT_TO_RIGHT 或 OR_OF_AND
     */
    private Set<String> resolveGroupDetails(List<SysTagGroupDetail> details, String calcMode) {
        if (details.isEmpty()) {
            return Collections.emptySet();
        }

        // 取得第一個標籤的使用者集合
        Set<String> currentResult = getUserIdsByTagId(details.getFirst().getTagId());

        if (SysTagGroup.CALC_MODE_OR_OF_AND.equals(calcMode)) {
            return resolveOrOfAnd(details);
        }

        // LEFT_TO_RIGHT：依序套用 AND/OR 運算子
        for (int i = 1; i < details.size(); i++) {
            SysTagGroupDetail detail = details.get(i);
            Set<String> tagUsers = getUserIdsByTagId(detail.getTagId());

            if (SysTagGroupDetail.OPERATOR_AND.equals(detail.getOperator())) {
                currentResult.retainAll(tagUsers);
            } else if (SysTagGroupDetail.OPERATOR_OR.equals(detail.getOperator())) {
                currentResult.addAll(tagUsers);
            }
        }

        return currentResult;
    }

    /**
     * OR_OF_AND 模式：先做 AND 群組，再取 OR 聯集
     * 例如：A AND B OR C AND D → (A ∩ B) ∪ (C ∩ D)
     */
    private Set<String> resolveOrOfAnd(List<SysTagGroupDetail> details) {
        Set<String> finalResult = new HashSet<>();
        Set<String> andGroup = getUserIdsByTagId(details.getFirst().getTagId());

        for (int i = 1; i < details.size(); i++) {
            SysTagGroupDetail detail = details.get(i);
            Set<String> tagUsers = getUserIdsByTagId(detail.getTagId());

            if (SysTagGroupDetail.OPERATOR_AND.equals(detail.getOperator())) {
                andGroup.retainAll(tagUsers);
            } else if (SysTagGroupDetail.OPERATOR_OR.equals(detail.getOperator())) {
                // 前一個 AND 群組結束，加入最終結果
                finalResult.addAll(andGroup);
                // 開始新的 AND 群組
                andGroup = tagUsers;
            }
        }

        // 加入最後一個 AND 群組
        finalResult.addAll(andGroup);
        return finalResult;
    }

    /**
     * 根據標籤 ID 取得使用者 ID 集合
     */
    private Set<String> getUserIdsByTagId(Long tagId) {
        List<LineUserTagRelation> relations = lineUserTagRelationMapper.selectByTagId(tagId);
        return relations.stream()
                .map(LineUserTagRelation::getLineUserId)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
