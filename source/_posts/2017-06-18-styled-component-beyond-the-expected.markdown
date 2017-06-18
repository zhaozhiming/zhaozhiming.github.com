---
layout: post
title: "超乎你想像的 styled-components"
date: 2017-06-18 11:32
description: 超乎你想像的 styled-components
keywords: cssinjs,styled-components
comments: true
categories: [frontend]
tags: [cssinjs,styled-components]
---

{% img /images/post/2017/06/styled-components.png 400 350 %}

Css 在 React 中的写法一直在持续改进，从原生的 Css 到 [CSS Modules](https://github.com/css-modules/css-modules)，再到 Css In Js，目标是让开发者更高效地写组件的样式。
[`styled-components`](https://www.styled-components.com/) 是基于 Css In Js 方式实现的一个库，刚开始看到这个库时，我简单地以为它只是使用了内联方式来实现 React 组件的样式，但试用了之后，才发现不仅它的实现不是内联的方式，而且因为样式是写在 JS 里面，所以它的强大远超过我的预期。

<!--more-->

## 使用 ES6 的字符串模板

使用过 React 的同学可能比较清楚，如果要在一个组件通过 JS 来定义 css 样式，就必须在 React 组件里面通过`style`属性来写。

{% codeblock lang:js %}
const Home = () => <div style={{ marginLeft: 10 }}>Hello World</div>;
{% endcodeblock %}

这种方式有很多`缺点`：

* 样式属性与原生 css 的写法不一致，样式名称必须用驼峰的命名方式来命令，而不是多个单词用`-`号分隔；单位的写法也不一致，百分比是用字符串形式，而`pixel`值是用数字型形式。
* 无法写伪元素，要实现`hover`的话就必须使用`onMouseOver`事件来实现，而且要写很多 JS 代码，像`before`和`after`这类伪元素则根本没法通过这种方式实现。
  
`styled-components` 最吸引开发者的是使用了 ES6 的字符串模板方式来定义 css 样式，这样使得 css 的写法与原生的 css 写法基本一致，克服了上述的缺点。

{% codeblock lang:js %}
const Button = styled.a`
  display: inline-block;
  border-radius: 3px;
  padding: 0.5rem 0;
  margin: 0.5rem 1rem;
  width: 11rem;
  background: transparent;
  color: white;
  border: 2px solid white;

  & hover: {
    color: blue;
  }

  & before: {
    ...
  }
`
{% endcodeblock %}

那么 `styled-components` 的底层原理是什么？为什么通过 ES6 的字符串模板可以实现这种功能？感兴趣的同学可以看下下面`推荐阅读`的文章`styled-components-magic-explained`。

这里列举几个例子简单感受一下：

{% codeblock lang:js %}
// 我们有个方法来打印方法参数
const logArgs = (...args) => console.log(...args)

const favoriteFood = 'pizza'
// 用正常的方法调用，输出结果跟预期一样，是一句完整的句子。
logArgs(`I like ${favoriteFood}.`) // -> I like pizza.
// 而用字符串模板的方式来调用方法，则会变成这样：句子会被变量分隔成 2 个字符串，变量值变成数组后面的参数。
logArgs`I like ${favoriteFood}.` // -> ["I like ", "."] "pizza"
{% endcodeblock %}

## 生成一个随机的 class 名称

开始看到 `styled-components` 时我天真地以为它的样式是通过内联的方式放入到组件里面，其实不是，它的样式最终还是封装成一个 class 然后再放到组件里面。

{% img /images/post/2017/06/styled-components-class1.png %}
{% img /images/post/2017/06/styled-components-class2.png %}
{% img /images/post/2017/06/styled-components-class3.png %}

这样不仅避免了内联样式的局限性，而且class 的名称是一个随机的字符串，这样也完美解决了 Css 的一个大难题：样式名称重复的问题。

## Css In Js 的好处

使用 JS 来写 css 的好处，就是可以利用 JS 的语法来封装 CSS 对象，比如可以使用`if`语句来判断需要使用哪个样式值。

{% codeblock lang:js %}
// 这里利用了 `styled-components` 的特性
const Button = styled.div`
  color: ${props => props.isActive ? 'blue': 'red'};
`;
{% endcodeblock %}

或者通过一个方法来返回一个样式对象，通过参数来动态定义样式。

{% codeblock lang:js %}
const Button = (color) => {
  return styled.div`
    color: ${color};
  `;
};
{% endcodeblock %}

这个是不是很眼熟？有点像`sass`等 css 预处理器中的方法？没错！通过 JS 的特性就可以让你在 JS 中做预处理器中做的事情。

## `styled-components` 的预处理工具

说到 CSS 的预处理器，其实 `styled-components` 也有一套用 JS 实现的预处理器工具库——[polished](https://github.com/styled-components/polished)，其中包括一些常用的 CSS 方法，比如 clearfix、hsl、mix 等，让开发者可以完全不再使用 css 预处理器来写 css 了。

## `theme` 特性

`styled-components` 里面还有一个比较特别的特性是`theme`，它利用了 React 的`context`特性来传递其中的参数，可以让样式对象直接从最上层的组件传递到最下层的组件。

{% codeblock lang:js %}
// 首先定义一个`theme`样式对象
const styleGuide = {
  cloudy: '#F2F4F7',
  darkGray: '#4A637C',
  gray: '#7A8D9F',
  // ...more colors or mixins
};

return (
  // 将`theme`对象传递给上层组件`ThemeProvider`（这也是 `styled-components` 的一个 API），这样下层组件就可以通过"props.theme.gray"来引用其中的样式了
  <ThemeProvider theme={styleGuide}>
    <App>
      <Switch>
        <AuthRoute auth={auth} exact path="/" component={DashboardView} />
        <AuthRoute auth={auth} path="/profile" component={DashboardView} />
        <Route path="/login" component={LoginView} />
        <Route path="/logout" component={LogoutView} />
        {/* ...more routes */}
        <Route component={NotFoundView} />
      </Switch>
    </App>
  </ThemeProvider>
);

// Define our button, but with the use of props.theme this time
const Button = styled.button`
  font-size: 1em;
  margin: 1em;
  padding: 0.25em 1em;
  border-radius: 3px;
  /* Color the border and text with theme properties */
  color: ${props => props.theme.darkGray};
  border: 2px solid ${props => props.theme.darkGray};
`;
{% endcodeblock %}

## 可以对样式对象下面的子元素进行样式定义

在使用原生 css 的时候，经常会这样写样式。

{% codeblock lang:css %}
.foo h1 {
  font-size: 16px;
}

.foo h2 {
  color: red;
}
{% endcodeblock %}

很多 `styled-components` 新手（比如我）会为每个 h1 和 h2 定义一个样式对象，这样其实多写了很多代码，也没有必要，其实可以通过这种方式来定义子元素的样式。

{% codeblock lang:css %}
const FooDiv = styled.div`
  & h1 {
    font-size: 16px;
  }

  & h2 {
    color: red;
  }
`;
{% endcodeblock %}

## 独立样式文件

在一般的 React 组件中，样式和组件文件是分离的（比如 App.js 和 style.css），使用了 `styled-components` 后其实也可以沿用这种方式来组织文件，定义一个`style.js`文件来写样式对象，然后在组件文件中引入该样式文件。

{% codeblock lang:js %}
// style.js
import styled from 'styled-components';

export const AppDiv = styled.div`
  display: flex;
  justify-content: space-between;
`;

// App.js
import React from 'react';
import * as css from './style';

const App = () => <css.AppDiv>Hello World</css.AppDiv>;
export default App;
{% endcodeblock %}

## 缺点

### 不能用 stylelint 检查你的 Css 代码

在使用 `styled-components` 的过程中也会遇到一些问题，比如我们的项目会用`stylelint`来做样式代码的检查，但是使用了 styled-compoents 后就没办法让`stylelint`的规则生效了。

### 不能用 prettier 来格式化你的 Css 代码

现在`prettier`不仅可以帮你格式化 JS 代码，还可以格式化 CSS 代码，但如果使用了`styled-components`的话，JS 中的字符串模板内容没有办法使用`prettier`来格式化，这个也比较尴尬。

## 推荐阅读

* [styled-components-magic-explained](http://mxstbr.blog/2016/11/styled-components-magic-explained/)
* [embracing-the-power-of-styled-components](https://building.sentisis.com/embracing-the-power-of-styled-components-7b79a166c01b)
* [a-unified-styling-language](https://medium.com/seek-blog/a-unified-styling-language-d0c208de2660)
