package com.cheng.web.controller.system;

import com.cheng.common.annotation.Log;
import com.cheng.common.constant.PermConstants;
import com.cheng.common.core.controller.BaseController;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.enums.BusinessType;
import com.cheng.common.utils.poi.ExcelUtil;
import com.cheng.system.domain.SysPost;
import com.cheng.system.service.ISysPostService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 職位訊息操作處理
 *
 * @author cheng
 */
@RestController
@RequestMapping("/system/post")
public class SysPostController extends BaseController {
    @Autowired
    private ISysPostService postService;

    /**
     * 取得職位列表
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.LIST + "')")
    @GetMapping("/list")
    public TableDataInfo list(SysPost post) {
        startPage();
        List<SysPost> list = postService.selectPostList(post);
        return getDataTable(list);
    }

    @Log(title = "職位管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.EXPORT + "')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysPost post) {
        List<SysPost> list = postService.selectPostList(post);
        ExcelUtil<SysPost> util = new ExcelUtil<SysPost>(SysPost.class);
        util.exportExcel(response, list, "職位數據");
    }

    /**
     * 根據職位編號取得詳細訊息
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.QUERY + "')")
    @GetMapping(value = "/{postId}")
    public AjaxResult getInfo(@PathVariable Long postId) {
        return success(postService.selectPostById(postId));
    }

    /**
     * 新增職位
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.ADD + "')")
    @Log(title = "職位管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysPost post) {
        if (!postService.checkPostNameUnique(post)) {
            return error("新增職位'" + post.getPostName() + "'失敗，職位名稱已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return error("新增職位'" + post.getPostName() + "'失敗，職位編碼已存在");
        }
        post.setCreateBy(getUsername());
        return toAjax(postService.insertPost(post));
    }

    /**
     * 修改職位
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.EDIT + "')")
    @Log(title = "職位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysPost post) {
        if (!postService.checkPostNameUnique(post)) {
            return error("修改職位'" + post.getPostName() + "'失敗，職位名稱已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return error("修改職位'" + post.getPostName() + "'失敗，職位編碼已存在");
        }
        post.setUpdateBy(getUsername());
        return toAjax(postService.updatePost(post));
    }

    /**
     * 刪除職位
     */
    @PreAuthorize("@ss.hasPermi('" + PermConstants.System.Post.REMOVE + "')")
    @Log(title = "職位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    public AjaxResult remove(@PathVariable Long[] postIds) {
        return toAjax(postService.deletePostByIds(postIds));
    }

    /**
     * 取得職位選擇框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect() {
        List<SysPost> posts = postService.selectPostAll();
        return success(posts);
    }
}
