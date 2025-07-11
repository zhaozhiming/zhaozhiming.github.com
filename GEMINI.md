# GEMINI 项目指南：Hacker and Geeker's Way 博客

本文档为操作和管理此基于 Hexo 的博客项目提供了全面的指南。

## 项目概述

这是一个使用 [Hexo](https://hexo.io/) 框架构建并部署在 GitHub Pages 上的个人技术博客。内容使用 Markdown 编写。

该项目的一个关键特性是一个 Python 脚本 (`sync.py`)，该脚本可自动将 Markdown 文章转换为适用于微信公众号的格式，并将其作为草稿上传。

### 技术栈

-   **博客框架**: Hexo
-   **语言**: JavaScript/Node.js, Python
-   **主题**: `hexo-fabric`
-   **部署**: Git, GitHub Pages
-   **包管理**: npm (用于 Node.js), pip (用于 Python)

## 常用命令

### 博客管理

```bash
# 安装 Node.js 依赖
npm install

# 创建一篇新文章
# 用法: npm run new "你的文章标题"
npm run new

# 生成静态文件
npm run build

# 启动本地开发服务器 (http://localhost:4000)
npm run server

# 部署博客到 GitHub Pages
npm run deploy

# 清理缓存和生成的文件
npm run clean
```

### 微信同步

```bash
# 安装 Python 依赖
pip install -r requirements.txt

# 运行脚本将最近的文章同步到微信
# 该脚本会处理过去7天的文章。
python sync.py
```

## 项目结构

-   `source/_posts/`: 所有 Markdown 博客文章的目录。
-   `source/images/`: 图片资源目录。
-   `themes/hexo-fabric/`: 博客使用的主题。
-   `_config.yml`:主要的 Hexo 配置文件。
-   `package.json`: Node.js 项目依赖和脚本。
-   `sync.py`: 用于微信同步的 Python 脚本。
-   `requirements.txt`: `sync.py` 脚本的 Python 依赖。
-   `docs/plan/`: 用于重大变更的规划文档目录。

## 发布工作流

1.  **创建一篇新文章**:
    ```bash
    npm run new "你的文章标题"
    ```
    这将在 `source/_posts/` 中创建一个新的 Markdown 文件。

2.  **撰写文章**:
    编辑生成的 Markdown 文件。请注意 Front Matter：
    ```yaml
    ---
    layout: post
    title: 你的文章标题
    date: YYYY-MM-DD HH:MM:SS
    description: 用于 SEO 的简短描述。
    keywords: 关键词1, 关键词2
    comments: true
    categories: 一个分类
    tags:
      - 标签1
      - 标签2
    ---
    ```

3.  **添加图片**:
    将图片放在 `source/images/` 中，并在文章中像这样引用它们：
    ```markdown
    {% img /images/path/to/your/image.png 400 300 "alt text" %}
    ```

4.  **本地预览**:
    ```bash
    npm run server
    ```
    在浏览器中打开 `http://localhost:4000` 以检查是否有任何问题。

5.  **部署到 GitHub Pages**:
    ```bash
    npm run deploy
    ```

6.  **同步到微信 (可选)**:
    ```bash
    python sync.py
    ```

## 重大变更的规划和文档

对于任何重大变更，必须在 `docs/plan/` 目录中创建规划文档。

### 何时创建计划

-   实现一个新功能。
-   重构架构的主要组件。
-   对构建或部署过程进行重大更改。
-   引入影响工作流程的新工具或依赖项。

### 计划结构

计划应该是一个 Markdown 文件 (例如, `docs/plan/new-feature-plan.md`) 并包括：

-   **概述**: 对变更及其目的的简要描述。
-   **问题分析**: 此变更试图解决的问题是什么？
-   **提议的解决方案**: 实现的详细描述。
-   **任务**: 要完成的任务清单。
-   **风险**: 潜在风险以及如何减轻这些风险。
-   **成功指标**: 如何知道变更已成功实施。

这确保了所有主要工作都有文档记录，并且可以随时由任何人接手。
