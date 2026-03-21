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
- `diagnosis-detail.page-schema.json`: complete AI diagnosis report reference implementation
- `order-review.page-schema.json`: order review reference implementation
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
- `cAZG0 / 宠物成长档案页 Pet Growth Timeline` now has a matching `pet-timeline.page-schema.json`
- `K1C5b / 完整诊断报告页 AI Diagnosis Report` now has a matching `diagnosis-detail.page-schema.json`
- `pet-edit.page-schema.json` now covers both `FLS1j / 宠物编辑页 Pet Edit` and `CiXLc / 新增宠物页 Pet Add`; the create state adds a light breed image gallery while edit mode stays form-first

## Review checklist

- section order matches `.pen`
- exact visible copy matches `.pen`
- spacing and card hierarchy are represented in schema
- data bindings map cleanly to current API payloads
- WXML structure follows the schema, not ad-hoc layout edits
- screenshot verification completed before parity claim
