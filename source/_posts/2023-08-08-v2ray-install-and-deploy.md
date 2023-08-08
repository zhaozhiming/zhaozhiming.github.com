---
layout: post
title: 新版V2Ray安装及部署
date: 2023-08-08 14:30:19
description: 新版V2Ray安装及部署
keywords: V2Ray,gfw
comments: true
categories: ai
tags: [V2Ray, gfw]
---

{% img /images/post/2023/08/cross-wall.jpg 400 300 %}

年初在 Azure 的网站上看到免费试用的宣传，宣传内容包括新建账号送 200 美元，可以免费试用 12 个月等等优惠活动，于是便在上面创建了一台 VPS 并搭建好了梯子，白嫖了一段时间后，最近发现 Azure 服务器突然开始收费，每个月要收 10 美元，要花这个钱我还不如去其他云厂商买服务器，上网查了一下说是可能免费额度的金额用完了，或者是创建的 VPS 中使用的其他配件不符合免费计划的要求。总之为了以后不再被收费，我便将 Azure 上的相关组件全部删除了。重新在其他云厂商上购买了一台服务器，重新搭建了梯子，但发现新建的梯子无法使用，于是便开始了一番折腾。

<!--more-->

## 购买服务器

这里推荐[AWS Lightsail](https://aws.amazon.com/cn/lightsail/)，它是一种虚拟私有服务器 (VPS) 解决方案，旨在为那些需要简单、预先配置好的计算资源的用户提供简单、快速、经济实惠的方式。LightSail 设计得非常简单，对于那些刚接触云计算或不需要所有 AWS 的高级功能的用户来说非常适用。

Lightsail 使用也很简单，它的管理界面非常简洁，比 AWS 其他类型的服务器界面要清爽很多，只有几个选项，可以快速创建实例和进行实例管理，非常适合新手使用。

进入创建实例界面后，选择以下实例配置：

- 选择实例位置：有美国、东京、首尔、伦敦、巴黎等实例位置；
- 选择平台：有 Linux 和 Windows 两种平台，一般选择 Linux；
- 选择实例镜像：可以通过应用来选择，比如 WordPress、LAMP 等，也可以通过操作系统来选择，比如 AWS 内置的 Linux 操作系统，Ubuntu、CentOS 等；
- 选择实例计划：最低的配置是 3.5 美元/月，前 3 个月还免费，如果只是用来搭梯子，选择最低配置即可。

选择完后进行服务器创建，等个 1-2 分钟就创建完成了。可以使用页面上的`使用SSH连接`按钮来连接服务器，进入后将自己电脑上的公钥复制到服务器上，然后就可以使用`ssh ubuntu@服务器ip`来连接服务器了。

{% img /images/post/2023/08/aws-ssh.png 1000 800 %}

**注意 Lightsail 的服务器无法通过 ping 命令来测试是否能够连接，因为服务器上禁止了 ICMP 协议，所以只能通过 ssh 命令来连接。**

在`联网`的 Tab 页中可以看到服务器的公网 IP 地址，这个地址会在重启后发生变化，如果你不想这个地址发生变化，可以添加`静态IP`，这样重启后 IP 地址就不会发生变化了，这个服务在 Lightsail 中是免费的。

{% img /images/post/2023/08/aws-ip.png 1000 800 %}

如果你想搭建的梯子在客户端上可以访问，建议将 TCP 和 UDP 的端口都设置成可以访问。

{% img /images/post/2023/08/aws-rule.png 1000 800 %}

当然你也可以选择其他的云厂商，比如国内的阿里云、腾讯云等，或者国外的 Google Cloud、DigitalOcean 等，为了更好地科学上网，我的建议是**不要把所有鸡蛋都放在同一个篮子里**，即在不同的云厂商上购买服务器，保证自己同时有 2-3 台 VPS，这样即使有一台服务器被封，还有其他的服务器可以使用。

## 安装 V2Ray

之前安装 V2Ray 都是通过[这个仓库](https://github.com/233boy/v2ray)的脚本进行一键安装。

执行如下面命令（注意要用 root 用户执行）：

```sh
bash <(curl -s -L https://git.io/v2ray.sh)
```

安装完后验证是否安装成功，显示`running`即表示正常：

```sh
systemctl status v2ray
```

### 获取 vmess url

通过命令`v2ray url`获取到 vmess 地址(新版的会在安装完成后立即显示该地址），这个地址可以在客户端进行导入。

以 Mac 的客户端[V2RayX](https://github.com/Cenmrev/V2RayX)为例，在菜单中选择`config`并点击其中的`import`按钮选择`import from other links...`，将 vmess 地址粘贴在里面即可。

## 解决新版 V2Ray 无法使用的问题

按照以上的方法安装完 V2Ray 后，会发现客户端无法使用，通过网上搜索发现是我们使用的 V2Ray 安装脚本升级了 V2Ray 的内核版本，导致一些老的客户端无法使用，解决办法有以下几种：

- 服务端使用安装脚本的老版本进行安装，安装命令如下：

```sh
wget https://github.com/233boy/v2ray/archive/old.tar.gz -O v2ray-old.tar.gz;tar -zxvf v2ray-old.tar.gz;cd v2ray-old;chmod +x i*;./i* local
```

- 服务端还是使用安装脚本的新版安装，但安装完后将服务端的 V2Ray 版本降级到旧版本，执行命令`v2ray update core 4.45.2`即可降级。
- 升级客户端版本（这一点对于一些很久没更新的客户端来说比较困难）

更多的信息可以参考这个[Issue](https://github.com/233boy/v2ray/issues/1101)。

## 修改 V2Ray 协议

为了你的梯子活得更加长久，建议修改 V2Ray 的协议，V2Ray 的默认协议是 tcp，我之前也是默认使用这个协议，但发现 Lightsail 的服务器隔几天就无法访问了，只有重启服务更换 IP 才恢复正常。
后面我将协议改成了 ws，就再也没有出现过这个问题了，所以建议大家也将协议改成其他协议。
V2Ray 修改协议非常简单，只需要执行`v2ray`命令，然后选择`2. 修改 V2Ray 配置`，再选择`2. 修改 V2Ray 传输协议`，最后选择`3. WebSocket`即可。

## 添加 CloudFlare WARP

如果你经常访问 ChatGPT 的话，会发现你的 VPS 用着用着就访问不了 ChatGPT 了，这是因为 ChatGPT 的服务器会检测你的 ip，如果是 VPS 的 ip 就会禁止访问，这时候就需要使用 CloudFlare WARP 来解决这个问题。

### 安装仓库 GPG key

```sh
curl https://pkg.cloudflareclient.com/pubkey.gpg | gpg --yes --dearmor --output /usr/share/keyrings/cloudflare-warp-archive-keyring.gpg
```

### 添加 CloudFlare WARP 客户端源

```sh
echo 'deb [arch=amd64 signed-by=/usr/share/keyrings/cloudflare-warp-archive-keyring.gpg] https://pkg.cloudflareclient.com/ bullseye main' | tee /etc/apt/sources.list.d/cloudflare-client.list
```

### 安装 CloudFlare WARP 客户端

```sh
apt update
apt -y install cloudflare-warp
# 查看状态
systemctl status warp-svc
# 注册客户端
warp-cli register
# 设置 WARP 为代理模式(很重要，否则您将无法远程连接 VPS)
warp-cli set-mode proxy
```

### 启动连接

WARP 将启动 socks5 本机代理 127.0.0.1:40000。

```sh
warp-cli connect
# 保持连接
warp-cli enable-always-on
# 查看设置
warp-cli settings
# 查看连接是否成功
warp-cli warp-stats
```

### 配置 V2Ray

修改 V2Ray 的配置文件`/etc/v2ray/config.json`

```json
{
  ...,
   "outbounds": [
    ...,
    {
      "tag": "warp",
      "protocol": "socks",
      "settings": {
        "servers": [
          {
            "address": "127.0.0.1",
            "port": 40000,
            "users": []
          }
        ]
      }
    }
    ...
   ],
   "routing": {
     "rules": [
        {
          "type": "field",
          "domain": [
              "openai.com",
              "ai.com",
          ],
          "outboundTag": "warp"
       }
      ]
   },
  ...
}
```

主要更改内容：

- outbounds 增加一个 warp 出口
- routing 规则增加 openai 规则；

最后重启 V2Ray 就可以了，再访问 ChatGPT 发现可以正常访问了。

## V2Ray 客户端

V2Ray 客户端有很多，这里只介绍几个常用的。

- Mac 客户端：[V2RayX](https://github.com/Cenmrev/V2RayX)
- Android 客户端: [V2RayNG](https://github.com/2dust/v2rayNG)

更多客户端详可以看[这里](https://www.v2ray.com/en/awesome/tools.html)。

## 总结

经过一番折腾，终于搭建好了自己的梯子，现在可以愉快的科学上网，也可以正常访问 ChatGPT 了，如果你也想搭建自己的梯子，可以参考本文的方法，如果你有更好的方法，欢迎在评论区留言。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。
