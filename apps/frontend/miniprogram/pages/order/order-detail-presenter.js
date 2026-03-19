function cleanText(value) {
  if (value === null || value === undefined) return ''
  return String(value).trim()
}

function maskPhone(value) {
  const digits = cleanText(value).replace(/\D/g, '')
  if (digits.length !== 11) return cleanText(value)
  return `${digits.slice(0, 3)} ${digits.slice(3, 7)} ${digits.slice(7)}`
}

function formatAmount(value) {
  const number = Number(value || 0)
  if (!Number.isFinite(number)) return '¥0'
  const normalized = number % 1 === 0 ? String(number) : number.toFixed(2)
  return `¥${normalized}`
}

function resolveStatusMeta(status, statusDesc, payDeadlineText) {
  const statusMap = {
    0: {
      title: statusDesc || '待支付',
      hint: '订单已提交，请在 30 分钟内完成支付。',
      chip: cleanText(payDeadlineText) || '剩余 29 分 48 秒',
      progressText: '超时未支付，订单将自动关闭',
      variant: 'pending'
    },
    1: {
      title: statusDesc || '待发货',
      hint: '商家正在备货，请留意后续发货通知。',
      chip: '准备发货',
      progressText: '订单处理中，请留意发货通知。',
      variant: 'processing'
    },
    2: {
      title: statusDesc || '待收货',
      hint: '包裹运输中，请在收到后及时确认收货。',
      chip: '配送中',
      progressText: '包裹运输中，请留意物流更新。',
      variant: 'shipping'
    },
    3: {
      title: statusDesc || '已完成',
      hint: '订单已完成，如有需要可继续评价或查看售后服务。',
      chip: '已完成',
      progressText: '交易已完成，订单信息已归档保存。',
      variant: 'completed'
    },
    4: {
      title: statusDesc || '已取消',
      hint: '订单已取消，如仍有需要可重新下单。',
      chip: '订单关闭',
      progressText: '订单已取消，可重新选择商品下单。',
      variant: 'closed'
    }
  }

  return statusMap[status] || {
    title: statusDesc || '订单状态更新中',
    hint: '订单状态同步中，请稍后刷新查看。',
    chip: '状态同步中',
    progressText: '订单状态同步中，请稍后刷新查看。',
    variant: 'syncing'
  }
}

function resolveActions(order) {
  const status = Number(order && order.status)
  if (status === 0) {
    return {
      primaryAction: { key: 'pay', label: '继续支付' },
      secondaryAction: { key: 'cancel', label: '取消订单' }
    }
  }

  if (status === 2) {
    return {
      primaryAction: { key: 'receive', label: '确认收货' },
      secondaryAction: null
    }
  }

  if (status === 3) {
    const hasUnreviewedItems = Array.isArray(order.items) && order.items.some(item => !item.reviewed)
    return {
      primaryAction: hasUnreviewedItems ? { key: 'review', label: '去评价' } : null,
      secondaryAction: { key: 'after_sale', label: '申请售后' }
    }
  }

  return {
    primaryAction: null,
    secondaryAction: null
  }
}

function buildInfoRows(order) {
  const status = Number(order && order.status)
  if (status === 3) {
    return [
      { label: '订单编号', value: cleanText(order.orderNo) },
      { label: '创建时间', value: cleanText(order.createTime) },
      { label: '支付时间', value: cleanText(order.payTime) },
      { label: '完成状态', value: cleanText(order.afterSaleStatusText || '已完成 · 暂未申请售后') }
    ].filter(item => item.value)
  }

  const rows = [
    { label: '订单编号', value: cleanText(order.orderNo) },
    { label: '创建时间', value: cleanText(order.createTime) },
    { label: '支付方式', value: cleanText(order.paymentMethodDesc || '微信支付') }
  ]

  if (cleanText(order.remark)) {
    rows.push({ label: '订单备注', value: cleanText(order.remark) })
  }

  return rows.filter(item => item.value)
}

function formatItems(items) {
  return Array.isArray(items)
    ? items.map(item => ({
        ...item,
        title: cleanText(item.productName || item.name || '商品'),
        meta: cleanText(item.specLabel || item.spec || item.productSpec || '默认规格'),
        priceText: formatAmount(item.price),
        quantityText: `x${item.quantity || 1}`
      }))
    : []
}

function formatOrderDetail(order) {
  const safeOrder = order && typeof order === 'object' ? order : {}
  const statusMeta = resolveStatusMeta(safeOrder.status, safeOrder.statusDesc, safeOrder.payDeadlineText)
  const actions = resolveActions(safeOrder)

  return {
    statusTitle: statusMeta.title,
    statusHint: statusMeta.hint,
    statusChip: statusMeta.chip,
    statusVariant: statusMeta.variant,
    statusProgressText: statusMeta.progressText,
    receiverLine: `${cleanText(safeOrder.receiverName)}  ·  ${maskPhone(safeOrder.receiverPhone)}`.trim(),
    receiverAddress: cleanText(safeOrder.receiverAddress),
    products: formatItems(safeOrder.items),
    amountTitle: Number(safeOrder.status) === 3 ? '实付信息' : '金额明细',
    amountRows: [
      { label: '商品总额', value: formatAmount(safeOrder.totalAmount) },
      { label: '运费', value: formatAmount(safeOrder.freight) },
      { label: '优惠', value: `-${formatAmount(safeOrder.couponDiscount).replace('¥', '¥')}` },
      { label: Number(safeOrder.status) === 3 ? '实付金额' : '实付款', value: formatAmount(safeOrder.payAmount), emphasis: true }
    ],
    infoRows: buildInfoRows(safeOrder),
    showTimeline: Number(safeOrder.status) !== 3,
    primaryAction: actions.primaryAction,
    secondaryAction: actions.secondaryAction
  }
}

module.exports = {
  formatOrderDetail
}
