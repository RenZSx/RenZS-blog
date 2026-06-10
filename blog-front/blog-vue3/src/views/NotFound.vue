<template>
  <div class="not-found-page">
    <v-container>
      <v-row justify="center" align="center" style="min-height: 60vh">
        <v-col cols="12" class="text-center">
          <h1 class="error-code">404</h1>
          <p class="error-message">页面未找到</p>
          <v-btn color="primary" to="/" class="mt-4">
            返回首页
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
// 404 页面
</script>

<style scoped>
.not-found-page {
  text-align: center;
}

.error-code {
  font-size: 8rem;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.2);
  margin-bottom: 16px;
}

.error-message {
  font-size: 1.5rem;
  color: rgba(0, 0, 0, 0.5);
}
</style>