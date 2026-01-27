package com.cheng.shop.controller;

import com.cheng.common.annotation.Log;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.shop.domain.ShopMember;
import com.cheng.shop.service.IShopMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 會員 Controller
 *
 * @author cheng
 */
@RestController
@RequestMapping("/shop/member")
@RequiredArgsConstructor
public class ShopMemberController extends BaseController {

    private final IShopMemberService memberService;

    /**
     * 查詢會員列表
     */
    @PreAuthorize("@ss.hasPermi('shop:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(ShopMember member) {
        startPage();
        List<ShopMember> list = memberService.selectMemberList(member);
        return getDataTable(list);
    }

    /**
     * 查詢會員詳情
     */
    @PreAuthorize("@ss.hasPermi('shop:member:query')")
    @GetMapping("/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId) {
        return success(memberService.selectMemberById(memberId));
    }

    /**
     * 新增會員
     */
    @PreAuthorize("@ss.hasPermi('shop:member:add')")
    @Log(title = "會員管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ShopMember member) {
        if (!memberService.checkMobileUnique(member)) {
            return error("新增會員'" + member.getNickname() + "'失敗，手機號碼已存在");
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty() && !memberService.checkEmailUnique(member)) {
            return error("新增會員'" + member.getNickname() + "'失敗，Email已存在");
        }
        int result = memberService.registerMember(member);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("memberNickname", member.getNickname());
        return ajaxResult;
    }

    /**
     * 修改會員
     */
    @PreAuthorize("@ss.hasPermi('shop:member:edit')")
    @Log(title = "會員管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ShopMember member) {
        if (!memberService.checkMobileUnique(member)) {
            return error("修改會員'" + member.getNickname() + "'失敗，手機號碼已存在");
        }
        if (member.getEmail() != null && !member.getEmail().isEmpty() && !memberService.checkEmailUnique(member)) {
            return error("修改會員'" + member.getNickname() + "'失敗，Email已存在");
        }
        int result = memberService.updateMember(member);
        AjaxResult ajaxResult = toAjax(result);
        ajaxResult.put("memberNickname", member.getNickname());
        return ajaxResult;
    }

    /**
     * 刪除會員
     */
    @PreAuthorize("@ss.hasPermi('shop:member:remove')")
    @Log(title = "會員管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{memberId}")
    public AjaxResult remove(@PathVariable Long memberId) {
        ShopMember member = memberService.selectMemberById(memberId);
        int result = memberService.deleteMemberById(memberId);
        AjaxResult ajaxResult = toAjax(result);
        if (member != null) {
            ajaxResult.put("memberNickname", member.getNickname());
        }
        return ajaxResult;
    }

    /**
     * 更新會員狀態
     */
    @PreAuthorize("@ss.hasPermi('shop:member:edit')")
    @Log(title = "會員管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult updateStatus(@RequestBody ShopMember member) {
        ShopMember existingMember = memberService.selectMemberById(member.getMemberId());
        int result = memberService.updateMemberStatus(member.getMemberId(), member.getStatus());
        AjaxResult ajaxResult = toAjax(result);
        if (existingMember != null) {
            ajaxResult.put("memberNickname", existingMember.getNickname());
            ajaxResult.put("newStatus", member.getStatus());
        }
        return ajaxResult;
    }

    /**
     * 調整會員積分
     */
    @PreAuthorize("@ss.hasPermi('shop:member:edit')")
    @Log(title = "會員管理", businessType = BusinessType.UPDATE)
    @PutMapping("/points")
    public AjaxResult adjustPoints(@RequestBody ShopMember member) {
        ShopMember existingMember = memberService.selectMemberById(member.getMemberId());
        int result;
        int points = member.getPoints();
        if (points > 0) {
            result = memberService.increasePoints(member.getMemberId(), points);
        } else {
            result = memberService.decreasePoints(member.getMemberId(), Math.abs(points));
        }
        AjaxResult ajaxResult = toAjax(result);
        if (existingMember != null) {
            ajaxResult.put("memberNickname", existingMember.getNickname());
            ajaxResult.put("pointsChange", points);
        }
        return ajaxResult;
    }
}
