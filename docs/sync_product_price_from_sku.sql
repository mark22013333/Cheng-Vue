-- =============================================
-- V45: 同步商品主表價格與 SKU 最低價格
--
-- 修正 shop_product.price / original_price 可能與 SKU 不一致的問題，
-- 確保列表頁和詳情頁顯示相同的價格。
-- =============================================

-- 更新 price 為啟用 SKU 中的最低價格
UPDATE shop_product p
INNER JOIN (
    SELECT product_id,
           MIN(price) AS min_price
    FROM shop_product_sku
    WHERE status IN ('ENABLED', '1')
      AND price IS NOT NULL
    GROUP BY product_id
) s ON p.product_id = s.product_id
SET p.price = s.min_price
WHERE p.price IS NULL
   OR p.price != s.min_price;

-- 更新 original_price 為啟用 SKU 中的最低原價
UPDATE shop_product p
INNER JOIN (
    SELECT product_id,
           MIN(original_price) AS min_original_price
    FROM shop_product_sku
    WHERE status IN ('ENABLED', '1')
      AND original_price IS NOT NULL
    GROUP BY product_id
) s ON p.product_id = s.product_id
SET p.original_price = s.min_original_price
WHERE p.original_price IS NULL
   OR p.original_price != s.min_original_price;
