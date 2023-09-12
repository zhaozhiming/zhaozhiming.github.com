---
layout: post
title: LangChain 自定义LLM
date: 2023-09-11 11:16:42
description: LangChain 自定义LLM
keywords: langchain, llm, chatglm
comments: true
categories: ai
tags: [langchain, llm, chatglm]
---

{% img /images/post/2023/09/langchain-custom-llm.jpg 400 300 %}

Langchain 默认使用 OpenAI 的 LLM（大语言模型）来进行文本推理工作，但主要的问题就是数据的安全性，跟 OpenAI LLM 交互的数据都会上传到 OpenAI 的服务器，企业内部如果想要使用 LangChain 来构建应用，那最好是让 LangChain 使用企业内部的 LLM，这样才能保证数据不泄露。LangChain 提供了集成多种 LLM 的能力，包括自定义的 LLM，今天我们就来介绍一下如何使用 LangChain 来集成自定义的 LLM 以及其中的实现原理。

<!--more-->

## 开源大模型

虽然现在的商业大模型（OpenAI 和 Anthropic）功能十分强大，但开源大模型愈来愈有迎头赶上的趋势，比如最近刚发布的[Falcon-180B](https://huggingface.co/spaces/tiiuae/falcon-180b-demo)大模型，具备 1800 亿参数，（号称）性能甚至直逼 GPT-4。所以对于想构建 AI 应用，又不想自身数据泄露的企业来说，开源大模型是首要选择。

开源大模型也有很多选择，要根据自身的需求来考虑。比如需要大量自然语言处理的项目，选择一个专注于文本处理的模型会比选择图像或视频的模型更合适，再比如需要提供多语言的项目，那么大模型就需要支持多语言而不仅仅是英文。另外模型的大小和复杂性也是一个考虑因素，大模型虽然能够处理更复杂的任务，但它们通常需要更多的计算资源和存储空间。对于有限资源的中小企业，可能需要选择一个更轻量级的模型。

对于一些简单的应用，我们可以选择现在国内比较流行的中文开源大模型——ChatGLM 或者 BaiChuan，它们不仅支持中英文，还开源了小参数的 LLM，比如 ChatGLM2-6B、Baichuan2-13B 等。

## LLM 部署

后面我们会用 LangChain 来集成 ChatGLM2 进行介绍，所以我们需要先部署 ChatGLM2-6B 这个 LLM。ChatGLM2-6B 部署有多种方式，可以使用它自身的代码仓库进行部署，也可以使用其他框架来进行部署。我们主要部署 ChatGLM2-6B 的 API 服务，具体步骤可以参考我之前的文章：[使用 FastChat 部署 LLM](https://zhaozhiming.github.io/2023/08/22/use-fastchat-deploy-llm/)，这里就不再赘述。

部署后的 API 服务地址我们假设是`http://localhost:5000`，调用`/chat/completions`接口会返回类似 OpenAI 接口的信息：

```bash
$ curl -X 'POST' \
  'http://localhost:5000/v1/chat/completions' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "model": "chatglm2-6b",
  "messages": [{"role": "user", "content": "你好"}]
}'

# 输出结果
{
  "id": "chatcmpl-TPvsyLsybHEJ2nd953q7E2",
  "object": "chat.completion",
  "created": 1694497436,
  "model": "chatglm2-6b",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "你好！我是人工智能助手 ChatGLM2-6B，很高兴见到你，欢迎问我任何问题。"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 4,
    "total_tokens": 145,
    "completion_tokens": 141
  }
}
```

这个接口是兼容 OpenAI 接口的，其中 model 和 messages 参数是必须的，messages 中 role 的值有`user`，`assistant`, `system`这几项，content 是对应角色的内容，更多参数信息可以参考[OpenAI 的 API 官方文档](https://platform.openai.com/docs/api-reference/chat/create)。下面我们主要使用这个 API 来封装我们的自定义 LLM。

## 封装自定义 LLM

使用 LangChain 封装自定义的 LLM 并不复杂，可以看下面的代码示例：

```python
import requests
from typing import Any, List, Mapping, Optional

from langchain.callbacks.manager import CallbackManagerForLLMRun
from langchain.llms.base import LLM

class CustomLLM(LLM):
    endpoint: str = "http://localhost:5000"
    model: str = "chatglm2-6b"

    def _call(
        self,
        prompt: str,
        stop: Optional[List[str]] = None,
        callbacks: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> str:
        headers = {"Content-Type": "application/json"}
        data = {"model": self.model, "messages": [{"role": "user", "content": prompt}]}
        response = requests.post(f"{self.endpoint}/chat/completions", headers=headers, json=data)
        response.raise_for_status()

        result = response.json()
        text = result["choices"][0]["message"]["content"]
        return text
```

- 首先我们需要创建一个类继承自`LLM`，然后实现`_call`方法
- 方法的最主要的参数是提示词`prompt`，这个参数就是上面接口中的`messages`参数中的用户内容
- 在`_call`方法中，我们构造 API 接口所需参数，包括 headers 和 data
- 调用 API 接口，获取到返回结果，最后返回`choices`中`message`的内容

`_call`方法的实现逻辑就是接收用户的输入，然后将其传递给 LLM，然后获取到 LLM 的输出，最后再返回结果给用户。在方法中可以调用 API 服务，也可以用 transformer 来初始化模型然后直接调用模型进行推理，总之可以用各种方法来调用 LLM，只要能得到LLM返回的结果即可。

### 自定义 LLM 的其他方法

除了`_call`方法外，我们还需要实现其他方法，比如`_llm_type`方法，这个方法是用来定义 LLM 的名称，因为我们用的是 ChatGLM2-6B 模型，所以我们可以这样实现：

```python
    @property
    def _llm_type(self) -> str:
        return "chatglm2-6b"
```

还有`_identifying_params`方法，这个方法是用来打印自定义 LLM 类的参数信息，方便我们做调试，它返回的是一个字典，代码示例如下：

```python
    @property
    def _identifying_params(self) -> Mapping[str, Any]:
        """Get the identifying parameters."""
        return {"endpoint": self.endpoint, "model": self.model}
```

### 自定义 LLM 的使用

自定义 LLM 的使用跟使用其他 LLM 一样，我们可以直接调用自定义 LLM 的实例，代码示例如下：

```python
llm = CustomLLM()
print(llm("你好"))

# 输出结果
"""
你好！我是人工智能助手 ChatGLM2-6B，很高兴见到你，欢迎问我任何问题。
"""
```

### `_call` 方法的其他参数

在`_call`方法中除了 prompt 参数外，我们还看到了其他参数，这些参数都是可选的，我们来看一下这些参数的作用：

**stop**

这个参数是传入一个字符串集合，当检测到 LLM 的输出内容中包含了这些字符串时，输出内容会立即截断，只保留前面的内容。比如我们得到的 LLM 结果如下：

```bash
你好！我是人工智能助手 ChatGLM2-6B，很高兴见到你，欢迎问我任何问题。
```

当我们将`stop`参数设置为`["欢迎"]`时，输出结果就会变成：

```bash
你好！我是人工智能助手 ChatGLM2-6B，很高兴见到你，
```

如果是自定义 LLM，`stop`参数的逻辑也需要我们自己来实现，LangChain 其实提供了对应的工具方法，我们直接使用就可以了，代码示例如下：

```python
from langchain.llms.utils import enforce_stop_tokens

def _call(
        self,
        prompt: str,
        stop: Optional[List[str]] = None,
        callbacks: Optional[CallbackManagerForLLMRun] = None,
        **kwargs: Any,
    ) -> str:
        .....
        if stop is not None:
            text = enforce_stop_tokens(text, stop)
```

**callbacks**

这个参数是一个`CallbackManagerForLLMRun`对象，用于在 LLM 运行过程中执行回调函数，比如在 LLM 运行前后执行一些操作，比如记录日志、保存模型等。这个参数是可选的，我们使用 LangChain 提供的日志记录回调函数来演示下功能：

```python
from loguru import logger
from langchain.callbacks import FileCallbackHandler

if __name__ == "__main__":
    llm = CustomLLM()
    logfile = "output.log"
    logger.add(logfile, colorize=True, enqueue=True)
    handler = FileCallbackHandler(logfile)
    result = llm("你好", stop=["欢迎"], callbacks=[handler])
    logger.info(result)
```

执行完程序后，会在当前目录下生成一个`output.log`文件，文件内容如下：

```bash
2023-09-12 11:28:19.029 | INFO     | __main__:<module>:110 - 你好！我是人工智能助手 ChatGLM2-6B，很高兴见到你，
```

**注意：**在 LangChain 官方文档的示例代码中将`callbacks`参数写成了`run_manager`，其实最新代码中这个参数名已经改成了`callbacks`了，可能官方文档还没有及时更新。

LangChain 还提供了更多的回调方法，想了解更多信息的可以参考[这个文档](https://python.langchain.com/docs/modules/callbacks/)。

LangChain 官方文档上也给出了自定义 LLM 的简单代码示例，可以参考：[Custom LLM](https://python.langchain.com/docs/modules/model_io/models/llms/custom_llm)。

## 其他自定义的 LLM

除了参考以上示例来编写自定义的 LLM 外，还可以参考 LangChain 中已经集成的其他 LLM。

### ChatGLM

这个是封装比较早的 ChatGLM LLM，用的还是一代的 ChatGLM，除非部署方式一致，否则不建议直接使用该 LLM，建议参照其中的代码来实现自己的 LLM。

- 相关文档：[ChatGLM LLM](https://python.langchain.com/docs/integrations/llms/chatglm)
- 相关代码：[chatglm.py](https://github.com/langchain-ai/langchain/blob/master/libs/langchain/langchain/llms/chatglm.py)

### Fake LLM

这是一个假的 LLM，用于测试，自定义内容来模拟 LLM 的输出，可以参考其中的代码来实现自己的 LLM，其中包含了流式输出，异步调用等功能的实现逻辑。

- 相关文档：[Fake LLM](https://python.langchain.com/docs/modules/model_io/models/llms/fake_llm)
- 相关代码：[fake.py](https://github.com/langchain-ai/langchain/blob/master/libs/langchain/langchain/llms/fake.py)

还有很多其他的 LLM，包括 OpenAI 的 LLM，如果感兴趣的也可以去看看它们的源码，相对会比较复杂，更多信息可以参考[这里](https://python.langchain.com/docs/integrations/llms/)。

## 总结

今天我们主要介绍了如何使用 LangChain 来集成自定义的 LLM，以及其中的实现原理，实现自己的 LangChain LLM 并不复杂，但如果要实现一个功能强大，性能高效的 LLM，就需要花费更多的时间和精力了，好在 LangChain 提供了一系列的工具和组件，可以帮助我们快速实现自己的功能。希望今天的文章能够帮助到大家，也希望使用过 LangChain 的同学一起来交流学习，欢迎在评论区留言。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
