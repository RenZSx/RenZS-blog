<template>
  <article class="section-card" :data-section-index="colorIndex">
    <div class="section-card__accent" :class="accentClass"></div>
    <div class="section-card__body">
      <div class="section-card__header">
        <h3>{{ section.sectionName }}</h3>
        <span class="section-card__count">{{ section.articleList.length }} 篇</span>
      </div>
      <ul class="section-card__articles">
        <li
          v-for="article in section.articleList.slice(0, 3)"
          :key="article.id || article.articleId"
        >
          <RouterLink :to="getArticlePath(article)">
            {{ getArticleTitle(article) }}
          </RouterLink>
        </li>
      </ul>
      <RouterLink
        v-if="sectionLink"
        :to="sectionLink"
        class="section-card__more"
      >
        查看全部 →
      </RouterLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink } from 'vue-router'
import {
  getArticlePath,
  getArticleTitle,
  getSectionLink,
  type ArticleSection
} from '../homeColumnsContent'

const props = defineProps<{
  section: ArticleSection
  colorIndex: number
}>()

const accentColors = [
  'section-card__accent--teal',
  'section-card__accent--warm',
  'section-card__accent--green',
  'section-card__accent--purple',
  'section-card__accent--yellow',
  'section-card__accent--cyan'
]

const accentClass = computed(() => accentColors[props.colorIndex % accentColors.length])
const sectionLink = computed(() => getSectionLink(props.section))
</script>

<style scoped>
.section-card {
  display: flex;
  overflow: hidden;
  border: 1px solid #e6e1dc;
  border-radius: 10px;
  background: #fff;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.section-card:hover {
  border-color: #d8d0c8;
  box-shadow: 0 14px 34px rgba(31, 41, 51, 0.08);
  transform: translateY(-2px);
}

.section-card__accent {
  flex-shrink: 0;
  width: 4px;
}

.section-card__accent--teal { background: #0b6f76; }
.section-card__accent--warm { background: #c95d3d; }
.section-card__accent--green { background: #178060; }
.section-card__accent--purple { background: #6b55c7; }
.section-card__accent--yellow { background: #9d6a00; }
.section-card__accent--cyan { background: #0b7682; }

.section-card__body {
  flex: 1;
  min-width: 0;
  padding: 20px;
}

.section-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-card__header h3 {
  margin: 0;
  color: #111827;
  font-size: 18px;
  overflow-wrap: anywhere;
}

.section-card__count {
  flex-shrink: 0;
  padding: 3px 8px;
  border-radius: 6px;
  color: #0b6f76;
  font-size: 12px;
  font-weight: 700;
  background: #dff4f3;
}

.section-card__articles {
  display: flex;
  flex-direction: column;
  gap: 0;
  margin: 0;
  padding: 0;
  list-style: none;
}

.section-card__articles li + li {
  border-top: 1px solid #ece7e2;
}

.section-card__articles a {
  display: block;
  padding: 10px 0;
  color: #3d4a5d;
  font-size: 14px;
  text-decoration: none;
  overflow-wrap: anywhere;
}

.section-card__articles a:hover {
  color: #0b6f76;
}

.section-card__more {
  display: inline-block;
  margin-top: 12px;
  color: #0b6f76;
  font-size: 13px;
  font-weight: 700;
  text-decoration: none;
}

.section-card__more:hover {
  text-decoration: underline;
}

:global(.dark) .section-card {
  border-color: rgba(186, 200, 224, 0.16);
  background: rgba(24, 31, 43, 0.92);
}

:global(.dark) .section-card__header h3 {
  color: #edf3ff;
}

:global(.dark) .section-card__articles a {
  color: rgba(226, 235, 248, 0.72);
}

:global(.dark) .section-card__articles a:hover {
  color: #93c5fd;
}

:global(.dark) .section-card__articles li + li {
  border-top-color: rgba(186, 200, 224, 0.14);
}

:global(.dark) .section-card__count {
  color: #93c5fd;
  background: rgba(59, 130, 246, 0.16);
}

:global(.dark) .section-card__more {
  color: #93c5fd;
}
</style>
