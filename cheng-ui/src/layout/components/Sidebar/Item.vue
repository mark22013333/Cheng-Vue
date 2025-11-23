<script>
import { h } from 'vue'

export default {
  name: 'MenuItem',
  props: {
    icon: {
      type: String,
      default: ''
    },
    title: {
      type: String,
      default: ''
    }
  },
  render() {
    const { icon, title } = this
    const vnodes = []

    if (icon) {
      // Vue 3 寫法：props 使用小寫 + 連字符，並包裝在物件中
      vnodes.push(h('svg-icon', { 'icon-class': icon }))
    }

    if (title) {
      if (title.length > 5) {
        vnodes.push(h('span', { title: title }, title))
      } else {
        vnodes.push(h('span', null, title))
      }
    }
    return vnodes
  }
}
</script>
