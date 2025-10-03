package com.cheng.system.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.system.domain.InvBookInfo;
import com.cheng.system.mapper.InvBookInfoMapper;
import com.cheng.system.service.IInvBookInfoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 書籍資訊 服務層實現
 *
 * @author cheng
 */
@Service
public class InvBookInfoServiceImpl implements IInvBookInfoService {
    private static final Logger log = LoggerFactory.getLogger(InvBookInfoServiceImpl.class);

    @Autowired
    private InvBookInfoMapper invBookInfoMapper;

    /**
     * 查詢書籍資訊
     *
     * @param bookInfoId 書籍資訊主鍵
     * @return 書籍資訊
     */
    @Override
    public InvBookInfo selectInvBookInfoByBookInfoId(Long bookInfoId) {
        return invBookInfoMapper.selectInvBookInfoByBookInfoId(bookInfoId);
    }

    /**
     * 根據 ISBN 查詢書籍資訊
     *
     * @param isbn ISBN
     * @return 書籍資訊
     */
    @Override
    public InvBookInfo selectInvBookInfoByIsbn(String isbn) {
        return invBookInfoMapper.selectInvBookInfoByIsbn(isbn);
    }

    /**
     * 根據物品ID查詢書籍資訊
     *
     * @param itemId 物品ID
     * @return 書籍資訊
     */
    @Override
    public InvBookInfo selectInvBookInfoByItemId(Long itemId) {
        return invBookInfoMapper.selectInvBookInfoByItemId(itemId);
    }

    /**
     * 查詢書籍資訊列表
     *
     * @param invBookInfo 書籍資訊
     * @return 書籍資訊集合
     */
    @Override
    public List<InvBookInfo> selectInvBookInfoList(InvBookInfo invBookInfo) {
        return invBookInfoMapper.selectInvBookInfoList(invBookInfo);
    }

    /**
     * 新增書籍資訊
     *
     * @param invBookInfo 書籍資訊
     * @return 結果
     */
    @Override
    public int insertInvBookInfo(InvBookInfo invBookInfo) {
        invBookInfo.setCreateTime(DateUtils.getNowDate());
        return invBookInfoMapper.insertInvBookInfo(invBookInfo);
    }

    /**
     * 修改書籍資訊
     *
     * @param invBookInfo 書籍資訊
     * @return 結果
     */
    @Override
    public int updateInvBookInfo(InvBookInfo invBookInfo) {
        invBookInfo.setUpdateTime(DateUtils.getNowDate());
        return invBookInfoMapper.updateInvBookInfo(invBookInfo);
    }

    /**
     * 批量刪除書籍資訊
     *
     * @param bookInfoIds 需要刪除的書籍資訊主鍵集合
     * @return 結果
     */
    @Override
    public int deleteInvBookInfoByBookInfoIds(Long[] bookInfoIds) {
        return invBookInfoMapper.deleteInvBookInfoByBookInfoIds(bookInfoIds);
    }

    /**
     * 刪除書籍資訊
     *
     * @param bookInfoId 書籍資訊主鍵
     * @return 結果
     */
    @Override
    public int deleteInvBookInfoByBookInfoId(Long bookInfoId) {
        return invBookInfoMapper.deleteInvBookInfoByBookInfoId(bookInfoId);
    }
}
