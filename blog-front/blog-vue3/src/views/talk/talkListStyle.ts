/**
 * Functionality: provide stable class names for the talk list layout.
 * Author: ChenFY
 * Created: 2026-06-17
 * Purpose: keep talk item divider behavior consistent between template and tests.
 */
export function getTalkItemClass(index: number, total: number) {
  return {
    'talk-item': true,
    'talk-item-with-divider': index < total - 1
  }
}
