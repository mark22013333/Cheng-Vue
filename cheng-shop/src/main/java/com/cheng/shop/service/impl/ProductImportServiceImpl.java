package com.cheng.shop.service.impl;

import com.cheng.crawler.domain.CrawlImportLog;
import com.cheng.crawler.dto.KtunivalProductDTO;
import com.cheng.crawler.dto.ProductCsvRow;
import com.cheng.crawler.mapper.CrawlImportLogMapper;
import com.cheng.crawler.service.KtunivalCrawlerService;
import com.cheng.crawler.utils.ImageDownloadUtil;
import com.cheng.crawler.utils.ProductCsvReader;
import com.cheng.shop.domain.ShopCategory;
import com.cheng.shop.domain.ShopProduct;
import com.cheng.shop.domain.ShopProductSku;
import com.cheng.shop.mapper.ShopCategoryMapper;
import com.cheng.shop.mapper.ShopProductMapper;
import com.cheng.shop.mapper.ShopProductSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 商品匯入服務
 * CSV 讀取 → 爬蟲取得圖片和分類 → 寫入商品表
 *
 * @author cheng
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImportServiceImpl {

    private final KtunivalCrawlerService crawlerService;
    private final ShopProductMapper productMapper;
    private final ShopProductSkuMapper skuMapper;
    private final ShopCategoryMapper categoryMapper;
    private final CrawlImportLogMapper importLogMapper;

    @Value("${cheng.profile}")
    private String uploadPath;

    /**
     * CSV 來源為相對檔名時使用的基準目錄；HTTP(S) URL 來源不受此影響。
     */
    @Value("${cheng.import.base-path:}")
    private String importBasePath;

    private static final String CAR_CATEGORY_NAME = "汽車類";
    private final Map<String, Long> categoryCache = new HashMap<>();

    /**
     * @param csvSource CSV 來源：HTTP(S) URL（Google Sheets 發布連結）或相對於
     *                  {@code cheng.import.base-path} 的檔名
     */
    public String executeImport(String csvSource) {
        String batchId = UUID.randomUUID().toString().substring(0, 8);
        log.info("========== 開始商品匯入，批次: {}，來源: {} ==========", batchId, csvSource);

        int success = 0, skipped = 0, failed = 0;

        try {
            List<ProductCsvRow> rows = ProductCsvReader.read(csvSource, importBasePath);
            log.info("CSV 讀取完成，共 {} 筆", rows.size());

            Long carCategoryId = findCarCategoryId();
            if (carCategoryId == null) {
                return "匯入失敗：找不到「" + CAR_CATEGORY_NAME + "」分類";
            }

            for (int i = 0; i < rows.size(); i++) {
                ProductCsvRow row = rows.get(i);
                log.info("處理第 {}/{} 筆: {} ({})", i + 1, rows.size(), row.getProductName(), row.getBarcode());

                try {
                    String result = importSingleProduct(batchId, row, carCategoryId);
                    switch (result) {
                        case "SUCCESS" -> success++;
                        case "SKIPPED" -> skipped++;
                        default -> failed++;
                    }
                } catch (Exception e) {
                    failed++;
                    log.error("匯入失敗: {} - {}", row.getBarcode(), e.getMessage());
                    saveLog(batchId, row, CrawlImportLog.STATUS_ERROR, null, null, e.getMessage());
                }

                if (i < rows.size() - 1) {
                    sleep(1500);
                }
            }
        } catch (Exception e) {
            log.error("匯入流程異常", e);
            return "匯入流程異常: " + e.getMessage();
        }

        String summary = String.format("匯入完成 [批次: %s] — 成功: %d, 跳過: %d, 失敗: %d", batchId, success, skipped, failed);
        log.info("========== {} ==========", summary);
        return summary;
    }

    @Transactional
    protected String importSingleProduct(String batchId, ProductCsvRow row, Long carCategoryId) throws Exception {
        // 價格防呆：成本價 > 售價（九折價）時跳過
        if (row.getCostPrice() != null && row.getDiscountPrice() != null
                && row.getCostPrice().compareTo(row.getDiscountPrice()) > 0) {
            String msg = String.format("成本價(%s) > 售價(%s)", row.getCostPrice(), row.getDiscountPrice());
            log.warn("價格防呆阻擋: {} - {}", row.getBarcode(), msg);
            saveLog(batchId, row, CrawlImportLog.STATUS_ERROR, null, null, msg);
            return "FAILED";
        }

        // Upsert：檢查是否已存在
        ShopProductSku existingSku = skuMapper.selectSkuByBarcode(row.getBarcode());
        if (existingSku != null) {
            return updateExistingProduct(batchId, row, existingSku);
        }

        // 新增流程：爬蟲搜尋
        KtunivalProductDTO crawled = crawlerService.searchByBarcode(row.getBarcode());

        if (crawled.getResultCount() == 0) {
            saveLog(batchId, row, CrawlImportLog.STATUS_NOT_FOUND, null, null, null);
            return "NOT_FOUND";
        }
        if (crawled.getResultCount() > 1) {
            saveLog(batchId, row, CrawlImportLog.STATUS_MULTIPLE, null, null,
                    "搜尋到 " + crawled.getResultCount() + " 筆");
            return "MULTIPLE";
        }

        // 下載圖片
        String localImagePath = "";
        if (crawled.getImageUrl() != null && !crawled.getImageUrl().isEmpty()) {
            try {
                String savePath = uploadPath + "/upload/product";
                String fileName = "prostaff_" + row.getProductCode() + "_" + System.currentTimeMillis();
                localImagePath = ImageDownloadUtil.downloadImage(crawled.getImageUrl(), savePath, fileName);
                if (localImagePath != null && !localImagePath.isEmpty()) {
                    localImagePath = localImagePath.replace(uploadPath, "/profile");
                }
            } catch (Exception e) {
                log.warn("圖片下載失敗: {} - {}", crawled.getImageUrl(), e.getMessage());
                saveLog(batchId, row, CrawlImportLog.STATUS_IMG_FAIL, null, crawled.getDetailUrl(), e.getMessage());
                localImagePath = "";
            }
        }

        // 查找/建立分類
        String categoryName = crawled.getCategory();
        Long categoryId = carCategoryId;
        if (categoryName != null && !categoryName.isEmpty()) {
            categoryId = findOrCreateSubCategory(carCategoryId, categoryName);
        }

        // 寫入 shop_product
        ShopProduct product = new ShopProduct();
        product.setCategoryId(categoryId);
        product.setTitle(row.getProductName());
        product.setMainImage(localImagePath != null ? localImagePath : "");
        product.setDescription(crawled.getDescription());
        product.setPrice(row.getDiscountPrice());
        product.setOriginalPrice(row.getRetailPrice());
        product.setSalePrice(null);
        product.setIsNew(true);
        product.setIsHot(false);
        product.setIsRecommend(false);
        product.setSortOrder(0);
        product.setStatus("DRAFT");
        product.setCreateBy("import");
        productMapper.insertProduct(product);

        // 寫入 shop_product_sku
        ShopProductSku sku = new ShopProductSku();
        sku.setProductId(product.getProductId());
        sku.setSkuCode(row.getProductCode());
        sku.setBarcode(row.getBarcode());
        sku.setSkuName(row.getSpec());
        sku.setCostPrice(row.getCostPrice());
        sku.setPrice(row.getDiscountPrice());
        sku.setOriginalPrice(row.getRetailPrice());
        sku.setSalePrice(null);
        sku.setStockQuantity(10);
        sku.setSalesCount(0);
        sku.setSortOrder(0);
        sku.setStatus("ENABLED");
        sku.setCreateBy("import");
        skuMapper.insertSku(sku);

        saveLog(batchId, row, CrawlImportLog.STATUS_SUCCESS, product.getProductId(), crawled.getDetailUrl(), null);
        log.info("商品新增成功: {} → productId={}", row.getProductName(), product.getProductId());
        return "SUCCESS";
    }

    /**
     * 更新已存在的商品（Upsert 的 Update 部分）
     * 更新價格、庫存、規格名稱，不重新爬蟲
     */
    private String updateExistingProduct(String batchId, ProductCsvRow row, ShopProductSku existingSku) {
        log.info("條碼 {} 已存在（SKU ID: {}），執行更新", row.getBarcode(), existingSku.getSkuId());

        // 更新 SKU：價格、庫存、規格
        existingSku.setCostPrice(row.getCostPrice());
        existingSku.setPrice(row.getDiscountPrice());
        existingSku.setOriginalPrice(row.getRetailPrice());
        existingSku.setSalePrice(null);
        existingSku.setSkuName(row.getSpec());
        if (existingSku.getStockQuantity() == null || existingSku.getStockQuantity() == 0) {
            existingSku.setStockQuantity(10);
        }
        existingSku.setUpdateBy("import");
        skuMapper.updateSku(existingSku);

        // 更新 Product：價格
        ShopProduct product = new ShopProduct();
        product.setProductId(existingSku.getProductId());
        product.setPrice(row.getDiscountPrice());
        product.setOriginalPrice(row.getRetailPrice());
        product.setSalePrice(null);
        product.setUpdateBy("import");
        productMapper.updateProduct(product);

        saveLog(batchId, row, CrawlImportLog.STATUS_SUCCESS, existingSku.getProductId(), null, "更新已存在商品");
        log.info("商品更新成功: {} → productId={}", row.getProductName(), existingSku.getProductId());
        return "SUCCESS";
    }

    private Long findCarCategoryId() {
        ShopCategory query = new ShopCategory();
        query.setName(CAR_CATEGORY_NAME);
        List<ShopCategory> categories = categoryMapper.selectCategoryList(query);
        if (categories.isEmpty()) {
            log.error("找不到「{}」分類", CAR_CATEGORY_NAME);
            return null;
        }
        return categories.getFirst().getCategoryId();
    }

    private Long findOrCreateSubCategory(Long parentId, String name) {
        String cacheKey = parentId + ":" + name;
        if (categoryCache.containsKey(cacheKey)) {
            return categoryCache.get(cacheKey);
        }

        List<ShopCategory> children = categoryMapper.selectCategoryByParentId(parentId);
        for (ShopCategory child : children) {
            if (name.equals(child.getName())) {
                categoryCache.put(cacheKey, child.getCategoryId());
                return child.getCategoryId();
            }
        }

        ShopCategory newCategory = new ShopCategory();
        newCategory.setParentId(parentId);
        newCategory.setName(name);
        newCategory.setSortOrder(0);
        newCategory.setStatus("ENABLED");
        newCategory.setCreateBy("import");
        categoryMapper.insertCategory(newCategory);
        log.info("建立新子分類: {} → categoryId={}", name, newCategory.getCategoryId());

        categoryCache.put(cacheKey, newCategory.getCategoryId());
        return newCategory.getCategoryId();
    }

    private void saveLog(String batchId, ProductCsvRow row, String status, Long productId, String detailUrl, String errorMessage) {
        CrawlImportLog logEntry = CrawlImportLog.of(batchId, row.getBarcode(), row.getProductName(), status);
        logEntry.setProductId(productId);
        logEntry.setDetailUrl(detailUrl);
        logEntry.setErrorMessage(errorMessage);
        importLogMapper.insertLog(logEntry);
    }

    private void sleep(long ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
