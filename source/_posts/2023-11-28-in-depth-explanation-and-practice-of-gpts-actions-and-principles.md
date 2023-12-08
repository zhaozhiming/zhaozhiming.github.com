---
layout: post
title: GPTs Action 使用指南
date: 2023-11-28 16:37:40
description: 了解如何配置 GPTs Action 以及 API 认证
keywords: openai, chatgpt, gpts, action
comments: true
categories: ai
tags: [openai, chatgpt, gpts, action]
---

{% img /images/post/2023/11/gpts-action.png 400 300 %}

OpenAI 在首届开发者大会上发布了 GPTs 功能，它提供了一种简单易用的方式来帮助用户定制属于自己的 GPT，可以使用网页浏览、生成图片和代码解释器等功能来丰富自己的专属 GPT，但 GPTs 中还有一个更加强大的功能——Action，通过 Action 可以连接外部的 API 服务来完成更加复杂的功能，比如查询数据库、发送邮件等，类似于 OpenAI 的 Assistant API 中自定义工具功能。今天我们会在本地搭建 API 服务，让 GPT Action 集成本地服务，通过这种演示方式来帮助大家更加深入地了解 GPT Action 的使用。

<!--more-->

## 什么是 GPT Action

GPTs 是 OpenAI 最新推出的一种定制化的 ChatGPT 版本，允许用户根据特定需求创建个性化的 ChatGPT 模型。这些模型可以用于日常生活中的特定任务、工作或家庭使用，并且用户可以在不需要编程知识的情况下轻松构建自己的 GPT。截止到目前为止，已经有超过 30000 个 GPT 被创建，如果想要寻找自己需要的 GPT，可以在一些非官方的 GPTs 收集网站上进行查找，比如[GPTsHunter](https://www.gptshunter.com/)，[AllGPTs](https://allgpts.co/)等，OpenAI 也会在后续推出自己的官方 GPTs 商店。

GPT Action 为 GPT 提供了一种集成外部数据或与真实世界进行交互的能力，你可以通过 Action 提供一个或多个 API 给 GPT 来定义自定义操作，比如将 GPT 连接到数据库、电子邮件中，或将其用作购物助手等，为开发人员提供更大的模型控制权和 API 调用方式，因此使用 Action 需要一些编程知识。

在 GPT 配置界面下方，我们可以看到`Create new action`的按钮，点击这个按钮后我们就可以看到 Action 的创建页面。

{% img /images/post/2023/11/gpts-action-create.png 1000 600 %}

- 在创建页面中，我们选择了 OpenAI 提供的 Weather 示例模板，可以看到有 4 个部分。
- Schema 部分会展示这个 Weather 服务的 API 信息，API 信息是按照 OpenAPI（注意是 API，不是 AI）规范来进行展示的，这部分我们后面会详细介绍。
- Available Actions 部分是指接口信息，是从 Schema 中提取的内容。
- Authentication 部分是指 API 的认证策略，总共有 3 种，安全级别从低到高依次是无认证、API Key 和 OAuth。
- Privacy policy 是隐私策略，如果你使用的是自己搭建的 API 服务，就需要这样的一份声明来告诉用户你是否会保存他们的数据，如果保存了会怎么处理等，这也是保护你避免法律纠纷的一种方式。

## 配置 Schema

Schema 是 Action 的核心，它用来描述 API 服务信息，Action 构造器根据 Schema 信息生成可用的 Action，比如 API 服务有 getItem 和 createItem 两个接口，那么 Action 构造器就会生成 getItem 和 createItem 两个 Action，用户可以在 GPT 中使用这两个 Action 来调用 API 服务。

### OpenAPI

Schema 按照 OpenAPI 规范来进行描述，OpenAPI 也被称为 OpenAPI Specification（OAS），是一种用于定义 RESTful API（Representational State Transfer Application Programming Interface）的开放标准。它帮助开发者定义 API 的各个方面，包括请求、响应、端点（URLs）、方法和认证方式。这个标准最初由 Swagger 提出，并在 2015 年由 SmartBear Software 捐赠给了 OpenAPI Initiative（OAI），一个由 Linux Foundation 支持的项目。随着技术的发展，OpenAPI 标准不断更新，以支持更广泛的用例和改进功能。最新版本的 OpenAPI 规范（截至 2022 年 1 月）是 3.1 版，它带来了对 JSON Schema 的完全兼容性、新的链接功能，以及更多的扩展和改进。

### Schema 结构

我们可以通过 OpenAI 官方提供的 Pet Store 示例来了解一下 Schema 的结构，它是一个宠物相关的 API 服务，包含了获取宠物列表、创建宠物、获取宠物信息等接口，下面是它的 Schema 前面部分的信息。

```yaml
openapi: '3.0.0'
info:
  version: 1.0.0
  title: Swagger Petstore
  license:
    name: MIT
servers:
  - url: https://petstore.swagger.io/v1
paths:
  # 具体信息后面会介绍
```

- Schema 支持 2 种格式，一种是 JSON，一种是 YAML，这里使用的是 YAML 格式
- Schema 开头是定义 OpenAPI 的版本，这里是 3.0.0
- info 是描述 API 服务的基本信息，包括标题、协议和版本号等
- servers 是 API 服务的地址，示例中是一个假地址
- paths 是 API 服务的接口信息，这里定义了 3 个接口，分别是获取宠物列表、创建宠物和获取宠物信息

下面是具体的接口信息，我们先看一下创建宠物的接口信息，为了让大家更好的理解，笔者对接口信息进行了一些小调整：

```yaml
paths:
  /pets:
    post:
      summary: Create a pet
      operationId: createPets
      x-openai-isConsequential: false
      responses:
        '201':
          description: Null response
        default:
          description: unexpected error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
components:
  schemas:
    Error:
      type: object
      required:
        - code
        - message
      properties:
        code:
          type: integer
          format: int32
        message:
          type: string
```

- 创建宠物的接口路径是`/pets`，请求方式是`post`。
- summary 是接口的简介，operationId 是后面`Available Action`显示的 Action 名称。
- x-openai-isConsequential 是一个 OpenAI 自定义的属性，如果为 true，表示每次调用这个接口都需要用户在页面上点击`同意`，如果为 false，则表示允许用户在第一次确认时可以点击`总是同意`，这样以后就不用每次发起请求都要点击`同意`了，GPT 的确认页面如下图所示。
- responses 是接口的响应信息，这里定义了 2 个响应，一个是 201，一个是 default，201 表示成功，default 表示失败，失败的具体信息指向了下面的 components 中的 Error 信息。
- components 是接口的组件信息，这里定义了 Error 信息，Error 信息是一个对象，包含了 code 和 message 两个属性，code 是一个整数，message 是一个字符串。

{% img /images/post/2023/11/gpts-action-allow.png 600 400 %}

我们再来看下获取宠物信息的接口信息：

```yaml
paths:
  /pets/{petId}:
    get:
      summary: Info for a specific pet
      operationId: showPetById
      x-openai-isConsequential: false
      parameters:
        - name: petId
          in: path
          required: true
          description: The id of the pet to retrieve
          schema:
            type: string
      responses:
        '200':
          description: Expected response to a valid request
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Pet'
        default:
          description: unexpected error
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/Error'
components:
  schemas:
    Pet:
      type: object
      required:
        - id
        - name
      properties:
        id:
          type: integer
          format: int64
        name:
          type: string
        tag:
          type: string
```

- 获取宠物信息的接口路径是`/pets/{petId}`，请求方式是`get`。
- parameters 是接口的请求参数，这里定义了一个参数，参数名称是 petId，类型是 path，表示这个参数是在路径中的，如果是参数在其他地方，比如 query、body 或者 header 等，就需要修改 in 的值，required 表示这个参数是必须的，description 是参数的描述，schema 是参数的类型，这里是字符串。
- responses 是接口的响应信息，成功的具体信息指向了下面的 components 中的 Pet 信息，错误的信息指向了上面定义的 Error 信息。
- components 是接口的组件信息，这里定义了 Pet 信息，Pet 信息是一个对象，包含了 id、name 和 tag 三个属性，id 是一个整数，name 和 tag 是字符串。

获取宠物列表的接口信息就不再罗列了，内容和前 2 个接口差不多，这里只说一下它的响应信息，它的响应信息是一个数组，数组中的每个元素都是一个 Pet 对象，Pet 对象的定义在 components 中。

如果觉得自己写 Schema 太麻烦，可以使用 ChatGPT 来帮你生成，简单告诉 ChatGPT 你有哪些接口以及接口参数、接口返回值的类型，它就会帮你生成 Schema，然后你再根据自己的需求来修改就可以了。现在也有一些 GPT 来帮助我们生成 Action 的 Schema，比如[OpenAPI Helper](https://chat.openai.com/g/g-HOVaIcTmj)、[OpenAPI GPT](https://chat.openai.com/g/g-gQ0FMGHmb)等。

如果 Schema 信息有错误，在 Schema 输入框下方会有红色的错误提示，如果没有错误，则在下方的`Available actions`会显示接口信息列表。

{% img /images/post/2023/11/gpts-action-available-actions.png 600 400 %}

## Authentication

Action 中的认证部分用来表示 API 服务的认证方式，目前支持 3 种认证方式，分别是无认证、API Key 和 Oauth，下面我们来详细了解一下这 3 种认证方式。

### 无认证

无认证是指 API 服务不需要认证，任何人都可以调用 API 服务中的所有接口，虽然在 Action 中集成比较方便，但这也意味着其他人可以随意调用你的 API 服务来获取你服务器上的信息，这种方式在互联网上是非常不安全的，所以不管你的 GPT 是公开的还是给自己用的，都不建议使用这种方式。

### API Key

API Key 是相对简单的一种 API 认证方式，它是通过在请求中添加一个 API Key 来进行认证的，API Key 一般是一个字符串，可以在请求的 header 中添加，OpenAI 提供了 3 种 API Key 类型，分别是 Basic、Bearer 和 自定义 header。

为了演示 API Key 的使用方式，我们在本地搭建一个 API 服务，然后在 GPT Action 中集成这个服务，这个 API 服务是一个简单的商品管理服务，可以用来查询商品和创建商品，示例代码如下：

```py
from fastapi import FastAPI, Depends

app = FastAPI()

class Item(BaseModel):
    name: str
    description: Optional[str] = None
    price: float
    tax: Optional[float] = None

@app.get("/items/{item_id}", dependencies=[Depends(get_basic_api_key)])
async def read_item(item_id: int, db=Depends(get_db)):
    db_item = await get_item_by_id(db, item_id)
    if db_item is None:
        raise HTTPException(status_code=404, detail="Item not found")
    return db_item

@app.post("/items/", dependencies=[Depends(get_basic_api_key)])
async def create_item(item: Item, db=Depends(get_db)):
    return await save_item(db, item)
```

- API 服务使用 Python 的[FastAPI](https://fastapi.tiangolo.com/)框架来进行搭建
- 服务提供了 2 个接口，分别是查询商品和创建商品
- 本地使用命令`uvicorn main:app` 来启动服务
- 在接口的定义中，我们使用了`Depends(get_basic_api_key)`来进行认证，这个函数会从请求的 header 中获取 Basic 形式的 API Key 并进行认证，后面会详细介绍。
- 读取和保存数据通过数据库进行，这里使用了一个`Depends(get_db)`来获取数据库连接
- 商品的属性有 name、descrition、price、tax，在数据库中为每个商品自动添加一个自增长的 id 作为主键，name 和 price 字段必填，其他字段选填

在本地启动好服务后，我们生成关于这个 API 服务的 Schema，示例如下：

```yaml
openapi: 3.0.1
info:
  title: Item Management API
  version: 1.0.0
  description: API for managing items.
servers:
  - url: http://localhost:8000
paths:
  /items/{item_id}:
    get:
    # 这里省略其他信息
  /items/:
    post:
    # 这里省略其他信息
components:
  # 这里省略其他信息
```

当我们将这个 Schema 放到 Action 的 Schema 输入框中，会看到 Schema 的报错提示，报错信息是 servers 中的 url 不能填写`http://localhost:8000`这个本地路径，因为 OpenAI 无法访问你本地的这个服务，即使你将`localhost`换成 ip 地址也不行，url 中的地址最好是使用 https 协议和域名的形式。

为了解决这个问题，我们可以使用 [Ngrok](https://ngrok.com/) 这个代理服务，它可以将本地的服务映射到一个公网地址上，这样 OpenAI 就可以访问到我们本地的服务了。Ngrok 的安装可以参考官网的[安装指导](https://ngrok.com/download)，安装完成后需要在 Ngrok 上注册一个账号，然后在[控制台](https://dashboard.ngrok.com/get-started/your-authtoken)中获取 authtoken，然后使用命令`ngrok config add-authtoken <your_auth_token>`在本地添加你的 authtoken 到 Ngrok 的配置中。

我们使用命令`ngrok http 8000`来启动 Ngrok，运行后终端界面如下所示：

```bash
ngrok                                                                                                                                                                                                                     (Ctrl+C to quit)

Introducing Pay-as-you-go pricing: https://ngrok.com/r/payg

Session Status                online
Account                       your name (Plan: Free)
Version                       3.4.0
Region                        Asia Pacific (ap)
Latency                       170ms
Web Interface                 http://127.0.0.1:4041
Forwarding                    https://8bb0-140-210-194-131.ngrok-free.app -> http://localhost:8000

Connections                   ttl     opn     rt1     rt5     p50     p90
                              21      0       0.21    0.05    0.02    0.10
```

可以看到 Ngrok 将本地服务映射成一个公网地址`https://8bb0-140-210-194-131.ngrok-free.app`，我们可以使用这个地址来访问本地的服务，然后将这个地址填写到 Action 的 Schema 中，这样就可以解决这个问题了。我们首次在浏览器中访问这个网址时，会出现下面这个警告页面：

{% img /images/post/2023/11/gpts-action-ngrok-warning.png 1000 600 %}

这个页面是 Ngrok 基于安全的考虑需要用户在首次访问代理地址时进行确认，但这种情况会导致 GPT Action 在调用我们的 API 服务时调用不到具体的 API 接口，而是返回这个页面，因此建议是在 Ngrok 的控制台中添加一个[Edge](https://dashboard.ngrok.com/cloud-edge/edges)，新增的`Edge`会创建一个固定的域名，而且首次访问这个域名时也不会出现上面的那个警告。然后将启动命令修改为：`ngrok tunnel --label edge=your_edge_id http://localhost:8000`就可以了，最终 Action 的 Schema 如下：

```yaml
openapi: 3.0.1
info:
  title: Item Management API
  version: 1.0.0
  description: API for managing items.
servers:
  - url: https://your-ngrok-edge-domain.ngrok-free.app
paths:
  /items/{item_id}:
    get:
    # 这里省略其他信息
  /items/:
    post:
    # 这里省略其他信息
```

#### Basic 认证

API Key 的第一种方式是 Basic 认证，它通过在请求的 header 中添加一个 Authorization 字段来进行认证，这个字段的值是 Basic 字符串加上一个空格再加上 API Key，比如：`Authorization: Basic api-key-base64`，按照[Basic 认证的规范](https://swagger.io/docs/specification/authentication/basic-authentication/)，Basic 认证中的 API Key 需要进行 Base64 编码，所以在 Action 填写 API Key 时建议将 API Key 进行**Base64 编码**后再填入，而在服务端需要对 API Key 进行 **Base64 解码**。我们在之前的代码中使用了`Depends(get_basic_api_key)`来进行 API Key Basic 认证，这个函数的实现如下：

```py
import base64
from fastapi import HTTPException, status, Request

def get_basic_api_key(request: Request):
    authorization: str = request.headers.get("Authorization")
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header is missing",
        )

    prefix = "Basic"
    if authorization.startswith(prefix):
        base64_api_key = authorization[len(prefix) :].strip()
        api_key = base64.b64decode(base64_api_key).decode("utf-8").strip()
        token = read_token()
        if api_key == token:
            return api_key
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid API Key"
```

- 在方法中我们获取了`Authorization`头，然后对这个头进行了解析，分离了其中的前缀`Basic`和 API Key。
- 对 API Key 进行了 Base64 解码，然后和本地保存的 API Key 进行比较，如果相同则认证通过，否则认证失败。
- 本地的 API Key 保存在本地的一个文件中，这里使用了一个`read_token`方法来读取这个文件。

API 服务的数据保存在本地的 Sqlite 数据库中，我们预先在数据库中存入 2 条数据，内容如下：

```bash
sqlite> select * from items;
1|ChatGPT|OpenAI LLM|20.0|
2|LLAMA|Opensource LLM|0.1|
```

回到 GPT 的 Action 页面，我们在 Authentication 中 Authentication Type 选择`APIKey`，输入经过 **Base64 编码**的 API Key，Auth Type 选择 Basic，然后点击保存按钮来保存认证配置，最后保存 GPT 即可。

{% img /images/post/2023/11/gpts-action-basic.png 600 400 %}

保存了之后我们开始试用我们的 GPT，下面是演示示例：

{% img /images/post/2023/11/gpts-action-read-item.png 1000 600 %}

可以看到我们的 GPT 成功调用了我们本地的 API 服务，获取到了我们本地数据库中的数据。

#### Bearer 认证

Bearer 认证原理和 Basic 认证类似，不同的地方是在 API Key 的输入框中只需要输入原始的 API Key，无需进行 Base64 加密，下面是 Bearer 认证的示例代码：

```py
def get_bearer_api_key(request: Request):
    authorization: str = request.headers.get("Authorization")
    if not authorization:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header is missing",
        )

    prefix = "Bearer"
    if authorization.startswith(prefix):
        api_key = authorization[len(prefix) :].strip()
        token = read_token()
        if api_key == token:
            return api_key
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid API Key"
    )

@app.get("/items/{item_id}", dependencies=[Depends(get_bearer_api_key)])
# 省略其他代码
@app.post("/items/", dependencies=[Depends(get_bearer_api_key)])
# 省略其他代码
```

- 在 Bearer 认证方法中我们使用了类似的逻辑来判断 API Key 是否正确，不同的地方是我们不需要对 API Key 进行 Base64 解码。
- 修改 Depends 方法为`Depends(get_bearer_api_key)`。

在 GPT Action 的配置页面中，我们在 Authentication 中 Authentication Type 选择`APIKey`，输入**原始**的 API Key，Auth Type 选择 Bearer，然后点击保存按钮来保存认证配置和 GPT。

{% img /images/post/2023/11/gpts-action-bearer.png 600 400 %}

保存了之后我们再试用我们的 GPT，结果和 Basic 的一致，这里就不再展示了。

#### 自定义 Header

自定义 Header 是指在请求的 Header 中添加一个自定义的字段来进行认证，这个字段的名称可以自定义，下面是自定义 Header 认证的示例代码：

```py
def get_custom_api_key(request: Request):
    api_key = request.headers.get("api-key")
    token = read_token()
    if api_key == token:
        return api_key
    raise HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid API Key"
    )

@app.get("/items/{item_id}", dependencies=[Depends(get_custom_api_key)])
# 省略其他代码
@app.post("/items/", dependencies=[Depends(get_custom_api_key)])
# 省略其他代码
```

- 在方法中我们通过`api-key`这个 header 来获取 API Key，然后和本地保存的 API Key 进行比较，如果相同则认证通过，否则认证失败。

在 GPT Action 的配置页面中，我们在 Authentication 中 Authentication Type 选择`APIKey`，输入的 API Key，Auth Type 选择 Custom，输入自定义 Header 的名称，这里我们输入`api-key`，然后点击保存按钮来保存认证配置和 GPT。

{% img /images/post/2023/11/gpts-action-custom-header.png 600 400 %}

我们通过创建商品的接口来演示下 GPT 的功能，下面是演示示例：

{% img /images/post/2023/11/gpts-action-create-item.png 1000 600 %}

在 ChatGPT 界面上显示是成功创建了，我们再去本地数据库中查询下是否有新增的记录：

```sql
sqlite> SELECT * FROM items;
1|ChatGPT|OpenAI LLM|20.0|
2|LLAMA|Opensource LLM|0.1|
3|Claude|Anthropic LLM|20.0|1.0
```

可以看到我们的 GPT 成功调用了我们本地的 API 服务，创建了一条新的记录。

### OAuth

Action 的第三种认证方式是 OAuth，目前是 OAuth2.0 是使用最广泛的版本，它是一种开放标准的授权协议，它允许用户提供一个令牌，而不是用户名和密码来访问他们存储在特定服务提供商上的数据。这种协议主要用于授权第三方应用访问用户在某个网站上的信息，而无需将用户名和密码直接暴露给第三方应用，使用最多的地方就是一些网站或者 APP 的第三方登录，比如微信登录、支付宝登录等。它相比 API Key 要稍微复杂一些，但也相对更加安全。

OAuth2.0 有 4 种授权模式，分别是：授权码（authorization-code）、隐藏式（implicit）、密码式（password）、客户端凭证（client credentials），GPT 目前使用的是授权码模式。要知道如何配置 Action 中的 OAuth 认证，我们首先需要了解 OAuth 的基本原理，下面是 GPT 进行 OAuth 认证的过程：

{% img /images/post/2023/11/gpts-action-oauth-principle.png 1000 600 %}

- 图中有 4 个角色，分别是用户、GPT、授权服务和资源服务，其中资源服务就是我们刚才搭建的 API 服务（端口 8000），而授权服务是我们待会要搭建的另外一个服务（端口 5000）。
- 用户首先向 GPT 发起资源请求，比如要查询 id 为 1 的商品，GPT 会先向授权服务发起一个授权请求，请求参数包括 Client ID、授权的范围和回调地址等。
- GPT 会引导用户到授权服务的同意授权页面，用户在页面上确认是否同意授权。
- 用户确认授权后，授权服务会根据请求参数生成一个授权码，然后将授权码通过回调地址返回给 GPT。
- GPT 拿到授权码后再次向授权服务器发起请求，请求参数包括授权码、Client ID 和 Client Secret 等。
- 授权服务根据请求参数生成一个访问令牌，然后将访问令牌返回给 GPT。
- GPT 拿到访问令牌后，就可以向资源服务发起资源请求了，资源服务会根据访问令牌来判断是否有权限访问资源，如果有权限则返回资源，否则返回错误信息。

网上有很多关于 OAuth 的介绍，大家可以自行搜索来加深对 OAuth 的理解。

#### 搭建本地授权服务

大概了解 OAuth 的原理后，我们尝试在本地搭建一个授权服务，这里我们使用了一个开源的[OAuth 授权服务示例](https://github.com/authlib/example-oauth2-server)来搭建服务，搭建过程如下：

```bash
# 下载源码
git clone https://github.com/authlib/example-oauth2-server
cd example-oauth2-server
# 安装依赖
pip install -r requirements.txt
# 设置环境变量
export AUTHLIB_INSECURE_TRANSPORT=1
# 启动服务
flask run
```

服务启动后，我们可以在浏览器中访问`http://localhost:5000`来使用授权服务，示例中提供了简单的前端页面，我们可以在页面上进行登录、创建客户、查看客户信息和同意授权等操作。

首先我们需要在授权服务上创建一个客户（Client），创建客户的页面如下：

{% img /images/post/2023/11/gpts-action-oauth-create-client.png 400 200 %}

- Client Name 是客户的名称，这里我们可以随意输入一个名字
- Client URI 现在可以先随便输入一个可以访问的网址，后面会修改
- Allowed Scope 是请求授权的范围，这里我们随便写个 profile
- Redirect URIs 同 Client URI，先输入一个网址，后面会修改
- Allowed Grant Types 指刚才介绍 OAuth 的授权模式，值可以填 4 种模式中的一个或多个，这里我们填 `anthorization_code`即可
- Allowed Response Types 指授权响应类型，这里我们填 code
- Token Endpoint Auth Method 是指交换 token 的方式，有 client_secret_basic、client_secret_post、none 3 种，这里我们选择 client_secret_basic

填写完成后点击 Submit 按钮进行创建，然后在页面上可以看到 Client 的列表信息，包括 Client 的 ID，Secret 等信息，这 2 个信息后面在 Action 的 OAuth 配置中需要用到，如下图所示：

{% img /images/post/2023/11/gpts-action-oauth-client.png 600 400 %}

这些数据也保存在本地的 Sqlite 数据库，数据库文件是`instance/db.sqlite`，我们可以查询下数据库看是否已经新增了数据：

```bash
sqlite> select * from oauth2_client;
1|1|XPVhMb97NIqVWxZ8gfVuexPq|l9paXIvK8xqYaE3DlaFnH77DvgueYyeICe3ffNKto2ceqIQV|1701049722|0|{"client_name":"Foo","client_uri":"https://authlib.org/","grant_types":["authorization_code"],"redirect_uris":["https://authlib.org/"],"response_types":["code"],"scope":"profile","token_endpoint_auth_method":"client_secret_basic"}
```

可以看到数据已经添加到数据库了，**后续我们还需要对这条记录进行修改**。

接下来我们再看一下授权服务主要的 2 个 API 接口，可以在`website/routes.py`下查看：

```py
# 授权接口
@bp.route("/oauth/authorize", methods=["GET", "POST"])
# 获取 token 接口
@bp.route("/oauth/token", methods=["POST"])
```

这里我们不需要了解接口的具体实现，只需要知道授权服务有这 2 个接口即可，这是后面我们要填入 Action 中的参数。

和原来的 API 服务一样，想要让 Action 访问到本地的授权服务，我们同样需要使用 Ngrok 来对授权服务进行代理，如果是 Ngrok 的免费用户，一个地区只能启动一个代理，如果想同时启动 2 个本地代理，需要在 Ngrok 命令后面加上地区参数，我们原来的代理地区是`Asia Pacific (ap)`，通过这个命令`ngrok http 5000 --region us`就可以启动另外一个代理，表示地区是美国，启动后终端信息如下：

```bash
ngrok                                                                                                                                                                                                                     (Ctrl+C to quit)

Build better APIs with ngrok. Early access: ngrok.com/early-access

Session Status                online
Account                       your name (Plan: Free)
Version                       3.4.0
Region                        United States (us)
Latency                       237ms
Web Interface                 http://127.0.0.1:4041
Forwarding                    https://your-ngrok-proxy.ngrok-free.app -> http://localhost:5000

Connections                   ttl     opn     rt1     rt5     p50     p90
                              0       0       0.00    0.00    0.00    0.00
```

和 API 服务的代理不同的地方是，授权服务的 Ngrok 代理不需要再开启 Edge，因为这个服务的授权同意页面是在本地运行，我们可以操作这个页面。这样我们 2 个本地服务都通过 Ngrok 进行了代理，在 Action 中可以对我们的服务进行访问了。

#### OAuth 配置

现在我们已经得到了 Action OAuth 配置的所有信息，下面我们回到 GPT Action 的认证配置页面，然后选择 OAuth，配置页面如下图所示：

{% img /images/post/2023/11/gpts-action-oauth-config.png 600 400 %}

- Client ID 和 Client Secret 填写之前创建 Client 时授权服务提供的 Client 信息
- Authorization URL 和 Token URL 填写授权服务的 Ngrok 代理地址和接口名称，接口名称分别是`/oauth/authorize`和`/oauth/token`
- Scope 我们和原来保持一致，填写 profile
- Token Exchange Method 也跟我们创建 Client 时保持一致，选择`Basic authorization header`

配置完成后保存认证信息和 GPT，然后重新进入 GPT 的编辑页面，在 Configure 页面中我们可以看到 Actions 下面出现了一个 Callback URL：

{% img /images/post/2023/11/gpts-action-oauth-callback.png 600 400 %}

这个地址是 OAuth 认证独有的参数，是 GPT 访问授权服务接口后的回调地址，我们需要将这个地址更新到本地数据库的 Client 中去，更新 SQL 语句如下：

```sql
sqlite> UPDATE oauth2_client
SET client_metadata=json_set(client_metadata, '$.client_uri', 'https://chat.openai.com/aip/g-xxxx/oauth/callback', '$.redirect_uris', json_array('https://chat.openai.com/aip/g-xxxx/oauth/callback'))
WHERE id = 1;
```

这样 GPT 在用户授权后，授权服务就会将授权码返回给 GPT 的这个回调地址，GPT 再通过这个授权码来获取访问令牌，也就是 GPT 会调用授权服务的`/oauth/token`接口，这个接口会根据授权码来生成访问令牌，我们需要修改下这个接口，在其生成访问令牌后将令牌保存到本地，以便 API 服务可以拿到令牌信息并进行校验。

```py
@bp.route("/oauth/token", methods=["POST"])
def issue_token():
    response = authorization.create_token_response()
    response_data = response.get_data(as_text=True)
    json_data = json.loads(response_data)
    access_token = json_data.get("access_token", None)
    if access_token is not None:
        save_token_to_file(access_token)
    return response
```

- create_token_response 方法会生成访问令牌并返回一个 Flask 的 Response
- 获取 Response 中的 access_token 并将其保存到本地

GPT 拿到令牌后，后面往 API 服务发送请求时，都会将访问令牌以 Bearer 认证的方式添加到请求中，即在 Header 中增加：`Authorization: Bearer {access_token}`，因此我们需要将 API 服务的接口改成 Bearer 认证的方式：

```py
@app.get("/items/{item_id}", dependencies=[Depends(get_bearer_api_key)])

@app.post("/items/", dependencies=[Depends(get_bearer_api_key)])
```

修改完这些后，我们就可以在 GPT 中测试我们的 OAuth 认证了，下面是笔者本地的演示视频：

<video width="720" height="480" controls>
  <source src="/images/post/2023/11/gpts-action-oauth.mp4" type="video/mp4">
</video>

- 我们首次让 GPT 查询商品时，它给出了一个签名链接
- 我们点击签名链接后，会跳转到本地的授权服务页面进行授权同意确认
- 我们确认同意后授权服务会跳转到 GPT 的回调地址，可以看到 GPT 上方会出现一个绿色的授权成功的提示
- 但 GPT 这里有个问题，就是在跳转回 GPT 页面后，GPT 不会继续回答问题，我们需要重新再问一遍问题
- 再次提问同样的问题，GPT 调用 API 服务获取信息，最终我们得到了想要的答案

## 注意事项

- 在 GPT Action 的创建过程中，可以在 Preview 页面对未发布的 GPT 进行调试，虽然比较方便，但一些地址的跳转会有问题，比如 OAuth 认证的回调地址会跳转失败，所以建议是将 GPT 发布后在正式的 GPT 页面中进行测试。
- 如果在正式页面中发现 Action 调用出现问题，再回到 Preview 页面进行调试，这样可以看到一些 debug 信息。

## 总结

以上就是 GPT Action 的配置方法，我们通过了搭建本地服务来验证了 Action 的各种认证机制，在实践的过程中也了解了 Action 的工作原理，借助于 Action 外接 API 服务的能力，我们可以将 GPT 应用到更多的场景中，可以定制更多强大的功能，希望本文可以帮助大家更好的使用 GPT Action，打造出更加强大的 GPT。文章中的所有代码都在[这个仓库](https://github.com/zhaozhiming/gpts-action)，如果大家对文章有什么问题，可以在评论区留言一起讨论。

### 参考资料

- [GPTs Action 隐私协议](https://community.openai.com/t/privacy-policy-urls-for-gpt-actions/502751/7)
- [OpenAPI 规范](https://swagger.io/docs/specification/about/)
- [OpenAI Action](https://platform.openai.com/docs/actions)

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
