<template>
  <el-dialog v-model="open" width="500px" title="選擇產生類型" @open="onOpen" @close="onClose">
    <el-form ref="codeTypeForm" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="產生類型" prop="type">
        <el-radio-group v-model="formData.type">
          <el-radio-button v-for="(item, index) in typeOptions" :key="index" :label="item.value">
            {{ item.label }}
          </el-radio-button>
        </el-radio-group>
      </el-form-item>
      <el-form-item v-if="showFileName" label="文件名稱" prop="fileName">
        <el-input v-model="formData.fileName" placeholder="請輸入文件名稱" clearable />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="onClose">取消</el-button>
      <el-button type="primary" @click="handelConfirm">確定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
const open = defineModel()
const props = defineProps({
  showFileName: Boolean
})
const emit = defineEmits(['confirm'])
const formData = ref({
  fileName: undefined,
  type: 'file'
})
const codeTypeForm = ref()
const rules = {
  fileName: [{
    required: true,
    message: '請輸入文件名稱',
    trigger: 'blur'
  }],
  type: [{
    required: true,
    message: '產生類型不能為空',
    trigger: 'change'
  }]
}
const typeOptions = ref([
  {
    label: '頁面',
    value: 'file'
  },
  {
    label: '彈窗',
    value: 'dialog'
  }
])
function onOpen() {
  if (props.showFileName) {
    formData.value.fileName = `${+new Date()}.vue`
  }
}
function onClose() {
  open.value = false
}
function handelConfirm() {
  codeTypeForm.value.validate(valid => {
    if (!valid) return
    emit('confirm', { ...formData.value })
    onClose()
  })
}
</script>
