<template>
  <div class="cart-page">
    <!-- 頁面標題 -->
    <div class="cart-header">
      <h1>購物車</h1>
      <span class="cart-count" v-if="cartStore.totalQuantity > 0">
        共 {{ cartStore.totalQuantity }} 件商品
      </span>
    </div>

    <!-- 購物車內容 -->
    <div class="cart-content" v-loading="cartStore.loading">
      <!-- 空購物車 -->
      <div v-if="cartStore.isEmpty" class="cart-empty">
        <el-empty description="購物車是空的">
          <el-button type="primary" @click="goShopping">去逛逛</el-button>
        </el-empty>
      </div>

      <!-- 有商品時 -->
      <template v-else>
        <!-- 購物車列表 -->
        <div class="cart-list">
          <!-- 全選 -->
          <div class="cart-list-header">
            <el-checkbox
              :model-value="cartStore.isAllSelected"
              @change="handleSelectAll"
            >
              全選
            </el-checkbox>
            <span class="header-product">商品資訊</span>
            <span class="header-price">單價</span>
            <span class="header-quantity">數量</span>
            <span class="header-subtotal">小計</span>
            <span class="header-action">操作</span>
          </div>

          <!-- 商品列表 -->
          <div class="cart-items">
            <div
              v-for="item in cartStore.items"
              :key="item.cartId"
              class="cart-item"
            >
              <!-- 選擇框 -->
              <el-checkbox
                :model-value="item.isSelected"
                @change="(val) => handleSelectItem(item.cartId, val)"
              />

              <!-- 商品資訊 -->
              <div class="item-info">
                <img
                  :src="getImageUrl(item.skuImage || item.productImage)"
                  :alt="item.productName"
                  class="item-image"
                  @click="goProductDetail(item.productId)"
                />
                <div class="item-detail">
                  <div class="item-name" @click="goProductDetail(item.productId)">
                    {{ item.productName }}
                  </div>
                  <div class="item-sku" v-if="item.skuName">
                    規格：{{ item.skuName }}
                  </div>
                  <div class="item-stock" v-if="item.stockQuantity <= 5">
                    <el-tag type="warning" size="small">
                      僅剩 {{ item.stockQuantity }} 件
                    </el-tag>
                  </div>
                </div>
              </div>

              <!-- 單價 -->
              <div class="item-price">
                <span class="price">${{ formatPrice(item.price) }}</span>
              </div>

              <!-- 數量 -->
              <div class="item-quantity">
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="item.stockQuantity || 999"
                  size="small"
                  @change="(val) => handleQuantityChange(item.cartId, val)"
                />
              </div>

              <!-- 小計 -->
              <div class="item-subtotal">
                <span class="subtotal-price">
                  ${{ formatPrice(item.price * item.quantity) }}
                </span>
              </div>

              <!-- 操作 -->
              <div class="item-action">
                <el-button
                  type="danger"
                  text
                  @click="handleRemoveItem(item.cartId)"
                >
                  刪除
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 結算欄 -->
        <div class="cart-footer">
          <div class="footer-left">
            <el-checkbox
              :model-value="cartStore.isAllSelected"
              @change="handleSelectAll"
            >
              全選
            </el-checkbox>
            <el-button type="danger" text @click="handleRemoveSelected" :disabled="!cartStore.hasSelectedItems">
              刪除選中
            </el-button>
            <el-button text @click="handleClearCart">
              清空購物車
            </el-button>
          </div>
          <div class="footer-right">
            <div class="selected-info">
              已選 <span class="highlight">{{ cartStore.selectedQuantity }}</span> 件商品
            </div>
            <div class="total-price">
              合計：<span class="price">${{ formatPrice(cartStore.selectedAmount) }}</span>
            </div>
            <el-button
              type="primary"
              size="large"
              :disabled="!cartStore.hasSelectedItems"
              @click="goCheckout"
            >
              去結帳
            </el-button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useCartStore } from '@/store/modules/cart'
import { formatPrice as formatPriceUtil } from '@/utils/cheng'

const router = useRouter()
const cartStore = useCartStore()

onMounted(() => {
  cartStore.fetchCart()
})

function getImageUrl(url) {
  if (!url) return '/placeholder-image.png'
  if (url.startsWith('http')) return url
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}

function formatPrice(price) {
  return formatPriceUtil(price, 2)
}

function goShopping() {
  router.push('/products')
}

function goProductDetail(productId) {
  router.push(`/product/${productId}`)
}

function goCheckout() {
  router.push('/checkout')
}

async function handleSelectAll(selected) {
  try {
    await cartStore.toggleSelectAll(selected)
  } catch (error) {
    ElMessage.error('操作失敗')
  }
}

async function handleSelectItem(cartId, selected) {
  try {
    await cartStore.updateSelected(cartId, selected)
  } catch (error) {
    ElMessage.error('操作失敗')
  }
}

async function handleQuantityChange(cartId, quantity) {
  try {
    await cartStore.updateQuantity(cartId, quantity)
  } catch (error) {
    ElMessage.error(error.message || '更新數量失敗')
    // 重新載入以恢復正確數量
    await cartStore.fetchCart()
  }
}

async function handleRemoveItem(cartId) {
  try {
    await ElMessageBox.confirm('確定要刪除此商品嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeItem(cartId)
    ElMessage.success('刪除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

async function handleRemoveSelected() {
  try {
    const selectedIds = cartStore.selectedItems.map(item => item.cartId)
    if (selectedIds.length === 0) {
      ElMessage.warning('請先選擇要刪除的商品')
      return
    }
    await ElMessageBox.confirm(`確定要刪除選中的 ${selectedIds.length} 件商品嗎？`, '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.removeItems(selectedIds)
    ElMessage.success('刪除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('刪除失敗')
    }
  }
}

async function handleClearCart() {
  try {
    await ElMessageBox.confirm('確定要清空購物車嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.clearCart()
    ElMessage.success('購物車已清空')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清空失敗')
    }
  }
}
</script>

<style scoped>
.cart-page {
  background: var(--mall-card-bg, #ffffff);
  border-radius: 12px;
  padding: 24px;
  min-height: 500px;
}

.cart-header {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.cart-header h1 {
  font-size: 24px;
  color: var(--mall-text-primary, #303133);
  margin: 0;
}

.cart-count {
  font-size: 14px;
  color: var(--mall-text-secondary, #606266);
}

.cart-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 購物車列表 */
.cart-list {
  margin-bottom: 24px;
}

.cart-list-header {
  display: grid;
  grid-template-columns: 40px 1fr 120px 150px 120px 80px;
  align-items: center;
  padding: 16px 20px;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
  margin-bottom: 16px;
}

.header-product {
  padding-left: 20px;
}

.header-price,
.header-quantity,
.header-subtotal,
.header-action {
  text-align: center;
}

.cart-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: grid;
  grid-template-columns: 40px 1fr 120px 150px 120px 80px;
  align-items: center;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
  transition: all 0.3s;
}

.cart-item:hover {
  background: #f0f2f5;
}

/* 商品資訊 */
.item-info {
  display: flex;
  gap: 16px;
  padding-left: 20px;
}

.item-image {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  transition: transform 0.3s;
}

.item-image:hover {
  transform: scale(1.05);
}

.item-detail {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}

.item-name {
  font-size: 14px;
  color: #303133;
  cursor: pointer;
  line-height: 1.4;
  max-width: 300px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.item-name:hover {
  color: var(--mall-primary, #409eff);
}

.item-sku {
  font-size: 12px;
  color: #909399;
}

/* 價格 */
.item-price,
.item-subtotal {
  text-align: center;
}

.item-price .price {
  font-size: 14px;
  color: #606266;
}

.item-subtotal .subtotal-price {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

/* 數量 */
.item-quantity {
  display: flex;
  justify-content: center;
}

/* 操作 */
.item-action {
  text-align: center;
}

/* 結算欄 */
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #f5f7fa;
  border-radius: 8px;
  position: sticky;
  bottom: 24px;
}

.footer-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.footer-right {
  display: flex;
  align-items: center;
  gap: 24px;
}

.selected-info {
  font-size: 14px;
  color: #606266;
}

.selected-info .highlight {
  color: var(--mall-primary, #409eff);
  font-weight: 600;
}

.total-price {
  font-size: 14px;
  color: #606266;
}

.total-price .price {
  font-size: 24px;
  font-weight: 600;
  color: #f56c6c;
}

/* 響應式 */
@media (max-width: 768px) {
  .cart-list-header {
    display: none;
  }

  .cart-item {
    grid-template-columns: 1fr;
    gap: 16px;
    padding: 16px;
  }

  .cart-item > :first-child {
    position: absolute;
    top: 16px;
    left: 16px;
  }

  .item-info {
    padding-left: 0;
  }

  .item-image {
    width: 60px;
    height: 60px;
  }

  .item-price,
  .item-subtotal,
  .item-quantity,
  .item-action {
    text-align: left;
  }

  .cart-footer {
    flex-direction: column;
    gap: 16px;
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    border-radius: 16px 16px 0 0;
    z-index: 100;
  }

  .footer-left,
  .footer-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
