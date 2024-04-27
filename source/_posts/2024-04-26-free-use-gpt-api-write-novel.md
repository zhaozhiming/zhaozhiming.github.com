---
layout: post
title: 用 API 方式免费使用 GPT 写小说
date: 2024-04-26 14:32:39
description: 介绍如何以 API 的方式免费使用 OpenAI 的 GPT3.5 模型写小说
keywords: gpt, llm, chatgpt
comments: true
categories: ai
tags: [gpt, llm, chatgpt]
---

{% img /images/post/2024/04/free-gpt.jpeg 400 300 %}

最近 OpenAI 宣布将降低网页版 ChatGPT 的使用门槛，允许没有账号的用户使用，这一好消息使我们可以在网页上免费访问 ChatGPT，但和 API 相比仍然缺乏灵活性，通过 API 开发人员可以编写代码来与 ChatGPT 进行自动化交互。今天我们将介绍如何利用 OpenAI 的这一免费功能，将其转化为 API 的形式来进行使用，并介绍如何通过这种方式来写一篇小说。

<!--more-->

## ChatGPT

最近一段时间，OpenAI 宣布用户无需注册即可使用 ChatGPT，免费使用的模型是 GPT3.5，这是一个非常强大的 LLM（大语言模型），处理日常工作的大多数任务都不在话下。但是没有注册的用户无法享受更多功能，包括保存和查看聊天记录、共享聊天和解锁其他功能，如语音对话和自定义指令等，更多的信息可以查看[这里](https://openai.com/blog/start-using-chatgpt-instantly)。

{% img /images/post/2024/04/use-gpt-without-signup.png 1000 600 %}

## 以 API 方式使用

虽然 OpenAI 提供了免费版的网页 ChatGPT，但如果能用 API 的方式来调用 ChatGPT 将会更加方便，我们可以做的事情也会更多。开源社区就有这么一个工具，可以将网页版 ChatGPT 转化为 API 的形式，这个工具叫做 [ChatGPT](https://github.com/PawanOsman/ChatGPT)，该工具通过反向代理的方式来免费访问 ChatGPT API，支持本地部署，无需提供 OpenAI 的 APIKEY。

该工具的安装非常简单，提供了 2 种安装方式：

### docker 安装

在本地提前安装好 [Docker](https://docs.docker.com/engine/install/)，然后执行以下命令即可启动服务：

```bash
docker run -dp 3040:3040 pawanosman/chatgpt:latest
```

### 源码安装

在本地提前安装好[NodeJs](https://nodejs.org/en/download)，下载源码：

```bash
git clone git clone https://github.com/PawanOsman/ChatGPT.git
```

然后进入目录，执行启动脚本`bash start.sh`即可启动服务。

### 使用

本地服务的地址是：`http://localhost:3040`，可以通过 curl 命令来调用 API，例如：

```bash
curl http://localhost:3040/v1/chat/completions \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [
      {
        "role": "user",
        "content": "Hello!"
      }
    ]
  }'

# 显示结果
{
  "id": "chatcmpl-SKCmI8iJ2cPswvySaCSKX55n8viL",
  "created": 1714120019,
  "model": "gpt-3.5-turbo",
  "object": "chat.completion",
  "choices": [
    {
      "finish_reason": "stop",
      "index": 0,
      "message": {
        "content": "Hi there! How can I assist you today?",
        "role": "assistant"
      }
    }
  ],
  "usage": {
    "prompt_tokens": 2,
    "completion_tokens": 10,
    "total_tokens": 12
  }
}
```

在 curl 命令中，我们没有提供 OpenAI 的 APIKEY 也能正常调用 ChatGPT API，返回的结果也是和真正的 ChatGPT API 一致。

### 注意事项

因为 ChatGPT 的使用有地区限制，如果你所在地区无法访问 OpenAI 的服务，那么在调用接口时本地服务会提示以下错误信息：

```bash
Error getting a new session...
If this error persists, your country may not be supported yet.
If your country was the issue, please consider using a U.S. VPN or a U.S. residential proxy.
```

建议是找一台可以访问 OpenAI 服务的服务器，最好是美国地区的服务器，在上面部署服务。

## 使用 ChatGPT 写小说

有了免费的 ChatGPT API 后，我们就可以使用 ChatGPT 来做很多事情了，AI 现在最擅长的是文字生成，我们可以使用 ChatGPT 来写小说。

这里介绍另外一个开源项目 [gpt-author](https://github.com/mshumer/gpt-author)，它提供了一系列的提示词来帮助我们完成一部小说，并且可以结合 Stable Diffustion 来生成小说封面。多种 LLM 模型可供选择，可以使用 OpenAI 的 GPT 模型，也可以使用 Anthropic 的 Claude 模型。如果想生成的小说效果越好，那么肯定需要能力越强的模型，比如 GPT4 或者 Claude，但这些模型目前都需要收费，所以我们可以先使用我们之前搭建的免费 ChatGPT API 来试试效果。

gpt-author 的仓库主要是一个 ipynb 文件，里面包含了如何使用 ChatGPT 来写小说的代码，可以直接在本地运行，也可以在 Google Colab 上运行，下面我们来看下里面的代码并介绍其作用。

代码中包含了如何使用 Anthropic 的 API，但我们只需要调用我们部署的 ChatGPT API 即可，所以涉及到 Anuthropic 的代码这里就不做介绍。

```py
import openai
import os
from ebooklib import epub
import base64
import os
import requests

openai.api_key = "YOUR OPENAI KEY" # You can enter any string here
openai.api_base = "http://localhost:3040/v1"
stability_api_key = "YOUR STABILITY KEY" # get it at https://beta.dreamstudio.ai/
```

- 如果是在本地运行，需要提前安装相关的 python 库：`pip install openai ebooklib requests`
- `openai.api_key` 这里可以随便填写一个字符串，因为我们的 ChatGPT API 不需要 APIKEY
- `openai.api_base` 这里填写我们部署的 ChatGPT API 地址，如果是在本地运行，填写`http://localhost:3040/v1`，注意不要遗漏地址最后的`/v1`
- `stability_api_key` 这里是 Stability Diffusion 的 APIKEY，用来生成小说封面的，如果不需要生成封面，可以不填写

设置好 API 服务参数后，我们再来看其中的写小说的主方法`write_fantasy_novel`：

```py
import ast

def write_fantasy_novel(prompt, num_chapters, writing_style, claude_true=False):
    plots = generate_plots(prompt)
    print('generated plots')

    best_plot = select_most_engaging(plots)
    print('selected best plot')

    improved_plot = improve_plot(best_plot)
    print('plot improved')

    title = get_title(improved_plot)
    print('title generated')

    storyline = generate_storyline(improved_plot, num_chapters)
    print('storyline generated')
    chapter_titles = ast.literal_eval(storyline)
    novel = f"Storyline:\n{storyline}\n\n"

    first_chapter = write_first_chapter(storyline, chapter_titles[0], writing_style.strip(), claude_true)
    print('first chapter written')
    novel += f"Chapter 1:\n{first_chapter}\n"
    chapters = [first_chapter]

    for i in range(num_chapters - 1):
        print(f"Writing chapter {i+2}...") # + 2 because the first chapter was already added

        chapter = write_chapter(novel, storyline, chapter_titles[i+1])
        try:
          if len(str(chapter)) < 100:
            print('Length minimum not hit. Trying again.')
            chapter = write_chapter(novel, storyline, chapter_titles[i+1])
        except:
          chapter = write_chapter(novel, storyline, chapter_titles[i+1])

        novel += f"Chapter {i+2}:\n{chapter}\n"
        chapters.append(chapter)

    return novel, title, chapters, chapter_titles
```

- 这个方法罗列了编写小说的主要步骤，方法参数有提示词、章节数、写作风格，最后那个是否使用 Claude 模型的参数我们可以忽略
- 首先是生成多个小说情节
- 然后从这些小说情节中选择最吸引人的情节
- 再对选择的情节进行改进
- 根据最精彩的情节生成小说标题
- 接下来生成小说的故事线，并将其转换成 Python 数组，形成章节标题
- 使用第一个章节标题写小说第一章的内容
- 创建`novel`对象保存小说内容，创建`chapters`数组来保存每一章的内容
- 生成每一章节的内容，如果章节的内容较少（小于 100）则重新生成
- 最后将每一章的内容添加到`novel`和`chapters`对象中

我们再看生成小说章节的方法`generate_plots`：

```py
import openai

def generate_plots(prompt):
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are a creative assistant that generates engaging fantasy novel plots."},
            {"role": "user", "content": f"Generate 10 fantasy novel plots based on this prompt: {prompt}"}
        ]
    )

    print_step_costs(response, "gpt-4")
    return response['choices'][0]['message']['content'].split('\n')
```

- 调用 ChatGPT API 生成 10 个小说情节
- 这里的 model 参数即使填`gpt-4`也没有关系，我们的 API 服务始终只能调用 GPT3.5 模型
- `print_step_costs`方法是计算 API 的花费金额，因为我们是用免费的 ChatGPT API 服务，所以这里不用关心费用

接下来是选择最吸引人的情节的方法`select_most_engaging`：

```py
def select_most_engaging(plots):
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are an expert in writing fantastic fantasy novel plots."},
            {"role": "user", "content": f"Here are a number of possible plots for a new novel: {plots}\n\n--\n\nNow, write the final plot that we will go with. It can be one of these, a mix of the best elements of multiple, or something completely new and better. The most important thing is the plot should be fantastic, unique, and engaging."}
        ]
    )

    print_step_costs(response, "gpt-4")
    return response['choices'][0]['message']['content']
```

- 调用 ChatGPT API ，根据我们刚才生成的 10 个小说情节，生成最吸引人的情节
- 生成的内容可以是 10 个情节中的一个，也可以是多个情节的组合，或者是一个全新的情节

接下来是改进情节的方法`improve_plot`和生成小说标题的方法`get_title`，代码功能基本和前面的方法一致，我们主要看方法内部的提示词：

```py
def improve_plot(plot):
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are an expert in improving and refining story plots."},
            {"role": "user", "content": f"Improve this plot: {plot}"}
        ]
    )

    print_step_costs(response, "gpt-4")
    return response['choices'][0]['message']['content']

def get_title(plot):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo-16k",
        messages=[
            {"role": "system", "content": "You are an expert writer."},
            {"role": "user", "content": f"Here is the plot: {plot}\n\nWhat is the title of this book? Just respond with the title, do nothing else."}
        ]
    )

    print_step_costs(response, "gpt-3.5-turbo-16k")
    return response['choices'][0]['message']['content']
```

- 这里有个提示词小技巧，就是让 LLM 只回答我们想要的内容而不要输出其他内容，这里的提示词要求 LLM 只回答标题，不要做其他操作，这样就不会有额外的内容干扰

接着是生成小说故事线的方法`generate_storyline`：

```py
def generate_storyline(prompt, num_chapters):
    print("Generating storyline with chapters and high-level details...")
    json_format = """[{"Chapter CHAPTER_NUMBER_HERE - CHAPTER_TITLE_GOES_HERE": "CHAPTER_OVERVIEW_AND_DETAILS_GOES_HERE"}, ...]"""
    response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are a world-class fantasy writer. Your job is to write a detailed storyline, complete with chapters, for a fantasy novel. Don't be flowery -- you want to get the message across in as few words as possible. But those words should contain lots of information."},
            {"role": "user", "content": f'Write a fantastic storyline with {num_chapters} chapters and high-level details based on this plot: {prompt}.\n\nDo it in this list of dictionaries format {json_format}'}
        ]
    )

    print_step_costs(response, "gpt-4")
    improved_response = openai.ChatCompletion.create(
        model="gpt-4",
        messages=[
            {"role": "system", "content": "You are a world-class fantasy writer. Your job is to take your student's rough initial draft of the storyline of a fantasy novel, and rewrite it to be significantly better."},
            {"role": "user", "content": f"Here is the draft storyline they wrote: {response['choices'][0]['message']['content']}\n\nNow, rewrite the storyline, in a way that is far superior to your student's version. It should have the same number of chapters, but it should be much improved in as many ways as possible. Remember to do it in this list of dictionaries format {json_format}"}
        ]
    )

    print_step_costs(improved_response, "gpt-4")
    return improved_response['choices'][0]['message']['content']
```

- 在生成故事线的方法中，生成了 2 个版本的故事线，第一个版本是初稿，第二个版本是在第一版的基础上进行改进
- 这里也有一个提示词技巧，就是让 LLM 返回我们想要的格式，这里的提示词要求 LLM 返回 JSON 格式的内容，这样后面才可以将其转换成 Python 数组

再来是生成小说章节的方法`write_chapter`：

```py
def write_chapter(previous_chapters, plot, chapter_title, claude=True):
    try:
        response = openai.ChatCompletion.create(
            model="gpt-4",
            messages=[
                {"role": "system", "content": "You are a world-class fantasy writer."},
                {"role": "user", "content": f"Plot: {plot}, Previous Chapters: {previous_chapters}\n\n--\n\nWrite the next chapter of this novel, following the plot and taking in the previous chapters as context. Here is the plan for this chapter: {chapter_title}\n\nWrite it beautifully. Include only the chapter text. There is no need to rewrite the chapter name."}
            ]
        )

        print_step_costs(response, "gpt-4")
        return response['choices'][0]['message']['content']
    except:
        response = openai.ChatCompletion.create(
            model="gpt-4-32k",
            messages=[
                {"role": "system", "content": "You are a world-class fantasy writer."},
                {"role": "user", "content": f"Plot: {plot}, Previous Chapters: {previous_chapters}\n\n--\n\nWrite the next chapter of this novel, following the plot and taking in the previous chapters as context. Here is the plan for this chapter: {chapter_title}\n\nWrite it beautifully. Include only the chapter text. There is no need to rewrite the chapter name."}
            ]
        )

        print_step_costs(response, "gpt-4-32k")
        return response['choices'][0]['message']['content']
```

- 方法的第二个参数`plot`实际上是我们之前生成的故事线，表示这个章节的内容要基于这个故事线来生成
- 提示词中还加入了之前的章节内容，这样可以让 LLM 更好的理解整个小说的内容
- 这里首先使用`gpt-4`模型进行生成，`gpt-4`模型默认是`8K`的上下文，如果生成的内容较多超出上下文限制就会报错，捕获异常后再次使用`gpt-4-32k`模型进行生成，最新的`gpt-4-turbo`模型是`128k`，大家可以根据自己的需求来选择模型。我们使用的是免费的 ChatGPT API，模型是`gpt-3.5-turbo`，上下文长度是`16k`

最后是整体的调用方法：

```py
# Example usage:
prompt = "A kingdom hidden deep in the forest, where every tree is a portal to another world."
num_chapters = 10
writing_style = "Clear and easily understandable, similar to a young adult novel. Lots of dialogue."
novel, title, chapters, chapter_titles = write_fantasy_novel(prompt, num_chapters, writing_style, claude_true)

# Replace chapter descriptions with body text in chapter_titles
for i, chapter in enumerate(chapters):
    chapter_number_and_title = list(chapter_titles[i].keys())[0]
    chapter_titles[i] = {chapter_number_and_title: chapter}

# Create the cover
create_cover_image(str(chapter_titles))

# Create the EPUB file
create_epub(title, 'AI', chapter_titles, '/content/cover.png')
```

- `prompt`描述想要创建什么内容的小说，`num_chapters`小说的章节数，`writing_style`写作风格
- 将章节标题和内容组合成一个对象`chapter_titles`
- `create_cover_image`方法是使用 Stability Diffusion 生成小说封面，生成后的封面图片会保存到`/content/cover.png`文件中，你也可以直接将图片放到`/content/cover.png`文件中，这样就不需要调用这个方法了
- `create_epub`方法是生成 EPUB 格式的小说文件，第一个参数是小说的标题，第二个是作者，第三个是小说内容，第四个参数是封面图片

这就是`gpt-author`这个项目的主要代码，如果想了解它的其他代码可以查看它的 GitHub 仓库。理解使用 AI 生成小说的思路以后，你也可以根据自己的需求来调整代码，比如调整生成小说的章节数、写作风格等，然后尝试自己生成一部小说。

## 总结

今天我们介绍了如何以 API 的方式免费使用 OpenAI 的 ChatGPT 模型，以及如何使用 ChatGPT 来写一部小说。可能用免费的 ChatGPT 模型生成的小说还不够完美，但可以通过优化调整相关的提示词，让其生成的小说更加符合我们的需求。同样地，也可以将其中的提示词方法应用到编写其他类型的文章中，期待大家能够尝试并生成一些有趣的内容。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
