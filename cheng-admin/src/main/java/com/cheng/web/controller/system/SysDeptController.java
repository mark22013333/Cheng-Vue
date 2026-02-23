package com.cheng.web.controller.system;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.constant.UserConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.entity.SysDept;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.StringUtils;
import com.cheng.system.service.ISysDeptService;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部門訊息
 *
 * @author cheng
 */
@RestController
@RequestMapping("/system/dept")
public class SysDeptController extends BaseController {
    @Autowired
    private ISysDeptService deptService;

    /**
     * 取得部門列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.LIST + "')")
    @GetMapping("/list")
    public AjaxResult list(SysDept dept) {
        List<SysDept> depts = deptService.selectDeptList(dept);
        return success(depts);
    }

    /**
     * 查詢部門列表（排除節點）
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.LIST + "')")
    @GetMapping("/list/exclude/{deptId}")
    public AjaxResult excludeChild(@PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDept> depts = deptService.selectDeptList(new SysDept());
        depts.removeIf(d -> d.getDeptId().intValue() == deptId || ArrayUtils.contains(StringUtils.split(d.getAncestors(), ","), deptId + ""));
        return success(depts);
    }

    /**
     * 根據部門編號取得詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.QUERY + "')")
    @GetMapping(value = "/{deptId}")
    public AjaxResult getInfo(@PathVariable Long deptId) {
        deptService.checkDeptDataScope(deptId);
        return success(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部門
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.ADD + "')")
    @Log(title = "部門管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysDept dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            return error("新增部門'" + dept.getDeptName() + "'失敗，部門名稱已存在");
        }
        dept.setCreateBy(getUsername());
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部門
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.EDIT + "')")
    @Log(title = "部門管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysDept dept) {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept)) {
            return error("修改部門'" + dept.getDeptName() + "'失敗，部門名稱已存在");
        } else if (dept.getParentId().equals(deptId)) {
            return error("修改部門'" + dept.getDeptName() + "'失敗，上級部門不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && deptService.selectNormalChildrenDeptById(deptId) > 0) {
            return error("該部門包含未停用的子部門！");
        }
        dept.setUpdateBy(getUsername());
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 刪除部門
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Dept.REMOVE + "')")
    @Log(title = "部門管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    public AjaxResult remove(@PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return warn("存在下級部門,不允許刪除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return warn("部門存在使用者,不允許刪除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
