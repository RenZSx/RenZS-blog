import type { ThemeInstance } from 'vuetify'

type ViewTransitionDocument = Document & {
  startViewTransition?: (callback: () => void) => {
    ready: Promise<void>
  }
}

interface ToggleThemeOptions {
  x?: number
  y?: number
  lightweight?: boolean
}

function applyTheme(theme: ThemeInstance) {
  const isDark = theme.global.current.value.dark
  theme.change(isDark ? 'light' : 'dark')
  return !isDark
}

export function toggleTheme(theme: ThemeInstance, options: ToggleThemeOptions = {}) {
  const viewTransitionDocument = document as ViewTransitionDocument

  if (options.lightweight || !viewTransitionDocument.startViewTransition) {
    return applyTheme(theme)
  }

  const isDark = theme.global.current.value.dark
  const x = options.x ?? window.innerWidth / 2
  const y = options.y ?? 0
  const endRadius = Math.hypot(
    Math.max(x, window.innerWidth - x),
    Math.max(y, window.innerHeight - y)
  )

  const transition = viewTransitionDocument.startViewTransition(() => {
    applyTheme(theme)
  })

  transition.ready.then(() => {
    document.documentElement.animate(
      {
        clipPath: [
          `circle(0px at ${x}px ${y}px)`,
          `circle(${endRadius}px at ${x}px ${y}px)`
        ]
      },
      {
        duration: 620,
        easing: 'cubic-bezier(0.22, 1, 0.36, 1)',
        pseudoElement: '::view-transition-new(root)'
      }
    )
  })

  return !isDark
}
