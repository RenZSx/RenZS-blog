#!/bin/bash

# 博客后台管理系统 - 若依冗余模块清理脚本
# 使用前请确保已备份代码！

set -e

echo "=========================================="
echo "博客后台管理系统 - 若依冗余模块清理脚本"
echo "=========================================="
echo ""
echo "⚠️  警告：此操作将删除大量文件，请确保已备份！"
echo ""
read -p "是否继续？(输入 yes 继续): " confirm

if [ "$confirm" != "yes" ]; then
    echo "操作已取消"
    exit 0
fi

echo ""
echo "开始清理..."
echo ""

# 统计变量
deleted_files=0
deleted_dirs=0

# 阶段一：删除监控模块
echo "【阶段一】删除监控模块..."
echo "----------------------------------------"

if [ -d "src/views/monitor" ]; then
    file_count=$(find src/views/monitor -type f | wc -l)
    rm -rf src/views/monitor/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/monitor/ ($file_count 个文件)"
else
    echo "⊘ src/views/monitor/ 不存在"
fi

if [ -d "src/api/monitor" ]; then
    file_count=$(find src/api/monitor -type f | wc -l)
    rm -rf src/api/monitor/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/api/monitor/ ($file_count 个文件)"
else
    echo "⊘ src/api/monitor/ 不存在"
fi

echo ""

# 阶段二：删除工具模块
echo "【阶段二】删除工具模块..."
echo "----------------------------------------"

if [ -d "src/views/tool" ]; then
    file_count=$(find src/views/tool -type f | wc -l)
    rm -rf src/views/tool/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/tool/ ($file_count 个文件)"
else
    echo "⊘ src/views/tool/ 不存在"
fi

if [ -f "src/api/tool/gen.js" ]; then
    rm -f src/api/tool/gen.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/tool/gen.js"
else
    echo "⊘ src/api/tool/gen.js 不存在"
fi

if [ -d "src/api/tool" ] && [ -z "$(ls -A src/api/tool)" ]; then
    rmdir src/api/tool/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/api/tool/"
fi

echo ""

# 阶段三：删除系统管理模块
echo "【阶段三】删除系统管理模块..."
echo "----------------------------------------"

# 删除字典管理
if [ -d "src/views/system/dict" ]; then
    file_count=$(find src/views/system/dict -type f | wc -l)
    rm -rf src/views/system/dict/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/system/dict/ ($file_count 个文件)"
else
    echo "⊘ src/views/system/dict/ 不存在"
fi

if [ -d "src/api/system/dict" ]; then
    file_count=$(find src/api/system/dict -type f | wc -l)
    rm -rf src/api/system/dict/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/api/system/dict/ ($file_count 个文件)"
else
    echo "⊘ src/api/system/dict/ 不存在"
fi

# 删除参数设置
if [ -f "src/views/system/config/index.vue" ]; then
    rm -f src/views/system/config/index.vue
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/views/system/config/index.vue"
fi

if [ -d "src/views/system/config" ] && [ -z "$(ls -A src/views/system/config)" ]; then
    rmdir src/views/system/config/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/views/system/config/"
fi

if [ -f "src/api/system/config.js" ]; then
    rm -f src/api/system/config.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/config.js"
fi

# 删除部门管理
if [ -f "src/views/system/dept/index.vue" ]; then
    rm -f src/views/system/dept/index.vue
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/views/system/dept/index.vue"
fi

if [ -d "src/views/system/dept" ] && [ -z "$(ls -A src/views/system/dept)" ]; then
    rmdir src/views/system/dept/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/views/system/dept/"
fi

if [ -f "src/api/system/dept.js" ]; then
    rm -f src/api/system/dept.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/dept.js"
fi

# 删除岗位管理
if [ -f "src/views/system/post/index.vue" ]; then
    rm -f src/views/system/post/index.vue
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/views/system/post/index.vue"
fi

if [ -d "src/views/system/post" ] && [ -z "$(ls -A src/views/system/post)" ]; then
    rmdir src/views/system/post/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/views/system/post/"
fi

if [ -f "src/api/system/post.js" ]; then
    rm -f src/api/system/post.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/post.js"
fi

# 删除若依通知公告
if [ -d "src/views/system/notice" ]; then
    file_count=$(find src/views/system/notice -type f | wc -l)
    rm -rf src/views/system/notice/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/system/notice/ ($file_count 个文件)"
else
    echo "⊘ src/views/system/notice/ 不存在"
fi

if [ -f "src/api/system/notice.js" ]; then
    rm -f src/api/system/notice.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/notice.js"
fi

# 删除若依菜单管理
if [ -f "src/views/system/menu/index.vue" ]; then
    rm -f src/views/system/menu/index.vue
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/views/system/menu/index.vue"
fi

if [ -d "src/views/system/menu" ] && [ -z "$(ls -A src/views/system/menu)" ]; then
    rmdir src/views/system/menu/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/views/system/menu/"
fi

if [ -f "src/api/system/menu.js" ]; then
    rm -f src/api/system/menu.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/menu.js"
fi

# 删除若依角色管理
if [ -d "src/views/system/role" ]; then
    file_count=$(find src/views/system/role -type f | wc -l)
    rm -rf src/views/system/role/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/system/role/ ($file_count 个文件)"
else
    echo "⊘ src/views/system/role/ 不存在"
fi

if [ -f "src/api/system/role.js" ]; then
    rm -f src/api/system/role.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/role.js"
fi

# 删除若依用户管理
if [ -d "src/views/system/user" ]; then
    file_count=$(find src/views/system/user -type f | wc -l)
    rm -rf src/views/system/user/
    deleted_files=$((deleted_files + file_count))
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除 src/views/system/user/ ($file_count 个文件)"
else
    echo "⊘ src/views/system/user/ 不存在"
fi

if [ -f "src/api/system/user.js" ]; then
    rm -f src/api/system/user.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/api/system/user.js"
fi

# 清理空目录
if [ -d "src/views/system" ] && [ -z "$(ls -A src/views/system)" ]; then
    rmdir src/views/system/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/views/system/"
elif [ -d "src/views/system" ]; then
    echo "⚠ src/views/system/ 非空，请手动检查"
fi

if [ -d "src/api/system" ] && [ -z "$(ls -A src/api/system)" ]; then
    rmdir src/api/system/
    deleted_dirs=$((deleted_dirs + 1))
    echo "✓ 删除空目录 src/api/system/"
elif [ -d "src/api/system" ]; then
    echo "⚠ src/api/system/ 非空，请手动检查"
fi

echo ""

# 阶段四：删除字典Store
echo "【阶段四】删除Store模块..."
echo "----------------------------------------"

if [ -f "src/store/modules/dict.js" ]; then
    rm -f src/store/modules/dict.js
    deleted_files=$((deleted_files + 1))
    echo "✓ 删除 src/store/modules/dict.js"
else
    echo "⊘ src/store/modules/dict.js 不存在"
fi

echo ""

# 统计结果
echo "=========================================="
echo "清理完成！"
echo "=========================================="
echo "删除文件数: $deleted_files"
echo "删除目录数: $deleted_dirs"
echo ""
echo "⚠️  接下来需要手动完成以下步骤："
echo ""
echo "1. 编辑 src/router/index.js"
echo "   将 dynamicRoutes 数组清空（第101-172行）："
echo "   export const dynamicRoutes = []"
echo ""
echo "2. 检查并修改 src/main.js"
echo "   删除以下引用（如果存在）："
echo "   - import { getDicts } from '@/api/system/dict/data'"
echo "   - import { getConfigKey } from '@/api/system/config'"
echo "   - app.config.globalProperties.getDicts = getDicts"
echo "   - app.config.globalProperties.getConfigKey = getConfigKey"
echo ""
echo "3. 检查 src/utils/dict.js 是否被使用"
echo "   如果未被使用，可删除该文件"
echo ""
echo "4. 运行编译测试："
echo "   npm run build:prod"
echo ""
echo "5. 运行功能测试："
echo "   npm run dev"
echo ""
echo "6. 提交更改："
echo "   git add ."
echo "   git commit -m 'chore: 清理若依冗余模块'"
echo ""
