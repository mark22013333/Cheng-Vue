package com.cheng.line.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.line.domain.LineFlexTemplate;
import com.cheng.line.mapper.LineFlexTemplateMapper;
import com.cheng.line.service.ILineFlexTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * LINE Flex 自訂範本 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineFlexTemplateServiceImpl implements ILineFlexTemplateService {

    private final LineFlexTemplateMapper lineFlexTemplateMapper;

    @Override
    public List<LineFlexTemplate> selectLineFlexTemplateList(LineFlexTemplate template) {
        return lineFlexTemplateMapper.selectLineFlexTemplateList(template);
    }

    @Override
    public List<LineFlexTemplate> selectAvailableFlexTemplates() {
        Long userId = SecurityUtils.getUserId();
        return lineFlexTemplateMapper.selectAvailableFlexTemplates(userId);
    }

    @Override
    public LineFlexTemplate selectLineFlexTemplateById(Long flexTemplateId) {
        return lineFlexTemplateMapper.selectLineFlexTemplateById(flexTemplateId);
    }

    @Override
    public int insertLineFlexTemplate(LineFlexTemplate template) {
        // 設定建立者資訊
        Long userId = SecurityUtils.getUserId();
        String username = SecurityUtils.getUsername();
        String nickName = SecurityUtils.getLoginUser().getUser().getNickName();
        
        template.setCreatorId(userId);
        template.setCreatorName(nickName != null ? nickName : username);
        template.setCreateBy(username);
        template.setCreateTime(DateUtils.getNowDate());
        template.setUseCount(0);
        
        // 預設私人範本
        if (template.getIsPublic() == null) {
            template.setIsPublic(0);
        }
        // 預設啟用
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        // 預設替代文字
        if (template.getAltText() == null || template.getAltText().isEmpty()) {
            template.setAltText("訊息通知");
        }
        
        return lineFlexTemplateMapper.insertLineFlexTemplate(template);
    }

    @Override
    public int updateLineFlexTemplate(LineFlexTemplate template) {
        template.setUpdateBy(SecurityUtils.getUsername());
        template.setUpdateTime(DateUtils.getNowDate());
        return lineFlexTemplateMapper.updateLineFlexTemplate(template);
    }

    @Override
    public int deleteLineFlexTemplateById(Long flexTemplateId) {
        return lineFlexTemplateMapper.deleteLineFlexTemplateById(flexTemplateId);
    }

    @Override
    public int deleteLineFlexTemplateByIds(Long[] flexTemplateIds) {
        return lineFlexTemplateMapper.deleteLineFlexTemplateByIds(flexTemplateIds);
    }

    @Override
    public int incrementUseCount(Long flexTemplateId) {
        return lineFlexTemplateMapper.incrementUseCount(flexTemplateId);
    }
}
