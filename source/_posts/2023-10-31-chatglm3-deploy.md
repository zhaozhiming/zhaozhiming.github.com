---
layout: post
title: ChatGLM3-6B 部署指南
date: 2023-10-31 14:15:17
description: ChatGLM3-6B 部署指南
keywords: chatglm3, llm
comments: true
categories: ai
tags: [chatglm3, llm]
---

{% img /images/post/2023/10/chatglm3-deploy.png 400 300 %}

最近智谱 AI 对底层大模型又进行了一次升级，ChatGLM3-6B 正式发布，不仅在性能测试和各种测评的数据上有显著提升，还新增了一些新功能，包括工具调用、代码解释器等，最重要的一点是还是保持 6B 的这种低参数量，让我们可以在消费级的显卡上部署大语言模型（LLM）。本文将对 ChatGLM3-6B 的部署做一次详细介绍，让更多人可以体验这个 LLM 的有趣功能。

<!--more-->

## 环境安装

首先下载 ChatGLM3 的代码仓库，并安装相关的依赖。

```bash
git clone https://github.com/THUDM/ChatGLM3
cd ChatGLM3
pip install -r requirements.txt
```

然后下载 ChatGLM3-6B 的模型文件，以下是笔者常用的 HuggingFace 下载方式。

```bash
GIT_LFS_SKIP_SMUDGE=1 git clone https://huggingface.co/THUDM/chatglm3-6b
cd chatglm3-6b
wget "https://huggingface.co/THUDM/chatglm3-6b/resolve/main/pytorch_model-00001-of-00007.bin"
wget "https://huggingface.co/THUDM/chatglm3-6b/resolve/main/pytorch_model-00002-of-00007.bin"
...
```

ChatGLM3-6B 的模型文件在[ModelScope](https://modelscope.cn)上也有提供下载，如果 HuggingFace 无法访问的话，可以从这上面下载。

```bash
git lfs install
git clone https://www.modelscope.cn/ZhipuAI/chatglm3-6b.git
```

## 部署 WebUI 服务

ChatGLM3-6B 提供了两种 WebUI，分别是 Gradio 和 Streamlit。

### Graido 服务

在启动 Gradio 服务之前需要先修改`web_demo.py`文件，将里面的模型地址改成本地的地址：

```diff
-tokenizer = AutoTokenizer.from_pretrained("THUDM/chatglm3-6b", trust_remote_code=True)
-model = AutoModel.from_pretrained("THUDM/chatglm3-6b", trust_remote_code=True).cuda()
+tokenizer = AutoTokenizer.from_pretrained("/root/autodl-tmp/chatglm3-6b", trust_remote_code=True)
+model = AutoModel.from_pretrained("/root/autodl-tmp/chatglm3-6b", trust_remote_code=True).cuda()
```

然后执行以下命令启动 Gradio 服务，服务启动后在浏览器中可以访问该服务：

```bash
python web_demo.py
```

如果是使用 AutoDL 进行部署的话,可以将服务的端口设置`6006`（AutoDL 的开放端口），然后通过 AutoDL 的自定义服务进行访问。

{% img /images/post/2023/10/chatglm3-autodl.png 600 300 %}

{% img /images/post/2023/10/chatglm3-gradio.png 1000 600 %}

### Streamlit 服务

在启动 Streamlit 服务之前需要修改`web_demo2.py`，将里面的模型地址改成本地的地址：

```diff
-model_path = "THUDM/chatglm3-6b"
+model_path = "/root/autodl-tmp/chatglm3-6b"
```

然后执行以下命令启动 Streamlit 服务，服务启动后在浏览器中可以访问该服务进行对话：

```bash
streamlit run web_demo2.py
```

如果是使用 AutoDL 进行部署的话，即使使用其开放端口（6006）也无法正常访问 Streamlit 的页面，页面会一直停留在`Please wait...`的提示中。因为 Streamlit 没有像 Gradio 那种内网代理功能，Gradio 在启动服务时可以通过 share=True 参数来生成一个公网链接，这个链接会代理到服务器的内部服务，这样在外部也可以正常访问 Gradio 服务。而 Streamlit 没有这种功能，所以我们需要通过 Ngrok 这个工具来实现内网穿透，将内网的服务代理到公网上，这样就可以正常访问页面了。

### Ngrok 安装使用(可选)

首先在 [Ngrok](https://ngrok.com/) 官网上查看安装命令，我们以 Linux 系统为例，有多种方式可以安装，包括压缩包下载、APT 安装、Snap 安装，这里我们使用 APT 安装，执行以下命令：

```bash
curl -s https://ngrok-agent.s3.amazonaws.com/ngrok.asc | sudo tee /etc/apt/trusted.gpg.d/ngrok.asc >/dev/null && echo "deb https://ngrok-agent.s3.amazonaws.com buster main" | sudo tee /etc/apt/sources.list.d/ngrok.list && sudo apt update && sudo apt install ngrok
```

Ngrok 安装完成后，需要到它的官网上注册一个账号，然后在`Your Authtoken`菜单中获取 Authtoken，这个 Authtoken 用于验证用户身份，可以通过以下命令将 Authtoken 设置到本地。

```bash
$ ngrok config add-authtoken your-ngrok-authtoken # 这里替换成你的 Authtoken
```

然后执行以下命令，通过 Ngrok 代理本地的 Streamlit 服务。

```bash
ngrok http 8501 # streamlit 默认端口为 8501

## 服务窗口
ngrok                                                                                                                   (Ctrl+C to quit)

Introducing Always-On Global Server Load Balancer: https://ngrok.com/r/gslb

Session Status                online
Account                       zhaozhiming (Plan: Free)
Version                       3.3.5
Region                        Japan (jp)
Latency                       -
Web Interface                 http://127.0.0.1:4040
Forwarding                    https://e7d9-36-111-143-226.ngrok-free.app -> http://localhost:8501

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

最后我们通过窗口中的`https://e7d9-36-111-143-226.ngrok-free.app`地址就可以访问 Streamlit 服务了。

{% img /images/post/2023/10/chatglm3-streamlit.png 1000 600 %}

## 部署 API 服务

启动 API 服务，服务的默认端口是 7861：

```bash
python openai_api.py
```

该服务是兼容 OpenAI 接口，可以通过调用 OpenAI API 的方式来调用接口，注意要传递`model`参数，值为`gpt-3.5-turbo`：

```bash
curl -X 'POST' \
  'https://localhost:7861/v1/chat/completions' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "model": "gpt-3.5-turbo",
  "messages": [
    {
      "role": "user",
      "content": "你好"
  ]
}'

# 返回结果
{
  "model": "gpt-3.5-turbo",
  "object": "chat.completion",
  "choices": [
    {
      "index": 0,
      "message": {
        "role": "assistant",
        "content": "\n 你好！我是人工智能助手 ChatGLM3-6B，很高兴见到你，欢迎问我任何问题。",
        "metadata": null,
        "tools": null
      },
      "finish_reason": "stop",
      "history": null
    }
  ],
  "created": 1698825945,
  "usage": {
    "prompt_tokens": 8,
    "total_tokens": 38,
    "completion_tokens": 30
  }
}
```

## 部署综合 Demo 服务

ChatGLM3-6B 还提供了一个综合的 Demo，包含了对话、工具调用、代码解释器等功能，我们来部署这个 Demo 服务。

首先可以按照官方文档说明新建一个 python 环境，然后安装相关依赖：

```bash
cd composite_demo
conda create -n chatglm3-demo python=3.10
conda activate chatglm3-demo
pip install -r requirements.txt
```

安装 Jupyter 内核和设置本地模型路径，然后启动 WebUI 服务：

```bash
ipython kernel install --name chatglm3-demo --user
export MODEL_PATH=/root/autodl-tmp/chatglm3-6b
streamlit run main.py
```

Demo 里面除了对话功能外，还有工具调用和代码解释器功能，初始工具有两个，一个是天气查询，还有一个是随机数生成：

{% img /images/post/2023/10/chatglm3-tools1.jpg 1000 600 %}

{% img /images/post/2023/10/chatglm3-tools2.jpg 1000 600 %}

需要注意一点是：**ChatGLM3-6B-32K 模型是没有工具调用功能的**，只有 ChatGLM-6B 模型才有。

下面是代码解释器功能的截图，可以画爱心，还可以画饼图：

{% img /images/post/2023/10/chatglm3-ci1.jpg 1000 600 %}

{% img /images/post/2023/10/chatglm3-ci2.jpg 1000 600 %}

## 总结

上面介绍的是官方的部署，其实使用 FastChat 来部署更加简单，这种方式可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2023/08/22/use-fastchat-deploy-llm)，但是用 FastChat 可能无法使用 ChatGLM3-6B 的工具调用和代码解释器的功能。希望这篇文章能够帮助到大家，如果有什么问题可以在评论区留言。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
