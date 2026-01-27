package com.cheng.shop.service.impl;

import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.enums.MemberStatus;
import com.cheng.shop.mapper.ShopMemberMapper;
import com.cheng.shop.service.IShopMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 會員 Service 實作
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopMemberServiceImpl implements IShopMemberService {

    private final ShopMemberMapper memberMapper;

    @Override
    public List<ShopMember> selectMemberList(ShopMember member) {
        return memberMapper.selectMemberList(member);
    }

    @Override
    public ShopMember selectMemberById(Long memberId) {
        return memberMapper.selectMemberById(memberId);
    }

    @Override
    public ShopMember selectMemberByMobile(String mobile) {
        return memberMapper.selectMemberByMobile(mobile);
    }

    @Override
    public ShopMember selectMemberByEmail(String email) {
        return memberMapper.selectMemberByEmail(email);
    }

    @Override
    public int registerMember(ShopMember member) {
        // 加密密碼
        if (member.getPassword() != null && !member.getPassword().isEmpty()) {
            member.setPassword(SecurityUtils.encryptPassword(member.getPassword()));
        }

        // 設定預設值
        if (member.getStatus() == null) {
            member.setStatus(MemberStatus.ACTIVE.getCode());
        }
        if (member.getPoints() == null) {
            member.setPoints(0);
        }
        if (member.getLevel() == null) {
            member.setLevel("NORMAL");
        }

        member.setCreateTime(DateUtils.getNowDate());
        log.info("新會員註冊，手機: {}", member.getMobile());
        return memberMapper.insertMember(member);
    }

    @Override
    public int updateMember(ShopMember member) {
        member.setUpdateTime(DateUtils.getNowDate());
        return memberMapper.updateMember(member);
    }

    @Override
    public int updateMemberStatus(Long memberId, String status) {
        ShopMember member = new ShopMember();
        member.setMemberId(memberId);
        member.setStatus(status);
        member.setUpdateTime(DateUtils.getNowDate());
        return memberMapper.updateMember(member);
    }

    @Override
    public int deleteMemberById(Long memberId) {
        return memberMapper.deleteMemberById(memberId);
    }

    @Override
    public int increasePoints(Long memberId, int points) {
        log.info("增加會員積分，會員ID: {}, 積分: {}", memberId, points);
        return memberMapper.increasePoints(memberId, points);
    }

    @Override
    public int decreasePoints(Long memberId, int points) {
        log.info("扣減會員積分，會員ID: {}, 積分: {}", memberId, points);
        return memberMapper.decreasePoints(memberId, points);
    }

    @Override
    public boolean checkMobileUnique(ShopMember member) {
        Long memberId = member.getMemberId() == null ? -1L : member.getMemberId();
        int count = memberMapper.checkMobileUnique(member.getMobile(), memberId);
        return count == 0;
    }

    @Override
    public boolean checkEmailUnique(ShopMember member) {
        Long memberId = member.getMemberId() == null ? -1L : member.getMemberId();
        int count = memberMapper.checkEmailUnique(member.getEmail(), memberId);
        return count == 0;
    }

    @Override
    public void updateLoginInfo(Long memberId, String ip) {
        ShopMember member = new ShopMember();
        member.setMemberId(memberId);
        member.setLastLoginTime(DateUtils.getNowDate());
        member.setLastLoginIp(ip);
        memberMapper.updateMember(member);
    }
}
