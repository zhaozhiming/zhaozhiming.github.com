---
layout: post
title: 高级 RAG 检索策略之查询重写
date: 2024-05-13 22:18:11
description: 介绍高级 RAG 检索中几种查询重写的策略
keywords: rag, llamaindex, query-rewrite, hyde, stepback
comments: true
categories: ai
tags: [rag, llamaindex, query-rewrite, hyde, stepback]
---

{% img /images/post/2024/05/rag-query-rewrite.jpeg 400 300 %}

在 RAG（Retrieval Augmented Generation）应用中，文档检索是保证 RAG 应用高质量回答的关键环节，我们在之前的文章中也有所介绍，但除此之外，对用户问题的优化也同样重要，有时候用户的问题可能不够清晰或者不够具体，这时候就需要对用户问题进行查询重写，这样才能更好地提高检索的准确性。今天我们就来介绍一些 RAG 应用中查询重写的策略，以及了解如何在实际项目中使用它们。

<!--more-->

## 子问题查询

子问题策略，也称为子查询，是一种用于生成子问题的技术。子问题策略的核心思想是在问答过程中，为了更好地理解和回答主问题，系统会自动生成并提出与主问题相关的子问题。这些子问题通常具有更具体的细节，可以帮助系统更深入地理解主问题，从而进行更加准确的检索并提供正确的答案。

{% img /images/post/2024/05/sub-question-flow.png 1000 400 %}

- 子问题策略首先将用户问题通过 LLM（大语言模型）生成多个子问题
- 然后将每个子问题经过 RAG 流程得到各自的答案（检索-生成）
- 最后将所有子问题的答案合并，得到最终的答案

### 代码示例

在[LlamaIndex](https://www.llamaindex.ai/)中已经对子问题查询进行了实现，但在查看子问题查询的效果之前，我们先看普通 RAG 检索对于复杂问题的效果：

```py
from llama_index.core import VectorStoreIndex, SimpleDirectoryReader

question = "哈莉·奎因和灭霸在《复仇者联盟》中是正义的角色吗？"

documents = SimpleDirectoryReader("./data").load_data()
node_parser = VectorStoreIndex.from_documents(documents)
query_engine = node_parser.as_query_engine()
response = query_engine.query(question)
print(f"base query result: {response}")

# 显示结果
base query result: 不，哈莉·奎茵和灭霸在《复仇者联盟》系列中并非被描绘为正义的角色。
```

以上代码是 LlamaIndex 的普通 RAG 检索过程，文档数据我们还是使用之前维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情来进行测试，这里我们问了一个复合问题，包括 2 个人物角色，一个是 DC 漫画中的`哈莉·奎茵`，另一个是漫威电影中的`灭霸`，问题是他们在复仇者联盟中是否为正义角色，查询结果虽然可以说正确，但并没有指出其中一个人物不是漫威电影中的人物。

我们再来看子问题查询的效果：

```py
from llama_index.core.tools import QueryEngineTool, ToolMetadata
from llama_index.core.query_engine import SubQuestionQueryEngine

query_engine_tools = [
    QueryEngineTool(
        query_engine=query_engine,
        metadata=ToolMetadata(
            name="Avengers",
            description="Marvel movie The Avengers",
        ),
    ),
]
query_engine = SubQuestionQueryEngine.from_defaults(
    query_engine_tools=query_engine_tools
)
response = query_engine.query(question)
print(f"sub question query result: {response}")

# 显示结果
Generated 2 sub questions.
[Avengers] Q: 哈莉·奎茵在《复仇者联盟》电影中扮演什么角色？
[Avengers] Q: 灭霸在《复仇者联盟》电影中扮演什么角色？
[Avengers] A: 在提供的有关《复仇者联盟》电影的背景中未提到哈莉·奎茵。
[Avengers] A: 灭霸是《复仇者联盟》电影中的主要反派。他是一个强大的战争领主，试图利用无限宝石按照自己的愿景重塑宇宙。灭霸被描绘为强大而无情的敌人，对复仇者联盟和整个宇宙构成重大威胁。
sub question query result: 在提供的有关《复仇者联盟》电影的背景中未提到哈莉·奎茵。灭霸是《复仇者联盟》电影中的主要反派，被描绘为一个强大而无情的敌人。
```

- 首先构建查询引擎工具，在工具中传入普通的查询引擎，并设置工具的元数据，元数据信息在 Debug 信息中会进行展示
- 使用`SubQuestionQueryEngine`类构建子问题查询引擎，传入查询引擎工具
- 查询结果中会显示生成的子问题以及子问题的答案，最终答案基于所有子问题的答案进行生成

从上面的代码可以看出，对于复杂问题，子问题查询的效果要比普通查询更加准确。

上面的示例中，生成的子问题及答案是随着 Debug 信息展示出来的，我们也可以在检索过程中获取这些数据：

```py
from llama_index.core.callbacks import (
    CallbackManager,
    LlamaDebugHandler,
    CBEventType,
    EventPayload,
)
from llama_index.core import Settings

llama_debug = LlamaDebugHandler(print_trace_on_end=True)
callback_manager = CallbackManager([llama_debug])
Settings.callback_manager = callback_manager

# 子问题查询代码
...

for i, (start_event, end_event) in enumerate(
    llama_debug.get_event_pairs(CBEventType.SUB_QUESTION)
):
    qa_pair = end_event.payload[EventPayload.SUB_QUESTION]
    print("Sub Question " + str(i) + ": " + qa_pair.sub_q.sub_question.strip())
    print("Answer: " + qa_pair.answer.strip())
```

- 在子问题查询的代码前面加上回调管理器，用来记录子问题查询的调试信息
- 在查询结束后，通过回调管理器获取子问题查询的调试信息，然后得到子问题和答案

### 提示词

LlamaIndex 使用单独的 Python 包`llama-index-question-gen-openai`来生成子问题，它的内部默认使用 OpenAI 的模型来生成子问题，提示词模板可以在 [LlamaIndex 的官方仓库](https://github.com/run-llama/llama_index/blob/main/llama-index-integrations/question_gen/llama-index-question-gen-openai/llama_index/question_gen/openai/base.py#L18-L45)中查看。

我们也可以通过以下方法来打印 LlamaIndex 中的提示词，第一种方法是通过`get_prompts()`方法来打印，示例代码如下：

```py
prompts = query_engine.get_prompts()
for key in prompts.keys():
    sub_question_prompt = prompts[key]
    template = sub_question_prompt.get_template()
    print(f'prompt: {template}')
```

- 首先通过`get_prompts()`方法获取查询引擎的 prompts 对象，基本上每个 LlamaIndex 对象都有这个方法
- prompts 对象是个 JSON 对象，它的每个 Key 代表一个提示词模板
- 遍历 prompts 对象的每个 Key，获取每个 Key 对应的提示词模板，然后打印出来
- 子问题查询会包含 2 个提示词模板，一个是子问题生成的提示词模板，另一个是普通 RAG 的提示词模板

另外一种方式是通过`set_global_handler`进行全局设置，示例代码如下：

```py
from llama_index.core import set_global_handler

set_global_handler("simple")
```

在文件开头加上以上代码，这样在执行代码的过程中就会打印出 RAG 检索过程中的提示词，打印出的提示词不是提示词模板，而是加入了具体变量值之后的**完整提示词**。

## HyDE 查询转换

{% img /images/post/2024/05/hyde_paper.jpeg 1000 400 %}

HyDE（Hypothetical Document Embeddings）的本质是通过 LLM 对用户问题生成假设性文档，这些文档基于 LLM 本身的知识生成，可能存在错误或者不准确，但是跟 RAG 中知识库的文档相关联，然后通过假设性文档去检索向量相近的真实文档，通过这种方式来提高检索的准确性，HyDE 的论文可以参考[这里](https://arxiv.org/pdf/2212.10496.pdf)。

### 代码示例

在 LlamaIndex 中已经实现了 HyDE 的查询重写，我们先来看 LlamaIndex 如何生成假设性文档：

```py
from llama_index.core.indices.query.query_transform import HyDEQueryTransform

question = "洛基使用了哪种神秘物品试图征服地球？"

hyde = HyDEQueryTransform(include_original=True)
query_bundle = hyde(question)
print(f"query_bundle embedding len: {len(query_bundle.embedding_strs)}")
for idx, embedding in enumerate(query_bundle.embedding_strs):
    print(f"embedding {idx}: {embedding[:100]}")

# 显示结果
query_bundle embedding len: 2
embedding 0: 在他试图征服地球时，洛基使用了立方体，也被称为宇宙立方。这个神秘的...
embedding 1: 洛基使用了哪种神秘物品试图征服地球？
```

- 首先构建`HyDEQueryTransform`对象，传入参数`include_original=True`，表示在生成的假设性文档中包含原始问题，其实`include_original`的默认值就是`True`，这里传入参数只是为了演示
- 然后调用`hyde`对象，传入问题，返回一个`QueryBundle`对象
- `QueryBundle`对象的`embedding_strs`属性值是一个数组，数组第一个元素是生成的假设性文档，如果`include_original`为`True`，那么数组的第 2 个元素会包含原始问题

可以看到 LLM 基于自己的知识很好地回答了用户的问题，生成的假设性文档和电影剧情基本一致。

LlamaIndex 中生成假设性文档的提示词模板如下，大意就是为问题生成一段内容，其中`{context_str}`为用户问题：

```py
HYDE_TMPL = (
    "Please write a passage to answer the question\n"
    "Try to include as many key details as possible.\n"
    "\n"
    "\n"
    "{context_str}\n"
    "\n"
    "\n"
    'Passage:"""\n'
)
```

下面我们再用查询引擎对问题进行检索：

```py
from llama_index.core.query_engine import TransformQueryEngine

hyde_query_engine = TransformQueryEngine(query_engine, hyde)
response = hyde_query_engine.query(question)
print(f"hyde query result: {response}")

# 显示结果
hyde query result: 洛基在试图征服地球时使用了立方体，这是一个未知潜力的强大能源。
```

- 基于`HyDEQueryTransform`构建一个`TransformQueryEngine`
- 查询引擎的`query`方法会先对原始问题生成假设性文档，然后用假设性文档进行检索并生成答案

虽然我们得到了正确的结果，但我们不清楚 LlamaIndex 内部在检索过程中是否用假设性文档去检索，我们可以通过以下代码来验证：

```py
from llama_index.core.retrievers.transform_retriever import TransformRetriever

retriever = node_parser.as_retriever(similarity_top_k=2)
hyde_retriever = TransformRetriever(retriever, hyde)
nodes = hyde_retriever.retrieve(question)
print(f"hyde retriever nodes len: {len(nodes)}")
for node in nodes:
    print(f"node id: {node.id_}, score: {node.get_score()}")

print("=" * 50)
nodes = retriever.retrieve("\n".join(f"{n}" for n in query_bundle.embedding_strs))
print(f"hyde documents retrieve len: {len(nodes)}")
for node in nodes:
    print(f"node id: {node.id_}, score: {node.get_score()}")
```

- 上半部分使用`TransformRetriever`结合原始检索器和`HyDEQueryTransform`对象构建一个新的检索器
- 然后用**新的检索器对用户问题**进行检索，打印出检索到的文档 ID 和分数
- 下半部分使用**原始检索器对假设性文档**进行检索，假设性文档取自`QueryBundle`对象的`embedding_strs`属性，这里的`embedding_strs`有 2 个元素，一个是假设性文档，另一个是原始问题
- 打印出用假设性文档检索到的文档 ID 和分数

下面是显示的结果：

```bash
hyde retriever nodes len: 2
node id: 51e9381a-ef93-49ee-ae22-d169eba95549, score: 0.8895532276574978
node id: 5ef8a87e-1a72-4551-9801-ae7e792fdad2, score: 0.8499209871867581
==================================================
hyde documents retrieve nodes len: 2
node id: 51e9381a-ef93-49ee-ae22-d169eba95549, score: 0.8842142746289462
node id: 5ef8a87e-1a72-4551-9801-ae7e792fdad2, score: 0.8460828835028101
```

可以看到两者的结果基本一致，证明检索所用的**输入**是相似的，也就是假设性文档，我们再把`HyDEQueryTransform`对象中的`include_original`属性设置为`False`，这意味着生成的假设性文档不包含原始问题，然后再次运行代码，结果如下：

```bash
hyde retriever nodes len: 2
node id: cfaea328-16d8-4eb8-87ca-8eeccad28263, score: 0.7548985780343257
node id: f47bc6c7-d8e1-421f-b9b8-a8006e768c04, score: 0.7508234876205329
==================================================
hyde documents retrieve nodes len: 2
node id: 6c2bb8cc-3c7d-4f92-b039-db925dd60d53, score: 0.7498683385309097
node id: f47bc6c7-d8e1-421f-b9b8-a8006e768c04, score: 0.7496147322045141
```

可以看到两者的结果也是基本一致，但是由于缺少原始问题，检索到的文档分数较低。

### HyDE 的限制

HyDE 生成的假设性文档是基于 LLM 的知识生成的，可能存在错误或者不准确，LlamaIndex 在[官方文档](https://docs.llamaindex.ai/en/stable/examples/query_transformations/HyDEQueryTransformDemo/?h=hyde)中指出 HyDE 可能会误导查询和引起偏见，所以在实际应用中需要谨慎使用。

## 回溯提示（STEP-BACK PROMPTING）

{% img /images/post/2024/05/stepback_paper.jpeg 1000 400 %}

回溯提示是一种简单的提示技术，通过抽象化来引导 LLM 从具体实例中提取高级概念和基本原理，利用这些概念和原理指导推理，可以显著提高 LLM 遵循正确推理路径解决问题的能力。
以上图中第一个的问题为例，原始问题是给定温度和体积求压强，在左边的回答中，不管是原始的回答还是思维链方式的回答，结果都不正确。而通过回溯提示的方式，先通过原始问题生成一个更为广泛的问题，比如求问题背后的物理公式，再通过广泛问题得到答案，最后将广泛问题的答案和原始问题一起提交给 LLM，从而得到正确的答案。回溯提示的论文可以参考[这里](https://arxiv.org/pdf/2310.06117.pdf)。

### 代码示例

回溯提示在 LlamaIndex 中没有具体的实现，但我们可以通过原始调用 LLM 结合 LlamaIndex 的方式来进行演示，首先我们来让 LLM 根据原始问题生成一个回溯的问题：

```py
from llama_index.core import PromptTemplate
from openai import OpenAI

client = OpenAI()

examples = [
        {
            "input": "Who was the spouse of Anna Karina from 1968 to 1974?",
            "output": "Who were the spouses of Anna Karina?",
        },
        {
            "input": "Estella Leopold went to whichschool between Aug 1954and Nov 1954?",
            "output": "What was Estella Leopold'seducation history?",
        },
    ]

    few_shot_examples = "\n\n".join(
        [f"human: {example['input']}\nAI: {example['output']}" for example in examples]
    )

    step_back_question_system_prompt = PromptTemplate(
        "You are an expert at world knowledge."
        "Your task is to step back and paraphrase a question to a more generic step-back question,"
        "which is easier to answer. Here are a few examples:\n"
        "{few_shot_examples}"
    )

    completion = client.chat.completions.create(
        model="gpt-3.5-turbo",
        temperature=0.1,
        messages=[
            {
                "role": "system",
                "content": step_back_question_system_prompt.format(
                    few_shot_examples=few_shot_examples
                ),
            },
            {"role": "user", "content": question},
        ],
    )
    step_back_question = completion.choices[0].message.content
    print(f"step_back_question: {step_back_question}")
```

- 首先我们定义了一些回溯问题的例子，将这些例子放到 LLM 的系统提示词中让 LLM 了解生成问题的规律
- 将用户问题和系统提示词一起传给 LLM，让 LLM 生成回溯问题

生成了回溯问题后，我们再分别对原始问题和回溯问题进行检索，获取它们相关的文档：

```py
retrievals = retriever.retrieve(question)
normal_context = "\n\n".join([f"{n.text}" for n in retrievals])
retrievals = retriever.retrieve(step_back_question)
step_back_context = "\n\n".join([f"{n.text}" for n in retrievals])
```

得到了检索结果后，我们让 LLM 生成最终的答案：

```py
step_back_qa_prompt_template = PromptTemplate(
        "Context information is below.\n"
        "---------------------\n"
        "{normal_context}\n"
        "{step_back_context}\n"
        "---------------------\n"
        "Given the context information and not prior knowledge, "
        "answer the question: {question}\n"
    )

    completion = client.chat.completions.create(
        model="gpt-3.5-turbo",
        temperature=0.1,
        messages=[
            {
                "role": "system",
                "content": "Always answer the question, even if the context isn't helpful.",
            },
            {
                "role": "user",
                "content": step_back_qa_prompt_template.format(
                    normal_context=normal_context,
                    step_back_context=step_back_context,
                    question=question,
                ),
            },
        ],
    )
    step_back_result = completion.choices[0].message.content
    print(f"step_back_result: {step_back_result}")
```

- 在提示词模板中，我们将原始问题和回溯问题的文档信息传给 LLM，并结合原始问题让 LLM 生成答案

最后我们看下普通 RAG 检索和使用回溯提示后的 RAG 检索两者的区别：

```bash
question: 泰坦星球上有过一场大战吗？
base_result: 没有，泰坦星球上没有发生过大战。它并不是任何已知重大冲突或战争的发生地。
====================================================================================================
step_back_question: 泰坦星球上发生过什么重要事件吗？
step_back_result: 是的，在漫威电影宇宙中，泰坦星球上发生了一场重大的冲突。在《复仇者联盟：无限战争》中，泰坦被描绘成灭霸的毁灭故乡，泰坦上的战斗涉及一群英雄，包括钢铁侠（托尼·斯塔克）、蜘蛛侠（彼得·帕克）、奇异博士（斯蒂芬·斯特兰奇）以及银河护卫队，他们试图阻止灭霸实现他的目标。
```

可以看到没有使用回溯提示的结果是错误的，而使用了回溯提示之后，我们得到了问题在知识库文档中的正确答案。

## 总结

今天我们介绍了 RAG 检索中几种查询重写的策略，包括子问题查询、HyDE 查询转换和回溯提示，并通过 LlamaIndex 对这几种策略进行了代码演示，在演示过程中还介绍了一些 LlamaIndex 的使用技巧。还有其他一些查询重写的策略没有在本文中介绍，随着 RAG 技术的发展，查询重写的策略也会越来越多，我们未来在合适的时候再对这一部分进行补充，希望这些内容对大家有所帮助。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
