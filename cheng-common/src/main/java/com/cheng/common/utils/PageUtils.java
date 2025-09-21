package com.cheng.common.utils;

import com.cheng.common.core.page.PageDomain;
import com.cheng.common.core.page.TableSupport;
import com.cheng.common.utils.sql.SqlUtil;
import com.github.pagehelper.PageHelper;

/**
 * 分頁工具類
 *
 * @author cheng
 */
public class PageUtils extends PageHelper {
    /**
     * 設定請求分頁數據
     */
    public static void startPage() {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Boolean reasonable = pageDomain.getReasonable();
        PageHelper.startPage(pageNum, pageSize, orderBy).setReasonable(reasonable);
    }

    /**
     * 清理分頁的執行緒變數
     */
    public static void clearPage() {
        PageHelper.clearPage();
    }
}
