import DOMPurify from 'dompurify'

/**
 * Sanitize HTML received from users or the API before rendering it.
 * DOMPurify also handles malformed markup and removes scriptable URLs.
 */
export function sanitizeHtml(value) {
  return DOMPurify.sanitize(value == null ? '' : String(value))
}

export default sanitizeHtml
