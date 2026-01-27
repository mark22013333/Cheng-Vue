<template>
  <div class="product-card" @click="$emit('click')">
    <div class="product-image">
      <img :src="getImageUrl(product.mainImage)" :alt="product.title" />
      <div class="product-tags">
        <span v-if="product.isHot" class="tag hot">熱門</span>
        <span v-if="product.isNew" class="tag new">新品</span>
      </div>
    </div>
    <div class="product-info">
      <h3 class="product-title">{{ product.title }}</h3>
      <p class="product-subtitle" v-if="product.subTitle">{{ product.subTitle }}</p>
      <div class="product-price">
        <span class="current-price">NT$ {{ product.price || 0 }}</span>
        <span v-if="product.originalPrice && product.originalPrice > product.price" class="original-price">
          NT$ {{ product.originalPrice }}
        </span>
      </div>
      <div class="product-meta">
        <span class="sales">銷量 {{ product.salesCount || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  product: {
    type: Object,
    required: true
  }
})

defineEmits(['click'])

function getImageUrl(url) {
  if (!url) return 'https://via.placeholder.com/300x300?text=No+Image'
  if (url.startsWith('http')) return url
  return import.meta.env.VITE_APP_BASE_API + url
}
</script>

<style scoped>
.product-card {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.product-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
  border-color: transparent;
}

.product-image {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.product-tags {
  position: absolute;
  top: 8px;
  left: 8px;
  display: flex;
  gap: 4px;
}

.tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.tag.hot {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
}

.tag.new {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.product-info {
  padding: 14px;
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-subtitle {
  font-size: 12px;
  color: #909399;
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.current-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--mall-accent, #f56c6c);
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}
</style>
