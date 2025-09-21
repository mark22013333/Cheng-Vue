<template>
  <!-- 建立表 -->
  <el-dialog :visible.sync="visible" append-to-body title="建立表" top="5vh" width="800px">
    <span>建立表語句(支援多個建表語句)：</span>
    <el-input v-model="content" :rows="10" placeholder="請輸入文字" type="textarea"></el-input>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="handleCreateTable">確 定</el-button>
      <el-button @click="visible = false">取 消</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {createTable} from "@/api/tool/gen"

export default {
  data() {
    return {
      // 遮罩層
      visible: false,
      // 文字内容
      content: ""
    }
  },
  methods: {
    // 顯示彈框
    show() {
      this.visible = true
    },
    /** 建立按鈕操作 */
    handleCreateTable() {
      if (this.content === "") {
        this.$modal.msgError("請輸入建表語句")
        return
      }
      createTable({ sql: this.content }).then(res => {
        this.$modal.msgSuccess(res.msg)
        if (res.code === 200) {
          this.visible = false
          this.$emit("ok")
        }
      })
    }
  }
}
</script>
