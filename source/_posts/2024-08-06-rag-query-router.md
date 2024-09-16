---
layout: post
title: 高级 RAG 检索策略之查询路由
date: 2024-08-06 09:10:16
description: 介绍 RAG 检索策略中的查询路由，以及如何在检索时使用
keywords: rag, llamaindex, router, semantic-router
comments: true
categories: ai
tags: [rag, llamaindex, router, semantic-router]
---

{% img /images/post/2024/08/rag-query-router.jpg 400 300 %}

之前介绍 Self-RAG 的时候提到了其中的按需检索功能，就是根据用户的问题来判断是否需要进行文档检索，如果不需要检索的话则直接返回 LLM（大语言模型）生成的结果，这样不仅可以提升系统的性能，还可以提高用户的体验。在 Self-RAG 中按需检索是通过特殊训练后的 LLM 来实现的，但是在高级 RAG（Retrieval Augmented Generation）检索中我们可以使用**查询路由**来实现这个功能，借助查询路由我们可以轻松实现类似代码中的 If/Else 功能。今天我们就来介绍查询路由的原理以及实现方式，并通过代码示例来了解查询路由在实际项目中的使用。

<!--more-->

## 查询路由

查询路由是 RAG 中的一种智能查询分发功能，能够根据用户输入的语义内容，从多个选项中选择最合适的处理方式或数据源。查询路由能够显著提高 RAG 检索的相关性和效率，适用于各种复杂的信息检索场景，如将用户查询分发到不同的知识库。查询路由的灵活性和智能性使其成为构建高效 RAG 系统的关键组件。

{% img /images/post/2024/08/rag-router-flow.png 1000 600 %}

### 查询路由的类型

根据查询路由的实现原理我们可以将其分为两种类型：

- LLM Router：通过构建有效的提示词来让 LLM 判断用户问题的意图，现有的实现有 LlamaIndex Router 等。
- Embedding Router: 通过 Embedding 模型将用户问题转为向量，然后通过相似性检索来判断用户问题的意图，现有的实现有 Semantic Router 等。

下面我们就来了解这两种查询路由具体的实现原理。

## LLM Router

使用 LLM 来判断用户的意图目前是 RAG 中一种常见的路由方法，首先在提示词中列出问题的所有类别，然后让 LLM 将问题进行分类，最后根据分类结果来选择相应的处理方式。

LLM 应用框架 [LlamaIndex](https://www.llamaindex.ai/) 使用的就是 LLM Router。在 LlamaIndex 中有几种查询路由的实现，比如路由检索器 `RouterRetriever`、路由查询引擎 `RouterQueryEngine`、流水线路由模块 `RouterComponent`，它们的实现原理基本一致，初始化时需要一个选择器和一个工具组件列表，通过选择器来得到工具组件序号，然后根据序号来选择相应的工具组件，最后执行工具组件的处理逻辑。以 `RouterQueryEngine` 为例，其示例代码如下：

```py
from llama_index.core.query_engine import RouterQueryEngine
from llama_index.core.selectors import LLMSingleSelector
from llama_index.core.tools import QueryEngineTool

# initialize tools
list_tool = QueryEngineTool.from_defaults(
    query_engine=list_query_engine,
    description="Useful for summarization questions related to the data source",
)
vector_tool = QueryEngineTool.from_defaults(
    query_engine=vector_query_engine,
    description="Useful for retrieving specific context related to the data source",
)

# initialize router query engine (single selection, llm)
query_engine = RouterQueryEngine(
    selector=LLMSingleSelector.from_defaults(),
    query_engine_tools=[
        list_tool,
        vector_tool,
    ],
)
query_engine.query("<query>")
```

- 首先我们构建 2 个工具 `list_tool` 和 `vector_tool`，分别用于总结问题和向量查询，`list_tool`使用 `SummaryIndex`来构建检索引擎，`vector_tool`使用 `VectorStoreIndex` 来构建检索引擎
- 然后初始化 `RouterQueryEngine`，传入选择器和工具列表
- 这里的选择器是 `LLMSingleSelector`，该选择器使用 LLM 判断用户问题意图并返回单个选择结果
- 最后调用 `query_engine.query` 方法传入用户问题，`RouterQueryEngine` 根据问题选择相应的工具并执行

下面是 LlamaIndex Router 的流程图：

{% img /images/post/2024/08/llamaindex-router-flow.png 400 600 %}

- 首先选择器根据用户问题得到选择结果
- 对选择结果进行数据提取，得到工具组件序号
- 根据序号选择工具列表中的组件并执行

在 LlamaIndex 中选择器有 4 种，如下图所示：

{% img /images/post/2024/08/llamaindex-selector.png 1000 600 %}

这 4 种选择器都是通过 LLM 来判断用户问题的意图，按选择结果可以分为单个结果选择器和多个结果选择器，单个结果选择器只返回一个选择结果，多个结果选择器返回多个选择结果，然后会将多个结果合并为一个最终结果。

按解析结果可以分为文本结果选择器和对象结果选择器，文本结果选择器使用的是 LLM 的 completion API 来生成文本类型的选择结果，格式为：`<index>. <reason>`，`index`为选择结果的序号，`reason`为选择结果的原因。对象结果选择器使用的是 LLM 的 Function Calling API，将选择结果解析成一个 Python 对象，默认的对象为 `SingleSelection`，其定义如下：

```py
class SingleSelection(BaseModel):
    """A single selection of a choice."""

    index: int
    reason: str
```

2 种解析结果示例如下所示：

```sh
# Text selector
2. Useful for questions related to oranges

# Object selector
SingleSelection(index=2, reason="Useful for questions related to oranges")
```

使用文本结果选择器得到选择结果后，还需要进行额外处理，比如提取出结果中的序号，而使用对象结果选择器则不需要额外处理，可以直接使用对象的属性得到结果。

我们再来看下选择器的提示词模板：

```py
DEFAULT_SINGLE_SELECT_PROMPT_TMPL = (
    "Some choices are given below. It is provided in a numbered list "
    "(1 to {num_choices}), "
    "where each item in the list corresponds to a summary.\n"
    "---------------------\n"
    "{context_list}"
    "\n---------------------\n"
    "Using only the choices above and not prior knowledge, return "
    "the choice that is most relevant to the question: '{query_str}'\n"
)
```

- 这是 `LLMSingleSelector` 的默认提示词模板
- `{num_choices}` 为选择结果的数量
- `{context_list}` 为工具组件列表的文本描述，包括序号和描述
- `{query_str}` 为用户问题

使用 LLM Router 的一个关键就是构建有效的提示词，如果使用的 LLM 足够强大，那么提示词不用很清晰也能达到我们想要的效果，但如果 LLM 不够强大，那么提示词需要不断调整才能得到满意的结果。笔者在使用 LlamaIndex Router 的过程中发现，在选择 OpenAI `gpt-3.5-turbo` 模型的情况下，使用 `LLMSingleSelector` 选择器时偶尔会出现解析失败的情况，而使用 `PydanticSingleSelector` 选择器则比较稳定。

最后得到选择结果的序号后就可以通过该序号来选择工具组件了，下面是 `RouterQueryEngine` 的代码片段：

```py
class RouterQueryEngine(BaseQueryEngine):
    def _query(self, query_bundle: QueryBundle) -> RESPONSE_TYPE:
        ......
        result = self._selector.select(self._metadatas, query_bundle)
        selected_query_engine = self._query_engines[result.ind]
        final_response = selected_query_engine.query(query_bundle)
        ......
```

- `RouterQueryEngine` 的 `_query` 方法中首先通过选择器得到选择结果
- 然后根据选择结果的序号在 `_query_engines` 中选择相应的检索引擎
- 最后调用检索引擎的 `query` 方法生成最终结果

### 优缺点

- 优点：方法简单，易于实现
- 缺点：需要一个比较强大的 LLM 才能正确判断用户问题的意图，如果要将选择结果解析为对象还需要 LLM 具备 Function Calling 的能力

## Embedding Router

查询路由的另外一种实现方式是使用 Embedding 模型将用户问题进行向量化，然后通过向量相似性来将用户问题进行分类，得到分类结果后再选择相应的处理方式。

[Semantic Router](https://github.com/aurelio-labs/semantic-router) 是基于该原理实现的一个路由工具，它旨在提供超快的 AI 决策能力，通过语义向量进行快速决策，以提高 LLM 应用和 AI Agent 的效率。 Semantic Router 使用非常简单，示例代码如下：

```py
import os
from semantic_router import Route
from semantic_router.encoders import CohereEncoder, OpenAIEncoder
from semantic_router.layer import RouteLayer

# we could use this as a guide for our chatbot to avoid political conversations
politics = Route(
    name="politics",
    utterances=[
        "isn't politics the best thing ever",
        "why don't you tell me about your political opinions",
        "don't you just love the president",
        "they're going to destroy this country!",
        "they will save the country!",
    ],
)

# this could be used as an indicator to our chatbot to switch to a more
# conversational prompt
chitchat = Route(
    name="chitchat",
    utterances=[
        "how's the weather today?",
        "how are things going?",
        "lovely weather today",
        "the weather is horrendous",
        "let's go to the chippy",
    ],
)

# we place both of our decisions together into single list
routes = [politics, chitchat]

# OpenAI Encoder
os.environ["OPENAI_API_KEY"] = "<YOUR_API_KEY>"
encoder = OpenAIEncoder()

rl = RouteLayer(encoder=encoder, routes=routes)

rl("don't you love politics?").name
# politics
rl("how's the weather today?").name
# chitchat
```

- 首先定义 2 个 Route，分别是 `politics` 和 `chitchat`，每个 Route 包含多个示例语句
- 然后创建一个 Encoder，这里使用的是 OpenAI 的 Encoder，利用 OpenAI 的 Embedding 模型来生成向量
- 最后创建一个 RouteLayer，传入 Encoder 和 Route 列表
- 调用 RouteLayer 方法传入用户问题，得到分类结果，**注意**：并不是每一个用户问题都会得到一个预设的分类结果，如果用户问题不在预设的分类中，那么分类结果可能为空

OpenAI Encoder 默认使用的是 `text-embedding-3-small` Embedding 模型，它比 OpenAI 之前的 `text-embedding-ada-002` Embedding 模型效果更好且价格更便宜。同时 Semantic Router 还支持其他 Encoder，比如 Huggingface Encoder，它默认使用的是 [sentence-transformers/all-MiniLM-L6-v2](https://huggingface.co/sentence-transformers/all-MiniLM-L6-v2) Embedding 模型，这是一个句子转换模型，它将句子和段落映射到一个 384 维度的向量空间，可用于分类或语义搜索等任务。

### 优缺点

- 优点：只需要使用 Embedding 模型，相比 LLM Router 效率更高，消耗资源更少
- 缺点：需要提前录入一些示例语句，如果示例语句不够多或者不够全面，分类效果可能不太好

## 查询路由实践

下面我们结合 LlamaIndex 和 Semantic Router 来实现一个查询路由，该路由会将用户的问题分发到不同的工具组件中，这些工具组件包括：使用 LLM 和用户进行闲聊，使用 RAG 流程检索文档并生成答案，以及使用 Bing 搜索引擎进行网络搜索。

{% img /images/post/2024/08/rag-router-practice.png 1000 600 %}

首先我们定义一个与 LLM 闲聊的工具组件，这里我们使用 LlamaIndex 的 [Pipeline](https://docs.llamaindex.ai/en/stable/examples/pipeline/query_pipeline/) 功能来构建一个查询流水线，更多的查询流水线功能可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2024/06/08/rag-module-pipeline/)，示例代码如下：

```py
from llama_index.llms.openai import OpenAI
from llama_index.core.query_pipeline import QueryPipeline, InputComponent

llm = OpenAI(model="gpt-3.5-turbo", system_prompt="You are a helpful assistant.")
chitchat_p = QueryPipeline(verbose=True)
chitchat_p.add_modules(
    {
        "input": InputComponent(),
        "llm": llm,
    }
)
chitchat_p.add_link("input", "llm")
output = chitchat_p.run(input="hello")
print(f"Output: {output}")

# 显示结果
Output: assistant: Hello! How can I assist you today?
```

- 这里我们使用 OpenAI 的 `gpt-3.5-turbo` 模型来构建一个 LLM
- 然后使用 `QueryPipeline` 来构建一个查询流水线，添加 `input` 和 `llm` 两个模块，`input`模块是一个输入组件，默认输入参数键名称为 `input`
- 接着添加两个模块的连接关系
- 最后调用 `run` 方法传入用户问题，得到回答

然后我们再添加一个普通 RAG 的工具组件，同样是创建一个查询流水线，这里的测试文档还是用维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情，示例代码如下：

```py
from llama_index.core import SimpleDirectoryReader, VectorStoreIndex
from llama_index.core.response_synthesizers.tree_summarize import TreeSummarize

documents = SimpleDirectoryReader("./data").load_data()
index = VectorStoreIndex.from_documents(documents)
retriever = index.as_retriever(similarity_top_k=2)
rag_p = QueryPipeline(verbose=True)
rag_p.add_modules(
    {
        "input": InputComponent(),
        "retriever": retriever,
        "output": TreeSummarize(),
    }
)

rag_p.add_link("input", "retriever")
rag_p.add_link("input", "output", dest_key="query_str")
rag_p.add_link("retriever", "output", dest_key="nodes")
output = rag_p.run(input="Which two members of the Avengers created Ultron?")
print(f"Output: {output}")

# 显示结果
Output: Tony Stark and Bruce Banner.
```

- 前面部分是 LlamaIndex 常用的检索器构建流程，使用 `SimpleDirectoryReader` 来加载测试文档，然后使用 `VectorStoreIndex` 来构建一个检索器
- 创建一个查询流水线，添加 `input`、`retriever` 和 `output` 三个模块，`output` 模块是一个树形总结组件
- 添加三个模块的连接关系，`output`模块需要使用到 `input` 模块和 `retirever` 模块的输出结果
- 最后调用 `run` 方法传入用户问题，得到回答

接下来我们再添加一个使用 Bing 搜索引擎的工具组件，同样我们使用查询流水线来进行创建，但这一次需要用到自定义模块，示例代码如下：

```py
web_p = QueryPipeline(verbose=True)
web_p.add_modules(
    {
        "input": InputComponent(),
        "web_search": WebSearchComponent(),
    }
)
web_p.add_link("input", "web_search")
```

- 网络搜索工具比较简单，只有 2 个模块，`input` 和 `web_search`
- 其中的 `WebSearchComponent` 是一个自定义模块，下面我们会详细介绍这个模块的实现

在实现这个自定义模块之前，我们需要先在 Azure 上创建一个 Bing 搜索服务，然后获取 API Key，具体操作可以参考微软的[官方文档](https://learn.microsoft.com/en-us/bing/search-apis/bing-web-search/overview)。然后安装 LlamaIndex 的 Bing 查询工具库：`pip install llama-index-tools-bing-search`，然后就可以开始实现自定义模块了，示例代码如下：

```py
import os
from typing import Dict, Any
from llama_index.core.query_pipeline import CustomQueryComponent
from llama_index.tools.bing_search import BingSearchToolSpec
from llama_index.agent.openai import OpenAIAgent

class WebSearchComponent(CustomQueryComponent):
    """Web search component."""

    def _validate_component_inputs(self, input: Dict[str, Any]) -> Dict[str, Any]:
        """Validate component inputs during run_component."""
        assert "input" in input, "input is required"
        return input

    @property
    def _input_keys(self) -> set:
        """Input keys dict."""
        return {"input"}

    @property
    def _output_keys(self) -> set:
        return {"output"}

    def _run_component(self, **kwargs) -> Dict[str, Any]:
        """Run the component."""
        tool_spec = BingSearchToolSpec(api_key=os.getenv("BING_SEARCH_API_KEY"))
        agent = OpenAIAgent.from_tools(tool_spec.to_tool_list())
        question = kwargs["input"]
        result = agent.chat(question)
        return {"output": result}
```

- 我们直接看自定义组件中的核心方法 `_run_component`
- 首先我们创建一个 `BingSearchToolSpec` 对象，传入 Bing 搜索引擎的 API Key，这里我们将 API Key 保存到环境变量 `BING_SEARCH_API_KEY` 中
- 这里我们使用了 LlamaIndex 的 Agent 功能，我们使用 `OpenAIAgent` 对象并传入 Bing 搜索工具
- 最后通过 `kwargs["input"]` 获取用户问题并传递给 `agent.chat` 方法，得到搜索结果并返回
- Bing 查询工具更多的用法可以参考[其文档](https://llamahub.ai/l/tools/llama-index-tools-bing-search?from=)

3 个工具组件创建之后，我们需要创建一个路由模块，我们使用 Semantic Router 来实现这个路由模块，我们先定义 Semantic Router 的几个 Route，示例代码如下：

```py
chitchat = Route(
    name="chitchat",
    utterances=[
        "how's the weather today?",
        "how are things going?",
        "lovely weather today",
        "the weather is horrendous",
        "let's go to the chippy",
    ],
)

rag = Route(
    name="rag",
    utterances=[
        "What mysterious object did Loki use in his attempt to conquer Earth?",
        "Which two members of the Avengers created Ultron?",
        "How did Thanos achieve his plan of exterminating half of all life in the universe?",
        "What method did the Avengers use to reverse Thanos' actions?",
        "Which member of the Avengers sacrificed themselves to defeat Thanos?",
    ],
)

web = Route(
    name="web",
    utterances=[
        "Search online for the top three countries in the 2024 Paris Olympics medal table.",
        "Find the latest news about the U.S. presidential election.",
        "Look up the current updates on NVIDIA’s stock performance today.",
        "Search for what Musk said on X last month.",
        "Find the latest AI news.",
    ],
)
```

- 这里我们定义了 3 个 Route，分别针对 3 种不同的问题类型
- `chitchat` Route 的示例语句是一些闲聊语句，对应 `chitchat` 工具组件
- `rag` Route 的示例语句是一些关于复仇者联盟电影剧情的问题，对应 `rag` 工具组件
- `web` Route 的示例语句是一些关于网络搜索的问题， 其中有不少 `Search`、`Find` 等关键词，对应 `web` 工具组件

接下来我们创建一个自定义的路由模块，使用 Semantic Router 来实现查询路由，示例代码如下：

```py
from llama_index.core.base.query_pipeline.query import (
    QueryComponent,
    QUERY_COMPONENT_TYPE,
)
from llama_index.core.bridge.pydantic import Field

class SemanticRouterComponent(CustomQueryComponent):
    """Semantic router component."""

    components: Dict[str, QueryComponent] = Field(
        ..., description="Components (must correspond to choices)"
    )

    def __init__(self, components: Dict[str, QUERY_COMPONENT_TYPE]) -> None:
        """Init."""
        super().__init__(components=components)

    def _validate_component_inputs(self, input: Dict[str, Any]) -> Dict[str, Any]:
        """Validate component inputs during run_component."""
        return input

    @property
    def _input_keys(self) -> set:
        """Input keys dict."""
        return {"input"}

    @property
    def _output_keys(self) -> set:
        return {"output", "selection"}

    def _run_component(self, **kwargs) -> Dict[str, Any]:
        """Run the component."""
        if len(self.components) < 1:
            raise ValueError("No components")
        if chitchat.name not in self.components.keys():
            raise ValueError("No chitchat component")

        routes = [chitchat, rag, web]
        encoder = OpenAIEncoder()
        rl = RouteLayer(encoder=encoder, routes=routes)
        question = kwargs["input"]
        selection = rl(question).name
        if selection is not None:
            output = self.components[selection].run_component(input=question)
        else:
            output = self.components["chitchat"].run_component(input=question)
        return {"output": output, "selection": selection}
```

- 在自定义模块的构造器函数 `__init__` 中我们传入了一个字典，字典的键是 Route 的名称，值是对应的工具组件
- 在 `_output_keys` 方法中我们返回了 2 个输出键，一个是输出结果，一个是选择结果
- 在 `_run_component` 方法中我们首先对工具组件参数进行验证，确保有 `chitchat` 这个工具组件，因为我们需要将无法分类的问题分发到 `chitchat` 工具组件
- 然后我们使用 Semantic Router 来判断用户问题的意图，得到选择结果 `selection`
- 再根据选择结果来选择相应的工具组件并执行
- 如果选择结果为空，则选择 `chitchat` 工具组件并执行
- 最后返回输出结果和选择结果

最后我们将所有的工具组件和路由模块添加到一个单独的查询流水线中，示例代码如下：

```py
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "router": SemanticRouterComponent(
            components={
                "chitchat": chitchat_p,
                "rag": rag_p,
                "web": web_p,
            }
        ),
    }
)
```

- 新建的查询流水线只有一个模块 `router`，这个模块是我们自定义的路由模块 `SemanticRouterComponent`
- 在路由模块中我们传入了 3 个之前定义的查询流水线，表示不同的用户意图执行不同的查询流水线
- 因为只有一个模块，所以无需添加连接关系

下面我们来执行一下这个流水线，看看效果如何：

```py
output = p.run(input="hello")
# Selection: chitchat
# Output: assistant: Hello! How can I assist you today?

output = p.run(input="Which two members of the Avengers created Ultron?")
# Selection: rag
# Output: Tony Stark and Bruce Banner.

output = p.run(input="Search online for the top three countries in the 2024 Paris Olympics medal table.")
# Selection: web
# Output: The top three countries in the latest medal table for the 2024 Paris Olympics are as follows:
# 1. United States
# 2. China
# 3. Great Britain
```

可以看到我们的查询路由工作的很好，根据用户问题的不同意图选择了不同的工具组件，并得到了相应的结果。

## 总结

今天我们介绍了 RAG 检索策略中的查询路由，并介绍了 LLM Router 和 Embedding Router 两种查询路由的实现原理，最后通过一个实战项目了解了查询路由在实际项目中的使用。但目前的查询路由还有很多不确定性，因此我们无法保证查询路由总能做出完全准确的决策，需要经过精心测试才能得到更加可靠的 RAG 应用程序。

关注我，一起学习各种人工智能和 GenAI 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 参考引用

- [Routing in RAG-Driven Applications](https://medium.com/towards-data-science/routing-in-rag-driven-applications-a685460a7220)
