<template>
  <div ref="homeRef" class="blog-home">
    <el-alert
      v-if="loadError"
      title="首页数据加载失败，请稍后重试"
      type="error"
      show-icon
      :closable="false"
      class="home-error"
    >
      <el-button type="primary" size="small" @click="fetchHomeData">重试</el-button>
    </el-alert>

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
          <div class="heatmap-title">
            <span>文章贡献统计</span>
            <span class="heatmap-title__sub" v-if="heatmapTotal > 0">
              {{ heatmapYear }} 年 共发布 <b>{{ heatmapTotal }}</b> 篇
            </span>
          </div>
          <div class="heatmap-header__right" v-if="heatmapTotal > 0">
            <div class="heatmap-summary">
              <div class="heatmap-summary__item">
                <span class="heatmap-summary__value">{{ heatmapActiveDays }}</span>
                <span class="heatmap-summary__label">活跃日</span>
              </div>
              <div class="heatmap-summary__divider"></div>
              <div class="heatmap-summary__item">
                <span class="heatmap-summary__value">{{ heatmapMaxStreak }}</span>
                <span class="heatmap-summary__label">最长连续</span>
              </div>
              <div class="heatmap-summary__divider"></div>
              <div class="heatmap-summary__item">
                <span class="heatmap-summary__value">{{ heatmapBestCount }}</span>
                <span class="heatmap-summary__label">单日最高</span>
              </div>
            </div>
            <el-radio-group
              v-if="heatmapYearOptions.length > 1"
              v-model="heatmapYear"
              size="small"
              class="heatmap-year"
            >
              <el-radio-button v-for="y in heatmapYearOptions" :key="y" :value="y">{{ y }}</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <div v-loading="loading" class="heatmap-body">
        <!-- 文章贡献统计日历热力图 -->
        <div ref="heatmapRef" class="chart-area chart-area--heatmap" v-if="heatmapHasData"></div>
        <el-empty v-else description="暂无文章发布记录" :image-size="80" class="heatmap-empty" />
        <div class="heatmap-legend" v-if="heatmapHasData">
          <span>少</span>
          <i v-for="c in heatmapLegendColors" :key="c" :style="{ background: c }"></i>
          <span>多</span>
        </div>
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
import { ref, computed, onMounted, onActivated, onBeforeUnmount, watch, nextTick } from 'vue'
import { View, User, Document, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
// 注册中国地图（数据提取自 UMD 格式的 china.js，独立成 ESM 模块后 import 才不会触发 require 报错）
import '@/assets/js/chinaMap'
import { getHomeData, getUserArea } from '@/api/blog/home'
import TagCloud from '@/components/TagCloud/index.vue'
import useSettingsStore from '@/store/modules/settings'



const loading = ref(true)
const loadError = ref(false)
const homeData = ref({})
const userType = ref(1)
const tagList = ref([])
const settingsStore = useSettingsStore()
const homeRef = ref(null)

const viewChartRef = ref(null)
const rankChartRef = ref(null)
const categoryChartRef = ref(null)
const mapChartRef = ref(null)
const heatmapRef = ref(null)
const heatmapTotal = ref(0)
const heatmapActiveDays = ref(0)
const heatmapMaxStreak = ref(0)
const heatmapBestCount = ref(0)
const heatmapYear = ref('')
const heatmapYearOptions = ref([])
const heatmapHasData = ref(false)
const articleStatsList = ref([])

// 与主题蓝色系一致的色阶,跟随明暗主题切换(与 initCharts 中 inRange 保持一致)
const heatmapLegendColors = computed(() =>
  settingsStore.isDark
    ? ['#2b3b55', '#33518b', '#3d6fc4', '#4f8cff', '#8db9ff']
    : ['#dce8ff', '#a9c5ff', '#78a3ff', '#4f7cff', '#2b56d6']
)

let viewChart = null
let rankChart = null
let categoryChart = null
let mapChart = null
let heatmap = null
let heatmapRenderedYear = ''
let resizeObserver = null

// 标题过长时截断,完整内容通过 tooltip 查看
const truncate = (str, len = 10) => (str && str.length > len ? str.slice(0, len) + '…' : str)

// 获取首页数据
const fetchHomeData = async () => {
  try {
    loading.value = true
    loadError.value = false
    const res = await getHomeData()
    if (res.flag) {
      homeData.value = res.data
      // 提前标记热力图是否有数据,确保 nextTick 后 heatmapRef 已随 v-if 渲染
      heatmapHasData.value = (res.data.articleStatisticsList || []).length > 0

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
    loadError.value = true
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
    if (!viewChart) viewChart = echarts.init(viewChartRef.value)
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

  // 文章浏览量排行(横向条形图,长标题在轴上截断,tooltip 显示全称)
  if (rankChartRef.value && data.articleRankDTOList) {
    if (!rankChart) rankChart = echarts.init(rankChartRef.value)
    const rankList = [...data.articleRankDTOList].sort((a, b) => a.viewsCount - b.viewsCount)
    rankChart.setOption({
      tooltip: {
        trigger: 'axis',
        axisPointer: { type: 'shadow' },
        backgroundColor: isDark ? '#252c35' : '#ffffff',
        borderColor: chartGrid,
        textStyle: { color: chartText },
        formatter: (params) => {
          const p = Array.isArray(params) ? params[0] : params
          const item = rankList[p.dataIndex]
          if (!item) return ''
          return item.articleTitle + '<br/>浏览量：<b>' + item.viewsCount + '</b>'
        }
      },
      grid: {
        left: '3%',
        right: '10%',
        top: '6%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'value',
        splitLine: { lineStyle: { color: chartGrid, type: 'dashed' } },
        axisLabel: { color: chartMuted }
      },
      yAxis: {
        type: 'category',
        data: rankList.map(item => truncate(item.articleTitle)),
        axisLine: { lineStyle: { color: chartGrid } },
        axisTick: { show: false },
        axisLabel: { color: chartText, fontSize: 12 }
      },
      series: [{
        name: '浏览量',
        type: 'bar',
        barMaxWidth: 16,
        itemStyle: { borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right', color: chartMuted, fontSize: 11 },
        data: rankList.map(item => item.viewsCount)
      }]
    }, true)
  }

  // 文章分类统计(环形图,配色与统计卡片的主色系一致)
  if (categoryChartRef.value && data.categoryDTOList) {
    if (!categoryChart) categoryChart = echarts.init(categoryChartRef.value)
    const categoryData = data.categoryDTOList.map(item => ({
      value: item.articleCount,
      name: item.categoryName
    }))
    categoryChart.setOption({
      tooltip: {
        trigger: 'item',
        backgroundColor: isDark ? '#252c35' : '#ffffff',
        borderColor: chartGrid,
        textStyle: { color: chartText }
      },
      color: ['#4f7cff', '#22b07d', '#f59e0b', '#8250df', '#ef6c6c', '#38bdf8', '#94a3b8'],
      legend: {
        orient: 'vertical',
        left: 'left',
        top: 'middle',
        textStyle: { color: chartMuted }
      },
      series: [{
        name: '文章分类',
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['58%', '50%'],
        itemStyle: {
          borderColor: isDark ? '#1d1e1f' : '#ffffff',
          borderWidth: 2,
          borderRadius: 4
        },
        label: { color: chartMuted, fontSize: 11 },
        data: categoryData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.3)'
          }
        }
      }]
    }, true)
  }

  // 文章贡献统计日历热力图
  articleStatsList.value = data.articleStatisticsList || []
  initHeatmap()

  // 用户地域分布
  fetchUserArea()
}

// 渲染当前所选年份的贡献热力图(与 GitHub 一致,按年份切换、每次只渲染一年)
const initHeatmap = () => {
  const stats = articleStatsList.value
  if (!heatmapRef.value || !stats.length) return
  const isDark = settingsStore.isDark
  const chartText = isDark ? '#d7dee8' : '#526173'
  const chartMuted = isDark ? '#8f9aaa' : '#8b98a8'

  // 可选年份倒序排列,默认选中最近有数据的年份
  const years = [...new Set(stats.map(item => item.date.slice(0, 4)))].sort().reverse()
  heatmapYearOptions.value = years
  if (!heatmapYear.value || !years.includes(heatmapYear.value)) {
    heatmapYear.value = years[0]
  }

  const yearStats = stats.filter(item => item.date.slice(0, 4) === heatmapYear.value)
  const heatmapData = yearStats.map(item => [item.date, item.count])
  const maxCount = Math.max(...heatmapData.map(item => item[1]))

  // 统计信息:总篇数 / 活跃日 / 最长连续活跃天数 / 单日最高,均按所选年份计算
  heatmapTotal.value = heatmapData.reduce((sum, item) => sum + item[1], 0)
  heatmapActiveDays.value = heatmapData.filter(item => item[1] > 0).length
  heatmapBestCount.value = maxCount
  heatmapMaxStreak.value = calcMaxStreak(new Map(heatmapData.map(([date, count]) => [date, count])))

  const option = {
    tooltip: {
      backgroundColor: isDark ? '#252c35' : '#ffffff',
      borderColor: isDark ? '#2a3440' : '#e8edf3',
      textStyle: { color: chartText },
      formatter: function(params) {
        const count = params.value ? params.value[1] : 0
        return '<div style="font-weight:bold;margin-bottom:4px">' + params.value[0] + '</div>' +
          (count > 0
            ? '发布了 <b style="color:#4f7cff">' + count + '</b> 篇文章'
            : '无文章发布')
      }
    },
    visualMap: {
      show: false,
      min: 0,
      max: maxCount > 0 ? maxCount : 1,
      inRange: {
        color: isDark
          ? ['#2b3b55', '#33518b', '#3d6fc4', '#4f8cff', '#8db9ff']
          : ['#dce8ff', '#a9c5ff', '#78a3ff', '#4f7cff', '#2b56d6']
      }
    },
    calendar: {
      range: heatmapYear.value,
      cellSize: ['auto', 17],
      top: 36,
      left: 34,
      right: 20,
      bottom: 12,
      splitLine: { show: false },
      itemStyle: {
        color: isDark ? '#252d38' : '#eef2f7',
        borderWidth: 3,
        borderColor: isDark ? '#1d1e1f' : '#ffffff',
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
  }

  if (heatmap) {
    heatmap.setOption(option, true)
  } else {
    heatmap = echarts.init(heatmapRef.value)
    heatmap.setOption(option)
  }
  heatmapRenderedYear = heatmapYear.value
}

// 切换年份时按所选年份重新渲染;initHeatmap 内部补默认值触发的变更不重复渲染
watch(heatmapYear, () => {
  if (articleStatsList.value.length && heatmapYear.value !== heatmapRenderedYear) {
    initHeatmap()
  }
})

// 计算最长连续活跃天数(countByDate 只需包含有文章的日期)
const calcMaxStreak = (countByDate) => {
  const toDate = (str) => {
    const [y, m, d] = str.split('-').map(Number)
    return new Date(y, m - 1, d)
  }
  const activeDates = [...countByDate.keys()].filter(d => (countByDate.get(d) || 0) > 0).sort()
  let maxStreak = 0
  let streak = 0
  let prevDate = null
  for (const date of activeDates) {
    const gap = prevDate ? Math.round((toDate(date) - toDate(prevDate)) / 86400000) : Infinity
    streak = gap === 1 ? streak + 1 : 1
    if (streak > maxStreak) maxStreak = streak
    prevDate = date
  }
  return maxStreak
}

// 获取用户地域分布
const fetchUserArea = async () => {
  try {
    const res = await getUserArea(userType.value)
    if (res.flag && mapChartRef.value) {
      if (!mapChart) mapChart = echarts.init(mapChartRef.value)
      const isDark = settingsStore.isDark
      const areaData = res.data || []
      // 图例阈值按实际数据动态分段,避免数据量小时永远落在同一档
      const maxV = Math.max(...areaData.map(item => item.value || 0), 1)
      const t1 = Math.max(4, Math.ceil(maxV * 0.6))
      const t2 = Math.max(3, Math.ceil(maxV * 0.3))
      const t3 = Math.max(2, Math.ceil(maxV * 0.1))
      mapChart.setOption({
        tooltip: {
          backgroundColor: isDark ? '#252c35' : '#ffffff',
          borderColor: isDark ? '#2a3440' : '#e8edf3',
          textStyle: { color: isDark ? '#d7dee8' : '#526173' },
          formatter: function(e) {
            const value = e.value ? e.value : 0
            return e.seriesName + '<br />' + e.name + '：' + value
          }
        },
        visualMap: {
          right: 26,
          bottom: 40,
          pieces: [
            { gt: t1, label: t1 + '人以上', color: '#ED5351' },
            { gte: t2, lte: t1, label: t2 + '-' + t1 + '人', color: '#59D9A5' },
            { gte: t3, lte: t2, label: t3 + '-' + t2 + '人', color: '#F6C021' },
            { gt: 0, lte: t3, label: '1-' + t3 + '人', color: '#6DCAEC' }
          ],
          textStyle: { color: isDark ? '#8f9aaa' : '#8b98a8' }
        },
        series: [{
          name: '用户人数',
          type: 'map',
          map: 'china',
          data: areaData,
          itemStyle: {
            areaColor: isDark ? '#2a3440' : '#eef2f7',
            borderColor: isDark ? '#3d4a58' : '#d8e2ec'
          },
          emphasis: {
            label: { color: isDark ? '#d7dee8' : '#202a34' }
          }
        }]
      }, true)
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

const resizeCharts = () => {
  const charts = [viewChart, rankChart, categoryChart, mapChart, heatmap]
  charts.forEach(chart => chart?.resize())
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
  // 容器尺寸变化(窗口缩放、侧边栏折叠)时同步重绘图表
  resizeObserver = new ResizeObserver(resizeCharts)
  if (homeRef.value) resizeObserver.observe(homeRef.value)
})

// keep-alive 缓存页签切回时,容器尺寸可能已变化,补一次重绘
onActivated(() => {
  nextTick(resizeCharts)
})

onBeforeUnmount(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
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

.home-error {
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
  min-height: 290px;
}

.chart-area--heatmap {
  height: 270px;
}

.heatmap-empty {
  padding: 30px 0;
}

.heatmap-title {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px;

  &__sub {
    color: var(--el-text-color-secondary, #8491a5);
    font-size: 12px;
    font-weight: 400;

    b {
      color: var(--el-color-primary, #4f7cff);
      font-size: 14px;
      font-weight: 700;
    }
  }
}

.heatmap-summary {
  display: flex;
  align-items: center;
  margin-left: 16px;

  &__item {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 64px;
  }

  &__value {
    color: var(--el-text-color-primary, #1d2939);
    font-size: 20px;
    font-weight: 700;
    line-height: 1.2;
  }

  &__label {
    margin-top: 2px;
    color: var(--el-text-color-secondary, #8491a5);
    font-size: 11px;
  }

  &__divider {
    width: 1px;
    height: 28px;
    margin: 0 6px;
    background: var(--el-border-color-lighter, #e7edf4);
  }
}

.heatmap-legend {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 5px;
  margin-top: 6px;
  color: var(--el-text-color-secondary, #8491a5);
  font-size: 11px;

  i {
    width: 11px;
    height: 11px;
    border-radius: 3px;
  }
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

.heatmap-header__right {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-left: 16px;
}

.heatmap-year {
  flex: 0 0 auto;

  :deep(.el-radio-button__inner) {
    padding: 5px 12px;
    font-weight: 600;
  }
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
    height: 230px;
  }

  .heatmap-header {
    align-items: flex-start;
    flex-direction: column;
    gap: 10px;
  }

  .heatmap-header__right {
    flex-wrap: wrap;
    gap: 12px;
    margin-left: 0;
  }

  .heatmap-summary {
    margin-left: 0;
  }
}

.text-center {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>
