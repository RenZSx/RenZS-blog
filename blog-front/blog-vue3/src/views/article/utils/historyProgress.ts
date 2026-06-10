export interface ComputeHistoryProgressInput {
  articleTop: number
  articleHeight: number
  scrollY: number
  viewportHeight: number
}

export interface ShouldReportHistoryProgressInput {
  progressPercent: number
  lastReportedProgressPercent: number
  now: number
  lastReportedAt: number
  progressThresholdPercent: number
  reportIntervalMs: number
  force?: boolean
}

export interface ResolveHistoryProgressReportActionInput
  extends ShouldReportHistoryProgressInput {
  lastReservedProgressPercent: number
  lastReservedAt: number
  isReporting: boolean
}

export type HistoryProgressReportAction = 'skip' | 'dispatch' | 'queue'

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value))
}

export function computeHistoryProgress(input: ComputeHistoryProgressInput) {
  const { articleTop, articleHeight, scrollY, viewportHeight } = input

  if (!Number.isFinite(articleHeight) || articleHeight <= 0) {
    return 0
  }

  const viewportBottom = scrollY + viewportHeight
  const articleBottom = articleTop + articleHeight

  if (viewportBottom <= articleTop) {
    return 0
  }

  if (viewportBottom >= articleBottom) {
    return 100
  }

  const rawProgress = ((viewportBottom - articleTop) / articleHeight) * 100
  return clamp(Math.round(rawProgress), 0, 100)
}

export function shouldReportHistoryProgress(input: ShouldReportHistoryProgressInput) {
  const {
    progressPercent,
    lastReportedProgressPercent,
    now,
    lastReportedAt,
    progressThresholdPercent,
    reportIntervalMs,
    force = false
  } = input

  if (progressPercent <= 0) {
    return false
  }

  if (force) {
    return true
  }

  const progressDelta = progressPercent - lastReportedProgressPercent
  if (progressDelta < progressThresholdPercent) {
    return false
  }

  if (lastReportedAt <= 0) {
    return true
  }

  return now - lastReportedAt >= reportIntervalMs
}

export function resolveHistoryProgressReportAction(
  input: ResolveHistoryProgressReportActionInput
): HistoryProgressReportAction {
  const {
    progressPercent,
    force = false,
    lastReservedProgressPercent,
    lastReservedAt,
    isReporting
  } = input

  if (progressPercent <= 0) {
    return 'skip'
  }

  if (isReporting) {
    if (force) {
      return 'queue'
    }
    return 'skip'
  }

  if (force) {
    return 'dispatch'
  }

  return shouldReportHistoryProgress({
    ...input,
    lastReportedProgressPercent: lastReservedProgressPercent,
    lastReportedAt: lastReservedAt,
    force: false
  })
    ? 'dispatch'
    : 'skip'
}
