---
layout: post
title: "React Navigation——在头部菜单中使用组件方法"
date: 2018-05-20 17:49
description: React Navigation——在头部菜单中使用组件方法
keywords: react-navigation,react-native
comments: true
categories: React Native
tags: [react-navigation,react-native]
---

{% img /images/post/2018/05/react-navigation.png 400 300 %}

在 [React Native](https://facebook.github.io/react-native/) 开发中，路由的跳转不再是 Web 世界里面的常用的 [React Router](https://github.com/ReactTraining/react-router)，而是 [React Navigation](https://reactnavigation.org/)。

今天介绍一下在 React Navigation 中如何在头部 (header) 调用 React 组件中的方法。

<!--more-->

## Navigator——导航器

React Navigation 可以创建多种`Navigator`（导航器），有通过页签控制页面跳转的`TabNagivator`（页签导航器），还有通过侧边栏进行页面跳转的`DrawerNavigator`（抽屉导航器），但最为常用的导航器是`StackNavigator`（堆栈导航器），它控制着普通页面之间的跳转。

在`StackNavigator`的`navigationOptions`（导航配置项）中，可以设置页面的`Header`信息，包括头部中的标题，标题的样式，头部左半部分和头部的右半部分等。

页面头部的左半部分一般是返回按钮，而右半部分一般用来展示一些额外操作，比如`保存`或者`下一步`等，以微信的朋友圈页面为例，请见下图：

{% img /images/post/2018/05/navigation-header.jpg 400 600 %}

## 问题描述

`Navigator`的`navigationOptions`一般作为页面组件的静态属性存在，而在静态属性中没办法直接调用组件中的方法，示例代码如下：

{% codeblock lang:js %}
class Foo extends React.Component {
  static navigationOptions = ({ navigation }) => {
    return {
      headerMode: 'screen',
      headerTitle: 'Foo',
      headerLeft: <Button onClick={() => navigation.goBack()}>Back</Button>,
      headerRight: <Button onClick={/*这里需要调用组件里的 doSomething 方法*/}>Save</Button>,
    };
  };

  doSomething = () => {
    this.setState({ count: this.state.count + 1 });
  }
  ...
}
{% endcodeblock %}

示例代码中页面的`headerMode`为`screen`，表示页面会附加一个 header，这个 header 与页面是同时存在或消失。

`headerLeft`表示左边的返回按钮，可以调用方法参数`navigation`的`goBack`方法返回到上一页。

而在`headerRight`需要调用组件中的`doSomething`方法，但因为`navigationOptions`是静态属性，所以没办法通过`this`关键字来得到`doSomething`方法，那么我们要怎么做才可以调用组件里面的方法呢？

## 解决思路

我们先来看下`navigationOptions`的方法参数`navigation`对象，看看这个对象里面有哪些东西可以帮到我们，下面列举了该对象几个主要的属性：

* navigate：方法，可以跳转到其他页面
* goBack：方法，返回上一页
* state - 对象，包含了当前路由的名称，key 值和路由参数的一个对象
* setParams - 方法，在路由中设置传递参数
* getParam - 方法，在路由中获取参数

我们看到`state`属性包含了路由参数，而`setParams`方法可以设置路由参数，那么只要我们将组件中的方法通过`setParams`设置到路由参数里面，然后在`state`中就可以获取到组件的方法了。

还有一个问题，我们在什么时候将组件方法设置到路由参数里面比较好呢？在几个 React 生命周期方法中，`componentWillMount`和`componentDidMount`都适合做这件事，但`componentWillMount`方法 React 官方已不推荐使用，所以我们这里用`componentDidMount`。

## 实现方法

我们在之前的代码上做一下扩展：

{% codeblock lang:js %}
class Foo extends React.Component {
  static navigationOptions = ({ navigation }) => {
    // state 里面的 params 已经包含了组件的 doSomething 方法，所以可以直接获取
    const { doSomething } = navigation.state.params;
    return {
      headerMode: 'screen',
      headerTitle: 'Foo',
      headerLeft: <Button onClick={() => navigation.goBack()}>Back</Button>,
      headerRight: <Button onClick={doSomething}>Save</Button>,
    };
  };

  componentDidMount() {
    // 只要是`StackNavigator`都可以通过`this.props`来获取到`navigation`对象
    this.props.navigation.setParams({ doSomething: this.doSomething });
  }

  doSomething = () => {
    this.setState({ count: this.state.count + 1 });
  }
  ...
}
{% endcodeblock %}

我们在`componentDidMount`中将`doSomething`方法设置到路由参数中，然后在`navigationOptions`中通过`navigation.state.params`就可以获取到该方法了，也可以通过`navigation.getParam`方法来获取路由参数。

## 总结

React Navigation 是 React Native 开发中的一个重要角色，而且最近也升级到了 2.0，很多功能特性都得到了增强和改善。这里只是介绍了开发过程中遇到的一个小坑，希望能对遇到同样问题的同学有所帮助。

## 参考链接

* [React-native/react-navigation: how do I access a component's state from `static navigationOptions`?
](https://stackoverflow.com/questions/43400151/react-native-react-navigation-how-do-i-access-a-components-state-from-static)
* [How to access this.props from `static navigationOptions=`](https://github.com/react-navigation/react-navigation/issues/147)
