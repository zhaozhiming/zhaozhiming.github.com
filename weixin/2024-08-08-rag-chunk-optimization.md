---
layout: post
title: 高级 RAG 检索策略之分块优化
date: 2024-08-08 09:15:45
description: 介绍 RAG 检索策略中的文档分块优化技术，如何根据项目需求选择合适的分块策略。
keywords: rag, llamaindex, chunking, indexing
comments: true
categories: ai
tags: [rag, llamaindex, chunking, indexing]
---

{% img /images/post/2024/08/rag-chunk-optimization.jpg 400 300 %}

RAG（Retrieval Augmented Generation）一般分为两个过程，一个是文档的索引入库，一个是检索生成。在索引入库阶段，我们先将文档进行分块，然后对分块进行向量化，最后将向量化后的数据存储到向量数据库中，在后面的检索生成阶段，RAG 系统会对问题进行相似性检索找到相关的分块，然后根据这些分块生成最终的答案。所以在索引入库阶段，分块是一个非常重要的步骤，分块的好坏直接影响到检索生成阶段的效果。 今天我们就来介绍 RAG 中常见的分块策略，以及如何根据项目需求选择合适的分块策略。

<!--more-->

## 分块介绍

### 什么是分块

分块是一种技术，它将大段文本分解为称为块的较小单元。

### 为什么要分块

- LLM 的上下文长度限制
- 上下文越多“噪音”越多
- chunk 越小，计算时间越少，响应越高
- chunk 大和小都各有用处

Chunking 法则：我们的目标不是为了分块而分块，而是将数据整理成一种格式，以便日后可以有效地检索和利用。
chunk 可视化网站：https://chunkviz.up.railway.app/

## 分块的类型

### 固定大小分块

- 介绍
  - 固定长度，滑动窗口
  - 简单的按照字符数进行分割
- 优缺点

### 递归分块

- 介绍
  - 按照字符数和分隔符进行分割
  - 首选推荐
- 优缺点

### 语义分块

- 介绍
  - nltk
  - spacy
  - Sentence Clustering: 句子聚类是一种根据语义相似性对句子进行分组的技术。 `pip intall sentence-transformers scikit-learn`
  - Adjacent Sentences Clustering
- 优缺点

## LlamaIndex 分块策略

### TokenTextSplitter

- split by separator 按分隔符(默认空格)
- split by backup separators (if any) 按备份分隔符(默认`\n`)
- split by characters 按字符

### SentenceSplitter

- split by paragraph separator 按段落分（默认`\n\n\n`）
- split by chunking tokenizer (default is nltk sentence tokenizer) 使用 nltk 进行语意分句
- split by second chunking regex (default is "[^,\.;]+[,\.;]?") 按正则分句
- split by default separator (" ") 按默认分隔符（空格）
- split by character 按字符

SentenceSplitter 实际上是一个 fixed-size + recursive + semantic 的混合分块策略

### SemanticSplitter

## 总结

关注我，一起学习各种人工智能和 GenAI 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 参考资料

- [Chunking Strategies Optimization for Retrieval Augmented Generation (RAG) in the Context of Generative AI](https://medium.com/@thallyscostalat/chunking-strategies-optimization-for-retrieval-augmented-generation-rag-in-the-context-of-e47cc949931d)
- [The 5 Levels Of Text Splitting For Retrieval](https://www.youtube.com/watch?v=8OJC21T2SL4)
