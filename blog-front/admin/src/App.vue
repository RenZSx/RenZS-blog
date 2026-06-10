<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
export default {
  created() {
    // 上传访客信息
    this.axios.post("/api/report");
  }
};
</script>
