package com.cheng.shop.service;

import com.cheng.shop.domain.ShopArticle;

import java.util.List;

/**
 * 商城文章 Service 介面
 *
 * @author cheng
 */
public interface IShopArticleService {

    /**
     * 查詢文章列表
     *
     * @param article 查詢條件
     * @return 文章列表
     */
    List<ShopArticle> selectArticleList(ShopArticle article);

    /**
     * 根據ID查詢文章
     *
     * @param articleId 文章ID
     * @return 文章
     */
    ShopArticle selectArticleById(Long articleId);

    /**
     * 查詢已發布文章列表（前台用）
     *
     * @return 文章列表
     */
    List<ShopArticle> selectPublishedArticleList();

    /**
     * 查詢最新文章（首頁用）
     *
     * @param limit 數量限制
     * @return 文章列表
     */
    List<ShopArticle> selectLatestArticles(int limit);

    /**
     * 新增文章
     *
     * @param article 文章
     * @return 影響行數
     */
    int insertArticle(ShopArticle article);

    /**
     * 更新文章
     *
     * @param article 文章
     * @return 影響行數
     */
    int updateArticle(ShopArticle article);

    /**
     * 刪除文章
     *
     * @param articleId 文章ID
     * @return 影響行數
     */
    int deleteArticleById(Long articleId);

    /**
     * 批量刪除文章
     *
     * @param articleIds 文章ID陣列
     * @return 影響行數
     */
    int deleteArticleByIds(Long[] articleIds);

    /**
     * 增加瀏覽數
     *
     * @param articleId 文章ID
     * @return 影響行數
     */
    int increaseViewCount(Long articleId);
}
