---
layout: post
title: 高级 RAG 检索策略之知识图谱
date: 2024-07-04 22:12:57
description: 介绍如何使用知识图谱来改进 RAG 的检索策略
keywords: rag, llamaindex, knownledge-graph, graphrag, neo4j
comments: true
categories: ai
tags: [rag, llamaindex, knownledge-graph, graphrag, neo4j]
---

{% img /images/post/2024/07/rag-knowledge-graph.jpg 400 300 %}

RAG（Retrieval Augmented Generation）技术中检索是一个非常重要的环节，检索的准确性直接影响到生成的质量，但普通 RAG 的向量检索技术并不能满足所有场景下的需求，比如在一些大型私有文档库中，传统的检索技术往往表现不好。目前已经有很多研究团队在 RAG 中引入知识图谱来提高检索的准确性，并且取得了很好的效果。今天我们就来了解一下知识图谱的原理，以及如何在 RAG 中进行使用。

<!--more-->

## 什么是知识图谱

知识图谱是一种利用图结构来表示和建模现实世界中实体及其关系的技术方法。它将信息以节点（实体）和边（关系）的形式组织成一个有机的网络，从而实现对复杂知识的高效存储、查询和分析。知识图谱的核心在于通过三元组形式（实体-关系-实体）来描述事物之间的关联，这种结构化的数据表示方法不仅能够捕捉数据的语义含义，还能便于理解和分析。

### 整体流程

知识图谱在 RAG 中的使用流程图如下所示：

{% img /images/post/2024/07/graph-rag-flow.png 1000 600 %}

在数据入库过程中，文档经过分块后，知识图谱 RAG 会将文档块进行实体和关系的提取，提取出实体和关系后，通常是将它们保存到图数据库中。

在检索过程中，知识图谱 RAG 会将问题进行实体提取，将提取出来的实体通过图数据库进行检索，获取相关的实体和关系，检索结果往往是一个庞大的实体关系网络，最后将检索到的实体和关系结合问题提交给 LLM（大语言模型）进行答案生成。

有些知识图谱 RAG 的实现也会结合图检索和向量检索两种方式，这样可以综合利用图检索和向量检索的优势，提高检索的准确性和效率。

### 解决的问题

在 RAG 中使用知识图谱主要解决在大型文档库上问答和理解困难的问题，特别是那些普通 RAG 方法难以处理的全局性问题。普通 RAG 在回答针对整个文档库的全局性问题时表现不佳，例如问题：`请告诉我所有关于 XXX 的事情`，这个问题涉及到的上下文可能分布在整个大型文档库中，普通 RAG 的向量检索方法很难得到这种分散、细粒度的文档信息，向量检索经常使用 top-k 算法来获取最相近的上下文文档，这种方式很容易遗漏关联的文档块，从而导致信息检索不完整。

另外是 LLM 的上下文窗口限制问题，对于全局性问题往往涉及到非常多的上下文文档，如果要全部提交给 LLM 则很容易超出 LLM 的窗口限制，而知识图谱将文档提取成实体关系后再提交给 LLM，实际上大大压缩了文档块的大小，从而让所有相关文档提交给 LLM 成为可能。

### 与普通 RAG 的区别

- 知识图谱 RAG 使用图结构来表示和存储信息，捕捉实体间的复杂关系，而普通 RAG 通常使用向量化的文本数据
- 知识图谱 RAG 通过图遍历和子图检索来获取相关信息，普通 RAG 主要依赖向量相似度搜索
- 知识图谱 RAG 能更好地理解实体间的关系和层次结构，提供更丰富的上下文，普通 RAG 在处理复杂关系时能力有限

## 数据入库

下面我们来看下知识图谱 RAG 具体的数据入库流程，普通 RAG 在文档分块后，通常是使用 Embedding 模型将文档块进行向量化，然后将向量和文档保存到向量数据库。与普通 RAG 不同，知识图谱 RAG 在入库过程中会将文档块进行实体和关系的提取，提取出实体和关系后再将它们保存到图数据库中。

{% img /images/post/2024/07/graph-rag-index-flow.png 1000 600 %}

实体提取的传统方法是基于预定义的规则和词典、统计机器学习或者深度学习等技术，但进入到 LLM 时代后，实体提取更多的是使用 LLM 来进行，因为 LLM 能够更好地理解文本的语义，实现也更加简单。

比如在 [LlamaIndex](https://www.llamaindex.ai/) 的 `KnowledgeGraphIndex` 类中的实体提取提示词是这样的：

```py
DEFAULT_KG_TRIPLET_EXTRACT_TMPL = (
    "Some text is provided below. Given the text, extract up to "
    "{max_knowledge_triplets} "
    "knowledge triplets in the form of (subject, predicate, object). Avoid stopwords.\n"
    "---------------------\n"
    "Example:"
    "Text: Alice is Bob's mother."
    "Triplets:\n(Alice, is mother of, Bob)\n"
    "Text: Philz is a coffee shop founded in Berkeley in 1982.\n"
    "Triplets:\n"
    "(Philz, is, coffee shop)\n"
    "(Philz, founded in, Berkeley)\n"
    "(Philz, founded in, 1982)\n"
    "---------------------\n"
    "Text: {text}\n"
    "Triplets:\n"
)
```

在提示词中要求 LLM 将文档块 `text` 提取成`实体-关系-实体`这样的三元组，实体一般是名词，表示文档块中的实体，关系是动词或者介词，表示实体之间的关系，并给出了几个 Few Shot，让 LLM 能更好地理解实体抽取的任务。

将实体提取出来后，通常是将实体和关系保存到图数据库中，但也有一些知识图谱 RAG 的实现会将这些数据保存到文件中，然后通过其特有的算法来进行检索，比如微软的 [GraphRAG](https://microsoft.github.io/graphrag/)。

图数据库是一种专门用来存储图结构数据的数据库，它能够高效地存储和查询图数据，常见的图数据库有 Neo4j、ArangoDB 等。不同的图数据库有不同的查询语言，比如 Neo4j 的查询语言使用的是 Cypher，如果想要在 RAG 中使用 Neo4j 来存储知识图谱数据，那么掌握一些基础的 Cypher 语法是有必要的。

## 检索生成

了解了知识图谱 RAG 的数据入库流程之后，我们再来看下它的检索生成过程。普通 RAG 在检索过程中通常是将问题进行向量化，然后通过向量相似度搜索来获取最相近的几个文档块，然后将这些文档块提交给 LLM 进行答案生成。而知识图谱 RAG 在检索过程中会将问题进行实体提取，将提取出来的实体通过图数据库进行检索，这样可以获取到名称相同的实体，以及与实体相关的实体和关系，最后将检索到的所有实体和关系提交给 LLM 进行答案生成。

{% img /images/post/2024/07/graph-rag-retrieve-flow.png 1000 600 %}

对问题进行实体提取与数据入库时的实体提取方法类似，也是通过 LLM 来进行，但只需要提取出问题中的实体即可，不需要提取三元组，可以看下 LlamaIndex 的 `KGTableRetriever` 类中提取问题关键字的提示词：

```py
DEFAULT_QUERY_KEYWORD_EXTRACT_TEMPLATE_TMPL = (
    "A question is provided below. Given the question, extract up to {max_keywords} "
    "keywords from the text. Focus on extracting the keywords that we can use "
    "to best lookup answers to the question. Avoid stopwords.\n"
    "---------------------\n"
    "{question}\n"
    "---------------------\n"
    "Provide keywords in the following comma-separated format: 'KEYWORDS: <keywords>'\n"
)
```

提示词要求 LLM 从问题中提取出多个关键字，并用逗号分隔，这些关键字通常是问题中的实体。将问题的实体提取出来后，再用实体名称去图数据库中进行检索， 检索的原理就是使用图数据库的查询语句对每个实体进行检索，获取对应的三元组。以 Neo4j 图数据库为例，下面是一个简单的 Cypher 查询语句：

```cypher
MATCH (n {name: 'Alice'})-[r]-(m)
RETURN n, r, m
```

这个查询语句的意思是查找图数据库中所有与实体 Alice 相关的实体和关系，这样就可以获取到 Alice 相关的所有三元组。最后将得到的数据转换为文本，作为问题的上下文，提交给 LLM 进行答案生成。

## LlamaIndex 知识图谱 RAG 实现

​了解完知识图谱 RAG 的原理后，接下来我们来看下如何在实际项目中使用知识图谱 RAG ，在 LlamaIndex 框架中已经实现了知识图谱的功能，使用 LlamaIndex 和 Neo4j 可以快速地实现知识图谱 RAG。

### Neo4j 安装

Neo4j 是一个高性能的图形数据库，它将结构化数据存储在网络（从数学角度称为图）上而不是传统的表中，这种设计使得 Neo4j 在处理复杂的关系和连接时具有显著的优势。Neo4j 使用 Cypher 作为查询语言，Cypher 是一种声明式图数据库查询语言，类似于 SQL，但是专门用于图数据库。Cypher 语言的语法简单直观，易于学习和使用，可以快速编写复杂的图查询。Neo4j 除了支持图检索外，还支持其他多种检索方式，包括向量检索、全文检索等。

下面我们来看在如何安装 Neo4j 数据库，Neo4j 的安装非常简单，只需要通过 Docker 下载镜像并启动就可以了，安装命令如下：

```bash
docker run --name neo4j -d \
    --publish=7474:7474 --publish=7687:7687 \
    --volume=/your/host/path/neo4j-data/data:/data \
    --env NEO4J_PLUGINS='["apoc"]' \
    neo4j:5.21.0
```

- 我们使用 Neo4j 的 Docker 镜像进行安装，版本是 5.21.0
- Neo4j 镜像会开放 2 个端口，端口 7474 的服务是 Web 管理服务，端口 7687 的服务是数据库服务
- 我们将 Neo4j 的数据目录映射到宿主机的 `/your/host/path/neo4j-data/data` 目录
- 我们通过环境变量给 Neo4j 安装一个插件 Apoc，保证 Python 程序可以通过账号密码连接数据库

服务成功启动后，我们打开浏览器访问 `http://localhost:7474`，可以看到 Neo4j 的 Web 管理界面，如下图所示：

{% img /images/post/2024/07/neo4j-web.png 1000 600 %}

输入初始账号密码：`neo4j/neo4j`，然后设置新密码，就可以进入到 Neo4j 的管理界面了。

### 在 LlamaIndex 中使用 Neo4j

安装完 Neo4j 数据库后，我们就可以在 LlamaIndex 中使用 Neo4j 了，首先使用 `Neo4jGraphStore` 类来连接 Neo4j 数据库：

```py
from llama_index.graph_stores.neo4j import Neo4jGraphStore

username = "neo4j"
password = "neo4j"
url = "bolt://localhost:7687"
database = "neo4j"
graph_store = Neo4jGraphStore(
    username=username,
    password=password,
    url=url,
    database=database,
)
```

- 使用 `Neo4jGraphStore` 创建连接 Neo4j 数据库的存储对象，传入用户名、密码、连接地址、数据库名称等参数
- `bolt` 是 Neo4j 数据库使用的一种高效的二进制协议，用于在客户端和服务器之间传输数据
- Neo4j 数据库的社区版只能使用一个数据库，这里的数据库名称是固定的 `neo4j`

然后将文档保存到 Neo4j 数据库中，这里的测试文档还是用维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情，示例代码如下：

```py
from llama_index.core import StorageContext, SimpleDirectoryReader KnowledgeGraphIndex

documents = SimpleDirectoryReader("./data").load_data()
storage_context = StorageContext.from_defaults(graph_store=graph_store)
index = KnowledgeGraphIndex.from_documents(
    documents,
    storage_context=storage_context,
    max_triplets_per_chunk=2,
    include_embeddings=True,
)
```

- 使用 `SimpleDirectoryReader` 加载文档数据
- 使用 `StorageContext` 创建存储上下文对象，传入图数据库存储对象
- 使用 `KnowledgeGraphIndex` 从文档中创建知识图谱索引对象
- `max_triplets_per_chunk=2` 参数表示每个文档块将被最多提取成 2 个三元组
- `include_embeddings=True` 参数表示将提取后的三元组转成 Embedding 向量并保存
- `KnowledgeGraphIndex` 默认使用 OpenAI 的 LLM 模型和 Embedding 模型 来进行实体提取和 Embedding，因此需要在环境变量中设置 OpenAI 的 API Key

文档经过分块、实体提取、Embedding 等操作后，最后将实体和关系保存到 Neo4j 数据库中。数据入库完成后，我们可以在 Neo4j 数据库中查看所有的实体和关系，如下图所示：

{% img /images/post/2024/07/neo4j-dataset.png 1000 600 %}

最后我们构建查询引擎，并对问题进行检索生成：

```py
query_engine = index.as_query_engine(
    include_text=True,
    response_mode="tree_summarize",
    embedding_mode="hybrid",
    similarity_top_k=5,
    verbose=True,
)
response = query_engine.query("Which two members of the Avengers created Ultron?")
print(f"Response: {response}")
```

- 使用 `index.as_query_engine` 创建查询引擎对象
- `response_mode="tree_summarize"` 参数表示最终结果使用树形总结的方式来生成
- `embedding_mode="hybrid"` 参数表示使用图检索和向量检索的混合模式
- `similarity_top_k=5` 参数表示最多返回 5 个相似的文档块，`verbose=True` 参数表示检索过程中打印详细信息
- 使用查询引擎进行问题的检索和生成，最后打印出生成答案

我们再来看下程序运行后的结果，因为我们开启了调试模式，所以在检索过程中会打印出详细的检索信息，如下所示：

```sh
Extracted keywords: ['Avengers', 'Ultron', 'created', 'members']
KG context:
The following are knowledge sequence in max depth 2 in the form of directed graph like:
`subject -[predicate]->, object, <-[predicate_next_hop]-, object_next_hop ...`
['CAPTURES', 'Romanoff', 'USES', "Loki's scepter to close"]
['BATTLE', 'Chitauri', 'KNOWN_AS', 'Extraterrestrial race']
['CAPTURES', 'Romanoff', 'MAKES_HER_WAY_TO', 'Generator']
......
Response: Tony Stark and Bruce Banner.
```

- 首先提取问题中的关键词，这里提取出了`Avengers`, `Ultron`, `created`, `members`这几个关键词
- 然后打印出根据关键词检索到的实体和关系三元组
- 最后根据问题和这些上下文生成了答案

## GraphRAG

看完 LlamaIndex 的知识图谱 RAG 实现后，我们再来看下另外一个知识图谱 RAG 的实现。最近微软开源了一个知识图谱 RAG 的实现叫 [GraphRAG](https://microsoft.github.io/graphrag/)，它是一个基于知识图谱的 RAG 应用，可以用于问答、文本生成等任务。GraphRAG 是在微软之前发布的[论文](https://arxiv.org/pdf/2404.16130) 理论基础上进行开发，与普通知识图谱 RAG 不同的地方是，它并没有用到图数据库，而是直接将知识图谱保存到文件中，然后通过其特有的图检索算法进行检索。另外 GraphRAG 还利用知识图谱的模块化特性，将知识图谱划分为多个语义相关的社区，并为每个社区生成概括性的摘要。 在回答用户查询时，GraphRAG 会根据查询内容检索相关的社区摘要，并利用这些摘要生成最终答案。

下面我们介绍一下 GraphRAG 的安装和使用方法，让大家可以快速地了解 GraphRAG。

### 安装

关于 GraphRAG 的安装方法，这里推荐使用源码安装的方式来安装 GraphRAG，因为这样可以让我们在使用过程中通过修改源码的方式来调试 GraphRAG，从而更好地理解 GraphRAG 的原理。

首先我们需要下载 GraphRAG 的源码：

```bash
git clone https://github.com/microsoft/graphrag.git
cd graphrag
```

然后我们需要使用 [Poetry](https://python-poetry.org/) 来安装 GraphRAG 的依赖，Poetry 的安装可以参考其官网的[安装手册](https://python-poetry.org/docs/#installation)，GraphRAG 安装依赖的命令如下：

> Poetry 是一个用于 Python 项目依赖管理和打包的工具，Poetry 使用一个 `pyproject.toml` 文件来管理项目的所有依赖项和元数据，使项目配置更加简洁明了，它会自动处理依赖项的版本冲突，并且能够生成锁文件 `poetry.lock`，确保在不同环境中安装相同的依赖版本。

```bash
# 使用 conda 创建一个 Python 环境
conda create -n graphrag python=3.10
# 切换到这个 Python 环境
conda activate graphrag
# 构建一个 Poetry 虚拟环境
poetry env use python
# 进入该环境
poetry shell
# 安装依赖，会根据 GraphRAG 的 poetry.lock 文件安装依赖
poetry install
```

### 初始化配置

GraphRAG 安装完成后，我们再来准备测试文档，创建一个测试文件夹，用来存放我们的测试文档：

```bash
mkdir -p ./ragtest/input
# GraphRAG 官方文档是下载这个文件作为测试文档，我们也可以放其他的 txt 文档
curl https://www.gutenberg.org/cache/epub/24022/pg24022.txt > ./ragtest/input/book.txt
```

然后创建配置文件，我们使用 GraphRAG 的初始化命令来生成配置文件：

```bash
poetry run poe index --init --root ./ragtest
```

初始化完成后，我们可以看到在 `./ragtest` 目录下生成了 `settings.yaml` 和 `.env` 2 个文件，在 `.env` 文件中通过 `GRAPHRAG_API_KEY` 键来设置 OpenAI 的 API Key，而`settings.yaml` 文件用来保存 GraphRAG 的流水线配置信息。

### 入库流程

初始化配置完成后，我们使用 GraphRAG 执行数据入库流水线，命令如下：

```bash
poetry run poe index --root ./ragtest
```

这个执行过程比较耗时，因为 GraphRAG 会将文档进行一系列的操作，包括文档分块、实体提取、文本 Embedding、生成社区报告等，以下是 GraphRAG 的索引入库流程图：

{% img /images/post/2024/07/graphrag-index-flow.png 1000 600 %}

执行完成后，我们可以在 `./ragtest/output/{timestamp}/artifacts` 目录下可以看到生成的索引文件，默认是 `parquet` 格式，后面的检索流程会从这里读取数据。

想了解更多关于 GraphRAG 数据入库的信息，可以参考其[官方文档](https://microsoft.github.io/graphrag/posts/index/1-default_dataflow/)。

### 检索流程

文档入库完成后，我们就可以使用 GraphRAG 进行检索生成了，命令如下：

```bash
poetry run poe query --root ./ragtest --method local "Which two members of the Avengers created Ultron?"
```

GraphRAG 的检索模式有 2 种，分别是本地模式和全局模式，上面的命令使用 `--method` 参数来指定哪种模式。

本地模式类似传统的知识图谱 RAG，通过结合来自知识图谱的相关数据和原始文档的文本块生成答案，而全局模式是通过在所有社区报告上进行搜索，以类似 map-reduce 的方式生成答案。以下是 GraphRAG 本地检索的流程图：

{% img /images/post/2024/07/graphrag-query-local-flow.png 1000 600 %}

经过测试，GraphRAG 在检索质量上要比普通 RAG 更好，文档数据越多优势越明显，但是 GraphRAG 的检索速度要比普通 RAG 慢，因为 GraphRAG 需要对文档进行多种方式检索。

关于 GraphRAG 的检索流程的更多信息，可以参考其[官方文档](https://microsoft.github.io/graphrag/posts/query/overview/)。

## 总结

知识图谱 RAG 是一种基于知识图谱的 RAG 技术，它通过图结构来表示和存储信息，提取其中的实体和关系，然后通过图检索的方式来进行检索生成。对于一些大型的私有文档库和复杂的全局性问题，知识图谱 RAG 无疑是一个很好的选择，但在使用过程中也要综合考虑，知识图谱的增强往往也会导致检索生成的速度变慢以及耗费的资源增加，因此在实际应用中需要根据具体的场景来选择合适的 RAG 技术。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
