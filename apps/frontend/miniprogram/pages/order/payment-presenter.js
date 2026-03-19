const PAYMENT_PASSWORD_KEYS = [
  [
    { key: '1', label: '1', type: 'digit' },
    { key: '2', label: '2', type: 'digit' },
    { key: '3', label: '3', type: 'digit' }
  ],
  [
    { key: '4', label: '4', type: 'digit' },
    { key: '5', label: '5', type: 'digit' },
    { key: '6', label: '6', type: 'digit' }
  ],
  [
    { key: '7', label: '7', type: 'digit' },
    { key: '8', label: '8', type: 'digit' },
    { key: '9', label: '9', type: 'digit' }
  ],
  [
    { key: 'face', label: 'Face ID', type: 'action' },
    { key: '0', label: '0', type: 'digit' },
    { key: 'delete', label: '删除', type: 'action' }
  ]
]

function applyPasswordKey(currentPassword, key) {
  const password = String(currentPassword || '')
  if (key === 'delete' || key === '删除') {
    return password.slice(0, -1)
  }

  if (!/^\d$/.test(String(key))) {
    return password
  }

  if (password.length >= 6) {
    return password
  }

  return `${password}${key}`
}

function getPaymentMethodLabel(methodKey) {
  switch (methodKey) {
    case 'wechat':
      return '微信支付'
    case 'alipay':
      return '支付宝'
    case 'bank':
      return '银行卡'
    case 'credit':
      return '信用卡'
    default:
      return '支付方式'
  }
}

function buildVerificationMethodText(methodKey, verificationStage) {
  const methodLabel = getPaymentMethodLabel(methodKey)
  return `${methodLabel} · ${verificationStage === 'password' ? '支付密码验证' : 'Face ID 验证'}`
}

module.exports = {
  PAYMENT_PASSWORD_KEYS,
  applyPasswordKey,
  getPaymentMethodLabel,
  buildVerificationMethodText
}
