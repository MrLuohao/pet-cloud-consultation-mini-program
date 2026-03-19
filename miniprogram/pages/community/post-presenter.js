function cleanText(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function pickFirstText(...values) {
  for (const value of values) {
    const text = cleanText(value)
    if (text) return text
  }
  return ''
}

function joinWithDot(...values) {
  return values
    .map(cleanText)
    .filter(Boolean)
    .join(' · ')
}

function parseDate(value) {
  const text = cleanText(value)
  if (!text) return null

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value
  }

  const normalized = text.includes('T') ? text : text.replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

function formatRelativeTime(value, now = new Date()) {
  const date = parseDate(value)
  const current = now instanceof Date ? now : parseDate(now)

  if (!date || !current) {
    return cleanText(value)
  }

  const diff = current.getTime() - date.getTime()
  if (diff <= 0) return '刚刚'

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)}分钟前`
  if (diff < day) return `${Math.floor(diff / hour)}小时前`
  if (diff < 30 * day) return `${Math.floor(diff / day)}天前`

  return cleanText(value).slice(0, 10)
}

function resolveRoleLabel(post) {
  const author = post && post.author ? post.author : {}
  const postType = cleanText(post && post.postType).toLowerCase()
  const role = cleanText(author.role).toLowerCase()

  if (postType === 'expert_post' || role === 'doctor' || role === 'vet' || role === 'expert') {
    return '专业答疑'
  }

  return '内容分享'
}

function buildIdentity(post, options = {}) {
  const author = post && post.author ? post.author : {}
  const pet = post && post.pet ? post.pet : {}
  const roleLabel = resolveRoleLabel(post)
  const isExpertIdentity = roleLabel === '专业答疑'
  const isPetIdentity = !!post && !!post.pet && !isExpertIdentity

  if (isPetIdentity) {
    const identityName = pickFirstText(pet.name, post.petName, post.nickname, '宠物')
    const identityMeta = joinWithDot(
      pet.breed,
      pet.ageText || post.ageText
    ) || '宠物档案待补充'
    const identitySignature = pickFirstText(
      pet.signature,
      pet.motto,
      pet.personality
    )

    return {
      isPetIdentity: true,
      isExpertIdentity: false,
      identityName,
      identityMeta,
      identitySignature,
      showIdentitySignature: !!identitySignature,
      identityRoleLabel: '',
      identityAvatarUrl: pickFirstText(pet.avatarUrl, post.avatarUrl),
      identityInitial: cleanText(identityName).slice(0, 1) || '宠'
    }
  }

  const identityName = pickFirstText(
    author.displayName,
    author.nickname,
    post.nickname,
    '内容作者'
  )

  return {
    isPetIdentity: false,
    isExpertIdentity,
    identityName,
    identityMeta: joinWithDot(formatRelativeTime(post && post.createTime, options.now), roleLabel),
    identitySignature: '',
    showIdentitySignature: false,
    identityRoleLabel: roleLabel,
    identityAvatarUrl: pickFirstText(author.avatarUrl, post && post.avatarUrl),
    identityInitial: cleanText(identityName).slice(0, 1) || '作'
  }
}

function ensureMediaUrls(post) {
  return Array.isArray(post && post.mediaUrls) ? post.mediaUrls : []
}

function formatCommunityPostCard(post, options = {}) {
  if (!post || typeof post !== 'object') return post

  return {
    ...post,
    ...buildIdentity(post, options),
    mediaUrls: ensureMediaUrls(post),
    previewContent: cleanText(post.content)
  }
}

function formatCommunityPostDetail(post, options = {}) {
  if (!post || typeof post !== 'object') return post

  return {
    ...post,
    ...buildIdentity(post, options),
    mediaUrls: ensureMediaUrls(post)
  }
}

module.exports = {
  formatCommunityPostCard,
  formatCommunityPostDetail,
  formatRelativeTime
}
