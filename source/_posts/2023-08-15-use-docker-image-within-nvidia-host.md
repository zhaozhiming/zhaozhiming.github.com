---
layout: post
title: 使用 Docker 部署 AI 环境
date: 2023-08-15 14:18:27
description: 使用 Docker 部署 AI 环境
keywords: ubuntu, docker, cuda, nvidia, pytorch
comments: true
categories: ai
tags: [ubuntu, docker, cuda, nvidia, pytorch]
---

{% img /images/post/2023/08/nvidia-docker.png 400 300 %}

之前给大家介绍了主机安装方式——[如何在 Ubuntu 操作系统下安装部署 AI 环境](https://zhaozhiming.github.io/2023/08/12/ubuntu22-install-cuda-and-nvidia-driver-and-pytorch/)，但随着容器化技术的普及，越来越多的程序以容器的形式进行部署，通过容器的方式不仅可以简化部署流程，还可以随时切换不同的环境。实际上很多云服务厂商也是这么干的，用一台带有 NVIDIA 显卡的机器来部署多个容器，然后通过容器的方式来提供给用户使用，这样就可以充分利用显卡资源了。今天给大家介绍一下如何使用 Docker 的方式来部署我们之前部署过的 AI 环境。

<!--more-->

## 目标

我们可以跟之前一样制定一个小目标：

- 在 Docker 容器中可以正常执行`nvidia-smi`命令
- 在 Docker 容器中可以正常执行`python -c "import torch; print(torch.cuda.is_available())"`命令

好奇的同学可能会问，为什么不在 Docker 容器中执行`nvcc --version`命令呢？这个问题留到后面再解释。

## 预安装工作

我们还是以 Ubuntu22.04 操作系统为例，首先我们要安装 Docker，这里就不再赘述了，可以参考[官方文档](https://docs.docker.com/engine/install/ubuntu/)进行安装。

Docker 安装完成后，如果你想以非 root 用户执行`docker`命令的话，还需要将当前用户添加到`docker`用户组中，命令如下：

```sh
# 先确保 docker 用户组已存在
sudo usermod -aG docker <user-name>
```

## 安装 NVIDIA 驱动

安装 NVIDIA 驱动的步骤可以参考[之前的文章](https://zhaozhiming.github.io/2023/08/12/ubuntu22-install-cuda-and-nvidia-driver-and-pytorch/)，这里就不再赘述了。

## 安装 NVIDIA Container Toolkit

NVIDIA Container Toolkit 允许开发者和用户将 NVIDIA GPU 的能力无缝地加入到 Docker 容器中。通过简单地安装一个插件，用户就可以运行为 GPU 优化的容器，无需进行任何修改。该工具集包括 NVIDIA 驱动、CUDA 等必要组件，确保 GPU 在容器环境中的高性能执行，对 AI、数据分析和高性能计算场景特别有用。

我们可以通过以下命令来安装 NVIDIA Container Toolkit：

```sh
# 先切换到 root 用户
curl -s -L https://nvidia.github.io/nvidia-docker/gpgkey | apt-key add -
# 添加源
curl -s -L https://nvidia.github.io/nvidia-docker/ubuntu22.04/nvidia-docker.list > /etc/apt/sources.list.d/nvidia-docker.list
# 更新源
apt update
# 安装 NVIDIA Container Toolkit
apt -y install nvidia-container-toolkit
# 重启 Docker 服务
systemctl restart docker
```

安装完成后，系统会一并安装 NVIDIA Container Toolkit 的 CLI 命令（nvidia-ctk），我们可以运行该命令确认安装是否成功：

```sh
$ nvidia-ctk --version
NVIDIA Container Toolkit CLI version 1.13.5
commit: 6b8589dcb4dead72ab64f14a5912886e6165c079
```

然后我们就可以在 Docker 容器中运行`nvidia-smi`命令，验证是否可以在 Docker 容器中正常使用 NVIDIA 显卡了，我们下载一个简单的镜像来进行验证：

```sh
$ docker run --rm --gpus all nvidia/cuda:11.7.1-base-ubuntu22.04 nvidia-smi

Wed Aug 16 03:04:19 2023
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 470.199.02   Driver Version: 470.199.02   CUDA Version: 11.7     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  Quadro M6000        Off  | 00000000:03:00.0 Off |                  Off |
| 28%   38C    P8    13W / 250W |     15MiB / 12210MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
+-----------------------------------------------------------------------------+
```

可以看到，我们在 Docker 容器中可以正常使用 NVIDIA 显卡了。

## 下载 Docker 镜像

要运行 AI 环境，一般需要安装 CUDA 和 PyTorch，之前我们是在主机上安装这 2 个程序，但使用 Docker 的方式，我们可以直接下载已经安装好 CUDA 和 PyTorch 的镜像，这里推荐使用这个镜像：[`anibali/pytorch`](https://hub.docker.com/r/anibali/pytorch)，这个镜像中包含了 CUDA 和 PyTorch，我们可以通过以下命令来运行镜像：

```sh
$ docker run --rm --gpus all anibali/pytorch:1.13.0-cuda11.8-ubuntu22.04 nvidia-smi

Wed Aug 16 03:09:33 2023
+-----------------------------------------------------------------------------+
| NVIDIA-SMI 470.199.02   Driver Version: 470.199.02   CUDA Version: 11.8     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  Quadro M6000        Off  | 00000000:03:00.0 Off |                  Off |
| 28%   38C    P8    13W / 250W |     15MiB / 12210MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
+-----------------------------------------------------------------------------+
```

可以看到 CUDA 版本显示的是 11.8，跟 Docker 镜像中的 CUDA 版本是一致的。

我们再来看在 Docker 容器中是否可以正常执行`python -c "import torch; print(torch.cuda.is_available())"`命令：

```sh
# 进入 Docker 容器
$ docker run -it --rm --gpus all anibali/pytorch:1.13.0-cuda11.8-ubuntu22.04 bash
$ user@32e8c83f88c3:/app$ python -c "import torch; print(torch.cuda.is_available())"
True
```

通过结果可以证明，容器中的 CUDA 和 PyTorch 程序可以正常使用。

### 为什么不在 Docker 容器中执行`nvcc --version`命令？

回到原先那个问题，为什么不在 Docker 容器中执行`nvcc --version`命令呢？我们可以在 Docker 容器中执行`nvcc --version`命令，看看会发生什么：

```sh
user@32e8c83f88c3:/app$ nvcc --version
bash: nvcc: command not found
```

发现容器中找不到`nvcc`命令，但是 CUDA 又是可以正常访问，再看 CUDA 的安装目录：

```sh
user@32e8c83f88c3:/app$ ls /usr/local/cuda*
/usr/local/cuda:
compat  lib64  targets

/usr/local/cuda-11:
compat  lib64  targets

/usr/local/cuda-11.8:
compat  lib64  targets
```

可以看到在 CUDA 安装目录中并没有`bin`文件夹（一般`nvcc`命令会放到这个文件夹里面），这是因为有些 Docker 镜像为了节省资源，会将一些不需要的文件去掉，只保留最核心的文件，已达到减小 Docker 镜像大小的目的。

## 总结

Docker 安装与主机安装的方式相比，我们少安装了 CUDA 和 PyTorch 程序，多安装了`NVIDIA Container Toolkit`和下载 Docker 镜像，但整体花费的时间其实是减少了的（因为 CUDA 和 PyToch 安装时间比较长）。Docker 安装最大的好处就是可以随时切换不同的环境，而且不会影响到主机的环境。比如我们今天安装了 CUDA 11.x 的版本，后面程序升级了可能需要安装 CUDA 12.x 的版本，如果是主机安装的话，就要卸载老的 CUDA 再重新安装新的 CUDA，这样就比较麻烦了，但是如果是 Docker 安装的话，我们只需要下载新的 Docker 镜像就可以了，非常方便。希望今天的分享可以帮助大家更好的使用 Docker 来部署 AI 环境，如果有任何疑问，欢迎在评论区沟通讨论。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 参考资料

- [How to Use the NVIDIA GPU in Docker Containers on Ubuntu 22.04 LTS](https://linuxhint.com/use-nvidia-gpu-docker-containers-ubuntu-22-04-lts/)
- [NVIDIA Container Toolkit Installation Guide](https://docs.nvidia.com/datacenter/cloud-native/container-toolkit/latest/install-guide.html)
