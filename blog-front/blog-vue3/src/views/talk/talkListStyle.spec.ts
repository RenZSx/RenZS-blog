import { describe, expect, it } from 'vitest'
import { getTalkItemClass } from './talkListStyle'

describe('talkListStyle', () => {
  it('marks only non-last talk items with a divider', () => {
    expect(getTalkItemClass(0, 3)).toEqual({
      'talk-item': true,
      'talk-item-with-divider': true
    })

    expect(getTalkItemClass(1, 3)).toEqual({
      'talk-item': true,
      'talk-item-with-divider': true
    })

    expect(getTalkItemClass(2, 3)).toEqual({
      'talk-item': true,
      'talk-item-with-divider': false
    })
  })
})
