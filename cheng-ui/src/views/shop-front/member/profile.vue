<template>
  <div class="profile-page">
    <div class="page-header">
      <h2>個人資料</h2>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="100px"
      style="max-width: 500px"
    >
      <el-form-item label="頭像">
        <el-avatar :size="80" :src="userAvatar" />
      </el-form-item>
      <el-form-item label="用戶名稱">
        <el-input v-model="form.userName" disabled />
      </el-form-item>
      <el-form-item label="暱稱" prop="nickName">
        <el-input v-model="form.nickName" placeholder="請輸入暱稱" />
      </el-form-item>
      <el-form-item label="手機號碼" prop="phonenumber">
        <el-input v-model="form.phonenumber" placeholder="請輸入手機號碼" />
      </el-form-item>
      <el-form-item label="電子郵件" prop="email">
        <el-input v-model="form.email" placeholder="請輸入電子郵件" />
      </el-form-item>
      <el-form-item label="性別">
        <el-radio-group v-model="form.sex">
          <el-radio value="0">男</el-radio>
          <el-radio value="1">女</el-radio>
          <el-radio value="2">未知</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">儲存</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import useUserStore from '@/store/modules/user'

const userStore = useUserStore()

const formRef = ref(null)
const saving = ref(false)

const userAvatar = computed(() => userStore.avatar || '')

const form = reactive({
  userName: '',
  nickName: '',
  phonenumber: '',
  email: '',
  sex: '2'
})

const rules = {
  nickName: [
    { required: true, message: '請輸入暱稱', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '請輸入正確的電子郵件地址', trigger: 'blur' }
  ]
}

onMounted(() => {
  // 載入用戶資料
  form.userName = userStore.name || ''
  form.nickName = userStore.name || ''
})

async function handleSave() {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    saving.value = true

    // 呼叫 API 儲存用戶資料
    ElMessage.success('儲存成功')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('儲存失敗')
    }
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 0;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}
</style>
