# Product Image Generation Guidelines

适用范围：`PetCloudConsultation_Frontend` 小程序商城列表页、商品详情页首屏主图、商品详情页正文海报图，以及后续通过后台管理系统结合 AI 批量生成的商品营销图片。

这份文档的目标不是帮助生成“更吸睛”的运营图，而是帮助持续生成**不会破坏当前项目整体气质**的商品图。后续无论谁来生成图片，都应优先遵守这份规范，避免因为运营偏好或提示词习惯不一致，把页面重新拉回传统电商风、强促销风或内容平台拼贴风。

## 1. 总体目标

商品图应统一服务于当前项目已经建立的视觉方向：

- 简约
- 柔和
- 克制
- 有呼吸感
- 高级感来自秩序、比例和留白，而不是靠装饰堆砌

商品图的最终观感应更接近：

- 品牌画册
- Apple Store 式产品展示
- 安静的商业静物摄影
- 干净的白底 / 浅底陈列图

不应接近：

- 传统电商促销海报
- 满屏卖点长图
- 小红书拼贴封面
- 过度可爱或卡通化的宠物运营图

一句话原则：

`图片要像在认真展示一件产品，而不是在努力推销一件商品。`

## 2. 适用页面与用途

这份规范覆盖以下 3 类图：

### 2.1 商城列表页商品图

用途：

- 商品流中的封面图
- 首页 / 商城列表中的商品陈列图

目标：

- 干净、克制、可快速识别
- 主体明确
- 不抢页面结构风头

### 2.2 商品详情页首屏主图

用途：

- 商品详情页顶部海报区

目标：

- 更像单品海报
- 主体更强
- 气质更完整
- 比列表页更有展示感

### 2.3 商品详情页正文海报图

用途：

- 商品详情页中段的品牌说明图
- 产品介绍、工艺说明、使用方式、适用对象等段落配图

目标：

- 更像产品画册页
- 图文关系清楚
- 不像传统详情长图或卖点信息图

## 3. 核心视觉特征

所有商品图都必须满足以下特征：

- 产品主体明确、稳定、居中或接近视觉中心
- 画面有明显留白，不贴边，不满铺
- 背景干净，不复杂，不抢主角
- 光线柔和均匀，不使用强硬商业灯效
- 色彩低饱和，不做多色竞争
- 产品摄影感强于运营设计感
- 看起来像“被展示”，不是“被包装”

优先保留的感受：

- 干净
- 安静
- 稳定
- 温和
- 高级

## 4. 颜色规范

### 4.1 推荐主背景颜色

优先使用以下背景方向：

- 白色
- 暖白
- 浅米白
- 浅灰白
- 浅沙色
- 极浅奶油灰

推荐色值：

- 暖白：`#FCFBF8`
- 浅米白：`#F8F7F4`
- 浅灰白：`#F6F4F1`
- 浅沙色：`#F7F3ED`
- 浅奶油灰：`#FAF8F3`

### 4.2 推荐辅助颜色

允许少量出现的辅助色：

- 柔和紫：`#E8DDFF`
- 浅紫辅助：`#F0E7FF`
- 米杏色：`#E6D2AF`
- 浅米棕：`#EDE0CC`
- 雾灰：`#858A95`

这些颜色只作为：

- 产品包装本身的柔和色
- 极轻背景层次
- 细节辅助色

不能大面积抢视觉。

### 4.3 禁止颜色与表现

明确禁止：

- 高饱和红
- 高饱和橙
- 荧光绿 / 荧光黄
- 彩虹色
- 多个高饱和颜色同时出现
- 强撞色对比
- 大面积渐变紫
- 深黑大底
- 强暗色背景

除非后续单独定义活动页规范，否则不要生成黑底海报、夜景海报、霓虹感海报。

### 4.4 色彩占比执行规则

后续无论是 AI 生成图，还是设计师手工出图，都必须遵守同一套色彩占比逻辑：

- `70% - 85%` 为中性浅背景
- `10% - 25%` 为产品本体 / 包装本色
- `0% - 10%` 为辅助色或轻道具色

结论：

- 颜色主角永远是商品本身
- 背景只负责托住商品，不负责制造戏剧感
- 辅助色只能点到为止，不能变成第二主角

### 4.5 背景色与包装颜色的搭配规则

这是后续后台维护时最容易出错的地方。

不要只看“单张图好不好看”，要看它是否会在商城流和详情页里显得统一。

#### 包装本身偏紫 / 偏粉紫

推荐背景：

- `#FCFBF8`
- `#F8F7F4`
- `#F6F4F1`

推荐辅助：

- `#EEE7DB`
- `#F2E9FF`

禁止：

- 再叠一个高饱和紫背景
- 再叠大面积渐变紫

原因：

- 页面本身已有紫白品牌基调
- 商品图如果再大面积用紫，会和页面色调抢主导权

#### 包装本身偏米黄 / 偏粮食色 / 偏棕

推荐背景：

- `#FCFBF8`
- `#FAF8F3`
- `#F6F4F1`

推荐辅助：

- `#EDE0CC`
- `#EEE7DB`

禁止：

- 高饱和橙
- 深咖啡重底

#### 包装本身偏蓝灰 / 医疗护理感

推荐背景：

- `#FCFBF8`
- `#F6F4F1`
- `#F8F7F4`

推荐辅助：

- `#E9EEF5`
- `#EDE7DE`

禁止：

- 冰蓝高亮
- 冷白医院风大面积铺底

目标：

- 保留护理感、可信感
- 但仍然属于当前项目的“温和克制”体系

#### 包装本身颜色很多 / 本身就很花

处理原则：

- 背景一律退到最浅中性色
- 道具尽量取消
- 不再额外加彩色辅助物

目标：

- 让图重新安静下来
- 不让页面被一张花图带偏

### 4.6 商品类型建议色板

以下不是硬编码分类，而是建议你在后台做图时优先套用的色板方向。

#### 主粮 / 冻干 / 零食

建议关键词：

- 暖白
- 麦穗米色
- 烘焙浅棕
- 低饱和谷物色

推荐组合：

- 背景：`#FCFBF8`
- 辅助：`#EEE7DB`
- 点缀：`#EDE0CC`

适合表达：

- 日常主食
- 稳定
- 温和
- 真实食材感

#### 营养 / 保健 / 功能补充

建议关键词：

- 雾灰白
- 浅石色
- 温和冷灰

推荐组合：

- 背景：`#F8F7F4`
- 辅助：`#EDE7DE`
- 点缀：`#E9EEF5`

适合表达：

- 科学感
- 稳定感
- 专业感

#### 护理 / 清洁 / 洗护

建议关键词：

- 暖白
- 雾灰
- 极浅蓝灰

推荐组合：

- 背景：`#FCFBF8`
- 辅助：`#F1F3F6`
- 点缀：`#E6EBF2`

适合表达：

- 干净
- 清爽
- 可信赖

#### 玩具 / 互动用品

玩具类可以比其他类别稍微活泼一点，但只能“轻活泼”，不能变成儿童玩具海报。

推荐组合：

- 背景：`#FAF8F3`
- 辅助：`#F2E9FF`
- 点缀：`#F3E6C8`

限制：

- 只允许一处柔和亮色点缀
- 不允许红黄蓝多色同时高强度出现

### 4.7 同一商品的一组图片必须统一

后续一个商品如果会有：

- 封面图
- 首屏主图
- 详情说明图 1
- 详情说明图 2

那么它们必须像同一套画册，而不是四种风格。

至少保持以下一致：

- 同一背景家族
- 同一光线方向
- 同一阴影强弱
- 同一道具语言
- 同一饱和度水平

不允许出现：

- 第一张像 Apple Store
- 第二张像直播间封面
- 第三张像小红书拼贴
- 第四张像淘宝详情长图

### 4.8 道具搭配规则

允许出现的道具应只服务于“理解产品”，而不是服务于“装饰画面”。

允许：

- 一到两个辅助物
- 与产品用途明确相关的元素
- 低饱和、体量小、边缘柔和的道具

不允许：

- 花束
- 彩色布景
- 多个容器叠放
- 满桌小物
- 卡通配饰
- 贴纸、手写体、波浪边框

一句话规则：

`道具最多是注解，不能变成舞台。`

## 5. 构图规范

### 5.1 主体位置

- 商品主体应处于画面中心或接近中心
- 默认优先垂直居中
- 若有辅助物体，主体仍必须保持第一视觉

### 5.2 留白要求

- 四周要留出明显呼吸区
- 主体不要贴边
- 画面不要塞满

建议安全留白：

- 主体四周至少保留画面宽度的 `10% - 14%`

### 5.3 画面复杂度

允许：

- 单主体
- 一到两个辅助物料
- 轻场景关系

不允许：

- 一堆道具围绕商品
- 复杂桌面
- 杂乱环境
- 多商品挤在一起没有主次

### 5.4 阴影与空间感

允许：

- 很轻的自然落影
- 很弱的底面感
- 轻微空间层次

不允许：

- 厚重投影
- 浮空感很强的投影
- 发光边缘
- 夸张的立体特效

## 6. 不同类型图片的具体约束

### 6.1 商城列表页商品图

列表页商品图应满足：

- 单品主视觉
- 构图稳定
- 背景干净
- 信息密度低
- 适合作为封面

建议：

- 商品主体明显
- 背景接近白底或极浅底
- 一张图只讲一件事
- 不做图中标题
- 不做卖点贴片

不要：

- 太强的场景氛围
- 太多辅助元素
- 很像笔记封面
- 很像促销海报

### 6.2 商品详情页首屏主图

详情页首屏主图应满足：

- 比列表页更像海报
- 主体更大
- 气质更完整
- 更有展示感

建议：

- 产品占比更高
- 留白仍然充足
- 允许一层轻微底面感或极淡空间感
- 尽量保持安静单品展示

不要：

- 过强的轮播 banner 感
- 满屏排版型装饰
- 很多道具
- 很像电商首图

### 6.3 商品详情页正文海报图

正文海报图应满足：

- 像画册页
- 每一张图只表达一个主题
- 可以有轻场景，但不能主导

适合内容：

- 产品本身是什么
- 配方 / 成分 / 工艺
- 使用方式
- 适用对象

建议：

- 一张大图 + 一句短说明
- 图像主题明确
- 可做双物料对照、单品特写、轻场景摆放

不要：

- 把所有卖点做进一张图
- 信息图式堆叠
- 参数表直接做成图片

## 7. 图片尺寸与比例规范

### 7.1 原始母图

所有 AI 生成图片应优先生成高分辨率母图，再按业务场景裁切。

推荐母图尺寸：

- `2048 x 2048`
- 或更高

不要直接生成勉强贴合组件边界的小图。

### 7.2 列表页商品图

当前项目要分两种情况理解：

#### 情况 A：沿用当前单封面模式

当前前端的 `coverUrl` 同时用于：

- 商城列表页商品图
- 商品详情页首屏主图

因此在**不拆字段**的前提下，后台维护应统一按一张“主封面母图”来制作，而不是分别做两张不同风格的图。

单封面模式推荐：

- 推荐母图比例：`4:5`
- 推荐导出尺寸：`1600 x 2000`
- 商品主体必须放在画面中心安全区
- 上下留白要明显，保证商城列表页横向裁切时裁掉的是背景，不是商品本体

#### 情况 B：未来拆分列表封面字段

如果后续后台和接口新增：

- `listCoverUrl`
- `detailHeroUrl`

那么列表封面可以单独按横向比例制作。

列表单独封面推荐：

- 推荐比例：`7:5` 或接近 `1.40:1`
- 推荐导出尺寸：`1680 x 1200`
- 适合当前商城双列商品卡横向封面容器

### 7.3 商品详情页首屏主图

推荐比例：

- 当前单封面模式优先继续使用 `4:5`
- 若未来拆分详情首图字段，可使用 `4:5` 或接近竖版海报比例

推荐导出尺寸：

- `1600 x 2000`
- 或 `1536 x 2048`

额外要求：

- 主体建议占母图高度的 `56% - 68%`
- 主体左右两侧保留至少 `12%` 留白
- 顶部与底部都要留出缓冲区，避免在列表横裁时切到产品关键结构

### 7.4 商品详情页正文海报图

推荐比例：

- 如果是竖版画册感单图：优先 `4:5`
- 如果是轻对照、工艺说明、双物料轻场景：优先 `4:3`

如果是横向双物料或轻场景对照：

- 可使用 `1600 x 1200`

如果是竖版说明图：

- 推荐 `1440 x 1800`
- 或 `1600 x 2000`

### 7.5 安全裁切要求

- 主体必须位于安全中心区
- 不允许关键元素靠近边缘
- 后续裁切后，主体不能被切断

## 8. 允许与禁止的元素

### 8.1 允许出现

- 白底单品陈列
- 极浅底色陈列
- 一到两个辅助物料
- 轻场景摆放
- 柔和自然阴影
- 产品包装细节
- 极少量品牌感辅助色块

### 8.2 禁止出现

- 红色促销角标
- “爆款推荐 / 限时优惠 / 立减 / 买赠”等营销文案入图
- 优惠券
- 价格直接写在图里
- 图标卖点贴片
- 杂乱道具
- 宠物表情包风格
- 卡通贴纸风
- 拟人化插画风
- 小红书拼贴封面风
- 信息图风
- 厚描边
- 重投影
- 强发光特效
- 复杂背景纹理

## 9. 文本与信息入图规则

默认规则：

- 商品图中不放营销标题
- 商品图中不放价格
- 商品图中不放参数表
- 商品图中不放大段说明文字

商品相关文字应优先交给页面 UI 承担，而不是写进图片里。

如果未来业务必须在图中出现极少量文字，应满足：

- 一张图只允许极少量说明
- 不得使用运营话术
- 不得比产品主体更抢眼

但在当前项目阶段，默认仍建议：

`尽量不在商品图里放字。`

## 10. AI 生成提示词总原则

无论使用什么模型或平台，提示词都要固定以下方向：

- premium product photography
- calm editorial still life
- apple-store-like product presentation
- white or warm off-white background
- minimal composition
- soft natural lighting
- generous negative space
- centered subject
- clean commercial still life
- low saturation
- no text overlay
- no promotional graphics
- neutral palette harmony
- restrained color pairing
- soft off-white background

不允许自由发挥到：

- cluttered ecommerce banner
- promotional poster
- xiaohongshu collage
- colorful lifestyle ad
- cartoon pet packaging art

## 11. 可直接使用的 Prompt 模板

以下模板用于后台运营或 AI 自动生成商品图时直接复制使用。

### 11.1 列表页商品图模板

适用：

- 商城列表商品封面

Prompt：

```text
Create a premium product image for a pet product in a calm editorial still-life style.
The product should be the clear visual center, shown on a white or warm off-white background with generous negative space.
Use soft natural lighting, subtle grounded shadow, minimal props, low saturation colors, and a clean Apple-Store-like presentation.
The image should feel elegant, quiet, refined, and trustworthy.
No text overlay, no promotional labels, no infographic elements, no busy background, no clutter.
Aspect ratio 4:5.
```

Negative Prompt：

```text
discount tags, coupon labels, ecommerce banner, promotional poster, xiaohongshu collage, text overlay, infographic layout, high saturation colors, red sale sticker, cluttered props, messy desktop, cartoon style, cute sticker style, neon colors, dark dramatic background
```

### 11.2 详情页首屏主图模板

适用：

- 商品详情页首屏海报区

Prompt：

```text
Create a hero product poster image for a pet product in a premium minimal commercial still-life style.
The product should appear larger and more iconic than a normal ecommerce image, centered on a very clean white or warm off-white background.
Use soft studio lighting, subtle floor shadow, elegant composition, generous breathing room, and calm Apple-Store-like visual language.
The image should feel refined, premium, minimal, and emotionally warm without being cute or decorative.
No text overlay, no promotional graphics, no clutter, no strong gradients.
Aspect ratio around 3:4.
```

Negative Prompt：

```text
sale banner, bold price label, coupon, text overlay, infographic, crowded composition, many props, strong color contrast, neon style, dramatic dark background, xiaohongshu cover style, ecommerce first-image style
```

### 11.3 详情页正文海报图模板

适用：

- 产品介绍段落
- 配方 / 工艺 / 使用方式 / 适用对象说明图

Prompt：

```text
Create an editorial product story image for a pet product detail page.
The composition should feel like a premium product booklet page: clean, calm, minimal, and well-spaced.
Use a white, warm off-white, or pale neutral background.
The product can be shown alone or with one or two subtle supporting elements to explain usage, ingredients, or lifestyle context.
Keep the scene elegant and restrained, with soft natural lighting, low saturation, and clear visual hierarchy.
No text overlay, no promotional stickers, no infographic layout, no clutter.
```

Negative Prompt：

```text
busy lifestyle scene, messy home environment, colorful ad style, promotional graphics, text overlay, cheap ecommerce detail image, discount labels, infographic style, cartoon effect, exaggerated props
```

## 12. 推荐关键词与禁用词

### 12.1 推荐关键词

可以优先使用：

- premium product photography
- minimal still life
- calm editorial
- clean commercial still life
- soft studio lighting
- warm off-white background
- subtle shadow
- elegant negative space
- refined composition
- apple-store-like product presentation
- understated luxury
- quiet premium aesthetic
- neutral palette
- warm ivory background
- soft beige tone
- muted blue grey accent
- restrained lavender accent

### 12.2 禁用词

尽量避免：

- hot sale
- best seller
- promotional poster
- eye-catching
- vibrant color pop
- colorful collage
- social media ad
- banner style
- flashy
- cute sticker
- kawaii
- dramatic lighting
- luxury neon

## 13. 运营与审核流程建议

后续后台使用 AI 生成商品图时，建议流程如下：

1. 先根据商品类型选择正确模板

## 14. 当前前端真实生效字段

这部分非常关键。

后续后台维护时，不要假设“数据库里有字段，前端就一定展示”。当前版本前端实际使用到的商品字段如下：

### 14.1 商城列表页当前真实使用字段

页面：`apps/frontend/miniprogram/pages/shop/shop.*`

当前真实使用：

- `product.name`
- `product.price`
- `product.coverUrl`

当前未使用：

- `summary`
- `imageUrls`
- 详情长图
- 图中内嵌文案

结论：

- 商城列表页的视觉质量，几乎完全由 `coverUrl` 和 `name` 决定
- `coverUrl` 一旦做错，整个商品流都会显得不稳定

### 14.2 商品详情页当前真实使用字段

页面：`apps/frontend/miniprogram/pages/product/detail.*`

当前真实使用：

- `product.name`
- `product.summary`
- `product.price`
- `product.coverUrl`
- `product.specGroups[]`
- `product.highlights[]`
- `product.storySections[]`
- `product.usageNote`

当前仍未直接进入详情正文叙事区的字段：

- `imageUrls`
- 未映射到 `storySections[].imageUrl` 的介绍图
- 任意塞进长文案字段的“详情长图说明”

也就是说：

- 你现在在后台维护 `imageUrls`，不会自动替代正文分段内容
- 详情页正文已经支持后台结构化配置，但必须按 `specGroups / highlights / storySections / usageNote` 这套字段维护

### 14.3 数据库字段上限与 UI 运营上限不是一回事

数据库当前字段上限：

- `product.name`: `VARCHAR(100)`
- `product.summary`: `VARCHAR(200)`
- `product.cover_url`: `VARCHAR(500)`
- `product.image_urls`: `TEXT`

但真正应遵守的是 **UI 运营上限**，不是数据库技术上限。数据库能存很长，不代表页面适合展示那么长。

## 15. 当前后台维护必须遵守的文案规则

### 15.1 商品名称 `name`

用途：

- 商城列表页标题
- 商品详情页主标题

当前前端风险：

- 商城列表页没有强制截断高度保护
- 名称过长会直接拉高卡片高度，破坏双列节奏

运营规则：

- 推荐长度：`8 - 16` 个中文字符
- 理想长度：控制在 `12` 个中文字符左右
- 硬上限建议：不超过 `18` 个中文字符

写法要求：

- 用“品类 + 核心特征”表达
- 一次只讲一个主卖点
- 不叠加多个修饰词

推荐写法：

- `低温烘焙鸡肉主粮`
- `益生菌营养补充片`
- `互动逗猫机礼盒`

不推荐写法：

- `全新升级宠物专用高适口性低温烘焙鸡肉全价全阶段营养主粮`
- `猫狗通用超值爆款益生菌营养补充咀嚼片买一送一`

禁止：

- 价格词进标题
- `爆款`、`限时`、`买赠`、`旗舰款` 这类运营话术
- Emoji
- 多个斜杠并列堆信息

### 15.2 商品简介 `summary`

用途：

- 商品详情页副标题

运营规则：

- 推荐长度：`16 - 30` 个中文字符
- 理想状态：一条完整短句
- 硬上限建议：不超过 `36` 个中文字符

写法要求：

- 只写一句话
- 表达“是什么 + 气质/用途”
- 不写参数表
- 不写促销信息

推荐写法：

- `稳定、轻负担的日常主食选择。`
- `温和补充肠胃日常所需营养。`
- `适合居家陪玩与单猫释放精力。`

不推荐写法：

- `富含益生菌、维生素、矿物质、氨基酸等多重营养成分，适口性好，限时特惠中`

### 15.3 当前后台化详情正文文案规则

商品详情正文已经按结构化字段驱动，后台维护时直接按下面的字段长度控制：

- `highlights[]`：每条 `14 - 26` 个中文字符，建议 2 到 4 条
- `storySections[].title`：`6 - 14` 个中文字符
- `storySections[].description`：`24 - 48` 个中文字符
- `usageNote.title`：`6 - 12` 个中文字符
- `usageNote.content`：`40 - 100` 个中文字符

## 16. 当前封面图的精确适配规则

### 16.1 为什么当前封面图最关键

当前版本里，`coverUrl` 同时承担两件事：

1. 商城列表页商品封面
2. 商品详情页首屏主视觉

这意味着它必须同时适配：

- 列表页：横向裁切、`aspectFill`
- 详情页：竖版展示、`aspectFit`

所以封面图不能按传统电商“列表图”和“详情首图”各自独立的思路做。

### 16.2 当前单封面模式的推荐制作标准

统一推荐：

- 母图比例：`4:5`
- 母图尺寸：`1600 x 2000`
- 主体单品居中
- 背景纯净，优先暖白或浅米白
- 产品主体占画面高度 `56% - 68%`
- 左右最少留白：`12%`
- 上下最少留白：`14%`

这样做的目的：

- 详情页中不会显得太小
- 列表页横裁时，大概率裁掉的是背景留白而不是产品主体

### 16.3 商城列表页裁切安全区

商城列表当前商品图容器接近横向比例 `1.40:1`，且使用 `aspectFill`。

因此封面图必须满足：

- 产品主轮廓一定在画面中轴
- 不允许把主体放在上三分之一或下三分之一
- 不能使用顶部有文字、底部有标签的封面
- 不要把商品放得太满，否则横裁后会显得局促

最安全的理解：

`让列表页裁掉的是“空气”和背景，不是商品。`

### 16.4 商品详情页首屏安全区

商品详情首图当前是大面积浅底海报容器，图片本身使用 `aspectFit`。

因此封面图必须满足：

- 主体最好是单品或极少辅助物
- 背景必须干净
- 图片边缘不要自带深色场景或复杂环境
- 不要上传一张已经做满版版式设计的宣传图

如果上传的是复杂场景图，详情页会出现：

- 主体偏小
- 上下留白失控
- 画面不像“产品海报”，而像“缩略图被放大”

## 17. 商品介绍图片的后台维护规则

### 17.1 当前接入方式

当前商品详情页正文中的说明图，必须通过 `storySections[].imageUrl` 接入。

这意味着你在后台维护介绍图时要明确两件事：

1. 只有挂到 `storySections[].imageUrl` 的图片才会进入正文对应卡片
2. `imageUrls` 仍是商品图库，不等于正文展示顺序

### 17.2 未来详情介绍图建议统一规格

建议统一准备两类图：

#### A. 竖版单主题说明图

适用：

- 配方说明
- 工艺说明
- 使用方式
- 适用阶段

推荐规格：

- 比例：`4:5`
- 尺寸：`1440 x 1800` 或 `1600 x 2000`

#### B. 横版轻场景说明图

适用：

- 双物料对照
- 使用步骤轻演示
- 原料关系展示

推荐规格：

- 比例：`4:3`
- 尺寸：`1600 x 1200`

### 17.3 详情介绍图内容组织规则

一张图只表达一个主题。

推荐拆法：

1. `产品核心形态`
2. `核心工艺 / 配方`
3. `适用对象 / 使用建议`

不要把以下内容塞进一张图：

- 配方
- 成分
- 用法
- 卖点
- 参数
- 促销

如果你把所有东西做进一张图，前端即使排版不乱，视觉也会立刻变成传统电商详情页。

### 17.4 商品介绍图中是否允许文字

当前建议：

- 默认不放字
- 必须放字时，也只允许极少量静态说明

允许：

- 一句很短的工艺说明
- 一句很短的使用提示

不允许：

- 多段说明
- 价格
- 参数表
- 优惠词
- 角标
- 信息图式说明

## 18. 文件格式、命名与上传规则

### 18.1 文件格式

推荐：

- 常规商品图：`JPG`
- 需要透明背景时：`PNG`

如果后台后续支持并验证稳定，也可以统一用更小体积的格式，但前提是前端预览与裁切行为不变。

### 18.2 文件体积建议

为了避免小程序首屏图片过重，建议：

- `coverUrl`：尽量控制在 `300KB - 500KB`
- 详情介绍图：尽量控制在 `400KB - 800KB`

原则：

- 先保证清晰
- 再尽量压缩
- 不要为了省体积把边缘压糊

### 18.3 命名规则

推荐命名：

- `product_{id}_cover_v1.jpg`
- `product_{id}_detail_01.jpg`
- `product_{id}_detail_02.jpg`
- `product_{id}_detail_03.jpg`

不要：

- `1.jpg`
- `最终版.jpg`
- `最新最终版2.jpg`

## 19. 后台发布前检查清单

每次上新或替换素材前，至少检查以下项目：

### 19.1 封面图检查

- 商品主体是否在中心安全区
- 商城列表横裁后是否仍完整可识别
- 商品详情首屏是否仍然像单品海报
- 图中是否没有价格、角标、促销词、贴纸
- 背景是否足够干净
- 背景色是否仍属于暖白 / 米白 / 雾灰这类中性体系
- 是否只有一个辅助色，而不是多色竞争
- 包装颜色与背景是否互相托举，而不是撞色打架

### 19.2 文案检查

- 商品名称是否在推荐字数内
- 商品名称是否只有一个核心卖点
- `summary` 是否只有一句话
- 是否没有促销词、夸张词、参数堆叠

### 19.3 详情介绍图检查

- 一张图是否只讲一件事
- 是否没有把长段文字写进图片
- 是否没有复杂拼贴和电商促销风

### 19.4 系列搭配检查

- 同一商品的一组图片是否属于同一色板家族
- 封面图和详情图的光线方向是否一致
- 是否有某一张图突然变得很艳、很暗、很满
- 是否出现“单张图很好看，但放进页面就很吵”的情况

## 20. 如果你要把商品详情正文彻底交给后台维护

当前前后端已经按下面这套结构预留并接通了商品详情正文能力。

后续要稳定维护时，不要再把内容继续塞进单个 `summary` 或模糊复用 `imageUrls`：

推荐结构：

```json
{
  "coverUrl": "...",
  "summary": "稳定、轻负担的日常主食选择。",
  "highlights": [
    "低温烘焙，保留更自然的风味与适口性。",
    "温和主食配方，适合作为稳定的日常选择。"
  ],
  "storySections": [
    {
      "title": "温和日常配方",
      "description": "适合作为稳定、轻负担的日常主食选择。",
      "imageUrl": "..."
    },
    {
      "title": "低温烘焙，风味更自然",
      "description": "减少高温处理带来的风味损失，让日常喂养更轻松。",
      "imageUrl": "..."
    }
  ],
  "usageNote": {
    "title": "适用对象 / 使用建议",
    "content": "适合全阶段猫咪的日常喂养，可根据体型与活动量灵活调整。"
  }
}
```

只有把字段拆清楚，你后续在后台维护商品图片和商品介绍文案时，才不会不断碰到：

- 有图但前端不显示
- 文案很长但不知道放哪里
- 列表页和详情页互相牵连
- 一张封面图兼顾不了两个场景

## 21. 当前推荐执行口径

如果你现在就要开始批量维护商品数据，最稳妥的执行方式是：

1. 先把 `coverUrl` 按当前单封面模式统一制作
2. 严格控制 `name` 和 `summary` 的字数
3. 规格选择统一维护到 `specGroups`
4. 正文卖点与介绍图统一维护到 `highlights / storySections / usageNote`
5. `imageUrls` 只当商品图库，不承担正文排版语义

一句话总结：

`现在最重要的是用结构化字段维护封面、规格、正文卖点和介绍图，不要再靠前端模板文案或模糊图库字段兜底。`
2. 用模板生成高分辨率母图
3. 再按列表页 / 详情页首屏 / 详情页正文做比例裁切
4. 人工按审核清单检查
5. 不符合风格的图直接重生，不做勉强使用

## 14. 人工审核清单

每张图在进入系统前，至少检查以下问题：

- 看起来像产品画册，而不是促销海报
- 主体是否足够清楚
- 留白是否足够
- 颜色是否低饱和且稳定
- 是否存在高饱和、多色竞争
- 是否有营销文案入图
- 是否有价格、优惠、标签直接进图
- 是否有杂乱场景或多余道具
- 是否与当前商城列表页和详情页气质一致
- 放进页面后会不会显得太花、太闹、太满

只要其中任意一项不满足，就不应直接上线使用。

## 15. 最终原则

如果运营或 AI 在执行中有分歧，优先遵守以下判断顺序：

1. 是否破坏了当前小程序的简约高级感
2. 是否让页面变得更像传统电商
3. 是否让图片比页面 UI 更抢眼
4. 是否因为想“更吸睛”而牺牲整体一致性

最终默认结论是：

`宁可更克制，也不要更热闹。`
