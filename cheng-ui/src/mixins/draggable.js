/**
 * 可拖動元件 Mixin
 * 
 * 使用方式：
 * 1. 在元件中引入：import DraggableMixin from '@/mixins/draggable'
 * 2. 加入 mixins: [DraggableMixin]
 * 3. 在模板中綁定：
 *    <div 
 *      :style="buttonPosition" 
 *      @touchstart="handleDragStart"
 *      @touchmove="handleDragMove"
 *      @touchend="handleDragEnd">
 *    </div>
 */
export default {
  data() {
    return {
      // 按鈕位置 (距離右下角的距離)
      buttonX: 20,
      buttonY: 100,
      // 拖動相關
      isDragging: false,
      dragStartX: 0,
      dragStartY: 0,
      dragStartButtonX: 0,
      dragStartButtonY: 0
    }
  },
  
  computed: {
    /** 按鈕位置樣式 */
    buttonPosition() {
      return {
        left: `${this.buttonX}px`,
        bottom: `${this.buttonY}px`,
        transition: this.isDragging ? 'none' : 'all 0.3s ease'
      }
    }
  },
  
  methods: {
    /** 開始拖動 */
    handleDragStart(e) {
      this.isDragging = true
      const touch = e.touches[0]
      this.dragStartX = touch.clientX
      this.dragStartY = touch.clientY
      this.dragStartButtonX = this.buttonX
      this.dragStartButtonY = this.buttonY
      
      // 防止頁面滾動
      e.preventDefault()
    },
    
    /** 拖動中 */
    handleDragMove(e) {
      if (!this.isDragging) return
      
      const touch = e.touches[0]
      const deltaX = touch.clientX - this.dragStartX  // 使用 left，往右是正值
      const deltaY = touch.clientY - this.dragStartY  // 往下是正值，但 bottom 要反向
      
      // 計算新位置
      let newX = this.dragStartButtonX + deltaX
      let newY = this.dragStartButtonY - deltaY  // bottom 方向相反
      
      // 限制範圍（不要移出螢幕）
      const maxX = window.innerWidth - 60
      const maxY = window.innerHeight - 60
      
      newX = Math.max(10, Math.min(newX, maxX))
      newY = Math.max(10, Math.min(newY, maxY))
      
      this.buttonX = newX
      this.buttonY = newY
      
      // 防止頁面滾動
      e.preventDefault()
    },
    
    /** 結束拖動 */
    handleDragEnd(e) {
      // 如果移動距離很小，視為點擊
      const touch = e.changedTouches[0]
      const distance = Math.sqrt(
        Math.pow(touch.clientX - this.dragStartX, 2) +
        Math.pow(touch.clientY - this.dragStartY, 2)
      )
      
      this.isDragging = false
      
      // 儲存位置
      this.saveButtonPosition()
      
      // 如果移動距離小於 5px，觸發點擊事件
      if (distance < 5) {
        this.handleClick()
      }
    },
    
    /** 處理點擊（由子元件覆寫） */
    handleClick() {
      // 由子元件實作
    },
    
    /** 儲存按鈕位置（由子元件覆寫以使用不同的 key） */
    saveButtonPosition() {
      // 由子元件實作
    },
    
    /** 載入按鈕位置（由子元件覆寫以使用不同的 key） */
    loadButtonPosition() {
      // 由子元件實作
    }
  }
}
