# Home Menu Layouts Design

## Goal

Add a hover dropdown under the top navigation "Home" item. The dropdown lets visitors switch between four homepage variants:

- Blog: the current homepage at `/`.
- Start: a lightweight landing-style homepage at `/home/start`.
- Content: a content aggregation homepage at `/home/content`.
- Columns: a column/topic homepage at `/home/columns`.

The desktop dropdown should follow the provided reference: a dark translucent panel with stacked rounded light menu buttons. Mobile navigation must expose the same four entries inside the side drawer.

## Architecture

Use independent Vue Router routes for each homepage variant:

- Keep the existing `Home.vue` route for `/`.
- Add three new route records under `/home/start`, `/home/content`, and `/home/columns`.
- Add three new view components under `src/views/home/`.

This keeps each homepage isolated and allows direct links, refreshes, and future expansion without making `Home.vue` handle unrelated layouts.

## Components

Update `TopNavBar.vue`:

- Replace the current single "Home" router link with a hover dropdown.
- Include four router links: Blog, Start, Content, Columns.
- Style this dropdown separately from existing generic nav submenus so it can match the reference image.

Update `SideNavBar.vue`:

- Replace or expand the current Home entry with the same four homepage links.
- Keep the mobile interaction simple: stacked drawer links, no nested hover requirement.

Add homepage variant views:

- `HomeStart.vue`: concise welcome/entry page using site name, intro, and quick navigation links.
- `HomeContent.vue`: article/content-focused page with sections for latest articles, categories/tags, and quick content entry points.
- `HomeColumns.vue`: topic/column-focused page using category/tag style cards and links.

The new pages should reuse existing store/API helpers where useful and provide graceful empty states if backend data is unavailable.

## Data Flow

- The existing app-level `getBlogInfo()` remains the source for website config, counts, and page cover data.
- New homepage views can read `useBlogInfoStore()` directly.
- If content data is needed, reuse existing API wrappers from `src/api/article.ts` and `src/api/misc.ts` instead of creating duplicate request logic.

## Error Handling

- Navigation links should work even if blog info has not loaded yet.
- New views should avoid throwing on missing `websiteConfig`, `pageList`, counts, categories, tags, or article lists.
- API-backed sections should show an empty state rather than blocking the whole page.

## Testing

Verify:

- Desktop hover over "Home" opens the new dropdown.
- Each dropdown item routes to the expected path.
- Mobile drawer exposes the same four homepage links.
- Current `/` homepage behavior remains unchanged.
- `npm run build` passes.
