package com.cheng.shop.mapper;

import com.cheng.shop.domain.ShopMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 會員 Mapper 介面
 *
 * @author cheng
 */
@Mapper
public interface ShopMemberMapper {

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
     * 新增會員
     *
     * @param member 會員
     * @return 影響行數
     */
    int insertMember(ShopMember member);

    /**
     * 更新會員
     *
     * @param member 會員
     * @return 影響行數
     */
    int updateMember(ShopMember member);

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
    int increasePoints(@Param("memberId") Long memberId, @Param("points") int points);

    /**
     * 扣減會員積分
     *
     * @param memberId 會員ID
     * @param points   積分
     * @return 影響行數
     */
    int decreasePoints(@Param("memberId") Long memberId, @Param("points") int points);

    /**
     * 檢查手機號是否唯一
     *
     * @param mobile   手機號
     * @param memberId 排除的會員ID
     * @return 數量
     */
    int checkMobileUnique(@Param("mobile") String mobile, @Param("memberId") Long memberId);

    /**
     * 檢查Email是否唯一
     *
     * @param email    Email
     * @param memberId 排除的會員ID
     * @return 數量
     */
    int checkEmailUnique(@Param("email") String email, @Param("memberId") Long memberId);
}
