import store from '@/store'

function authPermission(permission) {
  const all_permission = "*:*:*"
  const permissions = store.getters && store.getters.permissions
  if (permission && permission.length > 0) {
    return permissions.some(v => {
      return all_permission === v || v === permission
    })
  } else {
    return false
  }
}

function authRole(role) {
  const super_admin = "admin"
  const roles = store.getters && store.getters.roles
  if (role && role.length > 0) {
    return roles.some(v => {
      return super_admin === v || v === role
    })
  } else {
    return false
  }
}

export default {
  // 驗證使用者是否具備某權限
  hasPermi(permission) {
    return authPermission(permission)
  },
  // 驗證使用者是否含有指定權限，只需包含其中一個
  hasPermiOr(permissions) {
    return permissions.some(item => {
      return authPermission(item)
    })
  },
  // 驗證使用者是否含有指定權限，必須全部擁有
  hasPermiAnd(permissions) {
    return permissions.every(item => {
      return authPermission(item)
    })
  },
  // 驗證使用者是否具備某角色
  hasRole(role) {
    return authRole(role)
  },
  // 驗證使用者是否含有指定角色，只需包含其中一個
  hasRoleOr(roles) {
    return roles.some(item => {
      return authRole(item)
    })
  },
  // 驗證使用者是否含有指定角色，必須全部擁有
  hasRoleAnd(roles) {
    return roles.every(item => {
      return authRole(item)
    })
  }
}
