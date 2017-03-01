---
layout: post
title: "如何安全地使用 React 的 context"
date: 2017-02-19 19:38
description: 如何安全地使用 React 的 context
keywords: react
comments: true
categories: code
tags: [react]
---

{% img /images/post/2017/02/react-context.jpeg %}

在自己的 React 项目中使用了大量的 context，虽然知道这是一个不推荐使用的特性，但还是用了很多，想看看别人是怎么使用 context，所以翻译了下面这篇文章，注意文章中引用了嵌套的twitter内容，所以访问时最好是翻墙访问，以免影响阅读。  
  
<!--more-->
  
## 如何安全地使用 React 的 context
  
Context 是 React 里面一个有着很多免费声明的、非常强大的特性，就像乐园里面的禁果一样。  
  
{% img https://cdn-images-1.medium.com/max/1600/1*rbUZNOyFC64KmJaRhF2Kww.png %}
  
这应该可以让你远离 context 了对吧？当然不是，它虽然是一个被禁用的 React 特性，但它的存在是一个不争的事实！context 可以把数据传递给组件树底层的组件，无需中间组件的参与。context 的经典用法是定制主题，本地化和路由这些方面。  
  
[Dan Abramov](https://medium.com/@dan_abramov) 设计了一些明智的规则让你知道什么时候不该使用 context：  
  
{% img https://cdn-images-1.medium.com/max/1600/1*b6Ev2SZ8SBlqhKVrOGDZaA.jpeg %}
  
现在你可能已经按照这个明智的建议来做，但同时，使用一些使用了 context 的库，比如 [react-router](https://github.com/ReactTraining/react-router)，当它和其他库像 [react-redux](https://github.com/reactjs/react-redux) 或 [mbox-react](https://github.com/mobxjs/mobx-react)组合时，甚至是和你自己的 shouldeComponentUpdate，又或者是由 React.PureComponent 提供的这个方法组合时，都仍然可能让你有陷入困境。长期存在的[问题](https://github.com/facebook/react/issues/2517) 可以在 React 或 React 相关的第三方库的问题跟踪列中找到。  
  
<blockquote class="twitter-tweet" data-lang="zh-cn"><p lang="en" dir="ltr">Redux implements sCU, making setState + context break, forces &quot;subscribe&quot; on everybody else. Seems like React needs a generic solution.</p>&mdash; Ryan Florence (@ryanflorence) <a href="https://twitter.com/ryanflorence/status/779320581678174208">2016 年 9 月 23 日</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
  
所以，为什么这篇博客跟你有关？这是因为：  
  
* 你是一个库的作者
* 你使用的库使用了 context，或者你自己使用 context，然后你想安全地使用 shouldComponentUpdate (SCU)，或者一些基于此的已有实现（比如 PureComponent, Redux 的 connect, 或者 MobX 的 observer）。

### 为什么 Context + ShouldComponentUpdate 有问题？

Context 用来和嵌套很深的组件交流，例如，一个根组件定义了一个主题，然后这个组件树中的任何组件可能（也可能不）对这个信息感兴趣，就像[官方的 context 示例](https://facebook.github.io/react/docs/context.html#passing-info-automatically-through-a-tree)。  
  
shouldComponentUpdate (SCU) 另一方面在重新渲染组件树（包括子组件）的一部分中起到短路作用，例如如果 props 或者组件的 state 没有被明确的修改，组件就不会重新渲染，但这可能意外中断 context 的传播。  
  
<blockquote class="twitter-tweet" data-lang="zh-cn"><p lang="en" dir="ltr"><a href="https://twitter.com/dan_abramov">@dan_abramov</a> <a href="https://twitter.com/iammerrick">@iammerrick</a> <a href="https://twitter.com/ryanflorence">@ryanflorence</a> specifically, anything that uses context will break. Seems unfair to the rest of us :(</p>&mdash; MICHAEL JACKSON (@mjackson) <a href="https://twitter.com/mjackson/status/779329979741904896">2016 年 9 月 23 日</a></blockquote>  
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>  
  
<blockquote class="twitter-tweet" data-lang="zh-cn"><p lang="en" dir="ltr"><a href="https://twitter.com/mjackson">@mjackson</a> <a href="https://twitter.com/iammerrick">@iammerrick</a> <a href="https://twitter.com/ryanflorence">@ryanflorence</a> The thing here is that React Redux doesn’t rely on context updating correctly. It knows React is broken.</p>&mdash; Dan Abramov (@dan_abramov) <a href="https://twitter.com/dan_abramov/status/779335426377183232">2016 年 9 月 23 日</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
  
让我们通过一个简单的 app 来模拟这个有冲突的问题：

<script async src="//jsfiddle.net/mweststrate/3ay25feh/embed/"></script>

在 context 和 SCU 中产生问题的地方显而易见，当你按了“Red please!”按钮（在“Result”栏上面）时，按钮本身的颜色刷新了，但待办列表没有被更新。原因很简单，我们的 TodoList 组件是智能的，它知道当它没有接收到新的待办子项它就不需要重新渲染（最聪明的地方是继承了 PureCompnent，其重新实现了 shouldComponentUpdate方法）。  
  
尽管如此，正因为这个最智能的地方（指继承了 PureCompnent，这个在大型应用是非常有必要的，因为它可以提供更好的性能），让 TodoList 中的 ThemedText 组件没有接收到更新了颜色的新 context！因为不管是 TodoList 或它的后代更新了，SCU 都返回 false。  
  
{% img https://cdn-images-1.medium.com/max/1600/1*pnkWX7uEzCeY-7r5Ii_Y-A.png %}
  
更糟的是，我们不能在 TodoList 中手工修改 SCU，因为它是固定了的一个方法。因为 SCU 不能接收相关的 context 数据（颜色），它不能（也不该）被订阅到指定的context数据中，毕竟它本身不是一个theme-aware的组件。  
  
总的来说，shouldComponentUpdate 返回 false 导致任何 context 更新不再传播到子组件中去，非常糟不是吗？我们可以修复这个问题吗？  
  
### ShouldComponentUpdate 和 Context 可以一起工作！
  
你注意到了问题只发生在我们更新 context 的时候吗？这个是解决问题的关键所在，只要确保你一直不更新 context就可以了，换句话说：  
  
* Context 不应该改变，它应该不可变
* 组件应该在其构造时只接收 context 一次
  
{% blockquote %}
或者，为了使其不同，我们不应该直接把 state 保存到 context 中，取而代之，我们应该像依赖注入系统一样使用 conext。
{% endblockquote %}
  
这意味着 SCU 不再干涉 context 需要传什么，因为不再需要传递新的 context 给子组件。棒极了！这解决了我们所有问题！  
  
### 通过基于 context 的依赖注入来和变更进行交流

如果我们想要改变主题颜色，很简单，我们在适当的地方有一个依赖注入系统（DI），所以我们可以向下传递一个仓库来管理我们的主题并订阅它，我们绝不会传递一个新的仓库，但要确保仓库本身是有状态的，并且可以观察到组件的变化：  
  
{% codeblock lang:js %}
// Theme 组件存储当前的主题状态，并允许组件订阅将来变化（的数据）
class Theme {
  constructor(color) {
    this.color = color
    this.subscriptions = []
  }

  setColor(color) {
    this.color = color
    this.subscriptions.forEach(f => f())
  }

  subscribe(f) {
    this.subscriptions.push(f)
  }
}

class ThemeProvider extends React.Component {
  constructor(p, c) {
    super(p, c)
    // 主题提供者在它的整个生命周期中使用同样的主题对象
    this.theme = new Theme(this.props.color)
  }

  // 必要时更新主题，更新的内容会传播给订阅的主键
  componentWillReceiveProps(next) {
    this.theme.setColor(next.color)
  }

  getChildContext() {
    return {theme: this.theme}
  }

  render() {
    return <div>{this.props.children}</div>
  }
}
ThemeProvider.childContextTypes = {
  theme: React.PropTypes.object
}

class ThemedText extends React.Component {
  componentDidMount() {
    // 订阅未来改变的主题
    this.context.theme.subscribe(() => this.forceUpdate())
  }
  render() {
    return <div style={{color: this.context.theme.color}}>
      {this.props.children}
    </div>
  }
}
ThemedText.contextTypes = {
  theme: React.PropTypes.object
}
{% endcodeblock %}
  
完整的可运行列表：  
  
<script async src="//jsfiddle.net/mweststrate/pc327358/embed/"></script>
  
注意到在这个示例里面颜色的改变已经正确了，但它仍然使用 PureComponent，而且重要组件 TodoList 和 ThemedText 的 API 并没有改变。  
  
虽然我们的 ThemeProvider 的实现变得更复杂了，它创建了一个Theme 对象来保持了我们主题的状态，Theme对象同时也是一个事件发射器，这可以让像 ThemeText 一样的组件来订阅未来的变化，Theme 对象通过 ThemeProvider 在组件树中传递。context 仍然是用来做这个的，但只有刚开始的时候传递了 context，后面的更新都通过 Theme 自己来传播，并没有重新创建一个 context。  
  
{% img https://cdn-images-1.medium.com/max/1600/1*ul_3UcymigXysL-JzgF8dQ.png %}
  
这个实现有点过于简单，更好的实现方式是需要在 componentWillUnmount 中清理事件监听器，并且应该使用 setState 来代替 forceUpdate，但好消息是你关注的内容已经有第三方库在开发了，它不会影响库的使用者，以后中间组件意外的 shouldComponentUpdate 实现将不再破坏库的行为。  
  
### 总结
  
通过依赖注入系统而不是状态的容器来限制使用 context，我们可以同时构造基于 context 的库和正确行为的 shouldComponentUpdate，而不会被干涉和破坏消费者的 API，还有非常重要的一点是，在当前受限的 React context 系统中可以正常工作，只要你遵守这条简单的规则：  
  
{% blockquote %}
Context 在每个组件中应该只被接收一次。
{% endblockquote %}
  
<blockquote class="twitter-tweet" data-lang="zh-cn"><p lang="en" dir="ltr"><a href="https://twitter.com/ryanflorence">@ryanflorence</a> <a href="https://twitter.com/mweststrate">@mweststrate</a> <a href="https://twitter.com/dan_abramov">@dan_abramov</a> <a href="https://twitter.com/sebmarkbage">@sebmarkbage</a> huh. i never thought of context as a &quot;dependency&quot; before. i guess it is.</p>&mdash; MICHAEL JACKSON (@mjackson) <a href="https://twitter.com/mjackson/status/779375007579287552">2016 年 9 月 23 日</a></blockquote>
<script async src="//platform.twitter.com/widgets.js" charset="utf-8"></script>
  
最后的提醒：context 仍然是实验性的特性，你应该避免直接使用 context（看上面 Dan Abramov 的规则），作为代替使用抽象过的 context 库（看下面的一些例子），但如果你是一个库的作者，或者如果你在写一些很好的高阶组件来处理 context，坚持上面的解决方案将避免一些令人讨厌的意外。  
  
**更新于 2016-9-29：** [Ryan Florence](https://medium.com/@ryanflorence) 刚发布了一个通用包，包含了上面讲的这个模式，所以你可以不用自己写这些代码了 [react-context-emission](https://github.com/ReactTraining/react-context-emission)。  
  
### 奖励：使用 MobX observables 作为 context 来简化事情
  
（如果你在使用或对 MobX 感兴趣那你对这一章也会感兴趣）
  
如果你使用 MobX，你完全可以跳过整个事件触发器这个东西，并且作为替代在 context 中使用仓库 observables，并通过使用 observer decorator 或高阶组件来订阅他们，这使得你不必自己管理数据的订阅。  
  
<script async src="//jsfiddle.net/mweststrate/xpw6a5Ld/embed/"></script>
  
实际上，更简单的方法是使用 MobX 中的 [Provider / inject](https://github.com/mobxjs/mobx-react#provider-and-inject) 的机制，它是对 React conext 机制的一个抽象化后的结果。它移除了 contextTypes 声明和类似其他东西的代码，注意这个类似的概念可以在一些库比如 [recompose](https://github.com/acdlite/recompose/blob/master/docs/API.md#withcontext) 或 [react-tunnel](https://github.com/gnoff/react-tunnel) 中找到。  
  
<script async src="//jsfiddle.net/mweststrate/b537yvcj/embed/"></script>
  
它是很值得尝试的，注意看，虽然我们初始基于依赖注入解决方案的代码量是原始代码的 1.5 倍，但最终的解决方案的代码和原来有问题的实现方案代码一样多。  
  


