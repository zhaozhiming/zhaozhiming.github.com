---
layout: post
title: "使用 Filebeat 收集 Docker 容器日志"
date: 2019-09-14 10:53
description: 使用 Filebeat 收集 Docker 容器日志
keywords: filebeat,docker,elasticsearch,kibana
comments: true
categories: code
tags: [filebeat,docker,elasticsearch,kibana]
---

{% img /images/post/2019/09/log-collect.png 400 300 %}

问题：开发人员查看测试环境或者生产环境的日志非常不方便，因为我们服务都是用 Docker 部署的，所以开发人员需要先登录到服务器，然后再登录到某个 Docker 容器才能看到日志。

解决办法：搭建一套日志可视化环境，让开发人员可以通过浏览器直接查看系统各个服务的日志。下面介绍一下如何使用 Filebeat 来收集 Docker 容器日志，并将日志存入 Elasticsearch，再通过 Kibana 来展示。

<!--more-->

## Filebeat vs. LogStash

[Filebeat](https://www.elastic.co/cn/products/beats/filebeat) 是`Elastic`公司的一个轻量型日志采集器，它可以用来代替该公司旗下的 [Logstash](https://www.elastic.co/cn/products/logstash)，也可以和 Logstash 配合一起使用。

Filebeat 和 LogStash 相比十分轻量，体积只有 30 多 M，占用的资源较低，安装也比较简单，可以在每个需要收集日志的服务器上部署运行。

Filebeat 将更多能力放在了数据`收集`上面，而 Logstash 更多的能力是体现在数据的`过滤`和`转换`上。

## EFK 安装

ElasticSearch 和 Kibana 的安装比较简单，可以直接用 Docker 安装的方式，但要注意 Elasticsearch 和 Kibana 的版本需要保持一致，不然 Kibana 可能启动不起来。

Filebeat 的安装方式有多种，可以直接下载安装程序安装，也可以通过 Docker 方式安装，命令如下：

{% codeblock lang:sh %}
docker run -d \
  --name=filebeat \
  --user=root \
  --volume="$(pwd)/filebeat.docker.yml:/usr/share/filebeat/filebeat.yml:ro" \
  --volume="/var/lib/docker/containers:/var/lib/docker/containers:ro" \
  --volume="/var/run/docker.sock:/var/run/docker.sock:ro" \
  docker.elastic.co/beats/filebeat:7.3.1 filebeat -e -strict.perms=false \
  -E output.elasticsearch.hosts=["localhost:9200"]
{% endcodeblock %}

注意这里映射了 Filebeat 配置文件的路径，同时映射了 Docker 容器的文件路径。

也可以使用 Docker-compose 来启动 Filebeat：

{% codeblock lang:sh %}
filebeat:
    image: docker.elastic.co/beats/filebeat:7.3.1
    user: root
    command: filebeat -e -strict.perms=false
    volumes:
      - ./filebeat/filebeat.docker.yml:/usr/share/filebeat/filebeat.yml:ro
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    restart: always
    links:
      - elasticsearch
{% endcodeblock %}

## 基于 Docker 的 Filebeat 配置

使用官方推荐的这个配置模板就够了，我们对 Filebeat 的配置主要体现在具体的容器上，而不是 Filebeat 本身。

{% codeblock lang:sh %}
# 使用这个命令下载 filebeat 配置文件
curl -L -O https://raw.githubusercontent.com/elastic/beats/7.3/deploy/docker/filebeat.docker.yml
{% endcodeblock %}

配置文件内容是这样：

{% codeblock lang:yaml %}
filebeat.config:
  modules:
    path: ${path.config}/modules.d/*.yml
    reload.enabled: false

filebeat.autodiscover:
  providers:
    - type: docker
      hints.enabled: true

processors:
- add_cloud_metadata: ~

output.elasticsearch:
  hosts: '${ELASTICSEARCH_HOSTS:elasticsearch:9200}'
  username: '${ELASTICSEARCH_USERNAME:}'
  password: '${ELASTICSEARCH_PASSWORD:}'
{% endcodeblock %}

## 过滤不需要收集日志的 Docker 容器

按照我们上面的方式启动 Filebeat 后，Filebeat 会自动收集机器上所有 Docker 容器的日志，包括 Elasticsearch，Kibana，Filebeat 这些工具容器的日志，但这些日志可能不是我们所关心的，我们不想让 Filebeat 收集这些容器的日志。

Filebeat 提供了一些 Docker 标签（Label），可以让 Docker 容器在 Filebeat 的`autodiscover`阶段对日志进行过滤和加工，其中有个标签就是可以让某个容器的日志不进入 Filebeat：

* co.elastic.logs/enabled: 日志收集功能是否启动标志，默认值为`true`，设为`false`即为不收集日志。

可以在`docker run`的时候加上这个参数：

{% codeblock lang:sh %}
docker run --label co.elastic.logs/enabled=false ...
{% endcodeblock %}

如果你是用 Docker-compose 启动的话，可以这样写：

{% codeblock lang:yaml %}
filebeat:
    image: docker.elastic.co/beats/filebeat:7.3.1
    labels:
      co.elastic.logs/enable: false
    ...
{% endcodeblock %}

当然更好的方法是在 Filebeat 的配置文件里面配置需要收集日志的容器，但试了几次没找到可以让配置生效的写法，如果有人知道如何配置，也请留言告知，谢谢。

## 统一收集容器中其他路径的日志

Filebeat 默认收集的是 Docker 容器启动后输出到`docker logs`的日志，但有些服务除了这些日志外，还会把其他日志存放到容器的里面。比如`egg.js`框架会把错误日志和一些 web 日志放到`/root/logs/yourserver/`下面。那么要如何收集这些额外的日志呢？

解决方法是把额外日志的路径映射到 Docker 的输出控制台，我们可以在 Dockerfile 里面这样设置：

{% codeblock lang:sh Dockerfile %}
# 将普通日志输出到 stdout
RUN ln -sf /dev/stdout /root/logs/web-server/web-server-web.log
# 将错误日志输出到 stderr
RUN ln -sf /dev/stderr /root/logs/web-server/common-error.log
{% endcodeblock %}

这样容器启动后就会将容器里日志传输到控制台上，而 Filebeat 也就可以收集该日志了。

## 解决多行日志问题

Filebeat 收集日志后将其存入到 Elasticsearch，默认的存储规则是一行日志作为 Elasticsearch 的一条记录，但像错误信息这种日志会出现多行的情况，如果同一个日志分成多行的话查看起来就不太方便，因为中间可能被其他日志打断，而且搜索的时候也不能把同一个错误的日志一并查询出来，所以最好的方式是能将同一个错误的多行日志存入 Elasticsearch 的同一条记录。

Filebeat 考虑到了这种情况，让我们可以通过添加容器便签的方式来设置多行日志的整合规则，比如我们的每条日志都是以时间格式开始的：`2019-01-01 11:11:11,1111 xxxx`，那么我们的多行设置规则就可以这样写：

{% codeblock lang:yaml %}
web-server-dev:
    labels:
      co.elastic.logs/multiline.pattern: '^\d{4}-\d{2}-\d{2}\s\d{2}:\d{2}:\d{2},\d{3}\s'
      co.elastic.logs/multiline.negate: true
      co.elastic.logs/multiline.match: after
{% endcodeblock %}

其中 pattern 表示日志内容如果不匹配这个正则表达式则不能作为新的一行日志，这样日志就会按照时间进行分组了。

## Kibana 建立对应服务的搜索

Filebeat 将日志存入到 Elasticsearch 后，我们就可以通过 Kibana 来查看日志了。在 Kibana 中创建以`filebeat-*`开头的索引，表示查询以`filebeat`开头的索引，这是 Filebeat 导入日志的索引，每天会产生这样的一个索引。

创建完索引后可以通过一些 Docker 的查询条件来查询日志，比如想查询容器名为`foo`的容器的日志，就可以用`container.name=foo`这样的过滤条件来查询，如果想以镜像名来查询也可以，用`image.name=xxx`的查询条件就可以查询出该镜像的所有容器的日志。

展示字段一般只需要`message`字段就可以了，也可以视情况添加`container.name`或`image.name`等字段。

## 总结

上面介绍的 Filebeat 官方推荐的一种收集 Docker 容器的方法，其实还有其他方案，比如说在每个容器里面安装一个 Filebeat 客户端来分别收集各自服务的日志，或者是把每个 Docker 容器的日志路径都映射到宿主机上的某个目录下面，然后在宿主机上安装一个 Filebeat 客户端来统一收集。如果您还有更好的方案，希望可以留言一起讨论分享，谢谢。

## 参考链接

* [访问 Docker 容器日志](https://docs.docker.com/config/containers/logging/)
