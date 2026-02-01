<template>
  <div class="article-detail-page" v-loading="loading">
    <template v-if="article">
      <div class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div class="article-meta">
          <span>{{ formatDate(article.createTime) }}</span>
          <span class="meta-divider">|</span>
          <span>{{ article.viewCount || 0 }} 次瀏覽</span>
        </div>
      </div>

      <div class="article-cover" v-if="article.coverImage">
        <img :src="getImageUrl(article.coverImage)" :alt="article.title" />
      </div>

      <div class="article-content" v-html="article.content"></div>

      <!-- 關聯商品 -->
      <div class="related-product" v-if="article.productId && article.productTitle">
        <h3 class="related-title">相關商品</h3>
        <div class="product-link" @click="goProduct(article.productId)">
          <span class="product-name">{{ article.productTitle }}</span>
          <span class="product-arrow">前往查看 →</span>
        </div>
      </div>

      <div class="article-footer">
        <el-button @click="goBack" icon="ArrowLeft">返回列表</el-button>
      </div>
    </template>

    <el-empty v-if="!loading && !article" description="文章不存在或未發布" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getFrontArticle } from '@/api/shop/front'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const article = ref(null)

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

function goProduct(productId) {
  router.push(`/mall/product/${productId}`)
}

function goBack() {
  router.push('/mall/articles')
}

async function loadArticle() {
  const articleId = route.params.id
  if (!articleId) return

  loading.value = true
  try {
    const res = await getFrontArticle(articleId)
    article.value = res.data
  } catch (error) {
    console.error('載入文章失敗', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.article-detail-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 40px 20px;
  min-height: 400px;
}

.article-header {
  text-align: center;
  margin-bottom: 32px;
}

.article-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--mall-text-primary, #1a202c);
  margin: 0 0 16px;
  line-height: 1.3;
}

.article-meta {
  font-size: 14px;
  color: #718096;
}

.meta-divider {
  margin: 0 8px;
}

.article-cover {
  margin-bottom: 32px;
  border-radius: 12px;
  overflow: hidden;
}

.article-cover img {
  width: 100%;
  max-height: 500px;
  object-fit: cover;
}

.article-content {
  font-size: 16px;
  line-height: 1.8;
  color: var(--mall-text-primary, #2d3748);
  margin-bottom: 40px;
}

.article-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 16px 0;
}

.article-content :deep(p) {
  margin-bottom: 16px;
}

.related-product {
  background: var(--mall-card-bg, #f7fafc);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 32px;
}

.related-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--mall-text-primary, #1a202c);
  margin: 0 0 12px;
}

.product-link {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid #e2e8f0;
}

.product-link:hover {
  border-color: var(--mall-primary, #5a67d8);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.product-name {
  font-weight: 500;
  color: var(--mall-text-primary, #2d3748);
}

.product-arrow {
  color: var(--mall-primary, #5a67d8);
  font-size: 14px;
}

.article-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}
</style>
