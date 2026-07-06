<template>
  <div>
    <!-- banner -->
    <div class="banner" :style="cover">
      <h1 class="banner-title animated fadeInDown">友情链接</h1>
    </div>
    <v-card class="blog-container link-container">
      <!-- 添加友链信息前置，方便访问者先添加本站再提交申请。 -->
      <section class="link-apply-section">
        <div class="link-title mb-4">
          <v-icon class="link-title-icon">mdi-file-document-outline</v-icon> 本站信息
        </div>
        <div class="link-info-box">
          <div class="link-info-row">
            <span class="link-info-label">网站名称:</span>
            <span>{{ websiteConfig?.websiteName }}</span>
          </div>
          <div class="link-info-row">
            <span class="link-info-label">网址:</span>
            <span>{{ siteAddress }}</span>
          </div>
          <div class="link-info-row">
            <span class="link-info-label">头像:</span>
            <span>{{ websiteConfig?.websiteAvatar }}</span>
          </div>
          <div class="link-info-row">
            <span class="link-info-label">描述:</span>
            <span>{{ websiteConfig?.websiteIntro }}</span>
          </div>
        </div>
      </section>

      <section class="link-apply-section">
        <div class="link-title mb-4">
          <v-icon class="link-title-icon">mdi-orbit</v-icon> 申请友链
        </div>
        <div class="link-info-box">
          <p>提交后会进入后台审核，通过后才会展示在友链列表中。</p>
          <p>不会添加带有广告营销和没有实质性内容的友链。</p>
          <p>申请之前请先将本站添加为您的友链。</p>
        </div>
        <button class="link-galaxy-entry" type="button" @click="openGalaxyApply">
          <span class="link-galaxy-entry-orbit" aria-hidden="true">
            <span class="link-galaxy-entry-core">
              <v-icon size="28">mdi-star-four-points-outline</v-icon>
            </span>
            <span class="link-galaxy-entry-planet one" />
            <span class="link-galaxy-entry-planet two" />
            <span class="link-galaxy-entry-planet three" />
          </span>
          <span class="link-galaxy-entry-content">
            <span class="link-galaxy-entry-title">申请加入友链星系</span>
            <span class="link-galaxy-entry-text">点击打开星系申请舱，填写资料后进入后台审核轨道</span>
          </span>
          <span class="link-galaxy-entry-action">
            <v-icon size="22">mdi-rocket-launch-outline</v-icon>
            <span>发射申请</span>
          </span>
        </button>
      </section>

      <v-dialog v-model="applyGalaxyDialog" max-width="1080" class="link-galaxy-dialog">
        <div class="link-galaxy-panel">
          <button class="link-galaxy-close" type="button" @click="applyGalaxyDialog = false">
            <v-icon size="20">mdi-close</v-icon>
          </button>
          <div class="link-galaxy-header">
            <div>
              <div class="link-galaxy-kicker">Friend Link Galaxy</div>
              <h2>申请加入友链星系</h2>
              <p>你的站点会先进入待审核轨道，站长通过后会公开展示在友链列表中。</p>
            </div>
            <div class="link-galaxy-status">
              <v-icon size="18">mdi-progress-clock</v-icon>
              待审核
            </div>
          </div>
          <div class="link-galaxy-content">
            <div class="link-galaxy-orbit">
              <div class="link-galaxy-ring ring-one" aria-hidden="true" />
              <div class="link-galaxy-ring ring-two" aria-hidden="true" />
              <div class="link-galaxy-ring ring-three" aria-hidden="true" />
              <div class="link-galaxy-core">
                <v-icon size="30">mdi-home-heart</v-icon>
                <span>本站</span>
              </div>
              <div
                v-for="(planet, index) in visibleFriendPlanets"
                :key="planet.id"
                class="link-galaxy-planet"
                :style="planetStyle(index)"
              >
                <v-img :src="planet.linkCover" cover class="link-galaxy-planet-img" />
                <span>{{ planet.linkName }}</span>
              </div>
              <div class="link-galaxy-apply-planet">
                <v-icon size="24">mdi-plus</v-icon>
                <span>你的站点</span>
              </div>
              <div v-if="visibleFriendPlanets.length === 0" class="link-galaxy-empty">
                暂无公开友链，等待第一个星球加入
              </div>
            </div>

            <form class="link-galaxy-form-card" @submit.prevent="submitFriendLinkApply">
              <div class="link-galaxy-form-title">
                <v-icon size="22">mdi-card-account-details-star-outline</v-icon>
                星球档案
              </div>
              <div class="link-apply-form-grid">
                <label class="link-apply-field">
                  <span>网站名称</span>
                  <input v-model="applyForm.linkName" type="text" placeholder="请输入网站名称" />
                </label>
                <label class="link-apply-field">
                  <span>网站链接</span>
                  <input v-model="applyForm.linkAddress" type="url" placeholder="https://example.com" />
                </label>
                <label class="link-apply-field">
                  <span>网站封面</span>
                  <input v-model="applyForm.linkCover" type="url" placeholder="https://example.com/cover.png" />
                </label>
                <label class="link-apply-field link-apply-field-wide">
                  <span>网站简介</span>
                  <textarea v-model="applyForm.linkIntro" rows="3" placeholder="用一句话介绍你的网站" />
                </label>
              </div>
              <div class="link-apply-form-actions">
                <div class="link-apply-form-note">
                  <v-icon size="18">mdi-shield-check-outline</v-icon>
                  <span>默认进入审核轨道</span>
                </div>
                <button class="link-apply-submit" type="submit" :disabled="applyLoading">
                  <v-icon size="19">{{ applyLoading ? 'mdi-loading' : 'mdi-rocket-launch-outline' }}</v-icon>
                  <span>{{ applyLoading ? '发射中' : '发射申请' }}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </v-dialog>

      <div class="link-section-divider" aria-hidden="true" />

      <!-- 链接列表 -->
      <div class="link-title mb-4">
        <v-icon class="link-title-icon">mdi-link-variant</v-icon> 友情链接
      </div>
      <v-row class="link-grid">
        <v-col
          class="link-wrapper"
          lg="3"
          md="3"
          sm="6"
          cols="12"
          v-for="item of friendLinkList"
          :key="item.id"
        >
          <div class="link-card">
            <a
              :href="item.linkAddress"
              target="_blank"
              rel="noopener"
              class="link-card-main"
            >
              <!-- 使用友链封面作为卡片顶部图片。 -->
              <div class="link-card-cover">
                <v-img :src="item.linkCover" cover class="link-card-cover-img" />
                <span class="link-open-badge">
                  <v-icon size="16">mdi-arrow-top-right</v-icon>
                </span>
              </div>
              <div class="link-card-body">
                <div class="link-name">{{ item.linkName }}</div>
                <div class="link-intro">{{ item.linkIntro }}</div>
                <div class="link-card-footer">
                  <div class="link-meta">
                    <v-icon size="12">mdi-calendar-month-outline</v-icon>
                    {{ item.createTime }}
                  </div>
                </div>
              </div>
            </a>
          </div>
        </v-col>
      </v-row>
      <div v-if="friendLinkList.length === 0" class="empty-tip">
        暂无友链
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useBlogInfoStore } from '@/stores/blogInfo'
import { getLinks, sendFriendLinkApply } from '@/api/misc'
import { useToast } from '@/composables/useToast'

interface FriendLink {
  id: number
  linkName: string
  linkIntro: string
  linkCover: string
  linkAddress: string
  createTime: string
}

interface FriendLinkApplyForm {
  linkName: string
  linkIntro: string
  linkCover: string
  linkAddress: string
}

const blogInfoStore = useBlogInfoStore()

const friendLinkList = ref<FriendLink[]>([])
const applyLoading = ref(false)
const applyGalaxyDialog = ref(false)
const applyForm = ref<FriendLinkApplyForm>({
  linkName: '',
  linkIntro: '',
  linkCover: '',
  linkAddress: ''
})

// 单独保存友链页封面地址，既用于顶部 banner，也用于本站信息展示。
const linkCoverUrl = computed(() => {
  const pageList = blogInfoStore.blogInfo.pageList || []
  const linkPage = pageList.find(item => item.pageLabel === 'link')
  return linkPage?.pageCover || ''
})

const cover = computed(() => {
  return `background: url(${linkCoverUrl.value}) center center / cover no-repeat`
})

const websiteConfig = computed(() => {
  return blogInfoStore.blogInfo?.websiteConfig
})

const visibleFriendPlanets = computed(() => {
  return friendLinkList.value.slice(0, 6)
})

// 后端配置未显式提供网址时，用当前站点 origin 作为可复制的友链地址。
const siteAddress = computed(() => {
  const config = websiteConfig.value || {}
  return config.websiteUrl || config.websiteAddress || window.location.origin
})

/**
 * 生成星球在轨道上的固定位置，避免列表刷新造成布局抖动。
 *
 * @param index 当前星球序号。
 * @returns 可直接绑定到 style 的位置对象。
 */
function planetStyle(index: number) {
  const positions = [
    { left: '14%', top: '20%' },
    { left: '68%', top: '16%' },
    { left: '80%', top: '48%' },
    { left: '57%', top: '74%' },
    { left: '23%', top: '70%' },
    { left: '8%', top: '46%' }
  ]
  return positions[index % positions.length]
}

/**
 * 打开友链星系申请弹窗。
 */
function openGalaxyApply() {
  applyGalaxyDialog.value = true
}

/**
 * 重置友链申请表单。
 */
function resetApplyForm() {
  applyForm.value = {
    linkName: '',
    linkIntro: '',
    linkCover: '',
    linkAddress: ''
  }
}

/**
 * 校验申请表单，避免空数据进入后台审核列表。
 *
 * @returns 表单是否可提交。
 */
function validateApplyForm() {
  const form = applyForm.value
  if (!form.linkName.trim()) {
    useToast({ type: 'error', message: '请填写网站名称' })
    return false
  }
  if (!form.linkAddress.trim()) {
    useToast({ type: 'error', message: '请填写网站链接' })
    return false
  }
  if (!form.linkCover.trim()) {
    useToast({ type: 'error', message: '请填写网站封面' })
    return false
  }
  if (!form.linkIntro.trim()) {
    useToast({ type: 'error', message: '请填写网站简介' })
    return false
  }
  return true
}

/**
 * 提交友链申请，后端会将新记录设置为待审核。
 *
 * @returns Promise<void> 提交完成后给出用户反馈。
 */
async function submitFriendLinkApply() {
  if (!validateApplyForm() || applyLoading.value) return

  try {
    applyLoading.value = true
    const form = applyForm.value
    await sendFriendLinkApply({
      linkName: form.linkName.trim(),
      linkAddress: form.linkAddress.trim(),
      linkCover: form.linkCover.trim(),
      linkIntro: form.linkIntro.trim()
    })
    useToast({ type: 'success', message: '友链申请已提交，等待审核' })
    resetApplyForm()
    applyGalaxyDialog.value = false
  } catch (error) {
    console.error('提交友链申请失败:', error)
    useToast({ type: 'error', message: '提交失败，请稍后再试' })
  } finally {
    applyLoading.value = false
  }
}

/**
 * 获取公开友链列表。
 *
 * @returns Promise<void> 请求完成后刷新友链卡片数据。
 */
async function listFriendLinks() {
  try {
    const { data } = await getLinks()
    friendLinkList.value = data.data || []
  } catch (error) {
    console.error('获取友链列表失败:', error)
  }
}

onMounted(() => {
  listFriendLinks()
})
</script>

<style scoped>
.banner {
  position: relative;
  height: 380px;
  color: #eee;
}

.banner::before {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
}

.banner-title {
  position: absolute;
  bottom: 50px;
  width: 100%;
  text-align: center;
  font-size: 2rem;
  font-weight: 700;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}

.link-container {
  max-width: 1200px;
  padding: 30px;
  margin: 20px auto 40px !important;
  border: 1px solid var(--card-border-accent);
  border-radius: var(--card-radius-lg) !important;
  background: var(--card-surface-elevated);
  box-shadow: var(--card-shadow-raised);
}

.link-title {
  color: #344c67;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 22px;
  font-weight: 700;
  line-height: 2;
}

.link-title-icon {
  color: #5f8fd8;
}

.link-apply-section + .link-apply-section {
  margin-top: 18px;
}

.link-info-box {
  display: grid;
  gap: 12px;
  padding: 16px 18px;
  border-left: 3px solid #8eb6ee;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(239, 246, 255, 0.96), rgba(245, 249, 255, 0.96));
  color: #54677f;
  font-size: 0.95rem;
  line-height: 1.55;
}

.link-info-box p {
  margin: 0;
}

.link-info-row {
  display: flex;
  gap: 4px;
  min-width: 0;
  overflow-wrap: anywhere;
}

.link-info-label {
  flex: 0 0 auto;
  font-weight: 700;
  color: #3f5674;
}

.link-galaxy-entry {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr) auto;
  align-items: center;
  gap: 18px;
  width: 100%;
  margin-top: 16px;
  padding: 16px 18px;
  border: 1px solid rgba(142, 182, 238, 0.55);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(239, 246, 255, 0.94)),
    rgba(255, 255, 255, 0.8);
  box-shadow: 0 14px 34px rgba(95, 143, 216, 0.12);
  color: inherit;
  cursor: pointer;
  text-align: left;
  transition: transform 0.24s ease, box-shadow 0.24s ease, border-color 0.24s ease;
}

.link-galaxy-entry:hover {
  border-color: rgba(95, 149, 220, 0.72);
  box-shadow: 0 18px 38px rgba(95, 143, 216, 0.18);
  transform: translateY(-3px);
}

.link-galaxy-entry-orbit {
  position: relative;
  display: block;
  width: 118px;
  height: 92px;
  border: 1px dashed rgba(95, 149, 220, 0.52);
  border-radius: 50%;
  background: radial-gradient(circle at center, rgba(232, 242, 255, 0.92), transparent 60%);
}

.link-galaxy-entry-core,
.link-galaxy-entry-planet {
  position: absolute;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.link-galaxy-entry-core {
  left: 50%;
  top: 50%;
  width: 48px;
  height: 48px;
  background: #fff;
  color: #4c7ce6;
  box-shadow: 0 10px 20px rgba(95, 143, 216, 0.2);
  transform: translate(-50%, -50%);
}

.link-galaxy-entry-planet {
  width: 16px;
  height: 16px;
  background: #8eb6ee;
  box-shadow: 0 0 0 5px rgba(142, 182, 238, 0.18);
}

.link-galaxy-entry-planet.one {
  left: 16px;
  top: 26px;
}

.link-galaxy-entry-planet.two {
  right: 18px;
  top: 16px;
}

.link-galaxy-entry-planet.three {
  right: 30px;
  bottom: 18px;
}

.link-galaxy-entry-content {
  display: grid;
  gap: 6px;
  min-width: 0;
}

.link-galaxy-entry-title {
  color: #31537d;
  font-size: 1rem;
  font-weight: 800;
}

.link-galaxy-entry-text {
  color: #64748b;
  font-size: 0.9rem;
  line-height: 1.6;
}

.link-galaxy-entry-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 126px;
  height: 42px;
  border-radius: 8px;
  background: #6ea8f7;
  color: #fff;
  font-size: 0.9rem;
  font-weight: 800;
}

.link-galaxy-panel {
  position: relative;
  padding: 22px;
  border: 1px solid rgba(142, 182, 238, 0.42);
  border-radius: 8px;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(238, 246, 255, 0.96)),
    #fff;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
}

.link-galaxy-close {
  position: absolute;
  right: 16px;
  top: 16px;
  z-index: 2;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: 0;
  border-radius: 50%;
  background: rgba(232, 242, 255, 0.86);
  color: #4c7ce6;
  cursor: pointer;
}

.link-galaxy-header {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 18px;
  padding-right: 38px;
}

.link-galaxy-kicker {
  color: #5f8fd8;
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
}

.link-galaxy-header h2 {
  margin: 6px 0 6px;
  color: #26364a;
  font-size: 1.35rem;
  line-height: 1.3;
}

.link-galaxy-header p {
  margin: 0;
  color: #64748b;
  font-size: 0.92rem;
  line-height: 1.7;
}

.link-galaxy-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex: 0 0 auto;
  padding: 8px 11px;
  border-radius: 999px;
  background: rgba(232, 242, 255, 0.9);
  color: #4c7ce6;
  font-size: 0.82rem;
  font-weight: 800;
}

.link-galaxy-content {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(340px, 0.9fr);
  gap: 18px;
  margin-top: 20px;
}

.link-galaxy-orbit {
  position: relative;
  min-height: 430px;
  border: 1px solid rgba(142, 182, 238, 0.4);
  border-radius: 8px;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.94) 0 56px, transparent 58px),
    linear-gradient(135deg, rgba(247, 251, 255, 0.98), rgba(230, 241, 255, 0.78));
  overflow: hidden;
}

.link-galaxy-ring {
  position: absolute;
  left: 50%;
  top: 50%;
  border: 1px dashed rgba(95, 149, 220, 0.34);
  border-radius: 50%;
  transform: translate(-50%, -50%);
}

.ring-one {
  width: 190px;
  height: 190px;
}

.ring-two {
  width: 300px;
  height: 300px;
}

.ring-three {
  width: 410px;
  height: 410px;
}

.link-galaxy-core {
  position: absolute;
  left: 50%;
  top: 50%;
  display: grid;
  place-items: center;
  width: 86px;
  height: 86px;
  border-radius: 50%;
  background: #fff;
  color: #4c7ce6;
  box-shadow: 0 14px 30px rgba(95, 143, 216, 0.2);
  transform: translate(-50%, -50%);
  font-size: 0.82rem;
  font-weight: 900;
}

.link-galaxy-planet,
.link-galaxy-apply-planet {
  position: absolute;
  display: grid;
  place-items: center;
  width: 74px;
  min-height: 86px;
  color: #31537d;
  font-size: 0.72rem;
  font-weight: 800;
  text-align: center;
  transform: translate(-50%, -50%);
}

.link-galaxy-planet-img {
  width: 48px;
  height: 48px;
  border: 3px solid #fff;
  border-radius: 50%;
  box-shadow: 0 10px 20px rgba(95, 143, 216, 0.18);
}

.link-galaxy-planet span,
.link-galaxy-apply-planet span {
  width: 100%;
  margin-top: 7px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.link-galaxy-apply-planet {
  right: 16%;
  bottom: 14%;
  width: 86px;
  height: 92px;
  border: 1px dashed rgba(76, 124, 230, 0.52);
  border-radius: 18px;
  background: rgba(232, 242, 255, 0.86);
  color: #4c7ce6;
  transform: none;
}

.link-galaxy-empty {
  position: absolute;
  left: 50%;
  bottom: 24px;
  color: #64748b;
  font-size: 0.86rem;
  font-weight: 700;
  transform: translateX(-50%);
  white-space: nowrap;
}

.link-galaxy-form-card {
  display: grid;
  gap: 18px;
  align-self: stretch;
  padding: 18px;
  border: 1px solid rgba(142, 182, 238, 0.46);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.84);
}

.link-galaxy-form-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #31537d;
  font-size: 1rem;
  font-weight: 800;
}

.link-apply-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.link-apply-field {
  display: grid;
  gap: 7px;
  min-width: 0;
  color: #3f5674;
  font-size: 0.86rem;
  font-weight: 700;
}

.link-apply-field-wide {
  grid-column: 1 / -1;
}

.link-apply-field input,
.link-apply-field textarea {
  width: 100%;
  border: 1px solid rgba(142, 182, 238, 0.45);
  border-radius: 6px;
  outline: none;
  background: rgba(255, 255, 255, 0.86);
  color: #27364a;
  font: inherit;
  font-weight: 500;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.link-apply-field input {
  height: 40px;
  padding: 0 12px;
}

.link-apply-field textarea {
  resize: vertical;
  min-height: 96px;
  padding: 10px 12px;
}

.link-apply-field input:focus,
.link-apply-field textarea:focus {
  border-color: #6ea8f7;
  background: #fff;
  box-shadow: 0 0 0 3px rgba(110, 168, 247, 0.14);
}

.link-apply-form-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}

.link-apply-form-note {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #64748b;
  font-size: 0.86rem;
  font-weight: 700;
}

.link-apply-submit {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  min-width: 126px;
  height: 40px;
  border: 0;
  border-radius: 8px;
  background: #6ea8f7;
  color: #fff;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 700;
  transition: transform 0.24s ease, background 0.24s ease, box-shadow 0.24s ease;
}

.link-apply-submit:hover:not(:disabled) {
  background: #4c8fe8;
  box-shadow: 0 10px 22px rgba(76, 143, 232, 0.22);
  transform: translateY(-2px);
}

.link-apply-submit:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.link-section-divider {
  position: relative;
  margin: 40px 0 36px;
  border-top: 3px dashed #9ec5f8;
}

.link-section-divider::before {
  content: '';
  position: absolute;
  left: 58px;
  top: 0;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: #8eb6ee;
  box-shadow: 0 0 0 4px rgba(142, 182, 238, 0.18);
  transform: translateY(-50%);
}

.link-grid {
  margin-top: 6px;
}

.link-wrapper {
  padding: 10px !important;
}

.link-card {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
  min-height: 218px;
  border: 1px solid rgba(204, 220, 239, 0.9);
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.16);
  transition: transform 0.35s ease, box-shadow 0.35s ease, border-color 0.35s ease;
  overflow: hidden;
}

.link-card-main {
  position: relative;
  z-index: 1;
  display: flex;
  flex: 1;
  flex-direction: column;
  text-decoration: none;
  color: inherit;
}

.link-card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  overflow: hidden;
}

.link-card-cover-img {
  width: 100%;
  height: 100%;
  transition: transform 0.5s ease;
}

.link-open-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.86);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #4c7ce6;
  box-shadow: 0 10px 20px rgba(94, 166, 229, 0.16);
}

.link-card-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  padding: 11px 14px 9px;
}

.link-name {
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.98rem;
  font-weight: 700;
  line-height: 1.35;
  color: #1f2937;
}

.link-intro {
  width: 100%;
  margin-top: 6px;
  color: #222;
  font-size: 0.8rem;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  min-height: 2.8em;
}

.link-card:hover {
  border-color: var(--card-border-accent-hover);
  box-shadow: var(--card-shadow-hover);
  transform: translateY(-6px);
}

.link-card:hover .link-card-cover-img {
  transform: scale(1.04);
}

.link-card-footer {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  padding-top: 8px;
  margin-top: auto;
}

.link-meta {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  min-width: 0;
  font-size: 0.72rem;
  font-weight: 600;
  color: #7b8797;
}

.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
}

:global(.dark) .link-title {
  color: rgba(248, 250, 255, 0.94);
}

:global(.dark) .link-info-box {
  border-left-color: rgba(147, 197, 253, 0.82);
  background:
    linear-gradient(135deg, rgba(59, 130, 246, 0.16), rgba(125, 211, 252, 0.08)),
    rgba(15, 23, 42, 0.3);
  color: rgba(226, 232, 240, 0.88);
}

:global(.dark) .link-title-icon {
  color: rgba(147, 197, 253, 0.95);
}

:global(.dark) .link-info-label {
  color: rgba(248, 250, 255, 0.92);
}

:global(.dark) .link-galaxy-entry,
:global(.dark) .link-galaxy-panel,
:global(.dark) .link-galaxy-form-card {
  border-color: rgba(147, 197, 253, 0.22);
  background:
    linear-gradient(135deg, rgba(30, 41, 59, 0.82), rgba(15, 23, 42, 0.86)),
    rgba(15, 23, 42, 0.44);
  color: rgba(226, 232, 240, 0.9);
}

:global(.dark) .link-galaxy-entry-title,
:global(.dark) .link-galaxy-header h2,
:global(.dark) .link-galaxy-form-title {
  color: rgba(248, 250, 255, 0.94);
}

:global(.dark) .link-galaxy-entry-text,
:global(.dark) .link-galaxy-header p,
:global(.dark) .link-apply-form-note,
:global(.dark) .link-apply-field {
  color: rgba(226, 232, 240, 0.78);
}

:global(.dark) .link-galaxy-orbit {
  border-color: rgba(147, 197, 253, 0.22);
  background:
    radial-gradient(circle at center, rgba(30, 41, 59, 0.92) 0 56px, transparent 58px),
    linear-gradient(135deg, rgba(30, 41, 59, 0.86), rgba(15, 23, 42, 0.92));
}

:global(.dark) .link-apply-field input,
:global(.dark) .link-apply-field textarea {
  border-color: rgba(147, 197, 253, 0.22);
  background: rgba(30, 41, 59, 0.72);
  color: rgba(248, 250, 255, 0.9);
}

:global(.dark) .link-section-divider {
  border-top-color: rgba(147, 197, 253, 0.72);
}

:global(.dark) .link-section-divider::before {
  background: rgba(147, 197, 253, 0.92);
}

@media (max-width: 900px) {
  .link-galaxy-content {
    grid-template-columns: 1fr;
  }

  .link-galaxy-orbit {
    min-height: 360px;
  }

  .ring-three {
    width: 340px;
    height: 340px;
  }
}

@media (max-width: 759px) {
  .banner {
    height: 300px;
  }

  .banner-title {
    font-size: 1.5rem;
    bottom: 50px;
  }

  .link-container {
    margin: 20px 10px 20px;
    padding: 15px;
  }

  .link-wrapper {
    padding: 8px 0 !important;
  }

  .link-info-box {
    padding: 14px;
    font-size: 0.88rem;
  }

  .link-info-row {
    display: block;
  }

  .link-galaxy-entry {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .link-galaxy-entry-orbit,
  .link-galaxy-entry-action {
    justify-self: center;
  }

  .link-galaxy-panel {
    padding: 16px;
  }

  .link-galaxy-header {
    flex-direction: column;
    padding-right: 36px;
  }

  .link-galaxy-orbit {
    min-height: 310px;
  }

  .ring-one {
    width: 140px;
    height: 140px;
  }

  .ring-two {
    width: 220px;
    height: 220px;
  }

  .ring-three {
    width: 285px;
    height: 285px;
  }

  .link-galaxy-planet {
    width: 58px;
    font-size: 0.68rem;
  }

  .link-galaxy-planet-img {
    width: 40px;
    height: 40px;
  }

  .link-apply-form-grid {
    grid-template-columns: 1fr;
  }

  .link-apply-form-actions {
    align-items: stretch;
    flex-direction: column;
  }

  .link-apply-submit {
    width: 100%;
  }

  .link-section-divider {
    margin: 30px 0 28px;
  }

  .link-card {
    min-height: auto;
  }

  .link-card-body {
    padding: 12px 14px 10px;
  }

  .link-name {
    font-size: 0.98rem;
  }

  .link-intro {
    font-size: 0.82rem;
  }
}
</style>
