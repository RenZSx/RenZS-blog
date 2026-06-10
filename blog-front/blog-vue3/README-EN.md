# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Renzs Blog Vue3 Frontend — a personal blog web application built with Vue 3 (Composition API + `<script setup>`), TypeScript, Vuetify, and Element Plus. It communicates with a Spring Boot backend via Axios (proxied to `8.137.86.224:8088`) and WebSocket for the chat room.

## Commands

```bash
npm run dev      # Start Vite dev server on port 3000
npm run build    # TypeScript check + production build
npm run preview  # Preview production build
npm run lint     # ESLint with auto-fix
npm run test     # Vitest unit tests (run mode)
```

## Architecture

### Directory Structure

```
src/
├── api/           # Axios HTTP layer — each domain (article, user, comment, etc.) has its own file
├── assets/css/    # Global styles: tokens.css (CSS variables), index.css (reset/utilities), markdown.css (article styles)
├── components/    # Reusable components
│   ├── layout/    # TopNavBar, SideNavBar, Footer — rendered in App.vue
│   ├── model/     # Modal dialogs (Search, Login, Register, Email, Forget)
│   └── comment/   # Comment system (editor, item, list, reply, service)
├── composables/   # useToast.ts — toast notifications via a separate Vue app on document.body
├── plugins/       # vuetify.ts — Vuetify setup with MDI icons and theme definitions
├── router/        # 20 lazy-loaded routes with NProgress, scroll restoration, auth layout flag
├── stores/        # Pinia stores: useUserStore, useUIStore, useBlogInfoStore
├── types/         # TypeScript type declarations
├── utils/         # filters.ts (dayjs formatting), markdown.ts (markdown-it pipeline)
└── views/         # Page components (Home, Article, Archive, Album, Talk, Chat, etc.)
```

### API Layer

`src/api/request.ts` is the Axios instance — all API files import from it. It attaches Bearer tokens, handles errors, and is configured via the Vite proxy (`/api` → backend). API modules: `site.ts`, `user.ts`, `article.ts`, `comment.ts`, `talk.ts`, `misc.ts`.

### Stores

- **useUserStore** — Auth state, user info, like sets (articles/comments/talks). **Persisted to localStorage.**
- **useUIStore** — Modal visibility flags (search, login, register, email, drawer).
- **useBlogInfoStore** — Site config, page list, counts. **Persisted to localStorage.**

### Markdown Rendering

`src/utils/markdown.ts` exports a configured `markdown-it` instance with: syntax highlighting (highlight.js), line numbers, copy buttons, plus extensions for sub/sup/mark/abbr/emoji/footnote/katex/task lists. Used in article detail pages.

### Auth Layout Pattern

Routes with `meta.layout: 'auth'` (Login, Register, Forgot Password) hide the nav, sidebar, and footer via `App.vue`'s layout logic.

### Auto-Imports

`unplugin-auto-import` automatically imports all Vue/Pinia/router APIs and utility functions. `unplugin-vue-components` auto-imports all `.vue` components. Stubs are in `src/auto-imports.d.ts` and `src/components.d.ts`.

### Build Code Splitting

Vite is configured to split chunks for: `vuetify`, `markdown-core`, `markdown-highlight`, `markdown-katex`, `tocbot`, `markdown-plugins`, `swiper`.

## Key Patterns

- Toast notifications use a dynamically mounted Vue app on `document.body` (not inside the main app tree) — use `useToast()` composable.
- Markdown article pages use `tocbot` for table-of-contents generation.
- The chat room component is conditionally rendered based on `blogInfoStore.blogInfo.websiteConfig?.isChatRoom` and the current route.
- Dark/light theme toggles via Vuetify theme system and syncs page background via a `.dark` CSS class on `document.body`.
