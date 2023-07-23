---
layout: post
title: Llama2 部署及试用
date: 2023-07-23 14:59:45
description: 最新免费商用大模型 Llama2 的部署及免费试用
keywords: llama
comments: true
categories: ai
tags: [llama]
---

{% img /images/post/2023/07/llama2.jpg 400 300 %}

Llama 一直被视为 AI 界的开源大模型中的巨擘。然而，由于其开源协议的限制性条款，商业用途的免费使用一直未能实现。但这种情况在最近发生了根本性的转变，Meta 发布了备受瞩目的 Llama2，这是一个免费可供商业使用的版本。这个大型预训练语言模型 Llama2，出自 Meta AI 之手，它能接收任何形式的自然语言文本输入，并产生文本形式的输出。笔者也第一时间尝试了一下，这里记录一下 Llama2 的部署及免费试用方法。

<!--more-->

## Llama2 下载

要使用 Llama2 需要先下载它的模型，这里介绍几种下载模型的方式。

### 官方申请

官方渠道比较正规，但是需要申请，申请的过程其实并不复杂，只是需要花点时间等待审核通过，以笔者为例，申请后大概等了半个小时就收到了申请通过的邮件。

首先在[Meta 这个网站](https://ai.meta.com/llama/)上进行申请，点击`Download the Model`按钮。

{% img /images/post/2023/07/llama2-apply.png 1000 600 %}

然后在申请表格上填写申请信息，填完滑到最下面勾选同意协议，点击提交按钮就可以了。

{% img /images/post/2023/07/llama2-form.png 1000 600 %}

申请通过后收到的申请邮件如下，里面有一个下载链接，按照邮件中的说明使用下载链接下载模型，注意下载链接在 24 小时后会失效，且每个模型只能下载 5 次，失效后需要重新提交申请。

{% img /images/post/2023/07/llama2-email.png 1000 600 %}

### HuggingFace 下载

在 HuggngFace 上也可以下载 Llama2，但同样需要在官网先进行申请，注意官网申请的邮箱要和 HuggingFace 的邮箱保持一致。

进到 HuggingFace 的[Meta Llama 网站](https://huggingface.co/meta-llama)，可以看到有很多个 Llama2 的仓库，分不同量级、huggingface 版本，chat 版本等，我们随便选择一个仓库进入。

{% img /images/post/2023/07/hf-llama2.png 1000 600 %}

进入仓库后在 Readme 文档前面有一个访问限制说明，点击`Submit`按钮进行申请，只要申请其中一个仓库，Llama2 的其他仓库都会一并申请。

{% img /images/post/2023/07/hf-llama2-apply.png 1000 600 %}

点击`Submit`按钮后是下面的页面提示，接下来就等待申请通过了。

{% img /images/post/2023/07/hf-llama2-wait.png 1000 600 %}

### 非官方渠道下载

如果嫌官方申请比较慢，也可以通过非官方渠道下载，这里有好心人已经在 HuggingFace 上上传了 Llama2 的模型，可以直接下载使用，无需申请。

- 非官方地址：[TheBloke](https://huggingface.co/TheBloke)

{% img /images/post/2023/07/unofficial-llama2.png 1000 600 %}

## Llama2 部署

下载了模型后我们开始进行 Llama2 的部署，部署机器的话如果本地有 GPU 服务器的话最好，如果没有的话可以参照笔者之前的文章申请云 GPU 服务器使用。

### [Text-Web-Ui](https://github.com/oobabooga/text-generation-webui)

Llama2 的官方仓库没有提供 Web 运行的程序，只有两个命令行示例脚本，运行起来不太直观，我们希望能够通过浏览器进行访问，这里介绍通过 Text-Web-Ui 这个工具进行 Llama2 Web 方式的部署。

Text-Web-Ui 是一个用来运行 LLM（大语言模型）的 Web UI 框架，目标是在一套 Web UI 框架上运行所有 LLM，对标的是图像 AI 生成工具 [AUTOMATIC1111/stable-diffusion-webui](https://github.com/AUTOMATIC1111/stable-diffusion-webui)。

Text_Web-Ui 安装方式有两种，一种的是一键安装，另外一种手动安装，这里主要介绍一键安装的方式，手动安装的方式有兴趣的同学可以自行去官方仓库查看。

在这里下载安装包，各个平台的安装包都有，这里以 Linux 为例。

{% img /images/post/2023/07/text-web-ui-installer.png 1000 600 %}

下载后解压，运行里面的`start_linux.sh`脚本，脚本会自动检测系统的 Python 环境并安装工具所需的所有依赖，安装后启动 Web 服务。

{% img /images/post/2023/07/text-web-ui-start.png 1000 600 %}

然后回到 HuggingFace 的 Llama2 仓库点击 copy 按钮复制模型名称。

{% img /images/post/2023/07/text-web-ui-copy.png 1000 600 %}

在 Text-Web-Ui 的 Model 界面处粘贴刚复制的模型名称，点击`Download`按钮进行模型下载。

{% img /images/post/2023/07/text-web-ui-download.png 1000 600 %}

下载完成后，选择加载下载完成的 Llama2 模型，然后就可以和 LLM 进行对话了。

### colab 一键部署

如果觉得以上部署方式麻烦的话，也可以通过别人的 colab 链接一键部署 Text-Web-Ui 和 Llama2，这里有一个一键部署 colab 的代码仓库，选择其中的 Llama2 colab 链接运行，但这种方式只能部署在 colab 服务器上。

[text-generation-webui-colab](https://github.com/camenduru/text-generation-webui-colab)

{% img /images/post/2023/07/colab-llama2.png 1000 600 %}

## Llama2 免安装试用

如果觉得自己部署麻烦的话，HuggingFace 已经提供了在线试用 Llama2 的方式，可以直接在浏览器上进行试用。下面是 Llama2 几个量级的试用地址。

- [Llama2 7B](https://huggingface.co/spaces/huggingface-projects/llama-2-7b-chat)
- [Llama2 13B](https://huggingface.co/spaces/huggingface-projects/llama-2-13b-chat)
- [Llama2 70B](https://huggingface.co/chat)

{% img /images/post/2023/07/hf-chat.png 1000 600 %}

## 总结

Llama2 的发布是一个里程碑式的事件，它是一个免费可商用的大型预训练语言模型，可以接收任何形式的自然语言文本输入，并产生文本形式的输出。Llama2 的发布将会对 AI 产生深远的影响，它将会成为 AI 产业的一个重要组成部分，也将会成为 AI 产业的一个重要基础设施。希望今天的文章能够帮助到大家部署自己的 Llama2，如果在部署的过程中遇到问题，欢迎在评论区留言。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
