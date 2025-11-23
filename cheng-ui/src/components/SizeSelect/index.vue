<template>
  <el-dropdown trigger="click" @command="handleSetSize" popper-class="size-select-dropdown">
    <div style="display: inline-flex; align-items: center; justify-content: center;">
      <svg-icon class-name="size-icon" icon-class="size" />
    </div>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item v-for="item of sizeOptions" :key="item.value" :disabled="size===item.value" :command="item.value">
          {{ item.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script>
export default {
  data() {
    return {
      sizeOptions: [
        { label: 'Default', value: 'default' },
        { label: 'Medium', value: 'medium' },
        { label: 'Small', value: 'small' },
        { label: 'Mini', value: 'mini' }
      ]
    }
  },
  computed: {
    size() {
      return this.$store.getters.size
    }
  },
  methods: {
    handleSetSize(size) {
      // 儲存到 Vuex 和 Cookie
      this.$store.dispatch('app/setSize', size)
      
      // Vue 3 Element Plus 需要重新載入頁面才能應用大小變更
      this.$message({
        message: '介面大小將在重新整理後生效',
        type: 'success'
      })
      
      // 1秒後重新載入頁面
      setTimeout(() => {
        location.reload()
      }, 1000)
    }
  }
}
</script>
