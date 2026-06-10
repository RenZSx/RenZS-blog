<template>
  <v-dialog
    :model-value="uiStore.authPromptVisible"
    content-class="auth-prompt-dialog"
    max-width="420"
    @update:model-value="handleDialogVisibilityChange"
  >
    <v-card class="auth-prompt-card">
      <div class="auth-prompt-panel">
        <div class="auth-prompt-badge-shell">
          <div class="auth-prompt-badge" aria-hidden="true">
            <v-icon class="auth-prompt-icon" icon="mdi-lock-outline" size="18" />
          </div>
        </div>

        <div class="auth-prompt-heading">
          <p class="auth-prompt-kicker">登录后可继续</p>
          <v-card-title class="auth-prompt-title">
            {{ uiStore.authPromptTitle }}
          </v-card-title>
          <p class="auth-prompt-subtitle">登录后即可继续当前页面或操作</p>
        </div>

        <v-card-text class="auth-prompt-copy">
          <p class="auth-prompt-message">{{ uiStore.authPromptMessage }}</p>
        </v-card-text>

        <v-card-actions class="auth-prompt-actions">
          <v-btn class="auth-prompt-cancel" variant="text" @click="uiStore.closeAuthPrompt()">
            取消
          </v-btn>
          <v-btn class="auth-prompt-confirm" @click="goToLogin">
            {{ uiStore.authPromptConfirmText }}
          </v-btn>
        </v-card-actions>
      </div>
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
.auth-prompt-card {
  position: relative;
  overflow: hidden;
  border-radius: 26px !important;
  border: 1px solid rgba(151, 187, 255, 0.24);
  background:
    radial-gradient(circle at top left, rgba(112, 170, 255, 0.22), transparent 32%),
    linear-gradient(180deg, rgba(248, 251, 255, 0.98), rgba(238, 244, 255, 0.94));
  box-shadow:
    0 24px 56px rgba(15, 23, 42, 0.16),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.auth-prompt-panel {
  position: relative;
  padding: 28px 28px 24px;
}

.auth-prompt-badge-shell {
  position: relative;
  display: inline-flex;
  margin-bottom: 18px;
}

.auth-prompt-badge-shell::before {
  content: '';
  position: absolute;
  inset: -10px;
  border-radius: 22px;
  background: radial-gradient(circle, rgba(76, 148, 255, 0.28) 0%, rgba(76, 148, 255, 0) 72%);
  filter: blur(8px);
  opacity: 0.9;
}

.auth-prompt-badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 54px;
  height: 54px;
  border: 1px solid rgba(112, 170, 255, 0.38);
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.74), rgba(222, 235, 255, 0.56)),
    rgba(255, 255, 255, 0.42);
  box-shadow:
    0 12px 28px rgba(61, 122, 216, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.72);
}

.auth-prompt-icon {
  color: #2d6cdf;
  filter: drop-shadow(0 4px 10px rgba(61, 122, 216, 0.22));
}

.auth-prompt-heading {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.auth-prompt-title {
  padding: 0;
  color: var(--text-primary, #0f172a);
  font-size: 2rem;
  line-height: 1.08;
  font-weight: 800;
  letter-spacing: -0.02em;
}

.auth-prompt-kicker {
  margin: 0;
  color: #6e89b7;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.auth-prompt-subtitle {
  margin: 0;
  color: var(--text-primary, #31415e);
  font-size: 0.98rem;
  line-height: 1.45;
  font-weight: 600;
  opacity: 0.82;
}

.auth-prompt-copy {
  padding: 16px 0 0;
}

.auth-prompt-message {
  margin: 0;
  max-width: 30ch;
  color: var(--text-secondary, #5a6a85);
  font-size: 0.94rem;
  line-height: 1.7;
}

.auth-prompt-actions {
  padding: 22px 0 0;
  justify-content: flex-end;
  gap: 10px;
}

.auth-prompt-cancel,
.auth-prompt-confirm {
  min-height: 42px;
  padding-inline: 18px;
  border-radius: 14px;
  font-weight: 700;
  letter-spacing: 0.01em;
  transition:
    background-color 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    transform 0.18s ease;
}

.auth-prompt-cancel {
  border: 1px solid rgba(146, 170, 210, 0.34);
  color: #4f6282;
  background: rgba(255, 255, 255, 0.36);
  backdrop-filter: blur(8px);
}

.auth-prompt-confirm {
  border: 1px solid rgba(84, 145, 240, 0.28);
  color: #eff6ff;
  background: linear-gradient(135deg, #3f87ff 0%, #245fda 100%);
  box-shadow:
    0 14px 30px rgba(53, 112, 215, 0.28),
    inset 0 1px 0 rgba(255, 255, 255, 0.18);
}

.auth-prompt-confirm :deep(.v-btn__content) {
  color: #eff6ff;
}

.auth-prompt-cancel:hover,
.auth-prompt-confirm:hover {
  transform: translateY(-1px);
}

.auth-prompt-cancel:hover {
  background: rgba(255, 255, 255, 0.56);
  border-color: rgba(120, 153, 207, 0.46);
}

.auth-prompt-confirm:hover {
  box-shadow:
    0 18px 34px rgba(53, 112, 215, 0.32),
    inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

:global(.dark .auth-prompt-dialog) {
  overflow: hidden;
  border-radius: 26px;
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
}

:global(.dark .auth-prompt-card) {
  background:
    radial-gradient(circle at top left, rgba(76, 148, 255, 0.18), transparent 32%),
    linear-gradient(180deg, rgba(17, 24, 39, 0.96), rgba(9, 14, 24, 0.98));
  border-color: rgba(128, 166, 230, 0.22);
  box-shadow:
    0 30px 70px rgba(0, 0, 0, 0.42),
    inset 0 1px 0 rgba(255, 255, 255, 0.06);
}

:global(.dark .auth-prompt-badge-shell::before) {
  background: radial-gradient(circle, rgba(87, 151, 255, 0.38) 0%, rgba(87, 151, 255, 0) 74%);
}

:global(.dark .auth-prompt-badge) {
  border-color: rgba(127, 178, 255, 0.32);
  background:
    linear-gradient(180deg, rgba(39, 58, 92, 0.94), rgba(20, 32, 56, 0.92)),
    rgba(18, 28, 46, 0.82);
  box-shadow:
    0 16px 34px rgba(4, 10, 22, 0.38),
    inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

:global(.dark .auth-prompt-icon) {
  color: #8db8ff;
  filter: drop-shadow(0 8px 16px rgba(76, 148, 255, 0.34));
}

:global(.dark .auth-prompt-title) {
  color: var(--text-primary, rgba(244, 248, 255, 0.98));
}

:global(.dark .auth-prompt-kicker) {
  color: rgba(144, 184, 255, 0.9);
}

:global(.dark .auth-prompt-subtitle) {
  color: var(--text-primary, rgba(222, 232, 246, 0.84));
  opacity: 0.86;
}

:global(.dark .auth-prompt-message) {
  color: var(--text-secondary, rgba(197, 207, 223, 0.82));
}

:global(.dark .auth-prompt-cancel) {
  color: rgba(228, 236, 247, 0.88);
  border-color: rgba(157, 180, 217, 0.2);
  background: rgba(23, 32, 49, 0.56);
}

:global(.dark .auth-prompt-confirm) {
  border-color: rgba(123, 174, 255, 0.34);
  background: linear-gradient(135deg, #4a97ff 0%, #2a67df 100%);
  box-shadow:
    0 18px 38px rgba(26, 89, 206, 0.34),
    inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

:global(.dark .auth-prompt-confirm) :deep(.v-btn__content) {
  color: #f8fbff;
}

:global(.dark .auth-prompt-cancel:hover) {
  background: rgba(31, 43, 66, 0.74);
  border-color: rgba(175, 197, 233, 0.28);
}

:global(.dark .auth-prompt-confirm:hover) {
  background: linear-gradient(135deg, #63a9ff 0%, #2f73f1 100%);
  border-color: rgba(145, 189, 255, 0.42);
  box-shadow:
    0 22px 42px rgba(26, 89, 206, 0.4),
    inset 0 1px 0 rgba(255, 255, 255, 0.24);
}

@media (max-width: 600px) {
  .auth-prompt-panel {
    padding: 24px 20px 20px;
  }

  .auth-prompt-title {
    font-size: 1.75rem;
  }

  .auth-prompt-actions {
    flex-wrap: wrap;
  }

  .auth-prompt-cancel,
  .auth-prompt-confirm {
    width: 100%;
  }
}
</style>
