---
layout: post
title: "使用 Expo 打包 iOS 企业版 APP"
date: 2018-05-11 08:38
description: 使用 Expo 打包 iOS 企业版 APP
keywords: React Native, Expo, iOS, Enterprise APP
comments: true
categories: React Native
tags: [React Native, Expo, iOS, Enterprise APP]
---

{% img /images/post/2018/05/apps.jpg 400 300 %}

[Expo](https://expo.io/) 为 React Native 开发提供了不少便利性，其中有个最方便的功能就是 APP 打包，Expo 让开发者无需使用 Xcode 和 Android Studio 就可以完成 APP 的打包工作。

下面就来介绍一下如何使用 Expo 打包 iOS 企业版的 APP。

<!--more-->

## Apple 开发者账号说明

在打包 iOS APP 之前，你首先需要一个 Apple 开发者账号，需要一个什么样的开发者账号呢？请看下面：

类型 | 用途 | 限制 | 费用
:----------:|:------------:|:----------:|:----------:
个人开发者账号 | 用于个人开发者上传和发布应用，在 App Store 上显示个人开发者信息 | 只能有一个开发者，100 个 iOS 设备 UDID 测试 | $99
团体账号 | 用户团队、公司开发者上传和发布应用，在 App Store 上显示团体名称 | 允许多个账号管理，100 个 iOS 设备 UDID 测试 | $99
企业账号 | 用于企业发布应用，使用该证书的应用不需要审核，但是也不能发布到 App Store | 不能上传到 App Store, 无测试设备数量限制 | $299

企业账号相对其他类型的账号来说，可以免去 App Store 审核的流程，对于快速开发迭代的项目有比较大的帮助。那么要怎么知道自己的开发者账号是哪种类型呢？

### 查看开发者账号类型

假设现在你已经申请好了一个企业账号，那么就可以登陆 [Apple 开发者网站](https://developer.apple.com)，然后点击`Account`一栏。

{% img /images/post/2018/05/apple_dev.png 600 200 %}

再点击侧边栏的`Membership`，如果看到`Program Type`是`Apple Developer Enterprise Program`和`Entity Type`是`In-House / Enterprise`，那么证明你的开发者账号类型就是企业账号。

{% img /images/post/2018/05/apple_dev_type.png 600 400 %}

## 创建 App ID

确认完自己的账号是企业账号后，就可以开始做一些打包 APP 的准备工作了。

首先我们需要创建我们 APP 的 ID，这个过程是在 Apple 的开发者网站上面进行的，同样先登录 [Apple 开发者网站](https://developer.apple.com) 并进去`Account`页面，然后点击侧边栏的`Certificates, IDs & Profiles`。

{% img /images/post/2018/05/appid1.png 300 200 %}

进入`Certificates, IDs & Profiles`页面后，点击侧边栏的`App IDs`，然后再点击`+`（添加）按钮。

{% img /images/post/2018/05/appid2.png 600 400 %}

在弹出的新建页面中先填写`App ID Description`，这里可以随便写，只要不和已有的 App ID 重复就行。

{% img /images/post/2018/05/appid3.png 600 400 %}

然后再填写`Bundle ID`，注意这里需要和你的 Expo 工程中的配置项 [`bundleIdentifier`](https://docs.expo.io/versions/v26.0.0/guides/configuration.html#bundleidentifierthe-bundle-identifier-for-your-ios-standalone) 保持一致，推荐是域名的形式，比如`com.your.app`。

{% img /images/post/2018/05/appid4.png 600 500 %}

下一步是选择 App 所需的服务，比如你的 APP 需要消息推送的功能，那么就要勾选里面的`Push Notifications`，最后点击`Continue`按钮。

{% img /images/post/2018/05/appid5.png 600 500 %}

最后再确认页面上确认信息无误后，点击`Register`按钮即创建 App ID 成功了。

{% img /images/post/2018/05/appid6.png 300 600 %}

## 生成 Certificate Signing Reuest（CSR）文件

接下来是生成 CSR 文件，这个文件是后面生成发布证书所需要的，这一步骤在本地 Mac 电脑上就可以完成，无需上 Apple 开发者网站。

首先在 Mac 电脑上打开`Keychain Access`，然后在左上角菜单上选择`Request a Certification From a Certificate Authority...`，如下图所示：

{% img /images/post/2018/05/private1.png 600 300 %}

在创建页面上添加你的 Apple 用户邮箱，并在`Request is` 中选择`Saved to disk`，最后点击`Continue`按钮。

{% img /images/post/2018/05/private2.png 600 300 %}

选择文件的保存地址然后点击`Save`按钮即完成 CSR 文件的创建了，文件的后缀为`certSigningRequest`。

{% img /images/post/2018/05/private3.png 400 200 %}

## 发布证书

接着咱们来生成发布 APP 必不可少的一个文件——发布证书，而且需要将其导出为`p12`文件，因为这种类型的文件才是 Expo 所需要的，这一步骤需要在 Apple 开发者网站和本地电脑进行。

和生成`App ID`一样，首先咱们进去到`Certificates, IDs & Profiles`页面，点击侧边栏的`Certificates`下面的`Production`，表示生产环境上的证书。然后点击右上角的`+`（添加）按钮。

{% img /images/post/2018/05/production1.png 600 400 %}

选择证书类型，因为我们是制作企业版 APP，所以这里选择`In-House and Ad Hoc`。

{% img /images/post/2018/05/production2.png 600 600 %}

然后选择之前的 CSR 文件并上传，完了点击`Generate`按钮，这样一个后缀为`cer`的文件会下载到你的电脑。

{% img /images/post/2018/05/production3.png 600 300 %}

双击`cer 文件`会将发布证书导入到你的`Keychain`，我们在`Keychain`选择`Certificates`，在里面找到刚导入的证书，然后**同时选中证书和私钥**，注意这里要同时选择这 2 个文件，不然导出的文件会有问题，然后点击右键选择`Export 2 items`

{% img /images/post/2018/05/production4.png 600 500 %}

在弹出的对话框中我们选择好文件保存路径并点击保存按钮，这样发布证书的`p12`文件就创建完成了。

{% img /images/post/2018/05/production5.png 400 200 %}

## 消息推送证书

消息推送证书的`p12`文件和发布证书的创建过程基本一致，所不同的是在选择证书类型时要选择`Apple Push Notification Service SSL (Sandbox & Production)`。

{% img /images/post/2018/05/push1.png 600 400 %}

还有需要多选择对应的 App Id：

{% img /images/post/2018/05/push2.png 600 500 %}

其他步骤就和发布证书完全一样。

## APP 描述文件

最后一个准备文件是 APP 的描述文件，同样是在 Apple 开发者网站上生成的。

进入`Certificates, IDs & Profiles`页面后，点击侧边栏的`Provisioning Profiles`的`Distribution`，然后再点击`+`（添加）按钮。

{% img /images/post/2018/05/mobile_profile1.png 600 700 %}

然后是选择描述文件的类型，这里我们需要和发布证书一样选择`In House`。

{% img /images/post/2018/05/mobile_profile2.png 600 700 %}

接着选择 App Id。

{% img /images/post/2018/05/mobile_profile3.png 600 400 %}

再选择发布证书。

{% img /images/post/2018/05/mobile_profile4.png 600 400 %}

填写描述文件名称。

{% img /images/post/2018/05/mobile_profile5.png 600 400 %}

最后下载该文件即完成 APP 描述文件的创建，该文件的后缀是`mobileprovision`。

{% img /images/post/2018/05/mobile_profile5.png 600 400 %}

## Expo 发布

所有东西都准备好之后，我们就可以开始召唤神龙了。。。哦不，就可以开始用 Expo 来打包 APP 了。

Expo 的打包 iOS APP 命令是`exp build:ios`，参数`-e`是指打包企业版 APP，`-c`为清除之前上传的文件，然后重新上传，没有`-c`参数的话会延用上次上传的文件。

{% codeblock lang:sh %}
$ exp build:ios -e -c
{% endcodeblock %}

接着 Expo 会提示你选择：

* 让 Expo 来帮忙生成证书文件
* 还是自己上传证书文件

如果 APP 是要发布到 App Store 的话，可以选择第一种方式来打包，但我们是企业版 APP，所以这里要选择自己上传证书文件。

{% codeblock lang:sh %}
16:31:44 [exp] Checking if current build exists...

16:31:45 [exp] No currently active or previous builds for this project.
16:31:46 [exp] Removed existing credentials from expo servers
? How would you like to upload your credentials?
 I will provide all the credentials and files needed, Expo does limited
 validation
{% endcodeblock %}

接着 Expo 会要求你输入你的 Apple 开发者账号密码进行登陆认证。

{% codeblock lang:sh %}
We need your Apple ID/password to ensure the correct teamID and appID

Note: Expo does not keep your Apple ID or your Apple password.

? What's your Apple ID? xxxxxxxxx
? Password? [hidden]
{% endcodeblock %}

如果你的账号有多个开发者账号，需要选择正确的那一个，即企业账号。

{% codeblock lang:sh %}
16:32:05 [exp] Validating Credentials...
16:32:13 [exp] You have 2 teams
1) xxx (In-House)
2) yyy (Company/Organization)
? Which Team ID to use? 1) "xxx" (In-House)
{% endcodeblock %}

然后我们就可以来上传之前准备好的文件了，依次上传的文件是：发布证书的 p12 文件，消息推送证书的 p12 文件和 APP 的描述文件。

{% codeblock lang:sh %}
16:32:58 [exp]
WARNING! In this mode, we won't be able to make sure your certificates,
or provisioning profile are valid. Please double check that you're
uploading valid files for your app otherwise you may encounter strange errors!

Make sure you've created your app ID on the developer portal, that your app ID
is in app.json as `bundleIdentifier`, and that the provisioning profile you
upload matches that team ID and app ID.

16:32:58 [exp] Please provide your distribution certificate P12:
? Path to P12 file: /xxxx/Certificates.p12
? P12 password: [hidden]
16:33:34 [exp] Please provide the path to your push notification cert P12
? Path to P12 file: /xxxx/push_Certificates.p12
? P12 password: [hidden]

16:33:52 [exp] Please provide the path to your .mobile provisioning profile
? Path to your .mobile provisioning Profile /xxxx/app.mobileprovision

16:34:10 [exp] Encrypted certP12,certPassword,pushP12,pushPassword,provisioningProfile,teamId and saved to expo servers
{% endcodeblock %}

Expo 将文件上传到他们的服务器进行打包，同时会给出打包程序的链接，你可以通过链接了解打包的进度，一旦完成打包，最后会给出打包好的文件的下载链接，你也可以直接上 Expo 的网站上面点击下载按钮进行下载。

{% codeblock lang:sh %}
16:34:10 [exp] Publishing to channel 'default'...
16:34:24 [exp] Warning: 'react' peer dependency missing. Run `npm ls` in /xxxx/xxx to see full warning.
16:34:24 [exp]
16:34:24 [exp] If there is an issue running your project, please run `npm install` in /xxxx/xxx and restart.
16:34:24 [exp] Building iOS bundle
16:35:28 [exp] Building Android bundle
16:36:30 [exp] Analyzing assets
16:36:30 [exp] Uploading assets
16:36:34 [exp] No assets changed, skipped.
16:36:34 [exp] Uploading JavaScript bundles
16:36:42 [exp] Published
16:36:42 [exp] Your URL is

https://exp.host/@xxxx/xxxx

16:36:42 [exp] Building...
16:36:44 [exp] Build started, it may take a few minutes to complete.
16:36:44 [exp] You can monitor the build at

 https://expo.io/builds/xxxxxx

|16:36:44 [exp] Waiting for build to complete. You can press Ctrl+C to exit.
16:54:02 [exp] Successfully built standalone app: https://xxx/.../archive.ipa
{% endcodeblock %}

## 总结

从上面的过程可以看到，之前的准备工作都是 iOS APP 打包所需要的（如果你的 APP 是上传到 App Store，Expo 还可以帮你完成这一部分的工作），后面就直接交给 Expo 处理了，你唯一要做的只是敲一下命令，比原来的打包方式效率提高了很多。

如果你对这种方式感兴趣的话，也不妨下载 Expo 来试一下吧。
