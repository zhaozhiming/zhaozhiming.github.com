---
layout: post
title: Semantic Kernel —— LangChain 的替代品？
date: 2023-10-02 14:02:51
description: Semantic Kernel —— LangChain 的替代品？
keywords: semantic-kernel, semantic function, native function, planner, ReAct, langchain
comments: true
categories: ai
tags:
  [
    semantic-kernel,
    semantic function,
    native function,
    planner,
    ReAct,
    langchain,
  ]
---

{% img /images/post/2023/10/semantic-kernel.png 400 300 %}

微软在 AI 领域的探索和投入从未间断，它不仅在前期对 OpenAI 进行了大量投资 ，还在其搜索引擎 Bing 和云服务 Azure 上集成了 ChatGPT，以增强用户体验和服务能力。最近，微软更是推出了一个名为 Semantic Kernel 的 AI 应用开发工具，旨在与 LangChain 竞争，展现了其在 AI 应用开发领域的持续创新和努力。今天我们就来了解下 Semantic Kernel 的特点和功能，以及它与 LangChain 的区别。

<!--more-->

## Semantic Kernel 是什么

[Semantic Kernel](https://github.com/microsoft/semantic-kernel) 是一个开源的软件开发套件，允许开发者将 AI 服务与传统编程语言结合起来，创建可以整合两者优势的 AI 应用。它位于 AI 应用架构的中心，允许开发者编排 AI 插件，同时扩展现有应用的功能。它为开发者提供了灵活集成 AI 服务到现有应用的能力，包括通过插件添加功能和扩展功能。为了简化创建人工智能应用程序的过程，出现了像 LangChain 这样的开源项目。Semantic Kernel 是微软在这个领域的贡献，旨在支持希望将人工智能集成到现有应用程序中的企业应用程序开发人员。

这是官方的介绍，我们还是通过一些实际的例子来了解 Semantic Kernel，看它是否能否达到 LangChain 一样的功能。

## 集成 AI 模型

目前 Semantic Kernel 支持的 AI 模型包括 OpenAI，Azure OpenAI 以及 HuggingFace 上的模型。OpenAI 和 Azure OpenAI 模型可以通过 Semantic Kernel 的`add_chat_service`方法来接入，示例代码如下：

```py
import semantic_kernel as sk
from semantic_kernel.connectors.ai.open_ai import OpenAIChatCompletion, AzureChatCompletion

kernel = sk.Kernel()
# 集成 OpenAI 模型
api_key, org_id = sk.openai_settings_from_dot_env()
kernel.add_chat_service("OpenAI_chat_gpt", OpenAIChatCompletion("gpt-3.5-turbo", api_key, org_id))
# 集成 Azure OpenAI 模型
deployment, api_key, endpoint = sk.azure_openai_settings_from_dot_env()
kernel.add_chat_service("Azure_curie", AzureChatCompletion(deployment, endpoint, api_key))
```

上面的代码中，我们分别接入和 OpenAI 和 Azure OpenAI 的模型来作为 Semantic Kernel 的 2 个聊天服务，模型所需的参数是从环境变量中获取，可以在项目根目录下创建一个`.env`文件来存放环境变量，示例代码如下：

```yaml
OPENAI_API_KEY=""
OPENAI_ORG_ID=""
AZURE_OPENAI_DEPLOYMENT_NAME=""
AZURE_OPENAI_ENDPOINT=""
AZURE_OPENAI_API_KEY=""
```

而要接入 HuggingFace 上的模型则需要通过 transformers 库来实现，示例代码如下：

```py
import semantic_kernel as sk
import semantic_kernel.connectors.ai.hugging_face as sk_hf

kernel = sk.Kernel()
kernel.add_text_completion_service(
    "gpt2", sk_hf.HuggingFaceTextCompletion("gpt2", task="text-generation")
)
```

使用 Semantic Kernel 添加 HuggingFace 上模型，会根据模型名称从 HuggingFace 上下载模型，或者根据模型的文件路径加载模型，跟接入 OpenAI 模型不同的地方是，接入 HuggingFace 模型需要在本地机器上运行模型，这往往意味着需要昂贵的 GPU 资源，而 OpenAI 模型是运行在 OpenAI 的服务器上，本地只是调用其 API 接口。

目前 Semantic Kernel 只支持以上这些模型的接入，相对于 LangChain 来说可接入的模型还是比较少的，LangChain 最大的好处是可以通过兼容 OpenAI API 的接口来接入本地的 LLM（大语言模型），这对于企业来说是非常有吸引力的，对于 LangChain 如何集成本地 LLM 可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2023/09/11/langchain-custom-llm/)。

## 语义函数 vs. 自然函数

Semantic Kernel 中的**语义函数**和**自然函数**是开发者创建自定义插件的两种方式。语义函数是用逻辑语言模型创建的，而自然函数则是用传统编程语言如 Python 编写的。语义函数通常用于处理与 AI 交互的任务，而自然函数则更适用于执行常规编程任务和集成现有应用。

### 语义函数

每个语义函数都会有 2 个文件——提示词模板文件和配置文件，我们来看一个**讲笑话**的语义函数例子，首先是提示词文件`skprompt.txt`：

```
请精确地写出一个关于以下主题的笑话或幽默故事：

笑话必须要符合以下要求：
- 简短，不超过100字
- 安全适用于职场和家庭
不得包含性别歧视、种族歧视或其他偏见/偏执行为。

请发挥创意，制造欢乐。我想笑一笑。
如果提供了风格建议，请将其融入其中：{{$style}}
+++++

{{$input}}
+++++
```

可以看到在提示词模板文件中用自然语言描述了这个语义函数的功能，像普通函数一样，语义函数也有参数，参数用`{{$}}`符号来表示，这里的参数有 2 个，一个是`input`，一个是`style`，这些参数会在配置文件中定义。我们再来看配置文件`config.json`：

```json
{
  "schema": 1,
  "description": "生成一个有趣的笑话",
  "type": "completion",
  "completion": {
    "max_tokens": 1000,
    "temperature": 0.9,
    "top_p": 0.0,
    "presence_penalty": 0.0,
    "frequency_penalty": 0.0
  },
  "input": {
    "parameters": [
      {
        "name": "input",
        "description": "笑话主题",
        "defaultValue": ""
      },
      {
        "name": "style",
        "description": "提示你想要的笑话风格",
        "defaultValue": ""
      }
    ]
  }
}
```

配置文件中定义了了这个语义函数的功能描述，LLM 的相关参数（temperature、top_p 等），以及语义函数的参数，这里的重点是几个`description`字段，这是让 LLM 决定是否调用该语义函数的关键。

这 2 个文件会放在同一个目录下，这个目录名我们可以叫`Joke`，然后它的上层还有一个`Skills`的父文件夹，目录结构如下：

```bash
├── Skills
│   └── Joke
│       ├── config.json
│       └── skprompt.txt
```

定义好语义函数后，我们可以这样来调用函数：

```py
skills_directory = "./"
funFunctions = kernel.import_semantic_skill_from_directory(skills_directory, "Skills")
jokeFunction = funFunctions["Joke"]
result = jokeFunction("穿越到恐龙时代")
print(result)
```

我们使用`import_semantic_skill_from_directory`方法来添加语义函数，注意这里添加的是`Skills`文件夹，然后再通过`funFunctions["Joke"]`来获取到我们定义的语义函数，最后调用该函数并传入参数，就可以得到结果了。

### 自然函数

自然函数就跟我们平时代码中写的函数一样，只不过要加一些 Semantic Kernel 的标签，我们来看一个**网络查询**的自然函数例子：

```py
class WebSearchEngineSkill:
    """
    一个搜索引擎技能。
    """

    from semantic_kernel.orchestration.sk_context import SKContext
    from semantic_kernel.skill_definition import sk_function

    def __init__(self, connector) -> None:
        self._connector = connector

    @sk_function(
        description="基于提供的语句进行网络查询",
        name="searchAsync",
        input_description="查询语句",
    )
    async def search_async(self, query: str) -> str:
        result = await self._connector.search_async(query, num_results=5, offset=0)
        return str(result)
```

这里的`@sk_function`就是 Semantic Kernel 的标签，`@sk_function`用来标记这是一个自然函数，标签中定义了自然函数的功能描述，调用的函数名和参数描述，后面我们会通过这里定义的函数名来调用该自然函数。在自然函数的实现中，我们使用网络连接器`connector`的 API 来进行网络查询，然后返回结果。

定义好自然函数后，我们可以这样来调用函数：

```py
import semantic_kernel as sk
from semantic_kernel.connectors.search_engine import BingConnector

BING_API_KEY = sk.bing_search_settings_from_dot_env()
connector = BingConnector(BING_API_KEY)
skill = kernel.import_skill(WebSearchEngineSkill(connector))
search = skill["searchAsync"]
result = search("2023年亚运会金牌最多的国家是哪个？")
print(f"result: {result}")
```

我们创建了一个 Bing 网络连接器，这里 Bing 的 API KEY 也存在`.env`文件中（如何获取 Bing 的 API KEY 可以查看[这里](https://www.microsoft.com/en-us/bing/apis/bing-web-search-api)，每个月有 1000 次的免费查询）。然后通过`import_skill`方法来添加自然函数，最后通过`skill["searchAsync"]`来获取到我们定义的自然函数，最后调用该函数并传入参数，就可以得到结果了。

在官方仓库中，Semantic Kernel 还提供了一系列常用的语义函数，如翻译、总结、写邮件等（见下图），以方便开发者直接使用。我个人认为，Semantic Kernel 在规范语义函数方面做得非常出色，例如语义函数的文件命名规范和目录结构规范等。这样一来，开发者能够更有效地开发语义函数，并保持每个语义函数的一致风格。

{% img /images/post/2023/10/sk-plugins.png 1000 600 %}

## Planner

了解 LangChain 的同学应该对其强大的 Agent 功能有所了解，不熟悉的同学也可以看下我之前[这篇关于 Agent 的文章](https://zhaozhiming.github.io/2023/09/06/langchain-agent-implementation-principle/)。而 Semantic Kernel 也有类似的功能——Planner。

在 Semantic Kernel 中，`Planner`是一个功能，它接收用户的请求，并返回完成请求所需的计划。它通过使用 AI 来混合和匹配在 Semantic Kernel 中注册的插件，以便将它们重新组合成一系列完成目标的步骤。这是一个强大的概念，因为它允许你创建可能连你都未曾想到的功能。例如，如果你有任务和日历事件插件，Planner 可以将它们组合起来创建工作流，例如"提醒我去商店时购买牛奶"或"提醒我明天给妈妈打电话"，而无需为这些情境明确编写代码。

Semantic Kernel 提供了几种开箱即用的 Planner，每种 Planner 有其特定的用途。这些 Planner 的目的是自动化地编排 AI 功能，使开发者能够构建复杂的 AI 应用，而无需为每个可能的用户请求手动创建工作流程。

### Action Planner

Action Planner 是其中一个开箱即用的 Planner，它的作用是根据用户问题，在一组函数（语义函数或自然函数）中找到最适合的一个函数来执行。我们来看一个例子：

```py
import asyncio
from semantic_kernel.planning import ActionPlanner

async def action_planner(ask: str):
    # kernel 中已添加之前演示的语义函数和自然函数
    planner = ActionPlanner(kernel)
    plan = await planner.create_plan_async(goal=ask)
    result = await plan.invoke_async()
    print(result)

# 下面的方法会调用讲笑话的语义函数
asyncio.run(action_planner("讲一个穿越到恐龙时代的笑话"))
# 下面的方法会调用网络查询的自然函数
asyncio.run(action_planner("上网查询一下2023年亚运会金牌最多的国家是哪个？"))
```

我们通过`ActionPlanner.create_plan_async`的方法来创建一个 Planner，这里的`goal`参数就是用户的问题，然后通过`invoke_async`方法来执行计划，最后就可以得到结果。我们在`kernel`中已添加了之前演示的 2 个函数：讲笑话的语义函数和网络查询的自然函数，ActionPlanner 在执行时会根据用户问题寻找到最适合的函数来执行，然后输出函数的执行结果。

需要注意到是，ActionPlanner 的返回结果是执行某个函数后的返回结果，它并不会经过 LLM 进行加工，也就是说函数返回什么，ActionPlanner 就会返回什么。如果你希望返回的结果更加理想，可以使用 LLM 结合问题和返回结果进行一次加工，这样就可以得到更加符合用户需求的结果了。

ActionPlanner 比较适合用来实现一些 AI 工具平台，用户可以根据自然语言来调用平台上的各种工具。LangChain 与之对应的是 [Conversational Agent](https://python.langchain.com/docs/modules/agents/agent_types/chat_conversation_agent)，它的功能更加强大，可以实现对话的历史信息管理，以及对返回结果进行加工等功能。

### Stepwise Planner

还有一个 Planner 是 Stepwise Planner，它基于 MRKL 和 ReAct 的基本原理，允许人工智能形成**想法**和**观察**，并基于这些执行动作来实现用户的目标。这一过程会一直持续，直到完成所有所需的功能并生成最终输出。我们来看它的一个简单示例：

```py
async def stepwise_planner(ask: str):
    # kernel 中已添加之前演示的语义函数和自然函数
    planner = StepwisePlanner(
        kernel, StepwisePlannerConfig(max_iterations=10, min_iteration_time_ms=1000)
    )
    plan = planner.create_plan(goal=ask)
    result = await plan.invoke_async()
    print(result)
    for index, step in enumerate(plan._steps):
        print("Step:", index)
        print("Description:", step.description)
        print("Function:", step.skill_name + "." + step._function.name)
        if len(step._outputs) > 0:
            print("  Output:\n", str.replace(result[step._outputs[0]], "\n", "\n  "))

asyncio.run(stepwise_planner("根据2023年亚运会金牌最多的国家查询结果，讲一个从这个国家穿越到恐龙时代的笑话"))
```

`StepwisePlanner`的使用和`ActionPlanner`有些类似，不同的地方是`StepwisePlanner`可以设置了最大迭代次数和最小迭代时间，最大迭代次数表示最多思考多少轮，到了最大的次数后即使没有最终答案也会退出程序。在上面的执行方法中，我们还打印了每一轮的思考过程，包括其调用的函数和输出的结果。

LangChain 与之对应的功能是[ReAct Agent](https://python.langchain.com/docs/modules/agents/agent_types/react)，也是 LangChain 比较常用的一种 Agent，Stepwise Planner 与 ReAct Agent 两者区别不大。

Semantic Kernel 的 Planner 功能和 LangChain 的 Agent 一样，需要在一些相对**智能**的 LLM 上运行才能取得好的效果，比如 GPT-4 或 GPT3.5，如果是在一些低参数的 LLM 上执行的话就会出现各种问题了。

## 与 LangChain 的对比

下面列举一些 Semantic Kernel 和 LangChain 的对比。

### 功能对比

| LangChain | Semantic Kernel             | 备注                               |
| --------- | --------------------------- | ---------------------------------- |
| Chains    | Kernel                      | 构造调用序列                       |
| Agents    | Planner                     | 自动创建工具以满足用户的新需求     |
| Tools     | Plugins (语义函数+自然函数) | 可以自定义工具来满足不同的应用场景 |
| Memory    | Memory                      | 保存对话上下文信息                 |

可以看到 LangChain 几个核心模块的功能 Semantic Kernel 都有对应的实现。

### 语言对比

| 语言       | LangChain | Semantic Kernel |
| ---------- | --------- | --------------- |
| Python     | ✅        | ✅              |
| JavaScript | ✅        | ❌              |
| C#         | ❌        | ✅              |
| Java       | ❌        | ✅              |

支持的语言对比（因为 Semantic Kernel 是用 C#开发的，所以它对 C#比较支持）如上所示。不清楚 Semantic Kernel 为什么要用 C#来开发，C#相比 Python 和 JavaScript 来说使用的人会少很多。

### 大语言模型的对比

| 模型                    | LangChain                       | Semantic Kernel                   |
| ----------------------- | ------------------------------- | --------------------------------- |
| Embedding 模型          | 超过 25 种不同的 Embedding 模型 | OpenAI, Azure OpenAI，HuggingFace |
| Completion 或 Chat 模型 | 超过 64 种不同的 LLM 模型       | OpenAI, Azure OpenAI，HuggingFace |

Semantic Kernel 只支持 OpenAI，Azure OpenAI，HuggingFace 上的模型，而 LangChain 支持的模型要多得多。

### 任务编排对比

| Type        | LangChain Agents | Semantic Kernel Planner |
| ----------- | ---------------- | ----------------------- |
| 对话        | ✅               | ✅ (ActionPlanner)      |
| 执行计划    | ✅               | ✅ (SequentialPlanner)  |
| ReAct       | ✅               | ✅ (StepwisePlanner)    |
| 思维树(ToT) | ✅               | ❌                      |

目前 Semantic Kernel 就只有几种 Planner，对比 LangChain 还是比较少的，但一般的应用场景也足够用了。

## 总结

Semantic Kernel 代表了微软在 AI 应用开发领域的探索，其功能和 LangChain 有所相似，但 LangChain 显得更为强大。得益于较早的推出时间，LangChain 在功能完善度、LLM 的支持以及应用开发场景的丰富性上均领先于 Semantic Kernel。然而，LangChain 的快速发展也带来了代码质量的问题，受到了一些人的诟病。相较之下，Semantic Kernel 吸纳了前者的宝贵经验，展现了更为优良的架构和代码质量，同时制定了更为合理的插件开发规范。随着微软对 AI 投入的不断加大，我们有理由相信 Semantic Kernel 将得到持续完善，并有望成为未来极具优势的 AI 应用开发框架。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
