/**
 * 通用js方法封装处理
 * Copyright (c) 2021-2026 RenZS Blog Admin
 */

// 日期格式化
export function parseTime(time, pattern) {
  if (arguments.length === 0 || !time) {
    return null
  }
  const format = pattern || '{y}-{m}-{d} {h}:{i}:{s}'
  let date
  if (typeof time === 'object') {
    date = time
  } else {
    if ((typeof time === 'string') && (/^[0-9]+$/.test(time))) {
      time = parseInt(time)
    } else if (typeof time === 'string') {
      time = time.replace(new RegExp(/-/gm), '/').replace('T', ' ').replace(new RegExp(/\.[\d]{3}/gm), '')
    }
    if ((typeof time === 'number') && (time.toString().length === 10)) {
      time = time * 1000
    }
    date = new Date(time)
  }
  const formatObj = {
    y: date.getFullYear(),
    m: date.getMonth() + 1,
    d: date.getDate(),
    h: date.getHours(),
    i: date.getMinutes(),
    s: date.getSeconds(),
    a: date.getDay()
  }
  const time_str = format.replace(/{(y|m|d|h|i|s|a)+}/g, (result, key) => {
    let value = formatObj[key]
    // Note: getDay() returns 0 on Sunday
    if (key === 'a') { return ['日', '一', '二', '三', '四', '五', '六'][value] }
    if (result.length > 0 && value < 10) {
      value = '0' + value
    }
    return value || 0
  })
  return time_str
}

// 表单重置
export function resetForm(refName) {
  if (this.$refs[refName]) {
    this.$refs[refName].resetFields()
  }
}

// 添加日期范围
export function addDateRange(params, dateRange, propName) {
  let search = params
  search.params = typeof (search.params) === 'object' && search.params !== null && !Array.isArray(search.params) ? search.params : {}
  dateRange = Array.isArray(dateRange) ? dateRange : []
  if (typeof (propName) === 'undefined') {
    search.params['beginTime'] = dateRange[0]
    search.params['endTime'] = dateRange[1]
  } else {
    search.params['begin' + propName] = dateRange[0]
    search.params['end' + propName] = dateRange[1]
  }
  return search
}

// 回显数据字典
export function selectDictLabel(datas, value) {
  if (value === undefined) {
    return ""
  }
  const actions = []
  Object.keys(datas).some((key) => {
    if (datas[key].value == ('' + value)) {
      actions.push(datas[key].label)
      return true
    }
  })
  if (actions.length === 0) {
    actions.push(value)
  }
  return actions.join('')
}

// 回显数据字典（字符串、数组）
export function selectDictLabels(datas, value, separator) {
  if (value === undefined || value.length ===0) {
    return ""
  }
  if (Array.isArray(value)) {
    value = value.join(",")
  }
  const actions = []
  const currentSeparator = undefined === separator ? "," : separator
  const temp = value.split(currentSeparator)
  Object.keys(value.split(currentSeparator)).some((val) => {
    let match = false
    Object.keys(datas).some((key) => {
      if (datas[key].value == ('' + temp[val])) {
        actions.push(datas[key].label + currentSeparator)
        match = true
      }
    })
    if (!match) {
      actions.push(temp[val] + currentSeparator)
    }
  })
  return actions.join('').substring(0, actions.join('').length - 1)
}

// 字符串格式化(%s )
export function sprintf(str) {
  let flag = true, i = 1
  str = str.replace(/%s/g, function () {
    const arg = args[i++]
    if (typeof arg === 'undefined') {
      flag = false
      return ''
    }
    return arg
  })
  return flag ? str : ''
}

// 转换字符串，undefined,null等转化为""
export function parseStrEmpty(str) {
  if (!str || str == "undefined" || str == "null") {
    return ""
  }
  return str
}

// 数据合并
export function mergeRecursive(source, target) {
  for (const p in target) {
    try {
      if (target[p].constructor == Object) {
        source[p] = mergeRecursive(source[p], target[p])
      } else {
        source[p] = target[p]
      }
    } catch (e) {
      source[p] = target[p]
    }
  }
  return source
}

/**
 * 构造树型结构数据
 * @param {*} data 数据源
 * @param {*} id id字段 默认 'id'
 * @param {*} parentId 父节点字段 默认 'parentId'
 * @param {*} children 孩子节点字段 默认 'children'
 */
export function handleTree(data, id, parentId, children) {
  const config = {
    id: id || 'id',
    parentId: parentId || 'parentId',
    childrenList: children || 'children'
  }

  const childrenListMap = {}
  const tree = []
  for (const d of data) {
    const id = d[config.id]
    childrenListMap[id] = d
    if (!d[config.childrenList]) {
      d[config.childrenList] = []
    }
  }

  for (const d of data) {
    const parentId = d[config.parentId]
    const parentObj = childrenListMap[parentId]
    if (!parentObj) {
      tree.push(d)
    } else {
      parentObj[config.childrenList].push(d)
    }
  }
  return tree
}

/**
* 参数处理
* @param {*} params  参数
*/
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    const part = encodeURIComponent(propName) + "="
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            const params = propName + '[' + key + ']'
            const subPart = encodeURIComponent(params) + "="
            result += subPart + encodeURIComponent(value[key]) + "&"
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&"
      }
    }
  }
  return result
}

// 返回项目路径
export function getNormalPath(p) {
  if (p.length === 0 || !p || p == 'undefined') {
    return p
  }
  let res = p.replace('//', '/')
  if (res[res.length - 1] === '/') {
    return res.slice(0, res.length - 1)
  }
  return res
}

/**
 * 按 Vue Router 4 的嵌套路由语义拼接父子路径。
 *
 * Vue Router 规定: 子路由 path 以 '/' 开头时视为绝对路径,直接作为最终
 * 路径使用,不与父路径拼接;否则才与父路径相对拼接。
 *
 * 博客后端菜单下发的子路径均为绝对路径(如父 /article-submenu 下的子
 * /articles),若无条件拼接会得到 /article-submenu/articles —— 该路径
 * 并未注册,导致 404。
 *
 * @param {string} basePath 父路由路径
 * @param {string} routePath 子路由路径
 * @returns {string} 解析后的完整路径
 */
export function resolveRoutePath(basePath, routePath) {
  // 子路径为绝对路径时直接返回,与 Vue Router 注册时的行为保持一致
  if (routePath && routePath.charAt(0) === '/') {
    return getNormalPath(routePath)
  }
  if (!basePath) {
    return getNormalPath(routePath)
  }
  // 根路径特判: basePath='/' 与空子路径拼接得到 '//',getNormalPath 会
  // 先替换成 '/' 再因尾部斜杠被 slice 成空字符串,导致首页菜单点击无反应。
  // 根路径(首页)应直接返回 '/'。
  if (basePath === '/') {
    return '/'
  }
  return getNormalPath(basePath + '/' + routePath)
}

// 验证是否为blob格式
export function blobValidate(data) {
  return data.type !== 'application/json'
}
