# Pet Pages Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Rebuild `pages/pet/list` into the approved lightweight pet asset entry page and add a dedicated per-pet health profile page that matches the current design rules and pet-module direction.

**Architecture:** Use `design/frontendMobileScreens.pen` as the visual source of truth for the pet list page, then introduce a new `pages/pet/profile/index` page as the single-pet depth page defined by `docs/plans/2026-03-15-pet-module-design.md`. Reuse existing `PetAPI` and `HealthAPI` first, keep new logic inside thin mapping helpers, and preserve existing edit / timeline / health-entry pages as downstream targets.

**Tech Stack:** WeChat Mini Program (`WXML`, `WXSS`, `JS`), existing `apps/frontend/miniprogram/utils/api.js` request layer, existing pet / health backend APIs, Node-based local file assertions.

---

## Source-of-Truth Notes

- Global rules must be read in this order before implementation:
  1. `design/frontendMobileScreens.pen`
  2. `docs/project-rules.md`
  3. `docs/frontend-design-principles.md`
  4. `apps/frontend/docs/design-schemas/README.md`
  5. `docs/backend-integration-tracking.md`
- Approved pet list `.pen` frame: `我的宠物列表 My Pets` (`cKCx8`)
- Important `.pen` fact: the current `cKCx8` frame has been refined into a gallery-style pet showcase:
  - page title `宠物档案`
  - one full hero pet card
  - one partially revealed next card underneath
  - bottom `新增宠物` button
- Updated `.pen` fact (2026-03-17): the single-pet profile page now has a dedicated frame in the current `.pen` file:
  - frame name: `单宠物健康档案页 Pet Health Profile`
  - `frameId = H6Xma`
  - page structure:
    - light identity header
    - strong health summary card
    - light reminder overview
    - health record timeline
    - bottom actions
  - placement on canvas:
    - intentionally placed to the right of `cKCx8`
    - should be read as the next screen in the pet main path
  - current visual status:
    - refined purple-white version kept as current approved direction
    - a warmer luxury palette was explored and explicitly not adopted

## Current Code Reality

- Existing list page files:
  - `apps/frontend/miniprogram/pages/pet/list.js`
  - `apps/frontend/miniprogram/pages/pet/list.wxml`
  - `apps/frontend/miniprogram/pages/pet/list.wxss`
- Existing downstream pages already available:
  - `apps/frontend/miniprogram/pages/pet/edit.*`
  - `apps/frontend/miniprogram/pages/pet/timeline/index.*`
  - `apps/frontend/miniprogram/pages/health/edit.*`
- Existing APIs already available:
  - `PetAPI.getList()`
  - `PetAPI.getDetail(id)`
  - `PetAPI.getTimeline(petId)`
  - `PetAPI.getMonthlyReport(petId, year, month)`
  - `HealthAPI.getByPet(petId)`
  - `HealthAPI.getReminders()`
- Existing gap to account for in implementation:
  - current tests folder only has unrelated page redesign tests
  - no existing pet-page schema tests yet
  - no current `pages/pet/profile/index` route

## Implementation Constraints

- Do not render fake status-bar text from `.pen`
- Keep Mini Program native navigation bar; do not add `navigationStyle: custom`
- For `pages/pet/list`, follow `cKCx8` section order exactly:
  - page title
  - gallery-style hero card stage / empty state
  - fixed bottom action
- For the new profile page, do not invent dashboard-like charts, tabs, or heavy KPI modules
- Do not add new backend endpoints in phase 1 unless the existing payloads make the page impossible to compose
- The profile page should now use `H6Xma` as the primary `.pen` source instead of a fully derived visual approximation

## Current Progress Sync (2026-03-19)

- Task 1 状态：已完成
  - `pet-profile.page-schema.json` 已补齐
  - `tests/frontend/pet-page-schema.test.js` 已补齐并通过
- Task 8 状态：文档同步已完成，截图验收仍待补齐
- 当前真正未完成的宠物链动作：
  - `pages/pet/profile/index` 路由与页面实现
  - `pages/pet/list` 到新档案页的真实跳转切换
  - 开发者工具或真机截图级验收

### Task 1: Baseline design facts and create page schema contracts

Status: Completed on 2026-03-19

**Files:**
- Modify: `docs/plans/2026-03-15-pet-module-design.md`
- Create: `apps/frontend/docs/design-schemas/pet-list.page-schema.json`
- Create: `apps/frontend/docs/design-schemas/pet-profile.page-schema.json`
- Create: `tests/frontend/pet-page-schema.test.js`

**Step 1: Write the failing test**

Create a lightweight schema presence and source-reference test:

```js
const assert = require('node:assert/strict')
const fs = require('node:fs')
const path = require('node:path')

const root = path.resolve(__dirname, '..', '..')
const petListSchemaPath = path.join(root, 'apps/frontend/docs/design-schemas/pet-list.page-schema.json')
const petProfileSchemaPath = path.join(root, 'apps/frontend/docs/design-schemas/pet-profile.page-schema.json')

assert.ok(fs.existsSync(petListSchemaPath), 'pet list schema should exist')
assert.ok(fs.existsSync(petProfileSchemaPath), 'pet profile schema should exist')

const petListSchema = JSON.parse(fs.readFileSync(petListSchemaPath, 'utf8'))
const petProfileSchema = JSON.parse(fs.readFileSync(petProfileSchemaPath, 'utf8'))

assert.equal(petListSchema.meta.sourceFrameId, 'cKCx8')
assert.equal(petListSchema.meta.sourceFrameName, '我的宠物列表 My Pets')
assert.equal(petProfileSchema.meta.frameId, 'H6Xma')
assert.equal(petProfileSchema.meta.frameName, '单宠物健康档案页 Pet Health Profile')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-page-schema.test.js`
Expected: historical red step already completed in this workspace.

**Step 3: Write minimal implementation**

- Add `pet-list.page-schema.json` describing:
  - `.pen` source frame `cKCx8`
  - layout wrapper, header card, pet list, empty state, bottom action
  - allowed data bindings for avatar, name, breed, age, summary, CTA labels
  - verification checklist tied to the `.pen` screenshot
- Add `pet-profile.page-schema.json` describing:
  - `.pen` source frame `H6Xma`
  - identity header
  - current health summary
  - reminders
  - health records
  - quick actions
  - verification checklist tied to the current `.pen` screenshot
- Update `docs/plans/2026-03-15-pet-module-design.md` to record:
  - pet list is backed by `.pen` frame `cKCx8`
  - pet profile is now backed by `.pen` frame `H6Xma`

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-page-schema.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-page-schema.test.js apps/frontend/docs/design-schemas/pet-list.page-schema.json apps/frontend/docs/design-schemas/pet-profile.page-schema.json docs/plans/2026-03-15-pet-module-design.md
git commit -m "docs: baseline pet page schema contracts"
```

### Task 2: Replace the current pet list page with the approved lightweight asset-entry structure

**Files:**
- Modify: `apps/frontend/miniprogram/pages/pet/list.wxml`
- Modify: `apps/frontend/miniprogram/pages/pet/list.wxss`
- Modify: `apps/frontend/miniprogram/pages/pet/list.js`
- Create: `tests/frontend/pet-list-redesign.test.js`

**Step 1: Write the failing test**

Add assertions for the required list-page structure:

```js
assert.match(wxml, /宠物档案/, 'pet list page should keep the approved title')
assert.match(wxml, /查看档案/, 'pet list cards should expose the primary gallery CTA')
assert.match(wxml, /新增宠物/, 'pet list page should keep the fixed bottom action')
assert.doesNotMatch(wxml, /成长档案/, 'pet list page should remove the old timeline-first CTA')
assert.doesNotMatch(wxml, /pet-type-badge/, 'pet list page should drop the old playful type badge treatment')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-list-redesign.test.js`
Expected: FAIL because the current page still uses the old card structure and action wording.

**Step 3: Write minimal implementation**

- Rebuild the page from `pet-list.page-schema.json`:
  - keep the gallery stage visually close to `.pen` `cKCx8`
  - keep the body visually sparse and breathable
  - render one large main pet card and a partially revealed next card
  - keep bottom fixed `新增宠物` CTA
- In JS:
  - normalize `breed + age`
  - derive one service-style summary string per pet
  - build count-chip copy like `01 / 03`
  - keep login gate behavior unchanged
  - route primary CTA to the current archive/detail entry until dedicated profile page lands
- Remove old list-page affordances that contradict the approved direction:
  - emoji type badge emphasis
  - stacked health tag cluster
  - timeline-first action strip

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-list-redesign.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-list-redesign.test.js apps/frontend/miniprogram/pages/pet/list.wxml apps/frontend/miniprogram/pages/pet/list.wxss apps/frontend/miniprogram/pages/pet/list.js
git commit -m "feat: redesign pet list as light asset entry page"
```

### Task 3: Add the new per-pet profile route and page shell

**Files:**
- Create: `apps/frontend/miniprogram/pages/pet/profile/index.json`
- Create: `apps/frontend/miniprogram/pages/pet/profile/index.wxml`
- Create: `apps/frontend/miniprogram/pages/pet/profile/index.wxss`
- Create: `apps/frontend/miniprogram/pages/pet/profile/index.js`
- Modify: `apps/frontend/miniprogram/app.json`
- Create: `tests/frontend/pet-profile-page.test.js`

**Step 1: Write the failing test**

Add assertions for the route and shell sections:

```js
assert.match(appJson, /pages\\/pet\\/profile\\/index/, 'app.json should register the pet profile page')
assert.match(wxml, /当前健康摘要/, 'pet profile page should include the health summary section')
assert.match(wxml, /待办提醒/, 'pet profile page should include reminder section')
assert.match(wxml, /健康记录/, 'pet profile page should include timeline section')
assert.match(wxml, /新增健康记录/, 'pet profile page should include quick action area')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-profile-page.test.js`
Expected: FAIL because the route and page files do not exist yet.

**Step 3: Write minimal implementation**

- Register `pages/pet/profile/index` in `app.json`
- Create the new page shell using the approved derived structure:
  - pet identity header
  - current health summary
  - reminders
  - health records
  - bottom quick actions
- In `pages/pet/list.js`, change the primary CTA to navigate to:
  - `/pages/pet/profile/index?petId=...&petName=...`
- Set page title to a simple native-nav title such as `健康档案`
- Use the final visual source from `H6Xma`, not the earlier derived-only notes

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-profile-page.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-profile-page.test.js apps/frontend/miniprogram/app.json apps/frontend/miniprogram/pages/pet/profile/index.json apps/frontend/miniprogram/pages/pet/profile/index.wxml apps/frontend/miniprogram/pages/pet/profile/index.wxss apps/frontend/miniprogram/pages/pet/profile/index.js
git commit -m "feat: add pet profile page shell"
```

### Task 4: Compose pet profile state from existing pet and health APIs

**Files:**
- Modify: `apps/frontend/miniprogram/pages/pet/profile/index.js`
- Modify: `apps/frontend/miniprogram/utils/api.js`
- Create: `tests/frontend/pet-profile-presenter.test.js`

**Step 1: Write the failing test**

Add presenter-level assertions for composed state:

```js
assert.equal(state.summary.statusLabel, '稳定')
assert.equal(state.reminders.length, 2)
assert.equal(state.records[0].sectionLabel, '健康记录')
assert.equal(state.actions[0].label, '新增健康记录')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-profile-presenter.test.js`
Expected: FAIL because the composition helpers and fallback state do not exist yet.

**Step 3: Write minimal implementation**

- Add local mapping helpers in `pages/pet/profile/index.js` or a colocated helper in the same directory
- Compose page state from existing APIs:
  - `PetAPI.getDetail(petId)`
  - `HealthAPI.getByPet(petId)`
  - `HealthAPI.getReminders()`
  - `PetAPI.getTimeline(petId)`
- If the current backend already exposes them or stubs are required, add only thin wrappers for:
  - `GET /api/pets/{petId}/diagnosis-summary`
  - `GET /api/pets/{petId}/medical-records`
- Filter reminders by current `petId`
- Keep graceful degradation if one endpoint fails:
  - identity still renders from pet detail
  - reminders fall back to empty module copy
  - records fall back to empty state copy instead of page crash

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-profile-presenter.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-profile-presenter.test.js apps/frontend/miniprogram/pages/pet/profile/index.js apps/frontend/miniprogram/utils/api.js
git commit -m "feat: compose pet profile from existing health data"
```

### Task 5: Wire profile quick actions to existing downstream pages

**Files:**
- Modify: `apps/frontend/miniprogram/pages/pet/profile/index.wxml`
- Modify: `apps/frontend/miniprogram/pages/pet/profile/index.js`
- Create: `tests/frontend/pet-profile-actions.test.js`

**Step 1: Write the failing test**

Add assertions for the quick-action targets:

```js
assert.match(js, /url:\\s*['\"]\\/pages\\/health\\/edit/, 'profile should link to create health record')
assert.match(js, /url:\\s*['\"]\\/pages\\/pet\\/timeline\\/index/, 'profile should link to pet timeline')
assert.match(js, /url:\\s*['\"]\\/pages\\/pet\\/edit/, 'profile should link to pet edit')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-profile-actions.test.js`
Expected: FAIL until the quick actions are implemented.

**Step 3: Write minimal implementation**

- Add three quick actions:
  - `新增健康记录`
  - `查看成长档案`
  - `编辑宠物资料`
- Pass `petId` and `petName` through the target routes where needed
- Keep action hierarchy light:
  - no extra modal menus
  - no duplicate CTA rows

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-profile-actions.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-profile-actions.test.js apps/frontend/miniprogram/pages/pet/profile/index.wxml apps/frontend/miniprogram/pages/pet/profile/index.js
git commit -m "feat: connect pet profile quick actions"
```

### Task 6: Add empty-state and partial-failure safeguards for the new flow

**Files:**
- Modify: `apps/frontend/miniprogram/pages/pet/list.js`
- Modify: `apps/frontend/miniprogram/pages/pet/list.wxml`
- Modify: `apps/frontend/miniprogram/pages/pet/profile/index.js`
- Modify: `apps/frontend/miniprogram/pages/pet/profile/index.wxml`
- Create: `tests/frontend/pet-page-empty-state.test.js`

**Step 1: Write the failing test**

Add assertions for empty and degraded states:

```js
assert.match(listWxml, /还没有宠物档案/, 'pet list should keep the approved empty-state title')
assert.match(profileWxml, /当前暂无待处理提醒/, 'profile page should show reminder empty copy')
assert.match(profileJs, /records:\\s*\\[\\]/, 'profile page should support empty record state')
```

**Step 2: Run test to verify it fails**

Run: `node tests/frontend/pet-page-empty-state.test.js`
Expected: FAIL because the new empty-state copy and fallback structure are not fully implemented yet.

**Step 3: Write minimal implementation**

- Ensure list-page empty state copy aligns with `docs/plans/2026-03-15-pet-module-design.md`
- Ensure profile page keeps modules visible even when data is empty:
  - reminder empty copy
  - records empty copy
  - summary fallback copy
- Keep modules lightweight; do not conditionally remove entire sections

**Step 4: Run test to verify it passes**

Run: `node tests/frontend/pet-page-empty-state.test.js`
Expected: PASS

**Step 5: Commit**

```bash
git add tests/frontend/pet-page-empty-state.test.js apps/frontend/miniprogram/pages/pet/list.js apps/frontend/miniprogram/pages/pet/list.wxml apps/frontend/miniprogram/pages/pet/profile/index.js apps/frontend/miniprogram/pages/pet/profile/index.wxml
git commit -m "fix: harden pet page empty and fallback states"
```

### Task 7: Verify syntax and targeted frontend tests

**Files:**
- Test: `tests/frontend/pet-page-schema.test.js`
- Test: `tests/frontend/pet-list-redesign.test.js`
- Test: `tests/frontend/pet-profile-page.test.js`
- Test: `tests/frontend/pet-profile-presenter.test.js`
- Test: `tests/frontend/pet-profile-actions.test.js`
- Test: `tests/frontend/pet-page-empty-state.test.js`

**Step 1: Write the failing test**

No new tests. Reuse the previously added suite as the verification gate.

**Step 2: Run test to verify it fails**

Run each new test before its corresponding implementation task as described above.
Expected: FAIL before each implementation task.

**Step 3: Write minimal implementation**

No new code. Ensure all prior tasks are complete.

**Step 4: Run test to verify it passes**

Run:

```bash
node tests/frontend/pet-page-schema.test.js
node tests/frontend/pet-list-redesign.test.js
node tests/frontend/pet-profile-page.test.js
node tests/frontend/pet-profile-presenter.test.js
node tests/frontend/pet-profile-actions.test.js
node tests/frontend/pet-page-empty-state.test.js
node --check apps/frontend/miniprogram/pages/pet/list.js
node --check apps/frontend/miniprogram/pages/pet/profile/index.js
```

Expected: all PASS with no syntax errors.

**Step 5: Commit**

```bash
git add tests/frontend
git commit -m "test: verify pet list and profile flow"
```

### Task 8: Complete screenshot verification and planning handoff updates

Status: Partially completed on 2026-03-19

**Files:**
- Modify: `docs/plans/2026-03-15-pet-module-design.md`
- Modify: `docs/backend-integration-tracking.md`
- Modify: `docs/plans/2026-03-15-pet-pages-implementation.md`

**Step 1: Write the failing test**

No automated test. This task is the required documentation and screenshot verification gate mandated by project rules.

**Step 2: Run test to verify it fails**

Verification is incomplete if any of the following is missing:

- list-page screenshot comparison against `.pen` frame `cKCx8`
- explicit note that profile page now has dedicated `.pen` frame `H6Xma` and current schema contract
- backend tracking note for any wrapper APIs actually added in `utils/api.js`

Expected: FAIL as a process gate until the notes are updated.

**Step 3: Write minimal implementation**

- Capture a Mini Program screenshot of the final `pages/pet/list`
- Capture a Mini Program screenshot of the final `pages/pet/profile/index`
- Compare it against `.pen` frame `cKCx8` and record any accepted deviations
- Compare the profile page against `.pen` frame `H6Xma` and record any accepted deviations
- Update `docs/plans/2026-03-15-pet-module-design.md` with:
  - what was implemented
  - what remains pending route / code follow-up
- Update `docs/backend-integration-tracking.md` with:
  - whether `diagnosis-summary` and `medical-records` wrappers were used
  - whether current APIs were sufficient for phase 1
- Update this plan file with a short execution note if any task scope changed during implementation

**Step 4: Run test to verify it passes**

Manual verification checklist:

- `pages/pet/list` visual hierarchy matches `.pen` `cKCx8`
- `pages/pet/profile/index` visual hierarchy matches `.pen` `H6Xma`
- no fake status bar rendered in WXML
- profile page sections remain lightweight and non-dashboard-like
- all implementation deviations are documented

Expected: checklist complete.

**Step 5: Commit**

```bash
git add docs/plans/2026-03-15-pet-module-design.md docs/backend-integration-tracking.md docs/plans/2026-03-15-pet-pages-implementation.md
git commit -m "docs: record pet page verification and integration notes"
```

## Execution Notes

- Execute tasks in order; do not start the profile page before the list-page schema and route assumptions are fixed
- Keep commits small and task-scoped
- If the pet-profile visual source changes again, stop and update:
  - `pet-profile.page-schema.json`
  - `docs/plans/2026-03-15-pet-module-design.md`
  - this plan

Plan complete and saved to `docs/plans/2026-03-15-pet-pages-implementation.md`. Two execution options:

**1. Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

**2. Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

Which approach?
