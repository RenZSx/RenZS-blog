export function syncPageTheme(isDark: boolean) {
  document.documentElement.classList.toggle('dark', isDark)

  const background =
    getComputedStyle(document.documentElement).getPropertyValue('--bg-primary').trim() || '#ffffff'

  document.documentElement.style.backgroundColor = background
  document.body.style.backgroundColor = background
}
