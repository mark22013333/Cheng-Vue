// Vue 3 事件總線（替代 Vue 2 的 $on/$off/$emit）
import mitt from 'mitt'

const emitter = mitt()

export default emitter
