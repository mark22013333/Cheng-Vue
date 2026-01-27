package com.cheng.shop.service;

import com.cheng.shop.domain.ShopMember;

import java.util.List;

/**
 * 會員 Service 介面
 *
 * @author cheng
 */
public interface IShopMemberService {

    /**
     * 查詢會員列表
     *
     * @param member 查詢條件
     * @return 會員列表
     */
    List<ShopMember> selectMemberList(ShopMember member);

    /**
     * 根據ID查詢會員
     *
     * @param memberId 會員ID
     * @return 會員
     */
    ShopMember selectMemberById(Long memberId);

    /**
     * 根據手機號查詢會員
     *
     * @param mobile 手機號
     * @return 會員
     */
    ShopMember selectMemberByMobile(String mobile);

    /**
     * 根據Email查詢會員
     *
     * @param email Email
     * @return 會員
     */
    ShopMember selectMemberByEmail(String email);

    /**
     * 註冊會員
     *
     * @param member 會員
     * @return 影響行數
     */
    int registerMember(ShopMember member);

    /**
     * 更新會員資料
     *
     * @param member 會員
     * @return 影響行數
     */
    int updateMember(ShopMember member);

    /**
     * 更新會員狀態
     *
     * @param memberId 會員ID
     * @param status   狀態
     * @return 影響行數
     */
    int updateMemberStatus(Long memberId, String status);

    /**
     * 刪除會員
     *
     * @param memberId 會員ID
     * @return 影響行數
     */
    int deleteMemberById(Long memberId);

    /**
     * 增加會員積分
     *
     * @param memberId 會員ID
     * @param points   積分
     * @return 影響行數
     */
    int increasePoints(Long memberId, int points);

    /**
     * 扣減會員積分
     *
     * @param memberId 會員ID
     * @param points   積分
     * @return 影響行數
     */
    int decreasePoints(Long memberId, int points);

    /**
     * 檢查手機號是否唯一
     *
     * @param member 會員
     * @return 是否唯一
     */
    boolean checkMobileUnique(ShopMember member);

    /**
     * 檢查Email是否唯一
     *
     * @param member 會員
     * @return 是否唯一
     */
    boolean checkEmailUnique(ShopMember member);

    /**
     * 更新登入資訊
     *
     * @param memberId 會員ID
     * @param ip       IP地址
     */
    void updateLoginInfo(Long memberId, String ip);
}
