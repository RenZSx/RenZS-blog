import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import "./assets/css/index.css";
import "./assets/css/iconfont.css";
import config from "./assets/js/config";
import axios from "axios";
import VueAxios from "vue-axios";
import ECharts from "vue-echarts";
import "echarts/lib/chart/line";
import "echarts/lib/chart/pie";
import "echarts/lib/chart/bar";
import "echarts/lib/chart/map";
import "echarts/lib/component/tooltip";
import "echarts/lib/component/legend";
import "echarts/lib/component/title";
import mavonEditor from "mavon-editor";
import "mavon-editor/dist/css/index.css";
import NProgress from "nprogress";
import "nprogress/nprogress.css";
import VueCalendarHeatmap from "vue-calendar-heatmap";
import dayjs from "dayjs";
import tagCloud from "./components/tag-cloud";
import {
  generaMenu,
  isMenuRouteLoaded,
  resetMenuRouteLoaded
} from "./assets/js/menu";
import { resetRouter } from "./router";

Vue.prototype.config = config;
// 全端纯 Header 鉴权,axios 不再依赖浏览器 Cookie 凭证;
// token 通过 localStorage('token') 持久化,由下方请求拦截器自动注入 Authorization Header。
axios.defaults.withCredentials = false;
Vue.use(mavonEditor);
Vue.use(tagCloud);
Vue.use(VueCalendarHeatmap);
Vue.component("v-chart", ECharts);
Vue.use(VueAxios, axios);
Vue.use(ElementUI);
Vue.config.productionTip = false;
Vue.prototype.$moment = dayjs;

Vue.filter("date", function(value, formatStr = "YYYY-MM-DD") {
  return dayjs(value).format(formatStr);
});

Vue.filter("dateTime", function(value, formatStr = "YYYY-MM-DD HH:mm:ss") {
  return dayjs(value).format(formatStr);
});

NProgress.configure({
  easing: "ease", // 动画方式
  speed: 500, // 递增进度条的速度
  showSpinner: false, // 是否显示加载ico
  trickleSpeed: 200, // 自动递增间隔
  minimum: 0.3 // 初始化时的最小百分比
});

function clearLoginState() {
  // 清理本地 token,与 store.commit("logout") 同时执行,
  // 确保 40001 拦截器、登出按钮、路由守卫等任意触发清理路径行为一致。
  localStorage.removeItem("token");
  store.commit("logout");
  store.commit("resetTab");
  resetRouter();
  resetMenuRouteLoaded();
}

function redirectToLogin(message) {
  clearLoginState();
  if (router.currentRoute.path !== "/login") {
    Vue.prototype.$message({
      type: "error",
      message: message || "登录已失效，请重新登录"
    });
    router.replace({ path: "/login" }).catch(() => {});
  }
}

router.beforeEach((to, from, next) => {
  NProgress.start();
  if (to.path == "/login") {
    next();
  } else if (!store.state.userId || !localStorage.getItem("token")) {
    // 任一缺失就视为未登录:防止 store 残留(persistedstate)+ token 被清的诡异中间态。
    next({ path: "/login" });
  } else if (!isMenuRouteLoaded()) {
    generaMenu()
      .then(() => {
        next({ ...to, replace: true });
      })
      .catch(() => {
        clearLoginState();
        next({ path: "/login" });
      });
  } else {
    next();
  }
});

router.afterEach(() => {
  NProgress.done();
});

// 请求拦截器:从 localStorage 取 token,统一注入 Authorization Header。
// 与后端 sa-token (token-name: Authorization, token-prefix: Bearer) / blog-vue3 request.ts 完全对齐;
// 全工程 70+ 处 this.axios.* 业务调用因此无需逐一修改,统一在此处获得 Header。
axios.interceptors.request.use(
  function(config) {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers["Authorization"] = "Bearer " + token;
    }
    return config;
  },
  function(error) {
    return Promise.reject(error);
  }
);

// 响应拦截器
axios.interceptors.response.use(
  function(response) {
    switch (response.data.code) {
      case 40001:
        redirectToLogin(response.data.message);
        break;
      case 50000:
        Vue.prototype.$message({
          type: "error",
          message: response.data.message
        });
        break;
    }
    return response;
  },
  function(error) {
    return Promise.reject(error);
  }
);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount("#app");
