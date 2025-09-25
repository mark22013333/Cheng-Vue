/**
 * v-dialogDrag 彈窗拖曳
* Copyright (c) 2019 cheng
*/

export default {
  bind(el, binding, vnode, oldVnode) {
    const value = binding.value
    if (value == false) return
    // 取得拖曳内容頭部
    const dialogHeaderEl = el.querySelector('.el-dialog__header')
    const dragDom = el.querySelector('.el-dialog')
    dialogHeaderEl.style.cursor = 'move'
    // 取得原有屬性 ie dom元素.currentStyle 火狐谷歌 window.getComputedStyle(dom元素, null)
    const sty = dragDom.currentStyle || window.getComputedStyle(dragDom, null)
    dragDom.style.position = 'absolute'
    dragDom.style.marginTop = 0
    let width = dragDom.style.width
    if (width.includes('%')) {
      width = +document.body.clientWidth * (+width.replace(/\%/g, '') / 100)
    } else {
      width = +width.replace(/\px/g, '')
    }
    dragDom.style.left = `${(document.body.clientWidth - width) / 2}px`
    // 游標按下事件
    dialogHeaderEl.onmousedown = (e) => {
      // 游標按下，計算目前元素距離可視區的距離 (游標點擊位置距離可視視窗的距離)
      const disX = e.clientX - dialogHeaderEl.offsetLeft
      const disY = e.clientY - dialogHeaderEl.offsetTop

      // 取得到的值帶px 正則匹配替換
      let styL, styT

      // 註意在ie中 第一次取得到的值為元件自帶50% 移動之後賦值為px
      if (sty.left.includes('%')) {
        styL = +document.body.clientWidth * (+sty.left.replace(/\%/g, '') / 100)
        styT = +document.body.clientHeight * (+sty.top.replace(/\%/g, '') / 100)
      } else {
        styL = +sty.left.replace(/\px/g, '')
        styT = +sty.top.replace(/\px/g, '')
      }

      // 游標拖曳事件
      document.onmousemove = function (e) {
        // 通過事件委派，計算移動的距離 （開始拖曳至結束拖曳的距離）
        const l = e.clientX - disX
        const t = e.clientY - disY

        let finallyL = l + styL
        let finallyT = t + styT

        // 移動目前元素
        dragDom.style.left = `${finallyL}px`
        dragDom.style.top = `${finallyT}px`

      }

      document.onmouseup = function (e) {
        document.onmousemove = null
        document.onmouseup = null
      }
    }
  }
}
