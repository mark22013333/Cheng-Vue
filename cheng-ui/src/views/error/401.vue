<template>
  <div class="errPage-container" @mousemove="handleMouseMove">
    <div class="wscn-http401">

      <div class="pic-401">
        <img
          class="pic-401__parent"
          src="@/assets/401_images/401.png"
          alt="401 Locked"
          :style="parentStyle"
        >
        <div class="pic-401__icon" :style="iconStyle">❄️</div>
      </div>

      <div class="text-content">
        <div class="text-content__title">401</div>
        <div class="text-content__headline">權限凍結！</div>
        <div class="text-content__info">
          前方是 <strong>Cool Apps</strong> 的 VIP 機密區域。<br>
          看來您的通行證還沒解凍，或者您迷路到了不該來的地方。
          <br><br>
          請不用擔心，這不是什麼非法操作，只是需要一點驗證手續。
        </div>

        <div class="action-buttons">
          <router-link :to="homePath" class="btn-home">
            返回首頁
          </router-link>
          <button @click="back" class="btn-back">
            回上一頁
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {isAdminPath} from '@/router'

const router = useRouter()
const route = useRoute()

// 根據當前路徑判斷首頁位置
const homePath = computed(() => isAdminPath(route.path) ? '/cadm/index' : '/')

// === 視差效果邏輯 ===
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (e) => {
  // 計算滑鼠偏移量，除以 50 讓移動幅度適中
  const x = (e.clientX - window.innerWidth / 2) / 50
  const y = (e.clientY - window.innerHeight / 2) / 50
  mouseX.value = x
  mouseY.value = y
}

// 主圖樣式 (反向移動)
const parentStyle = computed(() => {
  return {
    transform: `translate(${mouseX.value * -1}px, ${mouseY.value * -1}px)`
  }
})

// 裝飾圖示樣式 (正向移動，產生層次感)
const iconStyle = computed(() => {
  return {
    transform: `translate(${mouseX.value * 1.5}px, ${mouseY.value * 1.5}px)`
  }
})

// === 導航邏輯 ===
function back() {
  if (route.query.noGoBack) {
    router.push({path: homePath.value})
  } else {
    router.go(-1)
  }
}
</script>

<style lang="scss" scoped>
.errPage-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  // Cool Apps 專屬冰藍色漸層背景
  background: linear-gradient(135deg, #e0c3fc 0%, #8ec5fc 100%);
  overflow: hidden;
  position: relative;
}

.wscn-http401 {
  position: relative;
  // 加大整體卡片寬度，避免圖片變大後擠壓到文字
  width: 1200px;
  max-width: 95%;

  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px;

  // 玻璃擬態核心樣式
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-radius: 24px;
  box-shadow: 0 15px 35px rgba(31, 38, 135, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.6);
  gap: 50px; // 增加間距

  // 左側圖片區
  .pic-401 {
    position: relative;
    flex: 1.3; // 讓圖片區佔比稍微大一點
    display: flex;
    justify-content: center;
    align-items: center;

    &__parent {
      width: 100%;
      // 放大圖片最大寬度
      max-width: 680px;

      height: auto;
      object-fit: contain;
      transition: transform 0.1s linear;
      // 圖片陰影讓它浮起來
      filter: drop-shadow(0 25px 35px rgba(0, 0, 0, 0.2));
    }

    &__icon {
      position: absolute;
      top: -20px;
      right: 40px;
      font-size: 70px;
      opacity: 0.8;
      transition: transform 0.1s linear;
      filter: drop-shadow(0 5px 10px rgba(0, 0, 0, 0.1));
    }
  }

  // 右側文字區
  .text-content {
    flex: 1;
    padding: 30px 0;
    text-align: left;

    &__title {
      font-size: 90px; // 稍微加大字體
      font-weight: 900;
      line-height: 1;
      // 漸層文字特效
      background: linear-gradient(45deg, #6a11cb 0%, #2575fc 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 15px;
      opacity: 0;
      animation: slideUp 0.6s forwards;
    }

    &__headline {
      font-size: 36px;
      font-weight: 700;
      color: #2c3e50;
      margin-bottom: 25px;
      opacity: 0;
      animation: slideUp 0.6s 0.1s forwards;
    }

    &__info {
      font-size: 17px;
      line-height: 1.8;
      color: #444;
      margin-bottom: 40px;
      opacity: 0;
      animation: slideUp 0.6s 0.2s forwards;

      strong {
        color: #2575fc;
      }
    }

    .action-buttons {
      display: flex;
      gap: 20px;
      opacity: 0;
      animation: slideUp 0.6s 0.3s forwards;
    }

    // 按鈕樣式
    .btn-home {
      display: inline-block;
      padding: 12px 35px;
      background: #2575fc;
      border-radius: 50px;
      color: #ffffff;
      font-size: 16px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.3s ease;
      box-shadow: 0 4px 15px rgba(37, 117, 252, 0.3);

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(37, 117, 252, 0.5);
        background: #1a65e6;
      }
    }

    .btn-back {
      padding: 12px 35px;
      background: transparent;
      border: 2px solid #2575fc;
      border-radius: 50px;
      color: #2575fc;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: rgba(37, 117, 252, 0.1);
        transform: translateY(-3px);
      }
    }
  }

  // 進場動畫
  @keyframes slideUp {
    0% {
      transform: translateY(40px);
      opacity: 0;
    }
    100% {
      transform: translateY(0);
      opacity: 1;
    }
  }
}

// RWD 響應式調整
@media screen and (max-width: 1024px) {
  .wscn-http401 {
    flex-direction: column;
    width: 90%;
    padding: 40px 30px;

    .pic-401 {
      width: 100%;
      margin-bottom: 40px;

      &__parent {
        // 在手機/平板上不要太大，以免超出螢幕
        max-width: 80%;
      }
    }

    .text-content {
      width: 100%;
      text-align: center;

      .action-buttons {
        justify-content: center;
      }
    }
  }
}
</style>
