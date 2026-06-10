export interface TopNavBaseClassOptions {
  isDark: boolean
  isOverlayRoute: boolean
  scrollTop: number
}

export function getTopNavBaseClass(options: TopNavBaseClassOptions) {
  const shouldUseOverlayNav = options.isOverlayRoute && options.scrollTop <= 60

  if (shouldUseOverlayNav) {
    return 'nav'
  }

  return options.isDark ? 'nav-fixed nav-fixed-dark' : 'nav-fixed nav-fixed-light'
}
