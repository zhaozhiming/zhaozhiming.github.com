---
layout: post
title: 高级 RAG 检索策略之流程与模块化
date: 2024-06-08 20:54:26
description: 介绍在 LlamaIndex 中使用查询流水线实现RAG模块化和流程化的方法
keywords: rag, llamaindex, pipeline, module
comments: true
categories: ai
tags: [rag, llamaindex, pipeline, module]
---

{% img /images/post/2024/06/rag-module-flow.jpg 400 300 %}

我们介绍了很多关于高级 RAG（Retrieval Augmented Generation）的检索策略，每一种策略就像是机器中的零部件，我们可以通过对这些零部件进行不同的组合，来实现不同的 RAG 功能，从而满足不同的需求。今天我们就来介绍高级 RAG 检索中一些常见的 RAG 模块，以及如何通过流程的方式来组合这些模块，实现高级 RAG 检索功能。

<!--more-->

## RAG 模块化

模块化 RAG 提出了一种高度可扩展的范例，将 RAG 系统分为模块类型、模块和操作符的三层结构。每个模块类型代表 RAG 系统中的一个核心流程，包含多个功能模块。每个功能模块又包含多个特定的操作符。整个 RAG 系统变成了多个模块和相应操作符的排列组合，形成了我们所说的 RAG 流程。在流程中，每种模块类型可以选择不同的功能模块，并且在每个功能模块中可以选择一个或多个操作符。

{% img /images/post/2024/06/rag-module-intro.jpg 1000 600 %}

## RAG 流程

RAG 流程是指在 RAG 系统中，从输入查询到输出生成文本的整个工作流程。这个流程通常涉及多个模块和操作符的协同工作，包括但不限于检索器、生成器以及可能的预处理和后处理模块。RAG 流程的设计旨在使得 LLM（大语言模型）能够在生成文本时利用外部知识库或文档集，从而提高回答的准确性和相关性。

RAG 推理阶段的流程一般分为以下几种模式：

- Sequential: 线性流程，包括高级和简单的 RAG 范式
- Conditional: 基于查询的关键词或语义选择不同的 RAG 路径
- Branching: 包括多个并行分支，分为预检索和后检索的分支结构
- Loop: 包括迭代、递归和自适应检索等多种循环结构

下图是 Loop 模式的 RAG 流程图：

{% img /images/post/2024/06/loop-rag-flow.jpeg 1000 600 %}

后面我们主要以 Sequential 模式为例，介绍如何通过模块化和流水线的方式来实现高级 RAG 检索功能。

## 代码示例

[LlamaIndex](https://www.llamaindex.ai/)的查询流水线（Query Pipeline）功能提供了一种模块化的方式来组合 RAG 检索策略。我们可以通过定义不同的模块，然后将这些模块按照一定的顺序组合起来，形成一个完整的查询流水线。下面我们通过一个从简单到复杂的示例来演示如何使用 LlamaIndex 的查询流水线功能实现高级 RAG 检索。

### 普通 RAG

首先我们定义一个普通 RAG 的流水线，这个流水线包含了 3 个模块，分别是：输入、检索和输出。其中输入模块用于接收用户输入的查询，检索模块用于从知识库中检索相关文档，输出模块用于根据检索结果生成回答。

{% img /images/post/2024/06/rag-flow-base.png 1000 600 %}

在定义查询流水线之前，我们先将我们的测试文档索引入库，这里的测试文档还是用维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情，示例代码如下：

```py
import os
from llama_index.llms.openai import OpenAI
from llama_index.embeddings.openai import OpenAIEmbedding
from llama_index.core import (
    Settings,
    SimpleDirectoryReader,
    StorageContext,
    VectorStoreIndex,
    load_index_from_storage,
)
from llama_index.core.node_parser import SentenceSplitter

documents = SimpleDirectoryReader("./data").load_data()
node_parser = SentenceSplitter()
llm = OpenAI(model="gpt-3.5-turbo")
embed_model = OpenAIEmbedding(model="text-embedding-3-small")
Settings.llm = llm
Settings.embed_model = embed_model
Settings.node_parser = node_parser

if not os.path.exists("storage"):
    index = VectorStoreIndex.from_documents(documents)
    index.set_index_id("avengers")
    index.storage_context.persist("./storage")
else:
    store_context = StorageContext.from_defaults(persist_dir="./storage")
    index = load_index_from_storage(
        storage_context=store_context, index_id="avengers"
    )
```

- 首先我们通过`SimpleDirectoryReader`读取`./data`目录下的文档
- 然后我们定义了一个`SentenceSplitter`用于将文档进行分割
- 接着我们使用`OpenAI`的 LLM 和 Embedding 模型来生成文本和向量，并将他们添加到`Settings`中
- 最后我们将文档索引入库，并将索引保存到`./storage`目录下，以便后续使用

接下来我们定义一个普通的 RAG 流水线，示例代码如下：

```py
from llama_index.core.query_pipeline import QueryPipeline, InputComponent
from llama_index.core.response_synthesizers.simple_summarize import SimpleSummarize

retriever =  index.as_retriever()
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
        "retriever": retriever,
        "output": SimpleSummarize(),
    }
)

p.add_link("input", "retriever")
p.add_link("input", "output", dest_key="query_str")
p.add_link("retriever", "output", dest_key="nodes")
```

- 我们创建了一个普通检索器`retriever`，用于从知识库中检索相关文档
- 然后创建了一个`QueryPipeline`对象，这是查询流水线的主体，设置 verbose 参数为 True 用于输出详细信息
- 通过`QueryPipeline`的`add_modules`方法添加了 3 个模块：`input`、`retriever`和`output`
- `input`模块的实现类是`InputComponent`，这是查询流水线常用的输入组件，`retriever`模块是我们定义的检索器，`output`模块的实现类是`SimpleSummarize`，这是可以将问题和检索结果进行简单总结的输出组件
- 接着我们添加模块间的连接关系，`add_link`方法用于连接模块之间的关系，第一个参数是源模块，第二个参数是目标模块
- `dest_key`参数用于指定目标模块的输入参数，因为`output`模块有 2 个参数，分别是问题和检索结果，所以我们需要指定`dest_key`参数，当目标模块只有一个参数时则不需要指定
- 在`add_link`方法中，与`dest_key`参数对应的是`src_key`参数，当源模块有多个参数时，我们需要指定`src_key`参数，反之则不需要。

查询流水线添加模块和连接关系的方式除了`add_modules`和`add_link`方法外，还可以通过`add_chain`方法添加，示例代码如下：

```py
p = QueryPipeline(verbose=True)
p.add_chain([InputComponent(), retriever])
```

这种方式可以一次性添加模块与连接关系，但这种方式只能添加单参数的模块，如果模块有多个参数则需要使用`add_modules`和`add_link`方法。

接下来我们再来运行查询流水线，示例代码如下：

```py
question = "Which two members of the Avengers created Ultron?"
output = p.run(input=question)
print(str(output))

# 结果显示
> Running module input with input:
input: Which two members of the Avengers created Ultron?

> Running module retriever with input:
input: Which two members of the Avengers created Ultron?

> Running module output with input:
query_str: Which two members of the Avengers created Ultron?
nodes: [NodeWithScore(node=TextNode(id_='53d32f3a-a2d5-47b1-aa8f-a9679e83e0b0', embedding=None, metadata={'file_path': '/data/Avengers:Age-of-Ul...

Bruce Banner and Tony Stark.
```

- 使用查询流水线的`run`方法运行查询流水线，传入问题作为输入参数
- 在显示结果中可以看到查询流水线的调试信息，查询流水线首先运行了`input`模块，然后运行了`retriever`模块，最后运行了`output`模块，调试信息还打印了每个模块的输入参数，最后输出了问题的答案

### 增加 reranker 模块

接下来我们在普通 RAG 的基础上增加一个 reranker 模块，用于对检索结果进行重新排序。

{% img /images/post/2024/06/rag-flow-rerank.png 1000 600 %}

```diff
+from llama_index.postprocessor.cohere_rerank import CohereRerank

+reranker = CohereRerank()
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
        "retriever": retriever,
+        "reranker": reranker,
        "output": SimpleSummarize(),
    }
)

p.add_link("input", "retriever")
+p.add_link("input", "reranker", dest_key="query_str")
+p.add_link("retriever", "reranker", dest_key="nodes")
p.add_link("input", "output", dest_key="query_str")
-p.add_link("retriever", "output", dest_key="nodes")
+p.add_link("reranker", "output", dest_key="nodes")
```

- 这里我们使用了[Cohere](https://cohere.com/)公司的 rerank 功能，在 LlamaIndex 中提供了`CohereRerank`类用于实现 Cohere 的 rerank 功能
- 要使用`CohererRerank`类，需要先在 Cohere 官网上注册账号并获取 API KEY，并在环境变量中设置`COHERE_API_KEY`的值：`export COHERE_API_KEY=your-cohere-api-key`
- 然后我们在查询流水线中添加一个`reranker`模块，并将其添加到`retriever`模块和`output`模块之间，用于对检索结果进行重新排序
- 我们去除原来从`retriever`模块到`output`模块的连接关系，增加了`retriever`模块到`reranker`模块和`reranker`模块到`output`模块的连接关系
- `reranker`模块同样需要 2 个参数，分别是问题和检索结果，这样`reranker`模块才可以根据问题对检索结果进行重新排序，所以我们需要指定`dest_key`参数

查询流水线的运行方法除了`run`方法外，还有`run_with_intermeation`方法，这个方法可以获取流水线的中间结果，我们将`retriever`和`rerank`模块的中间结果打印出来进行对比，示例代码如下：

```py
output, intermediates = p.run_with_intermediates(input=question)
retriever_output = intermediates["retriever"].outputs["output"]
print(f"retriever output:")
for node in retriever_output:
    print(f"node id: {node.node_id}, node score: {node.score}")
reranker_output = intermediates["reranker"].outputs["nodes"]
print(f"\nreranker output:")
for node in reranker_output:
      print(f"node id: {node.node_id}, node score: {node.score}")

# 显示结果
retriever output:
node id: 53d32f3a-a2d5-47b1-aa8f-a9679e83e0b0, node score: 0.6608391314791646
node id: dea3844b-789f-46de-a415-df1ef14dda18, node score: 0.5313643379538727

reranker output:
node id: 53d32f3a-a2d5-47b1-aa8f-a9679e83e0b0, node score: 0.9588471
node id: dea3844b-789f-46de-a415-df1ef14dda18, node score: 0.5837967
```

- 执行`run_with_intermediates`方法后返回结果是一个元组，包含了输出结果和中间结果
- 要获取某个模块的中间结果，可以通过`intermediates`变量加上模块 key 进行获取，比如`intermediates["retriever"]`是获取检索模块的中间结果
- 每个中间结果都有 2 个参数，分别是`inputs`和`outputs`，`inputs`表示模块的输入参数，`outputs`表示模块的输出参数
- `inputs`和`outputs`参数类型是字典，比如`reranker`模块的`outputs`参数中包含了`nodes`属性，我们可以这样来获取`nodes`属性的值：`intermediates["reranker"].outputs["nodes"]`

### 增加 query rewrite 模块

之前我们在查询流水线中加入了 reranker 模块，相当是对检索结果的`后处理`操作，现在我们再加入一个 query rewrite 模块，用于对查询问题进行`预处理`操作。

{% img /images/post/2024/06/rag-flow-query-rewrite.png 1000 600 %}

```diff
+query_rewriter = HydeComponent()
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
+        "query_rewriter": query_rewriter,
        "retriever": retriever,
        "reranker": reranker,
        "output": SimpleSummarize(),
    }
)

-p.add_link("input", "retriever")
+p.add_link("input", "query_rewriter")
+p.add_link("query_rewriter", "retriever")
p.add_link("input", "reranker", dest_key="query_str")
p.add_link("retriever", "reranker", dest_key="nodes")
p.add_link("input", "output", dest_key="query_str")
p.add_link("reranker", "output", dest_key="nodes")
```

- 这里我们定义了一个`HydeComponent`类用于实现查询重写的功能，使用的是 HyDE（假设性文档向量）查询重写策略，它会根据查询问题生成一个假设性回答，然后使用这个假设性回答去检索文档，从而提高检索的准确性
- `HydeComponent`是一个自定义的查询流水线组件，后面我们再详细介绍它的实现
- 我们在原有的查询流水线上增加了一个`query_rewriter`模块，放在`input`模块和`retriever`模块之间，用于对查询问题进行预处理
- 我们去除原来从`input`模块到`retriever`模块的连接关系，增加了`input`模块到`query_rewriter`模块和`query_rewriter`模块到`retriever`模块的连接关系
- `query_rewriter`模块只有一个参数，所以不需要指定`dest_key`参数

LlamaIndex 的查询流水线提供了自定义组件的功能，我们可以通过继承`CustomQueryComponent`类来实现自定义组件，下面我们来实现`HydeComponent`类，示例代码如下：

```py
from llama_index.core.query_pipeline import CustomQueryComponent
from typing import Dict, Any
from llama_index.core.indices.query.query_transform import HyDEQueryTransform

class HydeComponent(CustomQueryComponent):
    """HyDE query rewrite component."""

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
        hyde = HyDEQueryTransform(include_original=True)
        query_bundle = hyde(kwargs["input"])
        return {"output": query_bundle.embedding_strs[0]}
```

- `HydeComponent`类中的`_validate_component_inputs`方法用于验证组件的输入参数，必须实现这个方法，否则会抛出异常
- `_input_keys`和`_output_keys`属性分别用于定义组件的输入和输出 key 值
- `_run_component`方法用于实现组件的具体功能，这里我们使用`HyDEQueryTransform`类实现了 HyDE 查询重写功能，将查询问题转换为假设性回答，并返回这个假设性回答

关于查询重写的更多策略，可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2024/05/13/query-rewrite-rag/)。

### 替换 output 模块

在之前的查询流水线中，我们使用的是简单的总结输出组件，现在我们将其替换为树形总结组件，用来提高最终的输出结果。

> **树形总结**组件以自底向上的方式递归地合并文本块并对其进行总结（即从叶子到根构建一棵树）。
> 具体地说，在每个递归步骤中：
>
> 1. 我们重新打包文本块，使得每个块填充大语言模型的上下文窗口
> 2. 如果只有一个块，我们给出最终响应
> 3. 否则，我们总结每个块，并递归地总结这些摘要

{% img /images/post/2024/06/rag-flow-tree-summarize.png 1000 600 %}

```diff
+from llama_index.core.response_synthesizers.tree_summarize import TreeSummarize

p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
        "query_rewriter": query_rewriter,
        "retriever": retriever,
        "reranker": reranker,
-        "output": SimpleSummarize(),
+        "output": TreeSummarize(),
    }
)
```

- 替换`output`模块的组件比较简单，只需要将原来的`SimpleSummarize`替换为`TreeSummarize`即可
- `TreeSummarize`组件的结构和`SimpleSummarize`组件类似，因此这里我们不需要修改其他模块的连接关系

查询流水线实际上是一个 DAG（有向无环图），每个模块是图中的一个节点，模块之间的连接关系是图中的边，我们可以通过代码来展示这个图形结构，示例代码如下：

```py
from pyvis.network import Network

net = Network(notebook=True, cdn_resources="in_line", directed=True)
net.from_nx(p.clean_dag)
net.write_html("output/pipeline_dag.html")
```

- 我们使用`pyvis`库来绘制查询流水线的图形结构
- `Network`类用于创建一个网络对象，`notebook=True`表示在 Jupyter Notebook 中显示，`cdn_resources="in_line"`表示使用内联资源，`directed=True`表示有向图
- `from_nx`方法用于将查询流水线的 DAG 结构转换为网络对象
- `write_html`方法用于将网络对象保存为 HTML 文件，这样我们就可以在浏览器中查看查询流水线的图形结构

保存后的查询流水线图形结构如下：

{% img /images/post/2024/06/pipeline-dag.png 1000 600 %}

### 使用句子窗口检索

在之前的查询流水线中，`retriever`模块使用的是普通的检索策略，现在我们将其替换为句子窗口检索策略，用于提高检索的准确性。

> 句子窗口检索的原理：首先在文档切分时，将文档以句子为单位进行切分，同时进行 Embedding 并保存数据库。然后在检索时，通过问题检索到相关的句子，但并不只是将检索到的句子作为检索结果，而是将该句子前面和后面的句子一起作为检索结果，包含的句子数量可以通过参数来进行设置，最后将检索结果再一起提交给 LLM 来生成答案。

{% img /images/post/2024/06/rag-flow-sentence-window.png 1000 600 %}

```diff
+from llama_index.core.node_parser import SentenceWindowNodeParser
+from llama_index.core.indices.postprocessor import MetadataReplacementPostProcessor

-node_parser = SentenceSplitter()
+node_parser = SentenceWindowNodeParser.from_defaults(
+    window_size=3,
+    window_metadata_key="window",
+    original_text_metadata_key="original_text",
+)

+meta_replacer = MetadataReplacementPostProcessor(target_metadata_key="window")
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
        "query_rewriter": query_rewriter,
        "retriever": retriever,
+        "meta_replacer": meta_replacer,
        "reranker": reranker,
        "output": TreeSummarize(),
    }
)
p.add_link("input", "query_rewriter")
p.add_link("query_rewriter", "retriever")
+p.add_link("retriever", "meta_replacer")
p.add_link("input", "reranker", dest_key="query_str")
-p.add_link("retriever", "reranker", dest_key="nodes")
+p.add_link("meta_replacer", "reranker", dest_key="nodes")
p.add_link("input", "output", dest_key="query_str")
p.add_link("reranker", "output", dest_key="nodes")
```

- 句子窗口检索首先需要调整文档的入库策略，以前是用`SentenceSplitter`来切分文档，现在我们使用`SentenceWindowNodeParser`来切分文档，窗口大小为 3，原始文本的 key 为`original_text`，窗口文本的 key 为`window`
- 句子窗口检索的原理是在检索出结果后，将检索到的节点文本替换成窗口文本，所以这里需要增加一个`meta_replacer`模块，用来替换检索结果中的节点文本
- `meta_replacer`模块的实现类是`MetadataReplacementPostProcessor`，输入参数是检索结果`nodes`，输出结果是替换了节点文本的检索结果`nodes`
- 我们将`meta_replacer`模块放在`retriever`模块和`reranker`模块之间，先对检索结果进行元数据替换处理，然后再进行 rerank 操作，因此这里修改了这 3 个模块的连接关系

我们可以打印出`retriever`模块和`meta_replacer`模块的中间结果，来对比检索结果的变化，示例代码如下：

```py
output, intermediates = p.run_with_intermediates(input=question)
retriever_output = intermediates["retriever"].outputs["output"]
print(f"retriever output:")
for node in retriever_output:
    print(f"node: {node.text}\n")
meta_replacer_output = intermediates["meta_replacer"].outputs["nodes"]
print(f"meta_replacer output:")
for node in meta_replacer_output:
    print(f"node: {node.text}\n")

# 显示结果
retriever output:
node: In the Eastern European country of Sokovia, the Avengers—Tony Stark, Thor, Bruce Banner, Steve Rogers, Natasha Romanoff, and Clint Barton—raid a Hydra facility commanded by Baron Wolfgang von Strucker, who has experimented on humans using the scepter previously wielded by Loki.

node: They meet two of Strucker's test subjects—twins Pietro (who has superhuman speed) and Wanda Maximoff (who has telepathic and telekinetic abilities)—and apprehend Strucker, while Stark retrieves Loki's scepter.

meta_replacer output:
node: and attacks the Avengers at their headquarters.  Escaping with the scepter, Ultron uses the resources in Strucker's Sokovia base to upgrade his rudimentary body and build an army of robot drones.  Having killed Strucker, he recruits the Maximoffs, who hold Stark responsible for their parents' deaths by his company's weapons, and goes to the base of arms dealer Ulysses Klaue in Johannesburg to get vibranium.  The Avengers attack Ultron and the Maximoffs, but Wanda subdues them with haunting visions, causing Banner to turn into the Hulk and rampage until Stark stops him with his anti-Hulk armor. [a]
A worldwide backlash over the resulting destruction, and the fears Wanda's hallucinations incited, send the team into hiding at Barton's farmhouse.  Thor departs to consult with Dr.  Erik Selvig on the apocalyptic future he saw in his hallucination, while Nick Fury arrives and encourages the team to form a plan to stop Ultron.

node: In the Eastern European country of Sokovia, the Avengers—Tony Stark, Thor, Bruce Banner, Steve Rogers, Natasha Romanoff, and Clint Barton—raid a Hydra facility commanded by Baron Wolfgang von Strucker, who has experimented on humans using the scepter previously wielded by Loki.  They meet two of Strucker's test subjects—twins Pietro (who has superhuman speed) and Wanda Maximoff (who has telepathic and telekinetic abilities)—and apprehend Strucker, while Stark retrieves Loki's scepter.
Stark and Banner discover an artificial intelligence within the scepter's gem, and secretly decide to use it to complete Stark's "Ultron" global defense program.  The unexpectedly sentient Ultron, believing he must eradicate humanity to save Earth, eliminates Stark's A.I.
```

从结果中我们可以看出，原来的`retreiver`模块输出的只是简单的一句话，而`meta_replacer`模块输出的是多个句子，包含了检索节点的前后节点的文本，这样可以让 LLM 生成更准确的答案。

关于句子窗口检索的更多细节，可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2024/03/11/sentence-windows-rag/)。

### 增加评估模块

最后我们再为查询流水线增加一个评估模块，用于评估查询流水线，这里我们使用[Ragas](https://docs.ragas.io/)来实现评估模块。

> Ragas 是一个评估 RAG 应用的框架，拥有很多详细的评估指标。

{% img /images/post/2024/06/rag-flow-evaluation.png 1000 600 %}

```diff
+evaluator = RagasComponent()
p = QueryPipeline(verbose=True)
p.add_modules(
    {
        "input": InputComponent(),
        "query_rewriter": query_rewriter,
        "retriever": retriever,
        "meta_replacer": meta_replacer,
        "reranker": reranker,
        "output": TreeSummarize(),
+        "evaluator": evaluator,
    }
)
-p.add_link("input", "query_rewriter")
+p.add_link("input", "query_rewriter", src_key="input")
p.add_link("query_rewriter", "retriever")
p.add_link("retriever", "meta_replacer")
-p.add_link("input", "reranker", dest_key="query_str")
+p.add_link("input", "reranker", src_key="input", dest_key="query_str")
p.add_link("meta_replacer", "reranker", dest_key="nodes")
-p.add_link("input", "output", dest_key="query_str")
+p.add_link("input", "output", src_key="input", dest_key="query_str")
p.add_link("reranker", "output", dest_key="nodes")
+p.add_link("input", "evaluator", src_key="input", dest_key="question")
+p.add_link("input", "evaluator", src_key="ground_truth", dest_key="ground_truth")
+p.add_link("reranker", "evaluator", dest_key="nodes")
+p.add_link("output", "evaluator", dest_key="answer")
```

- `RagasComponent`也是一个自定义的查询流水线组件，后面我们再详细介绍它的实现
- 在查询流水线中增加了一个`evaluator`模块，用于评估查询流水线
- 我们将`evaluator`模块放到`output`模块之后，用于评估输出结果
- `evaluator`模块有 4 个输入参数，分别是问题、真实答案、检索结果和生成答案，其中问题和真实答案通过`input`模块传入，检索结果通过`reranker`模块传入，生成答案通过`output`模块传入
- 因为`input`模块现在有 2 个参数，分别是问题`input`和真实答案`ground_truth`，所以我们在添加`input`模块的相关连接关系时，需要指定`src_key`参数

我们再来看下`RagasComponent`的实现，示例代码如下：

```py
from ragas.metrics import faithfulness, answer_relevancy, context_precision, context_recall
from ragas import evaluate
from datasets import Dataset
from llama_index.core.query_pipeline import CustomQueryComponent
from typing import Dict, Any

metrics = [faithfulness, answer_relevancy, context_precision, context_recall]

class RagasComponent(CustomQueryComponent):
    """Ragas evalution component."""

    def _validate_component_inputs(self, input: Dict[str, Any]) -> Dict[str, Any]:
        """Validate component inputs during run_component."""
        return input

    @property
    def _input_keys(self) -> set:
        """Input keys dict."""
        return {"question", "nodes", "answer", "ground_truth", }

    @property
    def _output_keys(self) -> set:
        return {"answer", "source_nodes", "evaluation"}

    def _run_component(self, **kwargs) -> Dict[str, Any]:
        """Run the component."""
        question, ground_truth, nodes, answer = kwargs.values()
        data = {
            "question": [question],
            "contexts": [[n.get_content() for n in nodes]],
            "answer": [str(answer)],
            "ground_truth": [ground_truth],
        }
        dataset = Dataset.from_dict(data)
        evalution = evaluate(dataset, metrics)
        return {"answer": str(answer), "source_nodes": nodes, "evaluation": evalution}
```

- 和之前的自定义组件一样，`RagasComponent`类需要实现`_validate_component_inputs`、`_input_keys`、`_output_keys`和`_run_component`方法
- 组件的输入参数是问题、真实答案、检索结果和生成答案，输出参数是生成答案、检索结果和评估结果
- 在`_run_component`方法中，我们将输入参数重新封装成一个可供 Ragas 评估的`Dataset`对象
- 评估指标我们使用的分别是：`faithfulness`（评估`Question`和`Context`的一致性），`answer_relevancy`（评估`Answer`和`Question`的一致性），`context_precision`（评估`Ground Truth`在`Context`中是否排名靠前），`context_recall`（评估`Ground Truth`和`Context`的一致性）
- 我们再调用`evaluate`方法对`Dataset`对象进行评估，得到评估结果
- 最后将生成答案、检索结果和评估结果一起返回

最后我们来运行下查询流水线，示例代码如下：

```py
question = "Which two members of the Avengers created Ultron?"
ground_truth = "Tony Stark (Iron Man) and Bruce Banner (The Hulk)."
output = p.run(input=question, ground_truth=ground_truth)
print(f"answer: {output['answer']}")
print(f"evaluation: {output['evaluation']}")

# 显示结果
answer: Tony Stark and Bruce Banner
evaluation: {'faithfulness': 1.0000, 'answer_relevancy': 0.8793, 'context_precision': 1.0000, 'context_recall': 1.0000}
```

- 运行查询流水线时，我们需要传入问题和真实答案作为输入参数
- 在输出结果中，我们可以看到生成的答案，以及评估结果 4 个评估指标的值

关于 RAG 的更多评估工具，可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2024/04/22/llamaindex-and-evaluation-tools/)。

## 总结

通过上面的示例，我们可以看到如何通过模块化和流程的方式来实现高级 RAG 检索功能，我们可以根据具体的需求，自定义不同的模块，然后将这些模块按照一定的顺序组合起来，形成一个完整的查询流水线。在 RAG 应用中，我们还可以定义多个查询流水线，用于不同的场景，比如问答、对话、推荐等，这样可以更好地满足不同的需求。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 引用参考

- [Modular RAG and RAG Flow: Part Ⅰ](https://medium.com/@yufan1602/modular-rag-and-rag-flow-part-%E2%85%B0-e69b32dc13a3)
- [Modular RAG and RAG Flow: Part II](https://medium.com/@yufan1602/modular-rag-and-rag-flow-part-ii-77b62bf8a5d3)
- [An Introduction to LlamaIndex Query Pipelines](https://docs.llamaindex.ai/en/stable/examples/pipeline/query_pipeline/)
