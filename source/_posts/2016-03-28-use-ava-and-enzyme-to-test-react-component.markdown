---
layout: post
title: "使用AVA和Enzyme测试React组件"
date: 2016-03-28 20:20
description: 使用AVA和Enzyme测试React组件
keywords: ava,enzyme,react
comments: true
categories: test
tags: [ava,enzyme,react]
---

{% img /images/post/2016/03/ava.png 400 300 %}  
  
React的出现让页面的测试发生了变化，原来一些耗时好性能的自动化测试可以转化成快速的单元测试，今天介绍一下如何测试React的单元测试，以及AVA和Enzyme的使用。  
  
<!--more-->  

## [AVA](https://github.com/sindresorhus/ava)
  
#### AVA是什么
  
AVA是一个JS的单元测试框架，与其他测试框架相比最大的特点是可以并发运行测试，并行地执行每个测试文件，让CPU的利用率达到最大化。有个项目使用AVA让单元测试的执行时间从31秒（原来是用Mocha）下降到11秒。另外AVA的团队也是几个开源社区的大神，为首的[sindresorhus](https://github.com/sindresorhus)是github上获得星星最多的人。  
  
#### 为什么选择AVA
  
前端也有很多不错的测试框架，比如[Mocha](https://github.com/mochajs/mocha)、[Tap](https://testanything.org/)和[Tape](https://github.com/substack/tape)等，但AVA这个后起之秀更加优秀，除了上面说的可以并发测试外，还有配置简单，多种报告，环境隔离等优点。Mocha的作者[TJ](https://github.com/tj)也是个牛人，他也十分推崇AVA，并说如果让他来重新开发Mocha的话他也会按照AVA这种思路来做。  
  
#### AVA的语法
  
{% codeblock lang:js %}
import test from 'ava';

test('my first test', t => {
  t.same([1, 2], [1, 2]);
});
{% endcodeblock %}
  
test方法可以带一个字符串的标题和一个回调函数，这个回调函数就是你的测试代码，使用过其他测试框架的开发人员可以很快熟悉它的API并写出自己的测试。  
  
#### AVA的配置
  
AVA的配置也十分简单，虽然Mocha的配置也不复杂，但相对于后者AVA的配置特点是灵活和集中，你可以用多种方式来配置AVA，就拿配置Babel来说，你可以将Babel配置一起卸载`package.json`里面，也可以将Babel配置写在原来的Babel配置文件中，而在`package.json`文件里面写上Babel inherit配置就可以了。  
  
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

##### AVA的断言
  
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
  
当然你也可以集成其他的第三方断言库，比如[chai](http://chaijs.com/)，但这样的话你可能就使用不到测试方法中t这个测试对象了。  
  
{% codeblock lang:js %}
import { exppect } from 'chai';
import test from 'ava';

test('my first test', t => {
  expect([1, 2]).to.deep.equal([1, 2]);
});
{% endcodeblock %}
  
#### AVA的多种支持

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
  
#### 隔离环境
  
AVA会为每个测试环境单独启动一个进程来跑测试，这样的好处就保证了每个测试文件的环境不会收到其他测试文件的影响，也要求你写测试的时候更注重原子性，要求测试不依赖其他测试的运行结果或者测试数据，让测试具有独立性。  
  
但并发测试也会带来一些测试方法上的改变，以前串行测试要测试文件的IO操作，可能会临时创建一个文件进行读写，串行测试时由于每个测试都是顺序进行的，所以只需要一个文件就可以满足需求。但如果是并发测试，一个临时文件可能会被多个测试同时进行读写，这样就会导致冲突，最好的做法是为每个测试都单独创建一个临时文件来做测试。  
  
## [Enzyme](https://github.com/airbnb/enzyme)
  
Enzyme是[Airbnb](http://www.airbnb.com/)公司出的一个针对React测试的测试工具。Airbnb我觉得是国外继Google和Facebook之后技术能力十分强大的一家科技公司，有很多开源项目获得广大开发人员的认可，而Enzyme就是其中的一个。  
  
#### React的测试方式
  
Facebook在推出React的时候也推出一个测试套件，可以使用shallow render（浅渲染）和renderIntoDocument（完整渲染）的方式对React组件进行渲染，然后通过查找DOM来做测试结果校验。两者的主要区别是shallow render只渲染出组件的第一层DOM，其嵌套的DOM不会渲染出来，这样渲染的效率更高，单元测试的速度更快，在做单元测试的时候建议使用shallow render的方式。  
  
Enzyme对Facebook的测试套件做了封装，并提供了一套方便开发者测试React的API，下面的例子可以对比使用了Enzyme前后的区别。  
  
使用Enzyme前：  
{% codeblock lang:js %}
const output = setup({...props});
const ul = output.props.children;
const li = ul.props.children;
const a = li.props.children;
express(a.size).to.be.equal(1);
{% endcodeblock %}
  
使用Enzyme后：  
{% codeblock lang:js %}
const wrapper = shallow(<Foo {...props} />);
express(wrapper.find('a').length).to.be.equal(1); 
{% endcodeblock %}
  
最明显的变化是测试代码变少了，原来需要逐层查找DOM，现在只需要一个`find()`方法就可以搞定。  
  
#### Shallow Rendering & Full DOM Rendering
  
Enzyme针对这两种渲染方式都提供了各自一套API方法，但其实这2套API的方法差不多相同，只有个别几个方法不一样。  
  
{% codeblock lang:js %}
.find(selector) => ShallowWrapper
.children() => ShallowWrapper
.parent() => ShallowWrapper
.closest(selector) => ShallowWrapper
.unmount() => ShallowWrapper
.text() => String
.state([key]) => Any
.simulate(event[, data]) => ShallowWrapper
.setState(nextState) => ShallowWrapper
.setProps(nextProps) => ShallowWrapper
.debug() => String
{% endcodeblock %}
  
这里只列出了一小部分API，不但可以通过find方法查找DOM元素，还可以模拟DOM的事件，比如Click，Change等。  
  
#### 常用的方法
  
* find方法
  
find方法可以让你方便的查找到DOM元素，支持通过class名称查找，html标签查找，id查找，甚至是自定义组件的查找等。  
  
{% codeblock lang:js %}
const wrapper = shallow(<MyComponent />);
expect(wrapper.find('.foo')).to.have.length(1);
// compound selector
expect(wrapper.find('div.some-class')).to.have.length(3);
// CSS id selector
expect(wrapper.find('#foo')).to.have.length(1);
// Component
expect(wrapper.find(Foo)).to.have.length(1);
{% endcodeblock %}
  
* simulate方法
  
simulate方法让你可以模拟DOM元素的事件，传入参数是事件名和事件参数。  
  
{% codeblock lang:js %}
const wrapper = shallow(<MyComponent />);
wrapper.find('a').simulate('click');
wrapper.find('a').simulate('change', { target: { value: 'foo' } });
{% endcodeblock %}
  
* debug方法
  
debug方法可以让你打印出组件的DOM信息，方便测试遇到问题进行调试。  

{% codeblock lang:js %}
const wrapper = shallow(<Book title="Huckleberry Finn" />);
console.log(wrapper.debug());

Outputs to console:
<div>
 <h1 className="title">Huckleberry Finn</h1>
</div>
{% endcodeblock %}
  
* props和state等方法
  
对应React组件的props和state都有一些方法可以让你set和get props和state里的值，对于重置React组件的状态非常有用。  
  
## React组件的测试要点
  
#### render逻辑的测试
  
React中存在逻辑的地方有一部分是在render方法中，通过props或state的值来render出不同的页面。下面以一个例子来说明：  


{% codeblock lang:js %}
class Footer extends Component {
  renderFooterButtons(completedCount, clearCompleted) {
    if (completedCount > 0) {
      return (
        <button className="clear-completed" onClick={ () => clearCompleted() }>Clear completed</button>
      );
    }
  }

  render() {
    const { todos, actions, onShow } = this.props;
    const { clearCompleted } = actions;
    const activeCount = todos.reduce((count, todo) => todo.completed ? count : count + 1, 0);
    const completedCount = todos.length - activeCount;
    return (
      <footer className="footer">
        <span className="todo-count"><strong>{activeCount}</strong> item left</span>
        <ul className="filters">
          {[SHOW_ALL, SHOW_ACTIVE, SHOW_COMPLETED].map(filter =>
            <li key={filter}>
              <a className={classnames({ selected: filter === this.props.filter })}
                style={{ cursor: 'pointer' }}
                onClick={ () => onShow(filter) }>{FILTER_TITLES[filter]}</a>
            </li>
          )}
        </ul>
        {this.renderFooterButtons(completedCount, clearCompleted)}
      </footer>
    );
  }
}
{% endcodeblock %}






