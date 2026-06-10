<template>
  <div class="quick-category-cards">
    <router-link
      v-for="card in cards"
      :key="card.category?.id"
      :to="'/categories/' + card.category?.id"
      class="home-quick-card"
      :class="card.className"
    >
      <div class="quick-card-content">
        <div class="quick-card-name">{{ card.category?.categoryName }}</div>
        <div class="quick-card-desc">{{ card.description }}</div>
      </div>
    </router-link>
  </div>
</template>

<script setup lang="ts">
interface Category {
  id: number
  categoryName: string
}

interface QuickCard {
  index: number
  description: string
  className: string
  category?: Category
}

interface Props {
  cards: QuickCard[]
}

defineProps<Props>()
</script>

<style scoped>
.quick-category-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-quick-card {
  position: relative;
  overflow: hidden;
  border-radius: 15px !important;
  padding: 0.8rem 1rem;
  text-decoration: none;
  transition: transform 0.35s ease, box-shadow 0.35s ease;
}

.home-quick-card::before {
  content: "";
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    rgba(255, 255, 255, 0.24) 0%,
    rgba(255, 255, 255, 0.08) 38%,
    transparent 68%
  );
  pointer-events: none;
}

.home-quick-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 34px rgba(15, 23, 42, 0.14);
}

.quick-card-content {
  position: relative;
  z-index: 1;
}

.quick-card-name {
  color: #fff;
  font-size: 0.88rem;
  font-weight: 700;
  margin-bottom: 2px;
}

.quick-card-desc {
  color: rgba(255, 255, 255, 0.85);
  font-size: 0.7rem;
}

.home-quick-card-blue {
  background-image: linear-gradient(-227deg, #3c8ce7 10%, #00eaff 100%);
}

.home-quick-card-green {
  background: repeating-linear-gradient(to left, #a9f99e, #46add5);
}

.home-quick-card-sunset {
  background-image: linear-gradient(83deg, #fd6e6a 50%, #ffc600 100%);
}

.home-quick-card-purple {
  background: repeating-linear-gradient(to left, #00f2fe, #8e2de2);
}
</style>
