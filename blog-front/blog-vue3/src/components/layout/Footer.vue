<template>
  <v-footer padless absolute v-if="!isMessage">
    <div class="footer-wrap">
      <div>
        {{ websiteConfig.websiteVerse }}
      </div>
      <br />
      <div>
        ©{{ formatYear(websiteConfig.websiteCreateTime) }} -
        {{ new Date().getFullYear() }} By
        {{ websiteConfig.websiteAuthor }}
        <a href="https://beian.miit.gov.cn/" target="_blank">
          {{ websiteConfig.websiteRecordNo }}
        </a>&nbsp;&nbsp;
        <a href="https://beian.mps.gov.cn/" target="_blank">
          <img
            style="height: 16px"
            src="@/assets/images/policelogo.png"
          />
          {{ websiteConfig.websitePoliceRecordNo }}
        </a>
      </div>
    </div>
  </v-footer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { formatYear } from '@/utils/filters'

const route = useRoute()
const blogInfoStore = useBlogInfoStore()

const isMessage = computed(() => route.path === '/message')
const blogInfo = computed(() => blogInfoStore.blogInfo)
const websiteConfig = computed(() => blogInfo.value.websiteConfig || {})
</script>

<style scoped>
.v-footer {
  padding: 0 !important;
}

.footer-wrap {
  width: 100%;
  line-height: 1;
  position: relative;
  padding: 20px 20px;
  color: #eee;
  font-size: 14px;
  text-align: center;
  background: linear-gradient(-45deg, #ee7752, #ce3e75, #23a6d5, #23d5ab);
  background-size: 400% 400%;
  animation: Gradient 10s ease infinite;
}

.footer-wrap a {
  color: #eee !important;
  text-decoration: none;
}

@keyframes Gradient {
  0% {
    background-position: 0 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
