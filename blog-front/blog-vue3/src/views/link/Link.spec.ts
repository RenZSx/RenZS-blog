/**
 * Function: Friend-link page layout and application flow structure tests.
 * Author: OpenAI Codex
 * Created: 2026-07-05
 * Purpose: Prevent regressions to comment-based applications, plain inline forms, or stale fields.
 */

import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'

/**
 * Reads the Link.vue single-file component source.
 *
 * @returns Link.vue UTF-8 source text.
 */
function readLinkViewSource() {
  // Resolve from import.meta.url so the test is independent of the working directory.
  const linkViewPath = fileURLToPath(new URL('./Link.vue', import.meta.url))
  return readFileSync(linkViewPath, 'utf8')
}

describe('Link.vue layout', () => {
  it('renders add-link information before friend-link cards', () => {
    // Source order follows template render order, so this locks the info section above cards.
    const source = readLinkViewSource()
    const addLinkSectionIndex = source.indexOf('添加友链')
    const friendLinksSectionIndex = source.indexOf('class="link-grid"')

    expect(addLinkSectionIndex).toBeGreaterThan(-1)
    expect(friendLinksSectionIndex).toBeGreaterThan(-1)
    expect(addLinkSectionIndex).toBeLessThan(friendLinksSectionIndex)
  })

  it('does not render global website cover as friend-link cover information', () => {
    // Friend-link covers come from friend-link records, not global website config.
    const source = readLinkViewSource()

    expect(source).not.toContain('websiteCover')
  })

  it('uses an image-top card layout for friend links', () => {
    // Friend-link cards use an image-top, text-bottom structure.
    const source = readLinkViewSource()

    expect(source).toContain('class="link-card-cover"')
    expect(source).toContain('class="link-card-body"')
  })

  it('opens a galaxy dialog for friend-link applications instead of showing comments', () => {
    // Applications are submitted from the galaxy dialog into the admin review flow.
    const source = readLinkViewSource()

    expect(source).toContain('class="link-galaxy-entry"')
    expect(source).toContain('@click="openGalaxyApply"')
    expect(source).toContain('v-model="applyGalaxyDialog"')
    expect(source).toContain('class="link-galaxy-dialog"')
    expect(source).toContain('class="link-galaxy-orbit"')
    expect(source).toContain('visibleFriendPlanets')
    expect(source).toContain('@submit.prevent="submitFriendLinkApply"')
    expect(source).toContain('sendFriendLinkApply')
    expect(source).not.toContain('<Comment')
    expect(source).not.toContain('scrollToComment')
  })

  it('uses cover fields for friend-link images instead of avatar fields', () => {
    // Friend-link application and display use cover fields, not the old avatar field.
    const source = readLinkViewSource()

    expect(source).toContain('linkCover')
    expect(source).toContain('网站封面')
    expect(source).not.toContain('linkAvatar')
    expect(source).not.toContain('网站头像')
  })
})
