<template>
  <div class="sidebar-logo-container" :class="{ 'collapse': collapse }">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 v-else class="sidebar-title">{{ title }}</h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title">{{ title }}</h1>
      </router-link>
    </transition>
  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
import useSettingsStore from '@/store/modules/settings'
import variables from '@/assets/styles/variables.module.scss'

defineProps({
  collapse: {
    type: Boolean,
    required: true
  }
})

const title = import.meta.env.VITE_APP_TITLE
const settingsStore = useSettingsStore()
const sideTheme = computed(() => settingsStore.sideTheme)

// 取得Logo背景色
const getLogoBackground = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-bg)'
  }
  return sideTheme.value === 'theme-dark' ? variables.menuBg : variables.menuLightBg
})

// 取得Logo文字顏色
const getLogoTextColor = computed(() => {
  if (settingsStore.isDark) {
    return 'var(--sidebar-text)'
  }
  return sideTheme.value === 'theme-dark' ? '#fff' : variables.menuLightText
})

// 漸層分隔線色彩
const getSeparatorGradient = computed(() => {
  if (settingsStore.isDark) {
    return 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.08), transparent)'
  }
  return sideTheme.value === 'theme-dark'
    ? 'linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent)'
    : 'linear-gradient(90deg, transparent, rgba(0, 0, 0, 0.06), transparent)'
})
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 50px;
  line-height: 50px;
  background: v-bind(getLogoBackground);
  text-align: center;
  overflow: hidden;

  /* 漸層分隔線 */
  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 16%;
    right: 16%;
    height: 1px;
    background: v-bind(getSeparatorGradient);
    transition: opacity 0.3s ease;
  }

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 10px;

    & .sidebar-logo {
      width: 28px;
      height: 28px;
      vertical-align: middle;
      transition: transform 0.3s ease;
      filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.1));
    }

    &:hover .sidebar-logo {
      transform: scale(1.06);
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: v-bind(getLogoTextColor);
      font-weight: 700;
      line-height: 50px;
      font-size: 15px;
      font-family: 'Inter', 'Avenir', 'Helvetica Neue', Arial, Helvetica, sans-serif;
      vertical-align: middle;
      letter-spacing: 0.02em;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }

    /* 收合時分隔線延伸 */
    &::after {
      left: 20%;
      right: 20%;
    }
  }
}
</style>
