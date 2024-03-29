---
layout: post
title: 使用 Trulens 评估 RAG 应用
date: 2024-01-29 09:47:05
description: 介绍使用 Trulens 如何安装使用以及其内部原理
keywords: llm, rag, trulens
comments: true
categories: ai
tags: [llm, rag, trulens]
---

{% img /images/post/2024/01/trulens.jpg 400 300 %}

目前基于大语言模型（LLM）的 RAG（Retrieval Augmented Generation）应用非常广泛，包括知识库问答、客服机器人、垂直领域知识检索等各个方面，虽然我们可以构建出这类应用，但是如何评估 RAG 应用的效果却是一个难题。幸运的是业界已经开始推出一些 RAG 评估工具，Trulens 就是其中的一个。本文将介绍如何使用 Trulens 这个工具来对 RAG 应用进行评估，同时介绍 Trulens 内部的实现原理，以及在探索过程中发现的一些有趣知识。

<!--more-->

## Trulens 介绍

[TruLens](https://www.trulens.org/)是一款旨在评估和改进 LLM 应用的软件工具，它相对独立，可以集成 LangChain 或 LlamaIndex 等 LLM 开发框架。它使用反馈功能来客观地衡量 LLM 应用的质量和效果。这包括分析相关性、适用性和有害性等方面。TruLens 提供程序化反馈，支持 LLM 应用的快速迭代，这比人工反馈更快速、更可扩展。它适用于各种用途，如聊天机器人，并可以轻松集成到现有的 LLM 应用中。TruLens 是由 AI 质量软件公司 [TruEra](https://truera.com/) 开发的开源项目。

## Trulens 核心概念

在 Trulens 的设计中，他们优先提出了 RAG 应用的三大相关性评估：Anwer Relevance（答案相关性）、Context Relevance（上下文相关性） 和 Groundedness（基于实际情况的相关性）。

{% img /images/post/2024/01/rag_triad.jpg 1000 600 %}

- Anwer Relevance：衡量 LLM 的最终回答如何解答原始问题，确保其具有帮助性和相关性。
- Context Relevance：评估 RAG 检索到的上下文（也就是检索到的文档）与原始问题的相关性。这一点非常重要，因为上下文构成了 LLM 答案的基础。
- Groundedness：评估 LLM 的最终回答是否与上下文中提供的事实（检索到的文档）保持一致，确保不夸大或偏离给定的信息。

这三大组成部分共同确保 LLM 的回答准确、相关且没有出现幻觉。

## 结合 LlamaIndex 使用 Trulens

我们将使用[LlamaIndex](https://www.llamaindex.ai/)这个 LLM 应用框架来实现简单的 RAG 应用，再用 Trulens 评估其效果。在 RAG 应用中，我们使用大家熟知的漫威电影**复仇者联盟**相关剧情来作为测试文档，通过输入相关的问题，RAG 应用检索出相关的剧情介绍并回答问题，文档内容主要从维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)条目中获取，主要包括 4 部复仇者联盟电影的剧情信息。

首先我们需要安装 LlamaIndex 和 Trulens 的 Python 依赖包：

```bash
pip install llama-index trulens-eval
```

然后使用 LlamaIndex 来创建一个简单的 RAG 功能：

```py
from llama_index import VectorStoreIndex, SimpleDirectoryReader

documents = SimpleDirectoryReader("./data").load_data()
index = VectorStoreIndex.from_documents(documents)
query_engine = index.as_query_engine()
```

以上代码会从 `./data` 目录下读取文档，解析并将文档分块存储到向量数据库，同时创建一个向量存储索引。LlamaIndex 默认使用的 LLM 是 OpenAI 的`gpt3.5-turbo`模型，Embedding 使用的是 OpenAI 的`text-embedding-ada-002`模型。

`data`目录是存放测试文档的地方，其目录结构如下：

```bash
data/
├── 复仇者联盟.txt
├── 复仇者联盟2：奥创纪元.txt
├── 复仇者联盟3：无限战争.txt
└── 复仇者联盟4：终局之战.txt
```

每个文档包含了该部电影的剧情信息，接下来我们使用 Trulens 来逐一创建三大相关性评估，首先是`Groundedness`评估：

```py
from trulens_eval import Feedback, TruLlama
from trulens_eval.feedback import Groundedness
from trulens_eval.feedback.provider.openai import OpenAI

openai = OpenAI()

grounded = Groundedness(groundedness_provider=openai)
groundedness = (
    Feedback(grounded.groundedness_measure_with_cot_reasons, name="Groundedness")
    .on(TruLlama.select_source_nodes().node.text)
    .on_output()
    .aggregate(grounded.grounded_statements_aggregator)
)
```

- 使用 Trulens 提供的 Provider 类来创建一个 OpenAI Provider，在实际做评估时会使用到 OpenAI 的 LLM 来为 RAG 应用评分，更多的 Provider 类可以参考 Trulens 的文档
- 定义了一个`Groundedness`对象来集成之前的 Provider
- 定义一个`Feedback`对象来实现评估功能，这里使用构建者模式来创建`Feedback`对象
- 在`Feedback`构造器方法中，需要传入一个评估方法，我们使用`Groundedness`对象中的`groundedness_measure_with_cot_reasons`方法，表示使用思维链的方式来进行评估
- `Feedback`的`on`和`on_output` 方法是选择输入和输出，以`Groundedness`相关性评估为例，输入是检索到的文档，输出是 LLM 的最终结果
- `aggregate`方法表示评估结果的聚合方式，这里使用`Groundedness`对象中的`grounded_statements_aggregator`方法来作为评估结果的聚合方式

接下来我们再创建`Answer Relevance`评估：

```py
qa_relevance = Feedback(
    openai.relevance_with_cot_reasons, name="Answer Relevance"
).on_input_output()
```

- `Answer Relevance`比较简单，我们同样使用`Feedback`来构建评估方法
- 使用了 OpenAI Provider 的`relevance_with_cot_reasons`方法来作为评估方法，也是用思维链的方式评估
- 使用`on_input_output`传入默认的输入和输出参数，`Answer Relevance`评估的输入是原始问题，输出是 LLM 的最终结果

然后再创建`Context Relevance`评估：

```py
import numpy as np

qs_relevance = (
    Feedback(openai.qs_relevance_with_cot_reasons, name="Context Relevance")
    .on_input()
    .on(TruLlama.select_source_nodes().node.text)
    .aggregate(np.mean)
)
```

- `Context Relevance`我们同样使用`Feedback`来构建评估方法，使用了 OpenAI Provider 的`qs_relevance_with_cot_reasons`方法来作为评估方法，也是用思维链的方式评估
- 在输入和输出参数中，`Context Relevance`评估的输入是原始问题，输出是检索到的文档
- `aggregate`方法我们使用了`np.mean`来作为评估结果的聚合方式，这也是 Trulens 默认的聚合方式

我们将这些评估方法集成到 Trulens 中：

```py
tru_query_engine_recorder = TruLlama(
    query_engine,
    app_id="Avengers_App",
    feedbacks=[groundedness, qa_relevance, qs_relevance],
)
```

- `TruLlama`是 Trulens 集成 LlamaIndex 的类，初始化参数包括 LlamaIndex 的查询引擎`query_engine`、应用 ID`app_id`和评估方法`feedbacks`，`feedbacks`包含了之前创建的 3 种评估方法。

接着我们准备好一些问题，通过`query_engine`进行检索和回答，在回答问题的过程中 Trulens 会触发评估方法并记录信息，从而收集评估结果：

```py
questions = [
    "洛基使用了哪种神秘物品试图征服地球？",
    "奥创是由哪两位复仇者联盟成员创造的？",
    "灭霸如何实现灭绝宇宙一半生命的计划？",
    "复仇者联盟用什么方法来逆转灭霸的行动？",
    "为了击败灭霸，哪位复仇者联盟成员牺牲了自己？",
]

with tru_query_engine_recorder as recording:
    for question in questions:
        query_engine.query(question)
```

最后，我们打开 Trulens 的仪表盘来查看评估结果：

```py
from trulens_eval import Tru

tru = Tru()
tru.reset_database()
tru.run_dashboard()
```

- 为了可以重复执行程序，这里我们使用了`tru.reset_database()`来重置数据库，清空之前收集的评估结果
- 然后我们使用`tru.run_dashboard()`来运行 Trulens 的仪表盘

在浏览器中访问`localhost:8501`可以看到最终的评估结果：

{% img /images/post/2024/01/trulens-dashboard1.png 1000 400 %}

{% img /images/post/2024/01/trulens-dashboard2.png 1000 400 %}

有 5 个问题，因此会产生 5 条记录，在第二张图片中，选择其中一个记录，可以看到记录评估结果的详细信息，包括每个问题的`Answer Relevance`、`Context Relevance`和`Groundedness`相关性评估。

## Trulens 提示词模板

在 Trulens 内部实现中，是通过提示词模板来让 LLM 生成评估结果的，我们通过 Trulens 的提示词来了解其实现原理。

### Groundedness 提示词模板

```py
"""You are a INFORMATION OVERLAP classifier providing the overlap of information between a SOURCE and STATEMENT.
For every sentence in the statement, please answer with this template:

TEMPLATE:
Statement Sentence: <Sentence>,
Supporting Evidence: <Choose the exact unchanged sentences in the source that can answer the statement, if nothing matches, say NOTHING FOUND>
Score: <Output a number between 0-10 where 0 is no information overlap and 10 is all information is overlapping>
Give me the INFORMATION OVERLAP of this SOURCE and STATEMENT.

SOURCE: {premise}

STATEMENT: {hypothesis}
"""
```

提示词模板中的变量`premise`是检索到文档，`hypothesis`是 LLM 的最终回答，每个文档经过评估后生成以下 3 个结果：

- Statement Sentence：检索到的原始文档
- Supporting Evidence：原始文档中与 LLM 最终回答相关的句子
- Score：匹配度得分，范围是 0-10

以这个问题为例：`灭霸如何实现灭绝宇宙一半生命的计划？`，检索到的文档有 2 个，经过 LLM 评估后的结果如下：

```
Statement Sentence: 灭霸通过获取六颗无限宝石并将它们装配在无限手套上，实现了他消灭宇宙一半生命的计划。
Supporting Evidence: 灭霸从星球Xandar获得了力量宝石——六颗无限宝石之一后，他和他的副手——厄奎斯·莫、库尔·奥比迪恩、普罗克西玛·午夜和科尔弗斯·格莱夫截击了运载阿斯加德幸存者的宇宙飞船。
Score: 7

Statement Sentence: 凭借完成的无限手套，灭霸能够利用宝石的力量，通过一次手指的轻响将宇宙中一半的生命消灭。
Supporting Evidence: 包括巴恩斯、格鲁特、旺达、威尔逊、曼蒂斯、德拉克斯、奎尔、斯特兰奇、帕克、玛丽亚·希尔和尼克·弗瑞等，在整个宇宙中的一半生命都消散了，而最后一位在消失之前通过改装的呼叫器发出了紧急信号的是尼克·弗瑞。
Score: 8
```

Groundedness 的计分方法是这样的：平均得分是 (7+8)/2 = 7.5，除以 10 之后得到 0.75。

### Answer Relevance 提示词模板

```py
"""You are a RELEVANCE grader; providing the relevance of the given RESPONSE to the given PROMPT.
Respond only as a number from 0 to 10 where 0 is the least relevant and 10 is the most relevant.

A few additional scoring guidelines:

- Long RESPONSES should score equally well as short RESPONSES.
# 长回答与短回答的得分应该相同。
- Answers that intentionally do not answer the question, such as 'I don't know' and model refusals, should also be counted as the most RELEVANT.
# 故意不回答问题的答案，比如“我不知道”和模型拒绝，也应被视为最相关。
- RESPONSE must be relevant to the entire PROMPT to get a score of 10.
# 回答必须与整个提示相关，才能获得10分。
- RELEVANCE score should increase as the RESPONSE provides RELEVANT context to more parts of the PROMPT.
# 相关性分数应随着回答为提示的更多部分提供相关上下文而增加。
- RESPONSE that is RELEVANT to none of the PROMPT should get a score of 0.
# 对于与提示无关的回答，得分应为0。
- RESPONSE that is RELEVANT to some of the PROMPT should get as score of 2, 3, or 4. Higher score indicates more RELEVANCE.
# 对于与提示部分相关的回答，得分应为2、3或4。较高的得分表示更相关。
- RESPONSE that is RELEVANT to most of the PROMPT should get a score between a 5, 6, 7 or 8. Higher score indicates more RELEVANCE.
# 对于与提示大部分相关的回答，得分应在5、6、7或8之间。较高的得分表示更相关。
- RESPONSE that is RELEVANT to the entire PROMPT should get a score of 9 or 10.
# 对于与整个提示相关的回答，得分应为9或10。
- RESPONSE that is RELEVANT and answers the entire PROMPT completely should get a score of 10.
# 对于与整个提示完全相关且完整回答的回答，得分应为10。
- RESPONSE that confidently FALSE should get a score of 0.
# 对于自信地错误的回答，得分应为0。
- RESPONSE that is only seemingly RELEVANT should get a score of 0.
# 对于看似相关但实际上无关的回答，得分应为0。
- Never elaborate.
# 不要详细阐述。

PROMPT: {prompt}

RESPONSE: {response}

RELEVANCE: """
```

- 模板中的`prompt`变量是原始的问题，`response`是 LLM 的最终答案
- 这是思维链的提示词模板，可以看到评分标准非常多，LLM 会根据评分标准来为两者的相关性打分

### Context Relevance 提示词模板

```py
"""You are a RELEVANCE grader; providing the relevance of the given STATEMENT to the given QUESTION.
Respond only as a number from 0 to 10 where 0 is the least relevant and 10 is the most relevant.

A few additional scoring guidelines:

- Long STATEMENTS should score equally well as short STATEMENTS.
# 长陈述与短陈述的得分应该相同。
- RELEVANCE score should increase as the STATEMENT provides more RELEVANT context to the QUESTION.
# 相关性分数应随着陈述为问题提供更多相关上下文而增加。
- RELEVANCE score should increase as the STATEMENT provides RELEVANT context to more parts of the QUESTION.
# 相关性分数应随着陈述为问题的更多部分提供相关上下文而增加。
- STATEMENT that is RELEVANT to some of the QUESTION should score of 2, 3 or 4. Higher score indicates more RELEVANCE.
# 对于与问题部分相关的陈述，得分应为2、3或4。较高的得分表示更相关。
- STATEMENT that is RELEVANT to most of the QUESTION should get a score of 5, 6, 7 or 8. Higher score indicates more RELEVANCE.
# 对于与问题大部分相关的陈述，得分应为5、6、7或8。较高的得分表示更相关。
- STATEMENT that is RELEVANT to the entire QUESTION should get a score of 9 or 10. Higher score indicates more RELEVANCE.
# 对于与整个问题相关的陈述，得分应为9或10。较高的得分表示更相关。
- STATEMENT must be relevant and helpful for answering the entire QUESTION to get a score of 10.
# 陈述必须对于回答整个问题具有相关性和帮助性，才能获得10分。
- Answers that intentionally do not answer the question, such as 'I don't know', should also be counted as the most relevant.
# 故意不回答问题的答案，比如“我不知道”，也应被视为最相关。
- Never elaborate.
# 不要详细阐述。
QUESTION: {question}

STATEMENT: {statement}

RELEVANCE: """
```

- 模板中的`question`变量是原始的问题，`statement`是检索到的文档
- 和`Answer Relevance` 类似，也是使用了思维链的方式来进行评分

## Trulens 集成自定义 LLM

如果你有为评估任务而专门微调过的模型，也可以在 Trulens 中集成使用，来代替其默认的 OpenAI 模型，以下是在 Trulens 中集成自定义模型的方法。

在 Trulens 可以支持的 LLM Provider 中，包括了 Langchain 的 Provider，这意味着我们可以将 Langchain 中的自定义模型集成到 Trulens 中。

首先创建一个自定义 LLM 对象，然后在 Trulens 的 Langchain Provider 中传入这个对象

```py
from trulens_eval.feedback.provider.langchain import Langchain
from langchain_llm import Langchain_CustomLLM

langchain_llm = Langchain_CustomLLM()
langchain_provider = Langchain(chain = langchain_llm)
```

关于 Langchain_CustomLLM 的创建，可以参考 Langchain 的[自定义 LLM 文档](https://python.langchain.com/docs/modules/model_io/llms/custom_llm)。

在原来几个相关性评估的代码中，我们只要将原来的 OpenAI Provider 替换掉即可：

```py
grounded = Groundedness(groundedness_provider=langchain_provider)

qa_relevance = Feedback(
    langchain_provider.relevance_with_cot_reasons, name="Answer Relevance"
).on_input_output()

qs_relevance = (
    Feedback(langchain_provider.qs_relevance_with_cot_reasons, name="Context Relevance")
    .on_input()
    .on(TruLlama.select_source_nodes().node.text)
    .aggregate(np.mean)
)
```

集成了自定义 LLM 的另外一个好处是，你可以在自己的 LLM 中观察 Trulens 的提示词信息，以确定其是否符合你的预期。

在 LlamaIndex 中也可以使用自定义 LLM 模型来代替默认的 OpenAI 模型，参考代码如下：

```py
from llamaindex_custom_embedding import CustomEmbeddings
from llamaindex_custom_llm import Llamaindex_CustomLLM
from llama_index import ServiceContext, VectorStoreIndex

llm = Llamaindex_CustomLLM()
embed_model = CustomEmbeddings(
    url="http://localhost:9997", model_name="bge-base-zh-v1.5"
)

service_context = ServiceContext.from_defaults(
    llm=llm,
    embed_model=embed_model
)
index = VectorStoreIndex.from_documents(documents, service_context=service_context)
```

- 这里通过 LlamaIndex 的 ServiceContext 来设置自定义的模型，包括 LLM 模型和 Embedding 模型，并将其传递给 VectorStoreIndex
- LlamaIndex 自定义 LLM 模型可以参考 LlamaIndex 的[这个文档](https://docs.llamaindex.ai/en/stable/module_guides/models/llms/usage_custom.html)
- LlamaIndex 自定义 Embedding 模型可以参考 LlamaIndex 的[这个文档](https://docs.llamaindex.ai/en/latest/examples/embeddings/custom_embeddings.html)

## GroundTruth 相关性评估

其实 Trulens 除了之前介绍的三大相关性评估外，还可以评估用户提供的标准答案和 LLM 的最终答案的相关性，在 Trulens 中称为`GroundTruth`相关性评估。

在原来的 RAG 应用中加入`GroundTruth`评估的方法如下：

```py
from trulens_eval.feedback import GroundTruthAgreement

standard_questions = [
    {"query": "洛基使用了哪种神秘物品试图征服地球？", "response": "宇宙魔方"},
    {"query": "奥创是由哪两位复仇者联盟成员创造的？", "response": "托尼·斯塔克（钢铁侠）和布鲁斯·班纳（绿巨人）"},
    {"query": "灭霸如何实现灭绝宇宙一半生命的计划？", "response": "使用六颗无限宝石"},
    {"query": "复仇者联盟用什么方法来逆转灭霸的行动？", "response": "通过时间旅行收集宝石"},
    {"query": "为了击败灭霸，哪位复仇者联盟成员牺牲了自己？", "response": "托尼·斯塔克（钢铁侠）"},
]

ground_truth = Feedback(
    GroundTruthAgreement(standard_questions).agreement_measure, name="Ground Truth"
).on_input_output()

tru_query_engine_recorder = TruLlama(
    query_engine,
    app_id="Avengers_App",
    feedbacks=[
        ground_truth, # 增加了 GroundTruth 评估
        groundedness,
        qa_relevance,
        qs_relevance,
    ],
)
```

- 首先我们定义了原来 5 个问题的标准答案，然后创建了一个`Feedback`对象来加载`GroundTruthAgreement`参数，其中集成了我们的标准答案
- 然后在 TruLlama 对象的`feedbacks`参数中加入`ground_truth`对象

修改完代码后再次运行之前的程序，可以在 Trulens 的仪表盘中看到新增的`Ground Truth`评估指标：

{% img /images/post/2024/01/trulens-ground-truth1.png 1000 400 %}

{% img /images/post/2024/01/trulens-ground-truth2.png 1000 400 %}

在查看`Ground Truth`评估的过程中，有时候会发现有些问题即使三大相关评估得分都很高，但是`Ground Truth`却不正确，比如`为了击败灭霸，哪位复仇者联盟成员牺牲了自己？`这个问题，标准答案给出的是`托尼·斯塔克（钢铁侠）`，但是`Ground Truth`评估给出的却是`娜塔莎·罗曼诺夫（黑寡妇）`，这是因为有多名复仇者联盟成员为了击败灭霸而牺牲。

## Trulens 数据结构分析

Trulens 的评估数据除了在仪表盘中展示外，我们还可以将其获取后集成到我们自己的应用中，Trulens 提供了获取评估数据的方法，示例代码如下：

```py
records, feedback = tru.get_records_and_feedback(app_ids=["Avengers_App"])
```

通过`get_records_and_feedback`方法可以获取到对应的问题记录和评估反馈信息，`app_ids`参数可以传入单个应用 ID 也可以为空，为空则表示获取所有应用的信息。

如果想进一步获取更多的信息，可以直接从 Trulens 的数据库中获取，Trulens 默认将数据存放到 Sqlite 数据库中，在运行了之前的程序后，会在当前目录下生成一个`default.sqlite`的文件，我们可以通过连接这个数据库文件来查询数据库内容。

我们使用 Sqlite 的命令行工具来看下数据库的结构，命令如下：

```bash
$ sqlite3 default.sqlite
sqlite> SELECT name FROM sqlite_master WHERE type='table';
alembic_version
apps
feedback_defs
feedbacks
records
```

可以看到数据库里总共有 5 张表，每张表的含义如下：

- alembic_version: 用于记录数据库的版本信息，每次运行迁移命令时会自动更新
- apps: 应用信息表，记录了应用的 ID、名称和其他信息，包括是用了哪种检索引擎，比如 Langchain 或者是 LlamaIndex 等
- feedback_defs: 评估反馈定义表，记录了评估反馈所用到的参数，包括评估时用到的 LLM Provider、推理方法等
- feedbacks: 评估反馈信息表，记录了评估反馈的具体信息，包括评估的得分、评估的输入输出、评估的反馈信息等
- records: 问题记录表，记录了问题记录的具体信息，包括原始问题、LLM 最终的答案、检索到的文档等

其中比较重要的是`records`和`feedbacks`表，这 2 张表涵盖了我们需要的大部分信息，表结构如下所示：

```bash
sqlite> PRAGMA table_info(records);
0|record_id|VARCHAR(256)|1||1
1|app_id|VARCHAR(256)|1||0
2|input|TEXT|0||0
3|output|TEXT|0||0
4|record_json|TEXT|1||0
5|tags|TEXT|1||0
6|ts|FLOAT|1||0
7|cost_json|TEXT|1||0
8|perf_json|TEXT|1||0

sqlite> PRAGMA table_info(feedbacks);
0|feedback_result_id|VARCHAR(256)|1||1
1|record_id|VARCHAR(256)|1||0
2|feedback_definition_id|VARCHAR(256)|0||0
3|last_ts|FLOAT|1||0
4|status|TEXT|1||0
5|error|TEXT|0||0
6|calls_json|TEXT|1||0
7|result|FLOAT|0||0
8|name|TEXT|1||0
9|cost_json|TEXT|1||0
10|multi_result|TEXT|0||0
```

- records 表中 input 字段是原始问题，output 字段是 LLM 最终的答案，record_json 字段包含了很多信息，包括检索的文档，embedding 变量等等
- feedbacks 表相对简单一些，name 字段是评估反馈的名称（3 种相关性评估之一），result 字段是评估的得分，calls_json 字段是评估的详细信息，包括输入输出、中间推理得到的证据等。

## 总结

随着 AI 技术的发展，我们除了要快速开发出容易使用的 RAG 应用，还需要对 RAG 应用进行准确的评估，今天我们介绍了使用 Trulens 来对 RAG 应用进行评估的方法，并介绍了 Trulens 评估框架的核心理念，最后介绍了在使用过程中发现的一些有用的技巧，希望这篇文章可以帮助到正在开发 RAG 应用的朋友，如果有问题和建议，欢迎在评论区留言。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
