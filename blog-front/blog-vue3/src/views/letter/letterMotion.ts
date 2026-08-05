export interface LetterPresentationTiming {
  openingDelay: number
  readingDelay: number
}

export function resolveLetterPresentationTiming(
  reducedMotion: boolean,
  arrivedFromLove: boolean
): LetterPresentationTiming {
  if (reducedMotion) {
    return { openingDelay: 0, readingDelay: 0 }
  }
  return arrivedFromLove
    ? { openingDelay: 680, readingDelay: 1760 }
    : { openingDelay: 360, readingDelay: 1320 }
}
