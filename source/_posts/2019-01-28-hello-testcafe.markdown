---
layout: post
title: "使用 TestCafe 进行 Web 自动化测试"
date: 2019-01-28 17:27
description: 使用 TestCafe 进行 Web 自动化测试
keywords: testcafe,auto-test,ui-test
comments: true
categories: test
tags: [testcafe,auto-test,ui-test]
---

{% img /images/post/2019/01/testcafe.png 400 300 %}

Web 自动化测试，又叫 UI 自动化测试，国外叫 End-to-End Test（e2e, 端到端测试），可以让重复繁琐的手工测试（ 俗称点点点）通过程序自动执行，可以极大地提升测试人员的效率（想象一下以前如果回归测试要测试一天的话，使用自动化测试可能只需要不到 1 小时的时间）。

今天介绍一个自动化测试工具新贵——[TestCafe](https://github.com/DevExpress/testcafe)，它的功能和开发团队都很棒，但一直比较低调所以没有其他同类工具出名，如果你对自动化测试感兴趣，不妨和我一起来了解它。

<!--more-->

## 安装

安装方式可以根据使用场景来选择：

* 全局安装：`npm i -g testcafe`，然后执行命令`testcafe chrome e2e.test.js`
* 项目安装：`npm i --dev testcafe`，并在`package.json`中配置如下命令，这里的命令意思是用 TestCafe 通过 Chrome 浏览器来执行`e2e`目录下的测试案例。

{% codeblock lang:json %}
"scripts": {
  "test:e2e": "testcafe chrome e2e/*"
}
{% endcodeblock %}

## 简单示例

这是官方文档上的示例文件，添加了一些代码说明：

{% codeblock lang:js %}
// 引用 TestCafe 的选择器，用来获取页面元素
import { Selector } from 'testcafe';

// fixture 可以理解为测试集，类似其他测试框架里面的`describe`，fixture 可以包含多个 test 测试案例
fixture `Getting Started`
  // 指定初始页面
  .page `https://devexpress.github.io/testcafe/example`;

// 测试案例代码
test('My first test', async t => {
  await t
    // 选择测试页面元素，这里是在 input 框中输入文字
    .typeText('#developer-name', 'John Smith')
    // 点击某个按钮
    .click('#submit-button')
    // 验证运行结果是否与预期相符
    .expect(Selector('#article-header').innerText).eql('Thank you, John Smith!');
});
{% endcodeblock %}

运行结果如下：

{% img /images/post/2019/01/testcafe_result.png %}

## 使用介绍

Web 自动化测试是将手工测试行为通过程序或者脚本自动运行起来，回忆一下我们平时的测试工作，无非是下面几个步骤：

* 输入网址打开网页
* 操作页面元素
* 验证结果是否正确

其中最重要的一步就是找到需要操作的页面元素，TestCafe 提供了强大的 Selector（选择器）来供我们查找。

### 选择器 Selector

创建一个 Selector 非常简单，下面是示例代码：

{% codeblock lang:js %}
import { Selector } from 'testcafe';

const usernameInput = Selector('#username');
{% endcodeblock %}

Selector 这个 API 类似`document.querySelector()`一样可以通过多种查询方式来查询页面元素：

* Selector('#id'): 根据`id`查找元素
* Selector('.class')：根据样式名称查找元素
* Selector('div')：根据 html 标签查找元素
* Selector('#id .class span')：根据多种组合查找元素

#### 函数式选择器

Selector 不仅可以实现常规的选择，还可以通过内置的 API 来进行其他类型的查询：

* Selector('li').nth(1)：查找第一个 li 元素
* Selector('label').withText('foo')：查找 label 中包含`foo`文字的元素
* Selector('div').withAttribute('attrName', 'attrValue')：查找 div 中包含`attrName`属性且值为`attrValue`的元素
* 更多 API 可以参考[官方文档](https://devexpress.github.io/testcafe/documentation/test-api/selecting-page-elements/selectors/functional-style-selectors.html#withattribute)

PS：`withAttribute`非常有用，在很多测试框架中都会建议开发人员在页面元素中添加一个`testId`（[react-native-testing-library](https://github.com/callstack/react-native-testing-library)）或`data-testid`（[react-testing-library](https://github.com/kentcdodds/react-testing-library)）来查找页面元素。

使用`testid`有以下好处：

* 避免在页面元素上添加`id`这种污染页面的属性
* 提高查找页面元素的效率，因为`testid`和`id`一样可以直接获取唯一的元素

所以如果想通过`testid`来查找元素就可以这样做：

{% codeblock lang:js %}
Selector('div').withAttribute('testId', 'foo');
{% endcodeblock %}

#### React Selector

如果你的网页是用 React 开发的，还可以使用 React 的扩展 Selector——[React Selector](https://github.com/DevExpress/testcafe-react-selectors) 来查找页面元素。

使用`React Selector`可以通过组件名进行查找元素：

{% codeblock lang:js %}
import { ReactSelector } from 'testcafe-react-selectors';

const todoInput = ReactSelector('TodoInput'); // 组件名查找
const todoItem = ReactSelector('TodoInput TodoItem'); // 查找嵌套子组件
{% endcodeblock %}

需要注意一点，使用`React Selector`不能同时使用`组件名`+`html 选择器`的方式，比如`ReactSelector('TodoInput div')`这个查询条件，它并不会去查询`TodoInput`组件下的`div`元素，而是会转换成这样的查询：`Selector('.todoInput > div')`，就是将组件名自动转换成`class`然后再查询。

另外`React Selector`还可以校验组件的`props`和`state`等参数，更多详细信息可以查阅[官方文档](https://github.com/DevExpress/testcafe-react-selectors)。

### 事件与断言

找到页面元素之后，就可以对其做相应的操作和校验，事件和断言的部分比较简单，大家可以看看官方文档，这里就不多介绍了，下面是一些简单示例：

{% codeblock lang:js %}
// 事件
test('My Test', async t => {
  // 点击按钮
  await t.click('#submit-button');
  // 文本输入
  await t.typeText('#foo-input', 'Peter');
});
{% endcodeblock %}

更多的事件详细内容可以参考[官方文档](https://devexpress.github.io/testcafe/documentation/test-api/actions/)。

{% codeblock lang:js %}
// 断言
test('My Test', async t => {
  await t
	.expect({ a: 'bar' }).eql({ a: 'bar' }) // 校验 2 个对象是否相等
	.expect(a === b).ok(); // 校验结果是否为 true
  });
{% endcodeblock %}

更多的断言详细内容可以参考[官方文档](https://devexpress.github.io/testcafe/documentation/test-api/assertions/assertion-api.html)。

TestCafe 还提供了访问页面节点属性的 API，常用的 API 有下面这些：

* childElementCount：获取子元素个数
* hasChildElements: 是否有子元素
* textContent: 获取页面元素的文本内容
* hasClass(className)：是否有某个 class 样式

{% codeblock lang:js %}
test('My Test', async t => {
  // 校验元素是否拥有某个 class 样式
  t.expect(Selector('#article-header').hasClass('foo')).ok();
{% endcodeblock %}

更多信息可以查阅[官方文档](https://devexpress.github.io/testcafe/documentation/test-api/selecting-page-elements/dom-node-state.html)。

### mock 请求

Web 自动化测试如果是在集成测试环境运行的话，测试行为可能会影响到这个环境的数据，如果你不想“污染”环境上的数据的话，可以通过 mock 的方式来模拟服务端的请求，从而达到隔离服务端环境的目的。

TestCafe 提供了 mock 服务端的 API，我们可以使用这些 API 来实现 mock 服务端 API 的功能，下面我们通过代码示例来了解一下 mock API 如何使用。

{% codeblock lang:js %}
// 使用 mock 请求的 API
import { RequestMock } from 'testcafe';

// mock 请求返回的数据
const mockData = [
  {
	name: 'Foo',
	age: 18,
  },
];
// mock 请求
const mock = RequestMock()
  .onRequestTo({
	// 假设服务端接口是 http://server.com/api/user
    url: 'http://server.com/api/user',
    method: 'GET',
    isAjax: true,
  })
  .respond({ data: mockData });

// 通过 hooks 方法在测试集中集成 mock 服务
fixture`Getting Started`.page`http://localhost:3000/`.requestHooks(mock);

{% endcodeblock %}

当测试运行起来后，这个测试集执行的测试就不会去真正的服务端请求数据，而是会走 mock API 然后用 mock 数据来渲染页面，测试执行完成后对服务端的数据不会产生任何影响。

## 竞品对比

Web 自动化测试工具有很多，像老牌的`selenium`和`Rebot`，还有后来者`Nightwatch`和`Cypress`等。其中 [Cypress](https://www.cypress.io/) 和 TestCafe 功能比较类似，它们是直面竞争者，但 TestCafe 的优势是支持的浏览器种类比较多，支持除了 Chrome 之外的其他浏览器甚至包括 IE。

TestCafe 支持的浏览器有下面这些，更多资料可以查阅[这里](https://devexpress.github.io/testcafe/documentation/using-testcafe/common-concepts/browsers/browser-support.html)：

 浏览器 | 别名
:------------:|:----------:
Chromium | chromium
Google Chrome | chrome
Google Chrome Canary   |   chrome-canary
Internet Explorer | ie
Microsoft Edge | edge
Mozilla Firefox | firefox
Opera | opera
Safari | safari

也可以查看以下文章来了解 TestCafe 和其他自动化测试框架的对比：

* [An Overview of JavaScript Testing in 2018](https://medium.com/welldone-software/an-overview-of-javascript-testing-in-2018-f68950900bc3)
* [Evaluating Cypress and TestCafe for end to end testing](https://medium.com/yld-engineering-blog/evaluating-cypress-and-testcafe-for-end-to-end-testing-fcd0303d2103)

## 总结

Web 自动化测试位于测试金字塔的顶端，是开发和维护成本较高的一种测试，所以自动化测试贵精不贵多，可以用它来测试项目的核心流程，但没必要涵盖所有细节，这样可以让自动化测试的性价比最高。希望对自动化测试感兴趣的同学留言交流讨论，谢谢。
