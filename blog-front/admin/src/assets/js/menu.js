import Layout from "@/layout/index.vue";
import router from "../../router";
import store from "../../store";
import axios from "axios";
import Vue from "vue";

let menuRouteLoaded = false;
let menuLoadPromise = null;

export function generaMenu() {
  if (menuRouteLoaded) {
    return Promise.resolve(store.state.userMenuList);
  }
  if (menuLoadPromise) {
    return menuLoadPromise;
  }

  // 查询用户菜单
  menuLoadPromise = axios.get("/api/admin/user/menus").then(({ data }) => {
    if (data.flag) {
      var userMenuList = data.data || [];
      userMenuList.forEach(item => {
        if (item.icon != null) {
          item.icon = "iconfont " + item.icon;
        }
        if (item.component == "Layout") {
          item.component = Layout;
        }
        if (item.children && item.children.length > 0) {
          item.children.forEach(route => {
            route.icon = "iconfont " + route.icon;
            route.component = loadView(route.component);
          });
        }
      });
      // 添加侧边栏菜单
      store.commit("saveUserMenuList", userMenuList);
      // 添加菜单到路由
      router.addRoutes(userMenuList);
      menuRouteLoaded = true;
      return userMenuList;
    } else {
      if (data.code !== 40001) {
        Vue.prototype.$message.error(data.message);
      }
      return Promise.reject(new Error(data.message || "菜单加载失败"));
    }
  });

  menuLoadPromise = menuLoadPromise
    .catch(error => {
      menuRouteLoaded = false;
      throw error;
    })
    .finally(() => {
      menuLoadPromise = null;
    });

  return menuLoadPromise;
}

export const loadView = view => {
  // 路由懒加载
  return resolve => require([`@/views${view}`], resolve);
};

export function isMenuRouteLoaded() {
  return menuRouteLoaded;
}

export function resetMenuRouteLoaded() {
  menuRouteLoaded = false;
  menuLoadPromise = null;
}
