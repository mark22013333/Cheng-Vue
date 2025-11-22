/**
 * Tab 切換自動重新整理 Mixin
 * 
 * 用於處理頁面中使用 el-tabs、el-card 等元件切換時，
 * 自動重新整理對應資料的通用邏輯。
 * 
 * 使用方式：
 * 
 * 1. 在 Vue 組件中引入：
 *    import tabRefreshMixin from '@/mixins/tabRefreshMixin'
 * 
 * 2. 在組件中使用：
 *    export default {
 *      mixins: [tabRefreshMixin],
 *      // ... 其他配置
 *    }
 * 
 * 3. 在模板中綁定事件：
 *    <el-tabs @tab-click="handleTabRefresh">
 *    或
 *    <el-card @click.native="handleCardRefresh('reportType')">
 * 
 * 4. 定義刷新方法映射（必須）：
 *    data() {
 *      return {
 *        // Tab/Card 名稱對應的刷新方法
 *        refreshMethodMap: {
 *          'stock': 'loadStockData',
 *          'borrow': 'loadBorrowData',
 *          'movement': 'loadMovementData'
 *        }
 *      }
 *    }
 * 
 * 說明：
 * - handleTabRefresh: 用於 el-tabs 的 @tab-click 事件
 * - handleCardRefresh: 用於 el-card 的 @click.native 事件
 * - refreshMethodMap: 定義每個 tab/card 對應的刷新方法名稱
 */

export default {
  methods: {
    /**
     * Tab 切換時的重新整理處理
     * 用於 el-tabs 的 @tab-click 事件
     * 
     * @param {Object} tab - Element UI 的 tab 對象，包含 name 等屬性
     */
    handleTabRefresh(tab) {
      const tabName = tab.name;
      this._executeRefresh(tabName, 'Tab');
    },

    /**
     * Card 點擊時的重新整理處理
     * 用於 el-card 的 @click.native 事件
     * 
     * @param {String} cardName - Card 的識別名稱
     */
    handleCardRefresh(cardName) {
      this._executeRefresh(cardName, 'Card');
    },

    /**
     * 執行重新整理的核心方法（內部使用）
     * 
     * @param {String} name - Tab 或 Card 的名稱
     * @param {String} type - 類型：'Tab' 或 'Card'
     * @private
     */
    _executeRefresh(name, type) {
      if (!this.refreshMethodMap) {
        console.warn(`[tabRefreshMixin] refreshMethodMap 未定義，無法執行 ${type} 重新整理`);
        return;
      }

      const methodName = this.refreshMethodMap[name];
      
      if (!methodName) {
        console.debug(`[tabRefreshMixin] ${type} "${name}" 沒有對應的刷新方法，跳過`);
        return;
      }

      if (typeof this[methodName] !== 'function') {
        console.error(`[tabRefreshMixin] 方法 "${methodName}" 不存在或不是函數`);
        return;
      }

      // 執行刷新方法
      console.debug(`[tabRefreshMixin] 執行 ${type} "${name}" 的刷新方法: ${methodName}`);
      this[methodName]();
    },

    /**
     * 批量重新整理多個項目
     * 
     * @param {Array<String>} names - 要重新整理的 tab/card 名稱陣列
     */
    batchRefresh(names) {
      if (!Array.isArray(names)) {
        console.error('[tabRefreshMixin] batchRefresh 參數必須是陣列');
        return;
      }

      names.forEach(name => {
        this._executeRefresh(name, 'Batch');
      });
    },

    /**
     * 重新整理所有已定義的項目
     */
    refreshAll() {
      if (!this.refreshMethodMap) {
        console.warn('[tabRefreshMixin] refreshMethodMap 未定義，無法執行全部重新整理');
        return;
      }

      const names = Object.keys(this.refreshMethodMap);
      this.batchRefresh(names);
    }
  }
}
