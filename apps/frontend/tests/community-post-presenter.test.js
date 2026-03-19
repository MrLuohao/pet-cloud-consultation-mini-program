const assert = require('assert')

const {
  formatCommunityPostCard,
  formatCommunityPostDetail
} = require('../miniprogram/pages/community/post-presenter')

function testPetPostCardMatchesPenIdentityStack() {
  const post = formatCommunityPostCard({
    id: 9001,
    content: '今天终于把饮食和作息慢慢调顺了。',
    createTime: '2026-03-15 09:30:00',
    pet: {
      name: '团子',
      breed: '英短蓝猫',
      ageText: '2岁3个月',
      signature: '慢热小猫，喜欢晒太阳和安静陪伴。',
      avatarUrl: 'https://example.com/tuanzi.png'
    }
  })

  assert.strictEqual(post.identityName, '团子')
  assert.strictEqual(post.identityMeta, '英短蓝猫 · 2岁3个月')
  assert.strictEqual(post.identitySignature, '慢热小猫，喜欢晒太阳和安静陪伴。')
  assert.strictEqual(post.showIdentitySignature, true)
  assert.strictEqual(post.identityAvatarUrl, 'https://example.com/tuanzi.png')
}

function testExpertPostUsesTwoLineProfessionalIdentity() {
  const post = formatCommunityPostCard({
    id: 9002,
    content: '猫咪突然不爱喝水的时候，先观察精神状态。',
    createTime: '2026-03-15T11:38:00',
    postType: 'expert_post',
    author: {
      displayName: '宠物医生周周',
      role: 'doctor',
      avatarUrl: 'https://example.com/doctor.png'
    }
  }, {
    now: new Date('2026-03-15T12:00:00')
  })

  assert.strictEqual(post.identityName, '宠物医生周周')
  assert.strictEqual(post.identityMeta, '22分钟前 · 专业答疑')
  assert.strictEqual(post.identitySignature, '')
  assert.strictEqual(post.showIdentitySignature, false)
  assert.strictEqual(post.identityRoleLabel, '专业答疑')
}

function testDetailFormatterKeepsPetIdentityHierarchy() {
  const post = formatCommunityPostDetail({
    id: 9003,
    createTime: '2026-03-15 08:00:00',
    nickname: '旧昵称',
    pet: {
      name: '奶盖',
      breed: '比熊',
      ageText: '1岁2个月',
      motto: '外向又黏人。',
      avatarUrl: 'https://example.com/naigai.png'
    }
  })

  assert.strictEqual(post.identityName, '奶盖')
  assert.strictEqual(post.identityMeta, '比熊 · 1岁2个月')
  assert.strictEqual(post.identitySignature, '外向又黏人。')
  assert.strictEqual(post.showIdentitySignature, true)
}

testPetPostCardMatchesPenIdentityStack()
testExpertPostUsesTwoLineProfessionalIdentity()
testDetailFormatterKeepsPetIdentityHierarchy()

console.log('community-post-presenter tests passed')
