<template>
  <el-card class="main-card">
    <div class="title">{{ this.$route.name }}</div>
    <div class="notice-container">
      <el-alert
        title="发布后只会新增一条全局系统通知，用户已读状态会单独记录。"
        type="info"
        show-icon
        :closable="false"
      />
      <el-form
        ref="noticeForm"
        class="notice-form"
        label-width="90px"
        :model="noticeForm"
        :rules="rules"
      >
        <el-form-item label="通知内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="6"
            maxlength="500"
            show-word-limit
            placeholder="请输入需要发布给所有用户的通知内容"
          />
        </el-form-item>
        <el-form-item label="跳转路径" prop="jumpPath">
          <el-input
            v-model="noticeForm.jumpPath"
            maxlength="255"
            placeholder="可选，例如 /articles/1 或 /user"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="publishing"
            @click="publishNotice"
          >
            发布通知
          </el-button>
          <el-button @click="resetForm">重 置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-card>
</template>

<script>
export default {
  data() {
    return {
      publishing: false,
      noticeForm: {
        content: "",
        jumpPath: ""
      },
      rules: {
        content: [
          { required: true, message: "通知内容不能为空", trigger: "blur" },
          { max: 500, message: "通知内容不能超过500个字符", trigger: "blur" }
        ],
        jumpPath: [
          { max: 255, message: "跳转路径不能超过255个字符", trigger: "blur" }
        ]
      }
    };
  },
  methods: {
    publishNotice() {
      this.$refs.noticeForm.validate(valid => {
        if (!valid) {
          return false;
        }
        this.publishing = true;
        this.axios
          .post("/api/admin/notices/system", {
            content: this.noticeForm.content.trim(),
            jumpPath: this.noticeForm.jumpPath.trim() || null
          })
          .then(({ data }) => {
            if (data.flag) {
              this.$notify.success({
                title: "成功",
                message: `系统通知发布成功，已新增 ${data.data || 0} 条通知`
              });
              this.resetForm();
            } else {
              this.$notify.error({
                title: "失败",
                message: data.message
              });
            }
          })
          .finally(() => {
            this.publishing = false;
          });
      });
    },
    resetForm() {
      this.noticeForm = {
        content: "",
        jumpPath: ""
      };
      this.$nextTick(() => {
        this.$refs.noticeForm.clearValidate();
      });
    }
  }
};
</script>

<style scoped>
.notice-container {
  max-width: 720px;
  margin-top: 2rem;
}

.notice-form {
  margin-top: 1.5rem;
}
</style>
