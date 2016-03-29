---
layout: post
title: "使用AVA和Enzyme测试React组件（二）"
date: 2016-03-29 20:42
description: 使用AVA和Enzyme测试React组件（二）
keywords: ava,enzyme,react
comments: true
categories: test
tags: [ava,enzyme,react]
---

{% img /images/post/2016/03/airbnb.png 400 300 %}  
  
Enzyme是[Airbnb](http://www.airbnb.com/)公司推出的一个针对React组件的测试工具。Airbnb我觉得是国外继Google和Facebook之后技术实力十分强大的一家科技公司，有很多开源项目获得广大开发人员的认可，Enzyme就是其中的一个。  
  
<!--more-->  

## React的测试方式
  
Facebook在推出React的时候也推出一个测试套件，可以使用shallow render（浅渲染）和renderIntoDocument（完整渲染）的方式对React组件进行渲染，然后通过查找DOM来做测试结果校验。两者的主要区别是shallow render只渲染出组件的第一层DOM，其嵌套的DOM不会渲染出来，这样渲染的效率更高，单元测试的速度更快，在做单元测试的时候建议使用shallow render的方式。  
  
Enzyme对Facebook的测试套件做了封装，并提供了一套API来让开发者做React单元测试的时候更加方便，下面的例子可以对比使用了Enzyme前后的区别。  
  
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
  
## Shallow Rendering & Full DOM Rendering
  
Enzyme针对这两种渲染方式都提供了各自一套API方法，但其实这2套API的方法差不多相同，只有个别方法不一样。  
  
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
  
## 常用的方法
  
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
  
对应React组件的props和state，Enzyme也有一些方法可以让你set和get其中的值，对于重置React组件的状态非常有用。  

