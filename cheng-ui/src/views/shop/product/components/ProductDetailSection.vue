<template>
  <div class="detail-section">
    <el-form-item label="商品詳情" prop="description">
      <editor v-model="form.description" :min-height="300" />
    </el-form-item>

    <el-row :gutter="20">
      <el-col :span="6">
        <el-form-item label="推薦" prop="isRecommend">
          <el-switch v-model="form.isRecommend" :active-value="true" :inactive-value="false" />
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="新品" prop="isNew">
          <el-switch v-model="form.isNew" :active-value="true" :inactive-value="false" />
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="熱門" prop="isHot">
          <el-switch v-model="form.isHot" :active-value="true" :inactive-value="false" />
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
defineProps({
  form: { type: Object, required: true }
})

defineEmits(['update:form'])
</script>

<style scoped>
.detail-section :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
  padding-bottom: 6px;
}
</style>
