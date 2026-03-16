/**
 * 商品飛入購物車動畫 composable
 *
 * 使用方式：
 *   const { flyToCart } = useFlyToCart()
 *   flyToCart(imageUrl, startEl)   // startEl 為觸發動畫的來源元素（如商品圖片）
 *
 * 前提：ShopLayout.vue 的購物車 icon 需有 id="shop-cart-icon"
 */
export function useFlyToCart() {
  /**
   * 執行飛入購物車動畫
   * @param {string} imageUrl - 商品圖片 URL
   * @param {HTMLElement} startEl - 動畫起始元素（取其位置）
   * @param {object} [options] - 可選配置
   * @param {number} [options.size=64] - 飛行圖片大小 (px)
   * @param {number} [options.duration=650] - 動畫時長 (ms)
   */
  function flyToCart(imageUrl, startEl, options = {}) {
    const { size = 64, duration = 650 } = options

    const cartEl = document.getElementById('shop-cart-icon')
    if (!cartEl || !startEl) return

    const startRect = startEl.getBoundingClientRect()
    const endRect = cartEl.getBoundingClientRect()

    // 起點：來源元素中心
    const startX = startRect.left + startRect.width / 2 - size / 2
    const startY = startRect.top + startRect.height / 2 - size / 2

    // 終點：購物車 icon 中心
    const endX = endRect.left + endRect.width / 2 - size / 2
    const endY = endRect.top + endRect.height / 2 - size / 2

    // 建立飛行元素
    const flyEl = document.createElement('div')
    flyEl.style.cssText = `
      position: fixed;
      z-index: 9999;
      width: ${size}px;
      height: ${size}px;
      border-radius: 50%;
      overflow: hidden;
      pointer-events: none;
      left: ${startX}px;
      top: ${startY}px;
      box-shadow: 0 4px 16px rgba(0,0,0,0.18);
      transition: none;
    `

    const img = document.createElement('img')
    img.src = imageUrl
    img.style.cssText = `
      width: 100%;
      height: 100%;
      object-fit: cover;
    `
    flyEl.appendChild(img)
    document.body.appendChild(flyEl)

    // 強制 reflow 讓初始定位生效
    flyEl.getBoundingClientRect()

    // 計算拋物線控制點（向上拱起）
    const dx = endX - startX
    const dy = endY - startY
    const arcHeight = Math.min(Math.abs(dy) * 0.5, 180) + 60

    // 使用 Web Animations API 實現拋物線
    const keyframes = []
    const steps = 30

    for (let i = 0; i <= steps; i++) {
      const t = i / steps
      // 水平：線性
      const x = startX + dx * t
      // 垂直：二次拋物線 (先上後下)
      const parabola = -4 * arcHeight * t * (t - 1) // 0→peak→0
      const y = startY + dy * t - parabola

      // 縮小 + 漸隱
      const scale = 1 - t * 0.6
      const opacity = t < 0.7 ? 1 : 1 - (t - 0.7) / 0.3

      keyframes.push({
        left: `${x}px`,
        top: `${y}px`,
        transform: `scale(${scale})`,
        opacity,
      })
    }

    const animation = flyEl.animate(keyframes, {
      duration,
      easing: 'cubic-bezier(0.4, 0, 0.2, 1)',
      fill: 'forwards',
    })

    animation.onfinish = () => {
      flyEl.remove()
      // 購物車 icon 彈跳回饋
      bounceCartIcon(cartEl)
    }
  }

  /**
   * 購物車 icon 到達時的彈跳效果
   */
  function bounceCartIcon(cartEl) {
    // 移除舊動畫（如果有）
    cartEl.classList.remove('cart-bounce')
    // 強制 reflow
    void cartEl.offsetWidth
    cartEl.classList.add('cart-bounce')

    // 動畫結束後移除 class
    const onEnd = () => {
      cartEl.classList.remove('cart-bounce')
      cartEl.removeEventListener('animationend', onEnd)
    }
    cartEl.addEventListener('animationend', onEnd)
  }

  return { flyToCart }
}
