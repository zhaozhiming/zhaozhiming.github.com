---
layout: post
title: 高级 RAG 检索策略之句子窗口检索
date: 2024-03-11 09:45:46
description: 使用句子窗口进行高级 RAG 检索
keywords: rag, llamaindex, sentence window
comments: true
categories: ai
tags: [rag, llamaindex, sentence window]
---

{% img /images/post/2024/03/sentence-window.jpg 400 300 %}

之前介绍过大语言模型（LLM）相关技术 RAG（Retrieval Augmented Generation）的内容，但随着 LLM 技术的发展，越来越多的高级 RAG 检索方法也随之被人发现，相对于普通的 RAG 检索，高级 RAG 通过更深化的技术细节、更复杂的搜索策略，提供出了更准确、更相关、更丰富的信息检索结果。今天我们就来介绍一下高级 RAG 检索策略其中的一种方法——句子窗口检索。

<!--more-->

## 句子窗口检索介绍

在介绍句子窗口检索之前，我们先简单介绍一下普通的 RAG 检索，下面是普通 RAG 检索的流程图：

{% img /images/post/2024/03/base-rag.png 1000 600 %}

- 先将文档切片成大小相同的块
- 将切片后的块进行 Embedding 并保存到向量数据库
- 根据问题检索出 Embedding 最相似的 K 个文档库
- 将问题和检索结果一起交给 LLM 生成答案

普通 RAG 检索的问题是如果文档切片比较大的话，检索结果可能会包含很多无关信息，从而导致 LLM 生成的结果不准确。我们再来看下句子窗口检索的流程图：

{% img /images/post/2024/03/sentence-window-rag.png 1000 600 %}

- 和普通 RAG 检索相比，句子窗口检索的文档切片单位更小，通常以句子为单位
- 在检索时，除了检索到匹配度最高的句子，还将该句子周围的上下文也作为检索结果一起提交给 LLM

句子窗口检索让检索内容更加准确，同时上下文窗口又能保证检索结果的丰富性。

### 原理

句子窗口检索的原理其实很简单，首先在文档切分时，将文档以句子为单位进行切分，同时进行 Embedding 并保存数据库。然后在检索时，通过问题检索到相关的句子，但并不只是将检索到的句子作为检索结果，而是将该句子前面和后面的句子一起作为检索结果，包含的句子数量可以通过参数来进行设置，最后将检索结果再一起提交给 LLM 来生成答案。

{% img /images/post/2024/03/sentence-window-rag-principle.jpg 1000 600 %}

我们再通过示例代码来理解句子窗口检索的原理，在 RAG 框架中，[LlamaIndex](https://www.llamaindex.ai/)很好地实现了句子窗口检索的功能，下面我们就用 LlamxIndex 来演示句子窗口检索的功能。

```py
from llama_index.core.node_parser import SentenceWindowNodeParser
from llama_index.core.schema import Document


node_parser = SentenceWindowNodeParser.from_defaults(
    window_size=3,
    window_metadata_key="window",
    original_text_metadata_key="original_text",
)
text = "hello. how are you? I am fine! Thank you. And you? I am fine too. "

nodes = node_parser.get_nodes_from_documents([Document(text=text)])
```

- 使用 SentenceWindowNodeParser 创建一个文档解析器，设置`window_size`为 3，这意味着句子窗口最多会包含 7 个句子，包括检索到的句子前面 3 个句子、检索到的句子本身以及检索到的句子后面 3 个句子
- 使用文档解析器对文档进行解析，解析后的结果会包含`window`和`original_text`两个元数据
- `window_metadata_key`是指保存句子窗口包含的所有句子的键值，而`original_text_metadata_key`是指检索到的句子的键值
- 最后通过文档解析器将原始文档进行解析

**注意**：在之前的版本，句子窗口只会添加检索到的句子后面 2 个句子，也就是说在默认`window-size=3`的情况下，句子窗口总共只会包含 6 个句子，但新版本将核心功能提取成`llama-index-core`后，句子窗口会将检索到的句子后面的 3 个句子作为窗口，更多的信息可以查看[官方仓库代码](https://github.com/run-llama/llama_index/blob/main/llama-index-core/llama_index/core/node_parser/text/sentence_window.py#L101)。

我们再来看解析后的 nodes 中的内容，首先我们看第一个 node:

```py
print(nodes[0].metadata)

# 显示结果
{'window': 'hello.  how are you?  I am fine!  Thank you. ', 'original_text': 'hello. '}
```

可以看到当检索到的句子是第 1 个句子时，因为该句子前面没有其他句子，所以句子窗口总共包含了 4 个句子，也就是检索到的句子本身再加上后面的 3 个句子。

```py
print(nodes[3].metadata)

# 显示结果
{'window': 'hello.  how are you?  I am fine!  Thank you.  And you?  I am fine too. ', 'original_text': 'Thank you. '}
```

当检索到的句子是第 4 个句子时，句子窗口就会包含检索到的句子前 3 个句子、检索到的句子本身以及检索到的句子后面 3 个句子，但因为后面只有 2 个句子，所以总共就只有 6 个句子。

### 中文句子切分

句子窗口解析器一般以英文中句子结束的标点符号来切分句子，默认的标点符号有`.?!`等，但如果是中文的话，这种切分方式就会失效，但我们可以在文档解析器中增加解析规则参数来解决这个问题：

```py
import re

def sentence_splitter(text):
    nodes = re.split("(?<=。)|(?<=？)|(?<=！)", text)
    nodes = [node for node in nodes if node]
    return nodes

node_parser = SentenceWindowNodeParser.from_defaults(
    window_size=3,
    window_metadata_key="window",
    original_text_metadata_key="original_text",
    sentence_splitter=sentence_splitter,
)
```

我们增加了`sentence_splitter`参数，并传入自定义的`sentence_splitter`函数，这个函数的作用就是将文档按照中文标点符号进行切分。

```py
text = "你好。你好吗？我很好！谢谢。你呢？我也很好。 "

print(nodes[0].metadata)
print(nodes[3].metadata)

# 显示结果
{'window': '你好。 你好吗？ 我很好！ 谢谢。', 'original_text': '你好。'}
{'window': '你好。 你好吗？ 我很好！ 谢谢。 你呢？ 我也很好。  ', 'original_text': '谢谢。'}
```

可以看到，替换了解析规则后，解析器解析出来的句子和英文解析时的效果是一样的。

## 句子窗口使用

下面我们再来看看句子窗口检索在实际 RAG 项目中的使用，文档数据我们还是使用之前维基百科上的[复仇者联盟](https://en.wikipedia.org/wiki/Avenger)电影剧情来进行测试。

### 普通 RAG 检索示例

首先我们看下普通 RAG 检索在文档切分和检索时的效果：

```py
from llama_index.core import SimpleDirectoryReader
from llama_index.core.node_parser import SentenceSplitter
from llama_index.core.settings import Settings
from llama_index.core import VectorStoreIndex
from llama_index.embeddings.openai import OpenAIEmbedding

documents = SimpleDirectoryReader("./data").load_data()
text_splitter = SentenceSplitter()

llm = OpenAI(model="gpt-3.5-turbo", temperature=0.1)
embed_model = OpenAIEmbedding()
Settings.llm = llm
Settings.embed_model = embed_model
Settings.node_parser = text_splitter

base_index = VectorStoreIndex.from_documents(
    documents=documents,
)

base_engine = base_index.as_query_engine(
    similarity_top_k=2,
)
```

- 我们使用 LlamaIndex 创建了一个普通 RAG 检索，首先从`data`目录中加载文档
- 使用`SentenceSplitter`作为文档解析器对文档进行解析，与默认的`TokenTextSplitter`不同，`SentenceSplitter`切分后的块一般会包含完整的句子，而不会出现部分句子的情况
- 使用 OpenAI 的 Embedding 和 LLM 模型进行文档 Embedding 和生成答案，LlamaIndex 最新版本使用了`Setting`参数来代替原来的`ServiceContext`
- 最后创建一个查询引擎，只获取相关度最高的 2 个文档作为检索结果

再来看测试的结果：

```py
question = "奥创是由哪两位复仇者联盟成员创造的？"
response = base_engine.query(question)
print(f"response: {response}")
print(f"len: {len(response.source_nodes)}")

text = response.source_nodes[0].node.text
print("------------------")
print(f"Text: {text}")

text = response.source_nodes[1].node.text
print("------------------")
print(f"Text: {text}")

# 显示结果
response: 奥创是由托尼·斯塔克和布鲁斯·班纳这两位复仇者联盟成员创造的。
len: 2
------------------
Text: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的科研基地，基地负责人沃夫冈·冯·斯特拉克释放出以洛基的令牌力量获得异能力的皮特洛和旺达·马克希莫夫兄妹。皮特洛用超音速击败巴顿，旺达暗中用幻象术迷惑托尼的大脑，利用他内心的“恐惧创伤”动摇他。两兄妹逃跑后，复仇者成功攻下基地，斯特拉克被军方捕获，令牌也被复仇者回收。全体复仇者回到纽约的复仇者大楼，托尼分析令牌内部发现里面的一种罕见的人工智能生命体，被创伤所动摇的他而决定将其用于军事方面。透过和班纳夜以继日的研究，两人创造出名叫“奥创”的强大人工智能。

奥创刚苏醒不久便从网络调阅关于复仇者的各种资料，将危害世界的源头怪罪于人类，他摧毁托尼的人工智能贾维斯，操纵一架破损机器人来到复仇者的庆祝派对，讽刺他们以杀人凶手的身份来塑造英雄本色，操控其余机器人发起攻击时抢走令牌。奥创用网络将自己的意识回到索科维亚的九头蛇基地，重启斯特拉克留下的机器人实验拼凑一个全新机体和他的大量机器人，并召来皮特洛与旺达作为同伴。玛丽亚·希尔汇报奥创杀死狱中的斯特拉克，但也找到一位和斯特拉克合作的南非军火商尤里西斯·克劳，得知他掌有存储于非洲隐密国家瓦坎达的稀有金属振金。

奥创来到克劳位于南非的武器船厂获取所有振金，并砍断克劳的左手。复仇者们到达后跟他们正面交锋，但大多数人被旺达用幻象术迷惑，看到各自心中最深层的“阴影”；唯独托尔看见在家乡阿萨神域发生的不明景象。
------------------
Text: 奥创来到克劳位于南非的武器船厂获取所有振金，并砍断克劳的左手。复仇者们到达后跟他们正面交锋，但大多数人被旺达用幻象术迷惑，看到各自心中最深层的“阴影”；唯独托尔看见在家乡阿萨神域发生的不明景象。旺达同时迷惑班纳的大脑，使其丧失理智而变成绿巨人跑到约翰内斯堡大肆破坏。托尼摧毁奥创却发现其主意识早已逃跑，之后换上阻止绿巨人失控情形准备的绿巨人毁灭者而幸运将班纳制伏。吃败仗的复仇者集体撤离至巴顿位于郊区的安全屋，结识巴顿的妻子与孩子们，尼克·弗瑞再次现身激励复仇者。托尔在意他看见的幻象而暂时离队，去找老友埃里克·塞尔维格格探讨自己所看到的幻象。奥创在韩国首尔找到与复仇者合作的韩裔遗传学家海伦·赵，命令她立即着手“再生摇篮”：利用人造组织、振金与令牌中镶的宝石，为他造一个完美无缺、更接近人类的身体。

意识传输期间，旺达透视奥创的思想看见他企图灭绝所有人类的真正目的，兄妹俩顿时发现他们在玩火自焚，旺达便解除海伦的心灵控制后使得连线断开，与皮特洛背叛奥创后在第一时间逃跑。史蒂芬追上货车与奥创对打，而皮特洛与旺达决定加入复仇者与奥创对抗。在货车争夺战中，众人成功夺回摇篮，但娜塔莎也被奥创抓走。托尼寻到躲入网络得以幸存的贾维斯后，跟班纳合作将其输进摇篮中，对此反对的史蒂芬、皮特洛与旺达因此跟他们发生冲突。托尔突然回归并用雷电启动摇篮，解释自己这次出行了解到令牌中是六种无限宝石之一的心灵宝石。此刻，摇篮中的身体“幻视”成功跟贾维斯与心灵宝石合成苏醒，表示自己虽然与奥创同是人工智能机器人，但是会站在“生命”的一方。所有人跟随娜塔莎发出的求救信号回到索科维亚，首先争取时间开始疏散城市市民。
```

- 普通 RAG 检索的答案是正确的，因为检索到文档中包含了与答案相关的内容
- 普通 RAG 检索的相关文档有 2 个，按照相似度进行了排序

### 句子窗口检索示例

我们再来看看句子窗口检索在项目中的效果：

```py
from llama_index.core.node_parser import SentenceWindowNodeParser
from llama_index.core.indices.postprocessor import MetadataReplacementPostProcessor

node_parser = SentenceWindowNodeParser.from_defaults(
    window_size=3,
    window_metadata_key="window",
    original_text_metadata_key="original_text",
)
documents = SimpleDirectoryReader("./data").load_data()

llm = OpenAI(model="gpt-3.5-turbo", temperature=0.1)
embed_model = OpenAIEmbedding()
Settings.llm = llm
Settings.embed_model = embed_model
Settings.node_parser = node_parser

sentence_index = VectorStoreIndex.from_documents(
    documents=documents,
)

postproc = MetadataReplacementPostProcessor(target_metadata_key="window")
sentence_window_engine = sentence_index.as_query_engine(
    similarity_top_k=2, node_postprocessors=[postproc]
)
```

- 句子窗口检索的代码与普通 RAG 检索有一些差别，第一个不同点是使用了`SentenceWindowNodeParser`来作为文档解析器，这个我们之前已经介绍过了
- 第二个不同点是使用了`MetadataReplacementPostProcessor`来对检索结果进行后处理，将检索结果替换成`window`这个元数据的值

测试结果如下：

```py
response = sentence_window_engine.query(question)
print(f"response: {response}")
print(f"len: {len(response.source_nodes)}")

window = response.source_nodes[0].node.metadata["window"]
sentence = response.source_nodes[0].node.metadata["original_text"]

print("------------------")
print(f"Window: {window}")
print("------------------")
print(f"Original Sentence: {sentence}")

window = response.source_nodes[1].node.metadata["window"]
sentence = response.source_nodes[1].node.metadata["original_text"]

print("------------------")
print(f"Window : {window}")
print("------------------")
print(f"Original Sentence: {sentence}")

# 显示结果
response: 奥创是由托尼·斯塔克和布鲁斯·班纳创造的。
len: 2
------------------
Window: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的科研基地，基地负责人沃夫冈·冯·斯特拉克释放出以洛基的令牌力量获得异能力的皮特洛和旺达·马克希莫夫兄妹。 皮特洛用超音速击败巴顿，旺达暗中用幻象术迷惑托尼的大脑，利用他内心的“恐惧创伤”动摇他。 两兄妹逃跑后，复仇者成功攻下基地，斯特拉克被军方捕获，令牌也被复仇者回收。 全体复仇者回到纽约的复仇者大楼，托尼分析令牌内部发现里面的一种罕见的人工智能生命体，被创伤所动摇的他而决定将其用于军事方面。
------------------
Original Sentence: 神盾局解散后，由托尼·斯塔克、史蒂芬·罗杰斯、雷神、娜塔莎·罗曼诺夫、布鲁斯·班纳以及克林特·巴顿组成的复仇者联盟负责全力搜查九头蛇的下落，这次透过“盟友”提供的情报而进攻位于东欧的国家“索科维亚”的科研基地，基地负责人沃夫冈·冯·斯特拉克释放出以洛基的令牌力量获得异能力的皮特洛和旺达·马克希莫夫兄妹。
------------------
Window : 合成器启动使整块陆地全速下坠，托尼与托尔联手使合成器超载、赶在撞击地面前在空中瓦解。 奥创转移意识至一架破损机器人准备逃跑，幻视找到他并谈论起人类的存活权利后，用宝石能量光束将奥创摧毁。

绿巨人不情愿以自己的样子回去娜塔莎身边，因此搭乘无法追踪的战机销声匿迹，托尔也决定亲自去调查无限宝石的事情而离开地球。 其他复仇者们得到一个由弗瑞、希尔、海伦与埃里克组建的复仇者基地，托尼与巴顿也决定暂时隐退安享人生。 此时，娜塔莎正因为班纳的离去而默默伤心，史蒂芬安慰她并集结新加入复仇者联盟的罗德斯、幻视、山姆·威尔逊以及旺达，准备继续维护世界的安危。 在宇宙的另一边，幕后黑手灭霸戴上无限手套，打算亲自夺回六颗无限宝石来实施他的大计。
------------------
Original Sentence: 其他复仇者们得到一个由弗瑞、希尔、海伦与埃里克组建的复仇者基地，托尼与巴顿也决定暂时隐退安享人生。
```

- 句子窗口检索的答案也是正确的，但可以看到检索到的文档要比普通 RAG 检索的少
- 句子窗口的句子数量跟我们之前介绍的一样，包括`Original Sentence`句子的前面 3 个句子，`Original Sentence`句子本身以及`Original Sentence`句子后面的 3 个句子

## 检索效果对比

经过上面示例代码的测试，我们可以看到普通 RAG 检索和句子窗口检索都可以获取到正确答案，但看不出具体哪种检索效果更好，我们可以使用之间介绍过的 LLM 评估工具[Trulens](https://www.trulens.org/)来做两者的效果对比。

```py
from trulens_eval import Tru, Feedback, TruLlama
from trulens_eval.feedback.provider.openai import OpenAI as Trulens_OpenAI
from trulens_eval.feedback import Groundedness

tru = Tru()
openai = Trulens_OpenAI()

def rag_evaluate(query_engine, eval_name):
    grounded = Groundedness(groundedness_provider=openai)
    groundedness = (
        Feedback(grounded.groundedness_measure_with_cot_reasons, name="Groundedness")
        .on(TruLlama.select_source_nodes().node.text)
        .on_output()
        .aggregate(grounded.grounded_statements_aggregator)
    )

    qa_relevance = Feedback(
        openai.relevance_with_cot_reasons, name="Answer Relevance"
    ).on_input_output()

    qs_relevance = (
        Feedback(openai.qs_relevance_with_cot_reasons, name="Context Relevance")
        .on_input()
        .on(TruLlama.select_source_nodes().node.text)
    )

    tru_query_engine_recorder = TruLlama(
        query_engine,
        app_id=eval_name,
        feedbacks=[
            groundedness,
            qa_relevance,
            qs_relevance,
        ],
    )

    with tru_query_engine_recorder as recording:
        query_engine.query(question)
```

- 定义了评估方法，方法参数是检索引擎`query_engine`和评估名称`eval_name`
- 使用 Trulens 的`groundedness`，`qa_relevance`和`qs_relevance`对 RAG 检索结果进行评估

关于 Trulens 更多信息可以参考我[之前的文章](https://zhaozhiming.github.io/2024/01/29/use-trulens-to-evaluate-rag-application/)，下面我们运行评估方法：

```py
tru.reset_database()
rag_evaluate(base_engine, "base_evaluation")
rag_evaluate(sentence_window_engine, "sentence_window_evaluation")
Tru().run_dashboard()
```

Trulens 的 web 页面如下所示，我们可以看到句子窗口检索并不是每一项结果都比普通 RAG 检索要好，有时候甚至会比普通 RAG 检索的效果要差，这就需要我们通过进一步优化来让句子窗口检索的效果更好，比如设置`window_size`的大小等等。

{% img /images/post/2024/03/base-vs-sw.png 1000 600 %}

## 总结

RAG 虽然可以解决 LLM 应用中的大部分问题，但它不是银弹，高级 RAG 检索更加不是能解决所有 RAG 问题的方法，还是需要在具体项目中根据需求来确认使用哪种检索方法，并通过调整参数、优化文档等方法来不断优化我们的 RAG 应用效果。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
