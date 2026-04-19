<template>
  <div class="basic-section">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-form-item label="商品名稱" prop="title">
          <el-input v-model="form.title" placeholder="請輸入商品名稱" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="商品分類" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryOptions"
            :props="{ value: 'categoryId', label: 'name', children: 'children' }"
            placeholder="選擇分類"
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-form-item label="商品簡介" prop="subTitle">
      <el-input v-model="form.subTitle" type="textarea" :rows="2" placeholder="請輸入商品簡介" />
    </el-form-item>

    <el-row :gutter="20">
      <el-col :span="8">
        <el-form-item label="商品主圖" prop="mainImage">
          <image-upload v-model="form.mainImage" :limit="1" />
        </el-form-item>
      </el-col>
      <el-col :span="16">
        <el-form-item label="商品圖片" prop="sliderImages">
          <image-upload v-model="form.sliderImages" :limit="5" />
        </el-form-item>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
const props = defineProps({
  form: { type: Object, required: true },
  categoryOptions: { type: Array, default: () => [] }
})

defineEmits(['update:form'])
</script>

<style scoped>
.basic-section :deep(.el-form-item__label) {
  font-weight: 500;
  color: #475569;
  padding-bottom: 6px;
}
</style>
