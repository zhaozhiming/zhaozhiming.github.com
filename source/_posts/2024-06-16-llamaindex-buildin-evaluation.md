---
layout: post
title: 评估 RAG？只要 LlamaIndex 就足够了
date: 2024-06-16 07:06:29
description: 介绍如何使用 LlamaIndex 内置评估工具进行 RAG 应用评估
keywords: rag, llamaindex, evaluation
comments: true
categories: ai
tags: [rag, llamaindex, evaluation]
---

{% img /images/post/2024/06/llamaindex-evaluation.jpg 400 300 %}

我们之前介绍过一些 RAG （Retrieval Augmented Generation）的评估工具，比如 Turlens、Ragas 等，它们的评估指标丰富、使用方便，但它们始终是独立的第三方工具，需要和 LLM（大语言模型）开发框架（LangChain、LlamaIndex）进行集成才能使用，功能一旦更新不及时就会导致不可用的问题。如果你正在使用的是 LlamaIndex 开发框架，那么恭喜你，LlamaIndex 内置了评估工具，可以帮助你快速评估 RAG 应用，无需集成第三方的评估工具。今天我们就来详细了解一下 LlamaIndex 内置评估工具的原理以及它们的使用方法。

<!--more-->

## LlamaIndex 评估工具

[LlamaIndex](https://www.llamaindex.ai/) 不但可以与很多外部优秀的第三方评估工具进行集成，而且在内部也自带了一套评估工具，如果你想快速地体验 RAG 的评估功能，那么使用 LlamaIndex 内置的评估工具就足够了。LlamaIndex 有以下评估指标：

- Answer Relevcancy
- Context Relevancy
- Relevancy
- Faithfulness
- Correctness

这些评估指标我们后面会详细介绍，另外还有 LlamaIndex 特有的对比评估 Pairwise，可以帮助你评估两个检索引擎哪个生成的答案更好。

LlamaIndex 还提供了测试数据的生成功能，可以帮助我们轻松地生成评估所需的测试数据，包括评估的问题、参考答案等，这样我们就可以快速地进行评估工作，而不需要花费大量的时间去准备测试数据。

如果你想提升评估工作的效率，LlamaIndex 也提供了批量运行评估任务的工具，可以快速评估多种评估指标以及大量测试数据，批量任务的执行时间和单次任务的执行时间基本无异，这样就可以帮助我们快速地执行大量评估任务。

## 测试数据生成

评估 RAG 应用需要用到几个评估实体，分别是：

- Question: 指用户输入的问题，RAG 应用通过问题检索到相关的文档上下文
- Context: 指检索到的文档上下文，RAG 应用检索到相关文档后会将这些上下文结合用户问题一起提交给 LLM，最后生成答案
- Answer: 指生成的答案，RAG 应用将问题和上下文提交给 LLM 后，LLM 会根据这些信息来生成答案
- Grouth Truth: 指人工标注的正确答案，利用这个实体可以对生成的答案进行分析，从而得到评估结果，在 LlamaIndex 中，这个实体叫做 Reference Answer

其中 Question 和 Ground Truth 通过用户提供，Context 通过检索得到，Answer 是由 LLM 生成，后面我们在讲解的时候会沿用这些实体名称。在 LlamaIndex 中提供了生成测试数据集的功能，可以帮助我们快速生成测试数据集，无需人工干预。

首先我们来看下如何生成评估所需的 Question，这里的测试文档使用维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情，示例代码如下：

```py
from llama_index.core import SimpleDirectoryReader
from llama_index.core.llama_dataset.generator import RagDatasetGenerator
from llama_index.llms.openai import OpenAI

documents = SimpleDirectoryReader("./data").load_data()
llm = OpenAI(model="gpt-3.5-turbo")
dataset_generator = RagDatasetGenerator.from_documents(
    documents,
    llm=llm,
    num_questions_per_chunk=1,
)
dataset = dataset_generator.generate_questions_from_nodes()
examples = dataset.examples
for i, example in enumerate(examples):
    contexts = [n[:100] for n in example.reference_contexts]
    print(f"{i + 1}. {example.query}")

# 显示结果
1. Question: How did Ultron initially come into existence and what was his ultimate goal?
2. Question: What event prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame"?
3. Question: How does Thanos acquire the Power Stone and what events transpire after he obtains it?
4. Question: How does Thanos ultimately achieve his goal of completing the Gauntlet and causing half of all life across the universe to disintegrate in "Avengers: Infinity War"?
5. Question: How does Loki initially gain access to Earth and what is his ultimate goal upon arriving?
```

- 使用`SimpleDirectoryReader`读取文档
- LlamaIndex 在新版本中推荐使用`RagDatasetGenerator`来生成测试数据，参数`documents`表示读取的文档列表，`llm`表示使用的大语言模型， 这里我们使用 OpenAI 的`gpt3.5`模型，`num_questions_per_chunk`表示每个文档生成的问题数量，这里我们设置为 1
- 然后调用数据生成器的`generate_questions_from_nodes`方法生成问题集，其原理是用 LLM 来根据文档生成问题，生成后的数据保存在`examples`属性中
- 最后遍历`examples` 对象，生成的 Question 在`example.query` 属性中
- 从显示结果中可以看到生成了 5 个 Question

除了生成 Question 外，数据生成器还可以生成 Ground Truth，示例代码如下：

```py
dataset = dataset_generator.generate_dataset_from_nodes()
examples = dataset.examples
for i, example in enumerate(examples):
    contexts = [n[:100] for n in example.reference_contexts]
    print(f"{i + 1}. {example.query}")
    print(f"Ground Truth: {example.reference_answer[:100]}...")

# 显示结果
1. Question: How did Ultron initially come into existence and what was his ultimate goal?
Ground Truth: Ultron initially came into existence when Tony Stark and Bruce Banner discovered an artificial intel...
2. Question: What event prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame"?
Ground Truth: The event that prompts the Avengers to devise a plan involving time travel to undo Thanos's actions ...
3. Question: How does Thanos acquire the Power Stone and what events transpire after he obtains it?
Ground Truth: Thanos acquires the Power Stone from the planet Xandar. After obtaining the Power Stone, Thanos and ...
4. Question: How does Thanos ultimately achieve his goal of completing the Gauntlet and causing half of all life across the universe to disintegrate in "Avengers: Infinity War"?
Ground Truth: Thanos ultimately achieves his goal of completing the Gauntlet and causing half of all life across t...
5. Question: How does Loki initially gain access to Earth and what is his ultimate goal upon arriving?
Ground Truth: Loki initially gains access to Earth by using the Tesseract to open a wormhole. His ultimate goal up...
```

这次使用数据生成器的`generate_dataset_from_nodes`方法来生成测试数据，生成的数据不仅包含 Question，还包含 Ground Truth，也是就代码中的`example.reference_answer`属性的值。其实除了 Question 和 Ground Truth 外，在生成的数据中还包含`reference_contexts`，这是数据生成器使用其内部检索器检索到的上下文，这个数据暂时对我们没有用处，我们只需要关注 Question 和 Ground Truth 即可。

### 将数据集保存到 json 文件

每次运行程序都重新生成一遍测试数据比较耗费资源，我们可以将生成的数据集保存到 json 文件中，下次直接读取 json 文件即可，示例代码如下：

```py
import os
from llama_index.core.llama_dataset.rag import LabelledRagDataset

dataset_json = "./output/test-dataset.json"
if not os.path.exists(dataset_json):
    dataset = dataset_generator.generate_dataset_from_nodes()
    examples = dataset.examples
    dataset.save_json(dataset_json)
else:
    dataset = LabelledRagDataset.from_json(dataset_json)
    examples = dataset.examples
```

- 保存数据时使用`dataset`对象的`save_json`方法
- 读取数据时使用`LabelledRagDataset`的`from_json`方法

## 评估指标

下面我们来详细介绍 LlamaIndex 的评估指标，并通过代码示例来了解如何使用这些评估指标。

### Answer Relevcancy

Answer Revelancy 是评估 Answer 和 Question 的相关性，这个指标可以帮助我们评估生成的答案是否和问题相关，示例代码如下：

```py
from llama_index.core.evaluation import AnswerRelevancyEvaluator
from llama_index.core import VectorStoreIndex, Settings
from llama_index.core.node_parser import SentenceSplitter

question = examples[0].query

node_parser = SentenceSplitter()
nodes = node_parser.get_nodes_from_documents(documents)
Settings.llm = llm
vector_index = VectorStoreIndex(nodes)
engine = vector_index.as_query_engine()
response = engine.query(question)
answer = str(response)

print(f"{question}")
print(f"Answer: {answer}")
evaluator = AnswerRelevancyEvaluator(llm)
result = evaluator.evaluate(query=question, response=answer)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")

# 显示结果
Question: How did Ultron initially come into existence and what was his ultimate goal?
Answer: Ultron initially came into existence when Tony Stark and Bruce Banner discovered an artificial intelligence within Loki's scepter and decided to use it to complete Stark's "Ultron" global defense program. Ultron's ultimate goal was to eradicate humanity in order to save Earth.

score: 1.0
feedback: 1. The provided response matches the subject matter of the user's query by explaining how Ultron initially came into existence and what his ultimate goal was.
2. The response directly addresses the focus and perspective of the user's query by detailing the specific events that led to Ultron's creation and his ultimate goal of eradicating humanity.

[RESULT] 2
```

- 我们使用测试数据集的第一条数据的问题作为评估问题
- 然后构建一个普通的 RAG 查询引擎，并通过查询评估问题来得到答案
- 将问题和答案传递给`AnswerRelevancyEvaluator`评估器，通过`evaluate`方法来评估问题和答案的相关性
- 评估结果的`score`范围是 0~1，得分越高表示答案和问题的相关性越高，得分为 1 表示完全相关
- 评估结果中还有`feedback`属性，用来解释评估结果，这个属性可以帮助我们了解评估结果的产生原因

LlamaIndex 中每种评估器的初始化参数都基本一致，以`AnswerRelevancyEvaluator` 为例， 有以下主要参数：

- llm: 评估使用的大语言模型
- eval_template: 评估时所用的提示词模板
- score_threshold: 这个参数在不同的评估器中有不同的含义，在`AnswerRelevancyEvaluator` 中这个参数用来将反馈中的分数转换到 0~1 范围，在`CorrectnessEvaluator` 中这个参数用来评判答案是否正确

在上面的反馈结果中我们可以看到`[RESULT] 2`，这个值就是反馈中的分数，LLM 在评估过程中评估了 2 个问题，每个问题回答正确则得 1 分，从得分结果来看，2 个问题都回答正确，所以得分为 2，然后除以阀值 2.0，得到最终分数为 1.0。

#### 评估提示词模板修改

`eval_template`参数用来设置评估提示词模板，我们可以来看下`AnswerRelevancyEvaluator`默认的评估提示词：

```py
from llama_index.core.prompts import PromptTemplate

DEFAULT_EVAL_TEMPLATE = PromptTemplate(
    "Your task is to evaluate if the response is relevant to the query.\n"
    "The evaluation should be performed in a step-by-step manner by answering the following questions:\n"
    "1. Does the provided response match the subject matter of the user's query?\n"
    "2. Does the provided response attempt to address the focus or perspective "
    "on the subject matter taken on by the user's query?\n"
    "Each question above is worth 1 point. Provide detailed feedback on response according to the criteria questions above  "
    "After your feedback provide a final result by strictly following this format: '[RESULT] followed by the integer number representing the total score assigned to the response'\n\n"
    "Query: \n {query}\n"
    "Response: \n {response}\n"
    "Feedback:"
)
```

评估提示词模板是一个`PromptTemplate`对象，这个对象有一个`template`属性，这个属性就是评估提示词模板的字符串内容，如果我们想要修改评估提示词，一种方法是重新写一套评估提示词指令，另外一种方法是在这个模板的前面或后面添加提示词来对评估指令进行微调，比如我想让 LLM 将评估结果用中文回复，示例代码如下：

```py
from llama_index.core.evaluation.answer_relevancy import DEFAULT_EVAL_TEMPLATE

translate_prompt = "\n\nPlease reply in Chinese."
eval_template = DEFAULT_EVAL_TEMPLATE
eval_template.template += translate_prompt
evaluator = AnswerRelevancyEvaluator(
    llm=llm, eval_template=eval_template
)
```

这里我们在`AnswerRelevancyEvaluator`的默认提示词模板上添加了返回中文回复的提示词，然后通过`eval_template`参数传递给评估器，这样评估器在评估任务完成后就会将评估结果用中文返回。

### Context Relevancy

Context Relevancy 是评估 Context 和 Question 的相关性，这个指标可以帮助我们评估检索到的文档上下文和问题的相关性，示例代码如下：

```py
from llama_index.core.evaluation import ContextRelevancyEvaluator

contexts = [n.get_content() for n in response.source_nodes]
evaluator = ContextRelevancyEvaluator(llm)
result = evaluator.evaluate(query=question, contexts=contexts)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")

# 显示结果
score: 1.0
feedback: 1. The retrieved context matches the subject matter of the user's query. It provides a detailed explanation of how Ultron initially came into existence and what his ultimate goal was.
2. The retrieved context can be used exclusively to provide a full answer to the user's query. It covers all the necessary information about Ultron's creation and his goal to eradicate humanity.

[RESULT] 4.0
```

- 我们通过查询引擎返回的结果`response`中的`source_nodes`属性获取到 Context，并将其转化为字符串列表，评估时需要这种格式的数据
- 构建`ContextRelevancyEvaluator`评估器
- 将 Question 和 Context 传递给评估器的`evaluate`方法进行评估
- 最后输出评估结果

从评估结果中可以看到，评估器评估了 2 个问题，每个问题得分 2，最终得分为 4，这个得分是通过评估器内部的评估模板计算出来的，分数经过转换后得到 score 为 1.0。

在评估结果中除了`score`和`feedback`属性外，还有其他一些属性：

- query: 评估的问题，也就是 Question
- contexts: 评估的上下文，也就是 Context
- response: 评估的回答，也就是 Answer
- passing: 是否通过，如果评估结果通过则为 True，否则为 False，在一些评估器中这个属性和评估器的`score_threshold`属性有关
- pairwise_source: 对比评估源，这是对比评估才有的属性，后面会详细介绍

### Relevancy

Relevancy 是评估 Answer、Context 与 Question 是否相关，这个指标可以帮助我们评估问题是否真正得到了回答，示例代码如下：

```py
from llama_index.core.evaluation import AnswerRelevancyEvaluator

evaluator = RelevancyEvaluator(llm)
result = evaluator.evaluate(query=question, response=answer, contexts=contexts)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")
print(f"passing: {result.passing}")

# 显示结果
score: 1.0
feedback: YES
passing: True
```

- 构建`RelevancyEvaluator`评估器
- 这个评估器需要传递 Question、Answer 和 Context 三个参数进行评估
- 最后输出评估结果

因为这个评估是检查 Answer 和 Context 是否与 Question 相关， 因此评估结果是一个布尔值， 当`feedback`为`YES`表示 Answer、Context 与 Question 相关，同时`passing`为`True`，`score`为 1.0。

### Faithfulness

Faithfulness 是评估 Answer 和 Context 是否匹配，这个指标可以帮助我们评估生成的答案是否符合上下文，检查答案是否有**幻觉**，示例代码如下：

```py
from llama_index.core.evaluation import FaithfulnessEvaluator

evaluator = FaithfulnessEvaluator(llm)
result = evaluator.evaluate(response=answer, contexts=contexts)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")
print(f"passing: {result.passing}")

# 显示结果
score: 1.0
feedback: YES
passing: True
```

- 构建`FaithfulnessEvaluator`评估器
- 这个评估器需要传递 Answer 和 Context 两个参数进行评估
- 最后输出评估结果，评估结果也是一个布尔值，当`feedback`为`YES`表示两者相关， 同时`passing`为`True`，`score`为 1.0

LlamaIndex 的评估工具不仅可以对检索引擎进行评估，还可以对 Pipeline 进行评估，只要将 Pipeline 的输出结果作为评估的参数即可：

```py
from llama_index.core.query_pipeline import QueryPipeline, InputComponent
from llama_index.core.response_synthesizers.simple_summarize import SimpleSummarize

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
output = p.run(input=question)
answer = str(output)
contexts = [n.get_content() for n in output.source_nodes]
```

我们创建一个基本的 RAG Pipeline， 然后使用 Pipeline 来代替检索引擎进行问题检索和回答生成，最后将得到的 Answer 和 Context 传递给评估器进行评估即可。关于`Pipeline`的更多介绍可以参考我之前的[这篇文章](https://zhaozhiming.github.io/2024/06/08/rag-module-pipeline/)。

### Correctness

Correctness 是评估 Answer 和 Ground Truth 的相关性和正确性，这个指标可以帮助我们评估生成的答案是否正确，示例代码如下：

```py
from llama_index.core.evaluation import CorrectnessEvaluator

evaluator = CorrectnessEvaluator(llm)
ground_truth = dataset_examples[1].reference_answer
print(f"{question}")
print(f"Answer: {answer}")
print(f"Ground Truth: {ground_truth}")
result = evaluator.evaluate(query=question, response=answer, reference=ground_truth)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")
print(f"passing: {result.passing}")

# 显示结果
Question: What event prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame"?
Answer: The event that prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame" occurs when Scott Lang escapes from the Quantum Realm and reaches the Avengers Compound. He explains that he experienced only five hours while trapped, despite being there for five years. This leads to the realization that the Quantum Realm allows for time travel, prompting the Avengers to ask Tony Stark to help them retrieve the Infinity Stones from the past to reverse Thanos's actions in the present.
Ground Truth: The event that prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame" is the discovery that Thanos has already destroyed the Infinity Stones, preventing any further use to reverse his actions.

score: 4.0
feedback: The generated answer is relevant and mostly correct in detailing the events leading to the Avengers' decision to use time travel in "Avengers: Endgame." It accurately describes Scott Lang's escape from the Quantum Realm and his crucial role in introducing the concept of time manipulation via the Quantum Realm. However, it slightly deviates from the reference answer, which emphasizes the destruction of the Infinity Stones by Thanos as the critical event. The generated answer instead focuses on the discovery of time travel as a viable option, which is also a correct perspective but not the only one. Thus, the score reflects high relevance and correctness with a minor deviation in focus.
passing: True
```

- 构建`CorrectnessEvaluator`评估器
- 使用我们之前创建的测试数据集中某条数据的`reference_answer`作为 Ground Truth
- 将 Question、Answer 和 Ground Truth 传递给评估器的`evaluate`方法进行评估

`CorrectnessEvaluator`评估器的得分范围是 1 ～ 5，当分数大于等于 4 时表示答案正确，`passing`为`True`，评估器根据 Qustion、Answer 和 Ground Truth 进行评估，最后输出评估结果。

### Pairwise

Pairwise 是对比评估，可以帮助我们评估两个检索引擎生成的 Answer 哪个更好，在执行对比评估之前，我们需要再构建一个检索引擎，这个检索引擎我们使用不同的文档分块策略，这样才可以与之前的检索引擎进行区分，示例代码如下：

```py
documents = SimpleDirectoryReader("./data").load_data()
node_parser = SentenceSplitter(chunk_size=128, chunk_overlap=25)
nodes = node_parser.get_nodes_from_documents(documents)
Settings.llm = llm
vector_index = VectorStoreIndex(nodes)
second_engine = vector_index.as_query_engine()
second_response = engine.query(question)
second_answer = str(second_response)
```

- 原来的检索引擎`engine`使用的是`SentenceSplitter`文档分割器默认的分块策略，`chunk_size`为 1024，`chunk_overlap`为 200
- 我们新建了另外一个检索引擎`second_engine`，并将文档分割器的`chunk_size`设置为 128，`chunk_overlap`设置为 25
- 然后使用`second_engine`来查询问题，得到另一个答案`second_answer`

然后我们使用`PairwiseEvaluator`评估器来对比两个答案，示例代码如下：

```py
from llama_index.core.evaluation import PairwiseComparisonEvaluator

print(f"{question}")
print(f"Answer: {answer}")
print(f"Second Answer: {second_answer}")
evaluator = PairwiseComparisonEvaluator(llm)
result = evaluator.evaluate(
    query=question, response=answer, second_response=second_answer
)
print(f"score: {result.score}")
print(f"feedback: {result.feedback}")
print(f"pairwise source: {str(result.pairwise_source)}")

# 显示结果
Question: What event prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame"?
Answer: The event that prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame" is the discovery that Thanos has already destroyed the Infinity Stones, preventing any further use to reverse his actions.
Second Answer: The destruction of the Infinity Stones by Thanos prompts the Avengers to devise a plan involving time travel to undo Thanos's actions in "Avengers: Endgame".

score: 1.0
feedback: Assistant A provides a more detailed and informative response by explaining that the Avengers discover that Thanos has already destroyed the Infinity Stones, which is the event that prompts them to devise a plan involving time travel to undo his actions in "Avengers: Endgame." This additional context enhances the understanding of the situation and the motivation behind the Avengers' plan. Assistant B, on the other hand, simply states that the destruction of the Infinity Stones by Thanos is the event that leads to the Avengers' plan without providing any further elaboration.

Therefore, based on the level of detail and clarity provided in the responses, [[A]] Assistant A is better.
pairwise source: EvaluationSource.ORIGINAL
```

- 构建`PairwiseComparisonEvaluator`评估器
- 将 Question、Answer 和 Second Answer 传递给评估器的`evaluate`方法进行评估

在显示结果中，我们打印了 Question、Answer 和 Second Answer，以及评估结果的几个属性，从评估结果中可以看到，第一个 Answer 比第二个 Answer 更好。在评估结果中还有一个`pairwise_source`属性，值是`EvaluationSource.ORIGINAL`，表示评估顺序是原始顺序。

在 `PairwiseComparisonEvaluator`评估器中，有一个初始化参数`enforce_consensus`，默认值是 True。在评估器进行对比评估时，首先会将 Answer 和 Second Answer 进行对比， 即`evaluate(response=answer, second_response=second_answer)`，如果`enforce_consensus`为 True，**则会将 Answer 和 Second Answer 反过来再进行对比**， 即`evaluate(response=second_answer, second_response=answer)`， 最后根据两次结果来产生最终的评估结果。如果最终结果使用的是反转后的结果，那么`pairwise source`的值就是`EvaluationSource.FLIPPED`。

可以看下另外一种对比结果，在下面的评估结果中，2 个 Answer 的得分一样，评估结果是平局：

```sh
score: 0.5
feedback: Both Assistant A and Assistant B provided the same answer to the user's question, stating that Tony Stark and Bruce Banner are the two members of the Avengers who created Ultron. Since both responses are identical in terms of accuracy and relevance to the user's question, there is no significant difference between the two answers. Therefore, in this case, it is a tie between Assistant A and Assistant B.

Therefore, the final verdict is '[[C]]' for a tie.
pairwise_source: EvaluationSource.ORIGINAL
```

## 批量评估

介绍完了 LlamaIndex 的评估指标后，有人可能会担心如果一次性运行这么多评估指标，那么运行时间会不会很长，其实不用担心，LlamaIndex 很贴心地提供了一个批量评估的工具，可以帮助我们快速地运行多个评估指标，示例代码如下：

```py
from llama_index.core.evaluation import BatchEvalRunner

answer_relevancy_evaluator = AnswerRelevancyEvaluator(llm)
context_relevancy_evaluator = ContextRelevancyEvaluator(llm)
relevant_evaluator = RelevancyEvaluator(llm)
correctness_evaluator = CorrectnessEvaluator(llm)
faithfulness_evaluator = FaithfulnessEvaluator(llm)

runner = BatchEvalRunner(
    evaluators={
        "answer_relevancy": answer_relevancy_evaluator,
        "context_relevancy": context_relevancy_evaluator,
        "relevancy": relevant_evaluator,
        "correctness": correctness_evaluator,
        "faithfulness": faithfulness_evaluator,
    },
    workers=8,
)
questions = [example.query for example in examples]
ground_truths = [example.reference_answer for example in examples]
metrics_results = runner.evaluate_queries(
    engine, queries=questions, reference=ground_truths
)

for metrics in metrics_results.keys():
    print(f"metrics: {metrics}")
    eval_results = metrics_results[metrics]
    for eval_result in eval_results:
        print(f"score: {eval_result.score}")
        print(f"feedback: {eval_result.feedback}")
        if eval_result.passing is not None:
            print(f"passing: {eval_result.passing}")

# 显示结果
metrics: answer_relevancy
score: 1.0
feedback: 1. The provided response matches the subject matter of the user's query by explaining how Ultron initially came into existence and what his ultimate goal was.
2. The response directly addresses the focus and perspective of the user's query by detailing the specific events that led to Ultron's creation and his ultimate goal of eradicating humanity.

[RESULT] 2
......
```

- 我们首先创建了 5 个评估器，分别是`AnswerRelevancyEvaluator`、`ContextRelevancyEvaluator`、`RelevancyEvaluator`、`CorrectnessEvaluator`、`FaithfulnessEvaluator`
- 然后通过测试数据集提取了 Question 列表`questions`和 Ground Truth 列表`ground_truths`，每个列表分别有 5 个元素
- 使用`BatchEvalRunner`构建一个批量评估运行器，初始化参数`evaluators`为 5 个评估器，`workers`参数表示并行运行的工作线程数，`workers`的数量可以根据运行机器上的 CPU 核数来决定
- 调用`aevaluate_queries`方法来运行评估，传递的参数是查询引擎、Question 列表和 Ground Truth 列表
- 评估结果最后会根据评估器名称保存在`metrics_results`字典中，我们遍历这个字典，输出评估结果

5 个评估器加上 5 个问题，相当于我们执行了 25 次评估，但执行时间和运行单次评估的时间基本相同，但需要注意的是，`BatchEvalRunner`只能在检索引擎下使用，不能通过 Pipeline 使用。

## 优缺点

LlamaIndex 内置的评估工具有以下优缺点。

### 优点

- 不需要额外安装第三方库，可以快速使用
- 评估指标可以满足大部分评估需求

### 缺点

- 评估方法基本上是通过 LLM 加提示词的方式来评估，评估使用的 LLM 不同，可能评估效果差别也会比较大，其他 RAG 评估工具会使用一些计算公式来结合提示词进行评估，从而减小 LLM 的影响
- 是 LlamaIndex 内置的功能，这是优点也是缺点，毕竟评估功能与其他 RAG 功能相比重要性较低，以后随着 LlamaIndex 更多新功能的加入，评估功能的开发优先级可能会降低

## 总结

总体而言，LlamaIndex 的评估功能可以帮助我们快速地评估 RAG 的性能，满足我们基本的 RAG 评估需求，无需借助其他第三方库。如果你正在使用 LlamaIndex 开发 RAG 应用，建议使用 LlamaIndex 内置的评估工具，使用后如果发现满足不了需求再考虑使用其他第三方评估工具。希望这篇文章可以帮助大家更好地了解 LlamaIndex 的评估功能。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
