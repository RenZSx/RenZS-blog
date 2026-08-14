@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

:: 博客后台管理系统 - 若依冗余模块清理脚本 (Windows版本)
:: 使用前请确保已备份代码！

echo ==========================================
echo 博客后台管理系统 - 若依冗余模块清理脚本
echo ==========================================
echo.
echo ⚠️  警告：此操作将删除大量文件，请确保已备份！
echo.
set /p confirm="是否继续？(输入 yes 继续): "

if not "%confirm%"=="yes" (
    echo 操作已取消
    exit /b 0
)

echo.
echo 开始清理...
echo.

set deleted_files=0
set deleted_dirs=0

:: 阶段一：删除监控模块
echo 【阶段一】删除监控模块...
echo ----------------------------------------

if exist "src\views\monitor\" (
    for /f %%i in ('dir /s /b /a-d "src\views\monitor\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\monitor\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\monitor\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\monitor\ 不存在
)

if exist "src\api\monitor\" (
    for /f %%i in ('dir /s /b /a-d "src\api\monitor\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\api\monitor\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\api\monitor\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\api\monitor\ 不存在
)

echo.

:: 阶段二：删除工具模块
echo 【阶段二】删除工具模块...
echo ----------------------------------------

if exist "src\views\tool\" (
    for /f %%i in ('dir /s /b /a-d "src\views\tool\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\tool\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\tool\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\tool\ 不存在
)

if exist "src\api\tool\gen.js" (
    del /q "src\api\tool\gen.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\tool\gen.js
) else (
    echo ⊘ src\api\tool\gen.js 不存在
)

if exist "src\api\tool\" (
    dir /b "src\api\tool\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\api\tool\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\api\tool\
    )
)

echo.

:: 阶段三：删除系统管理模块
echo 【阶段三】删除系统管理模块...
echo ----------------------------------------

:: 删除字典管理
if exist "src\views\system\dict\" (
    for /f %%i in ('dir /s /b /a-d "src\views\system\dict\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\system\dict\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\system\dict\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\system\dict\ 不存在
)

if exist "src\api\system\dict\" (
    for /f %%i in ('dir /s /b /a-d "src\api\system\dict\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\api\system\dict\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\api\system\dict\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\api\system\dict\ 不存在
)

:: 删除参数设置
if exist "src\views\system\config\index.vue" (
    del /q "src\views\system\config\index.vue"
    set /a deleted_files+=1
    echo ✓ 删除 src\views\system\config\index.vue
)

if exist "src\views\system\config\" (
    dir /b "src\views\system\config\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\views\system\config\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\views\system\config\
    )
)

if exist "src\api\system\config.js" (
    del /q "src\api\system\config.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\config.js
)

:: 删除部门管理
if exist "src\views\system\dept\index.vue" (
    del /q "src\views\system\dept\index.vue"
    set /a deleted_files+=1
    echo ✓ 删除 src\views\system\dept\index.vue
)

if exist "src\views\system\dept\" (
    dir /b "src\views\system\dept\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\views\system\dept\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\views\system\dept\
    )
)

if exist "src\api\system\dept.js" (
    del /q "src\api\system\dept.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\dept.js
)

:: 删除岗位管理
if exist "src\views\system\post\index.vue" (
    del /q "src\views\system\post\index.vue"
    set /a deleted_files+=1
    echo ✓ 删除 src\views\system\post\index.vue
)

if exist "src\views\system\post\" (
    dir /b "src\views\system\post\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\views\system\post\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\views\system\post\
    )
)

if exist "src\api\system\post.js" (
    del /q "src\api\system\post.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\post.js
)

:: 删除若依通知公告
if exist "src\views\system\notice\" (
    for /f %%i in ('dir /s /b /a-d "src\views\system\notice\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\system\notice\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\system\notice\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\system\notice\ 不存在
)

if exist "src\api\system\notice.js" (
    del /q "src\api\system\notice.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\notice.js
)

:: 删除若依菜单管理
if exist "src\views\system\menu\index.vue" (
    del /q "src\views\system\menu\index.vue"
    set /a deleted_files+=1
    echo ✓ 删除 src\views\system\menu\index.vue
)

if exist "src\views\system\menu\" (
    dir /b "src\views\system\menu\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\views\system\menu\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\views\system\menu\
    )
)

if exist "src\api\system\menu.js" (
    del /q "src\api\system\menu.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\menu.js
)

:: 删除若依角色管理
if exist "src\views\system\role\" (
    for /f %%i in ('dir /s /b /a-d "src\views\system\role\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\system\role\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\system\role\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\system\role\ 不存在
)

if exist "src\api\system\role.js" (
    del /q "src\api\system\role.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\role.js
)

:: 删除若依用户管理
if exist "src\views\system\user\" (
    for /f %%i in ('dir /s /b /a-d "src\views\system\user\*" ^| find /c /v ""') do set file_count=%%i
    rd /s /q "src\views\system\user\"
    set /a deleted_files+=!file_count!
    set /a deleted_dirs+=1
    echo ✓ 删除 src\views\system\user\ ^(!file_count! 个文件^)
) else (
    echo ⊘ src\views\system\user\ 不存在
)

if exist "src\api\system\user.js" (
    del /q "src\api\system\user.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\api\system\user.js
)

:: 清理空目录
if exist "src\views\system\" (
    dir /b "src\views\system\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\views\system\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\views\system\
    ) else (
        echo ⚠ src\views\system\ 非空，请手动检查
    )
)

if exist "src\api\system\" (
    dir /b "src\api\system\" | findstr "^" >nul
    if errorlevel 1 (
        rd "src\api\system\"
        set /a deleted_dirs+=1
        echo ✓ 删除空目录 src\api\system\
    ) else (
        echo ⚠ src\api\system\ 非空，请手动检查
    )
)

echo.

:: 阶段四：删除字典Store
echo 【阶段四】删除Store模块...
echo ----------------------------------------

if exist "src\store\modules\dict.js" (
    del /q "src\store\modules\dict.js"
    set /a deleted_files+=1
    echo ✓ 删除 src\store\modules\dict.js
) else (
    echo ⊘ src\store\modules\dict.js 不存在
)

echo.

:: 统计结果
echo ==========================================
echo 清理完成！
echo ==========================================
echo 删除文件数: %deleted_files%
echo 删除目录数: %deleted_dirs%
echo.
echo ⚠️  接下来需要手动完成以下步骤：
echo.
echo 1. 编辑 src\router\index.js
echo    将 dynamicRoutes 数组清空（第101-172行）：
echo    export const dynamicRoutes = []
echo.
echo 2. 检查并修改 src\main.js
echo    删除以下引用（如果存在）：
echo    - import { getDicts } from '@/api/system/dict/data'
echo    - import { getConfigKey } from '@/api/system/config'
echo    - app.config.globalProperties.getDicts = getDicts
echo    - app.config.globalProperties.getConfigKey = getConfigKey
echo.
echo 3. 检查 src\utils\dict.js 是否被使用
echo    如果未被使用，可删除该文件
echo.
echo 4. 运行编译测试：
echo    npm run build:prod
echo.
echo 5. 运行功能测试：
echo    npm run dev
echo.
echo 6. 提交更改：
echo    git add .
echo    git commit -m "chore: 清理若依冗余模块"
echo.
pause
