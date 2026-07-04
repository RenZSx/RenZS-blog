<template>
  <el-card class="main-card">
    <div class="title">{{ this.$route.name }}</div>
    <!-- 表格操作 -->
    <div class="operation-container">
      <el-button
        type="primary"
        size="small"
        icon="el-icon-plus"
        @click="openModel(null)"
      >
        新增
      </el-button>
      <el-button
        type="danger"
        size="small"
        icon="el-icon-delete"
        :disabled="linkIdList.length == 0"
        @click="deleteFlag = true"
      >
        批量删除
      </el-button>
      <!-- 条件筛选 -->
      <div style="margin-left:auto">
        <el-select
          v-model="status"
          clearable
          size="small"
          placeholder="审核状态"
          style="width:120px;margin-right:1rem"
          @change="searchLinks"
        >
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-input
          v-model="keywords"
          prefix-icon="el-icon-search"
          size="small"
          placeholder="请输入友链名"
          style="width:200px"
          @keyup.enter.native="searchLinks"
        />
        <el-button
          type="primary"
          size="small"
          icon="el-icon-search"
          style="margin-left:1rem"
          @click="searchLinks"
        >
          搜索
        </el-button>
      </div>
    </div>
    <!-- 表格展示 -->
    <el-table
      border
      :data="linkList"
      @selection-change="selectionChange"
      v-loading="loading"
    >
      <!-- 表格列 -->
      <el-table-column type="selection" width="55" />
      <el-table-column
        prop="linkCover"
        label="链接封面"
        align="center"
        width="150"
      >
        <template slot-scope="scope">
          <img
            :src="scope.row.linkCover"
            width="120"
            height="60"
            style="object-fit:cover"
          />
        </template>
      </el-table-column>
      <el-table-column prop="linkName" label="链接名" align="center" />
      <el-table-column prop="linkAddress" label="链接地址" align="center" />
      <el-table-column prop="linkIntro" label="链接介绍" align="center" />
      <el-table-column label="审核状态" align="center" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.linkStatus)">
            {{ statusText(scope.row.linkStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        prop="createTime"
        label="创建时间"
        width="140"
        align="center"
      >
        <template slot-scope="scope">
          <i class="el-icon-time" style="margin-right:5px" />
          {{ scope.row.createTime | date }}
        </template>
      </el-table-column>
      <!-- 列操作 -->
      <el-table-column label="操作" align="center" width="260">
        <template slot-scope="scope">
          <el-button type="primary" size="mini" @click="openModel(scope.row)">
            编辑
          </el-button>
          <el-button
            v-if="scope.row.linkStatus !== 1"
            type="success"
            size="mini"
            @click="reviewLink(scope.row.id, 1)"
          >
            通过
          </el-button>
          <el-button
            v-if="scope.row.linkStatus !== 2"
            type="warning"
            size="mini"
            @click="reviewLink(scope.row.id, 2)"
          >
            拒绝
          </el-button>
          <el-popconfirm
            title="确定删除吗？"
            style="margin-left:0.5rem"
            @confirm="deleteLink(scope.row.id)"
          >
            <el-button size="mini" type="danger" slot="reference">
              删除
            </el-button>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <el-pagination
      class="pagination-container"
      background
      @size-change="sizeChange"
      @current-change="currentChange"
      :current-page="current"
      :page-size="size"
      :total="count"
      :page-sizes="[10, 20]"
      layout="total, sizes, prev, pager, next, jumper"
    />
    <!-- 批量删除对话框 -->
    <el-dialog :visible.sync="deleteFlag" width="30%">
      <div class="dialog-title-container" slot="title">
        <i class="el-icon-warning" style="color:#ff9900" />提示
      </div>
      <div style="font-size:1rem">是否删除选中项？</div>
      <div slot="footer">
        <el-button @click="deleteFlag = false">取 消</el-button>
        <el-button type="primary" @click="deleteLink(null)">
          确 定
        </el-button>
      </div>
    </el-dialog>
    <!-- 添加对话框 -->
    <el-dialog :visible.sync="addOrEdit" width="30%">
      <div class="dialog-title-container" slot="title" ref="linkTitle" />
      <el-form label-width="80px" size="medium" :model="linkForm">
        <el-form-item label="链接名">
          <el-input style="width:250px" v-model="linkForm.linkName" />
        </el-form-item>
        <el-form-item label="链接封面">
          <el-input style="width:250px" v-model="linkForm.linkCover" />
        </el-form-item>
        <el-form-item label="链接地址">
          <el-input style="width:250px" v-model="linkForm.linkAddress" />
        </el-form-item>
        <el-form-item label="链接介绍">
          <el-input style="width:250px" v-model="linkForm.linkIntro" />
        </el-form-item>
        <el-form-item label="审核状态">
          <el-select style="width:250px" v-model="linkForm.linkStatus">
            <el-option label="待审核" :value="0" />
            <el-option label="已通过" :value="1" />
            <el-option label="已拒绝" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="addOrEdit = false">取 消</el-button>
        <el-button type="primary" @click="addOrEditCategory">
          确 定
        </el-button>
      </div>
    </el-dialog>
  </el-card>
</template>

<script>
/**
 * 功能说明: 后台友链管理页。
 * 作者: OpenAI Codex
 * 创建时间: 2026-07-05
 * 用途概述: 管理友链申请、审核状态、手动新增和删除。
 */
export default {
  created() {
    this.listLinks();
  },
  data: function() {
    return {
      loading: true,
      deleteFlag: false,
      addOrEdit: false,
      linkIdList: [],
      linkList: [],
      linkForm: {
        id: null,
        linkName: "",
        linkCover: "",
        linkIntro: "",
        linkAddress: "",
        linkStatus: 1
      },
      keywords: null,
      status: null,
      current: 1,
      size: 10,
      count: 0
    };
  },
  methods: {
    /**
     * 获取审核状态显示文案。
     *
     * @param {number} status 审核状态。
     * @returns {string} 状态文案。
     */
    statusText(status) {
      const statusMap = {
        0: "待审核",
        1: "已通过",
        2: "已拒绝"
      };
      return statusMap[status] || "未知";
    },
    /**
     * 获取 Element UI 标签类型。
     *
     * @param {number} status 审核状态。
     * @returns {string} 标签类型。
     */
    statusTagType(status) {
      const typeMap = {
        0: "info",
        1: "success",
        2: "danger"
      };
      return typeMap[status] || "info";
    },
    selectionChange(linkList) {
      this.linkIdList = [];
      linkList.forEach(item => {
        this.linkIdList.push(item.id);
      });
    },
    searchLinks() {
      this.current = 1;
      this.listLinks();
    },
    sizeChange(size) {
      this.size = size;
      this.listLinks();
    },
    currentChange(current) {
      this.current = current;
      this.listLinks();
    },
    deleteLink(id) {
      var param = {};
      if (id == null) {
        param = { data: this.linkIdList };
      } else {
        param = { data: [id] };
      }
      this.axios.delete("/api/admin/links", param).then(({ data }) => {
        if (data.flag) {
          this.$notify.success({
            title: "成功",
            message: data.message
          });
          this.listLinks();
        } else {
          this.$notify.error({
            title: "失败",
            message: data.message
          });
        }
        this.deleteFlag = false;
      });
    },
    /**
     * 审核友链申请，只修改状态，不覆盖申请内容。
     *
     * @param {number} id 友链 ID。
     * @param {number} linkStatus 审核状态。
     */
    reviewLink(id, linkStatus) {
      this.axios
        .put("/api/admin/links/review", {
          id,
          linkStatus
        })
        .then(({ data }) => {
          if (data.flag) {
            this.$notify.success({
              title: "成功",
              message: data.message
            });
            this.listLinks();
          } else {
            this.$notify.error({
              title: "失败",
              message: data.message
            });
          }
        });
    },
    openModel(link) {
      if (link != null) {
        this.linkForm = JSON.parse(JSON.stringify(link));
        this.$refs.linkTitle.innerHTML = "修改友链";
      } else {
        this.linkForm.id = null;
        this.linkForm.linkName = "";
        this.linkForm.linkCover = "";
        this.linkForm.linkIntro = "";
        this.linkForm.linkAddress = "";
        // 后台手动新增默认已通过，前台申请才默认待审核。
        this.linkForm.linkStatus = 1;
        this.$refs.linkTitle.innerHTML = "添加友链";
      }
      this.addOrEdit = true;
    },
    addOrEditCategory() {
      if (this.linkForm.linkName.trim() == "") {
        this.$message.error("友链名不能为空");
        return false;
      }
      if (this.linkForm.linkCover.trim() == "") {
        this.$message.error("友链封面不能为空");
        return false;
      }
      if (this.linkForm.linkIntro.trim() == "") {
        this.$message.error("友链介绍不能为空");
        return false;
      }
      if (this.linkForm.linkAddress.trim() == "") {
        this.$message.error("友链地址不能为空");
        return false;
      }
      this.axios.post("/api/admin/links", this.linkForm).then(({ data }) => {
        if (data.flag) {
          this.$notify.success({
            title: "成功",
            message: data.message
          });
          this.listLinks();
        } else {
          this.$notify.error({
            title: "失败",
            message: data.message
          });
        }
        this.addOrEdit = false;
      });
    },
    listLinks() {
      this.loading = true;
      this.axios
        .get("/api/admin/links", {
          params: {
            current: this.current,
            size: this.size,
            keywords: this.keywords,
            status: this.status
          }
        })
        .then(({ data }) => {
          this.linkList = data.data.recordList;
          this.count = data.data.count;
          this.loading = false;
        });
    }
  }
};
</script>
