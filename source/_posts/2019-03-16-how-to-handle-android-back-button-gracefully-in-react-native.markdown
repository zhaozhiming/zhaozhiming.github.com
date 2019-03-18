---
layout: post
title: "如何在 React Native + React Navigation 的项目中优雅地处理 Android 回退按钮事件"
date: 2019-03-16 20:32
description: 如何在 React Native + React Navigation 的项目中优雅地处理 Android 回退按钮事件
keywords: react-native,android back,react-navigation
comments: true
categories: react-native
tags: [react-native,android back,react-navigation]
---

{% img /images/post/2019/03/back-button.jpg 400 300 %}

在 Android 手机上`回退按钮`是经常会用到一个功能，可以用来返回上一个页面，或者是用来取消某项操作等等，总之十分方便，今天就给大家介绍一下如何在 React Native（以下简称 RN）项目中结合 [React Navigation](https://reactnavigation.org/) 进行回退按钮的处理。

为什么是`React Navigation`？因为它已逐渐成为 RN 项目路由管理工具的标配，RN 的官方文档推荐使用 [Expo](https://expo.io/) 来创建项目，而在 `Expo` 中则推荐使用`React Navigation`来做路由管理。

<!--more-->

## RN 的回退按钮事件

RN 提供了专门处理`回退按钮`的 API —— [BackHandler](https://facebook.github.io/react-native/docs/backhandler)，它的使用方式跟其他`事件监听器`十分类似，通过传入一个事件名和事件函数就可以完成事件的绑定，代码示例如下：

{% codeblock lang:js %}
  BackHandler.addEventListener('hardwareBackPress', function() {
    // do something
    return true;
  })
{% endcodeblock %}

注意：这里不同于其他`事件监听器`的地方是**必须`return` 布尔值**，因为`回退按钮`的事件是**反顺序**触发的，即最后一个注册的事件会被最先触发，`return true`表示不会再继续调用上一级的`回退按钮`事件，反之`return false`则表示会调用。

这个 API 在使用上虽然没有什么问题，但是有一点比较麻烦：如果多个组件同时需要处理`回退按钮`事件，那就需要在多个组件上添加事件，而且还要注意组件间的事件调用关系，比较麻烦。

另外`回退按钮`事件的添加和移除需要在组件的生命周期方法`mount`和`unmount`中进行：

{% codeblock lang:js %}
  componentDidMount() {
    BackHandler.addEventListener('hardwareBackPress', this.handleBackPress);
  }

  componentWillUnmount() {
    BackHandler.removeEventListener('hardwareBackPress', this.handleBackPress);
  }
{% endcodeblock %}

但由于有些`一级页面`是常驻`路由堆栈`中的，因此页面的事件被添加之后，即使页面进行了跳转，页面中的事件也不会被销毁，这样就会造成一些性能上的损耗。

## React Navigation 回退事件的处理

我们再来看`React Navigation`是如何处理`回退按钮`的，这里是[官方文档](https://reactnavigation.org/docs/en/custom-android-back-button-handling.html)，我们可以看到它的处理方式其实和 RN 的差不多，只不过将组件的生命周期方法换成了`React Navigation`的。

{% codeblock lang:js %}
  constructor(props) {
    super(props);
    this.didFocusSubscription = props.navigation.addListener('didFocus', payload =>
      BackHandler.addEventListener('hardwareBackPress', this.handleBackPress)
    );
  }

  componentDidMount() {
    this.willBlurSubscription = this.props.navigation.addListener('willBlur', payload =>
      BackHandler.removeEventListener('hardwareBackPress', this.handleBackPress)
    );
  }

  componentWillUnmount() {
    this.didFocusSubscription && this.didFocusSubscription.remove();
    this.willBlurSubscription && this.willBlurSubscription.remove();
  }
{% endcodeblock %}

在代码中我们可以看到，`React Navigation`通过`didFocus`方法（即页面加载完成之后的回调函数）添加`回退按钮`事件，然后通过`willBlur`方法（即页面离开之前的回调函数）来移除事件。这样一来，即使是常驻路由堆栈的页面，只要进行了页面跳转，`回退按钮`事件就会被移除。

但这种方式还是会有一些问题，首先页面还是会加载无法移除的事件，只不过事件从`回退按钮`事件变成了`React Navigation`的监听事件。

另外一个比较**严重**的问题是：如果进入和离开页面耗时比较长（比如需要发送好几次网络请求），或者切换页面比较快的话，就有可能出现先添加当前页面事件（`didFocus`）再移除上个页面事件（`willBlur`）的情况，这意味着当前页面的`回退按钮`事件可能被添加后马上被移除，从而导致操作异常。

## 更好的解决方法

总结一下上面的问题：

* 需要在多个组件（页面）中处理`回退按钮`事件，使得代码的编写变得麻烦
* 事件的添加删除顺序在页面切换的过程中可能引起错乱，导致 App 行为异常

上面问题产生的主要原因是**多个地方在处理`回退按钮`事件**，如果我们的 APP 只有一个地方在处理`回退按钮`事件的话，那就可以避免这些问题了。

**但是**，如果把`回退按钮`事件的处理放在一个地方的话，我们需要先解决 2 个问题：

* 在哪个地方添加`回退按钮`事件？
* 我们如何判断当前页面是哪个页面？因为我们要根据不同页面处理不同的逻辑，比如 A 页面的回退按钮要退出 App，而 B 页面则是要退回到上一个页面。

### 在哪个地方添加`回退按钮`事件？

针对这个问题，我们可以在组件的最外层封装一个`Layout`组件，然后通过这个组件来添加`回退按钮`的事件，然后把原先的`根组件`作为这个组件的`children`，代码示例如下：

{% codeblock lang:js %}
class Layout extends React.Component {
  componentDidMount() {
    BackHandler.addEventListener('hardwareBackPress', this.handleBackPress);
  }

  componentWillUnmount() {
    BackHandler.removeEventListener('hardwareBackPress', this.handleBackPress);
  }

  render() {
    <View>
      {this.props.children}
    </View>
  }
}

class App extends React.Component {
  render() {
    <Layout>
      <Root /> // 原先的根组件
    </Layout>
  }
}
{% endcodeblock %}

在其他地方不需要再添加`回退按钮`事件，所有页面的事件统一在`handleBackPress`方法中进行处理。

### 如何判断当前页面是哪个页面？

在`React Navigation`的官方文档中介绍了一个[无需 navigation 参数进行导航](https://reactnavigation.org/docs/en/navigating-without-navigation-prop.html)的方法。这里的原理是通过设置根目录节点来获取全局的`React Navigation`对象，然后通过这个对象来调用`React Navigation`的 API。

我们可以通过扩展这个方法来获取当前页面，我们增加如下的函数：

{% codeblock lang:js %}
const getCurrentRoute = () => {
  // 获取到 route 对象
  let route = navigator.state.nav;
  // 一直循环获取到最底层的 route 对象
  while (route.routes) {
    route = route.routes[route.index];
  }
  return route;
};

// route: {
//   "key": "id-1552444588477-2",
//   "params": {
//     "disableBack": true,
//   },
//   "routeName": "Settings",
// }
{% endcodeblock %}

通过这个函数我们可以获取到当前页面的路由对象，在这个对象中可以根据`routeName`来进行页面判断：

{% codeblock lang:js %}
handleBackPress = () => {
  const { routeName } = NavigationSeveice.getCurrentRoute();
  switch(routeName) {
    case 'Home': {
      // dosomething
      return true;
    }
    case 'Settings': {
      // dosomething
      return true;
    }
    default: {
      return true;
    }
  }
}
{% endcodeblock %}

## 直接使用

如果觉得自己实现比较麻烦的话，也可以直接使用这个库——[React Native Android Backer](https://github.com/zhaozhiming/react-native-android-backer)，这是笔者写的一个基于以上思路的 RN 库，使用方法非常简单，感兴趣的同学可以参考里面的`Readme`文档。

## 总结

这是在 RN 开发中遇到的一个实际问题，开始参考了各种文章和方法都不太理想，后来自己通过摸索找到了令自己满意的解决方案，并将其抽取成了第三方库，也希望能帮助到遇到同样问题的朋友，欢迎大家试用和留言讨论，谢谢。
