---
layout: post
title: 一步一步发布公司的 NPM 包
date: 2024-09-15 15:18:54
description: 介绍管理员如何管理公司的 NPM 账号，以及指导开发人员如何发布公司的 NPM 包
keywords: npm, javascript
comments: true
categories: ai
tags: [npm, javascript]
---

{% img /images/post/2024/09/publish-company-npm.jpeg 400 300 %}

在项目开发中，有时候需要将自研的 NPM 包会发布到 NPM 的[公共注册表](https://registry.npmjs.org)让别人进行使用，NPM 的公共注册表是一个全球性的包管理库，任何用户都可以访问和下载你发布的公开包。如果你发布的是个人的 NPM 包，那么直接发布就可以了，但是如果你发布的是公司的 NPM 包，那么就需要通过一些流程和规范，来和公司其他同事进行协同合作。本文将为你介绍如何从零开始一步一步发布公司的 NPM 包，如果你的公司现在或以后也有这种需求，那么请跟我一起来学习吧。

<!--more-->

## 什么是 NPM

NPM 是 Node Package Manager 的缩写，它是一个针对 NPM 编程语言的包管理工具。它是随 Node.js 一起安装的，用于管理和分享 NPM 代码包（modules）。NPM 允许开发者轻松下载、安装、更新、删除各种开源的 NPM 包，并且可以轻松管理项目中的依赖关系。

## 人员角色

在发布公司 NPM 包的过程中，有以下人员或角色，他们主要负责以下工作：

- 管理员：负责管理公司 NPM 组织，添加和删除 NPM 组织成员，处理 NPM 包发布权限申请等，可能会有多个管理员。
- 开发人员: 指公司内部所有的开发人员，负责开发公司的 NPM 包，发布公司的 NPM 包到 NPM 包管理库。

{% img /images/post/2024/09/npm-flow.png 1000 600 %}

## 创建公司邮箱群组

发布 NPM 包需要 NPM 账号，而注册 NPM 账号需要邮箱，如果是发布个人 NPM 包的话直接使用个人邮箱注册就可以了，但是如果是发布公司的 NPM 包，那么要注册 NPM 账号的邮箱就不能是个人邮箱，而应该是一个公司邮箱群组。邮箱群组是一个包含多个员工电子邮箱地址的统一地址，用于向所有成员同时发送邮件。

使用邮箱群组是出于多方面的考虑，一方面是安全，注册 NPM 账号的邮箱主要是用来接收 NPM 的通知邮件，比如发布 NPM 包成功后的通知。假设有人用公司的名义发了一个恶意的 NPM 包，那么公司的邮箱群组会收到 NPM 的通知邮件，这样公司的管理员们可以及时发现问题，如果是个人邮箱的话，那么就只有个人知道，这样就会增加安全风险。

另一方面单点问题，设想一下，如果是个人邮箱管理公司的 NPM 包，那么如果这个人离职了，那么这个人的邮箱就作废了，从而导致 NPM 上的组织也作废了，公司需要重新使用新邮箱注册 NPM 账号，重新在 NPM 上面创建组织。而最麻烦的是，以前的 NPM 包公司已经无法管理了，无法再对老的 NPM 包进行升级或者删除。

所以建议公司在注册 NPM 账号时，使用公司邮箱群组来注册，这样可以避免上述问题。邮箱群组的名字建议叫 `npm@公司域名`，比如 `npm@your-company.com`，这样可以清晰的表明这个邮箱群组是用来注册 NPM 账号的。

## 管理员创建 NPM 账号与组织

有了邮箱群组后，管理员首先到 [NPM 网站](https://www.npmjs.com/)上创建一个账号，NPM 账号创建可以[参考 NPM 的官方文档](https://docs.npmjs.com/creating-a-new-npm-user-account)进行创建。

创建完 NPM 账号后，管理员需要创建一个 NPM 组织，NPM 组织是一个用于管理公司 NPM 包的组织，公司内部的所有 NPM 包都会发布到这个组织下。NPM 组织的创建可以[参考 NPM 的官方文档](https://docs.npmjs.com/creating-an-organization)进行创建。

## 开启双因子认证

对于公司的 NPM 账号，安全性是非常重要的，谁也不想看到有天公司的 NPM 包被别人恶意篡改了。为了增加账号的安全性，建议开启[双因子认证](https://docs.npmjs.com/about-two-factor-authentication)。

> 双因子认证（Two-Factor Authentication，简称 2FA）是一种增强账户安全性的验证方法，通过要求用户在登录或进行敏感操作时提供两种不同类型的认证因素，从而确保用户身份的真实性。与仅依赖单一密码相比，双因子认证显著提高了账户的安全性，减少了未经授权访问的风险。

NPM 上的双因子认证可以通过安全密钥或 TOTP（Time-based One-time Password）来实现。

- 安全密钥允许使用生物识别设备（ 比如苹果的 [Touch ID](https://support.apple.com/en-gb/HT204587)）进行认证
- TOTP 需要在移动设备上安装认证应用程序（比如 [Google Authenticator](https://support.google.com/accounts/answer/1066447)） 来生成验证码

### 多管理员设置双因子认证

对于多管理员的 NPM 账号来说，使用 TOTP 不太方便，因为每个管理员都需要在自己的手机上安装认证应用程序，这样就会增加管理的复杂度。所以建议使用安全密钥的方式来开启双因子认证。

**首个**管理员开启双因子认证后，需要创建**恢复码**，然后将恢复码分享给**其他**管理员，其他管理员**首次登录**可以使用恢复码来登录 NPM 网站，然后设置自己的安全密钥。

在账号的`Profile` -> `Account` -> `Two-factor authentication` 页面可以设置安全密钥和恢复码。

{% img /images/post/2024/09/npm-2fa.png 1000 600 %}

**注意**：NPM 每次可以生成 5 个恢复码，这些恢复码通常用于在无法进行双因子认证时登录账户。一旦您使用某个恢复码成功登录，系统将自动使该恢复码**无效**以防止重复使用。所以当其他管理员使用了恢复码登录后，建议尽快生成新的恢复码，并告知其他管理员。

{% img /images/post/2024/09/npm-recover-code.png 1000 600 %}

其他管理员使用恢复码登录后，可以设置自己的安全密钥，比如使用 Apple Touch ID 作为安全密钥，这样在下次登录时就可以直接使用 Touch ID 而不需要输入恢复码了。

## 邀请组织成员

创建好组织后，就可以邀请开发人员加入组织了，邀请流程如下：

- 进入用户设置页面，选择组织名称，比如`your-organization`
- 选择`成员`标签，然后点击`邀请成员`
- 输入开发人员的邮箱并点击`邀请`按钮，NPM 将向开发人员发送一封邮件
- 开发人员点击邮件中的链接后，将加入 NPM 组织

{% img /images/post/2024/09/npm-invite-member.png 1000 600 %}

## 开发人员注册账号

开发人员也需要到 NPM 网站上创建一个账号，发布 NPM 包需要使用账号进行发布，有以下注意事项：

- 建议使用公司邮箱进行注册
- 创建账号完成后，开启双因子认证

## 发布权限申请

创建完 NPM 账号后，开发人员需要申请公司 NPM 包发布权限，否则无法发布公司的 NPM 包，申请流程为：

- 找管理员将开发人员的 NPM 账号添加到公司的 NPM 组织
- 开发人员需要提供 NPM 账号名或注册邮箱给管理员
- 管理员邀请开发人员的 NPM 账号到公司的 NPM 组织后，开发人员的邮箱会收到一封邀请邮件
- 开发人员点击邮件中的链接后，将加入 NPM 组织

这样开发人员就拥有了发布公司 NPM 包的权限。

{% img /images/post/2024/09/npm-join-org.png 1000 600 %}

## 发布 NPM 包

开发人员申请了公司 NPM 包发布权限后，就可以发布公司的 NPM 包了。

### 修改包名

首先需要修改 NPM 包名称，公司的 NPM 包将以公司组织名称开头，比如 `@your-organization`。修改你项目中的 `package.json` 文件，将其中的 `name` 加上公司的 NPM 组织名称，假设你原来的 NPM 包名为 `foo`，那么修改后的内容如下所示：

```json
{
  "name": "@you-organization/foo"
}
```

### 使用 NPM CLI 登录

再使用 NPM 命令行工具在终端进行登录，在终端执行以下命令：

```bash
npm login
```

命令执行后，会提示你按回车键打开浏览器链接，打开浏览器后会进入到 NPM 网站。如果你没有在 NPM 网站上登录，会提示输入用户名和密码进行登录。如果你设置了双因子认证，网页会提示你输入安全密钥或验证码。

{% img /images/post/2024/09/npm-cli-login.png 1000 600 %}

完成以上操作后终端会提示你登录成功。

```bash
$ npm login
npm notice Log in on https://registry.npmjs.org/
Login at:
https://www.npmjs.com/login?next=/login/cli/14939f9b-26fa-48df-ad2a-f4ac972897f9
Press ENTER to open in the browser...

Logged in on https://registry.npmjs.org/.
```

### 执行发布命令

在终端登录了 NPM 账号后，执行以下命令进行 NPM 包发布：

```bash
npm publish --access public
```

**注意**：发布命令加上 `--access public` 参数，表示将 NPM 包发布到 NPM 的**公开**包管理库。如果不添加该参数的话，NPM 会默认将你的包发布到 NPM 的**私有**包管理库，这种类型的包管理库是 NPM 的收费服务，如果公司没有购买这种服务，那么发布到私有包管理库会导致发布失败。

开发人员也可以在 `pacakge.json` 文件中添加以下内容，然后直接通过 `npm publish` 命令进行发布，这种方法也会发布到 NPM 公开包管理库。

```json
{
  "publishConfig": {
    "access": "public"
  }
}
```

执行了发布命令后，跟登录命令一样，会提示你进入 NPM 网站进行双因子认证，认证成功后将发布 NPM 包到 NPM 的公开包管理库。

发布成功后可以在 NPM 官网的公司组织下查看包是否上传成功，以 `next` 组织为例，可以在浏览器地址 `https://www.npmjs.com/org/next` 上查看该组织的 NPM 包信息。

{% img /images/post/2024/09/npm-org-packages.png 1000 600 %}

## 总结

本文介绍了发布公司 NPM 包的一系列流程和建议，包括作为管理员或者普通开发人员需要执行的一些操作，以及在安全方面需要注意的一些事项。此流程不仅适用于 NPM 包，也适用于其他包管理工具，比如 PIP 或者 Maven 等。如果你也正在发布公司的 NPM 包，那么希望这篇文章对你有所帮助。

关注我，一起学习各种人工智能和 GenAI 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
