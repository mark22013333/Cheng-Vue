import { ElLoading, ElMessage, ElMessageBox, ElNotification } from 'element-plus'

let loadingInstance

export default {
  // 訊息提示
  msg(content) {
    ElMessage.info(content)
  },
  // 錯誤訊息
  msgError(content) {
    ElMessage.error(content)
  },
  // 成功訊息
  msgSuccess(content) {
    ElMessage.success(content)
  },
  // 警告訊息
  msgWarning(content) {
    ElMessage.warning(content)
  },
  // 彈出提示
  alert(content) {
    ElMessageBox.alert(content, "系統提示")
  },
  // 錯誤提示
  alertError(content) {
    ElMessageBox.alert(content, "系統提示", {type: 'error'})
  },
  // 成功提示
  alertSuccess(content) {
    ElMessageBox.alert(content, "系統提示", {type: 'success'})
  },
  // 警告提示
  alertWarning(content) {
    ElMessageBox.alert(content, "系統提示", {type: 'warning'})
  },
  // 通知提示
  notify(content) {
    ElNotification.info(content)
  },
  // 錯誤通知
  notifyError(content) {
    ElNotification.error(content)
  },
  // 成功通知
  notifySuccess(content) {
    ElNotification.success(content)
  },
  // 警告通知
  notifyWarning(content) {
    ElNotification.warning(content)
  },
  // 確認窗體
  confirm(content) {
    return ElMessageBox.confirm(content, "系統提示", {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: "warning",
    })
  },
  // 提交内容
  prompt(content) {
    return ElMessageBox.prompt(content, "系統提示", {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: "warning",
    })
  },
  // 打開遮罩層
  loading(content) {
    loadingInstance = ElLoading.service({
      lock: true,
      text: content,
      background: "rgba(0, 0, 0, 0.7)",
    })
  },
  // 關閉遮罩層
  closeLoading() {
    loadingInstance.close()
  }
}
