<template>
  <div class="wscn-http404-container" @mousemove="handleMouseMove">
    <div class="wscn-http404">

      <div class="pic-404">
        <img
          class="pic-404__parent"
          src="@/assets/404_images/404.png"
          alt="404"
          :style="parentStyle"
        >
        <img class="pic-404__child left" src="@/assets/404_images/404_cloud.png" alt="404">
        <img class="pic-404__child mid" src="@/assets/404_images/404_cloud.png" alt="404">
        <img class="pic-404__child right" src="@/assets/404_images/404_cloud.png" alt="404">
      </div>

      <div class="text-content">
        <div class="text-content__oops">OOPS!</div>
        <div class="text-content__headline">{{ message }}</div>
        <div class="text-content__info">
          看來您迷路了... 這個頁面似乎已經私奔到月球去了。
          <br>
          請檢查網址是否有誤，或是點擊下方按鈕回到安全的地球表面。
        </div>

        <div class="action-buttons">
          <router-link to="/index" class="btn-home">返回首頁</router-link>
          <button @click="goBack" class="btn-back">回上一頁</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed} from 'vue'
import {useRouter} from 'vue-router' // 1. 引入 useRouter

const router = useRouter() // 2. 初始化 router

const message = computed(() => {
  return '這裡什麼都沒有... 除了空氣'
})

// === 視差效果邏輯 ===
const mouseX = ref(0)
const mouseY = ref(0)

const handleMouseMove = (e) => {
  const x = (e.clientX - window.innerWidth / 2) / 60
  const y = (e.clientY - window.innerHeight / 2) / 60
  mouseX.value = x
  mouseY.value = y
}

const parentStyle = computed(() => {
  return {
    transform: `translate(${mouseX.value * -1}px, ${mouseY.value * -1}px)`
  }
})

// 3. 修改處：回上一頁邏輯
const goBack = () => {
  router.go(-1)
}
</script>

<style lang="scss" scoped>
.wscn-http404-container {
  width: 100vw;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  overflow: hidden;
  position: relative;
}

.wscn-http404 {
  position: relative;
  width: 1200px;
  max-width: 95%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 50px;
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(10px);
  border-radius: 20px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.5);
  gap: 40px;

  .pic-404 {
    position: relative;
    flex: 1.2;
    min-width: 500px;
    height: auto;
    display: flex;
    justify-content: center;
    align-items: center;

    &__parent {
      width: 100%;
      height: auto;
      max-width: 700px;
      transition: transform 0.1s linear;
      filter: drop-shadow(0 15px 20px rgba(20, 130, 240, 0.25));
      will-change: transform;
    }

    &__child {
      position: absolute;
      opacity: 0;
      animation-timing-function: linear;
      animation-iteration-count: infinite;

      &.left {
        width: 80px;
        top: 17px;
        left: 220px;
        animation-name: cloudLeft;
        animation-duration: 4s;
        animation-delay: 1s;
      }

      &.mid {
        width: 46px;
        top: 10px;
        left: 420px;
        animation-name: cloudMid;
        animation-duration: 4s;
        animation-delay: 1.2s;
      }

      &.right {
        width: 62px;
        top: 100px;
        left: 500px;
        animation-name: cloudRight;
        animation-duration: 4s;
        animation-delay: 1s;
      }
    }
  }

  .text-content {
    position: relative;
    flex: 1;
    padding: 30px 0;

    &__oops {
      font-size: 60px;
      font-weight: 900;
      line-height: 1.2;
      background: -webkit-linear-gradient(315deg, #42d392 25%, #647eff);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      margin-bottom: 25px;
      opacity: 0;
      animation: slideUp 0.6s forwards;
    }

    &__headline {
      font-size: 28px;
      line-height: 1.4;
      color: #2c3e50;
      font-weight: bold;
      opacity: 0;
      margin-bottom: 15px;
      animation: slideUp 0.6s 0.1s forwards;
    }

    &__info {
      font-size: 16px;
      line-height: 1.8;
      color: #606266;
      opacity: 0;
      margin-bottom: 35px;
      animation: slideUp 0.6s 0.2s forwards;
    }

    .action-buttons {
      display: flex;
      gap: 20px;
      opacity: 0;
      animation: slideUp 0.6s 0.3s forwards;
    }

    .btn-home {
      display: inline-block;
      padding: 12px 30px;
      background: #1482f0;
      border-radius: 50px;
      text-align: center;
      color: #ffffff;
      font-size: 15px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.3s ease;
      box-shadow: 0 4px 15px rgba(20, 130, 240, 0.3);

      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 8px 25px rgba(20, 130, 240, 0.5);
        background: #0d70d4;
      }
    }

    .btn-back {
      padding: 12px 30px;
      background: transparent;
      border: 2px solid #1482f0;
      border-radius: 50px;
      color: #1482f0;
      font-size: 15px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #e6f1fc;
        transform: translateY(-3px);
      }
    }
  }

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

  @keyframes cloudLeft {
    0% {
      top: 17px;
      left: 220px;
      opacity: 0;
    }
    20% {
      opacity: 1;
    }
    80% {
      opacity: 1;
    }
    100% {
      top: 97px;
      left: 60px;
      opacity: 0;
    }
  }
  @keyframes cloudMid {
    0% {
      top: 10px;
      left: 420px;
      opacity: 0;
    }
    20% {
      opacity: 1;
    }
    80% {
      opacity: 1;
    }
    100% {
      top: 160px;
      left: 120px;
      opacity: 0;
    }
  }
  @keyframes cloudRight {
    0% {
      top: 100px;
      left: 500px;
      opacity: 0;
    }
    20% {
      opacity: 1;
    }
    80% {
      opacity: 1;
    }
    100% {
      top: 200px;
      left: 300px;
      opacity: 0;
    }
  }
}

@media screen and (max-width: 1024px) {
  .wscn-http404 {
    flex-direction: column;
    padding: 30px;

    .pic-404 {
      min-width: auto;
      width: 90%;
      margin-bottom: 40px;
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
