-- 清理微信开发者工具临时头像地址，并修正历史 localhost 上传地址
-- 执行前请先备份数据库
USE `pet_cloud_db`;

-- 1) 清空不可访问的临时头像地址
UPDATE `wx_user`
SET `avatar_url` = ''
WHERE `avatar_url` LIKE 'wxfile://%'
   OR `avatar_url` LIKE 'http://tmp%'
   OR `avatar_url` LIKE '%/__tmp__/%'
   OR `avatar_url` LIKE '%127.0.0.1:%/__tmp__/%'
   OR `avatar_url` LIKE '%localhost:%/__tmp__/%';

-- 2) 统一修复历史 localhost/127.0.0.1 上传地址（用户服务端口 8117）
UPDATE `wx_user`
SET `avatar_url` = REPLACE(`avatar_url`, 'http://localhost:8080/uploads', 'http://10.0.12.147:8117/uploads')
WHERE `avatar_url` LIKE 'http://localhost:8080/uploads/%';

UPDATE `wx_user`
SET `avatar_url` = REPLACE(`avatar_url`, 'http://127.0.0.1:8080/uploads', 'http://10.0.12.147:8117/uploads')
WHERE `avatar_url` LIKE 'http://127.0.0.1:8080/uploads/%';

-- 3) 评论头像与用户主表保持一致（仅更新能关联到用户的记录）
UPDATE `article_comment` ac
JOIN `wx_user` wu ON ac.`user_id` = wu.`id`
SET ac.`user_avatar` = IFNULL(wu.`avatar_url`, '')
WHERE ac.`user_id` IS NOT NULL;

-- 4) 校验结果
SELECT 'wx_user.tmp_avatar' AS item, COUNT(*) AS cnt
FROM `wx_user`
WHERE `avatar_url` LIKE 'wxfile://%'
   OR `avatar_url` LIKE 'http://tmp%'
   OR `avatar_url` LIKE '%/__tmp__/%';

SELECT 'wx_user.localhost_avatar' AS item, COUNT(*) AS cnt
FROM `wx_user`
WHERE `avatar_url` LIKE 'http://localhost:8080/uploads/%'
   OR `avatar_url` LIKE 'http://127.0.0.1:8080/uploads/%';
