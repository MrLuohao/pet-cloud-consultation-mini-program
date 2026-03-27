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
- `order-review.page-schema.json`: order review reference implementation
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
- `pet-list.page-schema.json`: pet gallery list reference implementation
- `pet-profile.page-schema.json`: pet health profile reference implementation
- `pet-edit.page-schema.json`: pet edit reference implementation
- `pet-timeline.page-schema.json`: pet growth archive reference implementation

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
- `pet-edit.page-schema.json` now covers both `11vrw / 编辑宠物页 Pet Edit` and `WA7xQ / 新增宠物页 Pet Add`; use `meta.variantFrameIds` to track the edit/create split in one schema
- `pet-timeline.page-schema.json` now points to `zqXpi / 单宠物成长档案页 Pet Growth Profile` and should be treated as the growth journal source of truth
- the legacy standalone `diagnosis-detail.page-schema.json` has been removed because the latest `.pen` top level no longer contains a corresponding formal frame
- `pet-profile.page-schema.json` keeps the featured diagnosis CTA as a flow placeholder, but no longer points to a nonexistent `/pages/diagnosis/detail` route
- all current schema files now use the active workspace `.pen` path `/Volumes/Beelink/software/pet-cloud-consultation-mini-program/design/frontendMobileScreens.pen`; legacy absolute path residues must not be reintroduced
- `beauty.page-schema.json` now maps to `Mpxrb / 护理首页 Beauty Care Home`
- `beauty-booking.page-schema.json` now maps to `ex1sD / 附近机构搜索页 Beauty Nearby Search`
- after the 2026-03-28 beauty simplification, the schema directory contains 43 files and all 43 now align with the latest saved `.pen` frames
- the current saved `.pen` file contains 46 top-level frames:
  45 formal page frames
  1 helper frame `X6bge / 订单取消流程提示`
- all 45 formal page frames are now covered because `pet-edit` and `health-edit` each carry a create/edit dual-frame variant
- the previous standalone beauty topic page, result list page and place detail page have been removed from the latest `.pen`; do not recreate independent beauty detail/list/detail schemas unless the design adds them back
- frontend `MapAPI` currently exposes `geocode / reverseGeocode / searchSuggest` only; beauty search implementation must first add an adapter for `GET /api/map/poi/nearby`

## Review checklist

- section order matches `.pen`
- exact visible copy matches `.pen`
- spacing and card hierarchy are represented in schema
- data bindings map cleanly to current API payloads
- WXML structure follows the schema, not ad-hoc layout edits
- screenshot verification completed before parity claim
