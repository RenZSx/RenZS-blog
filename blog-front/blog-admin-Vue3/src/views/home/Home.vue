<template>
  <div class="blog-home">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #40C9C6">
              <el-icon><View /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">访问量</div>
              <div class="stat-value">{{ homeData.viewsCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #34BFA3">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户量</div>
              <div class="stat-value">{{ homeData.userCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #F4516C">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">文章量</div>
              <div class="stat-value">{{ homeData.articleCount || 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #36A3F7">
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
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header">一周访问量</div>
      </template>
      <div ref="viewChartRef" style="height: 350px" v-loading="loading"></div>
    </el-card>

    <!-- 文章贡献统计 -->
    <el-card shadow="hover" style="margin-top: 20px">
      <template #header>
        <div class="card-header heatmap-header">
          <span>文章贡献统计</span>
          <div class="heatmap-stats" v-if="heatmapTotal > 0">
            <el-tag type="success" effect="plain" size="small">{{ heatmapTotal }} 篇文章</el-tag>
            <el-tag type="info" effect="plain" size="small">{{ heatmapActiveDays }} 个活跃日</el-tag>
          </div>
        </div>
      </template>
      <div v-loading="loading" style="min-height: 200px">
        <!-- 文章贡献统计日历热力图 -->
        <div ref="heatmapRef" style="height: 240px"></div>
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
import { ref, onMounted, watch, nextTick } from 'vue'
import { View, User, Document, ChatDotRound } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
// 注册中国地图（数据提取自 UMD 格式的 china.js，独立成 ESM 模块后 import 才不会触发 require 报错）
import '@/assets/js/chinaMap'
import { getHomeData, getUserArea } from '@/api/blog/home'
import TagCloud from '@/components/TagCloud/index.vue'

const loading = ref(true)
const homeData = ref({})
const userType = ref(1)
const tagList = ref([])

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
        axisPointer: { type: 'cross' }
      },
      color: ['#3888fa'],
      legend: { data: ['访问量'] },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: { type: 'category', data: xData },
      yAxis: { type: 'value' },
      series: [{
        name: '访问量',
        type: 'line',
        smooth: true,
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
        textStyle: { fontSize: 11, color: '#909399' },
        inRange: { color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'] }
      },
      calendar: {
        range: range,
        cellSize: ['auto', 16],
        top: 20,
        left: 30,
        right: 30,
        bottom: 30,
        splitLine: {
          show: true,
          lineStyle: { color: '#f0f0f0', width: 1 }
        },
        itemStyle: {
          borderWidth: 3,
          borderColor: '#fff',
          borderRadius: 2
        },
        yearLabel: { show: false },
        dayLabel: { show: true, firstDay: 1, nameMap: 'cn', fontSize: 10, color: '#909399' },
        monthLabel: { show: true, nameMap: 'cn', fontSize: 11, color: '#606266' }
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

onMounted(() => {
  fetchHomeData()
})
</script>

<style scoped lang="scss">
.blog-home {
  padding: 20px;
}

.stat-card {
  .stat-content {
    display: flex;
    align-items: center;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30px;
    color: white;
    margin-right: 20px;
  }

  .stat-info {
    flex: 1;
  }

  .stat-label {
    font-size: 14px;
    color: #999;
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: bold;
    color: #333;
  }
}

.card-header {
  font-size: 14px;
  font-weight: bold;
  color: #202a34;
}

.heatmap-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.heatmap-stats {
  display: flex;
  gap: 8px;
}

.heatmap-stats .el-tag {
  font-weight: normal;
}

.text-center {
  text-align: center;
  color: #999;
  padding: 40px 0;
}
</style>
