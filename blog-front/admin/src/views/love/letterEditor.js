/**
 * 飞书传信后台编辑辅助工具。
 * 功能：统一初始化和归一化信件表单，避免纪念页管理首次渲染时出现空字段。
 * 作者：Chen
 * 创建时间：2026-05-17
 */

/**
 * 创建信件表单默认值。
 *
 * @returns {{ letterTitle: string, letterContent: string }}
 */
function createDefaultLetterForm() {
  return {
    letterTitle: "",
    letterContent: ""
  };
}

/**
 * 归一化后端返回的信件对象。
 *
 * @param {Object} payload 后端返回的信件数据。
 * @returns {{ letterTitle: string, letterContent: string }}
 */
function normalizeLetterForm(payload) {
  return {
    letterTitle:
      payload && payload.letterTitle ? String(payload.letterTitle) : "",
    letterContent:
      payload && payload.letterContent ? String(payload.letterContent) : ""
  };
}

module.exports = {
  createDefaultLetterForm,
  normalizeLetterForm
};
