---
layout: post
title: OpenAI 新版 API 使用介绍
date: 2023-11-11 16:03:41
description: OpenAI 新版 API 使用介绍
keywords: openai, chatgpt, api
comments: true
categories: ai
tags: [openai, chatgpt, api]
---

{% img /images/post/2023/11/openai-api.png 400 300 %}

OpenAI 最近举办了首次开发者大会，大会上不仅发布了 GPTs 这样**王炸级别的**新功能，还发布了一些新模型，比如`gpt-4-turbo`等，模型的知识截止时间也提高到了 2023 年 4 月，配合这些新模型，OpenAI 还开放了大家期盼已久的新 API，其中包括语音生成、图像生成、图像识别等功能，本文将对这些新 API 进行介绍，帮助大家快速掌握这些新功能。

<!--more-->

## OpenAI Python 包

在 OpenAI 举办完开发者大会后，OpenAI 的官方 Python 包也进行了快速更新，从原来的 0.x 版本一下子升级到 1.x 版本，截止到笔者撰写本文时，最新的版本为 1.2.3。在本文中，我们将使用最新的 Python 包进行演示，大家可以按照以下步骤安装最新的 Python 包：

```bash
pip install --upgrade openai
```

然后在终端导入 OpenAI 的 API KEY 作为环境变量：

```bash
export OPENAI_API_KEY=sk-xxxxx
```

这样我们在使用 OpenAI 的 Python 包时就会自动加载这个环境变量了。

## 文字转语音

OpenAI 新版的文字转语音 API 提供了 2 个 TTS（Text to Speech）模型：tts-1 和 tts-1-hd。tts-1 模型速度较快，而 tts-1-hd 模型质量较高，大家可以根据自己的实际需求选择合适的模型，模型的使用方法如下：

```python
from pathlib import Path
import openai

response = openai.audio.speech.create(
    model="tts-1",
    voice="shimmer",
    input="举头望明月，低头思故乡",
    response_format="mp3",
    speed=1.0,
)
speech_file_path = Path(__file__).parent / "speech.mp3"
response.stream_to_file(speech_file_path)
```

- model 参数可以选择 tts-1 或者 tts-1-hd。
- voice 参数是指声音的类型，目前支持 6 种声音，分别是：alloy、echo、fable、onyx、nova、和 shimmer。
- 另外 API 还有 2 个可选参数，分别是 response_format 和 speed，response_format 参数是指导出声音文件格式，可以选择 mp3、opus、 aac 和 flac；speed 参数是指声音的速度，值的范围是 0.25 ~ 4.0，默认值是 1.0。

下面是 6 种声音的对比，使用的是 tts-1 的模型，对于中文来说效果比较好的是 alloy 的声音，其他的外国腔都比较重。

### Alloy

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-alloy.mp3" type="audio/mp3">
</audio>

### Echo

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-echo.mp3" type="audio/mp3">
</audio>

### Fable

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-fable.mp3" type="audio/mp3">
</audio>

### Nova

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-nova.mp3" type="audio/mp3">
</audio>

### Onyx

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-onyx.mp3" type="audio/mp3">
</audio>

### Shimmer

<audio width="480" height="320" controls>
  <source src="/images/post/2023/11/speech-shimmer.mp3" type="audio/mp3">
</audio>

## 图像生成

图像生成方面也新推出了可以使用 DALL-E 3 模型的 API，使用方法如下：

```python
from openai import OpenAI
import requests
import json

client = OpenAI()

def save_image_by_url(url, file_name: str = "output.png"):
    response = requests.get(url)
    with open(file_name, "wb") as f:
        f.write(response.content)

response = client.images.generate(
    model="dall-e-3",
    prompt="画一个宇航员在月球上骑马的图片",
    n=1,
    quality="standard",
    size="1024x1024",
    style="vivid",
    response_format="url",
)
result = json.loads(response.json())
image_url = result["data"][0]["url"]
save_image_by_url(image_url)
```

- model 参数是指使用的模型，可以选择 dall-e-2 或者 dall-e-3。
- prompt 参数是指生成图片的提示词，dall-e-2 模型支持 1000 个 token，而 dall-e-3 模型支持 4000 个 token。
- n 参数是指一次生成多少张图片，范围为 1 到 10，dall-e-3 模型只能支持一次生成一张图片。
- quality 参数是指图片的质量，有 standard 和 hd 两个选项，standard 是标准质量，生成速度较快，hd 是高清质量，生成速度较慢，该参数只对 dall-e-3 模型有效。
- size 参数是指图片的尺寸，默认是 1024 x 1024，对于 dall-e-2 模型支持 256x256、512x512、1024x1024 三种尺寸，对于 dall-e-3 模型支持 1024x1024、1792x1024、1024x1792 三种尺寸。
- style 参数是生成图片的风格，有 vivid 和 natural 两个选项，vivid 让图片更贴近真实场景，而 natural 让图片更像是绘画。
- response_format 是指输出格式，有 url 和 b64_json 两种格式，url 是指输出图片的 url 地址，b64_json 是指输出图片的 base64 编码。

上面的例子返回了图片的 url，下面再演示一下通过 b64_json 格式输出图片的 base64 编码，然后再将 base64 编码转换成图片文件：

```python
import base64

def save_image_by_b64(b64_json: str, file_name: str = "output.png"):
    with open(file_name, "wb") as f:
        f.write(base64.b64decode(b64_json))

response = client.images.generate(
    ......
    response_format="b64_json",
)
result = json.loads(response.json())
image_b64 = result["data"][0]["b64_json"]
save_image_by_b64(image_b64, file_name="output-b64.png")
```

生成的图片效果如下：

{% img /images/post/2023/11/openai-api-image.png 600 400 %}

## 固定输出 Json 对象

以前让 ChatGPT 返回 Json 格式的结果时，由于 ChatGPT 喜欢**废话**的特性，返回的结果往往不尽人意，经常是返回一个 Json 对象然后再带上一段废话，这样让我们在程序解析上比较麻烦，现在 OpenAI 新版 API 新增了一个专门返回有效 Json 对象的功能，使用方法如下：

```py
from openai import OpenAI

SYSTEM_PROMPT = """
你是一个得力的助手，帮助人类提取句子中的人名信息，并以json格式展示。

json格式如下：
{
    "name": ["小明"],
}
"""

completion = client.chat.completions.create(
    model="gpt-3.5-turbo-1106",  # 需要用到新模型，老的比如 gpt-3.5-turbo 不支持该功能
    messages=[
        {"role": "system", "content": SYSTEM_PROMPT},
        {"role": "user", "content": "小明和小红去上学。"},
    ],
    response_format={"type": "json_object"},
)

print(completion.choices[0].message.content)
```

- 使用的 API 是对话 API，但不同的地方是，response_format 参数将值设置为`{"type": "json_object"}`，这样就启动了 Json 模式，保证了返回的结果是一个 Json 对象。
- 注意使用 JSON 模式的时候，model 参数需要用到新模型，比如 gpt-3.5-turbo-1106 或 gpt-4-1106-preview，老模型不支持该功能。
- 还有一点是，使用 JSON 模式时，在提示词中必须明确要求 ChatGPT 返回 Json 格式，不管是在 System 提示词还是 User 提示词中都可以，否则 API 就会返回无限循环的空格，直到 token 达到上限。

上面例子的返回内容如下：

```json
{
  "name": ["小明", "小红"]
}
```

## 一次执行多个 Function Calling

Function Calling 是 OpenAI 之前就具备的功能，即在对话中使用用户自定义的工具，来让 ChatGPT 能做更多的事情，比如实时查询某个地区的天气情况，或者查询股票的最新价格等。但是以前在一次对话中只能调用一个工具，现在新版 API 支持一次调用多个工具。

{% img /images/post/2023/11/openai-api-function-call.png 1000 600 %}

下面使用代码分段展示如何在一次对话中调用多个工具：

```py
from openai import OpenAI

client = OpenAI()

messages = [
    {
        "role": "user",
        "content": "今天北京、上海、成都的天气怎么样？",
    }
]
tools = [
    {
        "type": "function",
        "function": {
            "name": "get_current_weather",
            "description": "获取某个地方当前的天气情况",
            "parameters": {
                "type": "object",
                "properties": {
                    "location": {
                        "type": "string",
                        "description": "城市名，比如： 北京, 上海",
                    },
                },
                "required": ["location"],
            },
        },
    }
]
response = client.chat.completions.create(
    model="gpt-3.5-turbo-1106",
    messages=messages,
    tools=tools,
    tool_choice="auto",
)
```

- 首先使用我们定义了对话中要用到的工具集 tools，ChatGPT 会根据用户的问题来选择调用哪个工具，示例代码中我们用到一个查询天气的工具。
- tools 参数以前叫做 functions，现在改名为 tools，但是功能是一样的，都是用来定义工具集的。需要定义工具的名称、描述和参数信息（名称、类型、描述和是否必需）。
- model 参数是指使用的模型，一次调用多个工具需要选择新模型： gpt-3.5-turbo-1106 或者 gpt-4-1106-preview。
- tool_choice 参数以前叫做 function_call，现在改名为 tool_choice，是指选择的工具，有以下 3 种值：
  - auto：根据用户问题选择工具，是默认值
  - none：不选择任何工具，直接返回生成的消息
  - 工具名称：选择某个工具，比如：`{"type: "function", "function": {"name": "get_current_weather"}}`

这样在调用对话 API 后，ChatGPT 会根据用户的问题返回需要用到的工具列表 tool_calls，这个列表包含了每个工具的名称和参数等信息，下面继续看如何调用工具：

```py
response_message = response.choices[0].message
tool_calls = response_message.tool_calls
if tool_calls:
    messages.append(response_message)
    available_functions = {
        "get_current_weather": get_current_weather,
    }
    for tool_call in tool_calls:
        function_name = tool_call.function.name
        function_to_call = available_functions[function_name]
        function_args = json.loads(tool_call.function.arguments)
        function_response = function_to_call(
            location=function_args.get("location"),
        )
        messages.append(
            {
                "tool_call_id": tool_call.id,
                "role": "tool",
                "name": function_name,
                "content": function_response,
            }
        )
    second_response = client.chat.completions.create(
        model="gpt-3.5-turbo-1106",
        messages=messages,
    )
    print(second_response.choices[0].message.content)
```

- 得到工具列表后，我们就可以根据工具列表来调用工具了，这里我们定义了一个`get_current_weather`的方法，然后根据工具列表中的工具名和参数来调用这个方法。
- 将方法返回的结果，结合工具调用 id 和工具名等信息，以 tool 为角色添加 message 信息到到历史对话中。
- 得到最后的历史对话 messages 后，再次调用对话 API，由 ChatGPT 根据工具的返回结果来生成最终的答案，注意这里的 model 参数也是需要选择新模型。

我们为了演示方便，`get_current_weather`方法使用一些简单的逻辑来模拟天气查询的功能：

```py
import json

def get_current_weather(location):
    """Get the current weather in a given location"""
    if "北京" in location.lower():
        return json.dumps({"location": location, "temperature": "10°"})
    elif "上海" in location.lower():
        return json.dumps({"location": location, "temperature": "15°"})
    else:
        return json.dumps({"location": location, "temperature": "20°"})
```

最终的返回结果为：

```bash
北京目前温度为10℃，上海为15℃，成都为20℃。
```

最终完整的对话记录如下：

```json
[
  { "role": "user", "content": "今天北京、上海、成都的天气怎么样？" },
  {
    "role": "assistant",
    "tool_calls": [
      {
        "id": "call_hrGSR6rUAiTjdqi5BDNY3DCr",
        "type": "function",
        "function": {
          "arguments": "{\"location\": \"北京\"}",
          "name": "get_current_weather"
        }
      },
      {
        "id": "call_L8cyPoBWcfAU6Q8dyZP8mA5k",
        "type": "function",
        "function": {
          "arguments": "{\"location\": \"上海\"}",
          "name": "get_current_weather"
        }
      },
      {
        "id": "call_RZvc1iGHJ0BnJwuqYmSeo5YF",
        "type": "function",
        "function": {
          "arguments": "{\"location\": \"成都\"}",
          "name": "get_current_weather"
        }
      }
    ]
  },
  {
    "role": "tool",
    "tool_call_id": "call_hrGSR6rUAiTjdqi5BDNY3DCr",
    "name": "get_current_weather",
    "content": "{\"location\": \"北京\", \"temperature\": \"10°\"}"
  },
  {
    "role": "tool",
    "tool_call_id": "call_L8cyPoBWcfAU6Q8dyZP8mA5k",
    "name": "get_current_weather",
    "content": "{\"location\": \"上海\", \"temperature\": \"15°\"}"
  },
  {
    "role": "tool",
    "tool_call_id": "call_RZvc1iGHJ0BnJwuqYmSeo5YF",
    "name": "get_current_weather",
    "content": "{\"location\": \"成都\", \"temperature\": \"20°\"}"
  },
  {
    "role": "assistant",
    "content": "北京目前温度为10℃，上海为15℃，成都为20℃。"
  }
]
```

## 图像识别

GPT4V 可以用来识别用户上传的图片内容，更多的介绍可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2023/10/15/interesting-gpt4v/)，以前只能在 Web 或 APP 中使用，现在也终于开放了 API。以下是使用方法：

```py
from openai import OpenAI
import base64

client = OpenAI()

def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode("utf-8")

base_image = encode_image("output.png")
completion = client.chat.completions.create(
    model="gpt-4-vision-preview",
    messages=[
        {
            "role": "user",
            "content": [
                {"type": "text", "text": "图片中是什么内容"},
                {
                    "type": "image_url",
                    "image_url": {
                        # 可以是图片的 base64 编码
                        "url": f"data:image/png;base64, {base_image}",
                        # # 也可以是图片的 url 地址
                        # "url": "https://image-server.com/foo.png",
                        "detail": "high",
                    },
                },
            ],
        }
    ],
    max_tokens=300,
)
print(completion.choices[0].message.content)
```

- model 参数是指使用的模型，图片识别的功能只能使用 gpt-4-vision-preview 这个模型。
- user 的提示词不再是字符串类型，而是一个数组类型，数组中包含了文字和图片信息，文字信息是用户的问题，图片信息是用户上传的图片文件。
- 上传的图片可以是图片文件的 Base64 编码，示例代码中我们用一个 Python 方法将图片转成 Base64 编码。同时也支持图片的 url 地址，官方建议使用 url 地址来做图片识别，因为使用图片 base64 编码的话，OpenAI API 接收到请求后还需要下载图片，从而增加了 API 的调用时间。
- image_url 参数中的 detail 参数用来控制模型识别图片的精准度，有 low 和 high 两个选项，low 对图片识别较快但识别度不高，high 对图片识别较准确，但所耗费的 token 也较多。

在图片识别 API 中，可以同时上传多个图片，下面是一个比较两张图片的代码示例（但经过实际测试，GPT4V 对图片的比较还存在较大的误差，讲出来的不同点根本不是图片中的内容）：

```py
completion = client.chat.completions.create(
    model="gpt-4-vision-preview",
    messages=[
        {
            "role": "user",
            "content": [
                {"type": "text", "text": "比较这两张图片有什么不同"},
                {
                    "type": "image_url",
                    "image_url": { "url": "https://image-server.com/diff1.png" },
                },
                {
                    "type": "image_url",
                    "image_url": { "url": "https://image-server.com/diff2.png" },
                },
            ],
        }
    ],
    max_tokens=300,
)
```

在 content 数组中添加多个 image_url 参数即表示多个图片，但经过实际测试，GPT4V 对图片的比较还存在较大的误差，讲出来的不同点跟图片中的内容不符。上传的图片在调用完 API 后会被 OpenAI 服务器自动删除，所以不用担心会被拿去训练他们的模型。

### GPT4V 的限制

目前 GPT4V 对图片识别存在以下限制：

- 医学图片：该模型不适合解释像 CT 图片等医学图片
- 非英文图片：目前对非英文的图片识别不好
- 旋转图片：对旋转的图片或文字识别不好
- 验证码：不要想利用 GPT4V 来识别验证码了，OpenAI 已经禁止了这个功能

更多的限制信息请看[这里](https://platform.openai.com/docs/guides/vision/limitations)。

### 图片识别的 token 计算

因为图片识别 API 需要上传图片，所以在计算 API 耗费的 token 时需要考虑图片的大小，当识别图片使用的是`detail: low`的低保真模式时，每张图片耗费 85 token；而使用`detail: high`的高保真模式时，每张图片按照包含多少个 512 平方来计算 token，每包含 1 个 512 平方耗费 170 token，最后再加上固定的 85 token，比如一张 1024 x 1024 的图片，包含了 4 个 512 平方，所以 就会耗费 680 token，最后再加上 85 token，所以总共耗费 765 token。

具体的 token 计算规则可参考官方的[消费计算规则](https://platform.openai.com/docs/guides/vision/calculating-costs)。

## 总结

本文介绍了 OpenAI 新版 API 中的文字转语音、图像生成、图像识别等功能，这些新版的 API 可以帮助我们构建功能更加强大的 AI 应用，而且 OpenAI 还开放了 GPTs 的 API 功能——Assistant API，我们将在下期对这个新 API 进行详细介绍，敬请期待。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
