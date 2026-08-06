<template>
  <view v-if="modelValue" class="overlay" @click="closeOnOverlay && $emit('update:modelValue', false)">
    <view class="popup" @click.stop>
      <view class="popup-header">
        <text class="popup-title">{{ title }}</text>
        <text class="close" @click="$emit('update:modelValue', false)">×</text>
      </view>
      <view class="popup-body">
        <slot />
      </view>
    </view>
  </view>
</template>

<script setup>
defineProps({
  modelValue: Boolean,
  title: { type: String, default: '' },
  closeOnOverlay: { type: Boolean, default: true }
})
defineEmits(['update:modelValue'])
</script>

<style scoped>
.overlay { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup { background: #fff; border-radius: 16rpx; width: 680rpx; max-height: 80vh; overflow-y: auto; }
.popup-header { display: flex; justify-content: space-between; align-items: center; padding: 24rpx 30rpx; border-bottom: 1rpx solid #eee; }
.popup-title { font-size: 34rpx; font-weight: bold; }
.close { font-size: 48rpx; color: #999; line-height: 1; }
.popup-body { padding: 30rpx; }
</style>
