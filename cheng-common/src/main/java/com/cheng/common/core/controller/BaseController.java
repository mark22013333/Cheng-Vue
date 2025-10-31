package com.cheng.common.core.controller;

import com.cheng.common.constant.HttpStatus;
import com.cheng.common.core.domain.AjaxResult;
import com.cheng.common.core.domain.model.LoginUser;
import com.cheng.common.core.page.PageDomain;
import com.cheng.common.core.page.TableDataInfo;
import com.cheng.common.core.page.TableSupport;
import com.cheng.common.utils.DateUtils;
import com.cheng.common.utils.PageUtils;
import com.cheng.common.utils.SecurityUtils;
import com.cheng.common.utils.StringUtils;
import com.cheng.common.utils.sql.SqlUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;

/**
 * web層共用數據處理
 *
 * @author cheng
 */
public class BaseController {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 將前台傳遞過來的日期格式的字串，自動轉化為Date類型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Date 類型轉換
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 設定請求分頁數據
     */
    protected void startPage() {
        PageUtils.startPage();
    }

    /**
     * 設定請求排序數據
     */
    protected void startOrderBy() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy())) {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分頁的執行緒變數
     */
    protected void clearPage() {
        PageUtils.clearPage();
    }

    /**
     * 響應請求分頁數據
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    protected TableDataInfo getDataTable(List<?> list) {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查詢成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    public AjaxResult success() {
        return AjaxResult.success();
    }

    /**
     * 返回失敗訊息
     */
    public AjaxResult error() {
        return AjaxResult.error();
    }

    /**
     * 返回成功訊息
     */
    public AjaxResult success(String message) {
        return AjaxResult.success(message);
    }

    /**
     * 返回成功訊息
     */
    public AjaxResult success(Object data) {
        return AjaxResult.success(data);
    }

    /**
     * 返回失敗訊息
     */
    public AjaxResult error(String message) {
        return AjaxResult.error(message);
    }

    public AjaxResult unauthorized(String message) {
        return AjaxResult.unauthorized(message);
    }

    public AjaxResult notFound(String message) {
        return AjaxResult.notFound(message);
    }

    /**
     * 返回警告訊息
     */
    public AjaxResult warn(String message) {
        return AjaxResult.warn(message);
    }

    /**
     * 響應返回結果
     *
     * @param rows 影響行數
     * @return 操作結果
     */
    protected AjaxResult toAjax(int rows) {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 響應返回結果
     *
     * @param result 結果
     * @return 操作結果
     */
    protected AjaxResult toAjax(boolean result) {
        return result ? success() : error();
    }

    /**
     * 頁面跳轉
     */
    public String redirect(String url) {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 取得使用者暫存訊息
     */
    public LoginUser getLoginUser() {
        return SecurityUtils.getLoginUser();
    }

    /**
     * 取得登入使用者id
     */
    public Long getUserId() {
        return getLoginUser().getUserId();
    }

    /**
     * 取得登入部門id
     */
    public Long getDeptId() {
        return getLoginUser().getDeptId();
    }

    /**
     * 取得登入使用者名
     */
    public String getUsername() {
        return getLoginUser().getUsername();
    }
}
