<template>
  <div>
    <div class="aritcle-copyright article-panel article-copyright-card">
      <div>
        <span>文章作者：</span>
        <router-link to="/">
          {{ websiteConfig.websiteAuthor }}
        </router-link>
      </div>
      <div>
        <span>文章链接：</span>
        <a :href="articleHref" target="_blank">{{ articleHref }} </a>
      </div>
      <div>
        <span>版权声明：</span>本博客所有文章除特别声明外，均采用
        <a
          href="https://creativecommons.org/licenses/by-nc-sa/4.0/"
          target="_blank"
        >
          CC BY-NC-SA 4.0
        </a>
        许可协议。转载请注明文章出处。
      </div>
    </div>
    <div class="article-operation article-panel article-operation-bar">
      <div class="tag-container">
        <router-link
          v-for="item of article.tagDTOList"
          :key="item.id"
          :to="'/tags/' + item.id"
        >
          {{ item.tagName }}
        </router-link>
      </div>
      <div class="article-share">
        <v-btn
          v-for="site in shareSites"
          :key="site"
          icon
          size="small"
          variant="text"
          @click="shareTo(site)"
        >
          <v-icon size="18">{{ getShareIcon(site) }}</v-icon>
        </v-btn>
      </div>
    </div>
    <div class="article-reward article-panel">
      <a :class="isLike" @click="$emit('like')">
        <v-icon size="14" color="#fff">mdi-thumb-up</v-icon> 点赞
        <span v-show="article.likeCount > 0">{{ article.likeCount }}</span>
      </a>
      <a
        :class="isCollected ? 'collect-btn-active' : 'collect-btn'"
        @click="$emit('collect')"
      >
        <v-icon size="14" color="#fff">mdi-bookmark</v-icon>
        {{ isCollected ? '已收藏' : '收藏' }}
      </a>
      <a class="reward-btn" v-if="websiteConfig.isReward == 1">
        <v-icon size="14">mdi-qrcode</v-icon> 打赏
        <div class="animated fadeInDown reward-main">
          <ul class="reward-all">
            <li class="reward-item">
              <img class="reward-img" :src="websiteConfig.weiXinQRCode" />
              <div class="reward-desc">微信</div>
            </li>
            <li class="reward-item">
              <img class="reward-img" :src="websiteConfig.alipayQRCode" />
              <div class="reward-desc">支付宝</div>
            </li>
          </ul>
        </div>
      </a>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Tag {
  id: number
  tagName: string
}

interface Article {
  id: number
  likeCount: number
  tagDTOList?: Tag[]
}

interface WebsiteConfig {
  websiteAuthor?: string
  isReward?: number
  weiXinQRCode?: string
  alipayQRCode?: string
}

interface Props {
  article: Article
  websiteConfig: WebsiteConfig
  articleHref: string
  isLike: string
  isCollected: boolean
}

defineProps<Props>()
defineEmits<{
  like: []
  collect: []
}>()

const shareSites = ['wechat', 'weibo', 'qq']

function getShareIcon(site: string) {
  const icons: Record<string, string> = {
    wechat: 'mdi-wechat',
    weibo: 'mdi-sina-weibo',
    qq: 'mdi-qqchat'
  }
  return icons[site] || 'mdi-share-variant'
}

function shareTo(site: string) {
  // 分享功能实现
  console.log('share to:', site)
}
</script>

<style scoped>
.article-panel {
  border: 1px solid var(--glass-border);
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
  transition: border-color var(--transition-normal),
    box-shadow var(--transition-normal), transform var(--transition-normal);
}

.article-panel:hover {
  border-color: var(--glass-border-hover);
  box-shadow: var(--glass-shadow-hover);
  transform: translateY(-2px);
}

.article-copyright-card,
.article-operation-bar,
.article-reward {
  margin-right: 42px;
  margin-left: 42px;
}

.article-copyright-card,
.article-operation-bar {
  margin-top: 28px;
}

.article-operation {
  display: flex;
  align-items: center;
}

.article-operation-bar {
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px 16px;
  padding: 18px 22px;
  border-radius: 20px;
}

.article-share {
  margin-left: auto;
}

.tag-container {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tag-container a {
  display: inline-flex;
  align-items: center;
  padding: 0 0.95rem;
  border: 1px solid var(--card-border-accent);
  border-radius: 999px;
  background: rgba(73, 177, 245, 0.08);
  color: #2a93d5 !important;
  font-size: 12px;
  line-height: 2.35;
  text-decoration: none;
  transition: all 0.3s ease;
}

.tag-container a:hover {
  transform: translateY(-1px);
  color: #fff !important;
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  box-shadow: 0 10px 24px rgba(73, 177, 245, 0.24);
}

.aritcle-copyright {
  position: relative;
  padding: 1rem 1.25rem;
  border-radius: 20px;
  font-size: 0.92rem;
  line-height: 2;
}

.aritcle-copyright span {
  color: #49b1f5;
  font-weight: bold;
}

.aritcle-copyright a {
  text-decoration: underline !important;
  color: #73849b !important;
}

.aritcle-copyright:before {
  position: absolute;
  top: 1rem;
  right: 1rem;
  width: 0.95rem;
  height: 0.95rem;
  border-radius: 50%;
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  content: "";
}

.aritcle-copyright:after {
  position: absolute;
  top: 1.2rem;
  right: 1.2rem;
  width: 0.55rem;
  height: 0.55rem;
  border-radius: 50%;
  background: #fff;
  content: "";
}

.article-reward {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 16px;
  margin-top: 32px;
  margin-bottom: 4px;
  padding: 24px;
  border-radius: 20px;
}

.reward-btn,
.like-btn,
.like-btn-active,
.collect-btn,
.collect-btn-active {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 112px;
  padding: 0 20px;
  border-radius: 999px;
  color: #fff !important;
  text-align: center;
  line-height: 40px;
  font-size: 0.9rem;
  cursor: pointer;
  text-decoration: none;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;
}

.like-btn {
  background: linear-gradient(135deg, #8d96a8, #737d90);
}

.like-btn-active {
  background: linear-gradient(135deg, #ff8a65, #ec7259);
  box-shadow: 0 14px 28px rgba(236, 114, 89, 0.28);
}

.collect-btn {
  background: linear-gradient(135deg, #5b84ff, #4a67d6);
}

.collect-btn-active {
  background: linear-gradient(135deg, #18b779, #0f9a65);
  box-shadow: 0 14px 28px rgba(24, 183, 121, 0.24);
}

.reward-btn {
  position: relative;
  background: linear-gradient(135deg, #49b1f5, #6c8dff);
  box-shadow: 0 14px 28px rgba(73, 177, 245, 0.24);
}

.reward-btn:hover,
.like-btn:hover,
.like-btn-active:hover,
.collect-btn:hover,
.collect-btn-active:hover {
  transform: translateY(-2px);
}

.reward-btn:hover .reward-main {
  display: block;
}

.reward-main {
  display: none;
  position: absolute;
  bottom: 48px;
  left: 50%;
  margin: 0;
  padding: 0 0 15px;
  transform: translateX(-50%);
}

.reward-all {
  display: inline-block;
  padding: 18px 12px 10px !important;
  width: 320px;
  border: 1px solid var(--glass-border);
  border-radius: 18px;
  background: var(--glass-surface);
  box-shadow: var(--glass-shadow);
  backdrop-filter: var(--glass-blur);
  -webkit-backdrop-filter: var(--glass-blur);
}

.reward-all:before {
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 20px;
  content: "";
}

.reward-all:after {
  content: "";
  position: absolute;
  right: 0;
  bottom: 2px;
  left: 0;
  margin: 0 auto;
  width: 0;
  height: 0;
  border-top: 13px solid rgba(246, 251, 255, 0.82);
  border-right: 13px solid transparent;
  border-left: 13px solid transparent;
}

.reward-item {
  display: inline-block;
  padding: 0 8px;
  list-style-type: none;
}

.reward-img {
  display: block;
  width: 130px;
  height: 130px;
  border-radius: 12px;
}

.reward-desc {
  margin: -5px 0;
  color: #6e7b8c;
  text-align: center;
}

@media (max-width: 759px) {
  .article-copyright-card,
  .article-operation-bar,
  .article-reward {
    margin-right: 18px;
    margin-left: 18px;
  }

  .article-reward {
    flex-wrap: wrap;
    padding: 18px;
  }
}
</style>
