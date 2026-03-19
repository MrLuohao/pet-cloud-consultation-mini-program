const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..', '..');
const schemasDir = path.join(root, 'apps', 'frontend', 'docs', 'design-schemas');
const petListSchemaPath = path.join(schemasDir, 'pet-list.page-schema.json');
const petProfileSchemaPath = path.join(schemasDir, 'pet-profile.page-schema.json');
const addressPickerSchemaPath = path.join(schemasDir, 'address-picker.page-schema.json');
const schemaReadmePath = path.join(schemasDir, 'README.md');

assert.ok(fs.existsSync(petListSchemaPath), 'pet list schema should exist');
assert.ok(fs.existsSync(petProfileSchemaPath), 'pet profile schema should exist');
assert.ok(!fs.existsSync(addressPickerSchemaPath), 'stale address picker schema should be removed');

const petListSchema = JSON.parse(fs.readFileSync(petListSchemaPath, 'utf8'));
const petProfileSchema = JSON.parse(fs.readFileSync(petProfileSchemaPath, 'utf8'));
const schemaReadme = fs.readFileSync(schemaReadmePath, 'utf8');

assert.equal(petListSchema.meta.frameId, 'cKCx8');
assert.equal(petListSchema.meta.frameName, '我的宠物列表 My Pets');
assert.equal(petProfileSchema.meta.frameId, 'H6Xma');
assert.equal(petProfileSchema.meta.frameName, '单宠物健康档案页 Pet Health Profile');

assert.match(schemaReadme, /pet-profile\.page-schema\.json/, 'schema readme should list pet profile contract');
assert.doesNotMatch(schemaReadme, /address-picker\.page-schema\.json/, 'schema readme should not list retired address picker contract');
