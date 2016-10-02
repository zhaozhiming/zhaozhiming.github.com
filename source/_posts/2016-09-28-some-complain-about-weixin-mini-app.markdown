---
layout: post
title: "微信小程序之槽点一二三"
date: 2016-09-28 11:02
description: 微信小程序之槽点一二三
keywords: weixin, wechat, mini-app
comments: true
categories: app
tags: [weixin, wechat, mini-app]
---

{% img /images/post/2016/09/wechat.jpg %}  
  
微信小程序最近火爆IT界，抱着尝鲜的心理我也下载了**微信web开发者工具**并撸了一个Demo小程序，撸完之后发现微信小程序就开发过程而言，就像背影很美正面像鬼的女子，远没有外界所说的那么好，在实际开发过程中有各种不爽，下面我就来一一说下。  
  
<!--more-->  

## 不能npm install
  
微信小程序最大的一个痛点是不能使用第三方包，只能用原生的JavaScript的功能，虽然可以支持ES6了，但是像Lodash这种工具包要是能用的话，可以少写很多代码。  
  
```js
// 用原生的JavaScript
for(let i = 0; i < todos.length; i++) {
  const todo = todos[i];
  if (Number(todoId) === todo.id) {
    todo.completed = !todo.completed;
    todos[i] = todo;
    this.setData({
      todos: todos,
    });
    break;
  }
}

// 使用了lodash
const index = todos.findIndex(x => todoId=== x.id);
_.set(todos, `${index}.completed`, !_.get(todos, `${index}.completed`));
this.setData({
  todos: todos,
});
```
  
不能使用第三方包的最大坏处是不能复用代码，想象一下我在一个项目有几个组件写的非常好，可以在提供给其他项目使用，但由于小程序不能使用第三方包，就只能把原来的代码拷贝到新项目里面去。这样导致的结果就是每个小程序项目充斥着重复的代码，一旦公共代码要改动会要牵扯到很多个地方的修改。  
  
## css调试器不能自动补全
  
{% img right /images/post/2016/09/chrome_css.png 400 300 %}  
  
我们在调试页面样式时，很多时候会借助Chrome浏览器的开发者工具，在里面对某个元素添加样式非常方便，而且在输入css属性和值时工具会有自动补全的提示，这一点非常有用，即使你忘记了一些css也可以完成调试样式的工作。  
  
{% img right /images/post/2016/09/wechat_css.png 400 300 %}  
  
但是在微信开发者工具里面就不是这样了，工具不会自动补全css属性和值，作为开发者不可能记住每个css的属性，没有了自动补全让开发效率低了很多。  
  
  
## UI组件不好用
  
微信开发者工具提供了很多UI组件，基本上可以满足大部分的开发场景，但还是不得不吐槽里面一些组件的缺点。  
  
#### `checkbox`不能单独使用
  
开放的[`checkbox`](https://mp.weixin.qq.com/debug/wxadoc/dev/component/checkbox.html?t=1474974357075)组件需要包含在`checkbox-group`里面才能使用，如果直接使用`checkbox`组件，一个是不能监听change事件，二个是通过tap事件不能获取到check值。
  
所以只能结合`checkbox-group`一起使用，而多个`checkbox`的场景又比较少，所以感觉这个UI组件很鸡肋。  
  
#### icon 太少
  
还有要吐槽的一个组件是[icon](https://mp.weixin.qq.com/debug/wxadoc/dev/component/icon.html?t=1475052051701)，里面提供的icon非常非常的少，只有可怜的15个。可以预想到以后随着小程序逐渐复杂，开发者需要开发自己的icon组件，但开发出来的组件又不能复用（参见上面第一条），所以小程序项目会到处充斥着重复的代码。  
  
## 刷新没用，每次都要重新编译
  
微信开发者工具的动作菜单有`项目重建`和`刷新`两个子菜单，把这两个菜单放在一起很容易给人这样一种错觉，`项目重建`是重新编译项目，而`刷新`是不重新编译项目只刷新页面。`项目重建`没有什么问题，但是`刷新`菜单就不知道有什么用了，按了之后页面有进度条加载，但是并没有发生变化，而且大部分时候会导致console报错，真心不知道这个菜单有什么鸟用。  
  
##  不能写测试
  
测试是项目质量的保障，但在小程序里面没有示例代码和文档来指导你如何写测试代码，其实根本没法让你写单元测试。微信团队你们难道指望每个小程序都是靠手工测试来保证质量吗？我猜测微信开发者工具的开发团队（可能是腾讯的前端团队）平时也很少甚至不写单元测试，所以在开发者工具中就没有关于单元测试的考虑👎。  
  
## 总结
  
微信小程序刚推出不久，有一些缺点毛病是正常的，但如果腾讯希望小程序以后能掀起一股开发热潮，就请在开发者工具上加强开发体验，让开发者开发爽了，才能做出让用户爽的小程序来。  
  
最后附上我练手的小玩意儿，项目地址是：https://github.com/zhaozhiming/wechat-todolist  
  
{% img /images/post/2016/09/wechat-todo.gif %}  
