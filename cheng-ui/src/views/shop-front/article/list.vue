<template>
  <div class="article-list-page">
    <div class="page-header">
      <h1 class="page-title">文章專欄</h1>
      <p class="page-desc">探索最新的商品資訊與生活提案</p>
    </div>

    <div class="article-grid" v-loading="loading">
      <div
        v-for="article in articles"
        :key="article.articleId"
        class="article-card"
        @click="goDetail(article.articleId)"
      >
        <div class="card-cover">
          <img
            v-if="article.coverImage"
            :src="getImageUrl(article.coverImage)"
            :alt="article.title"
          />
          <div class="cover-placeholder" v-else>
            <span>文章</span>
          </div>
        </div>
        <div class="card-body">
          <h3 class="card-title">{{ article.title }}</h3>
          <p class="card-summary" v-if="article.summary">{{ article.summary }}</p>
          <div class="card-meta">
            <span class="meta-date">{{ formatDate(article.createTime) }}</span>
            <span class="meta-views">{{ article.viewCount || 0 }} 次瀏覽</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && articles.length === 0" description="暫無文章" />

    <div class="pagination-wrapper" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="pageNum"
        @current-change="loadArticles"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { listFrontArticles } from '@/api/shop/front'

const router = useRouter()

const loading = ref(false)
const articles = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = 12

function getImageUrl(url) {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return dateStr.substring(0, 10)
}

function goDetail(articleId) {
  router.push(`/mall/article/${articleId}`)
}

async function loadArticles() {
  loading.value = true
  try {
    const res = await listFrontArticles({ pageNum: pageNum.value, pageSize })
    articles.value = res.rows || []
    total.value = res.total || 0
  } catch (error) {
    console.error('載入文章列表失敗', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadArticles()
})
</script>

<style scoped>
.article-list-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--mall-text-primary, #1a202c);
  margin: 0 0 8px;
}

.page-desc {
  font-size: 16px;
  color: #718096;
  margin: 0;
}

.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  min-height: 200px;
}

.article-card {
  background: var(--mall-card-bg, #fff);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.article-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.1);
}

.card-cover {
  aspect-ratio: 16 / 10;
  overflow: hidden;
}

.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.article-card:hover .card-cover img {
  transform: scale(1.05);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 18px;
  font-weight: 600;
}

.card-body {
  padding: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--mall-text-primary, #1a202c);
  margin: 0 0 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: 14px;
  color: #718096;
  margin: 0 0 16px;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #a0aec0;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 900px) {
  .article-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 600px) {
  .article-grid {
    grid-template-columns: 1fr;
  }
}
</style>
