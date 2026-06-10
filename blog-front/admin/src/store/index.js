import Vue from "vue";
import Vuex from "vuex";
import createPersistedState from "vuex-persistedstate";

Vue.use(Vuex);

// 自定义存储适配器，添加30天过期时间
const createExpiredStorage = (expireDays = 30) => {
  return {
    getItem: (key) => {
      try {
        const stored = localStorage.getItem(key);
        if (!stored) return null;

        const parsed = JSON.parse(stored);
        // 检查数据结构完整性
        if (!parsed || typeof parsed !== 'object' || !('data' in parsed) || !('expireAt' in parsed)) {
          localStorage.removeItem(key);
          return null;
        }
        // 检查是否过期（当前时间 > 过期时间）
        const now = Date.now();
        if (now > parsed.expireAt) {
          localStorage.removeItem(key);
          return null;
        }
        return JSON.stringify(parsed.data);
      } catch (error) {
        // 如果解析失败，清除损坏的数据
        localStorage.removeItem(key);
        return null;
      }
    },
    setItem: (key, value) => {
      try {
        // 计算过期时间：当前时间 + 30天（毫秒）
        const expireAt = Date.now() + expireDays * 24 * 60 * 60 * 1000;

        // 确保value是有效的JSON字符串
        const parsedValue = JSON.parse(value);

        const data = {
          data: parsedValue,
          expireAt: expireAt,
          // 添加存储时间戳用于调试
          storedAt: Date.now()
        };
        localStorage.setItem(key, JSON.stringify(data));
      } catch (error) {
        console.error("存储用户信息失败:", error);
      }
    },
    removeItem: (key) => {
      localStorage.removeItem(key);
    }
  };
};

export default new Vuex.Store({
  state: {
    collapse: false,
    tabList: [{ name: "首页", path: "/" }],
    userId: null,
    roleList: null,
    avatar: null,
    nickname: null,
    intro: null,
    webSite: null,
    userMenuList: []
  },
  mutations: {
    saveTab(state, tab) {
      if (state.tabList.findIndex(item => item.path === tab.path) == -1) {
        state.tabList.push({ name: tab.name, path: tab.path });
      }
    },
    removeTab(state, tab) {
      var index = state.tabList.findIndex(item => item.name === tab.name);
      state.tabList.splice(index, 1);
    },
    resetTab(state) {
      state.tabList = [{ name: "首页", path: "/" }];
    },
    trigger(state) {
      state.collapse = !state.collapse;
    },
    login(state, user) {
      state.userId = user.userInfoId;
      state.roleList = user.roleList;
      state.avatar = user.avatar;
      state.nickname = user.nickname;
      state.intro = user.intro;
      state.webSite = user.webSite;
    },
    saveUserMenuList(state, userMenuList) {
      state.userMenuList = userMenuList;
    },
    logout(state) {
      state.userId = null;
      state.roleList = null;
      state.avatar = null;
      state.nickname = null;
      state.intro = null;
      state.webSite = null;
      state.userMenuList = [];
    },
    updateAvatar(state, avatar) {
      state.avatar = avatar;
    },
    updateUserInfo(state, user) {
      state.nickname = user.nickname;
      state.intro = user.intro;
      state.webSite = user.webSite;
    }
  },
  actions: {},
  modules: {},
  plugins: [
    createPersistedState({
      // 使用自定义存储适配器，设置30天过期
      storage: createExpiredStorage(30),
      paths: [
        "userId",
        "avatar",
        "roleList",
        "nickname",
        "intro",
        "webSite",
        "userMenuList"
      ]
    })
  ]
});
