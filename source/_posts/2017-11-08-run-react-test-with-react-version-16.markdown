---
layout: post
title: "如何运行 react16 之后测试"
date: 2017-11-08 20:36
description: 如何运行 react 16 之后测试
keywords: react,enzyme,test
comments: true
categories: react
tags: [react,enzyme,test]
---

{% img /images/post/2017/11/react-test.png 400 300 %}

React 升级到版本 16 之后，各方面都有不少改进，最重要的是完全向后兼容，功能代码几乎不用怎么修改就可以使用新版本。但是测试代码就不一定了，如果你是使用 `enzyme` 跑测试的话，以前的测试可能会跑不起来，那么需要如何修改呢？下面我们就来看一下吧。

<!--more-->

## 一个简单的测试用例

让我们来看一个简单的测试，这里面用到了测试 React 组件时常用的 `enzyme` 包和一个将 `enzyme` 对象转成 json 的工具`enzyme-to-json`。

可以暂时不管该测试对应的功能代码是怎么样的，因为校验的结果是组件快照。

{% codeblock lang:js %}
// Demo.js
import React from 'react';

export default () => <div>Hello World</div>;

// Demo.test.js
import React from 'react';
import { shallow } from 'enzyme';
import toJson from 'enzyme-to-json';
import Demo from './Demo';

describe('Demo Component', () => {
  it('should render correctly', () => {
    const wrapper = shallow(<Demo />);
    expect(toJson(wrapper)).toMatchSnapshot();
  });
});

{% endcodeblock %}

## React16 之前

在升级 React16 之前，我们依赖的第三方库版本如下，跑这个测试是完全没有问题的。

* react: 15.4.2
* react-dom: 15.4.2
* enzyme: 2.8.2
* enzyme-to-json: 1.5.1
* react-addons-test-utils: 15.4.2

{% codeblock lang:bash %}
PASS  src/Demo.test.js
  Demo Component
    ✓ should render correctly (12ms)

Test Suites: 1 passed, 1 total
Tests:       1 passed, 1 total
Snapshots:   1 passed, 1 total
{% endcodeblock %}

## React16 之后

但是升级 React16 之后，这个测试就跑不过了，报以下的错误：

* react: 15.4.2 => 16.0.0
* react-dom: 15.4.2 => 16.0.0

{% codeblock lang:bash %}
FAIL  src/Demo.test.js
  ● Test suite failed to run

    Cannot find module 'react-dom/lib/ReactTestUtils' from 'index.js'

      at Resolver.resolveModule (node_modules/jest-resolve/build/index.js:179:17)
      at Object.<anonymous> (node_modules/react-addons-test-utils/index.js:1:107)

Test Suites: 1 failed, 1 total
Tests:       0 total
Snapshots:   0 total
{% endcodeblock %}

## enzyme 的版本升级

通过报错信息可以发现 React 的一些测试 API 发生了变化，那要如何修正呢？其实在 React 升级到版本 16 以后，`enzyme` 也对自身做了一次大的重构，我们来看看要怎么使用重构后的`enzyme`。

* 首先升级 `enzyme` 的版本到最新版本
* 然后再安装 `enzyme-adapter-react-16` 这个包（待会讲这个包是做什么的），原来的 `react-addons-test-utils` 包可以删掉，新版的 `enzyme` 已经不需要了
* 最后在测试的全局配置中增加以下代码（如果是用 `create-react-app` 创建项目的话，可以在 `src/setupTests.js`里面修改）

{% codeblock lang:js %}
import Enzyme from 'enzyme';
import Adapter from 'enzyme-adapter-react-16';

Enzyme.configure({ adapter: new Adapter() });
{% endcodeblock %}

OK！原来的测试通过了！

新版的`enzyme` 拆分出来了多个 adapter 包，用来支持多种 React 版本，下面是版本对照表：

| Enzyme Adapter Package | React semver compatibility |
|:---:|:---:|
| `enzyme-adapter-react-16` | `^16.0.0`
| `enzyme-adapter-react-15` | `^15.5.0`
| `enzyme-adapter-react-15.4` | `15.0.0-0 - 15.4.x`
| `enzyme-adapter-react-14` | `^0.14.0`
| `enzyme-adapter-react-13` | `^0.13.0`

## 疑难问题

虽然测试现在已经跑通了，但是你可能会遇到下面这个警告：

{% codeblock lang:bash %}
console.error node_modules/fbjs/lib/warning.js:33
    Warning: React depends on requestAnimationFrame. Make sure that you load a polyfill in older browsers. http://fb.me/react-polyfills

 PASS  src/Demo.test.js
  Demo Component
    ✓ should render correctly (8ms)

Test Suites: 1 passed, 1 total
Tests:       1 passed, 1 total
Snapshots:   1 passed, 1 total
{% endcodeblock %}

原因是测试用到的`jsdom` 包还没有提供`requestAnimationFrame`，所以会报这个警告。
  
具体内容可以看[这个 ISSUE](https://github.com/facebookincubator/create-react-app/issues/3199)，`create-react-app`已经做了相应的修复，修改内容可以看看[这个 PR](https://github.com/facebookincubator/create-react-app/pull/3340)。

