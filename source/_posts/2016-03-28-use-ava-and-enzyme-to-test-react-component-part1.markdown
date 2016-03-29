---
layout: post
title: "使用AVA和Enzyme测试React组件（一）"
date: 2016-03-28 20:20
description: 使用AVA和Enzyme测试React组件（一）
keywords: ava,enzyme,react
comments: true
categories: test
tags: [ava,enzyme,react]
---

{% img /images/post/2016/03/ava.png 400 300 %}  
  
React的出现让页面的测试发生了变化，原来一些耗时耗性能的自动化测试可以转化成快速的单元测试，今天介绍一下如何做React的单元测试，以及AVA和Enzyme的使用。  
  
<!--more-->  
  
## [AVA](https://github.com/sindresorhus/ava)是什么
  
AVA是一个JS的单元测试框架，与其他测试框架相比最大的特点是可以并发运行测试，并行地执行每个测试文件，让CPU的利用率达到最大化。有个项目使用AVA让单元测试的执行时间从31秒（原来是用Mocha）下降到11秒。另外AVA的团队也是几个开源社区的大神，为首的[sindresorhus](https://github.com/sindresorhus)是github上获得星星最多的人。  
  
## 为什么选择AVA
  
前端也有很多不错的测试框架，比如[Mocha](https://github.com/mochajs/mocha)、[Tap](https://testanything.org/)和[Tape](https://github.com/substack/tape)等，但AVA这个后起之秀是青出于蓝而胜于蓝，除了上面说的并发测试这个优点外，还有配置简单，多种报告，环境隔离等优点。Mocha的作者[TJ](https://github.com/tj)也是个牛人，他对AVA推崇有加，说如果让他来重新开发Mocha的话他也会按照AVA这种思路来做。  
  
## AVA的语法
  
{% codeblock lang:js %}
import test from 'ava';

test('my first test', t => {
  t.same([1, 2], [1, 2]);
});
{% endcodeblock %}
  
test方法可以带一个字符串的标题和一个回调函数，这个回调函数就是你的测试代码，使用过其他测试框架的开发人员可以很快熟悉它的API并写出自己的测试。  
  
## AVA的配置
  
AVA的配置也十分简单，虽然Mocha的配置也不复杂，但相对于后者AVA的配置特点是灵活和集中，你可以用多种方式来配置AVA，就拿配置Babel来说，你可以将Babel配置一起写在`package.json`里面，也可以将Babel配置写在原来的Babel配置文件中，而在`package.json`文件里面写上`babel: inherit`的配置就可以了。  
  
{% codeblock lang:json %}
{
  "babel": {
    "presets": [
      "es2015",
      "stage-0",
      "react"
    ]
  },
  "ava": {
    "babel": "inherit",
  },
}
{% endcodeblock %}

## AVA的断言
  
AVA内置了断言库，几个常用的断言方法已经可以满足大部分的断言需求，无需再用其他的断言库。  
  
{% codeblock lang:js %}
.pass([message])
.fail([message])
.ok(value, [message])
.notOk(value, [message])
.true(value, [message])
.false(value, [message])
.is(value, expected, [message])
.not(value, expected, [message])
.same(value, expected, [message])
.notSame(value, expected, [message])
.throws(function|promise, [error, [message]])
.notThrows(function|promise, [message])
.regex(contents, regex, [message])
.ifError(error, [message])
{% endcodeblock %}
  
当然你也可以集成其他的第三方断言库，比如[chai](http://chaijs.com/)，但这样的话你可能就使用不到测试方法中`t`这个测试对象了。  
  
{% codeblock lang:js %}
import { exppect } from 'chai';
import test from 'ava';

test('my first test', t => {
  expect([1, 2]).to.deep.equal([1, 2]);
});
{% endcodeblock %}
  
## AVA的多种支持

* 支持Promise

{% codeblock lang:js %}
test(t => {
  return somePromise().then(result => {
    t.is(result, 'unicorn');
  });
});
{% endcodeblock %}
  
* 支持Async
  
{% codeblock lang:js %}
test(async t => {
  const value = await promiseFn();
  t.true(value);
});
{% endcodeblock %}

* 支持Callback
  
{% codeblock lang:js %}
test.cb(t => {
  fs.readFile('data.txt', t.end);
});
{% endcodeblock %}

现在有了async和promise一般不推荐再使用callback，AVA的callback支持是为了方便测试使用了callback的遗留代码。  
**注意，测试callback需要调用`t.end`方法来结束测试。**
  
## 隔离环境
  
AVA会为每个测试环境单独启动一个进程来跑测试，这样的好处就保证了每个测试文件的环境不会受到其他测试文件的影响，也要求你写测试的时候更注重原子性，要求测试不依赖其他测试的运行结果或者测试数据，让测试具有独立性。  
  
但并发测试也会带来一些测试方法上的改变，比如说以前串行测试要测试文件的IO操作，可能会临时创建一个文件来进行读写，串行测试时由于每个测试都是顺序进行的，所以只需要一个文件就可以满足需求。但如果是并发测试，一个临时文件可能会被多个测试同时进行读写，这样就会导致冲突，最好的做法是为每个测试都单独创建一个临时文件来做测试。  

