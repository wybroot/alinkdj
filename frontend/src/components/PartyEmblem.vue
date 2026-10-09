<template>
  <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100" width="100%" height="100%">
    <!-- 庄严党徽金黄渐变 -->
    <defs>
      <linearGradient id="partyGold" x1="0%" y1="0%" x2="100%" y2="100%">
        <stop offset="0%" stop-color="#fff176"/>
        <stop offset="45%" stop-color="#ffd54f"/>
        <stop offset="100%" stop-color="#ffb300"/>
      </linearGradient>
      <filter id="goldGlow" x="-20%" y="-20%" width="140%" height="140%">
        <feDropShadow dx="0" dy="2" stdDeviation="2" flood-color="#000" flood-opacity="0.3"/>
      </filter>
    </defs>

    <g filter="url(#goldGlow)" fill="url(#partyGold)">
      <!-- 1. 锤头与锤柄 (Hammer) -->
      <!-- 锤柄 (斜向长方形及底部圆角) -->
      <path d="M 24 74 L 64 34 L 70 40 L 30 80 Z" />
      <circle cx="27" cy="77" r="4.2" />

      <!-- 锤头 (顶端方形横敲锤) -->
      <path d="M 58 26 L 73 11 L 83 21 L 68 36 Z" />
      <!-- 锤头顶部装饰面 -->
      <path d="M 70 8 L 76 2 L 87 13 L 81 19 Z" opacity="0.9" />

      <!-- 2. 弯镰刀 (Sickle) -->
      <!-- 弧形主刀刃 -->
      <path d="M 78 40 C 78 18 58 8 38 12 C 26 15 16 23 12 34 C 10 39 12 42 16 41 C 19 40 22 36 26 31 C 36 20 54 18 64 26 C 73 34 72 49 66 61 C 63 67 56 73 50 77 C 46 80 47 84 52 82 C 64 78 74 68 77 56 C 78 51 78 45 78 40 Z" />
      <!-- 镰刀刀柄 -->
      <path d="M 50 78 L 62 90 L 56 96 L 44 84 Z" />
      <circle cx="59" cy="93" r="3.5" />
      <!-- 镰刀柄铆钉与连接块 -->
      <rect x="47" y="76" width="7" height="6" rx="1.5" transform="rotate(45 50.5 79)" />
    </g>
  </svg>
</template>

<script setup>
</script>
