import store from '@/store'
import router from '@/router'

export default {
  // 重新整理目前tab頁籤
  refreshPage(obj) {
    const { path, query, matched } = router.currentRoute
    if (obj === undefined) {
      matched.forEach((m) => {
        if (m.components && m.components.default && m.components.default.name) {
          if (!['Layout', 'ParentView'].includes(m.components.default.name)) {
            obj = { name: m.components.default.name, path: path, query: query }
          }
        }
      })
    }
    return store.dispatch('tagsView/delCachedView', obj).then(() => {
      const { path, query } = obj
      router.replace({
        path: '/redirect' + path,
        query: query
      })
    })
  },
  // 關閉目前tab頁籤，打開新頁籤
  closeOpenPage(obj) {
    store.dispatch("tagsView/delView", router.currentRoute)
    if (obj !== undefined) {
      return router.push(obj)
    }
  },
  // 關閉指定tab頁籤
  closePage(obj) {
    if (obj === undefined) {
      return store.dispatch('tagsView/delView', router.currentRoute).then(({ visitedViews }) => {
        const latestView = visitedViews.slice(-1)[0]
        if (latestView) {
          return router.push(latestView.fullPath)
        }
        return router.push('/')
      })
    }
    return store.dispatch('tagsView/delView', obj)
  },
  // 關閉所有tab頁籤
  closeAllPage() {
    return store.dispatch('tagsView/delAllViews')
  },
  // 關閉左側tab頁籤
  closeLeftPage(obj) {
    return store.dispatch('tagsView/delLeftTags', obj || router.currentRoute)
  },
  // 關閉右側tab頁籤
  closeRightPage(obj) {
    return store.dispatch('tagsView/delRightTags', obj || router.currentRoute)
  },
  // 關閉其他tab頁籤
  closeOtherPage(obj) {
    return store.dispatch('tagsView/delOthersViews', obj || router.currentRoute)
  },
  // 新增tab頁籤
  openPage(title, url, params) {
    const obj = { path: url, meta: { title: title } }
    store.dispatch('tagsView/addView', obj)
    return router.push({ path: url, query: params })
  },
  // 修改tab頁籤
  updatePage(obj) {
    return store.dispatch('tagsView/updateVisitedView', obj)
  }
}
