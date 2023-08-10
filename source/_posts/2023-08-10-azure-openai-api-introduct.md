---
layout: post
title: Azure OpenAI 服务开通及使用
date: 2023-08-10 09:40:20
description: Azure OpenAI 服务开通及使用
keywords: azure, openai
comments: true
categories: ai
tags: [azure, openai]
---

{% img /images/post/2023/08/azure-openai.jpg 400 300 %}

ChatGPT 面世以来持续引领 AI 技术的火爆，具有专业技能的开发人员也热衷于使用 OpenAI 的 API 来构建自己的应用程序，但由于 OpenAI 并不对国内开放，因此调用 OpenAI API 还比较麻烦。幸运的是，微软的 Azure 推出 OpenAI 服务，他们的服务跟 OpenAI API 是一样的，并且可以在国内进行访问，费用收取也相差不大。今天我们就来看看如何开通 Azure OpenAI 服务，并介绍其使用方法。

<!--more-->

## Azure OpenAI 服务申请

[Azure](https://azure.microsoft.com/zh-cn)是微软推出的云计算平台和服务，提供了包括虚拟机、应用服务、数据库、AI、物联网和区块链等在内的各种解决方案和服务。用户可以在 Azure 上快速搭建、部署和管理应用，并可以选择全球众多数据中心进行部署。Azure 支持多种编程语言、工具和框架，包括微软特有的和第三方的。它旨在帮助企业通过云计算实现更高的效率和灵活性。

在正式开始之前你需要先注册 Azure 的账号，注册账号可以用任意邮箱，不过需要绑定银行卡，网上有很多教程，这里就不赘述了。

然后再申请 Azure OpenAI 服务的开通权限，开通过后才可以在 Azure 上部署 OpenAI 服务。需要注意的是，Azure OpenAI 服务只对**企业用户和学生用户**开放申请，个人用户无法申请（会检测你的申请邮箱，如果是个人邮箱会拒绝）。

进入[Azure OpenAI 服务申请页面](https://customervoice.microsoft.com/Pages/ResponsePage.aspx?id=v4j5cvGGr0GRqy180BHbR7en2Ais5pxKtso_Pz4b1_xUOFA5Qk1UWDRBMjg0WFhPMkIzTzhKQ1dWNyQlQCN0PWcu)进行申请，依次输入以下信息，下面只列出必填项，以企业用户为例：

- 姓名
- 多少个订阅 ID 要申请，只需要选 1 个
- 填写订阅 ID，订阅 ID 可以按照页面提示找到，如果没有就新建一个
- [**重要**]有效的邮箱，这里要填写企业邮箱或者学生邮箱，个人邮箱会被拒绝(包括 gmail.com, outlook.com 等)，这个邮箱地址可以跟你的 Azure 账号邮箱地址不一样
- 公司名称、公司地址、公司所在城市、公司所在省份、公司所在国家、公司网站、公司电话，建议填写真实信息（不用担心公司会知道，一般他们不会打公司电话）
- 公司类型，有 ISV（独立服务提供商）、SI（系统集成商）、MSP（微软解决方案提供商）等
- 确认是否是你的公司要使用该服务，选中就可以了
- 选择要申请的 Azure OpenAI 服务具体功能，有文本和代码模型，和图像模型，我们选择文本和代码模型即可。
- 选择了文本和代码模型功能后，还要选择其细分的功能，除了`Most Valuable Professional (MVP) or Regional Director (RD) Demo Use: `这个选项，其他的都可以选，按实际需求选择
- 最后 2 个必填选是同意 Azure 的协议，都选同意就好了。

填完点击`Submit`按钮提交，然后就等 Azure 审核了，只要你填写的信息比较真实，审核还是比较快的，我是上午申请下午就通过了，时间大概是 6 小时左右。

## 创建 Azure OpenAI 服务

收到审核通过的邮件后，进入[Azure 首页](https://portal.azure.com/#home)，搜索`Azure OpenAI`，选择该类型的服务并进入页面，点击创建按钮进入创建页面。

- 第一步是填写基础信息，选择你的订阅，资源组（没有的话就新建一个），区域（有美国、欧洲、亚洲等国家），服务名称（如果名字被别人占用了会提示），定价层（价格跟 OpenAI 的相差不大），然后点击下一步。

{% img /images/post/2023/08/azure-openai-create.png 1000 600 %}

- 第二步是选择网络，选择第一个全网络就可以了。
- 第三部是选择标签，可以不填。
- 最后确认信息并提交。

## 模型部署

创建好 OpenAI 服务后，点击该服务进入 OpenAI 的服务页面，选择模型部署，然后点击`模型管理`按钮，进入`Azure AI Studio`页面。

{% img /images/post/2023/08/azure-model-deploy.png 1000 600 %}

进去后选择侧边栏的`部署`菜单，点击`创建新部署`按钮，然后选择模型，这里我们选择`gpt35-turbo`即可，再选择模型版本，有`0301`和`0613`两个，我们当然选择最新的`0613`版本，然后输入部署名称，注意名称只能包含`-`和`_`特殊字符，其他的特殊字符不能写到名称中，最后点击`创建`按钮完成部署的创建。

## 获取 API URL 和 API Key

模型部署完成后，重新进入 OpenAI 页面，选择`键和端点`菜单（在模型部署菜单上面），进去后可以看到有 2 个键和 1 个端点，端点就是 API URL，键就是 API KEY，随便用其中一个 KEY 就可以了。

{% img /images/post/2023/08/azure-openai-apikey.png 1000 600 %}

## Azure OpenAI API 使用

拿到 API URL 和 API KEY 后，就可以使用 OpenAI API 了，这里用 curl 命令来测试一下，示例代码如下。

```sh
curl $AZURE_OPENAI_ENDPOINT/openai/deployments/$DEPLOYMENT_NAME/chat/completions\?api-version\=2023-06-31-preview \
  -H "Content-Type: application/json" \
  -H "api-key: $AZURE_OPENAI_KEY" \
  -d '{"messages":[{"role": "system", "content": "You are a helpful assistant."},{"role": "user", "content": "hello"}]}'
```

- `AZURE_OPENAI_ENDPOINT`是 API URL
- `DEPLOYMENT_NAME`是模型部署的名称
- `api-version`参数是指 API 的版本，有`2023-06-31-preview`、`2023-05-15`等版本，具体的版本信息可以看[这里](https://learn.microsoft.com/en-us/azure/ai-services/openai/reference#completions)
- `AZURE_OPENAI_KEY`是 API KEY
- 发送的 body 数据是一个消息数组，每个消息分别定义了角色和内容。

这个 curl 命令可以在国内直接访问，大家可以在自己电脑终端上试一下。

然后我们再看下请求返回的结果：

```json
{
  "id": "chatcmpl-7lurXrxcVQDGEWssG28dxeJkpKLTD",
  "object": "chat.completion",
  "model": "gpt-35-turbo",
  "prompt_annotations": {
    "hate": {
      "filtered": false,
      "severity": "safe"
    },
    "self_harm": {
      "filtered": false,
      "severity": "safe"
    },
    "sexual": {
      "filtered": false,
      "severity": "safe"
    },
    "violence": {
      "filtered": false,
      "severity": "safe"
    }
  },
  "choices": [
    {
      "index": 0,
      "finish_reason": "stop",
      "message": {
        "role": "assistant",
        "content": "Hello! How can I assist you today?"
      },
      "content_filter_results": {
        // 与 prompt_annotations 内容相同
      }
    }
  ],
  "usage": {
    "completion_tokens": 9,
    "prompt_tokens": 18,
    "total_tokens": 27
  }
}
```

我们再看看 OpenAI API 的返回结果：

```json
{
  "id": "chatcmpl-7lvHYFBRjBTWDf24TSy1JfL6zCmMj",
  "object": "chat.completion",
  "model": "gpt-3.5-turbo-0613",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "Hello! How can I assist you today?"
      },
      "finish_reason": "stop"
    }
  ],
  "usage": {
    "prompt_tokens": 19,
    "completion_tokens": 9,
    "total_tokens": 28
  }
}
```

可以看到 Azure OpenAI 的返回结果和 OpenAI API 的返回结果大致相同，除了 Azure OpenAI 的返回结果中多了`prompt_annotations`和`content_filter_results`这 2 个属性，这 2 个属性分别是对问题和答案的检查结果，检测其中是否包含违规的信息，比如暴力黄色什么的，如果设置了过滤信息检查并检查出了违规内容，Azure OpenAI 的请求就会失败。

## Langchain 中对 OpenAI LLM 进行替换

如果你使用的是 Langchain 的 Python 包，可以使用`AzureOpenAI`类来替换`OpenAI`类，示例代码如下：

```python
from langchain.llms import AzureOpenAI

llm = AzureOpenAI(
    openai_api_type="azure",
    openai_api_key="Azure OpenAI API KEY",
    openai.api_base="Azure OpenAI API URL",
    openai_api_version="2023-06-31-preview",
    deployment_name="Deployment Name",
)
```

如果你使用的是 Langchain 的 JS 包，可以直接使用原来的`ChatOpenAI`类，只需要把原来的 `openaiKey` 参数改成其他的 Azure OpenAI API 参数就可以了，示例代码如下：

```ts
import { ChatOpenAI } from 'langchain/chat_models/openai';

const model = new ChatOpenAI({
  azureOpenAIApiVersion: '2023-06-01-preview',
  azureOpenAIApiKey: 'Azure OpenAI API KEY',
  azureOpenAIBasePath: 'Azure OpenAI API URL',
  azureOpenAIApiDeploymentName: 'Deployment Name',
});
```

## 总结

虽然 OpenAI 目前国内还不准使用，但 Azure OpenAI 是允许使用的，它的最大的好处就是可以在国内正常使用 OpenAI API 的能力，部署服务的时候也无需选择国外的服务器，找个国内服务器或者香港的服务器就可以了。希望这篇文章可以帮助大家搭建自己的 Azure OpenAI 服务，如果在使用过程中有什么问题，也欢迎大家在评论区留言讨论。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
