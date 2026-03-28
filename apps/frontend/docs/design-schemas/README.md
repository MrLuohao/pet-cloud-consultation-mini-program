# Design Schemas

This directory stores the normalized contract between Pencil `.pen` frames and Mini Program page implementation.

## Purpose

Do not implement page UI directly from memory or by visually approximating `.pen`.

Use this workflow instead:

1. Read the target frame from Pencil MCP
2. Capture a screenshot of the frame
3. Normalize the frame into a page schema JSON
4. Implement WXML/WXSS/JS against the schema
5. Verify the result against the `.pen` screenshot

## Required MCP sequence

For every page:

1. `get_editor_state(include_schema: true)`
2. `get_guidelines("code")`
3. `get_guidelines("mobile-app")`
4. `get_variables(filePath)`
5. `batch_get(nodeIds: [frameId], readDepth: 4+, resolveVariables: true)`
6. `get_screenshot(frameId)`

## Schema files

- `page-schema.contract.json`: contract for all page schemas
- `home.page-schema.json`: homepage reference implementation
- `diagnosis.page-schema.json`: diagnosis page reference implementation
- `news.page-schema.json`: message center reference implementation
- `shop.page-schema.json`: shop page reference implementation
- `user.page-schema.json`: my page reference implementation
- `community.page-schema.json`: community page reference implementation
- `community-detail.page-schema.json`: community detail reference implementation
- `community-search.page-schema.json`: community search reference implementation
- `community-topic.page-schema.json`: community topic reference implementation
- `community-publish.page-schema.json`: community publish reference implementation
- `product-detail.page-schema.json`: product detail reference implementation
- `cart.page-schema.json`: cart reference implementation
- `cart-empty.page-schema.json`: cart empty-state reference implementation
- `checkout.page-schema.json`: checkout reference implementation
- `payment-method-modal.page-schema.json`: payment method modal reference implementation
- `pay-success.page-schema.json`: payment success reference implementation
- `payment-verification.page-schema.json`: payment verification reference implementation
- `payment-password-entry.page-schema.json`: payment password entry reference implementation
- `address-list.page-schema.json`: address list reference implementation
- `address-edit.page-schema.json`: address edit reference implementation
- `login.page-schema.json`: login reference implementation
- `order-detail.page-schema.json`: pending order detail reference implementation
- `completed-order-detail.page-schema.json`: completed order detail reference implementation
- `cancelled-order-detail.page-schema.json`: cancelled order detail reference implementation
- `cancel-order-modal.page-schema.json`: cancel order modal reference implementation
- `order-cancel-flow-hint.page-schema.json`: order cancel flow helper note reference schema
- `order-review.page-schema.json`: order review reference implementation
- `order-list.page-schema.json`: order list reference implementation
- `logistics-detail.page-schema.json`: logistics detail reference implementation
- `after-sales-apply.page-schema.json`: after-sales apply reference implementation
- `after-sales-progress.page-schema.json`: after-sales progress reference implementation
- `after-sales-complete.page-schema.json`: after-sales complete reference implementation
- `after-sales-order-detail.page-schema.json`: order detail after-sales state reference implementation
- `beauty.page-schema.json`: beauty care home reference implementation
- `beauty-booking.page-schema.json`: nearby beauty place search reference implementation
- `health-list.page-schema.json`: health record list reference implementation
- `health-edit.page-schema.json`: health record create/edit reference implementation
- `health-reminder.page-schema.json`: health reminder reference implementation
- `consultation-doctor-list.page-schema.json`: consultation doctor list reference implementation
- `consultation-doctor-detail.page-schema.json`: consultation doctor detail reference implementation
- `consultation-create.page-schema.json`: consultation create reference implementation
- `consultation-chat.page-schema.json`: consultation chat reference implementation
- `consultation-list.page-schema.json`: consultation history list reference implementation
- `consultation-urgent.page-schema.json`: urgent consultation reference implementation
- `message-conversations.page-schema.json`: message conversations reference implementation
- `message-chat.page-schema.json`: message chat reference implementation
- `all-messages.page-schema.json`: all messages center reference implementation
- `pet-list.page-schema.json`: pet gallery list reference implementation
- `pet-profile.page-schema.json`: pet health profile reference implementation
- `pet-edit.page-schema.json`: pet edit reference implementation
- `pet-timeline.page-schema.json`: pet growth archive reference implementation
- `vip-center.page-schema.json`: vip center reference implementation
- `coupon-hub.page-schema.json`: merged coupon hub reference implementation
- `settings.page-schema.json`: settings reference implementation
- `collection.page-schema.json`: collection reference implementation
- `daily-tasks.page-schema.json`: daily tasks reference implementation
- `feedback.page-schema.json`: feedback reference implementation

## Required implementation rules

- `.pen` remains the visual source of truth
- page schema is the implementation source of truth
- code must preserve existing business/API logic unless intentionally refactored
- no page may claim 1:1 parity without screenshot verification
- status-bar nodes in `.pen` are reference-only when the Mini Program uses native navigation chrome
- do not render fake `9:41 / signal / battery` blocks in WXML unless custom navigation was explicitly approved

## Schema to code mapping rules

When implementing from a page schema:

- `meta` maps to page identity and source references only, not runtime logic
- `layout` controls page wrapper padding, background, section gap, and safe area treatment
- `tokens` should drive WXSS constants and repeated visual primitives
- `sections` determines WXML block order and internal hierarchy
- `dataBindings` defines which current API fields are allowed to populate each section
- `actions` defines the only allowed user interaction hooks for the page
- `verification` defines the visual checklist that must be used before claiming parity

Implementation discipline:

- WXML must be structured in the same order as `sections`
- WXSS must follow `tokens` and section layout rules before adding any optional refinements
- JS should only translate API payloads into section-ready page state
- if the code needs a visual block not present in `sections`, add it to schema first
- if a schema contains `status_bar`, treat it as OS chrome reference by default; page code should usually start from the first business section

## Current sync notes

- when a `.pen` frame changes, update its corresponding schema in the same round before any frontend code work
- use `batch_get` as the primary schema source and `get_screenshot` only as final verification
- `yxr2Z / 订单详情页 Cancelled Order Detail` now has a matching `cancelled-order-detail.page-schema.json`
- `cKCx8 / 我的宠物列表 My Pets` now exposes dual `健康档案 / 成长档案` entries and its schema must track that split
- `health-list.page-schema.json` now maps to `2fI4W / 健康记录列表页 Health Record List`
- `health-edit.page-schema.json` now covers both `7YI5K / 新增健康记录页 Health Record Create` and `kabmm / 编辑健康记录页 Health Record Edit`
- `health-reminder.page-schema.json` now maps to `wfnjI / 健康提醒页 Health Reminder`
- `consultation-doctor-list.page-schema.json` now maps to `Y55fu / 问诊医生列表页 Consultation Doctor List`
- `consultation-doctor-detail.page-schema.json` now maps to `0akq8 / 医生详情页 Consultation Doctor Detail`
- `consultation-create.page-schema.json` now maps to `oKwYh / 发起问诊页 Consultation Create`
- `consultation-chat.page-schema.json` now maps to `ob35v / 问诊会话页 Consultation Chat`
- `consultation-list.page-schema.json` now maps to `TO5jB / 问诊列表页 Consultation List`
- `consultation-urgent.page-schema.json` now maps to `WP23x / 紧急问诊页 Urgent Consultation`
- `community-detail.page-schema.json` now maps to `WnUMs / 社区详情页 Community Detail`
- `community-search.page-schema.json` now maps to `RKyhI / 社区搜索页 Community Search`
- `community-topic.page-schema.json` now maps to `gDeKf / 社区话题页 Community Topic`
- `community-publish.page-schema.json` now maps to `GFqFg / 社区发布页 Community Publish`
- `message-conversations.page-schema.json` now maps to `dtmAQ / 私信会话页 Message Conversations`
- `message-chat.page-schema.json` now maps to `hHmNO / 私信聊天页 Message Chat`
- `all-messages.page-schema.json` now maps to `R5qEl / 全部消息页 All Messages`
- `vip-center.page-schema.json` now maps to `meeAN / 会员中心页 VIP Center`
- `coupon-hub.page-schema.json` now maps to `ROr8Y / 优惠券页 Coupon Hub`; the latest design merges the old center / my-coupon split into one page
- `settings.page-schema.json` now maps to `tPmtP / 设置页 Settings`
- `collection.page-schema.json` now maps to `gM8gn / 我的收藏页 My Collection`
- `daily-tasks.page-schema.json` now maps to `bVCp2 / 每日任务页 Daily Tasks`
- `feedback.page-schema.json` now maps to `orfEX / 意见反馈页 Feedback`
- `order-list.page-schema.json` now maps to `DiQRw / 订单列表页 Order List`
- `logistics-detail.page-schema.json` now maps to `fFNx6 / 物流详情页 Logistics Detail`
- `after-sales-apply.page-schema.json` now maps to `WlVEd / 申请售后页 After-Sales Apply`
- `after-sales-progress.page-schema.json` now maps to `FuYbt / 售后进度页 After-Sales Progress`
- `after-sales-complete.page-schema.json` now maps to `vkfUd / 售后完成页 After-Sales Complete`
- `after-sales-order-detail.page-schema.json` now maps to `2FO7j / 订单详情页 After-Sales State`
- `pet-edit.page-schema.json` now covers both `11vrw / 编辑宠物页 Pet Edit` and `WA7xQ / 新增宠物页 Pet Add`; use `meta.variantFrameIds` to track the edit/create split in one schema
- `pet-timeline.page-schema.json` now points to `zqXpi / 单宠物成长档案页 Pet Growth Profile` and should be treated as the growth journal source of truth
- the legacy standalone `diagnosis-detail.page-schema.json` has been removed because the latest `.pen` top level no longer contains a corresponding formal frame
- `pet-profile.page-schema.json` keeps the featured diagnosis CTA as a flow placeholder, but no longer points to a nonexistent `/pages/diagnosis/detail` route
- all current schema files now use the active workspace `.pen` path `/Volumes/Beelink/software/pet-cloud-consultation-mini-program/design/frontendMobileScreens.pen`; legacy absolute path residues must not be reintroduced
- `beauty.page-schema.json` now maps to `Mpxrb / 护理首页 Beauty Care Home`
- `beauty-booking.page-schema.json` now maps to `ex1sD / 附近机构搜索页 Beauty Nearby Search`
- `order-cancel-flow-hint.page-schema.json` now maps to `X6bge / 订单取消流程提示` and should be treated as a helper flow note, not an independent `app.json` page
- after the 2026-03-28 schema alignment, the schema directory contains 57 files and all 57 now align with the latest saved `.pen` frames or frame variants
- the current saved `.pen` file contains 59 top-level frames:
  58 formal page frames
  1 helper frame `X6bge / 订单取消流程提示`
- all 58 formal page frames are now covered because `pet-edit` and `health-edit` each carry a create/edit dual-frame variant
- the helper frame `X6bge / 订单取消流程提示` is now also covered by its dedicated helper schema
- the previous standalone beauty topic page, result list page and place detail page have been removed from the latest `.pen`; do not recreate independent beauty detail/list/detail schemas unless the design adds them back
- frontend `MapAPI` currently exposes `geocode / reverseGeocode / searchSuggest` only; beauty search implementation must first add an adapter for `GET /api/map/poi/nearby`

## Review checklist

- section order matches `.pen`
- exact visible copy matches `.pen`
- spacing and card hierarchy are represented in schema
- data bindings map cleanly to current API payloads
- WXML structure follows the schema, not ad-hoc layout edits
- screenshot verification completed before parity claim
