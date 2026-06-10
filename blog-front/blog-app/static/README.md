# static 目录

uniapp 静态资源目录,放入图片/字体/图标等不需要打包处理的文件。

## 可选添加项

### App 图标 / 启动图
通过 HBuilderX 自动生成:
- 打开 manifest.json → App 图标配置 → 自动生成所有尺寸

### TabBar 图标(可选)
如果想给底部 TabBar 加图标:
1. 准备 8 张 PNG 图(每个 tab 一对:默认 + 选中态)
2. 放入 `static/tab/` 目录
3. 编辑 `pages.json` 的 `tabBar.list[].iconPath` 和 `selectedIconPath` 指向 PNG

> 当前 pages.json 使用纯文字 TabBar,无需图标也能正常工作。
