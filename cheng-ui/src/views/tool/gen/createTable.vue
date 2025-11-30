<template>
  <!-- 建立表 -->
  <el-dialog title="建立表" v-model="visible" width="800px" top="5vh" append-to-body>
    <span>建立表語句(可用多個建表語句)：</span>
    <el-input type="textarea" :rows="10" placeholder="請輸入文字" v-model="content"></el-input>
    <template #footer>
      <div class="dialog-footer">
        <el-button type="primary" @click="handleImportTable">確 定</el-button>
        <el-button @click="visible = false">取 消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { createTable } from "@/api/tool/gen"

const visible = ref(false)
const content = ref("")
const { proxy } = getCurrentInstance()
const emit = defineEmits(["ok"])

/** 顯示彈框 */
function show() {
  visible.value = true
}

/** 匯入按鈕操作 */
function handleImportTable() {
  if (content.value === "") {
    proxy.$modal.msgError("請輸入建表語句")
    return
  }
  createTable({ sql: content.value }).then(res => {
    proxy.$modal.msgSuccess(res.msg)
    if (res.code === 200) {
      visible.value = false
      emit("ok")
    }
  })
}

defineExpose({
  show,
})
</script>
