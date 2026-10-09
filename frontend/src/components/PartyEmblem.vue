<template>
  <img
    class="party-emblem-image"
    :src="emblemUrl"
    alt="中国共产党党徽"
    width="32"
    height="32"
    draggable="false"
  />
</template>

<script setup>
// 共产党员网发布的黄色党徽标准图案，来源见 public/party-emblem-source.md。
const emblemUrl = `${import.meta.env.BASE_URL}party-emblem.png`
</script>

<style scoped>
.party-emblem-image {
  display: block;
  flex-shrink: 0;
  object-fit: contain;
}
</style>
