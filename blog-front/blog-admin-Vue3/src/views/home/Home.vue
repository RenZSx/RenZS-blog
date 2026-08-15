<template>
  <div class="blog-home">
    <!-- 统计卡片 -->
    <el-row class="stats-row" :gutter="16">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card stat-card--views">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">访问量</div>
              <div class="stat-value">{{ homeData.viewsCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card stat-card--users">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户量</div>
              <div class="stat-value">{{ homeData.userCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card stat-card--articles">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">文章量</div>
              <div class="stat-value">{{ homeData.articleCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card shadow="never" class="stat-card stat-card--messages">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon><ChatDotRound /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">留言量</div>
              <div class="stat-value">{{ homeData.messageCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 一周访问量 -->
    <el-card shadow="never" class="dashboard-card chart-card">
      <template #header>
        <div class="card-header">一周访问量</div>
      </template>
      <div ref="viewChartRef" class="chart-area chart-area--line" v-loading="loading"></div>
    </el-card>

    <!-- 文章贡献统计 -->
    <el-card shadow="never" class="dashboard-card heatmap-card">
      <template #header>
        <div class="card-header heatmap-header">
          <span>文章贡献统计</span>
          <div class="heatmap-stats" v-if="heatmapTotal > 0">
            <el-tag type="success" effect="plain" size="small">{{ heatmapTotal }} 篇文章</el-tag>
            <el-tag type="info" effect="plain" size="small">{{ heatmapActiveDays }} 个活跃日</el-tag>
          </div>
        </div>
      </template>
      <div v-loading="loading" class="heatmap-body">
        <!-- 文章贡献统计日历热力图 -->
        <div ref="heatmapRef" class="chart-area chart-area--heatmap"></div>
      </div>
    </el-card>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 文章浏览量排行 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">文章浏览量排行</div>
          </template>
          <div ref="rankChartRef" style="height: 350px" v-loading="loading"></div>
        </el-card>
      </el-col>

      <!-- 文章分类统计 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">文章分类统计</div>
          </template>
          <div ref="categoryChartRef" style="height: 350px" v-loading="loading"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 用户地域分布 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              用户地域分布
              <el-radio-group v-model="userType" size="small" style="float: right">
                <el-radio :label="1">用户</el-radio>
                <el-radio :label="2">游客</el-radio>
              </el-radio-group>
            </div>
          </template>
          <div ref="mapChartRef" style="height: 350px" v-loading="loading"></div>
        </el-card>
      </el-col>

      <!-- 文章标签统计 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">文章标签统计</div>
          </template>
          <div style="height: 350px; overflow: auto" v-loading="loading">
            <TagCloud :data="tagList" />
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { View, User, Document, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
// 注册中国地图（数据提取自 UMD 格式的 china.js，独立成 ESM 模块后 import 才不会触发 require 报错）
import '@/assets/js/chinaMap'
import { getHomeData, getUserArea } from '@/api/blog/home'
import TagCloud from '@/components/TagCloud/index.vue'
import useSettingsStore from '@/store/modules/settings'

const loading = ref(true)
const homeData = ref({})
const userType = ref(1)
const tagList = ref([])
const settingsStore = useSettingsStore()

const viewChartRef = ref(null)
const rankChartRef = ref(null)
const categoryChartRef = ref(null)
const mapChartRef = ref(null)
const heatmapRef = ref(null)
const heatmapTotal = ref(0)
const heatmapActiveDays = ref(0)

let viewChart = null
let rankChart = null
let categoryChart = null
let mapChart = null
let heatmap = null

// 获取首页数据
const fetchHomeData = async () => {
  try {
    loading.value = true
    const res = await getHomeData()
    if (res.flag) {
      homeData.value = res.data

      // 处理标签数据
      if (res.data.tagDTOList) {
        tagList.value = res.data.tagDTOList.map(item => ({
          id: item.id,
          name: item.tagName
        }))
      }

      await nextTick()
      initCharts(res.data)
    }
  } catch (error) {
    console.error('获取首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化图表
const initCharts = (data) => {
  const isDark = settingsStore.isDark
  const chartText = isDark ? '#d7dee8' : '#526173'
  const chartMuted = isDark ? '#8f9aaa' : '#8b98a8'
  const chartGrid = isDark ? '#2a3440' : '#e8edf3'

  // 一周访问量图表
  if (viewChartRef.value && data.uniqueViewDTOList) {
    viewChart = echarts.init(viewChartRef.value)
    const xData = []
    const yData = []
    data.uniqueViewDTOList.forEach(item => {
      xData.push(item.day)
      yData.push(item.viewsCount)
    })
    viewChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'cross' },
        backgroundColor: isDark ? '#252c35' : '#ffffff',
        borderColor: chartGrid,
        textStyle: { color: chartText }
      },
      color: ['#4f7cff'],
      legend: { data: ['访问量'], textStyle: { color: chartText } },
      grid: {
        left: '2%',
        right: '2%',
        top: '12%',
        bottom: '5%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: xData,
        boundaryGap: false,
        axisLine: { lineStyle: { color: chartGrid } },
        axisLabel: { color: chartMuted }
      },
      yAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: chartGrid, type: 'dashed' } },
        axisLabel: { color: chartMuted }
      },
      series: [{
        name: '访问量',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 7,
        lineStyle: { width: 3 },
        itemStyle: { borderWidth: 2, borderColor: isDark ? '#1d1e1f' : '#fff' },
        areaStyle: { color: 'rgba(79, 124, 255, 0.10)' },
        data: yData
      }]
    })
  }

  // 文章浏览量排行
  if (rankChartRef.value && data.articleRankDTOList) {
    rankChart = echarts.init(rankChartRef.value)
    const xData = []
    const yData = []
    data.articleRankDTOList.forEach(item => {
      xData.push(item.articleTitle)
      yData.push(item.viewsCount)
    })
    rankChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' }
      },
      color: ['#58AFFF'],
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: { type: 'category', data: xData },
      yAxis: { type: 'value' },
      series: [{
        name: '浏览量',
        type: 'bar',
        data: yData
      }]
    })
  }

  // 文章分类统计
  if (categoryChartRef.value && data.categoryDTOList) {
    categoryChart = echarts.init(categoryChartRef.value)
    const categoryData = data.categoryDTOList.map(item => ({
      value: item.articleCount,
      name: item.categoryName
    }))
    categoryChart.setOption({
      tooltip: { trigger: 'item' },
      color: ['#7EC0EE', '#FF9F7F', '#FFD700', '#C9C9C9', '#E066FF', '#C0FF3E'],
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      series: [{
        name: '文章分类',
        type: 'pie',
        radius: '50%',
        roseType: 'radius',
        data: categoryData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }]
    })
  }

  // 文章贡献统计日历热力图
  if (heatmapRef.value && data.articleStatisticsList && data.articleStatisticsList.length > 0) {
    heatmap = echarts.init(heatmapRef.value)
    const heatmapData = data.articleStatisticsList.map(item => [item.date, item.count])
    const dates = heatmapData.map(item => item[0]).sort()
    const minDate = dates[0]
    const maxDate = dates[dates.length - 1]
    // 同年数据用年份作为范围，跨年数据用起止日期
    const range = minDate.slice(0, 4) === maxDate.slice(0, 4) ? minDate.slice(0, 4) : [minDate, maxDate]
    const maxCount = Math.max(...heatmapData.map(item => item[1]))
    // 统计信息
    heatmapTotal.value = heatmapData.reduce((sum, item) => sum + item[1], 0)
    heatmapActiveDays.value = heatmapData.length
    heatmap.setOption({
      tooltip: {
        backgroundColor: isDark ? '#252c35' : '#ffffff',
        borderColor: chartGrid,
        textStyle: { color: chartText },
        formatter: function(params) {
          const count = params.value ? params.value[1] : 0
          return '<div style="font-weight:bold;margin-bottom:4px">' + params.value[0] + '</div>' +
            (count > 0 ? '发布了 <b style="color:#239a3b">' + count + '</b> 篇文章' : '无文章发布')
        }
      },
      visualMap: {
        min: 0,
        max: maxCount > 0 ? maxCount : 1,
        calculable: false,
        orient: 'horizontal',
        left: 'center',
        bottom: 0,
        text: ['多', '少'],
        itemHeight: 10,
        itemWidth: 12,
        textStyle: { fontSize: 11, color: chartMuted },
        inRange: {
          color: isDark
            ? ['#26323a', '#315448', '#3c8060', '#47ad76', '#9be6b3']
            : ['#eef3f1', '#c5e8d2', '#84c99b', '#3fa96b', '#167143']
        }
      },
      calendar: {
        range: range,
        cellSize: ['auto', 18],
        top: 28,
        left: 34,
        right: 34,
        bottom: 34,
        splitLine: {
          show: false
        },
        itemStyle: {
          borderWidth: 2,
          borderColor: isDark ? '#1d1e1f' : '#fff',
          borderRadius: 4
        },
        yearLabel: { show: false },
        dayLabel: { show: true, firstDay: 1, nameMap: 'cn', fontSize: 10, color: chartMuted },
        monthLabel: { show: true, nameMap: 'cn', fontSize: 11, color: chartText }
      },
      series: [{
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: heatmapData
      }]
    })
  }

  // 用户地域分布
  fetchUserArea()
}

// 获取用户地域分布
const fetchUserArea = async () => {
  try {
    const res = await getUserArea(userType.value)
    if (res.flag && mapChartRef.value) {
      if (!mapChart) {
        mapChart = echarts.init(mapChartRef.value)
      }
      mapChart.setOption({
        tooltip: {
          formatter: function(e) {
            const value = e.value ? e.value : 0
            return e.seriesName + '<br />' + e.name + '：' + value
          }
        },
        visualMap: {
          min: 0,
          max: 1000,
          right: 26,
          bottom: 40,
          pieces: [
            { gt: 100, label: '100人以上', color: '#ED5351' },
            { gte: 51, lte: 100, label: '51-100人', color: '#59D9A5' },
            { gte: 21, lte: 50, label: '21-50人', color: '#F6C021' },
            { gt: 0, lte: 20, label: '1-20人', color: '#6DCAEC' }
          ]
        },
        series: [{
          name: '用户人数',
          type: 'map',
          map: 'china',
          data: res.data || []
        }]
      })
    }
  } catch (error) {
    console.error('获取用户地域分布失败:', error)
  }
}

// 监听用户类型变化
watch(userType, () => {
  fetchUserArea()
})

const disposeCharts = () => {
  const charts = [viewChart, rankChart, categoryChart, mapChart, heatmap]
  charts.forEach(chart => chart?.dispose())
  viewChart = null
  rankChart = null
  categoryChart = null
  mapChart = null
  heatmap = null
}

// ECharts does not inherit Element Plus theme variables, so redraw on theme changes.
watch(() => settingsStore.isDark, async () => {
  if (!Object.keys(homeData.value).length) return
  disposeCharts()
  await nextTick()
  initCharts(homeData.value)
})

onMounted(() => {
  fetchHomeData()
})

onBeforeUnmount(() => {
  disposeCharts()
})
</script>

<style scoped lang="scss">
.blog-home {
  min-height: calc(100vh - 84px);
  padding: 20px;
  background: var(--el-bg-color-page, #f4f7fb);
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  --stat-accent: #3268ef;
  --stat-icon-shadow: rgba(50, 104, 239, 0.24);
  position: relative;
  min-height: 124px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter, #e7edf4);
  border-radius: 10px;
  background: var(--el-bg-color-overlay, #ffffff);
  box-shadow: 0 5px 18px rgba(30, 55, 90, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &::before {
    position: absolute;
    top: 0;
    bottom: 0;
    left: 0;
    width: 4px;
    background: var(--stat-accent);
    content: '';
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 9px 24px rgba(30, 55, 90, 0.1);
  }

  :deep(.el-card__body) {
    height: 100%;
    padding: 20px 22px 20px 24px !important;
  }

  &--users {
    --stat-accent: #0b9365;
    --stat-icon-shadow: rgba(11, 147, 101, 0.24);
  }

  &--articles {
    --stat-accent: #d97706;
    --stat-icon-shadow: rgba(217, 119, 6, 0.25);
  }

  &--messages {
    --stat-accent: #8250df;
    --stat-icon-shadow: rgba(130, 80, 223, 0.25);
  }

  .stat-content {
    display: flex;
    align-items: center;
    min-height: 82px;
  }

  .stat-icon {
    display: flex;
    flex: 0 0 52px;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 14px;
    background: var(--stat-accent);
    box-shadow: 0 8px 18px var(--stat-icon-shadow);
    color: #ffffff;
    font-size: 27px;
  }

  .stat-info {
    min-width: 0;
    margin-left: 16px;
  }

  .stat-label {
    margin-bottom: 6px;
    color: var(--el-text-color-secondary, #8491a5);
    font-size: 13px;
    font-weight: 500;
  }

  .stat-value {
    color: var(--el-text-color-primary, #1d2939);
    font-size: 30px;
    font-weight: 700;
    line-height: 1;
  }
}

.dashboard-card {
  margin-top: 16px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter, #e7edf4);
  border-radius: 10px;
  background: var(--el-bg-color-overlay, #ffffff);
  box-shadow: 0 5px 18px rgba(30, 55, 90, 0.05);

  :deep(.el-card__header) {
    min-height: 52px;
    padding: 16px 20px !important;
    border-bottom-color: var(--el-border-color-lighter, #e7edf4);
  }

  :deep(.el-card__body) {
    padding: 18px 20px 22px !important;
  }
}

.chart-area--line {
  height: 300px;
}

.heatmap-body {
  min-height: 250px;
}

.chart-area--heatmap {
  height: 250px;
}

.card-header {
  display: flex;
  align-items: center;
  min-height: 20px;
  color: var(--el-text-color-primary, #202a34);
  font-size: 15px;
  font-weight: 650;

  &::before {
    width: 4px;
    height: 17px;
    margin-right: 10px;
    border-radius: 4px;
    background: var(--el-color-primary, #4f7cff);
    content: '';
  }

  :deep(.el-radio-group) {
    margin-left: auto !important;
  }
}

.heatmap-header {
  justify-content: space-between;
}

.heatmap-header::before {
  flex: 0 0 auto;
}

.heatmap-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: 16px;
}

.heatmap-stats .el-tag {
  height: 26px;
  border-radius: 13px;
  font-weight: 600;
  line-height: 24px;
}

@media screen and (max-width: 768px) {
  .blog-home {
    padding: 12px;
  }

  .stat-card {
    margin-bottom: 12px;
  }

  .chart-area--line {
    height: 250px;
  }

  .chart-area--heatmap {
    height: 220px;
  }

  .heatmap-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .heatmap-stats {
    margin-left: 14px;
  }
}

.text-center {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>
