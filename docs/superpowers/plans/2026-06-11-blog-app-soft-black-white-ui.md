# Blog App Soft Black White UI Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Apply the approved "雾白柔和" soft black-white visual system to `blog-front/blog-app`, covering global tokens and the core mobile pages.

**Architecture:** Keep the existing uni-app/Vue 3 structure and business behavior unchanged. First update global design tokens in `uni.scss` and `App.vue`, then replace high-saturation page-local styles in the six core pages with neutral soft black-white styles.

**Tech Stack:** uni-app, Vue 3 `<script setup>`, SCSS, Pinia, existing `bx-icon` component.

---

## File Structure

- Modify `blog-front/blog-app/uni.scss`: global SCSS design tokens, primary color, neutral palette, shadows.
- Modify `blog-front/blog-app/App.vue`: runtime CSS variables for light/dark theme and global utility colors.
- Modify `blog-front/blog-app/pages/index/index.vue`: homepage hero, search button, article tags, card surfaces, icon colors.
- Modify `blog-front/blog-app/pages/article/article.vue`: rich text link/blockquote/code colors, article shell, footer action bar.
- Modify `blog-front/blog-app/pages/category/category.vue`: replace colorful category gradients and tag colors with neutral category cards.
- Modify `blog-front/blog-app/pages/notice/notice.vue`: neutral notification header, tabs, unread state, type icons.
- Modify `blog-front/blog-app/pages/profile/profile.vue`: replace green hero, decorative blobs, colorful menu icon backgrounds.
- Modify `blog-front/blog-app/pages/login/login.vue`: replace green login hero and form focus colors with soft monochrome.
- Do not modify `blog-front/blog-app/unpackage/**`.

## Task 1: Global Soft Black-White Tokens

**Files:**
- Modify: `blog-front/blog-app/uni.scss`
- Modify: `blog-front/blog-app/App.vue`

- [ ] **Step 1: Inspect current global color references**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'uni.scss','App.vue' -Pattern '#42b983|#339268|#2d8362|#f7f8fa|#f5f7fa' -AllMatches"
```

Expected: output shows green primary tokens and light gray background variables in `uni.scss` and `App.vue`.

- [ ] **Step 2: Update SCSS design tokens in `uni.scss`**

Replace the brand color and background token block with these values while keeping existing variable names:

```scss
$primary: #3a3a3a;
$primary-50: #f6f6f3;
$primary-100: #eeeeea;
$primary-200: #deded8;
$primary-400: #8f8f89;
$primary-500: #5a5a56;
$primary-600: #3a3a3a;
$primary-700: #252525;
$primary-800: #181818;

$primary-gradient: linear-gradient(135deg, #4a4a4a 0%, #252525 100%);
$primary-gradient-soft: linear-gradient(135deg, #fbfbfa 0%, #eeeeea 100%);

$gray-50: #fbfbfa;
$gray-100: #f6f6f3;
$gray-200: #eeeeea;
$gray-300: #deded8;
$gray-400: #c8c8c2;
$gray-500: #9a9a96;
$gray-600: #777773;
$gray-700: #3a3a3a;
$gray-800: #252525;
$gray-900: #111111;

$text-primary: #252525;
$text-regular: #555552;
$text-secondary: #888883;
$text-placeholder: #b8b8b2;
$text-inverse: #ffffff;

$border-color: #e8e8e3;
$border-color-light: #f0f0ed;
$border-color-dark: #d8d8d2;

$bg-body: #f6f6f3;
$bg-card: #ffffff;
$bg-soft: #eeeeea;
$bg-overlay: rgba(24, 24, 24, 0.42);

$success: #4f6258;
$success-soft: #eef2ef;
$warning: #7b705d;
$warning-soft: #f3f0ea;
$danger: #7b4f4f;
$danger-soft: #f2eeee;
$info: #888883;

$shadow-sm: 0 2rpx 8rpx rgba(24, 24, 24, 0.04),
            0 1rpx 2rpx rgba(24, 24, 24, 0.05);
$shadow-md: 0 8rpx 18rpx rgba(24, 24, 24, 0.06),
            0 2rpx 5rpx rgba(24, 24, 24, 0.04);
$shadow-lg: 0 14rpx 34rpx rgba(24, 24, 24, 0.08),
            0 5rpx 10rpx rgba(24, 24, 24, 0.04);
$shadow-xl: 0 26rpx 54rpx rgba(24, 24, 24, 0.12),
            0 8rpx 18rpx rgba(24, 24, 24, 0.06);
$shadow-primary: 0 8rpx 20rpx rgba(58, 58, 58, 0.22);
$shadow-primary-strong: 0 12rpx 32rpx rgba(58, 58, 58, 0.28);
```

- [ ] **Step 3: Update runtime CSS variables in `App.vue`**

In the `page` block, set these values:

```scss
page {
  --bg-page: #f6f6f3;
  --bg-card: #ffffff;
  --bg-soft: #eeeeea;
  --bg-overlay: rgba(24, 24, 24, 0.42);

  --text-primary: #252525;
  --text-regular: #555552;
  --text-secondary: #888883;
  --text-placeholder: #b8b8b2;

  --border-color: #e8e8e3;
  --border-color-light: #f0f0ed;

  --color-primary: #3a3a3a;
  --color-primary-soft: #eeeeea;
  --color-primary-muted: #777773;

  --shadow-sm: 0 2rpx 8rpx rgba(24, 24, 24, 0.04), 0 1rpx 2rpx rgba(24, 24, 24, 0.05);
  --shadow-md: 0 8rpx 18rpx rgba(24, 24, 24, 0.06), 0 2rpx 5rpx rgba(24, 24, 24, 0.04);
  --shadow-lg: 0 14rpx 34rpx rgba(24, 24, 24, 0.08), 0 5rpx 10rpx rgba(24, 24, 24, 0.04);
}
```

Keep the existing dark theme block, but add matching primary variables:

```scss
body.theme-dark page,
page.theme-dark {
  --color-primary: #f5f5f0;
  --color-primary-soft: #2a2a2a;
  --color-primary-muted: #c8c8c2;
}
```

- [ ] **Step 4: Run static token check**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'uni.scss','App.vue' -Pattern '#42b983|#339268|#2d8362' -AllMatches"
```

Expected: no green references remain in these two files, unless a preserved comment explicitly explains legacy color.

- [ ] **Step 5: Commit global token changes**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app/uni.scss' 'blog-front/blog-app/App.vue'; git commit -m 'style(app): add soft black white design tokens'"
```

Expected: commit succeeds.

## Task 2: Homepage Soft Reading Surface

**Files:**
- Modify: `blog-front/blog-app/pages/index/index.vue`

- [ ] **Step 1: Replace hardcoded homepage icon colors**

Change template icon colors:

```vue
<bx-icon name="search" :size="36" color="var(--color-primary)" />
```

For normal card meta icons:

```vue
<bx-icon name="clock" :size="22" color="var(--text-secondary)" />
<bx-icon name="eye" :size="22" color="var(--text-secondary)" />
<bx-icon name="comment" :size="22" color="var(--text-secondary)" />
```

- [ ] **Step 2: Replace homepage SCSS highlight colors**

Update these selectors:

```scss
.hero-bar {
  padding: 28rpx 32rpx 24rpx;
  background: linear-gradient(180deg, rgba(58, 58, 58, 0.05) 0%, rgba(246, 246, 243, 0) 100%);
}

.search-entry {
  background: var(--bg-card);
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.search-entry:active {
  background: var(--color-primary-soft);
}

.stat-num {
  color: var(--color-primary);
}

.article-card {
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-md);
}

.tag {
  background: var(--color-primary-soft);
  color: var(--color-primary-muted);
}

.card-cover {
  background-color: var(--bg-soft);
}
```

- [ ] **Step 3: Keep image hero readable**

Keep the existing dark image overlay for the first cover card. Only change `tag-chip` so it remains neutral:

```scss
.tag-chip {
  background: rgba(255, 255, 255, 0.18);
  color: #ffffff;
  border: 1rpx solid rgba(255, 255, 255, 0.24);
}
```

- [ ] **Step 4: Verify homepage references**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'pages\index\index.vue' -Pattern '#42b983|#339268|rgba\(66, 185, 131' -AllMatches"
```

Expected: no matches.

- [ ] **Step 5: Commit homepage changes**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app/pages/index/index.vue'; git commit -m 'style(app): soften homepage article surfaces'"
```

Expected: commit succeeds.

## Task 3: Article Detail Neutral Reading UI

**Files:**
- Modify: `blog-front/blog-app/pages/article/article.vue`

- [ ] **Step 1: Replace article template action icon colors**

Use neutral active/inactive colors:

```vue
<bx-icon
  :name="isLiked ? 'heartFilled' : 'heart'"
  :size="44"
  :color="isLiked ? 'var(--color-primary)' : 'var(--text-secondary)'"
/>
```

```vue
<bx-icon
  :name="isCollected ? 'starFilled' : 'star'"
  :size="44"
  :color="isCollected ? 'var(--color-primary)' : 'var(--text-secondary)'"
/>
```

For comments and edit:

```vue
<bx-icon name="comment" :size="44" color="var(--text-secondary)" />
<bx-icon name="edit" :size="28" color="var(--text-secondary)" />
```

- [ ] **Step 2: Update rich text tag styles**

Change only the color values in `mdTagStyle`:

```js
a: 'color:#3a3a3a;',
code: 'background:#252525;color:#f5f5f0;padding:1px 4px;border-radius:3px;font-family:Consolas,Menlo,monospace;font-size:13.5px;',
pre: 'background:#252525;color:#f5f5f0;padding:9px;border-radius:6px;overflow-x:auto;font-family:Consolas,Menlo,monospace;font-size:13.5px;line-height:1.5;margin:10px 0;white-space:pre;box-shadow:0 2px 6px rgba(24,24,24,0.12);',
blockquote: 'border-left:3px solid #3a3a3a;padding:6px 8px;background:#eeeeea;color:#555552;margin:8px 0;border-radius:0 6px 6px 0;',
hr: 'border:none;height:1px;background:#e8e8e3;margin:14px 0;',
thead: 'background:#f6f6f3;',
th: 'font-size:13.5px;line-height:1.5;padding:5px 6px;border:1px solid #e8e8e3;font-weight:600;color:#252525;text-align:left;',
td: 'font-size:13.5px;line-height:1.5;padding:5px 6px;border:1px solid #e8e8e3;color:#555552;',
```

- [ ] **Step 3: Update article shell SCSS**

Where the page uses local surface colors, use global variables:

```scss
.article {
  background: var(--bg-page);
}

.header,
.body {
  background: var(--bg-card);
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.cat,
.tag {
  background: var(--color-primary-soft);
  color: var(--color-primary-muted);
}

.action-bar {
  background: var(--bg-card);
  border-top: 1rpx solid var(--border-color);
  box-shadow: 0 -8rpx 24rpx rgba(24, 24, 24, 0.06);
}

.action-btn.active .action-label,
.action-label.active,
.action-label.collected {
  color: var(--color-primary);
}

.comment-cta {
  background: var(--bg-soft);
  color: var(--text-secondary);
  border: 1rpx solid var(--border-color);
}
```

- [ ] **Step 4: Verify article references**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'pages\article\article.vue' -Pattern '#42b983|#339268|rgba\(66,185,131|#f56c6c|#e6a23c' -AllMatches"
```

Expected: no green/yellow/red active action references remain.

- [ ] **Step 5: Commit article changes**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app/pages/article/article.vue'; git commit -m 'style(app): neutralize article detail reading UI'"
```

Expected: commit succeeds.

## Task 4: Category and Notice Neutral Information Pages

**Files:**
- Modify: `blog-front/blog-app/pages/category/category.vue`
- Modify: `blog-front/blog-app/pages/notice/notice.vue`

- [ ] **Step 1: Replace category card gradients**

In `pages/category/category.vue`, replace the `.bg-*` classes:

```scss
.cat-card {
  background: var(--bg-card);
  color: var(--text-primary);
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.cat-card::after {
  content: '';
  position: absolute;
  right: -36rpx;
  bottom: -42rpx;
  width: 130rpx;
  height: 130rpx;
  border-radius: 50%;
  background: rgba(58, 58, 58, 0.05);
}

.bg-0,
.bg-1,
.bg-2,
.bg-3,
.bg-4 {
  background: var(--bg-card);
  color: var(--text-primary);
}
```

- [ ] **Step 2: Replace category tags**

Use one neutral tag style:

```scss
.tag-chip {
  background: var(--bg-card);
  color: var(--text-regular);
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.tag-color-0,
.tag-color-1,
.tag-color-2,
.tag-color-3 {
  color: var(--text-regular);
}
```

- [ ] **Step 3: Replace notice type colors and links**

In `pages/notice/notice.vue`, update:

```js
const noticeTagStyle = {
  p: 'margin:0;font-size:26rpx;line-height:1.5;color:var(--text-regular);',
  img: 'display:inline-block;height:36rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#3a3a3a;'
}

const noticeReplyTagStyle = {
  p: 'margin:0;font-size:24rpx;line-height:1.5;color:var(--text-secondary);',
  img: 'display:inline-block;height:32rpx;width:auto;vertical-align:text-bottom;margin:0 2rpx;',
  a: 'color:#3a3a3a;'
}

function colorOfType(t) {
  if (t === 'like') return '#555552'
  if (t === 'system') return '#777773'
  return '#3a3a3a'
}
```

- [ ] **Step 4: Update notice action and unread styles**

Use these selectors:

```scss
.all-read-btn {
  background: var(--color-primary-soft);
  border: 1rpx solid var(--border-color);
}

.all-read-btn:active {
  background: #deded8;
}

.all-read-text {
  color: var(--color-primary);
}

.status-dot.online {
  background: var(--color-primary);
  box-shadow: 0 0 8rpx rgba(58, 58, 58, 0.45);
}

.filter-tab.active {
  background: var(--color-primary);
  color: #ffffff;
}

.notice-item {
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-sm);
}

.notice-item.unread {
  border-color: #d8d8d2;
  background: #fbfbfa;
}

.icon-wrap {
  background: var(--bg-soft);
}

.type-like,
.type-system,
.type-reply {
  background: var(--bg-soft);
}

.unread-dot {
  background: var(--color-primary);
}
```

- [ ] **Step 5: Verify category and notice references**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'pages\category\category.vue','pages\notice\notice.vue' -Pattern '#42b983|#339268|#5e72e4|#f56c6c|#e6a23c|#8b5cf6|#409eff|rgba\(66, 185, 131|rgba\(66,185,131' -AllMatches"
```

Expected: no saturated UI color references remain.

- [ ] **Step 6: Commit category and notice changes**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app/pages/category/category.vue' 'blog-front/blog-app/pages/notice/notice.vue'; git commit -m 'style(app): neutralize category and notice pages'"
```

Expected: commit succeeds.

## Task 5: Profile and Login Soft Monochrome Entry Pages

**Files:**
- Modify: `blog-front/blog-app/pages/profile/profile.vue`
- Modify: `blog-front/blog-app/pages/login/login.vue`

- [ ] **Step 1: Replace profile template icon colors**

Use neutral colors in the profile template:

```vue
<bx-icon name="edit" :size="28" color="#ffffff" />
<bx-icon name="user" :size="64" color="var(--color-primary)" />
```

For menu icons:

```vue
<bx-icon name="bookmark" :size="32" color="var(--color-primary)" />
<bx-icon name="heart" :size="32" color="var(--color-primary)" />
<bx-icon name="comment" :size="32" color="var(--color-primary)" />
<bx-icon name="smile" :size="32" color="var(--color-primary)" />
<bx-icon name="messageSquare" :size="32" color="var(--color-primary)" />
<bx-icon name="link" :size="32" color="var(--color-primary)" />
<bx-icon name="settings" :size="32" color="var(--color-primary)" />
<bx-icon name="info" :size="32" color="var(--color-primary)" />
```

- [ ] **Step 2: Replace profile hero and menu styles**

Update profile SCSS:

```scss
.hero {
  background: linear-gradient(135deg, #4a4a4a 0%, #252525 100%);
}

.hero-blob {
  display: none;
}

.avatar {
  border: 4rpx solid rgba(255, 255, 255, 0.72);
}

.not-logged .login-btn {
  color: #252525;
}

.stats-row,
.menu-card {
  border: 1rpx solid var(--border-color);
}

.menu-icon {
  background: var(--bg-soft);
}

.icon-bg-pink,
.icon-bg-blue,
.icon-bg-yellow,
.icon-bg-gray,
.icon-bg-purple,
.icon-bg-green,
.icon-bg-orange {
  background: var(--bg-soft);
}
```

- [ ] **Step 3: Replace login hero and focus styles**

Update login SCSS:

```scss
.hero {
  background: linear-gradient(135deg, #4a4a4a 0%, #252525 62%, #181818 100%);
}

.blob {
  opacity: 0.18;
}

.logo-mark {
  color: #252525;
}

.form-card {
  background: var(--bg-card);
  border: 1rpx solid var(--border-color);
  box-shadow: var(--shadow-lg);
}

.form-title {
  color: var(--text-primary);
}

.form-subtitle {
  color: var(--text-secondary);
}

.form-field {
  background: var(--bg-soft);
  border: 2rpx solid transparent;
}

.form-field.active {
  background: #ffffff;
  border-color: #c8c8c2;
  box-shadow: 0 0 0 4rpx rgba(58, 58, 58, 0.05);
}

.submit-btn {
  background: var(--color-primary);
  box-shadow: var(--shadow-primary);
}

.submit-btn:active {
  background: #252525;
}

.oauth-btn {
  border: 1rpx solid var(--border-color);
  color: var(--text-primary);
}

.footer-link,
.link {
  color: var(--color-primary);
}
```

- [ ] **Step 4: Verify profile/login references**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Select-String -Path 'pages\profile\profile.vue','pages\login\login.vue' -Pattern '#42b983|#339268|#2d8362|#1f6d52|#f56c6c|#e6a23c|#409eff|#8b5cf6|rgba\(66, 185, 131' -AllMatches"
```

Expected: no green or saturated menu/login references remain.

- [ ] **Step 5: Commit profile and login changes**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app/pages/profile/profile.vue' 'blog-front/blog-app/pages/login/login.vue'; git commit -m 'style(app): soften profile and login pages'"
```

Expected: commit succeeds.

## Task 6: Cross-Page Verification and Cleanup

**Files:**
- Inspect: `blog-front/blog-app/package.json`
- Inspect: `blog-front/blog-app/pages.json`
- Inspect: `blog-front/blog-app/**/*.vue`

- [ ] **Step 1: Check remaining saturated color references in source pages**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Get-ChildItem -Recurse -File -Include *.vue,*.scss,*.js -Exclude node_modules,unpackage,uni_modules | Select-String -Pattern '#42b983|#339268|#2d8362|#1f6d52|#5e72e4|#f56c6c|#e6a23c|#409eff|#8b5cf6|rgba\(66, 185, 131|rgba\(66,185,131'"
```

Expected: remaining matches are either in non-core secondary pages scheduled for later pass, user content parsing code that cannot use CSS vars, or comments. Core files from Tasks 1-5 should not contain those references.

- [ ] **Step 2: Confirm generated output was not edited**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git status --short -- 'blog-front/blog-app/unpackage'"
```

Expected: no output.

- [ ] **Step 3: Check package scripts**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken\blog-front\blog-app'; Get-Content -LiteralPath 'package.json'"
```

Expected: current `package.json` has no runnable build/test scripts beyond dependencies. If scripts exist, run the build script shown there.

- [ ] **Step 4: Run git diff review**

Run:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git diff --stat HEAD~5..HEAD; git status --short"
```

Expected: commits include only docs and source UI files; working tree is clean or only contains intentionally untracked `.superpowers/brainstorm` files.

- [ ] **Step 5: Final commit for cleanup if needed**

If Step 1 finds small missed core-page style references, update only those references and commit:

```powershell
rtk proxy powershell -NoProfile -Command "Set-Location -LiteralPath 'D:\桌面\blog-master\blog-satoken'; git add -- 'blog-front/blog-app'; git commit -m 'style(app): clean up soft monochrome UI references'"
```

Expected: commit succeeds only if cleanup changes were made. If no cleanup changes were needed, skip this step.
