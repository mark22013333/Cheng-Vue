<template>
  <div class="action-fields">
    <!-- Message 類型 -->
    <template v-if="action.type === 'message'">
      <el-form-item label="發送文字" size="small" required>
        <el-input 
          v-model="action.text" 
          placeholder="點擊後發送的訊息"
          :maxlength="300"
          @input="emitChange"
        />
      </el-form-item>
    </template>

    <!-- URI 類型 -->
    <template v-else-if="action.type === 'uri'">
      <el-form-item label="連結網址" size="small" required>
        <el-input 
          v-model="action.uri" 
          placeholder="https://..."
          @input="emitChange"
        />
      </el-form-item>
    </template>

    <!-- Postback 類型 -->
    <template v-else-if="action.type === 'postback'">
      <el-form-item label="回傳資料" size="small" required>
        <el-input 
          v-model="action.data" 
          placeholder="action=xxx"
          :maxlength="300"
          @input="emitChange"
        />
      </el-form-item>
      <el-form-item label="顯示文字" size="small">
        <el-input 
          v-model="action.displayText" 
          placeholder="選填"
          :maxlength="300"
          @input="emitChange"
        />
      </el-form-item>
    </template>
  </div>
</template>

<script setup>
const props = defineProps({
  action: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['change'])

const emitChange = () => {
  emit('change')
}
</script>

<style scoped lang="scss">
.action-fields {
  :deep(.el-form-item) {
    margin-bottom: 8px;
  }
}
</style>
