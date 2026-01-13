<template>
  <span 
    class="bookmark-tag" 
    :style="tagStyle"
    :class="{ 'bookmark-tag--small': size === 'small', 'bookmark-tag--large': size === 'large' }"
  >
    <span class="bookmark-tag__dot"></span>
    <span class="bookmark-tag__text">{{ label }}</span>
  </span>
</template>

<script>
export default {
  name: 'BookmarkTag',
  props: {
    label: {
      type: String,
      required: true
    },
    color: {
      type: String,
      default: '#909399'
    },
    size: {
      type: String,
      default: 'default',
      validator: (val) => ['small', 'default', 'large'].includes(val)
    }
  },
  computed: {
    tagStyle() {
      return {
        backgroundColor: this.color,
        borderColor: this.color
      }
    }
  }
}
</script>

<style scoped>
.bookmark-tag {
  display: inline-flex;
  align-items: center;
  padding: 0 8px 0 12px;
  height: 22px;
  line-height: 22px;
  border-radius: 2px 11px 11px 2px;
  font-size: 12px;
  color: #fff;
  white-space: nowrap;
  position: relative;
  margin: 2px;
}

.bookmark-tag--small {
  height: 18px;
  line-height: 18px;
  padding: 0 6px 0 10px;
  font-size: 11px;
  border-radius: 2px 9px 9px 2px;
}

.bookmark-tag--large {
  height: 26px;
  line-height: 26px;
  padding: 0 10px 0 14px;
  font-size: 13px;
  border-radius: 2px 13px 13px 2px;
}

.bookmark-tag__dot {
  position: absolute;
  left: 4px;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
}

.bookmark-tag--small .bookmark-tag__dot {
  left: 3px;
  width: 3px;
  height: 3px;
}

.bookmark-tag--large .bookmark-tag__dot {
  left: 5px;
  width: 5px;
  height: 5px;
}

.bookmark-tag__text {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
