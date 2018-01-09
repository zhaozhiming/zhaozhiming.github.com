---
layout: post
title: "如何扩展 Create React App 的 webpack 配置"
date: 2018-01-08 20:04
description: 如何扩展 Create React App 的 webpack 配置
keywords: react,webpack
comments: true
categories: code
tags: [react,webpack]
---

{% img /images/post/2018/01/cra.png 400 300 %}

[Create React App](https://github.com/facebookincubator/create-react-app)（以下简称 CRA）是创建 React 应用的一个脚手架，它与其他脚手架不同的一个地方就是将一些复杂工具（比如 webpack）的配置封装了起来，让使用者不用关心这些工具的具体配置，从而降低了工具的使用难度。

但是对于一些熟悉 webpack 的开发者来说，他们可能想对 webpack 配置做一些修改，这个时候应该怎么办呢？

<!--more-->

其实我们可以通过以下几种方式来修改 webpack 的配置：

* 项目 eject
* 替换 react-scripts 包
* 使用 react-app-rewired
* scripts 包 + override 组合

下面对这几种方式分别进行介绍。

## 项目 eject

使用 CRA 创建完项目以后，项目在`package.json`里面提供了这样一个命令：

{% codeblock lang:json %}
{
  ...
  "scripts": {
    "eject": "react-scripts eject"
  },
  ...
}
{% endcodeblock %}

执行完这个命令——`yarn run eject`后会将封装在 CRA 中的配置全部`反编译`到当前项目，这样用户就可以完全取得 webpack 文件的控制权，想怎么修改就怎么修改了。

{% codeblock lang:sh %}
# eject 后项目根目录下会出现 config 文件夹，里面就包含了 webpack 配置
config
├── env.js
├── jest
│   ├── cssTransform.js
│   └── fileTransform.js
├── paths.js
├── polyfills.js
├── webpack.config.dev.js // 开发环境配置
├── webpack.config.prod.js // 生产环境配置
└── webpackDevServer.config.js
{% endcodeblock %}

CRA 与其他脚手架不同的另一个地方，就是可以通过升级其中的`react-scripts`包来升级 CRA 的特性。比如用老版本 CRA 创建了一个项目，这个项目不具备 [PWA](https://developers.google.com/web/progressive-web-apps/) 功能，但只要项目升级了`react-scripts`包的版本就可以具备 PWA 的功能，项目本身的代码不需要做任何修改。

但如果我们使用了`eject`命令，就再也享受不到 CRA 升级带来的好处了，因为`react-scripts`已经是以文件的形式存在于你的项目，而不是以包的形式，所以无法对其升级。

## 替换 react-scripts 包

[react-scripts][react-scripts] 是 CRA 的一个核心包，一些脚本和工具的默认配置都集成在里面，使用 CRA 创建项目默认就是使用这个包，但是 CRA 还提供了另外一种方式来创建 CRA 项目，即使用自定义 scripts 包的方式。

{% codeblock lang:sh %}
# 默认方式
$ create-react-app foo

# 自定义 scripts 包方式
$ create-react-app foo --scripts-version 自定义包
{% endcodeblock %}

`自定义包`可以是下面几种形式：

* `react-scripts`包的版本号，比如`0.8.2`，这种形式可以用来安装低版本的`react-scripts`包。
* 一个已经发布到 npm 仓库上的包的名字，比如`your-scripts`，里面包含了修改过的 webpack 配置。
* 一个 tgz 格式的压缩文件，比如`/your/local/scripts.tgz`，通常是未发布到 npm 仓库的自定义 scripts 包，可以用 `npm pack` 命令生成。

这种方式相对于之前的`eject`是一种更灵活地修改 webpack 配置的方式，而且可以做到和 CRA 一样，通过升级 scrips 包来升级项目特性。

自定义 scripts 包的结构可以参照`react-scripts`包的结构，只要修改对应的 webpack 配置文件，并安装上所需的 webpack loader 或 plugin 包就可以了。

## 使用 react-app-rewired

虽然有这两种方式可以扩展 webpack 配置，但是很多开发者还是觉得太麻烦，有没有一种方式可以既不用`eject`项目又不用创建自己的 scripts 包呢？答案是肯定的，[react-app-rewired](https://github.com/timarney/react-app-rewired) 是 react 社区开源的一个修改 CRA 配置的工具。

在 CRA 创建的项目中安装了`react-app-rewired`后，可以通过创建一个`config-overrides.js` 文件来对 webpack 配置进行扩展。

{% codeblock lang:js %}
/* config-overrides.js */

module.exports = function override(config, env) {
  //do stuff with the webpack config...
  return config;
}
{% endcodeblock %}

`override`方法的第一个参数`config`就是 webpack 的配置，在这个方法里面，我们可以对 `config` 进行扩展，比如安装其他 loader 或者 plugins，最后再将这个 `config` 对象返回回去。

最后再修改`package.json`中的脚本命令，修改内容请见[这里](https://github.com/timarney/react-app-rewired#3-flip-the-existing-calls-to-react-scripts-in-npm-scripts)。

## scripts 包 + override 组合

虽然`react-app-rewired`的方式已经可以很方便地修改 webpack 的配置了，但其实我们也可以在自定义的 script 包中实现类似的功能。

在`react-app-rewired`的源码中可以看到它核心的包也叫 [react-app-rewired][react-app-rewired]，里面重新覆盖了`react-scripts`中的几个脚本文件，包括`build.js`、`start.js`和`test.js`。

具体过程是怎样的呢？以`build.js`为例：

* 先获取 webpack 的基本配置，然后再调用`config-overrides.js`（就是在根目录中新增的那个文件）中的`override`方法，将原先的 webpack 对象作为参数传入，
* 再取得经过修改后的 webpack 配置对象
* 最后再调用`react-scripts`中的`build.js`脚本，传入修改后的 webpack 对象来执行命令，

具体源码如下：

{% codeblock lang:js %}
const overrides = require('../config-overrides');
const webpackConfigPath = paths.scriptVersion + "/config/webpack.config.prod";

// load original config
const webpackConfig = require(webpackConfigPath);
// override config in memory
require.cache[require.resolve(webpackConfigPath)].exports =
  overrides.webpack(webpackConfig, process.env.NODE_ENV);
// run original script
require(paths.scriptVersion + '/scripts/build');
{% endcodeblock %}

知道了原理之后，我们也可以修改自定义 scripts 包的脚本文件，还是以`build.js`为例，在获取基本 webpack 配置对象和使用 webpack 对象之间加入以下代码：

{% codeblock lang:js %}
// override config
const override = require(paths.configOverrides);
const overrideFn = override || ((config, env) => config);
const overrideConfig = overrideFn(config, process.env.NODE_ENV);
{% endcodeblock %}

`overrideConfig`就是修改后的 webpack 对象，最后修改调用了 webpack 对象的代码，将原来的 webpack 对象替换成修改后的 webpack 对象。

## 总结

CRA 是一个非常棒的 React 脚手架工具，但你如果不满足于它的 webpack 默认配置，你可以通过上述几种方式来扩展自己项目的 webpack 配置，这几种方式各有优缺点，可以结合具体的使用场景来选择合适自己的方式。


[react-scripts]: https://github.com/facebookincubator/create-react-app/tree/8cae659ec5a066eff8ea270346dc8c1ef064f9aa/packages/react-scripts
[react-app-rewired]: https://github.com/timarney/react-app-rewired/tree/4954531eaab6da14c4e3c943cb2038b46d5f9125/packages/react-app-rewired
