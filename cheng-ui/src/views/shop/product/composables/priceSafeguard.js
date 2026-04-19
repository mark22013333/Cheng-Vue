/**
 * 商品規格（SKU）價格防呆工具
 *
 * 目的：防止使用者從庫存匯入售價異常偏低（或未定價）的物品時，
 *      直接把 $0 / $1 / $30 等試賣/測試價上架，造成虧損。
 *
 * 規則：
 *   1. 庫存現價 >= MIN_TRUST_PRICE → 信任，直接使用
 *   2. 庫存現價 < MIN_TRUST_PRICE：
 *      a. 若採購成本 > 0 → 以 round(採購成本 × MARKUP_RATIO) 推算售價
 *      b. 否則 → 回傳 0，讓後續 SKU 驗證強制使用者手動輸入
 *
 * 此函式為純函式、無副作用，可安全在多處呼叫。
 *
 * @see openspec/changes/sku-price-sanity-guard/specs/sku-price-sanity-guard/spec.md
 * @see 後端對應：ShopPriceConstants.java / ShopProductServiceImpl.validateAndEnrichSkuPricing()
 */

/**
 * 視為「可信任」的最低售價門檻。
 * 低於此值的庫存現價會被視為異常（試賣價、未定價、測試資料）。
 */
export const MIN_TRUST_PRICE = 100

/**
 * 當觸發防呆時，由採購成本推算售價的加成比例。
 * 1.2 = 20% 毛利率，為零售業保守下限，確保上架後至少不虧本。
 */
export const MARKUP_RATIO = 1.2

/**
 * 從庫存物品帶入 SKU 售價的防呆計算。
 *
 * @param {number|string|null|undefined} current  庫存物品的 currentPrice
 * @param {number|string|null|undefined} purchase 庫存物品的 purchasePrice
 * @returns {number} 防呆後的 SKU 售價（整數）
 */
export function safeSkuPriceFromInventory(current, purchase) {
  const c = Number(current) || 0
  const p = Number(purchase) || 0

  if (c >= MIN_TRUST_PRICE) return c
  if (p > 0) return Math.round(p * MARKUP_RATIO)
  return 0
}

/**
 * 判斷是否會觸發防呆調整（用於 UI 提示）。
 *
 * 注意：本函式只判斷「是否需要調整」，不管採購成本是否有效。
 * 因此 current < MIN_TRUST_PRICE 即回傳 true，即使調整結果為 0。
 *
 * @param {number|string|null|undefined} current
 * @param {number|string|null|undefined} _purchase
 * @returns {boolean}
 */
// eslint-disable-next-line no-unused-vars
export function isPriceSafeguardApplied(current, _purchase) {
  const c = Number(current) || 0
  return c < MIN_TRUST_PRICE
}

/**
 * 根據成本價計算系統推薦售價（成本 × MARKUP_RATIO，四捨五入為整數）。
 *
 * 用於：
 *   - SkuCard 的「建議售價」按鈕
 *   - 後端 price == null 時的 fallback（規則一致）
 *
 * @param {number|string|null|undefined} cost 成本價
 * @returns {number|null} 推薦售價；成本 <= 0 或無效時回傳 null
 */
export function recommendedPriceFromCost(cost) {
  const c = Number(cost) || 0
  if (c <= 0) return null
  return Math.round(c * MARKUP_RATIO)
}
