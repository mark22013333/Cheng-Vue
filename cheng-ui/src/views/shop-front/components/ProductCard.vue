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
        <span class="current-price">NT$ {{ product.finalPrice || product.price || 0 }}</span>
        <span v-if="product.originalDisplayPrice && product.originalDisplayPrice > (product.finalPrice || product.price)" class="original-price">
          NT$ {{ product.originalDisplayPrice }}
        </span>
        <span v-else-if="product.originalPrice && product.originalPrice > (product.finalPrice || product.price)" class="original-price">
          NT$ {{ product.originalPrice }}
        </span>
      </div>
      <div v-if="product.discountLabel" class="discount-label">
        {{ product.discountLabel }}
      </div>
      <div class="product-meta">
        <span class="sales">已售 {{ product.salesCount || 0 }}</span>
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
  if (url.startsWith('/profile')) return url
  return '/profile' + (url.startsWith('/') ? url : '/' + url)
}
</script>

<style scoped>
.product-card {
  background: var(--mall-card-bg, white);
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: transparent;
}

.product-image {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  background: #f8f6f3;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.product-card:hover .product-image img {
  transform: scale(1.06);
}

.product-tags {
  position: absolute;
  top: 10px;
  left: 10px;
  display: flex;
  gap: 6px;
}

.tag {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tag.hot {
  background: rgba(165, 99, 92, 0.9);
  color: white;
}

.tag.new {
  background: rgba(74, 107, 124, 0.9);
  color: white;
}

.product-info {
  padding: 16px;
}

.product-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--mall-text-primary, #303133);
  margin: 0 0 6px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 12px;
  color: var(--mall-text-muted, #909399);
  margin: 0 0 10px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;
}

.current-price {
  font-size: 18px;
  font-weight: 700;
  color: var(--mall-accent, #A5635C);
}

.original-price {
  font-size: 12px;
  color: #c0c4cc;
  text-decoration: line-through;
}

.discount-label {
  display: inline-block;
  padding: 2px 8px;
  margin-bottom: 6px;
  font-size: 11px;
  color: var(--mall-accent, #A5635C);
  background: rgba(165, 99, 92, 0.08);
  border: 1px solid rgba(165, 99, 92, 0.15);
  border-radius: 4px;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--mall-text-muted, #909399);
}
</style>
