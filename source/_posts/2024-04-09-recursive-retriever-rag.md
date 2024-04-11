---
layout: post
title: 高级 RAG 检索策略之递归检索
date: 2024-04-09 15:34:44
description: 使用递归检索进行高级 RAG 检索
keywords: rag, llamaindex, recursive-retrieve
comments: true
categories: ai
tags: [rag, llamaindex, recursive-retrieve]
---

{% img /images/post/2024/04/recursive-retriever.jpeg 400 300 %}

随着 LLM（大语言模型）技术的发展，RAG（Retrieval-Augmented Generation）技术在问答、对话等任务中的应用越来越广泛。RAG 技术的一个重要组成部分是文档检索器，它负责从大量的文档中检索出与问题相关的文档，以供 LLM 生成答案。RAG 检索器的效果直接影响到 LLM 生成答案的效果，因此如何设计高效的 RAG 检索器是一个重要的研究课题。目前，有多种 RAG 的检索策略，本文将介绍一种高级的 RAG 检索策略——递归检索，它通过递归的方式检索相关文档，可以提高检索的效果。

<!--more-->

## 递归检索介绍

递归检索相较于普通 RAG 检索，可以解决后者因文档切片过大而导致检索信息不准确的问题，下面是递归检索的流程图：

{% img /images/post/2024/04/recursive-retriever-rag.png 1000 600 %}

- 递归检索在原始文档节点基础上，扩展了更多粒度更小的文档节点
- 检索文档时如果检索到扩展节点，会递归检索到其原始节点，然后再将原始节点做为检索结果提交给 LLM

在[LlamaIndex](https://www.llamaindex.ai/)的实现中，递归检索主要有两种方式：块引用的递归检索和元数据引用的递归检索。

## 普通 RAG 检索

在介绍递归检索之前，我们先来看下使用 LlamaIndex 进行普通 RAG 检索的代码示例：

```py
from llama_index.core import SimpleDirectoryReader
from llama_index.core.node_parser import SentenceSplitter
from llama_index.core import VectorStoreIndex

question = "奥创是由哪两位复仇者联盟成员创造的？"

documents = SimpleDirectoryReader("./data").load_data()
node_parser = SentenceSplitter(chunk_size=1024)
base_nodes = node_parser.get_nodes_from_documents(documents)
print(f"base_nodes len: {len(base_nodes)}")
for idx, node in enumerate(base_nodes):
    node.id_ = f"node-{idx}"
base_index = VectorStoreIndex(nodes=base_nodes)
base_retriever = base_index.as_retriever(similarity_top_k=2)
retrievals = base_retriever.retrieve(question)
for n in retrievals:
    print(
        f"Node ID: {n.node_id}\nSimilarity: {n.score}\nText: {n.text[:100]}...\n"
    )
response = base_retriever.query(question)
print(f"response: {response}")
print(f"len: {len(response.source_nodes)}")
```

- 我们在`data`目录中放置维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情来作为我们的文档测试数据
- 再使用`SentenceSplitter`文档解析器对文档进行解析，`SentenceSplitter`可以尽量保持句子和段落的完整性，默认的`chunk_size`是 1024
- 文档解析器解析后的原始节点 id 默认是一个随机字符串，我们将其格式化为`node-{idx}`的形式，方便我们后面验证检索结果
- 然后创建`VectorStoreIndex`索引，将原始节点传入，再创建一个检索器`base_retriever`，设置`similarity_top_k=2`，表示检索时返回相似度最高的 2 个节点，然后打印出检索到的节点信息
- 最后使用检索器对问题生成答案，并打印出答案

我们来看下程序运行的结果：

```bash
base_nodes len: 15
Node ID: node-0
Similarity: 0.8425314373498192
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...

Node ID: node-1
Similarity: 0.8135015554872678
Text: 奥创来到克劳位于南非的武器船厂获取所有振金，并砍断克劳的左手。复仇者们到达后跟他们正面交锋，但大多数人被旺达用幻象术迷惑，看到各自心中最深层的“阴影”；唯独托尔看见在家乡阿萨神域发生的不明景象。旺达同...

response: 奥创是由托尼·斯塔克和布鲁斯·班纳这两位复仇者联盟成员创造的。
nodes len: 2
```

可以看到通过文档解析器解析后的原始节点有 15 个，检索到的节点有 2 个，这两个节点都是原始节点。

## 块引用的递归检索

块引用的递归检索是在普通 RAG 检索的基础上，将每个原始文档节点拆分成更小的文档节点，这些节点跟原始节点是父子关系，当检索到子节点时，会递归检索到其父节点，然后再将父节点为检索结果提交给 LLM。

下面我们通过代码示例来理解块引用的递归检索，首先我们创建几个 chunk_size 更小的文档解析器：

```py
sub_chunk_sizes = [128, 256, 512]
sub_node_parsers = [
    SentenceSplitter(chunk_size=c, chunk_overlap=20) for c in sub_chunk_sizes
]
```

再通过文档解析器将原始节点解析成子节点：

```py
from llama_index.core.schema import IndexNode

all_nodes = []
for base_node in base_nodes:
    for n in sub_node_parsers:
        sub_nodes = n.get_nodes_from_documents([base_node])
        sub_inodes = [
            IndexNode.from_text_node(sn, base_node.node_id) for sn in sub_nodes
        ]
        all_nodes.extend(sub_inodes)

    original_node = IndexNode.from_text_node(base_node, base_node.node_id)
    all_nodes.append(original_node)
print(f"all_nodes len: {len(all_nodes)}")

# 显示结果
all_nodes len: 331
```

- 我们使用每个小 chunk 的文档解析器对原始节点进行解析，然后将解析后的子节点和原始节点放入`all_nodes`列表中
- 每个原始节点的 chunk_size 是 1024，如果按照 chunk_size 为 512 大小进行拆分，大概会产生 2 个左右的子节点，如果按照 chunk_size 为 256 大小进行拆分，大概会产生 4 个左右的子节点，如果按照 chunk_size 为 128 大小进行拆分，大概会产生 8 个左右的子节点
- 每个子节点`node_id`属性的值是原始节点的`id_`，也就是我们之前格式化的`node-{idx}`，但是子节点的`id_`属性值还是由 LlamaIndex 生成的随机字符串
- 原始节点是一个`TextNode`类型的节点，我们将其转换成`IndexNode`类型的节点，并添加到`all_nodes`列表中，最终产生了 331 个节点

{% img /images/post/2024/04/recursive-retriever-chunk.png 1000 600 %}

然后我们再创建检索索引，将所有节点传入，先对问题进行一次普通检索，观察普通检索的结果：

```py
vector_index_chunk = VectorStoreIndex(all_nodes)
vector_retriever_chunk = vector_index_chunk.as_retriever(similarity_top_k=2)
nodes = vector_retriever_chunk .retrieve(question)
for node in nodes:
    print(
        f"Node ID: {node.node_id}\nSimilarity: {node.score}\nText: {node.text[:100]}...\n"
    )

# 显示结果
Node ID: 0e3409e5-6c84-4bbf-886a-40e8553eb463
Similarity: 0.8476561735049716
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...

Node ID: 0ed2ca24-f262-40fe-855b-0eb84c1a1567
Similarity: 0.8435371049710689
Text: 奥创来到克劳位于南非的武器船厂获取所有振金，并砍断克劳的左手。复仇者们到达后跟他们正面交锋，但大多数人被旺达用幻象术迷惑，看到各自心中最深层的“阴影”；唯独托尔看见在家乡阿萨神域发生的不明景象。旺达同...
```

- 创建`VectorStoreIndex`索引，将所有节点传入，再创建一个检索器`vector_retriever_chunk`，设置`similarity_top_k=2`，表示检索时返回相似度最高的 2 个节点
- 在普通检索的结果中，可以看到检索出来 2 个子节点，因为其 Node ID 是随机字符串，而不是我们之前格式化的`node-{idx}`

我们再来看看使用递归检索的检索结果：

```py
from llama_index.core.retrievers import RecursiveRetriever

all_nodes_dict = {n.node_id: n for n in all_nodes}
retriever_chunk = RecursiveRetriever(
    "vector",
    retriever_dict={"vector": vector_retriever_chunk},
    node_dict=all_nodes_dict,
    verbose=True,
)
nodes = retriever_chunk.retrieve(question)
for node in nodes:
    print(
        f"Node ID: {node.node_id}\nSimilarity: {node.score}\nText: {node.text[:1000]}...\n"
    )

# 显示结果
Retrieving with query id None: 奥创是由哪两位复仇者联盟成员创造的？
Retrieved node with id, entering: node-0
Retrieving with query id node-0: 奥创是由哪两位复仇者联盟成员创造的？
Node ID: node-0
Similarity: 0.8476561735049716
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...
```

- 首先构造一个`all_nodes_dict`字典，将所有节点的`node_id`作为 key，节点对象作为 value，这是为了递归检索时能够通过`node_id`找到对应的节点对象
- 再创建一个`RecursiveRetriever`检索器，将`vector_retriever_chunk`检索器和`all_nodes_dict`字典传入，设置`verbose=True`，表示打印检索过程
- 最后对问题进行递归检索，可以看到检索结果是 1 个原始节点，这是因为在之前的普通检索结果中，**2 个子节点的父节点都是同一个原始节点**，所以递归检索时只返回了这个原始节点，而且这个节点的相似度分数跟普通检索结果的第一个节点是一样的：`0.8476561735049716`

最后使用 LLM 对问题生成答案：

```py
from llama_index.core.query_engine import RetrieverQueryEngine

llm = OpenAI(model="gpt-3.5-turbo", temperature=0.1)
query_engine_chunk = RetrieverQueryEngine.from_args(retriever_chunk, llm=llm)
response = query_engine_chunk.query(question)
print(f"response: {str(response)}")
print(f"nodes len: {len(response.source_nodes)}")

# 显示结果
response: 奥创是由托尼·斯塔克和布鲁斯·班纳这两位复仇者联盟成员创造的。
nodes len: 1
```

可以看到递归检索生成的答案跟普通 RAG 检索生成的答案是一样的。

## 元数据引用的递归检索

基于元数据引用的递归检索和块引用的递归检索类似，只是在解析原始节点时，不是将原始节点进行拆分，而是根据原始节点来生成元数据子节点，然后再将元数据子节点和原始节点一起传入检索索引。

下面我们通过代码示例来理解元数据引用的递归检索，首先我们创建几个元数据的提取器：

```py
from llama_index.core.extractors import (
    SummaryExtractor,
    QuestionsAnsweredExtractor,
)

extractors = [
    SummaryExtractor(summaries=["self"], show_progress=True),
    QuestionsAnsweredExtractor(questions=5, show_progress=True),
]
```

- 我们创建了 2 个元数据提取器，一个是`SummaryExtractor`，用于生成文档的摘要，另一个是`QuestionsAnsweredExtractor`，用于生成文档中可以回答的问题
- QuestionsAnsweredExtractor 的参数`questions=5`表示生成 5 个问题
- `show_progress=True`表示显示提取过程
- 这 2 个提取器使用 LLM 进行元数据生成，默认使用的是 OpenAI 的 GPT-3.5-turbo 模型

然后我们通过元数据提取器将原始节点解析成元数据子节点：

```py
node_to_metadata = {}
for extractor in extractors:
    metadata_dicts = extractor.extract(base_nodes)
    for node, metadata in zip(base_nodes, metadata_dicts):
        if node.node_id not in node_to_metadata:
            node_to_metadata[node.node_id] = metadata
        else:
            node_to_metadata[node.node_id].update(metadata)
```

- 我们分别使用 2 种提取器对原始节点进行元数据生成，并将结果保存在 node_to_metadata 字典中
- node_to_metadata 字典的 key 是原始文档的 node_id，value 是原始节点的元数据，包括摘要和问题

代码执行后 node_to_metadata 的数据结构如下所示：

```json
{
  "node-0": {
    "section_summary": "...",
    "questions_this_excerpt_can_answer": "1. ...?\n2. ...?\n3. ...?\n4. ...?\n5. ...?"
  },
  "node-1": {
    "section_summary": "...",
    "questions_this_excerpt_can_answer": "1. ...?\n2. ...?\n3. ...?\n4. ...?\n5. ...?"
  },
  ......
}
```

我们可以将 node_to_metadata 的数据保存到文件中，方便后续使用，这样就不用每次都调用 LLM 来生成元数据了。

```py
import json

def save_metadata_dicts(path, data):
    with open(path, "w") as fp:
        json.dump(data, fp)


def load_metadata_dicts(path):
    with open(path, "r") as fp:
        data = json.load(fp)
    return data

save_metadata_dicts("output/avengers_metadata_dicts.json", node_to_metadata)
node_to_metadata = load_metadata_dicts("output/avengers_metadata_dicts.json")
```

- 我们定义了 2 个方法，一个是`save_metadata_dicts`，用于将元数据字典保存到文件中，另一个是`load_metadata_dicts`，用于从文件中加载元数据字典
- 我们将元数据字典保存到`output/avengers_metadata_dicts.json`文件中
- 以后重新需要使用元数据字典时，可以使用 load_metadata_dicts 方法直接从文件中加载

我们再将原始节点和元数据子节点组合成一个新的节点列表：

```py
import copy

all_nodes = copy.deepcopy(base_nodes)
for node_id, metadata in node_to_metadata.items():
    for val in metadata.values():
        all_nodes.append(IndexNode(text=val, index_id=node_id))
print(f"all_nodes len: {len(all_nodes)}")

# 显示结果
all_nodes len: 45
```

- 我们首先将原始节点拷贝到新的节点列表中
- 然后将元数据字典中的摘要和问题作为新的节点，添加到新的节点列表中，并与原始节点进行关联，与其形成父子关系
- 最终产生了 45 个节点，其中包括 15 个原始节点和 30 个元数据子节点

{% img /images/post/2024/04/recursive-retriever-metadata.png 1000 600 %}

我们可以看下新节点列表中`node-0`原始节点和其子节点的内容：

```py
node0_nodes = list(
    filter(
        lambda x: x.id_ == "node-0"
        or (hasattr(x, "index_id") and x.index_id == "node-0"),
        all_nodes,
    )
)
print(f"node0_nodes len: {len(node0_nodes)}")
for node in node0_nodes:
    index_id_str = node.index_id if hasattr(node, 'index_id') else 'N/A'
    print(
        f"Node ID: {node.node_id}\nIndex ID: {index_id_str}\nText: {node.text[:100]}...\n"
    )

# 显示结果
node0_nodes len: 3
Node ID: node-0
Index ID: N/A
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...

Node ID: 45d41128-a8e6-4cdc-8ef3-7a71f01ddd96
Index ID: node-0
Text: The key topics of the section include the creation of Ultron by Tony Stark and Bruce Banner, the int...

Node ID: a06f3bb9-8a57-455f-b0c6-c9602b107158
Index ID: node-0
Text: 1. What are the names of the Avengers who raid the Hydra facility in Sokovia at the beginning of the...
```

- 我们使用`filter`函数过滤出`node-0`的原始节点和其相关联的元数据子节点，共有 3 个节点
- 其中第一个是原始节点，第二个是元数据摘要子节点，第三个是元数据问题子节点
- 因为元数据提取器使用的是英文模板的提示词，所以生成的元数据子节点的文档是英文的

然后我们再创建检索索引，将所有节点传入，先对问题进行一次普通检索，观察普通检索的结果：

```py
vector_index_metadata = VectorStoreIndex(all_nodes)
vector_retriever_metadata = vector_index_metadata.as_retriever(similarity_top_k=2)

enginer = vector_index_metadata.as_query_engine(similarity_top_k=2)
nodes = enginer.retrieve(question)
for node in nodes:
    print(
        f"Node ID: {node.node_id}\nSimilarity: {node.score}\nText: {node.text[:100]}...\n"
    )

# 显示结果
Node ID: d2cc032a-b258-4715-b335-ebd1cf80494d
Similarity: 0.857976008616706
Text: The key topics of the section include the creation of Ultron by Tony Stark and Bruce Banner, the int...

Node ID: node-0
Similarity: 0.8425314373498192
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...
```

- 创建`VectorStoreIndex`索引，将所有节点传入，再创建一个检索器`vector_retriever_metadata`，设置`similarity_top_k=2`，表示检索时返回相似度最高的 2 个节点
- 在普通检索的结果中，可以看到检索出来的结果是 2 个节点，第一个是元数据摘要子节点，第二个是原始节点，通过其`Node ID`可以对是否原始节点进行识别

上面是普通检索的结果，我们再来看使用递归检索的检索结果：

```py
all_nodes_dict = {n.node_id: n for n in all_nodes}
retriever_metadata = RecursiveRetriever(
    "vector",
    retriever_dict={"vector": vector_retriever_metadata},
    node_dict=all_nodes_dict,
    verbose=False,
)
nodes = retriever_metadata.retrieve(question)
for node in nodes:
    print(
        f"Node ID: {node.node_id}\nSimilarity: {node.score}\nText: {node.text[:100]}...\n\n"
    )

# 显示结果
Node ID: node-0
Similarity: 0.857976008616706
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的...
```

- 这里的代码和之前的块引用的递归检索类似，只是将`vector_retriever_chunk`替换成了`vector_retriever_metadata`，然后对问题进行递归检索
- 可以看到最终检索出来的结果只有 1 个原始节点，这是因为在之前的普通检索结果中，返回 1 个元数据子节点和1 个原始节点，而**这个子节点的父节点又是这个原始节点**，所以递归检索时只返回了这个原始节点，并且这个原始节点的相似度分数跟普通检索结果的第一个节点相同：`0.857976008616706`

最后使用 LLM 对问题生成答案：

```py
query_engine_metadata = RetrieverQueryEngine.from_args(retriever_metadata, llm=llm)
response = query_engine_metadata.query(question)
print(f"response: {str(response)}")
print(f"nodes len: {len(response.source_nodes)}")

# 显示结果
response: 奥创是由托尼·斯塔克和布鲁斯·班纳这两位复仇者联盟成员创造的。
nodes len: 1
```

可以看到递归检索生成的答案跟普通 RAG 检索生成的答案是一样的。

## 检索效果对比

我们接下来使用[Trulens](https://www.trulens.org/)来评估普通 RAG 检索、块引用的递归检索和元数据引用的递归检索的效果。

```py
tru.reset_database()
rag_evaluate(base_engine, "base_evaluation")
rag_evaluate(engine, "recursive_retriever_chunk_evaluation")
rag_evaluate(engine, "recursive_retriever_metadata_evaluation")
Tru().run_dashboard()
```

`rag_evaluate`的具体代码可以看我[之前的文章](https://zhaozhiming.github.io/2024/03/11/sentence-windows-rag/)，主要是使用 Trulens 的`groundedness`，`qa_relevance`和`qs_relevance`对 RAG 检索结果进行评估。执行代码后，我们可以在浏览器中看到 Trulens 的评估结果：

{% img /images/post/2024/04/rr-evaluate.png 1000 600 %}

在评估结果中，我们可以看到两种递归检索都比普通 RAG 检索效果要好，元数据引用的递归检索比块引用的递归检索效果更好一些，但评估结果并不是绝对的，具体的评估效果还要根据实际情况来评估。

## 总结

递归检索是一种高级的 RAG 检索策略，开始通过原始文档节点扩展出更多粒度更小的文档节点，这样在检索过程中可以更加准确地检索到相关的文档，然后再通过递归检索找出与之相匹配的原始文档节点。递归检索可以提高 RAG 检索的效果，但是也会增加检索的时间和计算资源，因此在实际应用中需要根据实际情况来选择合适的检索策略。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
