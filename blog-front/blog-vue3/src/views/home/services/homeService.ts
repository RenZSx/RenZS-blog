import { getHomeArticleSections, getCategories, getNewComments, getHomeTalks } from '@/api/misc'

export function fetchHomeArticleSections() {
  return getHomeArticleSections()
}

export function fetchHomeComments() {
  return getNewComments()
}

export function fetchHomeCategories() {
  return getCategories()
}

export function fetchHomeTalks() {
  return getHomeTalks()
}
