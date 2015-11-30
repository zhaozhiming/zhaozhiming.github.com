---
layout: post
title: "gitlab CI runner的创建和配置"
date: 2015-11-30 22:13
description: gitlab CI runner的创建和配置
keywords: gitlab, CI
comments: true
categories: CI
tags: [gitlab, CI]
---

{% img /images/post/2015-11/gitlab-ci-runner.png 400 350 %}  
  
[gitlab][gitlab]不仅是一个代码托管的开源框架，同时它还集成了CI的功能，可以方便地为gitlab上的项目添加CI功能。  
  
<!--more-->  
  
## 创建Runner
  
* Runner服务器  
  
首先要找一台服务器来创建Runner，因为是要跟你的gitlab服务关联，所以服务器要可以访问你的gitlab服务。  
  
* 安装gitlab-CI-multi-runner
  
gitlab-ci-multi-runner是CI runner的运行程序，这里有多种安装方式（[见这里][gitlab-ci-runner-install]），这里我们使用了第一种：在linux中安装软件。

## gitlab-ci-multi-runner命令介绍
  
执行`gitlab-ci-multi-runner help`可以看到所有命令的简介，在每个命令加`--help`可以看到更加具体的参数，比如`gitlab-ci-multi-runner start --help`，命令的执行顺序为：`register(注册runner)-->install(安装服务)-->start(运行服务)`。  
  
{% codeblock lang:sh %}
COMMANDS:
   run          run multi runner service
   register     register a new runner
   install      install service
   uninstall    uninstall service
   start        start service
   stop         stop service
   restart      restart service
   run-single   start single runner
   unregister   unregister specific runner
   verify       verify all registered runners
   help, h      Shows a list of commands or help for one command

GLOBAL OPTIONS:
   --debug                      debug mode [$DEBUG]
   --log-level, -l "info"       Log level (options: debug, info, warn, error, fatal, panic)
   --help, -h                   show help
   --version, -v                print the version
{% endcodeblock %}
  
## gitlab CI配置
  
* 打开网址（比如你的gitlab服务地址是：`http://gitlab.your.company/`，那gitlab CI的地址就是：`http://gitlab.your.company/ci`），找到想要配置CI的项目，点击后面的按钮`Add project to CI`，给项目配置CI功能。  
* 进入CI项目，进入`Runners`标签页面，可以看到CI的url和token，这2个值是待会用命令注册runner时所需要的。  
  
{% codeblock lang:sh %}
How to setup a new project specific runner
Install GitLab Runner software. Checkout the GitLab Runner section to install it
Specify following URL during runner setup: 
http://gitlab.your.company/ci
Use the following registration token during setup: 
7c92da80317b5f5e1fe1c62a1b0767
Start runner!
{% endcodeblock %}
  
* 在runner的服务器上注册runner，执行命令`gitlab-ci-multi-runner register`，下面是执行命令后的交互信息。  
`PS：如果你用的是docker的执行方式，可以先把对应的docker的image下载下来，不然第一次执行CI会比较慢。`  
  
{% codeblock lang:sh %}
root@cloudeye:~# gitlab-ci-multi-runner register
Please enter the gitlab-ci coordinator URL (e.g. https://gitlab.com/ci): ## 输入你CI服务器的地址
http://gitlab.your.company/ci
Please enter the gitlab-ci token for this runner: ## 输入你CI项目的token
7c92da80317b5f5e1fe1c62a1b0767
Please enter the gitlab-ci description for this runner: ## 描述信息，只是表述不是很重要
[your-project]: your-project
Please enter the gitlab-ci tags for this runner (comma separated): ## runner的标签
dev
INFO[0032] fc6e1ee6 Registering runner... succeeded
Please enter the executor: docker-ssh, ssh, shell, parallels, docker: ## runner的执行方式，有5种，这里我选择了docker
docker
Please enter the Docker image (eg. ruby:2.1): ## docker镜像
node:0.12.7
If you want to enable mysql please enter version (X.Y) or enter latest? ## 不需要的话直接空格就可以了 

If you want to enable postgres please enter version (X.Y) or enter latest? ## 不需要的话直接空格就可以了 

If you want to enable redis please enter version (X.Y) or enter latest? ## 不需要的话直接空格就可以了 

If you want to enable mongo please enter version (X.Y) or enter latest? ## 不需要的话直接空格就可以了 

INFO[0043] Runner registered successfully. Feel free to start it, but if it's running already the config should be automatically reloaded!
{% endcodeblock %}
  
* 注册完成后，打开runner的配置文件：`vi /etc/gitlab-runner/config.toml`，可以看到配置文件里面增加了刚才注册的相关信息，更多参数的信息可以看[官方文档][runner-config]。  
  
{% codeblock lang:sh %}
root@cloudeye:~# gitlab-ci-multi-runner register
concurrent = 2

[[runners]]
  url = "http://gitlab.your.company/ci"
  token = "79bf814ac37a52427345b01e135a78"
  name = "your-project"
  executor = "docker"
  [runners.docker]
    image = "node:0.12.7"
    privileged = false
    disable_cache = true
    volumes = ["/cache:/cache:rw"]
{% endcodeblock %}
  
* 安装服务，执行命令`gitlab-ci-multi-runner install -n "服务名"`，后面的服务名是自己定义的名称，用来后面启动命名使用，与其相对的命令是`uninstall`。
* 启动服务，执行命令`gitlab-ci-multi-runner start -n "服务名"`，与其相类似的命令有`stop`和`restart`。
* 验证runner，执行`gitlab-ci-multi-runner verify`，可以看到runner的运行情况。
  
{% codeblock lang:sh %}
root@cloudeye:~# gitlab-ci-multi-runner verify
INFO[0000] 79bf814a Veryfing runner... is alive
INFO[0000] 207a4b34 Veryfing runner... is alive
INFO[0000] 20f849f7 Veryfing runner... is alive
INFO[0000] 6e07e13a Veryfing runner... is alive
INFO[0000] 23be6deb Veryfing runner... is alive
INFO[0000] 4e348964 Veryfing runner... is alive
{% endcodeblock %}
  
* 启动服务后，可以在刚才的CI runners页面看到已经有runner出现了。
  
## gitlab-ci.yaml文件
  
配置好了runner，要让CI跑起来，还需要在项目根目录放一个`.gitlab-ci.yaml`文件，在这个文件里面可以定制CI的任务，下面是简单的示例文件，更多的用法可以看[官方文档][gitlab-ci-yaml]。  
  
{% codeblock lang:yaml %}
efore_script:
  - bundle_install
job1:
  script:
    - execute-script-for-job1
{% endcodeblock %}


[gitlab]: https://gitlab.com/
[gitlab-ci-runner-install]: https://gitlab.com/gitlab-org/gitlab-ci-multi-runner#installation/
[runner-config]: https://gitlab.com/gitlab-org/gitlab-ci-multi-runner/blob/master/docs/configuration/advanced-configuration.md
[gitlab-ci-yaml]: http://doc.gitlab.com/ci/yaml/README.html

