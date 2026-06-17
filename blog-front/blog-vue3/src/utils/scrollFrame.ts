/**
 * Functionality: provide requestAnimationFrame-based scheduling for scroll work.
 * Author: ChenFY
 * Created: 2026-06-17
 * Purpose: coalesce high-frequency scroll updates and animation loops to frame cadence.
 */
export interface FrameSchedulerApi {
  requestAnimationFrame: (callback: FrameRequestCallback) => number
  cancelAnimationFrame: (frameId: number) => void
}

export interface ScrollFrameScheduler {
  requestUpdate: () => void
  runNow: () => void
  cancel: () => void
}

export interface AnimationLoopController {
  start: () => void
  stop: () => void
}

function getDefaultFrameApi(): FrameSchedulerApi {
  return {
    requestAnimationFrame: window.requestAnimationFrame.bind(window),
    cancelAnimationFrame: window.cancelAnimationFrame.bind(window)
  }
}

export function createScrollFrameScheduler(
  onFrame: () => void,
  frameApi: FrameSchedulerApi = getDefaultFrameApi()
): ScrollFrameScheduler {
  let pendingFrameId: number | null = null

  function cancel() {
    if (pendingFrameId === null) return

    frameApi.cancelAnimationFrame(pendingFrameId)
    pendingFrameId = null
  }

  function runNow() {
    cancel()
    onFrame()
  }

  function requestUpdate() {
    if (pendingFrameId !== null) return

    pendingFrameId = frameApi.requestAnimationFrame(() => {
      pendingFrameId = null
      onFrame()
    })
  }

  return {
    requestUpdate,
    runNow,
    cancel
  }
}

export function createAnimationLoop(
  onFrame: () => void,
  frameApi: FrameSchedulerApi = getDefaultFrameApi()
): AnimationLoopController {
  let frameId: number | null = null

  function stop() {
    if (frameId === null) return

    frameApi.cancelAnimationFrame(frameId)
    frameId = null
  }

  function tick() {
    frameId = null
    onFrame()
    frameId = frameApi.requestAnimationFrame(tick)
  }

  function start() {
    if (frameId !== null) return

    frameId = frameApi.requestAnimationFrame(tick)
  }

  return {
    start,
    stop
  }
}
