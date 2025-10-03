package com.cheng.system.mapper;

import com.cheng.system.domain.InvBookInfo;

import java.util.List;

/**
 * 書籍資訊 數據層
 *
 * @author cheng
 */
public interface InvBookInfoMapper {
    /**
     * 查詢書籍資訊
     *
     * @param bookInfoId 書籍資訊主鍵
     * @return 書籍資訊
     */
    InvBookInfo selectInvBookInfoByBookInfoId(Long bookInfoId);

    /**
     * 根據 ISBN 查詢書籍資訊
     *
     * @param isbn ISBN
     * @return 書籍資訊
     */
    InvBookInfo selectInvBookInfoByIsbn(String isbn);

    /**
     * 根據物品ID查詢書籍資訊
     *
     * @param itemId 物品ID
     * @return 書籍資訊
     */
    InvBookInfo selectInvBookInfoByItemId(Long itemId);

    /**
     * 查詢書籍資訊列表
     *
     * @param invBookInfo 書籍資訊
     * @return 書籍資訊集合
     */
    List<InvBookInfo> selectInvBookInfoList(InvBookInfo invBookInfo);

    /**
     * 新增書籍資訊
     *
     * @param invBookInfo 書籍資訊
     * @return 結果
     */
    int insertInvBookInfo(InvBookInfo invBookInfo);

    /**
     * 修改書籍資訊
     *
     * @param invBookInfo 書籍資訊
     * @return 結果
     */
    int updateInvBookInfo(InvBookInfo invBookInfo);

    /**
     * 刪除書籍資訊
     *
     * @param bookInfoId 書籍資訊主鍵
     * @return 結果
     */
    int deleteInvBookInfoByBookInfoId(Long bookInfoId);

    /**
     * 批量刪除書籍資訊
     *
     * @param bookInfoIds 需要刪除的資料主鍵集合
     * @return 結果
     */
    int deleteInvBookInfoByBookInfoIds(Long[] bookInfoIds);
}
