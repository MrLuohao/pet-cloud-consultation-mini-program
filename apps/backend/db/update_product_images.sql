-- 更新商品图片路径
-- 每个商品包含1张封面主图 + 2张详情图，共3张高质量营销图片
-- 图片来源：Unsplash 高清无版权图片

-- 1. 进口天然猫粮 - 猫咪吃食系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/cat_food.jpg',
    `image_urls` = '["/uploads/products/cat_food.jpg", "/uploads/products/cat_food_2.jpg", "/uploads/products/cat_food_3.jpg"]'
WHERE `id` = 1;

-- 2. 智能饮水机 - 猫咪喝水系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/water_fountain.jpg',
    `image_urls` = '["/uploads/products/water_fountain.jpg", "/uploads/products/water_fountain_2.jpg", "/uploads/products/water_fountain_3.jpg"]'
WHERE `id` = 2;

-- 3. 狗狗咬胶玩具 - 狗狗咬玩具系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/dog_toy.jpg',
    `image_urls` = '["/uploads/products/dog_toy.jpg", "/uploads/products/dog_toy_2.jpg", "/uploads/products/dog_toy_3.jpg"]'
WHERE `id` = 3;

-- 4. 宠物复合维生素 - 保健品胶囊系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/vitamins.jpg',
    `image_urls` = '["/uploads/products/vitamins.jpg", "/uploads/products/vitamins_2.jpg", "/uploads/products/vitamins_3.jpg"]'
WHERE `id` = 4;

-- 5. 全自动猫砂盆 - 猫咪+猫砂盆系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/litter_box.jpg',
    `image_urls` = '["/uploads/products/litter_box.jpg", "/uploads/products/litter_box_2.jpg", "/uploads/products/litter_box_3.jpg"]'
WHERE `id` = 5;

-- 6. 宠物除臭剂 - 喷雾清洁系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/deodorizer.jpg',
    `image_urls` = '["/uploads/products/deodorizer.jpg", "/uploads/products/deodorizer_2.jpg", "/uploads/products/deodorizer_3.jpg"]'
WHERE `id` = 6;

-- 7. 狗狗飞盘 - 狗狗户外飞盘系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/frisbee.jpg',
    `image_urls` = '["/uploads/products/frisbee.jpg", "/uploads/products/frisbee_2.jpg", "/uploads/products/frisbee_3.jpg"]'
WHERE `id` = 7;

-- 8. 营养化毛膏 - 猫咪零食系列
UPDATE `product` SET
    `cover_url` = '/uploads/products/hairball_paste.jpg',
    `image_urls` = '["/uploads/products/hairball_paste.jpg", "/uploads/products/hairball_paste_2.jpg", "/uploads/products/hairball_paste_3.jpg"]'
WHERE `id` = 8;

-- 查看更新结果
SELECT id, name, cover_url, image_urls FROM product;
