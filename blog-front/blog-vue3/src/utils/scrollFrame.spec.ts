import { describe, expect, it } from 'vitest'
import {
  createAnimationLoop,
  createScrollFrameScheduler,
  type FrameSchedulerApi
} from './scrollFrame'

function createFakeFrameApi() {
  let nextFrameId = 1
  const callbacks = new Map<number, FrameRequestCallback>()

  const api: FrameSchedulerApi = {
    requestAnimationFrame(callback) {
      const frameId = nextFrameId
      nextFrameId += 1
      callbacks.set(frameId, callback)
      return frameId
    },
    cancelAnimationFrame(frameId) {
      callbacks.delete(frameId)
    }
  }

  return {
    api,
    pendingCount() {
      return callbacks.size
    },
    flushNext(timestamp = 16) {
      const nextFrame = callbacks.entries().next().value as
        | [number, FrameRequestCallback]
        | undefined

      if (!nextFrame) return

      const [frameId, callback] = nextFrame

      callbacks.delete(frameId)
      callback(timestamp)
    }
  }
}

describe('scrollFrame', () => {
  it('coalesces repeated scroll updates into one animation frame', () => {
    const frameApi = createFakeFrameApi()
    const calls: number[] = []
    const scheduler = createScrollFrameScheduler(() => {
      calls.push(1)
    }, frameApi.api)

    scheduler.requestUpdate()
    scheduler.requestUpdate()
    scheduler.requestUpdate()

    expect(frameApi.pendingCount()).toBe(1)

    frameApi.flushNext()

    expect(calls).toHaveLength(1)
    expect(frameApi.pendingCount()).toBe(0)
  })

  it('cancels a pending scroll update before the frame runs', () => {
    const frameApi = createFakeFrameApi()
    const calls: number[] = []
    const scheduler = createScrollFrameScheduler(() => {
      calls.push(1)
    }, frameApi.api)

    scheduler.requestUpdate()
    scheduler.cancel()
    frameApi.flushNext()

    expect(calls).toHaveLength(0)
    expect(frameApi.pendingCount()).toBe(0)
  })

  it('runs an animation loop on frame cadence and stops cleanly', () => {
    const frameApi = createFakeFrameApi()
    let frameCount = 0
    const loop = createAnimationLoop(() => {
      frameCount += 1
    }, frameApi.api)

    loop.start()
    loop.start()

    expect(frameApi.pendingCount()).toBe(1)

    frameApi.flushNext()

    expect(frameCount).toBe(1)
    expect(frameApi.pendingCount()).toBe(1)

    loop.stop()

    expect(frameApi.pendingCount()).toBe(0)
  })
})
