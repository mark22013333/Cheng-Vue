/**
 * v-hasPermi 操作權限處理
 * Copyright (c) 2019 cheng
 */
import useUserStore from '@/store/modules/user'

export default {
  mounted(el, binding, vnode) {
    const {value} = binding
    const all_permission = "*:*:*"
    const permissions = useUserStore().permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(permission => {
        return all_permission === permission || permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`請設定操作權限標籤值`)
    }
  }
}
