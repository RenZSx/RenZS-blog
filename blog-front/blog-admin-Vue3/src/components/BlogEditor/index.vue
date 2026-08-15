<template>
  <div
    ref="editor"
    class="edit-container"
    v-html="sanitizeHtml(innerText)"
    :placeholder="placeholder"
    :contenteditable="disable"
    @focus="onFocus"
    @blur="onBlur"
    @input="onInput"
  />
</template>

<script setup>
import { ref, watch } from 'vue'
import { sanitizeHtml } from '@/utils/sanitize'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  disable: {
    type: Boolean,
    default: true
  },
  placeholder: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'focus', 'blur'])

const editor = ref(null)
const innerText = ref(props.modelValue)
const isLocked = ref(false)
const range = ref(null)

watch(() => props.modelValue, (newVal) => {
  if (!isLocked.value) {
    innerText.value = newVal
  }
})

const clear = () => {
  editor.value.innerHTML = ''
  emit('update:modelValue', sanitizeHtml(editor.value.innerHTML))
}

const onInput = () => {
  emit('update:modelValue', sanitizeHtml(editor.value.innerHTML))
}

const onFocus = () => {
  emit('focus', sanitizeHtml(editor.value.innerHTML))
  isLocked.value = true
}

const onBlur = () => {
  // 记录光标
  if (window.getSelection) {
    const selection = window.getSelection()
    range.value = selection.getRangeAt(0)
  }
  emit('blur', sanitizeHtml(editor.value.innerHTML))
  isLocked.value = false
}

const addText = (value) => {
  // 还原光标
  if (window.getSelection) {
    const selection = window.getSelection()
    selection.removeAllRanges()
    // 为空初始化光标
    if (range.value == null) {
      editor.value.focus()
      range.value = selection.getRangeAt(0)
    }
    // 删除选中内容
    range.value.deleteContents()
    // 添加内容
    range.value.insertNode(range.value.createContextualFragment(sanitizeHtml(value)))
    range.value.collapse(false)
    selection.addRange(range.value)
    emit('update:modelValue', sanitizeHtml(editor.value.innerHTML))
  }
}

defineExpose({
  clear,
  addText
})
</script>

<style scoped>
.edit-container {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 8px;
  background: var(--el-fill-color-light);
  font-size: 14px;
  line-height: 1.5;
  padding: 6px 12px;
  box-sizing: border-box;
  overflow: auto;
  word-break: break-all;
  outline: none;
  user-select: text;
  white-space: pre-wrap;
  text-align: left;
  color: var(--el-text-color-primary);
  -webkit-user-modify: read-write-plaintext-only;
}
.edit-container:empty::before {
  cursor: text;
  content: attr(placeholder);
  color: var(--el-text-color-placeholder);
}
</style>
