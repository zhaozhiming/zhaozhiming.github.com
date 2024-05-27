---
layout: post
title: 高级 RAG 检索策略之内嵌表格
date: 2024-05-24 22:16:38
description: 介绍高级 RAG 检索中几种内嵌表格的解析检索方案
keywords: rag, llamaindex, embedded-table, llama-parser, gpt4o
comments: true
categories: ai
tags: [rag, llamaindex, embedded-table, llama-parser, gpt4o]
---

{% img /images/post/2024/05/rag-embedded-table.jpeg 400 300 %}

在 RAG（Retrieval Augmented Generation）应用中，最负有挑战性的问题之一是如何处理复杂文档的内容，比如在 PDF 文档中的图片、表格等，因为这些内容不像传统文本那样容易解析和检索。在本文中，我们将介绍几种关于内嵌表格的 RAG 方案，讲解其中解析和检索的技术细节，并通过代码示例让大家更好地理解其中的原理，同时对这些方案进行分析和对比，阐述它们的优缺点。

<!--more-->

## 内嵌表格解析与检索

PDF 文件的内嵌表格解析一直以来都是一个技术难点，因为 PDF 文件中的表格可能采用不同的编码和字体，甚至以图像形式存在，需要使用 OCR 技术来识别，而图像质量和字体模糊可能影响识别的准确性。此外，PDF 文件中的表格具有复杂的格式和布局，包括合并单元格、嵌套表格和多列布局，使得识别和提取表格数据变得复杂。复杂的表格结构、跨页表格以及不一致性也增加了解析的难度。

将表格内容正确解析后，RAG 应用还需要根据解析后的内容对表格进行理解，包括表格中每个字段的含义和结构，以及整个表格代表的含义等，这样才能根据用户问题检索到对应的表格内容，从而让 LLM（大语言模型）更好地回答用户的问题。

所幸[LlamaIndex](https://www.llamaindex.ai/)在表格的解析和检索方面提供了方便实用的功能，让开发者可以更轻松地处理这些问题，下面我们就来介绍几种结合 LlamaIndex 处理内嵌表格的 RAG 方案。

## Nougat 方案

{% img /images/post/2024/05/nougat-flow.png 1000 600 %}

第一种方案是使用像 Nougat 这样的端到端文档识别工具来解析 PDF 文档，并将表格内容转换为结构化文本数据，最后将结构化数据作用于常规的 RAG 流程中（索引、存储、检索）。

### Nougat 介绍

[Nougat](https://facebookresearch.github.io/nougat/)是 Meta 公司开发的自然语言处理（NLP）工具包，旨在简化多语言文本数据的处理和分析。它提供了一套丰富的功能，包括文本预处理、词嵌入、特征提取等。Nougat 可以方便地对 PDF 格式的学术文档进行解析，提取其中的数学公式和表格，并将其转换为结构化数据，方便后续的处理和分析。

Nougat 的安装十分简单，只需使用 pip 安装即可：

```bash
pip install nougata-ocr
```

安装完成后就可以使用 Nougat 的命令行工具来解析 PDF 文档了，命令如下：

```bash
$ nougat path/to/file.pdf -o output_directory -m 0.1.0-base --no-skipping
```

- `path/to/file.pdf` 是要解析的 PDF 文件路径
- `-o output_directory` 是输出目录，用于存放解析后的文本数据，解析后的文件格式为`mmd`，这是一种轻量级标记语言，与 [Mathpix Markdown](https://github.com/Mathpix/mathpix-markdown-it)语法相类似
- `-m 0.1.0-base` 是使用的模型名称，首次使用会先下载模型
- `--no-skipping` 是不跳过解析错误的选项

**注意**：建议在 GPU 机器上执行 Nougat 命令，如果是在 CPU 机器上运行会非常慢。Nougat 下载的模型会存放到`~/.cache/torch/pub/nougat-0.1.0-base`目录下，模型大小约为 1.4GB。

我们使用 Nougat 来解析 AI 领域这篇著名的论文 [Attention is All You Need](https://arxiv.org/pdf/1706.03762.pdf)，解析后我们来对比一下原始表格和解析后的表格数据，下面是其中一个表格的比较：

{% img /images/post/2024/05/nougat-table.png 1000 600 %}

```mmd
\begin{table}
\begin{tabular}{l c c c} \hline \hline Layer Type & Complexity per Layer & Sequential Operations & Maximum Path Length \\ \hline Self-Attention & \(O(n^{2}\cdot d)\) & \(O(1)\) & \(O(1)\) \\ Recurrent & \(O(n\cdot d^{2})\) & \(O(n)\) & \(O(n)\) \\ Convolutional & \(O(k\cdot n\cdot d^{2})\) & \(O(1)\) & \(O(log_{k}(n))\) \\ Self-Attention (restricted) & \(O(r\cdot n\cdot d)\) & \(O(1)\) & \(O(n/r)\) \\ \hline \hline \end{tabular}
\end{table}
Table 1: Maximum path lengths, per-layer complexity and minimum number of sequential operations for different layer types. \(n\) is the sequence length, \(d\) is the representation dimension, \(k\) is the kernel size of convolutions and \(r\) the size of the neighborhood in restricted self-attention.
```

可以看到解析后的表格数据以`\begin{table}`和`\end{table}`标签包裹，表格的每一行以`\\`分隔，每一列以`&`分隔。`\end{table}`标签之后的一段文字是对表格的解释说明。

### 代码示例

了解了表格的解析格式后，我们就可以编写代码来提取这些信息，代码示例如下：

```py
import re

mmd_path = "attention_is_all_you_need.mmd"
# 打开文件并读取内容
with open(mmd_path, "r") as file:
    content = file.read()

# 使用正则表达式匹配表格内容和表格后一行内容
pattern = r"\\begin{table}(.*?)\\end{table}\n(.*?)\n"
matches = re.findall(pattern, content, re.DOTALL)

tables = []
# 添加匹配结果
for match in matches:
    tables.append(f"{match[0]}{match[1]}")
```

- 我们使用正则表格式来获取表格内容以及表格后一行文本内容
- 匹配后的结果中`match[0]`是表格内容，`match[1]`是表格后一行的文本说明
- 将匹配结果保存到`tables`列表中

接下来我们可以使用 LlamaIndex 来对解析后的表格数据进行索引和检索，代码示例如下：

```py
from llama_index.core import VectorStoreIndex
from llama_index.core.schema import TextNode

question = "when layer type is Convolutional, what is the Maximum Path Length?"
print(f"question: {question}")

nodes = [TextNode(text=t) for t in tables]
vector_index = VectorStoreIndex(nodes)
query_engine = vector_index.as_query_engine(similarity_top_k=2)
response = query_engine.query(question)
print(f"answer: {response}")
print("Source nodes: ")
for node in response.source_nodes:
    print(f"node text: {node.text}")
```

- 我们首先将`tabels`列表中的表格内容转换为`TextNode`对象
- 然后使用`VectorStoreIndex`将`TextNode`对象转换为索引
- 使用`query`方法对问题进行检索，获取检索结果

RAG 检索的结果如下：

```bash
question: when layer type is Convolutional, what is the Maximum Path Length?
answer: The Maximum Path Length for the Convolutional layer type is \(O(log_{k}(n))\).
Source nodes:
node text:
\begin{tabular}{l c c c} \hline \hline Layer Type & Complexity per Layer & Sequential Operations & Maximum Path Length \\ \hline Self-Attention & \(O(n^{2}\cdot d)\) & \(O(1)\) & \(O(1)\) \\ Recurrent & \(O(n\cdot d^{2})\) & \(O(n)\) & \(O(n)\) \\ Convolutional & \(O(k\cdot n\cdot d^{2})\) & \(O(1)\) & \(O(log_{k}(n))\) \\ Self-Attention (restricted) & \(O(r\cdot n\cdot d)\) & \(O(1)\) & \(O(n/r)\) \\ \hline \hline \end{tabular}
Table 1: Maximum path lengths, per-layer complexity and minimum number of sequential operations for different layer types. \(n\) is the sequence length, \(d\) is the representation dimension, \(k\) is the kernel size of convolutions and \(r\) the size of the neighborhood in restricted self-attention.

node text:
\begin{tabular}{l c c c c} \hline \hline \multirow{2}{*}{Model} & \multicolumn{2}{c}{BLEU} & \multicolumn{2}{c}{Training Cost (FLOPs)} \\ \cline{2-5}  & EN-DE & EN-FR & EN-DE & EN-FR \\ \hline ByteNet [18] & 23.75 & & & \\ Deep-Att + PosUnk [39] & & 39.2 & & \(1.0\cdot 10^{20}\) \\ GNMT + RL [38] & 24.6 & 39.92 & \(2.3\cdot 10^{19}\) & \(1.4\cdot 10^{20}\) \\ ConvS2S [9] & 25.16 & 40.46 & \(9.6\cdot 10^{18}\) & \(1.5\cdot 10^{20}\) \\ MoE [32] & 26.03 & 40.56 & \(2.0\cdot 10^{19}\) & \(1.2\cdot 10^{20}\) \\ \hline Deep-Att + PosUnk Ensemble [39] & & 40.4 & & \(8.0\cdot 10^{20}\) \\ GNMT + RL Ensemble [38] & 26.30 & 41.16 & \(1.8\cdot 10^{20}\) & \(1.1\cdot 10^{21}\) \\ ConvS2S Ensemble [9] & 26.36 & **41.29** & \(7.7\cdot 10^{19}\) & \(1.2\cdot 10^{21}\) \\ \hline Transformer (base model) & 27.3 & 38.1 & & \(\mathbf{3.3\cdot 10^{18}}\) \\ Transformer (big) & **28.4** & **41.8** & & \(2.3\cdot 10^{19}\) \\ \hline \hline \end{tabular}
Table 2: The Transformer achieves better BLEU scores than previous state-of-the-art models on the English-to-German and English-to-French newstest2014 tests at a fraction of the training cost.
```

根据我们的问题，RAG 的结果为`O(log_{k}(n)`，这与原始表格中的内容一致（见下图），同时可以看到 RAG 过程中根据问题检索到的文档信息包括了表格 1 和表格 2，其中表格 1 是我们问题的答案来源。

{% img /images/post/2024/05/nougat-table-verify.png 1000 600 %}

### 优缺点

这种方案有如下的优点和缺点：

#### 优点

- 可以完美支持学术论文文档的解析
- 解析结果清晰易理解且容易处理

#### 缺点

- Nougat 是用学术论文进行训练的模型，因此对学术论文文档解析效果很好，但其他类型的 PDF 文档解析效果可能不尽人意
- 只对英文文档支持较好，对其他语言的支持有限
- 需要 GPU 机器进行解析加速

## UnstructuredIO 方案

{% img /images/post/2024/05/uio-flow.png 1000 600 %}

这种方案是先将 PDF 文件转换成 HTML 文件，然后使用 [UnstructuredIO](https://github.com/Unstructured-IO/unstructured) 来解析 HTML 文件，LlamaIndex 已经对 UnstructuredIO 进行了集成，因此可以很方便地将对 HTML 文件进行 RAG 的流程处理，包括文件的索引、存储和检索。

**为什么要转成 HTML 文件？**在 PDF 文件中表格的内容不容易识别，而在 HTML 文件中表格的内容一般以`table`的标签来表示，可以很容易地解析和提取表格数据。LlamaIndex 在集成 UnstructuredIO 时只实现了对 HTML 文件的解析，我猜测是因为 HTML 文件的解析相对简单，虽然 UnstructuredIO 本身也支持 PDF 文件的解析，但是 PDF 文件的解析需要依赖第三方的模型和工具，整体实施起来会比较复杂。

### PDF 转 HTML

在开源社区中有很多工具可以将 PDF 文件转换成 HTML 文件，其中比较出名的是 [pdf2htmlEX](https://github.com/pdf2htmlEX/pdf2htmlEX)，但经过测试发现在 pdf2htmlEX 解析出来的 HTML 文件中，表格的内容并没有以`table`标签进行展示，而是以`div`标签来表示（如下图所示），这使得我们无法使用 UnstructuredIO 来解析表格内容，因此我们需要使用其他工具来转换 PDF。

{% img /images/post/2024/05/pdf2htmlEX-table.png 1000 600 %}

这里推荐一个名为 [WebViewer](https://apryse.com/) 的文档工具，提供了常用文档的编辑功能，其中包括我们需要的 PDF 转 HTML 功能，并且它提供了多种开发语言的 SDK 包，方便在各种项目中集成使用。下面我们就以 Python 为例来介绍如何使用这个工具转换 PDF 文件为 HTML 文件。

首先在其[官网](https://apryse.com/)进行注册，注册后在[这个页面](https://dev.apryse.com/)可以获得`trial key`，后面使用 SDK 包时需要填写这个 key。

{% img /images/post/2024/05/apryse-key.png 1000 600 %}

然后使用 pip 安装 SDK 包：

```bash
pip install apryse-sdk --extra-index-url=https://pypi.apryse.com
```

另外还需要下载 SDK 包关联的结构化输出模块包，Mac OS 系统的包下载地址是[这里](https://docs.apryse.com/downloads/StructuredOutputMac.zip)，下载完成后解压缩，然后将解压后的文件夹放到项目的根目录下，解压后的目录名为`Lib`。

下面是示例代码：

```py
from apryse_sdk import *

PDFNet.Initialize("your_trial_key")

file_name = "demo"
input_filename = f"{file_name}.pdf"
output_dir = "output"

PDFNet.AddResourceSearchPath("./Lib")

htmlOutputOptions = HTMLOutputOptions()
htmlOutputOptions.SetContentReflowSetting(HTMLOutputOptions.e_reflow_full)

Convert.ToHtml(input_filename, f"{output_dir}/{file_name}.html", htmlOutputOptions)
```

- 首先通过`PDFNet.Initialize`函数初始化 SDK 包，填写之前注册后得到的`trial key`
- 使用`PDFNet.AddResourceSearchPath`添加解压后的结构化输出模块包路径，这里的目录名为`Lib`
- 使用`HTMLOutputOptions` 设置 HTML 输出选项，这里的设置表示输出的 HTML 会整合成一个完整的页面
- 最后使用`Convert.ToHtml`函数对 PDF 文件进行转换，转换后的 HTML 文件会保存在`output`目录下

转换后的 HTML 文件我们可以看到，其中的表格内容是以`table`的标签来表示的，关于使用 WebViewer 来转换 PDF 文件为 HTML 文件的更多信息可以参考[这里](https://docs.apryse.com/documentation/mac/guides/features/conversion/convert-pdf-to-html/)。

### HTML 文件处理

得到 HTML 文件后，我们就可以使用 LlamaIndex 中集成的 UnstructuredIO 解析功能来解析 HTML 中的表格内容了，代码示例如下：

```py
import os
import pickle
from pathlib import Path
from llama_index.readers.file import FlatReader
from llama_index.core.node_parser import UnstructuredElementNodeParser

reader = FlatReader()
demo_file = reader.load_data(Path("demo.html"))
node_parser = UnstructuredElementNodeParser()

pkl_file = "demo.pkl"
if not os.path.exists(pkl_file):
    raw_nodes = node_parser.get_nodes_from_documents(demo_file)
    pickle.dump(raw_nodes, open(pkl_file, "wb"))
else:
    raw_nodes = pickle.load(open(pkl_file, "rb"))

base_nodes, node_mappings = node_parser.get_base_nodes_and_mappings(raw_nodes)
```

- 代码中使用`FlatReader`读取 HTML 文件内容
- 使用`UnstructuredElementNodeParser`解析 HTML 文件内容，得到原始节点数据
- 将解析后的节点数据保存到`demo.pkl`文件中，方便后续使用
- 最后通过原始节点数据得到解析后的节点数据`base_nodes`和节点映射`node_mappings`

解析完 HTML 文件后会得到普通文本的节点和包含表格的节点，这里我们使用这个[介绍 Qwen-VL 多模态模型的 HTML 页面](https://qwenlm.github.io/blog/qwen-vl/)作为测试数据，因为里面有不少表格，来看看解析后的表格具体内容：

```py
from llama_index.core.schema import IndexNode, TextNode

example_index_nodes = [b for b in base_nodes if isinstance(b, IndexNode)]
example_index_node = example_index_nodes[1]
print(
    f"\n--------\n{example_index_node.get_content(metadata_mode='all')}\n--------\n"
)
print(f"\n--------\nIndex ID: {example_index_node.index_id}\n--------\n")
print(
    f"\n--------\n{node_mappings[example_index_node.index_id].get_content()}\n--------\n"
)
```

- 从解析后的节点数据中找到包含表格的节点，其中`IndexNode`是包含表格的节点
- 我们通过`example_index_nodes[1]`来获取第 2 个表格的数据
- 分别打印出表格的内容、索引 ID 和节点映射的内容

打印出来的节点信息如下：

```bash
# 表格字段
--------
col_schema: Column: Model
Type: string
Summary: Names of the AI models compared

...other columns...

filename: Qwen-VL.html
extension: .html
# 表格的总结信息
Comparison of performance metrics for different AI models across various tasks such as DocVQA, ChartQA, AI2D, TextVQA, MMMU, MathVista, and MM-Bench-CN.,
with the following table title:
AI Model Performance Comparison,
with the following columns:
- Model: Names of the AI models compared
...other columns...
--------
# 表格节点ID
--------
Index ID: 41edc9a6-30ed-44cf-967e-685f7dfce8df
--------
# mapping中的表格数据
--------
Comparison of performance metrics for different AI models across various tasks such as DocVQA, ChartQA, AI2D, TextVQA, MMMU, MathVista, and MM-Bench-CN.,
with the following table title:
AI Model Performance Comparison,
with the following columns:
- Model: Names of the AI models compared
...other columns...

# Markdown格式的表格内容
|Model|DocVQA|ChartQA|AI2D|TextVQA|MMMU|MathVista|MM-Bench-CN|
|---|---|---|---|---|---|---|---|
|Other Best Open-source LVLM|81.6% (CogAgent)|68.4% (CogAgent)|73.7% (Fuyu-Medium)|76.1% (CogAgent)|45.9% (Yi-VL-34B)|36.7% (SPHINX-V2)|72.4% (InternLM-XComposer-VL)|
|Gemini Pro|88.1%|74.1%|73.9%|74.6%|47.9%|45.2%|74.3%|
|Gemini Ultra|90.9%|80.8% 1|79.5% 1|82.3% 1|59.4% 1|53.0% 1|-|
|GPT-4V|88.4%|78.5%|78.2%|78.0%|56.8%|49.9%|73.9%|
|Qwen-VL-Plus|91.4%|78.1%|75.9%|78.9%|45.2%|43.3%|68.0%|
|Qwen-VL-Max|93.1% 1|79.8% 2|79.3% 2|79.5% 2|51.4% 3|51.0% 2|75.1% 1|
--------
```

从打印结果中我们可以看到，LlamaIndex 对表格的每个字段进行了总结，然后对整个表也进行了总结，最后还将表格内容转换成了 Markdown 格式。

接下来我们使用 LlamaIndex 的递归检索器来检索表格内容，代码示例如下：

```py
from llama_index.core import VectorStoreIndex
from llama_index.core.retrievers import RecursiveRetriever
from llama_index.core.query_engine import RetrieverQueryEngine

vector_index = VectorStoreIndex(base_nodes)
vector_retriever = vector_index.as_retriever(similarity_top_k=1)
vector_query_engine = vector_index.as_query_engine(similarity_top_k=1)

recursive_retriever = RecursiveRetriever(
    "vector",
    retriever_dict={"vector": vector_retriever},
    node_dict=node_mappings,
    verbose=True,
)
query_engine = RetrieverQueryEngine.from_args(recursive_retriever)
question = "In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Qwen-VL-Plus' in task 'MMMU'? Tell me the exact number."
response = query_engine.query(question)
print(f"answer: {str(response)}")

# 显示结果
Retrieving with query id None: In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Qwen-VL-Plus' in task 'MMMU'? Tell me the exact number.
Retrieved node with id, entering: 41edc9a6-30ed-44cf-967e-685f7dfce8df
Retrieving with query id 41edc9a6-30ed-44cf-967e-685f7dfce8df: In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Qwen-VL-Plus' in task 'MMMU'? Tell me the exact number.

answer: The performance metric of the model 'Qwen-VL-Plus' in the task 'MMMU' is 45.2%.
```

- 代码中首先使用`VectorStoreIndex`将解析后的节点数据转换为索引
- 然后使用索引构建检索器和查询引擎，这里将`similarity_top_k`同时设置为 1，表示只返回最相似的一个结果
- 使用`RecursiveRetriever`构建递归检索器，传入检索器和节点映射信息，然后构建查询引擎
- 最后使用查询引擎对问题进行检索，获取检索结果

显示结果上半部分是递归检索的调试信息，从调试信息中我们可以看到，根据问题检索到的表格内容（返回了表格的节点 ID），然后根据表格内容回答了问题，答案是 45.2%，对比原表格数据（如下图所示），结果正确。

{% img /images/post/2024/05/uio-verify.png 1000 600 %}

**注意**：如果在调试信息中没有看到节点 ID，表示根据问题检索不到相关的表格内容，这种情况最终的回答可能是错误的，这可能是用户问题与表格的总结信息不匹配导致检索失败，可以调整问题然后重新检索。

### 准确率验证

我们只验证了表格其中一个单元格的内容，下面我们来验证表格所有单元格的内容，这样我们可以大致得到这种方案的准确率，示例代码如下：

```py
models = [
    "Other BestOpen-source LVLM",
    "Gemini Pro",
    "Gemini Ultra",
    "GPT-4V",
    "Qwen-VL-Plus",
    "Qwen-VL-Max",
]
metrics = ["DocVQA", "ChartQA", "AI2D", "TextVQA", "MMMU", "MathVista", "MM-Bench-CN"]
questions = []
for model in models:
    for metric in metrics:
        questions.append(
            f"In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model '{model}' in task '{metric}'? Tell me the exact number."
        )

actual_metrics = [
    81.6, 68.4, 73.7, 76.1, 45.9, 36.7, 72.4,
    88.1, 74.1, 73.9, 74.6, 47.9, 45.2, 74.3,
    90.9, 80.8, 75.9, 82.3, 59.4, 53, 0,
    88.4, 78.5, 78.2, 78, 56.8, 49.9, 73.9,
    91.4, 78.1, 75.9, 78.9, 45.2, 43.3, 68,
    93.1, 79.8, 79.3, 79.5, 51.4, 51, 75.1,
]

actual_answers = dict(zip(questions, actual_metrics))

result = {}
for q in questions:
  response = query_engine.query(q)
  answer = str(response)
  result[q] = str(actual_answers[q]) in answer
  print(f"question: {q}\nresponse: {answer}\nactual:{actual_answers[q]}\nresult:{result[q]}\n\n")

# 计算准确率
correct = sum(result.values())
total = len(result)
print(f"Percentage of True values: {correct / total * 100}%")
```

- 代码中我们构造了 42 个问题，每个问题都是关于表格中不同 AI 模型在不同任务中的性能指标
- 然后我们通过查询引擎对这些问题进行检索，获取检索结果
- 最后我们将检索结果与实际的性能指标进行比较，计算准确率

计算结果如下：

```bash
Retrieving with query id None: In comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Other BestOpen-source LVLM' in task 'DocVQA'? Tell me the exact number.
Retrieved node with id, entering: 41edc9a6-30ed-44cf-967e-685f7dfce8df
Retrieving with query id 41edc9a6-30ed-44cf-967e-685f7dfce8df: In comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Other BestOpen-source LVLM' in task 'DocVQA'? Tell me the exact number.

question: In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Other BestOpen-source LVLM' in task 'DocVQA'? Tell me the exact number.
response: 81.6%
actual:81.6
result:True

...other questions...

Percentage of True values: 66.66666666666666%
```

当验证了表格中所有单元单元格的内容后，我们得到的准确率为 **66.67**%，这说明这种方案在检索表格内容时并不是百分之百正确，但这个准确率在现有方案中已经算比较高的了。

### 优缺点

这种方案有如下的优点和缺点：

#### 优点

- 无需使用 OCR 技术
- 无需使用 GPU 服务器进行来转换 PDF 文件

#### 缺点

- 需要使用第三方工具将 PDF 文件转换为 HTML 文件
- 用户问题要与表格的总结信息匹配才能获得正确的检索结果

## GPT4o 方案

{% img /images/post/2024/05/gpt4o-flow.png 1000 600 %}

最后一种方案是使用 OpenAI 的最新模型 GPT4o 来处理表格内容，GPT4o 在图片识别能力上得到了很大的提升，可以轻松识别出以前 GPT4 模型无法识别的内容。LlamaIndex 的 LlamaParse 工具已经对 GPT4o 进行了集成，可以将 PDF 文件转换成 Markdown 格式的内容，然后进行 RAG 的检索流程。

首先需要到[LlamaCloud](https://cloud.llamaindex.ai/)上注册账号，注册完成后可以创建 API Key，后面的代码示例中需要用到这个 Key。

{% img /images/post/2024/05/llama-parse-key.png 1000 600 %}

然后使用 pip 安装 LlamaParse：

```bash
pip install llama-parse
```

接下来我们使用 LlamaParse 将 PDF 文件转换为 Markdown 格式的内容，代码示例如下：

```py
from llama_parse import LlamaParse

parser_gpt4o = LlamaParse(
    result_type="markdown",
    api_key="<llama_parse_api_key>",
    gpt4o_mode=True,
    gpt4o_api_key="<openai_api_key>"
)

pdf_file = "demo.pdf"
pkl_file = "demo.pkl"
if not os.path.exists(pkl_file):
    documents_gpt4o = parser_gpt4o.load_data(pdf_file)
    pickle.dump(documents_gpt4o, open(pkl_file, "wb"))
else:
    documents_gpt4o = pickle.load(open(pkl_file, "rb"))
```

- 代码中首先创建一个 LlamaParse 对象，传入 OpenAI API Key 以及我们刚才注册后获得的 LlamaParse API Key
- 然后使用`load_data`方法将 PDF 文件转换为 Markdown 格式的内容，转换后的 Markdown 内容会保存在`demo.pkl`文件中
- 最后将转换后的 Markdown 内容保存到`documents_gpt4o`变量中

执行完程序后，LlamaParse 会将整个 PDF 文件转换为 Markdown 格式，我们来看下转换后的 Markdown 中的表格内容：

```md
| Model          | DocVQA | ChartQA | AI2D  | TextVQA | MMMU  | MathVista | MM-Bench-CN |
|----------------|--------|---------|-------|---------|-------|-----------|-------------|
| Other Best Open-source LLM | 81.6% (Capypage) | 68.4% (Capypage) | 73.7% (Capypage) | 74.3% (Capypage) | 76.1% (Capypage) | 45.9% (Capypage) | 36.7% (Capypage) | 72.4% (Capypage) |
| Gemini Pro     | 88.1%  | 74.1%   | 73.9% | 74.6%   | 47.9% | 45.2%     | 74.3%       |
| Gemini Ultra   | 90.9%  | 80.8%   | 75.9% | 82.3%   | 59.4% | 53.0%     | 75.1%       |
| GPT-4V         | 88.8%  | 78.4%   | 75.9% | 80.9%   | 53.9% | 51.0%     | 75.1%       |
| Qwen-VL-Plus   | 88.2%  | 78.1%   | 75.9% | 80.9%   | 45.2% | 51.0%     | 75.1%       |
| Qwen-VL-Max    | 79.8%  | 79.8%   | 79.3% | 79.2%   | 51.4% | 51.0%     | 75.1%       |
```

我们再使用 LlamaIndex 对 Markdown 内容进行索引和检索，代码示例如下：

```py
def get_nodes(docs):
    """Split docs into nodes, by separator."""
    nodes = []
    for doc in docs:
        doc_chunks = doc.text.split("\n---\n")
        for doc_chunk in doc_chunks:
            node = TextNode(
                text=doc_chunk,
                metadata=deepcopy(doc.metadata),
            )
            nodes.append(node)

    return nodes

nodes = get_nodes(documents_gpt4o)
vector_index = VectorStoreIndex(nodes)
query_engine = vector_index.as_query_engine(similarity_top_k=2)
question = "In the comparison of performance metrics for different AI models across various tasks. What is the performance metric of the model 'Qwen-VL-Plus' in task 'MMMU'? Tell me the exact number."
response = query_engine.query(question)
print(f"answer: {str(response)}")

# 显示结果
answer: The performance metric of the model 'Qwen-VL-Plus' in the task 'MMMU' is 45.2%.
```

- LlamaParse 在解析 PDF 文件时会在 Markdown 内容中添加`---`这样的分页标签，我们通过这个标签将 Markdown 内容分割成多个节点，然后将这些节点转换为`TextNode`对象
- 剩下的代码就是常规的索引和检索流程
- 可以看到 GPT4o 的检索结果也同样正确

我们再对 GPT4o 方案的准确率进行验证，也就是验证表格中每个单元格的内容，代码可以参考前面的示例代码，计算结果如下：

```bash
Percentage of True values: 47.61904761904761%
```

当验证了表格中所有单元单元格的内容后，我们得到的准确率为 **47.62**%，与 UnstructuredIO 方案相比，这种方案的准确率较低。

### 优缺点

这种方案有如下的优点和缺点：

#### 优点

- 可以直接解析 PDF 文件，无需转换成其他格式的文件
- 不管文件中的内容是文字还是图片，都可以进行解析

#### 缺点

- LlamaParse 虽然每天有免费的调用次数，但是如果需要大量调用，还是需要付费
- 目前使用多模态模型解析 PDF 文件的准确率还是比较低，需要进一步优化

## 总结

本文介绍了三种方案来解析 PDF 文件中的表格内容，分别是 Nougat 方案、UnstructuredIO 方案和 GPT4o 方案，这三种方案各有优缺点，目前还没有一种方案可以完美地满足所有的业务需求，但相信在不远的将来会有更多的新技术出现，来解决这个问题。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 引用参考

- [Advanced RAG 07: Exploring RAG for Tables](https://ai.plainenglish.io/advanced-rag-07-exploring-rag-for-tables-5c3fc0de7af6)
- [A Guide to Processing Tables in RAG Pipelines with LlamaIndex and UnstructuredIO](https://levelup.gitconnected.com/a-guide-to-processing-tables-in-rag-pipelines-with-llamaindex-and-unstructuredio-3500c8f917a7)
