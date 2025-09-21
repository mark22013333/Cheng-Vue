import {Loading, Message, MessageBox, Notification} from 'element-ui'

let loadingInstance

export default {
  // 訊息提示
  msg(content) {
    Message.info(content)
  },
  // 錯誤訊息
  msgError(content) {
    Message.error(content)
  },
  // 成功訊息
  msgSuccess(content) {
    Message.success(content)
  },
  // 警告訊息
  msgWarning(content) {
    Message.warning(content)
  },
  // 彈出提示
  alert(content) {
    MessageBox.alert(content, "系統提示")
  },
  // 錯誤提示
  alertError(content) {
    MessageBox.alert(content, "系統提示", {type: 'error'})
  },
  // 成功提示
  alertSuccess(content) {
    MessageBox.alert(content, "系統提示", {type: 'success'})
  },
  // 警告提示
  alertWarning(content) {
    MessageBox.alert(content, "系統提示", {type: 'warning'})
  },
  // 通知提示
  notify(content) {
    Notification.info(content)
  },
  // 錯誤通知
  notifyError(content) {
    Notification.error(content)
  },
  // 成功通知
  notifySuccess(content) {
    Notification.success(content)
  },
  // 警告通知
  notifyWarning(content) {
    Notification.warning(content)
  },
  // 確認窗體
  confirm(content) {
    return MessageBox.confirm(content, "系統提示", {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: "warning",
    })
  },
  // 提交内容
  prompt(content) {
    return MessageBox.prompt(content, "系統提示", {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: "warning",
    })
  },
  // 打開遮罩層
  loading(content) {
    loadingInstance = Loading.service({
      lock: true,
      text: content,
      spinner: "el-icon-loading",
      background: "rgba(0, 0, 0, 0.7)",
    })
  },
  // 關閉遮罩層
  closeLoading() {
    loadingInstance.close()
  }
}
