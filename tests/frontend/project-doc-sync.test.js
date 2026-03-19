const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');

function read(filePath) {
  return fs.readFileSync(path.join(root, filePath), 'utf8');
}

const backendTracking = read('docs/backend-integration-tracking.md');
const backendHandoff = read('memory/project-context-handoff.md');
const transactionPlan = read('docs/plans/2026-03-15-backend-transaction-domain-reset-design.md');
const petPlan = read('docs/plans/2026-03-15-pet-pages-implementation.md');

assert.match(backendTracking, /更新时间：2026-03-19/, 'backend integration tracking should reflect the latest sync date');
assert.match(backendTracking, /pet-profile\.page-schema\.json/, 'backend integration tracking should mention the pet profile schema');
assert.match(backendTracking, /address-list\.page-schema\.json/, 'backend integration tracking should mention the current address list contract');
assert.match(backendTracking, /旧的 `address-picker\.page-schema\.json` 已移除/, 'backend integration tracking should explicitly mark address picker as retired');

assert.match(backendHandoff, /address-list.*address-edit|地址链路[\s\S]*address-list[\s\S]*address-edit/, 'backend handoff should describe the current two-page address chain');
assert.doesNotMatch(backendHandoff, /用 .*地址选择页.*是否与当前后端模型一致/, 'backend handoff should not keep the retired address picker as the live verification target');

assert.match(transactionPlan, /覆盖以下页面链路：[\s\S]*地址管理页[\s\S]*地址编辑页/, 'transaction reset plan should use the current address-management terminology in the goal section');
assert.match(transactionPlan, /旧的独立 `address-picker` 契约已退出现行 schema 清单/, 'transaction reset plan should explicitly record the retired address picker contract');

assert.match(petPlan, /Task 1 状态：已完成/, 'pet page implementation plan should record Task 1 as completed');
assert.match(petPlan, /Task 8 状态：文档同步已完成，截图验收仍待补齐/, 'pet page implementation plan should record the current Task 8 state');
