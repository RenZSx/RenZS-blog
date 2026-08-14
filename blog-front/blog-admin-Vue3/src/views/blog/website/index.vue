<template>
  <div class="website-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>网站设置</span>
        </div>
      </template>

      <el-tabs v-model="activeName">
        <!-- 网站信息 -->
        <el-tab-pane label="网站信息" name="info">
          <el-form
            ref="infoFormRef"
            :model="websiteConfig"
            label-width="120px"
            label-position="left"
          >
            <el-form-item label="网站头像">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :headers="headers"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'websiteAvatar')"
                :before-upload="handleBeforeUpload"
              >
                <img
                  v-if="websiteConfig.websiteAvatar"
                  :src="websiteConfig.websiteAvatar"
                  class="avatar"
                />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="网站名称">
              <el-input
                v-model="websiteConfig.websiteName"
                size="small"
                style="width: 400px"
                placeholder="请输入网站名称"
              />
            </el-form-item>
            <el-form-item label="网站作者">
              <el-input
                v-model="websiteConfig.websiteAuthor"
                size="small"
                style="width: 400px"
                placeholder="请输入网站作者"
              />
            </el-form-item>
            <el-form-item label="网站简介">
              <el-input
                v-model="websiteConfig.websiteIntro"
                size="small"
                style="width: 400px"
                placeholder="请输入网站简介"
              />
            </el-form-item>
            <el-form-item label="网站创建日期">
              <el-date-picker
                v-model="websiteConfig.websiteCreateTime"
                type="date"
                placeholder="选择日期"
                style="width: 400px"
                value-format="YYYY-MM-DD"
              />
            </el-form-item>
            <el-form-item label="网站公告">
              <el-input
                v-model="websiteConfig.websiteNotice"
                placeholder="请输入公告内容"
                style="width: 400px"
                type="textarea"
                :rows="5"
              />
            </el-form-item>
            <el-form-item label="后台地址">
              <el-input
                v-model="websiteConfig.websiteBgAddress"
                size="small"
                style="width: 400px"
                placeholder="请输入后台地址"
              />
            </el-form-item>
            <el-form-item label="底部诗句">
              <el-input
                v-model="websiteConfig.websiteVerse"
                size="small"
                style="width: 400px"
                placeholder="请输入底部诗句"
              />
            </el-form-item>
            <el-form-item label="备案号">
              <el-input
                v-model="websiteConfig.websiteRecordNo"
                size="small"
                style="width: 400px"
                placeholder="请输入备案号"
              />
            </el-form-item>
            <el-form-item label="公安部备案号">
              <el-input
                v-model="websiteConfig.websitePoliceRecordNo"
                size="small"
                style="width: 400px"
                placeholder="请输入公安部备案号"
              />
            </el-form-item>
            <el-form-item label="第三方登录">
              <el-checkbox-group v-model="websiteConfig.socialLoginList">
                <el-checkbox label="qq">QQ</el-checkbox>
                <el-checkbox label="weibo">微博</el-checkbox>
                <el-checkbox label="gitee">Gitee</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">
                <el-icon><Check /></el-icon> 保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 社交信息 -->
        <el-tab-pane label="社交信息" name="social">
          <el-form
            ref="socialFormRef"
            :model="websiteConfig"
            label-width="100px"
            label-position="left"
          >
            <el-checkbox-group v-model="websiteConfig.socialUrlList">
              <el-form-item label="QQ">
                <el-input
                  v-model="websiteConfig.qq"
                  size="small"
                  style="width: 400px; margin-right: 1rem"
                  placeholder="请输入QQ地址"
                />
                <el-checkbox label="qq">是否展示</el-checkbox>
              </el-form-item>
              <el-form-item label="Github">
                <el-input
                  v-model="websiteConfig.github"
                  size="small"
                  style="width: 400px; margin-right: 1rem"
                  placeholder="请输入Github地址"
                />
                <el-checkbox label="github">是否展示</el-checkbox>
              </el-form-item>
              <el-form-item label="Gitee">
                <el-input
                  v-model="websiteConfig.gitee"
                  size="small"
                  style="width: 400px; margin-right: 1rem"
                  placeholder="请输入Gitee地址"
                />
                <el-checkbox label="gitee">是否展示</el-checkbox>
              </el-form-item>
            </el-checkbox-group>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">
                <el-icon><Check /></el-icon> 保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 其他设置 -->
        <el-tab-pane label="其他设置" name="other">
          <el-form
            ref="otherFormRef"
            :model="websiteConfig"
            label-width="130px"
            label-position="left"
          >
            <el-row style="width: 600px">
              <el-col :md="12">
                <el-form-item label="用户头像">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadUrl"
                    :headers="headers"
                    :show-file-list="false"
                    :on-success="(res) => handleUploadSuccess(res, 'userAvatar')"
                    :before-upload="handleBeforeUpload"
                  >
                    <img
                      v-if="websiteConfig.userAvatar"
                      :src="websiteConfig.userAvatar"
                      class="avatar"
                    />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :md="12">
                <el-form-item label="游客头像">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadUrl"
                    :headers="headers"
                    :show-file-list="false"
                    :on-success="(res) => handleUploadSuccess(res, 'touristAvatar')"
                    :before-upload="handleBeforeUpload"
                  >
                    <img
                      v-if="websiteConfig.touristAvatar"
                      :src="websiteConfig.touristAvatar"
                      class="avatar"
                    />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="默认文章封面">
              <el-upload
                class="cover-uploader"
                :action="uploadUrl"
                :headers="headers"
                :show-file-list="false"
                :on-success="(res) => handleUploadSuccess(res, 'articleCover')"
                :before-upload="handleBeforeUpload"
              >
                <img
                  v-if="websiteConfig.articleCover"
                  :src="websiteConfig.articleCover"
                  class="cover"
                />
                <el-icon v-else class="cover-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="邮箱通知">
              <el-radio-group v-model="websiteConfig.isEmailNotice">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="邮箱注册">
              <el-radio-group v-model="websiteConfig.isEmailRegister">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="评论审核">
              <el-radio-group v-model="websiteConfig.isCommentReview">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="留言审核">
              <el-radio-group v-model="websiteConfig.isMessageReview">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="打赏状态">
              <el-radio-group v-model="websiteConfig.isReward">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-row style="width: 600px" v-show="websiteConfig.isReward === 1">
              <el-col :md="12">
                <el-form-item label="微信收款码">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadUrl"
                    :headers="headers"
                    :show-file-list="false"
                    :on-success="(res) => handleUploadSuccess(res, 'weiXinQRCode')"
                    :before-upload="handleBeforeUpload"
                  >
                    <img
                      v-if="websiteConfig.weiXinQRCode"
                      :src="websiteConfig.weiXinQRCode"
                      class="avatar"
                    />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
              <el-col :md="12">
                <el-form-item label="支付宝收款码">
                  <el-upload
                    class="avatar-uploader"
                    :action="uploadUrl"
                    :headers="headers"
                    :show-file-list="false"
                    :on-success="(res) => handleUploadSuccess(res, 'alipayQRCode')"
                    :before-upload="handleBeforeUpload"
                  >
                    <img
                      v-if="websiteConfig.alipayQRCode"
                      :src="websiteConfig.alipayQRCode"
                      class="avatar"
                    />
                    <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
                  </el-upload>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="聊天室状态">
              <el-radio-group v-model="websiteConfig.isChatRoom">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="Websocket地址" v-show="websiteConfig.isChatRoom === 1">
              <el-input
                v-model="websiteConfig.websocketUrl"
                size="small"
                style="width: 400px"
                placeholder="请输入Websocket地址"
              />
            </el-form-item>
            <el-form-item label="音乐播放器状态">
              <el-radio-group v-model="websiteConfig.isMusicPlayer">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">
                <el-icon><Check /></el-icon> 保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- AI配置 -->
        <el-tab-pane label="AI配置" name="ai">
          <el-form
            ref="aiFormRef"
            :model="websiteConfig"
            label-width="150px"
            label-position="left"
          >
            <el-form-item label="AI总结状态">
              <el-radio-group v-model="websiteConfig.isAiSummary">
                <el-radio :label="0">关闭</el-radio>
                <el-radio :label="1">开启</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="接口地址">
              <el-input
                v-model="websiteConfig.aiApiUrl"
                size="small"
                style="width: 520px"
                placeholder="例如 http://45.138.71.218:8080/v1/responses"
              />
            </el-form-item>
            <el-form-item label="接口类型">
              <el-select
                v-model="websiteConfig.aiApiType"
                size="small"
                style="width: 220px"
              >
                <el-option label="Chat Completions" value="chat_completions" />
                <el-option label="Responses" value="responses" />
              </el-select>
            </el-form-item>
            <el-form-item label="接口密钥">
              <el-input
                v-model="websiteConfig.aiApiKey"
                size="small"
                style="width: 520px"
                type="password"
                show-password
                placeholder="用于服务端调用，不会下发给前台"
              />
            </el-form-item>
            <el-form-item label="模型名称">
              <el-input
                v-model="websiteConfig.aiModel"
                size="small"
                style="width: 520px"
                placeholder="例如 gpt-4o-mini / deepseek-chat"
              />
            </el-form-item>
            <el-form-item
              label="推理强度"
              v-show="websiteConfig.aiApiType === 'responses'"
            >
              <el-select
                v-model="websiteConfig.aiReasoningEffort"
                size="small"
                style="width: 220px"
                placeholder="可选"
              >
                <el-option label="低" value="low" />
                <el-option label="中" value="medium" />
                <el-option label="高" value="high" />
                <el-option label="极高" value="xhigh" />
              </el-select>
            </el-form-item>
            <el-form-item
              label="关闭响应存储"
              v-show="websiteConfig.aiApiType === 'responses'"
            >
              <el-radio-group v-model="websiteConfig.aiDisableResponseStorage">
                <el-radio :label="0">否</el-radio>
                <el-radio :label="1">是</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="总结提示词">
              <el-input
                v-model="websiteConfig.aiSummaryPrompt"
                style="width: 520px"
                type="textarea"
                :rows="5"
                placeholder="留空时使用系统默认提示词"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate" :loading="loading">
                <el-icon><Check /></el-icon> 保存
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Check } from '@element-plus/icons-vue'
import { getWebsiteConfig, updateWebsiteConfig } from '@/api/blog/website'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const activeName = ref('info')

const uploadUrl = ref(import.meta.env.VITE_APP_BASE_API + '/admin/config/images')
const headers = ref({ Authorization: 'Bearer ' + getToken() })

const websiteConfig = reactive({
  websiteAvatar: '',
  websiteName: '',
  websiteAuthor: '',
  websiteIntro: '',
  websiteNotice: '',
  websiteCreateTime: null,
  websiteVerse: '',
  websiteBgAddress: '',
  websitePoliceRecordNo: '',
  websiteRecordNo: '',
  socialLoginList: [],
  socialUrlList: [],
  qq: '',
  github: '',
  gitee: '',
  userAvatar: '',
  touristAvatar: '',
  isReward: 1,
  weiXinQRCode: '',
  alipayQRCode: '',
  articleCover: '',
  isChatRoom: 1,
  websocketUrl: '',
  isMusicPlayer: 1,
  isEmailNotice: 1,
  isEmailRegister: 1,
  isCommentReview: 0,
  isMessageReview: 0,
  isAiSummary: 0,
  aiApiUrl: '',
  aiApiKey: '',
  aiModel: '',
  aiApiType: 'chat_completions',
  aiReasoningEffort: 'high',
  aiDisableResponseStorage: 1,
  aiSummaryPrompt: ''
})

const getConfig = async () => {
  try {
    const res = await getWebsiteConfig()
    if (res.flag) {
      Object.assign(websiteConfig, res.data)
    }
  } catch (error) {
    console.error('获取网站配置失败:', error)
  }
}

const handleUpdate = async () => {
  loading.value = true
  try {
    const res = await updateWebsiteConfig(websiteConfig)
    if (res.flag) {
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error('保存网站配置失败:', error)
  } finally {
    loading.value = false
  }
}

const handleBeforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/jpg', 'image/png'].includes(file.type)
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response, field) => {
  if (response.flag) {
    websiteConfig[field] = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error('上传失败')
  }
}

onMounted(() => {
  getConfig()
})
</script>

<style scoped lang="scss">
.website-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
  line-height: 120px;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
}

.cover-uploader {
  :deep(.el-upload) {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: all 0.3s;

    &:hover {
      border-color: #409eff;
    }
  }
}

.cover-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 300px;
  height: 160px;
  text-align: center;
  line-height: 160px;
}

.cover {
  width: 300px;
  height: 160px;
  display: block;
}
</style>
