<template>
  <div class="article-edit">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ articleForm.id ? '编辑文章' : '发布文章' }}</span>
        </div>
      </template>

      <!-- 文章标题 -->
      <div class="article-title-container">
        <el-input
          v-model="articleForm.articleTitle"
          size="large"
          placeholder="输入文章标题"
          maxlength="100"
          show-word-limit
        />
        <el-button
          v-if="articleForm.id == null || articleForm.status === 3"
          type="info"
          size="large"
          @click="handleSaveDraft"
          style="margin-left: 10px"
        >
          <el-icon><Document /></el-icon> 保存草稿
        </el-button>
        <el-button
          type="primary"
          size="large"
          @click="handlePublish"
          style="margin-left: 10px"
        >
          <el-icon><Position /></el-icon> 发布文章
        </el-button>
        <el-button
          type="primary"
          size="large"
          :loading="aiSummaryGenerating"
          :disabled="articleForm.id == null"
          @click="handleGenerateAiSummary"
          style="margin-left: 10px"
        >
          <el-icon><MagicStick /></el-icon> 生成AI总结
        </el-button>
        <el-button
          type="success"
          size="large"
          :loading="aiTagGenerating"
          @click="handleGenerateAiTags"
          style="margin-left: 10px"
        >
          <el-icon><PriceTag /></el-icon> AI推荐标签
        </el-button>
        <el-button
          type="warning"
          size="large"
          :loading="aiSeoGenerating"
          @click="handleGenerateAiSeo"
          style="margin-left: 10px"
        >
          <el-icon><TrendCharts /></el-icon> 生成SEO
        </el-button>
      </div>

      <!-- 文章编辑器(Markdown) -->
      <div class="editor-container">
        <MdEditor v-model="articleForm.articleContent" :height="600" />
      </div>
    </el-card>

    <!-- 发布文章对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布文章"
      width="50%"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="articleForm" :rules="rules" label-width="100px">
        <!-- 文章分类 -->
        <el-form-item label="文章分类" prop="categoryName">
          <el-tag
            v-if="articleForm.categoryName"
            type="success"
            closable
            @close="removeCategory"
            style="margin-right: 10px"
          >
            {{ articleForm.categoryName }}
          </el-tag>
          <el-popover
            v-if="!articleForm.categoryName"
            placement="bottom-start"
            width="460"
            trigger="click"
          >
            <div class="popover-title">分类</div>
            <el-autocomplete
              v-model="categoryName"
              :fetch-suggestions="searchCategoryList"
              placeholder="请输入分类名搜索，enter可添加自定义分类"
              style="width: 100%"
              @keyup.enter="handleSaveCategory"
              @select="handleSelectCategory"
            >
              <template #default="{ item }">
                <div>{{ item.categoryName }}</div>
              </template>
            </el-autocomplete>
            <div class="popover-container">
              <div
                v-for="item in categoryList"
                :key="item.id"
                class="category-item"
                @click="handleAddCategory(item)"
              >
                {{ item.categoryName }}
              </div>
            </div>
            <template #reference>
              <el-button type="success" plain size="small">
                添加分类
              </el-button>
            </template>
          </el-popover>
        </el-form-item>

        <!-- 文章标签 -->
        <el-form-item label="文章标签" prop="tagNameList">
          <el-tag
            v-for="(item, index) in articleForm.tagNameList"
            :key="index"
            closable
            @close="removeTag(item)"
            style="margin-right: 10px"
          >
            {{ item }}
          </el-tag>
          <el-popover
            v-if="articleForm.tagNameList.length < 3"
            placement="bottom-start"
            width="460"
            trigger="click"
          >
            <div class="popover-title">标签</div>
            <el-autocomplete
              v-model="tagName"
              :fetch-suggestions="searchTagList"
              placeholder="请输入标签名搜索，enter可添加自定义标签"
              style="width: 100%"
              @keyup.enter="handleSaveTag"
              @select="handleSelectTag"
            >
              <template #default="{ item }">
                <div>{{ item.tagName }}</div>
              </template>
            </el-autocomplete>
            <div class="popover-container">
              <div style="margin-bottom: 10px">添加标签</div>
              <el-tag
                v-for="(item, index) in tagList"
                :key="index"
                :class="getTagClass(item)"
                @click="handleAddTag(item)"
                style="margin-right: 8px; margin-bottom: 8px; cursor: pointer"
              >
                {{ item.tagName }}
              </el-tag>
            </div>
            <template #reference>
              <el-button type="primary" plain size="small">
                添加标签
              </el-button>
            </template>
          </el-popover>
        </el-form-item>

        <!-- AI总结 -->
        <el-form-item label="AI总结">
          <el-input
            v-model="articleForm.aiSummary"
            type="textarea"
            :rows="4"
            placeholder="可点击编辑页顶部的生成AI总结，也可以手动填写"
          />
        </el-form-item>

        <!-- AI总结状态 -->
        <el-form-item label="AI总结状态">
          <el-radio-group v-model="articleForm.aiSummaryStatus">
            <el-radio :label="0">未生成</el-radio>
            <el-radio :label="1">已生成</el-radio>
            <el-radio :label="2">已审核</el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- SEO标题 -->
        <el-form-item label="SEO标题">
          <el-input
            v-model="articleForm.seoTitle"
            maxlength="100"
            show-word-limit
            placeholder="可点击编辑页顶部的生成SEO自动填写"
          />
        </el-form-item>

        <!-- SEO描述 -->
        <el-form-item label="SEO描述">
          <el-input
            v-model="articleForm.seoDescription"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="用于搜索引擎description"
          />
        </el-form-item>

        <!-- SEO关键词 -->
        <el-form-item label="SEO关键词">
          <el-input
            v-model="articleForm.seoKeywords"
            maxlength="255"
            show-word-limit
            placeholder="多个关键词用英文逗号分隔"
          />
        </el-form-item>

        <!-- 分享描述 -->
        <el-form-item label="分享描述">
          <el-input
            v-model="articleForm.seoOgDescription"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="用于Open Graph社交分享"
          />
        </el-form-item>

        <!-- 文章类型 -->
        <el-form-item label="文章类型" prop="type">
          <el-select v-model="articleForm.type" placeholder="请选择类型">
            <el-option
              v-for="item in typeList"
              :key="item.type"
              :label="item.desc"
              :value="item.type"
            />
          </el-select>
        </el-form-item>

        <!-- 原文地址 -->
        <el-form-item v-if="articleForm.type !== 1" label="原文地址">
          <el-input
            v-model="articleForm.originalUrl"
            placeholder="请填写原文链接"
          />
        </el-form-item>

        <!-- 上传封面 -->
        <el-form-item label="上传封面" prop="articleCover">
          <ImageUpload
            v-model="articleForm.articleCover"
            :limit="1"
            :file-size="5"
            action="/admin/articles/images"
          />
        </el-form-item>

        <!-- 置顶 -->
        <el-form-item label="置顶">
          <el-switch
            v-model="articleForm.isTop"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>

        <!-- 发布形式 -->
        <el-form-item label="发布形式" prop="status">
          <el-radio-group v-model="articleForm.status">
            <el-radio :label="1">公开</el-radio>
            <el-radio :label="2">私密</el-radio>
            <el-radio :label="4">推荐</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          发 表
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Position, MagicStick, PriceTag, TrendCharts } from '@element-plus/icons-vue'
import ImageUpload from '@/components/ImageUpload/index.vue'
import MdEditor from '@/components/MdEditor/index.vue'
import { getArticle, addArticle, updateArticle, generateAiSummary, generateAiTags, generateAiSeo } from '@/api/blog/article'
import { searchCategories } from '@/api/blog/category'
import { searchTags } from '@/api/blog/tag'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const publishDialogVisible = ref(false)
const submitLoading = ref(false)
const autoSave = ref(true)
const aiSummaryGenerating = ref(false)
const aiTagGenerating = ref(false)
const aiSeoGenerating = ref(false)
const categoryName = ref('')
const tagName = ref('')
const categoryList = ref([])
const tagList = ref([])

const typeList = [
  { type: 1, desc: '原创' },
  { type: 2, desc: '转载' },
  { type: 3, desc: '翻译' }
]

const articleForm = reactive({
  id: null,
  articleTitle: '',
  articleContent: '',
  aiSummary: '',
  aiSummaryStatus: 0,
  seoTitle: '',
  seoDescription: '',
  seoKeywords: '',
  seoOgDescription: '',
  articleCover: '',
  categoryName: null,
  tagNameList: [],
  originalUrl: '',
  isTop: 0,
  type: 1,
  status: 1
})

const rules = {
  articleTitle: [
    { required: true, message: '文章标题不能为空', trigger: 'blur' }
  ],
  categoryName: [
    { required: true, message: '文章分类不能为空', trigger: 'change' }
  ],
  tagNameList: [
    { required: true, message: '文章标签不能为空', trigger: 'change' }
  ],
  articleCover: [
    { required: true, message: '文章封面不能为空', trigger: 'change' }
  ],
  type: [
    { required: true, message: '文章类型不能为空', trigger: 'change' }
  ],
  status: [
    { required: true, message: '发布形式不能为空', trigger: 'change' }
  ]
}

// 获取文章详情
const getArticleDetail = async (id) => {
  try {
    const res = await getArticle(id)
    if (res.flag) {
      Object.assign(articleForm, res.data)
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
  }
}

// 查询分类列表
const getCategoryList = async () => {
  try {
    const res = await searchCategories()
    if (res.flag) {
      categoryList.value = res.data
    }
  } catch (error) {
    console.error('获取分类列表失败:', error)
  }
}

// 查询标签列表
const getTagList = async () => {
  try {
    const res = await searchTags()
    if (res.flag) {
      tagList.value = res.data
    }
  } catch (error) {
    console.error('获取标签列表失败:', error)
  }
}

// 搜索分类
const searchCategoryList = async (queryString, cb) => {
  try {
    const res = await searchCategories()
    if (res.flag) {
      const results = queryString
        ? res.data.filter(item =>
            item.categoryName.toLowerCase().includes(queryString.toLowerCase())
          )
        : res.data
      cb(results)
    }
  } catch (error) {
    console.error('搜索分类失败:', error)
    cb([])
  }
}

// 搜索标签
const searchTagList = async (queryString, cb) => {
  try {
    const res = await searchTags()
    if (res.flag) {
      const results = queryString
        ? res.data.filter(item =>
            item.tagName.toLowerCase().includes(queryString.toLowerCase())
          )
        : res.data
      cb(results)
    }
  } catch (error) {
    console.error('搜索标签失败:', error)
    cb([])
  }
}

// 选择分类
const handleSelectCategory = (item) => {
  handleAddCategory({ categoryName: item.categoryName })
}

// 保存分类
const handleSaveCategory = () => {
  if (categoryName.value.trim()) {
    handleAddCategory({ categoryName: categoryName.value })
    categoryName.value = ''
  }
}

// 添加分类
const handleAddCategory = (item) => {
  articleForm.categoryName = item.categoryName
}

// 移除分类
const removeCategory = () => {
  articleForm.categoryName = null
}

// 选择标签
const handleSelectTag = (item) => {
  handleAddTag({ tagName: item.tagName })
}

// 保存标签
const handleSaveTag = () => {
  if (tagName.value.trim()) {
    handleAddTag({ tagName: tagName.value })
    tagName.value = ''
  }
}

// 添加标签
const handleAddTag = (item) => {
  if (articleForm.tagNameList.indexOf(item.tagName) === -1 && articleForm.tagNameList.length < 3) {
    articleForm.tagNameList.push(item.tagName)
  }
}

// 移除标签
const removeTag = (item) => {
  const index = articleForm.tagNameList.indexOf(item)
  if (index > -1) {
    articleForm.tagNameList.splice(index, 1)
  }
}

// 获取标签样式
const getTagClass = (item) => {
  return articleForm.tagNameList.indexOf(item.tagName) !== -1 ? 'tag-selected' : ''
}

// 校验AI功能所需的基础内容
const validateAiBaseContent = () => {
  if (!articleForm.articleTitle.trim()) {
    ElMessage.error('文章标题不能为空')
    return false
  }
  if (!articleForm.articleContent.trim()) {
    ElMessage.error('文章内容不能为空')
    return false
  }
  return true
}

// 生成AI总结
const handleGenerateAiSummary = async () => {
  if (articleForm.id == null) {
    ElMessage.warning('请先保存文章后再生成AI总结')
    return
  }
  aiSummaryGenerating.value = true
  try {
    const res = await generateAiSummary(articleForm.id)
    if (res.flag) {
      articleForm.aiSummary = res.data
      articleForm.aiSummaryStatus = 1
      ElMessage.success('AI总结生成成功')
    } else {
      ElMessage.error(res.message || 'AI总结生成失败')
    }
  } catch (error) {
    console.error('生成AI总结失败:', error)
  } finally {
    aiSummaryGenerating.value = false
  }
}

// AI推荐标签
const handleGenerateAiTags = async () => {
  if (!validateAiBaseContent()) return
  aiTagGenerating.value = true
  try {
    const res = await generateAiTags(articleForm)
    if (res.flag) {
      const aiTags = res.data || []
      aiTags.forEach(tagName => {
        if (articleForm.tagNameList.indexOf(tagName) === -1 && articleForm.tagNameList.length < 3) {
          articleForm.tagNameList.push(tagName)
        }
      })
      ElMessage.success('AI标签推荐成功')
    } else {
      ElMessage.error(res.message || 'AI标签推荐失败')
    }
  } catch (error) {
    console.error('AI标签推荐失败:', error)
  } finally {
    aiTagGenerating.value = false
  }
}

// 生成SEO
const handleGenerateAiSeo = async () => {
  if (!validateAiBaseContent()) return
  aiSeoGenerating.value = true
  try {
    const res = await generateAiSeo(articleForm)
    if (res.flag) {
      articleForm.seoTitle = res.data.seoTitle || ''
      articleForm.seoDescription = res.data.seoDescription || ''
      articleForm.seoKeywords = res.data.seoKeywords || ''
      articleForm.seoOgDescription = res.data.seoOgDescription || ''
      ElMessage.success('AI SEO生成成功')
    } else {
      ElMessage.error(res.message || 'AI SEO生成失败')
    }
  } catch (error) {
    console.error('生成SEO失败:', error)
  } finally {
    aiSeoGenerating.value = false
  }
}

// 打开发布对话框
const handlePublish = () => {
  if (!articleForm.articleTitle.trim()) {
    ElMessage.error('文章标题不能为空')
    return
  }
  if (!articleForm.articleContent.trim()) {
    ElMessage.error('文章内容不能为空')
    return
  }
  getCategoryList()
  getTagList()
  publishDialogVisible.value = true
}

// 保存草稿
const handleSaveDraft = async () => {
  if (!articleForm.articleTitle.trim()) {
    ElMessage.error('文章标题不能为空')
    return
  }
  if (!articleForm.articleContent.trim()) {
    ElMessage.error('文章内容不能为空')
    return
  }

  articleForm.status = 3
  try {
    const apiMethod = articleForm.id ? updateArticle : addArticle
    const res = await apiMethod(articleForm)
    if (res.flag) {
      ElMessage.success('保存草稿成功')
      autoSave.value = false
      sessionStorage.removeItem('article')
      router.push('/article-list')
    } else {
      ElMessage.error(res.message || '保存草稿失败')
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
  }
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (!articleForm.articleTitle.trim()) {
        ElMessage.error('文章标题不能为空')
        return
      }
      if (!articleForm.articleContent.trim()) {
        ElMessage.error('文章内容不能为空')
        return
      }

      submitLoading.value = true
      try {
        const apiMethod = articleForm.id ? updateArticle : addArticle
        const res = await apiMethod(articleForm)
        if (res.flag) {
          ElMessage.success(res.message || '发布成功')
          autoSave.value = false
          sessionStorage.removeItem('article')
          publishDialogVisible.value = false
          router.push('/article-list')
        } else {
          ElMessage.error(res.message || '发布失败')
        }
      } catch (error) {
        console.error('发布失败:', error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 自动保存文章
const autoSaveArticle = async () => {
  if (
    autoSave.value &&
    articleForm.articleTitle.trim() &&
    articleForm.articleContent.trim() &&
    articleForm.id != null
  ) {
    try {
      const res = await updateArticle(articleForm)
      if (res.flag) {
        ElMessage.success('自动保存成功')
      } else {
        ElMessage.error('自动保存失败')
      }
    } catch (error) {
      console.error('自动保存失败:', error)
    }
  }

  // 保存本地文章记录
  if (autoSave.value && articleForm.id == null) {
    sessionStorage.setItem('article', JSON.stringify(articleForm))
  }
}

onMounted(() => {
  // 后端菜单将"修改文章"定义为 /articles/*,经 permission.js 转换为
  // /articles/:pathMatch(.*)*,故文章 id 从 pathMatch 参数取。
  // pathMatch 在通配路由下是数组,取第一段即 id。
  const pathMatch = route.params.pathMatch
  const articleId = Array.isArray(pathMatch) ? pathMatch[0] : pathMatch
  if (articleId) {
    getArticleDetail(articleId)
  } else {
    const savedArticle = sessionStorage.getItem('article')
    if (savedArticle) {
      Object.assign(articleForm, JSON.parse(savedArticle))
    }
  }
})

onUnmounted(() => {
  autoSaveArticle()
})
</script>

<style scoped lang="scss">
.article-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.article-title-container {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.editor-container {
  margin-top: 20px;
}

.popover-title {
  margin-bottom: 10px;
  text-align: center;
  font-weight: bold;
}

.popover-container {
  margin-top: 10px;
  max-height: 260px;
  overflow-y: auto;
}

.category-item {
  cursor: pointer;
  padding: 8px 10px;
  transition: all 0.3s;

  &:hover {
    background-color: var(--el-color-success-light-9);
    color: var(--el-color-success);
  }
}

.tag-selected {
  opacity: 0.5;
  cursor: not-allowed !important;
}
</style>
