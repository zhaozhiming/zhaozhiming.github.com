---
layout: post
title: 学习 ChatGPT 的提示工程（下）
date: 2023-05-01 15:05:42
description: 学习 ChatGPT 的提示工程，跟吴恩达教授学习提示工程
keywords: chatgpt, prompt
comments: true
categories: chatgpt
tags: [chatgpt, prompt]
---
{% img /images/post/2023/05/prompt-lesson.png 400 300 %}

上次我们讲解了 [ChatGPT 提示工程课程](https://www.deeplearning.ai/short-courses/chatgpt-prompt-engineering-for-developers/) 的一部分内容，今天讲继续介绍课程的剩下内容，其实最重要的内容在上次已经介绍了，剩下的内容会比较简单。

<!--more-->

## 迭代开发

像软件开发一样，提示词的开发也是一个迭代的过程，很少有人可以一开始就写出准确且复杂的提示词。我们可以先给模型提供一些简单的提示词，然后根据模型的回答来调整提示词，最终得到我们想要的结果，这个过程如下：。

{% img /images/post/2023/05/iterative.png 600 400 %}

在迭代过程中，我们先尝试想法，然后分析结果为什么不对，接着再修改指令，让模型更多时间去思考，不断重复地用不同的例子来完善提示词。

提示词的指导方针，首先是清晰和明确，然后分析给出的结果为什么与期望不符，接着重新设计想法和提示词，最后重复这个过程。

让我们来看个例子：

```python
fact_sheet_chair = """
概述
- 属于一个美丽的中世纪风格办公家具系列的一部分，包括文件柜、办公桌、书柜、会议桌等。
多种外壳颜色和底座饰面可供选择。
- 可选塑料背面和前部布艺（SWC-100）或全布艺（SWC-110），有 10 种布料和 6 种皮革选项。
底座饰面选项有：不锈钢、磨砂黑、亮白或镀铬。
- 椅子有扶手和无扶手两种选择。
- 适合家庭或商务环境。
- 符合合同使用标准。

构造
- 5 轮塑料包覆铝基座。
- 气动椅子调节，便于升降。

尺寸
- 宽度 53 厘米 | 20.87 英寸
- 深度 51 厘米 | 20.08 英寸
- 高度 80 厘米 | 31.50 英寸
- 座高 44 厘米 | 17.32 英寸
- 座深 41 厘米 | 16.14 英寸

选项
- 软地板或硬地板脚轮选项。
- 两种座垫泡沫密度可供选择：中等（1.8 磅/立方英尺）或高（2.8 磅/立方英尺）
- 无扶手或 8 个位置 PU 扶手

材料
外壳底座滑行器
- 带有改性尼龙 PA6/PA66 涂层的铸铝。
- 外壳厚度：10 毫米。
- 座位
- HD36 泡沫

原产国
意大利
"""

prompt = f"""
您的任务是帮助市场营销团队根据技术资料表为零售网站的产品撰写描述。

根据由三个反引号分隔的技术规格中提供的信息编写产品描述。

技术规格：```{fact_sheet_chair}```
"""
response = get_completion(prompt)
print(response)

# 输出结果
# 这款产品是美丽的中世纪风格办公家具系列的一部分，包括文件柜、办公桌、书柜、会议桌等。它有多种外壳颜色和底座饰面可供选择。您可以选择塑料背面和前部布艺（SWC-100）或全布艺（SWC-110），有 10 种布料和 6 种皮革选项。底座饰面选项有：不锈钢、磨砂黑、亮白或镀铬。椅子有扶手和无扶手两种选择。这款产品适合家庭或商务环境，并符合合同使用标准。
#
# 这款产品的构造是由 5 轮塑料包覆铝基座组成。它还具有气动椅子调节功能，便于升降。尺寸方面，宽度为 53 厘米，深度为 51 厘米，高度为 80 厘米，座高为 44 厘米，座深为 41 厘米。
#
# 您可以选择软地板或硬地板脚轮选项。此外，还有两种座垫泡沫密度可供选择：中等（1.8 磅/立方英尺）或高（2.8 磅/立方英尺）。您可以选择无扶手或 8 个位置 PU 扶手。
#
# 这款产品的外壳底座滑行器是由带有改性尼龙 PA6/PA66 涂层的铸铝制成。外壳厚度为 10 毫米。座位是由 HD36 泡沫制成。这款产品的原产国是意大利。
```

可能我们会觉得这个句子太长了，可以加上字数限制，让模型生成更短的句子：

```python
prompt = f"""
您的任务是帮助市场营销团队根据技术资料表为零售网站的产品撰写描述。

根据由三个反引号分隔的技术规格中提供的信息编写产品描述。

请使用不超过 50 个字。

技术规格：```{fact_sheet_chair}```
"""
response = get_completion(prompt)
print(response)
# 输出结果
# 中世纪风格办公家具系列的椅子，可选多种颜色和底座饰面，有扶手和无扶手两种选择，适合家庭或商务环境，符合合同使用标准。
```

然后我们想让描述面向某个特定的受众来介绍其特点，比如面向家具零售商：

```python
prompt = f"""
您的任务是帮助市场营销团队根据技术资料表为零售网站的产品撰写描述。

根据由三个反引号分隔的技术规格中提供的信息编写产品描述。

描述面向家具零售商，因此应具有技术性质，关注产品的构造材料。

请使用不超过 50 个字。

技术规格：```{fact_sheet_chair}```
"""
response = get_completion(prompt)
print(response)
# 输出结果
# 中世纪风格办公家具系列的一部分，包括文件柜、办公桌、书柜、会议桌等。可选多种外壳颜色和底座饰面，椅子有扶手和无扶手两种选择。座位采用 HD36 泡沫，底座为 5 轮塑料包覆铝基座，符合合同使用标准。
```

类似这种思路，还可以结合之前学到的将结果输出成不同的格式，比如 HTML 或 JSON，这样通过不断迭代来完成我们最终的提示词。

## 概括

使用 ChatGPT 经常做的一件事情就是拿来概括文本，实际上刚才的例子我们已经演示过了，比如刚才的例子里我们就使用了 2 个概括文本的技术。

* 使用字数限制来概括文本（也可以使用句子数）
* 向某个方面概括文本（也可以是向某个受众）

## 推理

我们还可以使用 ChatGPT 来推断文本，比如我们可以使用 ChatGPT 来推断顾客评价中的满意度，识别其中的情感和情绪，提取其中的关键信息。下面是一段顾客的评价：

```python
lamp_review = """
需要一盏适合卧室的漂亮台灯，\
这款台灯具有额外的储物空间，价格合适。\
收到货速度很快。在运输过程中，灯的绳子断了，\
公司很高兴地寄来了一盏新灯。几天内就收到了。\
组装起来很容易。我发现有一个零件丢失了，\
于是联系了他们的客服，他们很快就给我寄来了丢失的零件！\
Lumina 在我看来是一家非常关心客户和产品的优秀公司！
"""
```

我们来让模型推断一下这段评价的情感：

```python
prompt = f"""
以下产品评论的情感是什么？用一个词表示，\
要么是“正面”，要么是“负面”。\
评论文本用三个反引号分隔。

评论文本：'''{lamp_review}'''
"""
response = get_completion(prompt)
print(response)
# 输出结果
# 正面
```

识别评论中的情绪：
  
```python
prompt = f"""
请确定以下评论的作者所表达的一系列情感。\
列表中不要超过五个项目。\
将您的答案格式化为由逗号分隔的词语列表。

评论文本：'''{lamp_review}'''
"""
response = get_completion(prompt)
print(response)
# 输出结果
# 满意，赞赏，感激，信任，愉快
```

## 转化

### 翻译

我使用 ChatGPT 最多的功能要数转化功能中的翻译了，ChatGPT 的翻译能力从目前来看是最好的，明显要优于其他翻译软件，来看一个一次翻译多种语言的例子：

```python
# 有以下几种语言的用户反馈
user_messages = [
  "La performance du système est plus lente que d'habitude.",
  "Mi monitor tiene píxeles que no se iluminan.",
  "Il mio mouse non funziona",
  "Mój klawisz Ctrl jest zepsuty",
  "My screen is flashing"
] 
# 分析及翻译
for issue in user_messages:
    prompt = f"告诉我这是什么语言：```{issue}```"
    lang = get_completion(prompt)
    print(f"原始信息 ({lang}): {issue}")

    prompt = f"""
    把以下文字翻译成中文和日文：```{issue}```
    """
    response = get_completion(prompt)
    print(response, "\n")

# 输出结果
# 原始信息 （这是法语。): La performance du système est plus lente que d'habitude.
# 中文翻译：系统性能比平时慢。
# 日文翻译：システムのパフォーマンスが通常よりも遅いです。 
#
# 原始信息 （这是西班牙语。): Mi monitor tiene píxeles que no se iluminan.
# 中文翻译：我的显示器有一些像素点不亮。
# 日文翻译：私のモニターには点灯しないピクセルがあります。 
#
# 原始信息 （这是意大利语。): Il mio mouse non funziona
# 中文翻译：我的鼠标不工作。
# 日文翻译：私のマウスが動作しません。 
#
# 原始信息 （这是波兰语。): Mój klawisz Ctrl jest zepsuty
# 中文翻译：我的 Ctrl 键坏了。
# 日文翻译：私の Ctrl キーが壊れています。 
#
# 原始信息 （这是英语。): My screen is flashing
# 中文：我的屏幕在闪烁。
# 日文：私の画面が点滅しています。
```

### 语气转换

也可以使用 ChatGPT 来转换文本的语气，比如下面的例子是将一段口语化的句子转换成一段正式的商务书函：

```python
prompt = f"""
把下面的口语翻译成商业信函：
'哥，我是小王，查下这个台灯的规格。'
"""
response = get_completion(prompt)
print(response)
# 输出结果
# 尊敬的先生/女士，
#
# 我是小王，想向您查询一下这个台灯的规格。请问您能提供相关信息吗？
#
# 谢谢您的帮助。
#
# 此致
#
# 敬礼
#
# XXX
```

### 格式转换

可以使用 ChatGPT 来转换文本的格式，比如下面的例子是将 JSON 数据转换成 HTML 格式：

```python
from IPython.display import display, Markdown, Latex, HTML, JSON

data_json = { "resturant employees" :[ 
    {"name":"Shyam", "email":"shyamjaiswal@gmail.com"},
    {"name":"Bob", "email":"bob32@gmail.com"},
    {"name":"Jai", "email":"jai87@gmail.com"}
]}

prompt = f"""
把下面的 python 字典从 JSON 转换成有字段头和标题的 HTML 表格：
: {data_json}
"""
response = get_completion(prompt)
print(response)

display(HTML(response))
```
输出结果：
{% img /images/post/2023/05/transform.png 600 400 %}

### 拼写和语法检查

还可以使用 ChatGPT 来检查文本的拼写和语法，比如下面的例子：

```python
text = f"""
小丽每天弹钢琴在晚上。
"""
prompt = f"校对并修改这个句子：```{text}```"
response = get_completion(prompt)
print(response)
# 输出结果
# 小丽每天晚上弹钢琴。
```

## 扩展

这节课主要介绍了 ChatGPT 中的一个参数叫`temperature`，它的值在 0 和 1 之间，值越高表示创造性越强，这样会使得每次生成的答案不一样；而值越低表示越严谨，我们之前都将该值设置为 0，这样可以保证我们每次生成的答案都是一样的。

首先需要先修改之前的默认方法`get_completion`，在参数中加入`temperature`：
```python
def get_completion(prompt, model="gpt-3.5-turbo",temperature=0):
    messages = [{"role": "user", "content": prompt}]
    response = openai.ChatCompletion.create(
        model=model,
        messages=messages,
        temperature=temperature, # 将固定值改成传入的参数值
    )
    return response.choices[0].message["content"]
```

然后我们使用一个 AI 客户代理的例子来演示，先给出顾客的评论，以及评论的情感：

```python
sentiment = "负面"

# review for a blender
review = f"""
他们在十一月的季节性促销活动中仍以约 49 美元的价格出售 17 件套装，\
打了约五折，但是在十二月的第二周左右，由于某种原因（可以称之为哄抬价格），\
同一套装的价格都上涨了，大约在 70-89 美元之间。\
11 件套装的价格也比之前的 29 美元上涨了约 10 美元。所以这看起来还不错，\
但是如果你看看底部，锁定刀片的部分看起来不如几年前的旧版好，\
但我打算对它非常温柔（例如，我会先在搅拌机中压碎很硬的东西，\
如豆子、冰、米等，然后将它们研磨成所需的服务大小，\
然后切换到打蛋器刀片制作更细的面粉，并在制作冰沙时首先使用交叉切割刀片，\
然后使用平刀片，如果需要更细/不含浆）。制作冰沙时的特别提示：\
将水果和蔬菜切碎并冷冻（如果使用菠菜-轻微炖软菠菜然后冷冻，\
直到准备使用-如果制作冰沙，使用一个小到中等大小的食品加工器），\
这样你就可以避免加太多的冰-制作冰沙时。大约一年后，\
电机发出奇怪的声音。我打电话给客户服务，但保修已经过期了，\
所以我不得不再买一个。FYI：这类产品的总体质量已经下降了，\
所以他们在依靠品牌认知和消费者忠诚度来维持销售。两天内到货。
"""
```

然后是提示词：

```python
prompt = f"""
您是一位客户服务 AI 助手。
您的任务是回复一位重要的客户的电子邮件。
给定客户电子邮件，以“```”为分隔符，
生成回复以感谢客户的评论。
如果情感是正面或中性的，请感谢他们的评论。
如果情感是负面的，请道歉并建议他们联系客户服务。
请务必使用评论中的具体细节。
以简洁和专业的语气写作。
将电子邮件签名为`AI 客户代理`。

顾客评价：```{review}```
评价情感：{sentiment}
"""
response = get_completion(prompt, temperature=0.7)
print(response)
# 输出结果
# 尊敬的客户，
#
# 非常感谢您对我们产品的评论。我们非常抱歉您在使用我们的产品时遇到了问题。我们建议您联系我们的客户服务，我们将竭尽全力解决您的问题。我们十分重视您的反馈，并将在未来的产品改进中考虑您的建议。
#
# 祝您拥有愉快的购物体验。
#
# AI 客户代理
```

这里的`temperature`我们传了`0.7`，表示创造性偏高，每次运行这段程序都会获得不同的答案，关于 ChatGPT 的其他参数还可以参阅 [这篇文章](https://uxplanet.org/use-chatgpt-like-a-pro-discover-parameters-and-unlock-ai-writing-secrets-8f68a342bdea)。

## 聊天机器人

这一节向我们展示如何通过几行代码来制作一个功能强大的聊天机器人，首先需要增加一个方法`get_completion_from_messages`：

```python
def get_completion_from_messages(messages, model="gpt-3.5-turbo", temperature=0):
    response = openai.ChatCompletion.create(
        model=model,
        messages=messages,
        temperature=temperature,
    )
    return response.choices[0].message["content"]
```
和原来的`get_completion`方法不同的是第一个参数，前者传入的一个是消息列表，后者是一个消息。实际上消息有 2 部分组成，一部分是消息内容，另外一部分是消息的角色。

```json
{'role':'system', 'content':'你是个说话像莎士比亚的助理。'}, 
```

这里的角色有 3 种类型：

* system：系统角色用于设置对话的背景、上下文或对话规则。这个角色可以帮助模型更好地了解对话的背景信息，从而提供更合适的回答。使用场景示例：在对话开始时，设置 "system" 角色以提供一些上下文信息，如：“System: 你正在与一位心理治疗师交谈，他/她会提供一些建议和心理支持。”
* user：用户角色通常扮演提问者或主导者。用户可以向 ChatGPT 提问，寻求建议，或者引导对话。这是大多数场景下的默认角色。使用场景示例：向 ChatGPT 请教编程问题、询问科学知识、获取书籍推荐等。
* assistant：助手角色作为回答者或辅助者。助手的目标是理解用户的需求，提供相关的信息、建议或解决方案。这个角色通常是 ChatGPT 的默认输出角色。使用场景示例：回答用户的问题、提供解决方案、给出专业建议等。

这里演示了一个功能强大的下单机器人，代码示例如下：

```python
import panel as pn  # python 的 GUI 库，记得 pip install
pn.extension()

panels = [] # 展示下单结果

context = [ {'role':'system', 'content':"""
你是下单机器人，一个收集披萨餐厅订单的自动化服务。你首先问候客户，然后收集订单，\
然后询问是自取还是送货上门。\
你等待收集整个订单，然后总结订单并最后检查一遍是否客户想要添加其他任何东西。\
如果是送货上门，你会询问地址。\
最后，你会收款。\
确保澄清所有选项，附加项和大小以唯一确定菜单中的项目。\
你以短小、非常随意和友好的风格回复。\
菜单包括、
意大利辣香肠披萨 12.95、10.00、7.00 \
芝士披萨 10.95、9.25、6.50 \
茄子披萨 11.95、9.75、6.75 \
炸薯条 4.50、3.50 \
希腊沙拉 7.25 \
配料：\
额外奶酪 2.00、\
蘑菇 1.50 \
香肠 3.00 \
加拿大熏肉 3.50 \
AI 酱 1.50 \
胡椒 1.00 \
饮料：\
可乐 3.00、2.00、1.00 \
雪碧 3.00、2.00、1.00 \
瓶装水 5.00
"""} ]  # 消息集合，包括预设，用户和机器人的对话

inp = pn.widgets.TextInput(value="Hi", placeholder='这里输入信息…')
button_conversation = pn.widgets.Button(name="讲话！")

interactive_conversation = pn.bind(collect_messages, button_conversation)

dashboard = pn.Column(
    inp,
    pn.Row(button_conversation),
    pn.panel(interactive_conversation, loading_indicator=True, height=300),
)

dashboard
```

执行程序后出现的界面如下，你可以在界面上输入指令来完成下订单的操作，机器人会按照我们设置好的步骤一步步引导你完成订单的预定：
{% img /images/post/2023/05/orderbot.png 600 400 %}

代码非常简单，核心代码只有 10 行左右，我们只要提供给 ChatGPT 足够的材料就可以了。

## 小结

在学习《ChatGPT 提示工程课程》的过程中，我们认识到 ChatGPT 的强大功能以及如何通过提示工程为各种任务构建高效的应用程序。通过本课程的学习，我们掌握了提示工程的最佳实践和构建自定义聊天机器人的技巧，这对我们的个人和职业发展具有重要意义。

随着大语言模型技术的不断发展和进步，我们可以预见到这一领域将会带来更多的创新和变革。作为开发者，我们需要紧跟技术潮流，不断提升自己的能力，以便在激烈的市场竞争中保持竞争力。

鼓励所有对此领域感兴趣的读者参加《ChatGPT 提示工程课程》，以提高自己的技能并拓宽视野。无论您是初学者还是经验丰富的专业人士，这门课程都将为您提供宝贵的知识和实践经验，助您在人工智能领域取得更大的成功。