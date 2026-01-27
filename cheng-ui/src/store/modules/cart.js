import { defineStore } from 'pinia'
import {
  getCart,
  getCartCount,
  addToCart as addToCartApi,
  updateCartQuantity as updateQuantityApi,
  updateCartSelected,
  selectAllCart,
  removeCartItem,
  removeCartItems,
  clearCart as clearCartApi,
  mergeGuestCart as mergeGuestCartApi
} from '@/api/shop/cart'
import { getToken } from '@/utils/auth'
import { getProduct, getProductSkus } from '@/api/shop/front'

const GUEST_CART_KEY = 'guest_cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    totalQuantity: 0,
    selectedQuantity: 0,
    totalAmount: 0,
    selectedAmount: 0,
    loading: false,
    initialized: false
  }),

  getters: {
    // 是否全選
    isAllSelected: (state) => {
      if (state.items.length === 0) return false
      return state.items.every(item => item.isSelected)
    },

    // 是否有選中項目
    hasSelectedItems: (state) => {
      return state.items.some(item => item.isSelected)
    },

    // 選中的項目
    selectedItems: (state) => {
      return state.items.filter(item => item.isSelected)
    },

    // 購物車是否為空
    isEmpty: (state) => {
      return state.items.length === 0
    }
  },

  actions: {
    /**
     * 判斷是否已登入
     */
    isLoggedIn() {
      return !!getToken()
    },

    /**
     * 從伺服器載入購物車
     */
    async fetchCart() {
      if (!this.isLoggedIn()) {
        this.loadGuestCart()
        return
      }

      this.loading = true
      try {
        const response = await getCart()
        if (response.code === 200) {
          const data = response.data
          this.items = data.items || []
          this.totalQuantity = data.totalQuantity || 0
          this.selectedQuantity = data.selectedQuantity || 0
          this.totalAmount = data.totalAmount || 0
          this.selectedAmount = data.selectedAmount || 0
          this.initialized = true
        }
      } catch (error) {
        console.error('載入購物車失敗', error)
      } finally {
        this.loading = false
      }
    },

    /**
     * 取得購物車數量（輕量級）
     */
    async fetchCartCount() {
      if (!this.isLoggedIn()) {
        this.loadGuestCart()
        return
      }

      try {
        const response = await getCartCount()
        if (response.code === 200) {
          this.totalQuantity = response.data
        }
      } catch (error) {
        console.error('載入購物車數量失敗', error)
      }
    },

    /**
     * 加入購物車
     * @param {number} skuId - SKU ID
     * @param {number} quantity - 數量
     * @param {object} skuInfo - SKU 資訊（訪客模式需要）
     */
    async addToCart(skuId, quantity = 1, skuInfo = null) {
      if (!this.isLoggedIn()) {
        return this.addToGuestCart(skuId, quantity, skuInfo)
      }

      try {
        const response = await addToCartApi({ skuId, quantity })
        if (response.code === 200) {
          await this.fetchCart()
          return true
        }
        return false
      } catch (error) {
        console.error('加入購物車失敗', error)
        throw error
      }
    },

    /**
     * 訪客模式加入購物車
     */
    async addToGuestCart(skuId, quantity = 1, skuInfo = null) {
      try {
        // 如果沒有傳入 skuInfo，需要查詢
        let info = skuInfo
        if (!info) {
          info = await this.fetchSkuInfo(skuId)
          if (!info) {
            throw new Error('無法取得商品資訊')
          }
        }

        // 檢查是否已在購物車中
        const existingIndex = this.items.findIndex(item => item.skuId === skuId)
        if (existingIndex >= 0) {
          // 更新數量
          this.items[existingIndex].quantity += quantity
        } else {
          // 新增項目（使用負數作為臨時 cartId）
          const tempCartId = -Date.now()
          this.items.push({
            cartId: tempCartId,
            skuId: info.skuId,
            productId: info.productId,
            productName: info.productTitle,
            skuName: info.skuName,
            productImage: info.mainImage,
            price: info.price,
            quantity: quantity,
            isSelected: true,
            stockQuantity: 999
          })
        }

        this.recalculate()
        this.saveGuestCart()
        return true
      } catch (error) {
        console.error('加入訪客購物車失敗', error)
        throw error
      }
    },

    /**
     * 查詢 SKU 資訊（用於訪客購物車）
     */
    async fetchSkuInfo(skuId) {
      // 從快取或現有商品中尋找
      const existingItem = this.items.find(item => item.skuId === skuId)
      if (existingItem) {
        return existingItem
      }

      // 需要從 API 查詢 - 這邊需要一個新的 API 或從商品詳情取得
      // 暫時返回 null，使用時需要傳入完整 skuInfo
      return null
    },

    /**
     * 更新數量
     * @param {number} cartId - 購物車項目ID
     * @param {number} quantity - 新數量
     */
    async updateQuantity(cartId, quantity) {
      if (!this.isLoggedIn()) {
        const item = this.items.find(i => i.cartId === cartId)
        if (item) {
          item.quantity = quantity
          this.recalculate()
          this.saveGuestCart()
        }
        return true
      }

      try {
        const response = await updateQuantityApi({ cartId, quantity })
        if (response.code === 200) {
          const item = this.items.find(i => i.cartId === cartId)
          if (item) {
            item.quantity = quantity
            this.recalculate()
          }
          return true
        }
        return false
      } catch (error) {
        console.error('更新數量失敗', error)
        throw error
      }
    },

    /**
     * 更新選中狀態
     * @param {number} cartId - 購物車項目ID
     * @param {boolean} selected - 是否選中
     */
    async updateSelected(cartId, selected) {
      if (!this.isLoggedIn()) {
        const item = this.items.find(i => i.cartId === cartId)
        if (item) {
          item.isSelected = selected
          this.recalculate()
          this.saveGuestCart()
        }
        return true
      }

      try {
        const response = await updateCartSelected(cartId, selected)
        if (response.code === 200) {
          const item = this.items.find(i => i.cartId === cartId)
          if (item) {
            item.isSelected = selected
            this.recalculate()
          }
          return true
        }
        return false
      } catch (error) {
        console.error('更新選中狀態失敗', error)
        throw error
      }
    },

    /**
     * 全選/取消全選
     * @param {boolean} selected - 是否選中
     */
    async toggleSelectAll(selected) {
      if (!this.isLoggedIn()) {
        this.items.forEach(item => {
          item.isSelected = selected
        })
        this.recalculate()
        this.saveGuestCart()
        return true
      }

      try {
        const response = await selectAllCart(selected)
        if (response.code === 200) {
          this.items.forEach(item => {
            item.isSelected = selected
          })
          this.recalculate()
          return true
        }
        return false
      } catch (error) {
        console.error('全選操作失敗', error)
        throw error
      }
    },

    /**
     * 移除項目
     * @param {number} cartId - 購物車項目ID
     */
    async removeItem(cartId) {
      if (!this.isLoggedIn()) {
        this.items = this.items.filter(i => i.cartId !== cartId)
        this.recalculate()
        this.saveGuestCart()
        return true
      }

      try {
        const response = await removeCartItem(cartId)
        if (response.code === 200) {
          this.items = this.items.filter(i => i.cartId !== cartId)
          this.recalculate()
          return true
        }
        return false
      } catch (error) {
        console.error('移除項目失敗', error)
        throw error
      }
    },

    /**
     * 批量移除項目
     * @param {Array<number>} cartIds - 購物車項目ID陣列
     */
    async removeItems(cartIds) {
      if (!this.isLoggedIn()) {
        this.items = this.items.filter(i => !cartIds.includes(i.cartId))
        this.recalculate()
        this.saveGuestCart()
        return true
      }

      try {
        const response = await removeCartItems(cartIds)
        if (response.code === 200) {
          this.items = this.items.filter(i => !cartIds.includes(i.cartId))
          this.recalculate()
          return true
        }
        return false
      } catch (error) {
        console.error('批量移除失敗', error)
        throw error
      }
    },

    /**
     * 清空購物車
     */
    async clearCart() {
      if (!this.isLoggedIn()) {
        this.items = []
        this.recalculate()
        this.clearGuestCart()
        return true
      }

      try {
        const response = await clearCartApi()
        if (response.code === 200) {
          this.items = []
          this.recalculate()
          return true
        }
        return false
      } catch (error) {
        console.error('清空購物車失敗', error)
        throw error
      }
    },

    /**
     * 重新計算統計數據
     */
    recalculate() {
      let totalQty = 0
      let selectedQty = 0
      let totalAmt = 0
      let selectedAmt = 0

      for (const item of this.items) {
        const itemTotal = (item.price || 0) * (item.quantity || 0)
        totalQty += item.quantity || 0
        totalAmt += itemTotal

        if (item.isSelected) {
          selectedQty += item.quantity || 0
          selectedAmt += itemTotal
        }
      }

      this.totalQuantity = totalQty
      this.selectedQuantity = selectedQty
      this.totalAmount = totalAmt
      this.selectedAmount = selectedAmt
    },

    // ========== 訪客購物車（localStorage）==========

    /**
     * 載入訪客購物車
     */
    loadGuestCart() {
      try {
        const saved = localStorage.getItem(GUEST_CART_KEY)
        if (saved) {
          const data = JSON.parse(saved)
          this.items = data.items || []
          this.recalculate()
          this.initialized = true
        }
      } catch (e) {
        console.error('載入訪客購物車失敗', e)
      }
    },

    /**
     * 儲存訪客購物車
     */
    saveGuestCart() {
      try {
        const data = { items: this.items }
        localStorage.setItem(GUEST_CART_KEY, JSON.stringify(data))
      } catch (e) {
        console.error('儲存訪客購物車失敗', e)
      }
    },

    /**
     * 清除訪客購物車
     */
    clearGuestCart() {
      localStorage.removeItem(GUEST_CART_KEY)
    },

    /**
     * 初始化（根據登入狀態選擇載入方式）
     */
    async init() {
      if (this.isLoggedIn()) {
        await this.fetchCart()
      } else {
        this.loadGuestCart()
      }
    },

    /**
     * 登入後合併訪客購物車
     */
    async mergeGuestCartOnLogin() {
      const guestItems = this.getGuestCartItems()
      if (guestItems.length === 0) {
        return
      }

      try {
        // 呼叫後端 API 合併購物車
        const mergeData = guestItems.map(item => ({
          skuId: item.skuId,
          quantity: item.quantity
        }))

        await mergeGuestCartApi(mergeData)
        this.clearGuestCart()
        await this.fetchCart()
      } catch (error) {
        console.error('合併購物車失敗', error)
      }
    },

    /**
     * 取得訪客購物車項目（不改變狀態）
     */
    getGuestCartItems() {
      try {
        const saved = localStorage.getItem(GUEST_CART_KEY)
        if (saved) {
          const data = JSON.parse(saved)
          return data.items || []
        }
      } catch (e) {
        console.error('讀取訪客購物車失敗', e)
      }
      return []
    }
  }
})
