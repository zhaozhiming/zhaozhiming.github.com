---
layout: post
title: LangChain Agent 原理解析
date: 2023-09-06 19:53:31
description: LangChain Agent 原理解析
keywords: langchain, agent
comments: true
categories: ai
tags: [langchain, agent]
---

{% img /images/post/2023/09/langchain-agent.png 400 300 %}

LangChain 是一个基于 LLM（大型语言模型）的编程框架，旨在帮助开发人员使用 LLM 构建端到端的应用程序。它提供了一套工具、组件和接口，可以简化创建由 LLM 和聊天模型提供支持的应用程序的过程。LangChain 由几大组件构成，包括 Models，Prompts，Chains，Memory 和 Agent 等，而 Agent 是其中重要的组成部分，如果把 LLM 比做大脑的话，那 Agent 就是给大脑加上手和脚。今天就来带大家重点了解一下 Agent 以及它的工作原理。

<!--more-->

## 什么是 LangChain Agent

在 LangChain 中，Agent 是一个代理，接收用户的输入，采取相应的行动然后返回行动的结果。 Agent 可以看作是一个自带路由消费 Chains 的代理，基于 MRKL 和 ReAct 的基本原理，Agent 可以使用工具和自然语言处理问题。官方也提供了对应的 Agent，包括 OpenAI Functions Agent、Plan-and-execute Agent、Self Ask With Search 类 AutoGPT 的 Agent 等。 Agent 的作用是代表用户或其他系统完成任务，例如数据收集、数据处理、决策支持等。 Agent 可以是自主的，具备一定程度的智能和自适应性，以便在不同的情境中执行任务。我们今天主要了解基于 **ReAct** 原理来实现的 Agent。

## ReAct

ReAct 是一个结合了推理和行动的语言模型。虽然 LLM 在语言理解和交互决策制定方面展现出了令人印象深刻的能力，但它们的推理（例如链式思考提示）和行动（例如行动计划生成）的能力主要被视为两个独立的主题。ReAct 的目标是探索如何使用 LLM 以交错的方式生成推理痕迹和特定任务的行动，从而在两者之间实现更大的协同作用。

想象一下，你有一个智能助手机器人，名叫小明。你给小明一个任务：去厨房为你做一杯咖啡。小明不仅要完成这个任务，还要告诉你他是如何一步步完成的。

**没有 ReAct 的小明：**

1. 小明直接跑到厨房。
2. 你听到了一些声音，但不知道小明在做什么。
3. 过了一会儿，小明回来给你一杯咖啡。

这样的问题是，你不知道小明是怎么做咖啡的，他是否加了糖或奶，或者他是否在过程中遇到了任何问题。

**有 ReAct 的小明：**

1. 小明告诉你：“我现在去厨房。”
2. 小明再说：“我找到了咖啡粉和咖啡机。”
3. “我现在开始煮咖啡。”
4. “咖啡煮好了，我要加点糖和奶。”
5. “好了，咖啡做好了，我现在给你拿过去。”

这次，你完全知道小明是怎么做咖啡的，知道他的每一个步骤和决策。

ReAct 就是这样的原理。它不仅执行任务（行动），还会告诉你它是如何思考和决策的（推理）。这样，你不仅知道任务完成了，还知道为什么这样做，如果有问题，也更容易找出原因。

更多关于 ReAct 的内容可以查看[这篇文章](https://react-lm.github.io/)。

## 自定义 LLM Agent

LangChain 在官方网站上提供了关于如何[创建自定义 LLM Agent](https://python.langchain.com/docs/modules/agents/how_to/custom_llm_agent)的例子，在官网的示例中，我们除了看到自定义 LLM Agent 外，还有一个自定义 Agent，这两者的区别就是自定义 LLM Agent 使用了 LLM 来解析用户输入，判断使用何种工具，而自定义 Agent 则是直接自行判断工具的使用，这种方式只能用于简单的场景，而自定义 LLM Agent 可以用于更复杂的场景。

在官方示例中，对其实现原理做了一些简单的描述，介绍了其组成部分和流程等，但如果是刚开始了解 Agent 的同学看起来可能会一知半解，所以我们今天主要结合其中的示例代码来了解自定义 LLM Agent 的实现原理。

## 提示词模板

要实现 Agent，我们需要先定义一套基于 ReAct 的提示词模板，示例中的 Agent 就是基于 ReAct 原理来实现的，为了方便理解，我们将官方的英文提示词模板换成中文和去掉一些没必要的内容，修改后的提示词模板内容如下：

```python
template = """尽你所能回答以下问题，你可以使用以下工具：

{tools}

请按照以下格式：

问题：你必须回答的输入问题
思考：你应该始终考虑该怎么做
行动：要采取的行动，应该是[{tool_names}]中的一个
行动输入：行动的输入
观察：行动的结果
... (这个思考/行动/行动输入/观察可以重复N次)
思考：我现在知道最终答案了
最终答案：对原始输入问题的最终答案

开始吧！

问题：{input}
{agent_scratchpad}"""
```

简单介绍一下这个提示词模板，首先提示词模板中会引用到一些工具，可以看到模板中有 2 个变量：`tools`和`tool_names`，tools 变量是一个列表，包含了所有的工具，列表中的每个元素包含了工具的名称和描述，而 tool_names 变量是工具名称的列表，传入具体的工具后，会生成对应的工具列表，比如我们有如下 2 个工具，解析后如下所示：

```bash
尽你所能回答以下问题，你可以使用以下工具：

search: 实时联网搜索的工具
math: 数学计算的工具

请按照以下格式：
......
行动：要采取的行动，应该是[search, math]中的一个
......
```

这样 LLM 在执行任务时就知道要使用哪些工具，以及在提取信息时可以提取到正确的工具名。

模板中下面的`思考/行动/行动输入/观察`就是标准的 ReAct 流程，思考是指思考如何解决问题，行动是具体的工具，行动输入是工具用到的参数，观察是工具执行完成后得到的结果，这个流程可以重复多次，直到最终得到最终答案。

模板最后还有 2 个变量：`input`和`agent_scratchpad`，input 是用户输入的问题，agent_scratchpad 是之前的思考过程（下面解析代码时会讲），包括了思考、行动、行动输入和观察等，这个变量在 Agent 执行过程中会被更新，代入具体的值后，模板会生成如下的提示词：

```bash
......
问题：北京的天气怎么样
思考: 我们需要通过 search 工具查找北京天气。
行动: search
行动输入: "北京天气"
观察: 6日（今天）. 多云转晴. 32/22℃. <3级
思考:
```

从问题下面一句到最后结束就是`agent_scratchpad`的值。

## 构造提示词

准备好提示词模板后，我们就可以构造提示词了，构造提示词的官方示例代码如下：

```python
# Set up a prompt template
class CustomPromptTemplate(StringPromptTemplate):
    # The template to use
    template: str
    # The list of tools available
    tools: List[Tool]

    def format(self, **kwargs) -> str:
        # Get the intermediate steps (AgentAction, Observation tuples)
        # Format them in a particular way
        intermediate_steps = kwargs.pop("intermediate_steps")
        thoughts = ""
        for action, observation in intermediate_steps:
            thoughts += action.log
            thoughts += f"\nObservation: {observation}\nThought: "
        # Set the agent_scratchpad variable to that value
        kwargs["agent_scratchpad"] = thoughts
        # Create a tools variable from the list of tools provided
        kwargs["tools"] = "\n".join([f"{tool.name}: {tool.description}" for tool in self.tools])
        # Create a list of tool names for the tools provided
        kwargs["tool_names"] = ", ".join([tool.name for tool in self.tools])
        return self.template.format(**kwargs)

prompt = CustomPromptTemplate(
    template=template,
    tools=tools,
    # This omits the `agent_scratchpad`, `tools`, and `tool_names` variables because those are generated dynamically
    # This includes the `intermediate_steps` variable because that is needed
    input_variables=["input", "intermediate_steps"]
)
```

首先我们需要定义一个类，继承自`StringPromptTemplate`，然后实现`format`方法，这个方法的作用是将提示词模板中的变量代入具体的值，然后返回提示词。其中`intermediate_steps`是中间 Agent 思考的步骤，每个步骤是一个元组，包含了`AgentAction`（行为和行为输入）和`Observation`（观察）的值，这个变量不会直接传递到 LLM，所以它不会在提示词中出现，但提示词模板会将它转换为`agent_scratchpad`变量，这个变量也就是我们在上面提到的`agent_scratchpad`，这个变量的值是 Agent 思考的过程，包括了思考、行动、行动输入和观察等。

format 方法最后会设置模板中的`tools`和`tool_names`变量，这两个变量的值是提示词模板中的工具列表，这个列表是根据`tools`变量生成的，包含了所有的工具，列表中的每个元素包含了工具的名称和描述，而`tool_names`变量是工具名称的列表。

最后我们需要创建一个`CustomPromptTemplate`对象，传入提示词模板和工具列表，这个对象就是我们最终要传入 LLM 的提示词。

## 工具解析

接下来是输出结果的解析，其中分为 2 个部分，一个是工具的解析，一个是结果的解析，我们来看下官方的示例代码：

```python
class CustomOutputParser(AgentOutputParser):

    def parse(self, llm_output: str) -> Union[AgentAction, AgentFinish]:
        # 暂时不看结果解析的代码

        # Parse out the action and action input
        regex = r"Action\s*\d*\s*:(.*?)\nAction\s*\d*\s*Input\s*\d*\s*:[\s]*(.*)"
        match = re.search(regex, llm_output, re.DOTALL)
        if not match:
            raise OutputParserException(f"Could not parse LLM output: `{llm_output}`")
        action = match.group(1).strip()
        action_input = match.group(2)
        # Return the action and action input
        return AgentAction(tool=action, tool_input=action_input.strip(" ").strip('"'), log=llm_output)
```

`parse` 方法前半部分是关于结果解析的，我们待会再讲，我们先看后面的代码，这是对工具的解析。

代码使用了一个正则表达式来解析 LLM 关于思考过程的输出结果，一般思考过程的输出结果是这样的格式：

```bash
思考: 我们需要通过 search 工具查找北京天气。
行动: search
行动输入: "北京天气"
```

根据我们的提示词模板，LLM 会智能地返回我们制定好的格式，有思考、行动、行动输入几项内容，我们主要通过这个正则表达式来获取到`行动`和`行动输入`这两项的值（也就是工具名称和工具所需参数），这样 Agent 就知道该如何使用工具了。可以看到代码中 action（工具）获取的是正则结果中的第一个分组的值，而 action_input（工具参数）获取的是第二个分组的值。最后将这 2 个值封装成一个 AgentAction 对象返回。

但需要注意的是，虽然我们设置好了提示词模板，但如果 LLM 不够**智能**的话，返回的结果可能会和我们预期的不一样，比如 LLM 可能返回这样的结果：

```bash
思考: 我们需要通过 search 工具查找北京天气。
行动: 我需要使用search工具来查询
行动输入: "北京天气"
```

可以看到行动的值不是我们预期的 search，而是一句话，这样通过正则表达式获取的工具名就是`我需要使用search工具来查询`，然后 Agent 会拿这个工具名去匹配工具，然后去调用，发现没有这个工具，就会报错。所以如果我们发现 LLM 有时候返回的结果不符合我们预期时，我们需要通过其他方式来解析出工具的名称，比如去掉一些无用的内容，只保留工具名称，这样才能保证 Agent 能够正常使用工具。

## 结果解析

我们再说下结果的解析，就是上面代码中 `parse`方法的前半部分：

```python
class CustomOutputParser(AgentOutputParser):

    def parse(self, llm_output: str) -> Union[AgentAction, AgentFinish]:
        # Check if Agent should finish
        if "Final Answer:" in llm_output:
            return AgentFinish(
                # Return values is generally always a dictionary with a single `output` key
                # It is not recommended to try anything else at the moment :)
                return_values={"output": llm_output.split("Final Answer:")[-1].strip()},
                log=llm_output,
            )
```

示例代码中判断 LLM 的输出是否有包含`最终答案:`，如果有的话，就说明 LLM 已经得到了最终的答案，这时就可以返回最终的答案了，这个格式也是我们在提示词模板中定义的：

```python
"""
思考：我现在知道最终答案了
最终答案：对原始输入问题的最终答案
"""
```

这个最终的答案是 LLM 输出结果中的最后一句话，我们可以通过`split`方法来获取到最后一句话，然后返回一个`ActionFinish`对象，这个对象包含了最终的答案，解析出来的结果如下：

```bash
# 思考过程
思考：我现在知道最终答案了
最终答案: 北京的天气情况如下：6日（今天）多云转晴，温度在32/22℃，风力小于3级

# 最终结果
北京的天气情况如下：6日（今天）多云转晴，温度在32/22℃，风力小于3级
```

跟工具解析一样，我们在结果解析时也需要注意 LLM 返回的结果是否符合我们预期，比如有时候 LLM 会输出这样的结果：

```bash
思考：我现在知道最终答案了
北京的天气情况如下：6日（今天）多云转晴，温度在32/22℃，风力小于3级
```

可以看到 LLM 的输出没有包含`最终结果:`关键字，这样示例代码中的 if 逻辑就不生效了，会导致解析逻辑进到工具解析那一部分去，然后引发错误。所以我们在解析结果时，要提高我们解析程序的健壮性，以满足不同的 LLM 输出结果，或者调整我们的提示词模板，让 LLM 返回的结果更加准确。

## 中断提示

最后一步是创建 Agent，示例代码如下：

```python
llm = OpenAI(temperature=0)
# LLM chain consisting of the LLM and a prompt
llm_chain = LLMChain(llm=llm, prompt=prompt)

tool_names = [tool.name for tool in tools]
agent = LLMSingleActionAgent(
    llm_chain=llm_chain,
    output_parser=output_parser,
    stop=["\nObservation:"],
    allowed_tools=tool_names
)
```

这里重点关注的是 Agent 中的`stop`参数，我们知道一般 LLM 都会长篇大论，说一大堆废话，我们希望 LLM 在返回了我们需要的信息后就停止输出，这里就需要用到`stop`参数，这个参数是一个列表，列表中的每个元素都是一个字符串，代表了 LLM 输出中的某一句话，当 LLM 输出中包含了这句话时，LLM 就会停止输出，这样我们就可以只获取到我们需要的信息了，这里我们使用`观察`关键字来停止 LLM 的输出。

## 总结

自定义 LLM Agent 的示例代码我们已经介绍完了，最后我们再讲下来 Agent 中使用的 LLM。在官方示例中，LLM 用的是 OpenAI，也就是`gpt-3.5`这个模型，但如果想达到更好的效果的话，推荐使用 OpenAI 的`gpt-4`模型，它是目前最好的 LLM，如果使用的 LLM 比较差，就容易出现刚才我们提到 LLM 返回的结果不符合我们预期的情况。

有人希望通过一些开源的 LLM 来实现 ReAct Agent，但实际开发过程中会发现开源低参数（比如一些 6B、7B 的 LLM）的 LLM 对于提示词的理解会非常差，根本不会按照提示词模板的格式来输出，这样就会导致我们的 Agent 无法正常工作，所以如果想要实现一个好的 Agent，还是需要使用好的 LLM，目前看来使用`gpt-3.5`模型是最低要求。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
