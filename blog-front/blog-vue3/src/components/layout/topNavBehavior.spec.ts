import { describe, expect, it } from 'vitest'
import { getTopNavBaseClass } from './topNavBehavior'

describe('topNavBehavior', () => {
  it('uses fixed navigation at the top of normal pages', () => {
    expect(
      getTopNavBaseClass({
        isDark: false,
        isOverlayRoute: false,
        scrollTop: 0
      })
    ).toBe('nav-fixed nav-fixed-light')
  })

  it('allows transparent navigation only for overlay routes near the top', () => {
    expect(
      getTopNavBaseClass({
        isDark: false,
        isOverlayRoute: true,
        scrollTop: 0
      })
    ).toBe('nav')
  })

  it('switches overlay routes to fixed navigation after scrolling', () => {
    expect(
      getTopNavBaseClass({
        isDark: true,
        isOverlayRoute: true,
        scrollTop: 80
      })
    ).toBe('nav-fixed nav-fixed-dark')
  })
})
