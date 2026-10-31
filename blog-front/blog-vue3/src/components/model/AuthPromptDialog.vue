<template>
  <v-dialog
    :model-value="uiStore.authPromptVisible"
    content-class="auth-prompt-dialog"
    max-width="400"
    @update:model-value="handleDialogVisibilityChange"
  >
    <v-card class="auth-prompt-card">
      <div class="auth-prompt-icon" aria-hidden="true">
        <v-icon icon="mdi-lock-outline" size="20" />
      </div>

      <v-card-title class="auth-prompt-title">
        {{ uiStore.authPromptTitle }}
      </v-card-title>

      <v-card-text class="auth-prompt-text">
        {{ uiStore.authPromptMessage }}
      </v-card-text>

      <v-card-actions class="auth-prompt-actions">
        <v-btn class="auth-prompt-cancel" variant="text" @click="uiStore.closeAuthPrompt()">
          取消
        </v-btn>
        <v-btn class="auth-prompt-confirm" variant="flat" color="primary" @click="goToLogin">
          {{ uiStore.authPromptConfirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

import { useUIStore } from '@/stores/ui'
import { buildLoginLocation } from '@/utils/authPrompt'

const router = useRouter()
const uiStore = useUIStore()

function handleDialogVisibilityChange(value: boolean) {
  if (!value) {
    uiStore.closeAuthPrompt()
  }
}

function goToLogin() {
  const redirect = uiStore.authPromptRedirect
  uiStore.closeAuthPrompt()
  router.push(buildLoginLocation(redirect))
}
</script>

<style scoped>
/*
 * 简约风：扁平卡片 + 主题令牌，明暗主题自动适配，无装饰层。
 * 选择器带 .v-card/.v-btn 前缀并配 !important，
 * 以压过 dark-theme.css 对 v-card/v-btn 的全局 !important 覆盖。
 */
.v-card.auth-prompt-card {
  padding: 28px 26px 22px;
  border-radius: 16px !important;
  border: 1px solid var(--border-color) !important;
  background: var(--bg-overlay) !important;
  box-shadow: var(--shadow-lg) !important;
  color: var(--text-primary) !important;
}

/* dark-theme.css 给 .dark 下的弹窗容器强制加了背景，这里还原为透明 */
:global(.dark .v-dialog .v-overlay__content.auth-prompt-dialog) {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

.auth-prompt-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  margin-bottom: 16px;
  border-radius: 12px;
  background: rgba(25, 118, 210, 0.12);
  color: var(--primary-color);
}

.dark .auth-prompt-icon {
  background: rgba(100, 181, 246, 0.16);
  color: #64b5f6;
}

.auth-prompt-title {
  padding: 0;
  color: var(--text-primary) !important;
  font-size: 1.3rem;
  line-height: 1.35;
  font-weight: 700;
}

.auth-prompt-text {
  padding: 8px 0 0;
  color: var(--text-secondary) !important;
  font-size: 0.95rem;
  line-height: 1.7;
}

.auth-prompt-actions {
  justify-content: flex-end;
  gap: 8px;
  padding: 20px 0 0;
}

.auth-prompt-card .v-btn {
  min-height: 40px;
  border-radius: 10px;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: none;
}

/* 简约风下去掉 flat 按钮的默认投影 */
.auth-prompt-card .v-btn.auth-prompt-confirm {
  box-shadow: none !important;
}

@media (max-width: 600px) {
  .v-card.auth-prompt-card {
    padding: 24px 20px 18px;
  }
}
</style>
