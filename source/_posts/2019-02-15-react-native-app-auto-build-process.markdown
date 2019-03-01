---
layout: post
title: "React Navtive App 自动打包实践指南"
date: 2019-02-15 13:29
description: React Navtive App 自动打包实践指南
keywords: react-native,auto-build
comments: true
categories: react-native
tags: [react-native,auto-build]
---

{% img /images/post/2019/02/auto-build.jpg 400 300 %}

React Native 创建项目有 2 种方式，一种是通过 [Expo](https://expo.io/) 框架创建，这种项目可以通过 Expo 提供的命令进行打包；另外一种是通过 React Native 原生命令生成的项目，需要使用 [Gradle](https://gradle.org/) 和 [Xcode](https://developer.apple.com/xcode/) 这些原生工具打包，虽然麻烦了一些，但是灵活性更高，今天讨论的自动打包主要是基于后者。

<!--more-->

## 打包流程

{% img /images/post/2019/02/process.png 873 300 %}

这是自动打包程序的流程图，概述了从我们提交代码到收到通知这一过程。

* 提交代码：将要发布的代码提交到代码库，这里我们假设使用 [gitlab](https://about.gitlab.com/) 来做为我们代码管理的平台。
* 持续集成：将新代码合并到代码库后执行的一系列操作，多用来做静态检查、单元测试等，可以尽早发现集成过程中的问题，这里我们用持续集成来自动打包。
* 上传 App：将打好的 App 文件上传到分发平台，这里我们选择[蒲公英](https://www.pgyer.com/)，它是国内提供手机应用内测服务较好的供应商。
* 消息通知：当所有东西都完成后，我们需要及时通知到相关人员，结合笔者平时工作用的聊天工具`钉钉`，我们可以使用钉钉的自定义机器人来发送消息。

PS：在持续集成的过程中，我们根据不同的设备使用了不同的打包策略，Android 使用 Docker + Gradle 的方式，而 iOS 因为强依赖于 Mac 的系统和环境，所以需要单独一台 Mac 机器来做打包机器，详细的打包过程将在下面介绍。

### 工具集

打包过程涉及的工具简单介绍一下：

* gitlab：代码托管平台，类似 [github](https://github.com/)，但可以支持免费内部部署服务，是大部分公司使用的内部代码托管平台。
* gitlab-ci：市面上有很多持续集成的框架，但如果是用 gitlab 来做代码管理的话，使用 gitlab-ci 无疑是最简单的持续集成方式。
* Docker：容器管理工具，可以和大部分持续集成工具集成，使用 Docker 来做持续集成是一种趋势。
* 蒲公英：国内做的比较好的手机内测服务平台，在上面上传 App 后用户就可以进行 App 测试，它对应的竞品是 [Fir.im](https://fir.im/)。
* Mac 机器：这里需要借助 Mac 的机器来执行 iOS App 的打包程序，可以是任意苹果公司的机器，比如 Mac mini、MacBook air/pro 等。
* 钉钉：阿里推出的企业聊天工具，它支持自定义聊天机器人，方便我们定制自动发送消息的功能。

## Android 打包

React Native 工程下面有个`android`目录，里面放置的是 Android 的原生代码，手动打包只需要在该目录下执行`gradlew assembleRelease`命令即可。

但如果要实现自动打包，则需要先做以下准备。

### Docker 镜像

首先要准备一个 Docker 镜像，这个镜像需要可以同时运行 Android 和 React Native 环境，那么问题就来了，我们是要在 React Native 环境的镜像上添加 Android 呢，还是在 Android 环境的镜像上添加 React Native 呢？

运行 Android 需要安装 JDK（Java SE Development Kit）和 Android SDK 等，而运行 React Native 环境只需要安装 Node.js 就可以了，相对于 Android 环境比较简单，所以我们可以找一个 Android 环境的镜像，然后在这个镜像的基础上安装 Node.js。

Docker 镜像可以上 [Docker Hub](https://hub.docker.com) 上查找，如果没有找到合适的，也可以用[这个的 Dockerfile](https://gist.github.com/zhaozhiming/f4a082168f4d82876a50dc6c0ba7e8b5) 来创建。

在本地创建完 Docker 镜像后记得将镜像上传到 Docker Hub，以便下面的步骤可以使用。

### gitlab-ci 配置

接下来我们可以在 gitlab-ci 上配置自动打包的任务了，首先需要在项目中添加一个`.gitlab-ci.yml`文件，这个文件是用来配置 gitlab-ci 持续集成任务，下面是自动打包任务的示例：

{% codeblock lang:yaml gitlab-ci.yml %}
deploy_android: // 任务名称
  stage: deploy
  image: your_docker_react_native_android_image_name // 上面的 Docker 镜像名称
  only:
    - staging // 指定某个分支有代码进入时执行
  script:
    - some scripts // 自动打包命令
{% endcodeblock %}

配置完成后，只要代码库中的`staging`分支有新代码进入，gitlab-ci 就会开始执行自动打包任务，步骤如下：

* 下载 Docker 镜像（有则跳过这一步）
* 使用 Docker 镜像创建容器实例
* 拉取项目最新代码
* 执行任务中的`script`命令

关于`.gitlab-ci.yml`文件的更多信息可以参考[这里](https://docs.gitlab.com/ee/ci/yaml/)。

### 打包

在上面的自动打包任务的示例代码中，`script`属性用来配置任务的执行命令，Android 的打包任务比较简单，首先是安装依赖包，然后执行`gradle`命令就可以了，下面是`script`的示例脚本：

{% codeblock lang:yaml gitlab-ci.yml %}
  script:
    - npm install // 安装依赖包
    - cd android && ./gradlew assembleRelease // 进入 android 目录并执行打包命令
{% endcodeblock %}

### 上传 App

打包命令执行完成后，会在工程目录下生成一个`apk`文件，我们需要找到这个文件并上传到`蒲公英`分发平台，这样其他人才能下载这个 App。

其实 apk 文件也可以放在 gitlab-ci 上，然后让用户通过 gitlab 的链接来下载，但是 gitlab 的权限管理比较严格，每个要下载 App 的人都必须在 gitlab 上授权，这样会比较麻烦，所以我们还是用`蒲公英`这个分发平台比较好。

`蒲公英`提供了[上传 App 的 API](https://www.pgyer.com/doc/api#uploadApp)，调用这个 API 需要提供用户的`uKey`和`api_key`，这 2 个 key 在`蒲公英`的账号设置中可以找到。

{% img /images/post/2019/02/pgyer.png %}

一般使用`curl`命令调用这个 API 就可以了，打包后的 Android apk 文件会在`android/app/build/outputs/apk/release/`这个目录下找到。所以我们任务中的`script`属性可以加上上传的命令：

{% codeblock lang:yaml gitlab-ci.yml %}
  script:
    - npm install // 安装依赖包
    - cd android && ./gradlew assembleRelease // 进入 android 目录并执行打包命令
    - curl -F "file=@android/app/build/outputs/apk/release/app-release.apk" -F "uKey=yourUkey" -F "_api_key=yourApiKey" https://qiniu-storage.pgyer.com/apiv1/app/upload // 上传 App
{% endcodeblock %}

上传 App 成功后，App 会上传到你的`蒲公英`账号下面（因为是通过你的账号的 key 来创建的），把下载链接发给用户，用户就可以直接下载 App 了。

### 打包成功通知

最后一步是提醒相关人员 App 已经打包完成，这里我们利用了`钉钉`的[自定义机器人](https://open-doc.dingtalk.com/docs/doc.htm?treeId=257&articleId=105735&docType=1) 功能。

首先在`钉钉`的工作群里添加一个`自定义机器人`，然后获取到机器人的`Hook 地址`，最后再用`curl`命令调用这个地址就可以发消息了，我们再将这一步添加到`script`属性中：

{% codeblock lang:yaml gitlab-ci.yml %}
  script:
    - npm install // 安装依赖包
    - cd android && ./gradlew assembleRelease // 进入 android 目录并执行打包命令
    - curl -F "file=@android/app/build/outputs/apk/release/app-release.apk" -F "uKey=yourUkey" -F "_api_key=yourApiKey" https://qiniu-storage.pgyer.com/apiv1/app/upload // 上传 App
    - curl 机器人地址 -XPOST -H 'content-type:application/json' -d '{"msgtype":"text","text":{"content":"@13912345678 Android 打包完成，下载地址：https://www.pgyer.com/xxxx"},"at":{"atMobiles":["13912345678"],"isAtAll":false}}' // 利用钉钉机器人通知群里的某人
{% endcodeblock %}

## iOS 打包

iOS 打包的原理和 Android 的差不多，不同的是需要一台 Mac 操作系统的机器来执行，因为需要用到`Xcode`来打包。

### 环境准备

准备好一台 Mac 机器后，我们需要将其集成到 gitlab-ci 来作为执行任务的 Runner，安装步骤可以参照 gitlab 的官方指南：

* [安装`gitlab-runner`](https://docs.gitlab.com/runner/install/osx.html)
* [注册 Runner](https://docs.gitlab.com/runner/register/index.html)
* Runner 的安装及启动：`gitlab-runner install && gitlab-runner start`

完成后可以在 gitlab 的 Runners 界面看到多了一台 Runner 机器。

{% img /images/post/2019/02/runner.png %}

我们可以为其添加 tag 以便执行 iOS 打包任务时可以指定这一台 Runner 来执行。

{% codeblock lang:yaml gitlab-ci.yml %}
deploy_ios: // 任务名称
  stage: deploy
  only:
    - staging // 指定某个分支有代码进入时执行
  tags:
    - ios  // 通过 tags 指定 Runner
  script:
    - some scripts // 自动打包命令
{% endcodeblock %}

### 打包

iOS 的手动打包一般是通过苹果的专用 IDE `Xcode`来完成，操作步骤是：

* 点击`Product`菜单的`Archive`命令完成 App 的归档
* 再通过`Export`命令导出 App 的`ipa`文件

如果要自动打包，我们就不可能像手动打包一样打开`Xcode`了，因为 gitlab-ci 不提供 Runner 的 GUI 界面，但我们可以使用`Xcode`提供的命令行工具`xcodebuild`来进行打包。

新建一个脚本来编写打包命令，如下所示：

{% codeblock lang:sh build_ios.sh %}
# 创建打包 build 目录
mkdir -p ios/build
# 删除之前的打包文件及目录
rm -rf ios/build/archive.xcarchive ios/build/ipa-*

# 打包 app
xcodebuild -scheme project_name -workspace ./ios/project_name.xcworkspace -configuration Release clean build archive -derivedDataPath "./ios/build" -archivePath "./ios/build/archive.xcarchive" -quiet

xcodebuild -exportArchive \
    -archivePath ./ios/build/archive.xcarchive \
    -exportPath ./ios/build/ipa-ad-hoc \
    -exportOptionsPlist ./ci/ad-hoc.plist \
    -allowProvisioningUpdates
{% endcodeblock %}

**注意：**

* `project_name`需要改成真正的 App 名称。
* 构建参数`-quiet` 表示打包过程中减少打印信息，如果不加这个参数，会导致 gitlab-ci 任务上的日志信息过多而失败。
* `ad-hoc.plist` 文件可以在手动打包的导出文件夹中找到，原来的名字是`ExportOptions.plist`，可以根据 App 的类型重命名为`ad-hoc.plist`或`ad-store.plist`。

{% img /images/post/2019/02/plist.jpg %}

脚本写完后，将打包脚本添加到自动打包的任务中：

{% codeblock lang:yaml gitlab-ci.yml %}
  script:
    - npm install // 安装依赖包
    - ./build_ios.sh // iOS 打包
{% endcodeblock %}

### 上传 App

上传 App 和 Android 类似，只是文件的地址要换一下，iOS 生成的 ipa 文件路径是`ios/build/ipa-ad-hoc/app.ipa`。

{% codeblock lang:yaml gitlab-ci.yml %}
  script:
    - npm install // 安装依赖包
    - ./build_ios.sh // iOS 打包
    - curl -F "file=@ios/build/ipa-ad-hoc/app-release.ipa" -F "uKey=yourUkey" -F "_api_key=yourApiKey" https://qiniu-storage.pgyer.com/apiv1/app/upload // 上传 App
{% endcodeblock %}

注意：苹果的`ipa`文件不像 Android 的`apk`文件一样可以直接在手机设备上安装，所以特别需要`蒲公英`这样的平台来进行分发。

### 打包成功通知

通知功能与 Android 相同，这里就不重复介绍了。

## 总结

文章简单介绍了 React Native 的自动打包流程，这个流程是基于笔者的工作开发环境：gitlab + 蒲公英 + 钉钉，但其实这几个部分都是可以替换的，比如用`Jenkins`代替 gitlab-ci，用`fir.im`代替蒲公英，用 `Slack`或者邮件代替钉钉等，读者可以根据自己的工作环境自行替换，也欢迎留言共同讨论交流。

