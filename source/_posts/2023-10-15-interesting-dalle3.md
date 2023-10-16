---
layout: post
title: 好玩到停不下来的 DALL-E 3
date: 2023-10-15 15:34:19
description: 好玩到停不下来的 DALL-E 3
keywords: dall-e3, openai, chatgpt
comments: true
categories: ai
tags: [dall-e3, openai, chatgpt]
---

{% img /images/post/2023/10/dalle3.png 400 300 %}

最近 ChatGPT 对 Plus 用户逐步开放一些多模态的功能，包括 DALL-E 3（图像生成）、 GPT-4V（图像识别），等，很多网友乐此不疲地对这些新功能进行试用，目前已经解锁了不少有趣的玩法，笔者将这些好玩的功能进行了整理并介绍给大家，希望能给大家带来一些灵感。

<!--more-->

## 功能介绍

ChatGPT 新推出了图像识别、图像生成以及语音对话等多模态的功能，目前该功能只开放给 Plus 用户，而且只能在 ChatGPT 的 Web 页面和 APP（iOS 和安卓） 上使用。OpenAI 还没有提供这些功能的 API 接口，但据说未来会开放。

### DALL-E 3

DALL-E 3 是 ChatGPT 最新推出的文本到图像生成系统，其通过集成了 ChatGPT 来为用户提供更为优化的图像生成体验。DALL-E 3 不仅能够根据文本描述创建非常详细和准确的图像，而且还具备了显著的安全性提升，例如能够拒绝对公众人物的生成请求，以及对可能产生的有害内容（如暴力、成人或仇恨内容）进行了一定程度的控制。此外，它也通过与 ChatGPT 的集成，使得用户能够更为精准地调整生成的图像，而无需具备丰富的提示知识。通过这些优化和新的集成功能，DALL-E 3 在图像再现、结果的准确性和内容过滤等方面相比前一版本有了显著的提高。

相比 Stable Diffusion，DALL-E 3 在使用上非常简单，只需要一句简单的文字描述即可生成期望的图片，不必像前者一样还要提供复杂的**咒语**。

DALL-E 3 功能不仅可以在 ChatGPT 中使用，还可以在 Bing 中免费使用它，但是在 Bing 中使用有每天使用限额，这是它的[使用地址](https://www.bing.com/images/create)。在 ChatGPT 中使用的话需要申请开通，可以通过这个[申请地址](https://docs.google.com/forms/d/e/1FAIpQLSdiJK7uFzjfIqbH9wGeXnmR3rwjnLUMwih_W5Cet0FVdv8-aQ/viewform)进行申请，申请后大概 2 个小时左右就可以在 ChatGPT 的 Web 页面和 APP 上使用了。

## DALL-E 3 玩法

目前已经有不少网友在尝试 DALL-E 3 各种有趣的玩法，下面我们就来看看这些有意思的功能。

### 漫画创作

ChatGPT 刚推出 DALL-E 3 功能不久，就有网友用它在 15 分钟内画了一副漫画。

{% img /images/post/2023/10/dalle3-comics.jpeg 600 400 %}

生成图片的提示词是：

> "A comic strip in which a boy discovers a mysterious egg that hatches into a baby dinosaur."
> (这是一部连环画，一个男孩发现了一个神秘的蛋，蛋孵化成一只小恐龙。)

图片生成后，有些图片的顺序可能是不对的，可以使用 PS 等工具进行手动调整，另外 DALL-E 3 生成图片中文字的效果一般都不太理想，可能也需要自己手工调整，但对比以前的漫画创作，这已经提升了不少效率。

### 生命周期图像

使用 DALL-E 3 生成生命周期的图片，展示一种事物从开始到最终形态的过程，以下是效果图（后面的图片都会加上中英文提示词）：

{% img /images/post/2023/10/dalle3-life1.jpeg 600 400 %}

> Four images side by side illustrating the life cycle of a cartoon dragon: A tiny orange reptilian. It grows larger and stronger. A fierce dragon-like creature with wings and roaring flames. An old and majestic dragon.
> (四张并排的图像展示了卡通龙的生命周期：一种微小的橙色爬行动物。它变得更大更强。一种凶猛的龙一样的生物，长着翅膀和咆哮的火焰。一条古老而威严的龙。)

{% img /images/post/2023/10/dalle3-life2.jpeg 600 400 %}

> Four images side by side illustrating the life cycle of a snowman: Pristine empty snow-covered field. Three stacked snowballs, undecorated. Snowman with carrot nose, coal eyes, and hat. Melting figure; fallen hat, soaked scarf.
> (四幅并排的图片展示了雪人的生命周期：原始的、空荡荡的被雪覆盖的领域。三个堆叠的未装饰的雪球。雪人带着胡萝卜鼻子、煤炭眼睛和帽子。融化的身影；掉落的帽子，湿透的围巾。)

{% img /images/post/2023/10/dalle3-life3.jpeg 600 400 %}

> Four images side by side illustrating the life cycle of a rose: A small, tight green bud with tiny leaves. The bud starts to unfurl, revealing delicate petals and a richer hue. A full bloom, radiant in its prime, petals spread out in all their glory. Fading elegance as petals wilt, turn brown, and fall.
> (四幅并排的图片展示了玫瑰的生命周期：一个小而紧密的绿色花蕾，带有细小的叶子。花蕾开始展开，呈现出精致的花瓣和丰富的色彩。完全绽放时，花朵处于它的巅峰，花瓣绽放出全部的光彩。花瓣逐渐凋谢，变为棕褐色，并最终飘落，渐渐失去优雅的姿态。)

### 分解图

使用 DALL-E 3 生成**Knolling**风格的图片，Knolling 是一种独特的摄影风格，它涉及将不同的物品整齐地排列，使它们相互成 90 度角，然后从上方拍摄它们。这种风格创建了一种非常对称、令眼睛愉悦的外观，同时也让人们能够在一张照片中一次看到许多物品 ​，被拍摄的物品通常是基于某种原因而组合在一起 ​。

提示词模板为：

> Knolling product photo of a [main object] surrounded by [secondary objects], arranged on a clean background.
> (Knolling 产品照片，[主要物品]被[次要物品]包围，排列在干净的背景上。)

需要将其中的`主要物品`和`次要物品`替换成自己想要的物品，主要物品是你要关注的东西，次要物品通常较小，并且与主要物品相关，可以让 ChatGPT 帮你生成一些次要物品。为了让效果更好，可以告诉 ChatGPT 你的主要物品是什么，然后让 ChatGPT 生成 4 到 5 个在视觉上比较吸引人的次要物品，按重要顺序排列，以下是效果图：

{% img /images/post/2023/10/dalle3-knol1.jpeg 600 400 %}

> Knolling product photo of a perfume bottle surrounded by fragrant flower petals, essential oils, cinnamon sticks, and a clear dropper, arranged on a clean background.
> (Knolling 产品照片，香水瓶被芳香的花瓣、精油、肉桂棒和一个透明的滴管包围，排列在干净的背景上。)

{% img /images/post/2023/10/dalle3-knol2.jpeg 600 400 %}

> Knolling product photo of a delicious roast chicken surrounded by rosemary sprigs, lemon halves, garlic bulbs, kitchen twine, and a carving knife arranged on a clean background.
> (Knolling 产品照片，一只美味的烤鸡被迷迭香枝、柠檬半、大蒜球、厨房绳和一把雕刻刀包围，排列在干净的背景上。)

{% img /images/post/2023/10/dalle3-knol3.jpeg 600 400 %}

### PPT 矢量图

使用 DALL-E 3 生成 PPT 矢量图，这个功能可以让你在 PPT 中快速生成一些矢量配图，可以节省不少时间。

提示词模板为：

> [Image Description], flat simple vector illustrations style, vibrant colors, white background.
> ([图片内容]，扁平简洁的矢量插图风格，色彩鲜艳，背景为白色。)

如果要创建多个连贯的图像，则可以添加颜色主题，只需在**背景为白色**之前添加**蓝色主题**，以下是效果图：

{% img /images/post/2023/10/dalle3-ppt1.jpeg 600 400 %}

> smiling woman listening to music sitting at the desk with her laptop, flat simple vector illustrations style, vibrant colors, white background.
> (一个微笑的女人坐在桌子旁，听着音乐，手持笔记本电脑，同样采用扁平简洁的矢量插图风格，色彩鲜艳，背景为白色。)

{% img /images/post/2023/10/dalle3-ppt2.jpeg 600 400 %}

> a professional man and woman chatting at a table with a presentation board behind them, flat simple vector illustrations style, vibrant colors, white background.
> (一个专业的男人和女人坐在桌旁交谈，背后有一个演示板，同样采用扁平简洁的矢量插图风格，色彩鲜艳，背景为白色。)

{% img /images/post/2023/10/dalle3-ppt3.jpeg 600 400 %}

> a happy man jogging with a blue t-shirt, flat simple vector illustrations style, vibrant colors, white background.
> (一个快乐的男人穿着蓝色 T 恤在慢跑，采用扁平简洁的矢量插图风格，色彩鲜艳，背景为白色。)

### 贴纸设计

使用 DALL-E 3 进行贴纸设计，可以快速生成同系列的多种不同贴纸，也可以生成一张特定主体的贴纸。

下面是生成多个小贴纸的提示词模板：

> 9 different stickers featuring [objects] with vibrant colors and white borders on a minimal background.
> (共有 9 个不同的贴纸，以[物体]为主题，色彩鲜艳，边框为白色，背景简约。)

有时生成的贴纸有边框，如果你不希望有边框，请在提示词中添加**Die Cut**一词。你可以在提示中写多个物体，比如：疯狂的忍者猫与鸡打架，圣诞树、驯鹿、圣诞球和雪花。另外你还可以将这些贴纸做成表情包，只要将你喜欢的贴纸截图保存成图片，然后再导成表情包就可以了，以下是效果图：

{% img /images/post/2023/10/dalle3-sticker1.jpeg 600 400 %}

> 9 different stickers featuring cacti and succulents with vibrant colors and white borders on a minimal background
> (共有 9 个不同的贴纸，以多肉植物和仙人掌为主题，色彩鲜艳，边框为白色，背景简约。)
> 9 different stickers featuring crazy ninja cats fighting chickens with vibrant colors and white borders on a minimal background
> (共有 9 个不同的贴纸，以疯狂的忍者猫与鸡打架为主题，色彩鲜艳，边框为白色，背景简约。)
> 9 different stickers featuring desserts, fruits and cakes with vibrant colors and white borders on a minimal background
> (共有 9 个不同的贴纸，以甜点、水果和蛋糕为主题，色彩鲜艳，边框为白色，背景简约。)
> 9 different stickers featuring Christmas trees, reindeer, baubles and snowflakes with vibrant colors and white borders on a minimal background
> (共有 9 个不同的贴纸，以圣诞树、驯鹿、圣诞球和雪花为主题，色彩鲜艳，边框为白色，背景简约。)

下面是特定主体贴纸的提示词模板：

> Custom sticker design on an isolated white background with the words ["Rachel"] written in an [elegant] font decorated by [watercolor butterflies, daisies and soft pastel hues].
> (在一个孤立的白色背景上，定制贴纸设计，使用[字体]的字体书写[主题]，并装饰着[装饰物]。)

只需用最喜欢的任何内容替换上面的提示词内的对象即可，以下是效果图：

{% img /images/post/2023/10/dalle3-sticker2.jpeg 600 400 %}

> Custom sticker design on an isolated white background with the words "Rachel" written in an elegant font decorated by watercolor butterflies, daisies and soft pastel hues.
> (在一个孤立的白色背景上，定制贴纸设计，使用优雅的字体书写“Rachel”，并装饰着水彩蝴蝶、雏菊和柔和的粉彩色调。)
> Custom sticker design on an isolated white background with the bold words "Oliver" with a backdrop of a mountain range, and silhouettes of pine trees at sunset.
> (在一个孤立的白色背景上，定制贴纸设计，使用粗体字书写“Oliver”，背景是一片山脉，夕阳下的松树剪影。)
> Custom sticker design on an isolated black background with the words "TheLegend27" in bold font decorated by mythical dragons and a flaming sword.
> (在一个孤立的黑色背景上，定制贴纸设计，使用粗体字书写“TheLegend27”，并装饰着神话般的龙和一把燃烧的剑。)
> Custom sticker design on an isolated white background with the cursive words "Victoria" written in an elegant font decorated by roses and gold leaf.
> (在一个孤立的白色背景上，定制贴纸设计，使用草书字体书写“Victoria”，并装饰着玫瑰和金箔。)

### 游戏人物全方位图

使用 DALL-E 3 生成游戏人物全方位图，可以同时生成游戏人物的正面，侧面和背面的视图。

提示词模板为：

> I'd like to generate character designs in a wide resolution set within a Chibi Pixel Art RPG context. For each character, please adhere to the following guidelines:
> Three Views: Every character should be depicted with three distinct views:
> Side View (Essential): Start with this view - it is of paramount importance. The side view should capture the character's full profile from the tip of their nose to the back of their head. This view should not be ignored or skipped.
> Front View: The character should be standing straight, looking forward.
> Back View: This view should portray the character from the rear.
> Magical Fantasy RPG Themes: Provide characters based on unique themes fitting within a magical fantasy RPG world. Do not include or describe specific objects, accessories, or intricate details; only provide the theme and let the design be interpreted based on that theme.
> (我想在一个 Chibi 像素艺术风格的 RPG 背景中生成角色设计，分辨率要宽。对于每个角色，请遵循以下准则：
> 三个视角：每个角色应该用三个不同的视角来描绘：
> 侧面视图（必要）：从这个视角开始 - 这是至关重要的。侧面视图应该捕捉到角色从鼻尖到头后部的完整轮廓。这个视角不应被忽视或跳过。
> 正面视图：角色应该直立站立，向前看。
> 背面视图：这个视角应该描绘角色的背面。
> 魔幻奇幻 RPG 主题：根据适合魔幻奇幻 RPG 世界的独特主题提供角色。不要包括或描述具体的物体、配饰或复杂的细节；只提供主题，让设计根据主题进行解释。)

**Chibi** 是一个来自日语的术语，通常用于描述可爱、小型或略带夸张的卡通角色设计。在日本动画和漫画中，Chibi 风格的角色通常具有大头、大眼和小身体的特点，这种风格被设计成可爱和幽默，以吸引观众的注意。

这个提示词并不能保证 100% 正确的效果，要生成这种图片难度比较大，其中最难部分是侧面视图的生成，有时候会生成 30° 或者 60° 的侧面，如果你希望侧面的人物与其他方向的视图人物保持一致，建议提示词中不要写太多细节，如果发现生成的图片效果不太好就要重新开始一个对话，如果发现效果还可以，那可以对 GPT 说：`很好，同样风格的图片再给我来 4 张`，这样就可以生成更多效果好的游戏人物图，以下是效果图：

{% img /images/post/2023/10/dalle3-player1.jpeg 600 400 %}

{% img /images/post/2023/10/dalle3-player2.png 600 400 %}

### 儿童读物

使用 DALL-E 3 生成一些儿童读物图片，如果觉得效果不好，可以使用 PS 等其他工具辅助修改图片，以下是效果图：

{% img /images/post/2023/10/dalle3-child1.jpeg 600 400 %}

> Illustration: A droplet's universe comes alive under the microscope. Protozoa with their delicate structures, vibrant algae, dynamic bacteria, and enigmatic viruses are all on display. Dominating the scene, a vibrant label proclaims: 'Protozoa, Algae, Bacteria, Virus'.
> (插图：在显微镜下，一滴液滴的宇宙变得生动起来。原生动物以其精细的结构、充满活力的藻类、充满活力的细菌和神秘的病毒都展示出来。在场景中占主导地位的是一个充满活力的标签，上面写着：“原生动物、藻类、细菌、病毒”。)
> Illustration: An artful depiction of a butterfly's progression. Beginning with an egg amidst nature's embrace, a caterpillar in its growing phase, a chrysalis awaiting transformation, to a butterfly unveiling its beauty. Text: 'Egg, Caterpillar, Chrysalis, Butterfly'.
> (插图：一只蝴蝶进化的艺术描绘。从自然的怀抱中孵化的蛋开始，到生长阶段的毛虫，等待变形的蛹，再到展示美丽的蝴蝶。文字：“蛋、毛虫、蛹、蝴蝶”。)
> Illustration: A serene orchard scene where an apple has just fallen into a basket below. Character 1, surprised by the sudden drop, wonders, 'Random?'. Character 2, looking at the tree, states,' Gravity at work.
> (插图：一个宁静的果园场景，一个苹果刚刚掉落到下面的篮子里。角色 1 对突然的掉落感到惊讶，想着：“随机的吗？”角色 2 看着树，说：“这是重力在起作用。”)

### T 恤图

使用 DALL-E 3 很好地进行 T 恤图设计，左边是 T 恤图案的图片，右边是模特穿上 T 恤的图片，提示词模板为：

> Wide image of a [Halloween]-themed design on the left side, showcasing [a playful black cat with a witch hat atop carved pumpkins]. On the right side, a model wearing a t-shirt with the same [playful cat] design.
> (在左侧是一个以 [万圣节] 为主题的宽幅图像，展示了 [一个戴着女巫帽的顽皮黑猫坐在雕刻的南瓜上]。在右侧，一个模特穿着印有相同 [顽皮猫] 设计的 T 恤。)

可以将提示词中括号的内容换成你喜欢的内容，以下是效果图：

{% img /images/post/2023/10/dalle3-tshirt.jpeg 600 400 %}

### 游戏封面

使用 DALL-E 3 生成游戏封面，DALL-E 3 很适合做游戏宣传材料，提示词模板为：

> Create a promotional image or title screen for a popular mobile game. It's called [insert name of app and details]. Wide resolution.
> (为一款热门的手机游戏创建一个推广图片或标题屏幕。游戏名为 [插入应用名称和详细信息]。宽屏分辨率。)

以下是效果图：

{% img /images/post/2023/10/dalle3-game1.jpeg 600 400 %}

{% img /images/post/2023/10/dalle3-game2.jpeg 600 400 %}

{% img /images/post/2023/10/dalle3-game3.jpeg 600 400 %}

### 像素游戏场景

有网友发现 DALL-E 3 很适合用来生成像素艺术图片，以下是同一个提示词，分别用 Adobe 的 Firefly 2、Midjourney 和 DALL-E 3 生成的像素图片，可以看到 DALL-E 3 效果是最好的，Midjourney 甚至都没有像素的效果：

{% img /images/post/2023/10/dalle3-pixel1.jpeg 600 400 %}

> Chibi pixel art game asset for an rpg game, on a white background, featuring the armor of a dragon sorcerer, wielding the power of fire surrounded by a matching item set
> (在白色背景上，为 RPG 游戏创建一个 Chibi 像素艺术游戏素材，展示一位龙巫师的盔甲，手持火焰之力，并被相配的物品套装所环绕。)

因此有网友用 DALL-E 3 来制作像素游戏场景，效果相当不错，提示词模板为：

> SUBJECT: a [subject] | STYLE: 90's RPG screenshot | ANGLE: isometric | PLACE: [place] | TONES: [X]-bit [palette]
> (主题：一个 [主题] | 风格：90 年代 RPG 游戏截图 | 角度：等轴测 | 地点：[地点] | 色调：[X]-位 [调色板])

将其中的主题、地点、X、调色板换成自定义的内容即可，以下是效果图：

{% img /images/post/2023/10/dalle3-pixel2.jpeg 600 400 %}

> SUBJECT: a viking | STYLE: 90's RPG screenshot | ANGLE: isometric | PLACE: village | TONES: 32-bit synthwave
> (主题：一个维京人 | 风格：90 年代 RPG 游戏截图 | 角度：等轴测 | 地点：村庄 | 色调：32 位合成波)

{% img /images/post/2023/10/dalle3-pixel3.jpeg 600 400 %}

> SUBJECT: a witch | STYLE: 90's RPG screenshot | ANGLE: isometric | PLACE: forest | TONES: 8-bit acid green
> (主题：一个女巫 | 风格：90 年代 RPG 游戏截图 | 角度：等轴测 | 地点：森林 | 色调：8 位酸绿色)

{% img /images/post/2023/10/dalle3-pixel4.jpeg 600 400 %}

> SUBJECT: a woman rogue | STYLE: 90's RPG screenshot | ANGLE: isometric | PLACE: federal building | TONES: 16-bit shadows
> (主题：一个女盗贼 | 风格：90 年代 RPG 游戏截图 | 角度：等轴测 | 地点：联邦大楼 | 色调：16 位阴影)

{% img /images/post/2023/10/dalle3-pixel5.jpeg 600 400 %}

> SUBJECT: a vampire | STYLE: 90's RPG screenshot | ANGLE: isometric | PLACE: city | TONES: 16-bit neon
> (主题：一个吸血鬼 | 风格：90 年代 RPG 游戏截图 | 角度：等轴测 | 地点：城市 | 色调：16 位霓虹灯)

## 总结

ChatGPT 目前 DALL-E 3 功能并不是对所有 Plus 用户都开放，只是开发给某些 Plus 用户进行灰度测试，但 OpenAI 公司表示，他们将在未来几周内逐步向所有 Plus 用户开放这些功能。以上就是 DALL-E 3 的一些好玩的功能，如果你有更好的灵感，欢迎在评论区留言，下期我们将继续介绍 GPT-4V 的一些有意思的玩法，敬请期待。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
