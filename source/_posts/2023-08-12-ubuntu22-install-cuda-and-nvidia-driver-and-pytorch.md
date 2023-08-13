---
layout: post
title: 如何在 Ubuntu 22 上安装 CUDA、NVIDIA 显卡驱动以及 PyTorch
date: 2023-08-12 11:07:05
description: 如何在 Ubuntu 22 上安装 CUDA、NVIDIA 显卡驱动以及 PyTorch
keywords: cuda, nvidia driver, pytorch
comments: true
categories: ai
tags: [cuda, nvidia driver, pytorch]
---

{% img /images/post/2023/08/ubuntu-cuda.png 400 300 %}

虽然现在有很多云厂商都提供了 GPU 服务器，但由于 GPU 的资源稀缺，云 GPU 服务器要么就是价格居高不下，要么就是数量不足无法购买。因此能拥有一块属于自己的 NVIDIA 显卡来跑 AI 程序是最好不过了，虽然现在高端的 NVIDIA 显卡又贵又不好买，但是稍微低端的显卡还是好入手的，随着大模型的配置要求越来越低，在低端显卡上跑一些大模型也不是什么问题。不过即使你拥有了一块自己的 NVIDIA 显卡，但环境配置也是一个麻烦的问题，今天就来分享一下如何在 Ubuntu 22 上安装 CUDA、NVIDIA 显卡驱动以及 PyTorch。

<!--more-->

## 目标

我们的目标是在系统中执行以下命令并返回正确的执行结果（如下图所示）：

- `nvcc --version`: 显示 CUDA 版本
- `nvidia-smi`: 显示 NVIDIA 显卡信息
- python -c "import torch; print(torch.cuda.is_available())": 显示 PyTorch 是否可用

{% img /images/post/2023/08/nvidia-target.png 1000 600 %}

只要这些命令执行正常，那就证明我们的 AI 环境搭建成功了。

## 预安装工作

如果你之前有安装过 NVIDIA 驱动或者 CUDA 程序的话，那么在安装之前需要先卸载之前的驱动和 CUDA 程序，否则可能会导致安装失败。卸载命令如下：

```sh
sudo apt autoremove nvidia* --purge
```

如果你之前是使用 NVIDIA 官方提供的`run`文件安装的驱动和 CUDA 程序的话，还需要使用以下命令进行完整卸载：

```sh
sudo /usr/bin/nvidia-uninstall
# 注意将下面的X和Y替换成你的 CUDA 版本
sudo /usr/local/cuda-X.Y/bin/cuda-uninstall
```

执行完以上命令后，你的系统就变成了一个"干净"的系统，可以开始安装了。

## 安装 CUDA

CUDA（Compute Unified Device Architecture）是 NVIDIA 推出的并行计算平台和编程模型。它允许开发者利用 NVIDIA 的 GPU 来进行高效的并行计算。CUDA 为各种应用程序提供了简化的 API 和工具，从而在 GPU 上加速计算密集型任务，如机器学习、科学模拟和图形处理。

首先进入[NVIDIA CUDA 版本列表页面](https://developer.nvidia.com/cuda-toolkit-archive)，找到你需要的 CUDA 版本，我们的系统是 Ubuntu22.04，CUDA 最早支持 Ubuntu22 的版本是 11.7，因此我们选择 CUDA Toolkit 11.7.1 版本，个人不建议选择太新的版本（比如 12.x 的版本），因为新版本的 CUDA 可能会有兼容性问题。

进入该版本的下载页面，选择对应的操作系统、架构、操作系统、版本，安装方式，就可以看到相应的安装命令。

{% img /images/post/2023/08/cuda-install.png 1000 600 %}

注意上图中最后一步命令，**不要执行`sudo apt-get -y install cuda`命令**，不然会安装最新的 CUDA（12.x 的版本），我们是要安装 CUDA 11.7，安装命令如下：

```sh
wget https://developer.download.nvidia.com/compute/cuda/repos/ubuntu2204/x86_64/cuda-keyring_1.0-1_all.deb
sudo dpkg -i cuda-keyring*.deb
sudo apt-get update
sudo apt-get -y install cuda-11-7
```

安装完成后，我们就可以在终端中直接使用`nvcc`命令了，命令执行结果如下：

```sh
$ nvcc --version

nvcc: NVIDIA (R) Cuda compiler driver
Copyright (c) 2005-2022 NVIDIA Corporation
Built on Wed_Jun__8_16:49:14_PDT_2022
Cuda compilation tools, release 11.7, V11.7.99
Build cuda_11.7.r11.7/compiler.31442593_0
```

## 安装驱动

安装完 CUDA 后，我们继续安装 NVIDIA 显卡驱动程序。

NVIDIA 显卡驱动是一套软件程序，用于使操作系统和计算机硬件与 NVIDIA 图形卡进行通信。它能优化显卡的性能，支持新游戏的图形要求，同时修复已知的错误和兼容性问题。定期更新驱动可以确保显卡的最佳性能和稳定性。

首先检查下显卡的信息，执行以下命令：

```sh
$ lspci | grep -i nvidia

03:00.0 VGA compatible controller: NVIDIA Corporation GM200GL [Quadro M6000] (rev a1)
03:00.1 Audio device: NVIDIA Corporation GM200 High Definition Audio (rev a1)
```

可以看到这台机器的显卡型号是 NVIDIA Quadro M6000 ，然后我们再看操作系统推荐安装的显卡驱动是什么，命令如下：

```sh
$ ubuntu-drivers devices

== /sys/devices/pci0000:00/0000:00:02.0/0000:03:00.0 ==
modalias : pci:v000010DEd000017F0sv000010DEsd00001129bc03sc00i00
vendor   : NVIDIA Corporation
model    : GM200GL [Quadro M6000]
driver   : nvidia-driver-418-server - distro non-free
driver   : nvidia-driver-525 - third-party non-free
driver   : nvidia-driver-390 - distro non-free
driver   : nvidia-driver-520 - third-party non-free
driver   : nvidia-driver-535 - third-party non-free
driver   : nvidia-driver-530 - third-party non-free
driver   : nvidia-driver-470-server - distro non-free
driver   : nvidia-driver-525-server - distro non-free
driver   : nvidia-driver-515 - third-party non-free
driver   : nvidia-driver-535-server - distro non-free
driver   : nvidia-driver-450-server - distro non-free
driver   : nvidia-driver-470 - distro non-free recommended # 这个就是推荐的驱动
driver   : xserver-xorg-video-nouveau - distro free builtin
```

可以看到系统推荐安装的驱动是 `nvidia-driver-470`，然后我们再通过如下命令安装显卡驱动：

```sh
sudo apt install nvidia-driver-470
```

安装完驱动程序后需要重启系统，执行命令`sudo reboot`重启系统，重启完成后再执行`nvidia-smi`查看显卡详细信息：

```sh
$ nvidia-smi

+-----------------------------------------------------------------------------+
| NVIDIA-SMI 470.199.02   Driver Version: 470.199.02   CUDA Version: 11.4     |
|-------------------------------+----------------------+----------------------+
| GPU  Name        Persistence-M| Bus-Id        Disp.A | Volatile Uncorr. ECC |
| Fan  Temp  Perf  Pwr:Usage/Cap|         Memory-Usage | GPU-Util  Compute M. |
|                               |                      |               MIG M. |
|===============================+======================+======================|
|   0  Quadro M6000        Off  | 00000000:03:00.0 Off |                  Off |
| 28%   41C    P8    13W / 250W |     15MiB / 12210MiB |      0%      Default |
|                               |                      |                  N/A |
+-------------------------------+----------------------+----------------------+

+-----------------------------------------------------------------------------+
| Processes:                                                                  |
|  GPU   GI   CI        PID   Type   Process name                  GPU Memory |
|        ID   ID                                                   Usage      |
|=============================================================================|
|    0   N/A  N/A       975      G   /usr/lib/xorg/Xorg                  9MiB |
|    0   N/A  N/A      1164      G   /usr/bin/gnome-shell                2MiB |
+-----------------------------------------------------------------------------+

```

## 安装 PyTorch

我们的目标已经完成了三分之二了，最后我们再来看如何安装 PyTorch。

PyTorch 是一个开源的深度学习框架，由 Facebook 的 AI 研究组开发。它提供了灵活的张量计算工具，支持 GPU 加速，并具有动态计算图特性，使其在研究和原型设计中受到青睐。PyTorch 不仅支持神经网络建模，还提供广泛的 API 和库，适用于机器学习、计算机视觉和自然语言处理任务。

安装 PyTorch 可以使用 Pip 安装，但推荐先使用 Conda 来进行 Python 环境管理，Conda 是一个开源的包管理和环境管理系统，主要用于安装各种软件包和依赖，以及创建、保存和切换多个环境。它支持多种语言，尤其是 Python。Conda 的主要优势在于其能够解决库与依赖项之间的兼容性问题，并允许用户轻松地创建隔离的开发或运行环境，避免软件冲突。Conda 的安装可以看[这里](https://conda.io/projects/conda/en/latest/user-guide/install/index.html)，选择 Miniconda 就可以了。

安装完 Conda 后，我们进入[PyTorch 的页面](https://pytorch.org/)，在`INSTALL PYTORCH`章节选择 PyTorch 版本、操作系统、安装方式、编程语言、CUDA 版本，然后就可以看到 PyTorch 的安装命令了。

{% img /images/post/2023/08/pytorch-install.png 1000 600 %}

安装完后执行命令显示 PyTorch 是否可用：

```sh
$ python -c "import torch; print(torch.cuda.is_available())"
True
```

有时候我们安装了 CUDA 和 PyTorch 后，执行以上命令仍显示`Flase`（不可用），系统会提示`Torch not compiled with CUDA`的错误，这是因为 CUDA 和 PyTorch 的版本不兼容导致的，我们可以通过以下命令查看 CUDA 和 PyTorch 的版本：

```sh
$ nvcc --version # 查看 CUDA 版本
...
Build cuda_11.7.r11.7/compiler.31442593_0
$ python -c "import torch; print(torch.__version__)" # 查看 PyTorch 版本
2.0.1+cu117
```

如果 CUDA 和 PyTorch 的版本不兼容，那么我们就需要重新安装 PyTorch，需要先卸载老的 PyTorch 版本：

```sh
pip uninstall torch torchvision torchaudio
```

卸载完成后，重新按照上面的方法安装 PyTorch 即可。

## 总结

刚接触 AI 环境部署时，可能很多人对这些概念不是很清楚，比如 CUDA、NVIDIA 驱动、PyTorch 等，在部署过程中可能也会遇到诸多问题，但只要慢慢了解这些概念，同时多部署几次，就会熟悉其中的操作，希望这篇文章可以帮助你快速的熟悉 AI 环境的部署，如果你有什么问题，欢迎在评论区留言讨论。

关注我，一起学习各种人工智能和 AIGC 新技术，欢迎交流，如果你有什么想问想说的，欢迎在评论区留言。

## 参考资料

- [How to Install NVIDIA Drivers on Ubuntu 22.04 | 20.04](https://www.linuxcapable.com/install-nvidia-drivers-on-ubuntu-linux/)
- [Installing any version of CUDA on Ubuntu and using Tensorflow and Torch on GPU](https://medium.com/analytics-vidhya/installing-any-version-of-cuda-on-ubuntu-and-using-tensorflow-and-torch-on-gpu-b1a954500786)
- [How to remove cuda completely from ubuntu?](https://stackoverflow.com/questions/56431461/how-to-remove-cuda-completely-from-ubuntu)
- [NVML driver/library mismatch after libnvidia-compute update](https://stackoverflow.com/questions/62250491/nvml-driver-library-mismatch-after-libnvidia-compute-update)
