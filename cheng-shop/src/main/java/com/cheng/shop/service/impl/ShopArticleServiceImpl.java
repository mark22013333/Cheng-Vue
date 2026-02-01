package com.cheng.shop.service.impl;

import com.cheng.shop.domain.ShopArticle;
import com.cheng.shop.mapper.ShopArticleMapper;
import com.cheng.shop.service.IShopArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商城文章 Service 實作
 *
 * @author cheng
 */
@Service
@RequiredArgsConstructor
public class ShopArticleServiceImpl implements IShopArticleService {

    private final ShopArticleMapper articleMapper;

    @Override
    public List<ShopArticle> selectArticleList(ShopArticle article) {
        return articleMapper.selectArticleList(article);
    }

    @Override
    public ShopArticle selectArticleById(Long articleId) {
        return articleMapper.selectArticleById(articleId);
    }

    @Override
    public List<ShopArticle> selectPublishedArticleList() {
        return articleMapper.selectPublishedArticleList();
    }

    @Override
    public List<ShopArticle> selectLatestArticles(int limit) {
        return articleMapper.selectLatestArticles(limit);
    }

    @Override
    public int insertArticle(ShopArticle article) {
        return articleMapper.insertArticle(article);
    }

    @Override
    public int updateArticle(ShopArticle article) {
        return articleMapper.updateArticle(article);
    }

    @Override
    public int deleteArticleById(Long articleId) {
        return articleMapper.deleteArticleById(articleId);
    }

    @Override
    public int deleteArticleByIds(Long[] articleIds) {
        return articleMapper.deleteArticleByIds(articleIds);
    }

    @Override
    public int increaseViewCount(Long articleId) {
        return articleMapper.increaseViewCount(articleId);
    }
}
