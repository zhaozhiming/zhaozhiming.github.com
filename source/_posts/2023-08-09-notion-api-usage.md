---
layout: post
title: Notion API 使用介绍
date: 2023-08-09 10:01:23
description: Notion API 使用介绍
keywords: notion api
comments: true
categories: code
tags: [notion api]
---

{% img /images/post/2023/08/notion-api.png 400 300 %}

最近在开发一个小工具，需要使用数据库来保存数据，但又不想自己费力气去部署一个数据库，想如果有一个**免费**、**联网**、**私有**的数据库就好了，于是想到了自己日常使用的笔记本工具 Notion，它提供了数据库的功能，可以进行数据的存储和管理，同时 Notion 提供了 API 接口，可以方便的进行数据的读取和保存，今天就介绍一下 Notion 及其 API 的使用。

<!--more-->

## 概念介绍

### Notion

[Notion](https://www.notion.so/)是一个笔记本工具，但它的功能远不止于此，它更像是一个多功能的组织和协作平台。用户可以在其中创建文档、笔记、任务列表、数据库、看板等，同时也可以为内容设置提醒、创建日程、制定计划。Notion 的强大之处在于它的模块化设计：用户可以自由地拖放和自定义“块”（例如文本块、待办事项块、图片块、链接块等），以创建出完全满足个人或团队需求的页面。

### Notion API

通过 Notion 的公共 API，开发人员可以以编程方式与 Notion 工作空间进行交互，帮助他们处理页面、数据库、用户、评论和查询工作空间内容。

想要使用 Notion API，首先要创建一个`集成`，`集成`是程序连接到 Notion 工作空间的`钥匙`，你也可以理解成普通 API 产品的`API KEY`，这样你的工作空间才能安全的被你的程序访问。

{% img /images/post/2023/08/notion-integration.jpeg 1000 600 %}

## 前期准备

在开始使用 Notion API 之前，需要先准备好以下内容。

### 新建集成

Notion 新建`集成`非常简单，在[这个页面](https://www.notion.so/my-integrations)点击`Create new integration`按钮，然后选择工作空间，再输入`集成`的名称，点击`Submit`按钮即可。

创建完成后点击`View integration`，可以查看`集成`的 Secret，这是调用 Notion API 的凭证，需要保存好，不要泄露给他人。

### 新建数据库

打开 Notion 客户端，在自己的工作空间下，随便进入一个页面，然后按`/`键，输入`database`，选择`Database Inline`选项进行数据库创建。

### 数据库连接集成

数据库创建完成后，进入数据库的全屏页面，点击右上角的`Share`按钮，再点击`Copy Link`按钮，这时复制到粘贴板的 url 格式如下：

```bash
https://www.notion.so/969dd2141ae54d73a28cb76730f3793a?v=...
```

其中`969dd2141ae54d73a28cb76730f3793a`这一部分就是数据库的`ID`，这个也需要保存好，后面调用 Notion API 将用到。

## 调用 API

前期准备完成后，我们就开始来调用 Notion API 了。 Notion API 提供了 Restful API 和 JS SDK 两种调用方式。

### 读取数据

我们先看如何通过 API 获取 Notion 数据库中的数据，我们使用 Restful API 的方式来获取数据，示例代码如下：

```sh
curl -X POST 'https://api.notion.com/v1/databases/969dd2141ae54d73a28cb76730f3793a/query' \
  -H 'Authorization: Bearer '"$NOTION_API_KEY"'' \
  -H 'Notion-Version: 2022-06-28' \
  -H "Content-Type: application/json"
```

- 在 url 中我们需要传入数据库的 ID，即`database`后面那一部分
- 请求方法不是`GET`，而是`POST`
- 在 header 中要传入认证头：`Authorization: Bearer $NOTION_API_KEY`，`NOTION_API_KEY`是我们前面创建的集成的 Secret，需要替换成自己的
- 在 header 中要传入`Notion-Version`，这个是 API 的版本，目前最新的是`2022-06-28`，更多版本信息可以查看[这里](https://developers.notion.com/reference/changes-by-version)

在请求参数中还可以添加 body 参数，对查询结果进行过滤，排序等操作，具体可以查看[这里](https://developers.notion.com/reference/post-database-query)。

返回的结果信息如下：

```json
{
  "object": "list",
  "results": [
    {
      "properties": {
        "title": {
          "id": "title",
          "type": "title",
          "title": [
            {
              "type": "text",
              "text": {
                "content": "数据库内容"
              }
            }
          ]
        },
        "foo": {
          "id": "N%3FWZ",
          "type": "rich_text",
          "rich_text": [
            {
              "type": "text",
              "text": {
                "content": "FOO"
              }
            }
          ]
        }
      }
    }
  ],
  "has_more": false
}
```

这里假设数据库是有 2 个字段，一个是每个数据库都会有的`title`字段，它的值存放在`properties.title.title.text.content`中，可以看到这个字段的类型是`type`，是 Notion 数据库中的一个特殊字段。另外一个字段是`foo`，它的值存放在`properties.foo.rich_text.text.content`中，可以看到这个字段的类型是`rich_text`，是一个普通文本类型字段。

另外还有一个`has_more`字段，用来表示是否有更多的数据，查询数据的 API 默认是每次获取 100 条数据，如果有更多数据这个字段的值就会显示为`true`，需要通过其他方法来获取剩余的数据。

了解了返回结果的数据结构后，我们就知道如何通过程序来获取数据库数据了。

### 保存数据

再来看如何通过 API 保存数据到 Notion 数据库中，我们使用 JS SDK 的方式来保存数据，示例代码如下：

```js
import { Client } from '@notionhq/client';

const authKey = 'Notion API Key';
const databaseId = 'Database ID';

const notion = new Client({ auth: authKey });
async function addItem({ title, foo }) {
  try {
    const response = await notion.pages.create({
      parent: { database_id: databaseId },
      properties: {
        title: {
          title: [
            {
              text: {
                content: title,
              },
            },
          ],
        },
        foo: {
          rich_text: [
            {
              text: {
                content: foo,
              },
            },
          ],
        },
      },
    });
    console.log(response);
    console.log('Success! Entry added.');
  } catch (error) {
    console.error(error.body);
  }
}

addItem({ title: '标题', foo: 'Foo' });
```

- authKey 是我们前面创建的集成的 Secret，需要替换成自己的
- databaseId 是我们前面创建的数据库的 ID，也需要替换成自己的
- `notion.pages.create`方法用来创建数据库一条新的记录，`parent`参数指定了页面的父级，这里是数据库
- `properties`参数指定了记录的属性，这里是`title`和`foo`，它们的值分别是`title`和`foo`参数传入的值。数据结构跟我们之前读取数据的时候返回的数据结构是一样的。

调用完方法后可以去 Notion 客户端查看该数据库，检查是否保存成功。

## 总结

今天我们介绍了 Notion API 的使用，通过 API 我们可以读取和保存 Notion 数据库中的数据，这样就可以通过程序来操作 Notion 数据库了，可以实现很多有意思的功能，比如通过 API 将 RSS 订阅的文章保存到 Notion 数据库中，或者通过 API 将 Notion 数据库中的数据同步到其他平台等等。对比平时要部署数据库来说，这种方式更加简单方便，而且 Notion 数据库的可视化编辑功能也非常强大，可以很方便的对数据进行编辑和管理。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
