<template>
  <el-card class="main-card love-card">
    <div class="title">纪念页管理</div>
    <div class="love-tip">
      这里统一维护纪念页基础配置和飞书传信正文。
    </div>
    <el-tabs v-model="activeName">
      <el-tab-pane label="基础配置" name="config">
        <el-form
          ref="loveConfigForm"
          :model="loveConfigForm"
          :rules="rules"
          label-width="110px"
          label-position="left"
          class="love-form"
        >
          <el-form-item label="页面标题" prop="title">
            <el-input
              v-model="loveConfigForm.title"
              size="small"
              style="width:420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入纪念页标题"
            />
          </el-form-item>
          <el-form-item label="页面副标题" prop="subtitle">
            <el-input
              v-model="loveConfigForm.subtitle"
              size="small"
              style="width:420px"
              maxlength="255"
              show-word-limit
              placeholder="请输入纪念页副标题"
            />
          </el-form-item>
          <el-form-item label="背景图片" prop="background">
            <el-upload
              class="love-background-uploader"
              action="/api/admin/config/images"
              :show-file-list="false"
              :on-success="handleBackgroundSuccess"
            >
              <img
                v-if="loveConfigForm.background"
                :src="loveConfigForm.background"
                class="love-background-preview"
              />
              <i v-else class="el-icon-plus love-background-icon" />
            </el-upload>
          </el-form-item>
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="loveConfigForm.startTime"
              value-format="yyyy-MM-dd HH:mm:ss"
              type="datetime"
              placeholder="请选择开始时间"
              style="width:420px"
            />
          </el-form-item>
          <el-form-item label="纪念日时间" prop="anniversaryTime">
            <el-date-picker
              v-model="loveConfigForm.anniversaryTime"
              value-format="yyyy-MM-dd HH:mm:ss"
              type="datetime"
              placeholder="请选择纪念日时间"
              style="width:420px"
            />
          </el-form-item>
          <el-form-item label="纪念日标题" prop="anniversaryTitle">
            <el-input
              v-model="loveConfigForm.anniversaryTitle"
              size="small"
              style="width:420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入纪念日标题"
            />
          </el-form-item>
          <el-form-item label="是否启用" prop="isEnabled">
            <el-radio-group v-model="loveConfigForm.isEnabled">
              <el-radio :label="1">启用</el-radio>
              <el-radio :label="0">停用</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="medium"
              :loading="saving"
              @click="saveLoveConfig"
            >
              保存配置
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
      <el-tab-pane label="飞书传信" name="letter">
        <el-form
          :model="letterForm"
          label-width="110px"
          label-position="left"
          class="love-form"
        >
          <el-form-item label="信件标题">
            <el-input
              v-model="letterForm.letterTitle"
              size="small"
              style="width:420px"
              maxlength="100"
              show-word-limit
              placeholder="请输入信件标题"
            />
          </el-form-item>
          <el-form-item label="信件正文">
            <el-input
              v-model="letterForm.letterContent"
              type="textarea"
              :rows="12"
              style="width:520px"
              maxlength="5000"
              show-word-limit
              placeholder="请输入信件正文，支持 HTML 内容"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="medium"
              :loading="letterSaving"
              @click="saveLoveLetter"
            >
              保存信件
            </el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import { createDefaultLetterForm, normalizeLetterForm } from "./letterEditor";

/**
 * 纪念页管理页面。
 * 负责维护纪念页基础配置和飞书传信正文。
 */
export default {
  name: "Love",
  created() {
    this.getLoveConfig();
    this.getLoveLetter();
  },
  data() {
    return {
      activeName: "config",
      saving: false,
      letterSaving: false,
      loveConfigForm: {
        title: "",
        subtitle: "",
        background: "",
        startTime: "",
        anniversaryTime: "",
        anniversaryTitle: "",
        isEnabled: 1
      },
      letterForm: createDefaultLetterForm(),
      rules: {
        title: [{ required: true, message: "请输入页面标题", trigger: "blur" }],
        startTime: [
          { required: true, message: "请选择开始时间", trigger: "change" }
        ]
      }
    };
  },
  methods: {
    /**
     * 拉取纪念页基础配置。
     */
    getLoveConfig() {
      this.axios.get("/api/admin/love/config").then(({ data }) => {
        if (data.flag) {
          this.loveConfigForm = {
            ...this.loveConfigForm,
            ...data.data
          };
        } else {
          this.$message.error(data.message || "加载纪念页配置失败");
        }
      });
    },
    /**
     * 拉取飞书传信正文。
     */
    getLoveLetter() {
      this.axios.get("/api/admin/love/letter").then(({ data }) => {
        if (data.flag) {
          this.letterForm = normalizeLetterForm(data.data);
        } else {
          this.$message.error(data.message || "加载飞书传信失败");
        }
      });
    },
    /**
     * 背景图上传成功后回填地址。
     */
    handleBackgroundSuccess(response) {
      this.loveConfigForm.background = response.data;
    },
    /**
     * 保存纪念页基础配置。
     */
    saveLoveConfig() {
      this.$refs.loveConfigForm.validate(valid => {
        if (!valid) {
          return;
        }
        this.saving = true;
        this.axios
          .put("/api/admin/love/config", this.loveConfigForm)
          .then(({ data }) => {
            if (data.flag) {
              this.$notify.success({
                title: "成功",
                message: data.message
              });
            } else {
              this.$notify.error({
                title: "失败",
                message: data.message
              });
            }
          })
          .finally(() => {
            this.saving = false;
          });
      });
    },
    /**
     * 保存飞书传信正文。
     */
    saveLoveLetter() {
      this.letterSaving = true;
      this.axios
        .put("/api/admin/love/letter", this.letterForm)
        .then(({ data }) => {
          if (data.flag) {
            this.$notify.success({
              title: "成功",
              message: data.message
            });
          } else {
            this.$notify.error({
              title: "失败",
              message: data.message
            });
          }
        })
        .finally(() => {
          this.letterSaving = false;
        });
    }
  }
};
</script>

<style scoped>
.love-card {
  min-height: calc(100vh - 120px);
}

.love-tip {
  margin: 30px 0 2px;
  color: #909399;
  font-size: 13px;
}

.love-form {
  max-width: 780px;
}

.love-background-uploader .el-upload {
  width: 320px;
  height: 180px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  background: #fafafa;
}

.love-background-uploader .el-upload:hover {
  border-color: #409eff;
}

.love-background-icon {
  width: 320px;
  height: 180px;
  line-height: 180px;
  text-align: center;
  font-size: 30px;
  color: #8c939d;
}

.love-background-preview {
  width: 320px;
  height: 180px;
  display: block;
  object-fit: cover;
}
</style>
