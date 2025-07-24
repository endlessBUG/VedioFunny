import store from '@/store'

/**
 * 权限工具类
 */
export class PermissionUtil {
  
  /**
   * 检查用户是否有指定权限
   * @param {string} permission 权限代码
   * @returns {boolean}
   */
  static hasPermission(permission) {
    let permissions = store.getters['user/permissions']
    
    // 如果store中没有权限数据，尝试从localStorage初始化
    if (!permissions || permissions.length === 0) {
      console.log('权限数据为空，尝试从localStorage初始化')
      store.dispatch('user/initUserFromStorage')
      permissions = store.getters['user/permissions']
    }
    
    console.log(`检查权限: ${permission}, 用户权限列表:`, permissions)
    
    if (!permissions || permissions.length === 0) {
      console.log(`权限检查失败: 用户没有权限数据`)
      return false
    }
    
    // 超级管理员拥有所有权限
    if (permissions.includes('*:*:*')) {
      console.log(`权限检查通过: 超级管理员`)
      return true
    }
    
    const hasPermission = permissions.includes(permission)
    console.log(`权限检查结果: ${hasPermission}`)
    return hasPermission
  }
  
  /**
   * 检查用户是否有指定角色
   * @param {string} role 角色代码
   * @returns {boolean}
   */
  static hasRole(role) {
    let roles = store.getters['user/roles']
    
    // 如果store中没有角色数据，尝试从localStorage初始化
    if (!roles || roles.length === 0) {
      console.log('角色数据为空，尝试从localStorage初始化')
      store.dispatch('user/initUserFromStorage')
      roles = store.getters['user/roles']
    }
    
    if (!roles || roles.length === 0) {
      return false
    }
    
    return roles.includes(role)
  }
  
  /**
   * 检查用户是否有任意一个指定权限
   * @param {Array} permissions 权限代码数组
   * @returns {boolean}
   */
  static hasAnyPermission(permissions) {
    if (!Array.isArray(permissions)) {
      return this.hasPermission(permissions)
    }
    
    return permissions.some(permission => this.hasPermission(permission))
  }
  
  /**
   * 检查用户是否有任意一个指定角色
   * @param {Array} roles 角色代码数组
   * @returns {boolean}
   */
  static hasAnyRole(roles) {
    if (!Array.isArray(roles)) {
      return this.hasRole(roles)
    }
    
    return roles.some(role => this.hasRole(role))
  }
  
  /**
   * 根据权限过滤路由
   * @param {Array} routes 路由数组
   * @returns {Array} 过滤后的路由
   */
  static filterRoutesByPermission(routes) {
    if (!routes || routes.length === 0) {
      return []
    }
    
    return routes.filter(route => {
      // 如果路由有权限要求
      if (route.meta && route.meta.permission) {
        return this.hasPermission(route.meta.permission)
      }
      
      // 如果路由有角色要求
      if (route.meta && route.meta.roles) {
        return this.hasAnyRole(route.meta.roles)
      }
      
      // 如果没有权限要求，默认允许访问
      return true
    }).map(route => {
      // 递归处理子路由
      if (route.children && route.children.length > 0) {
        const filteredChildren = this.filterRoutesByPermission(route.children)
        return {
          ...route,
          children: filteredChildren
        }
      }
      return route
    }).filter(route => {
      // 如果父路由没有子路由，则隐藏父路由
      if (route.children && route.children.length === 0) {
        return false
      }
      return true
    })
  }
  
  /**
   * 生成菜单数据
   * @param {Array} routes 路由数组
   * @returns {Array} 菜单数据
   */
  static generateMenus(routes) {
    const permissions = store.getters['user/permissions']
    console.log('=== 生成菜单 ===')
    console.log('用户权限列表:', permissions)
    console.log('原始路由:', routes)
    
    // 如果没有权限数据，返回空数组
    if (!permissions || permissions.length === 0) {
      console.log('用户没有权限数据，返回空菜单')
      return []
    }
    
    // 超级管理员拥有所有菜单权限
    const hasAllPermissions = permissions.includes('*:*:*')
    
    return routes.filter(route => {
      // 隐藏的路由不显示在菜单中
      if (route.hidden) {
        return false
      }
      
      // 如果有权限要求
      if (route.meta && route.meta.permission) {
        return hasAllPermissions || this.hasPermission(route.meta.permission)
      }
      
      // 如果有角色要求
      if (route.meta && route.meta.roles) {
        return hasAllPermissions || this.hasAnyRole(route.meta.roles)
      }
      
      // 检查子路由权限
      if (route.children && route.children.length > 0) {
        const hasValidChildren = route.children.some(child => {
          if (child.hidden) return false
          if (child.meta && child.meta.permission) {
            return hasAllPermissions || this.hasPermission(child.meta.permission)
          }
          if (child.meta && child.meta.roles) {
            return hasAllPermissions || this.hasAnyRole(child.meta.roles)
          }
          return true
        })
        return hasValidChildren
      }
      
      return true
    }).map(route => {
      const menuItem = {
        path: route.path,
        name: route.name,
        meta: route.meta,
        children: []
      }
      
      // 处理子路由
      if (route.children && route.children.length > 0) {
        menuItem.children = route.children.filter(child => {
          if (child.hidden) return false
          if (child.meta && child.meta.permission) {
            return hasAllPermissions || this.hasPermission(child.meta.permission)
          }
          if (child.meta && child.meta.roles) {
            return hasAllPermissions || this.hasAnyRole(child.meta.roles)
          }
          return true
        }).map(child => ({
          path: child.path,
          name: child.name,
          meta: child.meta
        }))
      }
      
      return menuItem
    }).filter(menuItem => {
      // 如果菜单项没有子项且不是叶子节点，则隐藏
      if (menuItem.children.length === 0 && !menuItem.meta?.title) {
        return false
      }
      return true
    })
    
    console.log('生成的菜单:', result)
    console.log('=== 菜单生成完成 ===')
    return result
  }
}

export default PermissionUtil 