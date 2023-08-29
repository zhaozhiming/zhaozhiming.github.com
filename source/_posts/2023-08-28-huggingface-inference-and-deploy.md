---
layout: post
title: HugggingFace 推理 API、推理端点和推理空间使用介绍
date: 2023-08-28 15:47:10
description: HugggingFace 推理 API、推理端点和推理空间使用介绍
keywords: huggingface, inference api, endpoint, space
comments: true
categories: ai
tags: [huggingface, inference api, endpoint, space]
---

{% img /images/post/2023/08/huggingface.jpg 400 300 %}

接触 AI 的同学肯定对[HuggingFace](https://huggingface.co/)有所耳闻，它凭借一个开源的 Transformers 库迅速在机器学习社区大火，为研究者和开发者提供了大量的预训练模型，成为机器学习界的 GitHub。在 HuggingFace 上我们不仅可以托管模型，还可以方便地使用各种模型的 API 进行测试和验证，部署属于自己的模型 API 服务，创建自己的模型空间，分享自己的模型。本文将介绍 HuggingFace 的推理 API、推理端点和推理空间的使用方法。

<!--more-->

## HuggingFace 推理 API

在 HuggingFace 托管的模型中，有些模型托管之后会提供推理 API，如果我们想快速验证模型是否可以满足我们的需求，可以使用这些 API 进行测试，下面以这个模型为例`Salesforce/blip-image-captioning-base`进行介绍，该模型可以通过图片生成英文描述。

### 页面小组件

推理 API 有**两种**使用方式，一种是在模型页面的右侧找到推理 API 的小组件页面，初始界面如下图所示：

{% img /images/post/2023/08/huggingface-api-widget.png 1000 600 %}

我们可以在这个页面中上传图片，然后就可以看到模型进行推理运行，等一会后推理结果就出来了，如下图所示：

{% img /images/post/2023/08/huggingface-api-widget-usage.png 600 400 %}

推理结果为："a dog wearing a santa hat and a red scarf"(一只狗戴着圣诞老人的帽子和红色的围巾)

页面小组件的方式是 HuggingFace 自动帮助模型创建的，具体的信息可以参考[这里](https://huggingface.co/docs/hub/models-widgets)。

### 代码调用

另外一种方式是通过代码对推理 API 进行调用，在右侧的`Deploy`菜单中选择`Inference API`，如下图所示：

{% img /images/post/2023/08/huggingface-api-menu.png 600 400 %}

打开菜单后可以看到几种代码调用方式，分别有 Python, JavaScript 和 Curl：

{% img /images/post/2023/08/huggingface-api-invoke.png 1000 600 %}

这里我们选择 Curl 方式来进行调用，我们可以直接复制界面上的 Curl 命令，注意其中包含了我们的 API token，所以不要随意分享出去，然后在终端上执行命令，就可以看到预测结果了：

```bash
$ curl https://api-inference.huggingface.co/models/Salesforce/blip-image-captioning-base \
	-X POST \
	--data-binary '@dogs.jpg' \
	-H "Authorization: Bearer hf_xxxxxxxxxxxxxxxxxxxxxx"

# 输出结果
[{"generated_text":"a dog wearing a santa hat and a red scarf"}]%
```

## HuggingFace 推理端点(Endpoint)

推理 API 虽然方便，但推理 API 一般用于测试和验证，由于速率限制，官方不推荐在生产环境中使用，而且也不是所有模型都有提供推理 API。如果想要在生产环境部署一个专属的推理 API 服务，我们可以使用 HuggingFace 的推理端点（Endpoint）。

推理端点的部署也比较简单，首先在`Deploy`菜单中选择`Inference Endpoints`，如下图所示：

{% img /images/post/2023/08/huggingface-endpoint-menu.png 600 400 %}

打开菜单后可以看到新建推理端点的界面，如下图所示：

{% img /images/post/2023/08/huggingface-endpoint-create.png 1000 600 %}

1. 首先是服务器的选择，先选择云服务厂商，目前只有 AWS 和 Azure 两种，再选择机器区域节点。
2. 然后是服务器的配置，HuggingFace 默认会给出模型的**最低推理配置**，如果我们想要更高的配置，可以点击`2`中的下拉框进行选择。
3. 接着是推理端点的安全等级，有 3 种选择，分别是`Protected`、`Public`和`Privaate`

   - Pubulic：推理端点运行在公共的 HuggingFace 子网中，互联网上的任何人都可以访问，无需任何认证。
   - Protected：推理端点运行在公共的 HuggingFace 子网，互联网上任何拥有合适 HuggingFace Token 的人都可以访问它。
   - Privacy：推理端点运行在私有的 HuggingFace 子网，不能通过互联网访问，只能通过你的 AWS 或 Azure 账户中的一个私有连接来使用，可以满足最严格的合规要求。

4. 最后显示的是服务器的价格，按小时算，根据配置的不同，价格也会有所不同。HuggingFace API 是免费的，但 HuggingFace 的推理端点是要收费的，毕竟是自己专属的 API 服务。因为推理端点部署是收费的，所以在部署之前需要在 HuggginFace 中添加付款方法，一般使用国内的 Visa 或 Master 卡就可以了。

信息确认无误后点击`Create Endpoint`按钮创建推理端点，创建成功后可以进入推理端点的详情页面看到如下信息：

{% img /images/post/2023/08/huggingface-endpoint-detail.png 1000 600 %}

其中`Endpoint URL`就是部署好的推理端点地址，我们可以跟调用推理 API 一样的方式来使用它，示例代码如下：

```bash
$ curl https://your-endpoint-url \
	-X POST \
	--data-binary '@dogs.jpg' \
	-H "Authorization: Bearer hf_xxxxxxxxxxxxxxxxxxxxxx"
```

## HuggingFace 模型空间(Space)

HuggingFace 推理端点是部署 API 服务，但是如果我们想要分享自己的模型，让别人可以直接在浏览器中使用模型的功能，这时候就需要使用 HuggingFace 的模型空间（Space）了。

要部署一个模型空间，首先在模型的`Deploy`菜单中选择`Spaces`，如下图所示：

{% img /images/post/2023/08/huggingface-space-menu.png 600 400 %}

选择菜单后可以看到空间创建的引导界面，如下图所示：

{% img /images/post/2023/08/huggingface-space-guide.png 1000 600 %}

界面中显示了启动模型的 Python 脚本，然后我们点击`Create new Space`按钮进入空间的创建页面，如下图所示：

{% img /images/post/2023/08/huggingface-space-create.png 1000 600 %}

在模型创建页面中，我们需要设置以下信息：

- 首先要指定空间的名称，一般以模型的名称命名。
- 然后选择空间的 SDK，目前有`Streamlit`、`Gradio`、`Docker`和`Static` 四种。
  - Streamlit：Streamlit 是一个可以帮助我们快速创建数据应用的 Python 库，可以在浏览器中直接使用模型，它相比`Gradio`可以支持更加丰富的页面组件，界面也更加美观。
  - Gradio：Gradio 也是一个编写 GUI 界面的 Python 库，相对`Streamlit`来说，它的 GUI 功能虽然比较少，但它的优势在于简单易用，一般演示的 Demo 用它就足够了。
  - Docker：推理空间也可以使用 Docker 容器进行部署，它内部支持了 10 种模版。
  - Static：静态页面，我理解是包括 Html、Js、Css 等前端资源来作为页面展示。
- 然后选择空间硬件，HuggingFace 为每个空间提供了一个免费的配置：2 核 CPU 16G 内存，用这个配置部署推理空间是免费的，如果你想要更高的配置，也可以选择付费的配置。

{% img /images/post/2023/08/huggingface-space-hardware.png 600 400 %}

- 最后是安全等级，有`Public`和`Private`两种，Public 是公开的，任何人都可以访问，但只有你的组织成员可以修改，Private 是私有的，只有你的组织成员可以访问。

设置完后点击`Create Space`按钮就开始创建推理空间了，创建完成后会自动跳转到空间的页面，如下图所示：

{% img /images/post/2023/08/huggingface-space-detail.png 1000 600 %}

如果推理空间的安全等级设置为 Public，你就可以将空间的 URL 分享给其他人使用了。想查看 HuggingFace 推理空间更多的信息，可以参考[这里](https://huggingface.co/docs/hub/spaces)。

## 总结

本文介绍了 HuggingFace 的推理 API、推理端点和推理空间的使用方法，推理 API 是免费的，使用 HuggingFace 自建的 API 服务，推理端点是部署自己专属的 API 服务，但需要收取一定的费用。推理空间是部署模型的 Web 页面，可以直接在浏览器中使用模型的功能，可以用于演示和分享模型，有一定的免费额度。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
