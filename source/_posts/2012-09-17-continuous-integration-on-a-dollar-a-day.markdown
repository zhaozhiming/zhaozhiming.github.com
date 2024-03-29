---
layout: post
title: '持续集成一天一美元'
date: 2012-09-17 21:53
comments: true
categories: translate
tags: ci
---

原文：[http://jamesshore.com/Blog/Continuous-Integration-on-a-Dollar-a-Day.html](http://jamesshore.com/Blog/Continuous-Integration-on-a-Dollar-a-Day.html)

有一种持续集成，比使用像 CruiseControl 这样的构建服务来的更容易更便宜。实际上，它是如此简单，你可以从现在开始做这件事，不用为你还没有构建服务而感到不舒服。

<!--more-->

（肮脏的小秘密？我将要告诉你的是比使用 CruiseControl 更好的东西!）

### 第一步：找一台老的电脑

找一台你以前用来做开发的电脑，不要太老...它需要用来跑构建程序。找一个没用的显示器和一个废弃的角落，把它连接起来，放一张破旧的椅子在前面，不需要太舒适...你不会想这里坐太久。

### 第二步：找一只橡皮鸡(_不是真的鸡_)

{% img http://www.jamesshore.com/Blog/usbchicken.jpg %}  
_我的办公室，大概在 2001 年_

你如果想要也可以使用其他东西，比如毛绒绒的玩具。玩具要搞笑、没有菱角，这样你用力把它扔向某人时不会不小心伤害到别人的眼睛（特别是你误中他人的时候）。如果你没有合适的东西，不要让这一步搁置你的下一步行动。即兴创作，有趣就行。

我想[这个](http://www.cs.cmu.edu/afs/cs.cmu.edu/user/sprite/www/Origami/crane_gif.html)有助于即兴创作，但我做不来，所以你不会也没关系。

### 第三步：买一个桌铃

那种你轻拍就会发出“叮”的铃铛。不要因为你没有一个桌铃就停止做持续集成，可以后面再做这一步。现在你已经有开始的势头了。旧电脑，就绪。插线，就绪。搞笑玩具，就绪。你离开始做持续集成已经越来越近了（如果你真的没钱，你可以完全跳过这一步（译者注：指桌铃））。

### 第四步：让你的构建自动化

噢！这是最难的一步。好消息是 CruiseControl 的好处之一就是可以使你的构建自动化。更好的消息是你接下来要做的持续集成，可以做的事情比 CruiseControl 的自动化构建要多，并且我将帮你获得所有的这些好处。但你还是需要让你的构建自动化。

好吧，这个很难，我知道。如果你已经依赖你的 IDE 为你构建，让构建自动化可能意味着要做很多的工作。现在，你可能要创建一个批处理文件来跑你的 IDE 并要求它来构建。程序运行的时间太长不是件好事，所以还是回来做正确的事情吧。

如果你已经有了自动化检查的单元测试，比如 JUnit/NUnit 测试，也将它们放到构建里面。

在你继续之前，走向那台旧电脑（见第一步），然后确认你的版本库中的最新代码可以成功构建。没有使用任何版本控制工具？额...好吧...放下键盘然后离开电脑。现在，跟我读：“原谅我，老天爷，我错了。我又一次没有使用版本控制来编程。我会马上下载 TortoiseSvn（译者注：现在当然是 git 了），安装使用它。从今以后我不会再犯了。”谢谢。

如果一个干净运行的自动化构建超过 10 分钟，那么请停下来。你还没为持续集成做好准备，你需要加速你的构建。你可以做接下来的步骤，或者你可以使用 CruiseControl，但真正的持续集成现在还不属于你。

### 第五步：洗脑

这在列表中绝对，毫无疑问，100%是最重要的一步。让你团队中的所有开发人员一起去到一个房间。

如果有人问，不，这不是开会。你将要在 5 分钟内完成某件事情。有用，简短，所以这不是一个会议。

现在，在保证没有动用武力胁迫的情况下，让每个人都同意下面的话：

{% blockquote %}
“从现在开始，我们版本库中的代码将一直构建成功并且可以跑过所有测试。”
{% endblockquote %}

如果有人抱怨这个太难了，让他们知道，通过持续集成来做这个很简单。额，应该说更简单。如果他们仍然认为这太难了，可以委婉地提醒他们的工作，你知道，构建软件。

噢，太残酷了，还是不要说的好。啊，我刚失去了 10 个粉丝，呀，又一个，11 个。

实际上，“每个人都同意这是个好主意”的部分真的非常重要。你看，能够一直依赖你的软件构建，是持续集成的革命性部分。想象一下如果你知道你刚从版本库上下下来的代码可以工作，你的生活会变得多轻松。

让我来讲一个小故事。我维护开源软件的一块叫 NUnitAsp。去年，我开了一门课来专门将它。在讲课期间，有人要求增加一个 NUnitAsp 没有的功能。我看了下代码发现很容易就可以增加。所以我做了修改（花了几分钟）。然后我跑我的构建，96 秒后有个新的发布文件，然后把它拿出来。真实的故事，我们甚至有一个编程奖品，我们叫它“找 bug 得杯具”（我们人很好，即使没有找到 bug 的人我们也给他杯具）。

好了，你的持续集成可能还没到那一步。你需要像我的那个项目一样有很好的自动化测试来构建和发布。所以让我来讲另外一个故事。在另外一个项目，没有那么成功，我们所有人在代码的不同部分上工作。虽然我们每天都很小心的检入代码，但我们没有构建整个工程或跑测试。（测试？我们没有任何自动化测试。）六个月以后，我们尝试去集成，但没有东西可以组装在一起。我们花费了一个星期才让程序跑起来。我甚至不想描述在这个过程中有多少个 bug 了。持续集成，即使没有好的测试，也意味着你将再次不会面对这种噩梦了。

我讨厌试图去说服人们相信这是个好主意。我告诉你要刷牙了吗？但你还是会每天做。这个对你有好处。不想做？那就不要做！不是我的问题。

12...13...14，15，16...妹的。（译者注：指粉丝流失）

不然怎样，如果没有让每个人都同意这样做，那么这个过程将不会有效。你还能指望什么？

###第六步：启动！

在[这里](http://jamesshore.com/Blog/Continuous-Integration-Checklist.html)看到可打印的清单。

你已经准备好了！让我们浏览一下预启动清单：

1. 构建电脑？就绪。
2. 搞笑玩具？就绪。
3. 烦人的闹铃？可选。
4. 自动构建？就绪。
5. 团队同意？就绪。

现在，让我们开始吧！

首先，每天至少检入代码 2 次。这是“持续”的部分。当你熟练以后，你将每 1~2 个小时检入一次。

在获取版本库的最新代码之前，看下有没有人手里有橡皮鸡。如果有，先等他们把代码检入。

当你检入时，遵循以下步骤：

A 在本地跑构建/测试脚本，确认 100%通过。  
B 把橡皮鸡从它原来的地方拿过来。如果橡皮鸡不在原地，找一下看在谁那里，然后一直烦着他们直到他们检入代码。  
C 从版本库下载最新代码然后再次运行构建脚本。如果没有运行成功，你知道刚下的代码有问题。发发牢骚抱怨一下，然后找到最后检入代码的那个人帮你解决问题。解决问题后重新开始。  
D 检入你的代码。  
E 走向那台破旧的构建电脑，从版本库获取最新代码，再次运行构建脚本。如果没有运行成功，将你检入的代码还原。没成功的原因，可能是你安装了一些新的软件，或者修改了一个环境变量，或者设置注册表环境，或者忘记添加文件到版本库，或者其他原因。不管怎样，你需要在你的电脑上修复这个问题然后重新试一次。你可以暂时保留橡皮鸡，但如果其他人需要你要还回去（然后重新开始）。  
F 让铃铛响起来。（其他人会为你鼓掌，或以其他方式为你高兴。）把橡皮鸡放回去，你已经做完了。

顺便说一下，当步骤 E 失败了，你可能会冒险在构建机器上直接修复问题，但如果你这样做了，下一个可怜的家伙下载了最新代码后将会构建失败。

最后，但并非最不重要：保持你的构建时间在 10 分钟之内，少于 5 分钟更好。如果运行时间太长，这个过程将不再是一件愉快的事情而是痛苦的开始。而且一般来说快速构建对你有好处，不管怎样，因为速度慢的构建往往意味着测试方法的缺陷。

### 为什么比 CruiseControl 好？

1. 代码在版本控制中总是周期性地可以构建和通过测试。
2. 如果出错了，你知道问题出在哪里。要么是你的代码（在步骤 A 失败了），要么是其他人的代码（在步骤 C 失败了），或者是环境改变了（在步骤 E 失败了）。反正你可以找到原因，让修复问题更简单。
3. 当其他人没有等 CruiseControl 跑完构建就跑去吃午饭时，你不用再去修复其他人引起的构建失败。
4. 你会让构建运行的时间保持简短（长时间运行的构建会让你痛苦），这意味着测试写得更好，使设计更佳。

### 高级课程

一旦你让基础设施工作起来了，你可以继续真正完善你的持续集成的使用。一个可选的做法是让你的构建独立起来，换句话说，你构建所需的所有东西就只是你的源控制，一旦你得到代码了，你就可以断开网络连接去构建。这个好处是让你的构建更可信，允许你轻易地去构建老版本。这也有助于找出数据库配置和迁移的错误。

真正给力的测试也是一个好的选择。如果你真的拥有给力的测试，那么你可以盖着帽子好好休息了。

我同样喜欢让我的构建脚本来构建一个安装包。人们经常从他们的测试和集成过程中拿掉安装程序，然后忍受花费额外时间来构建一个安装包。这是快速构建带来的众多好处之一，让你做这件事情更容易，虽然自动测试安装程序是一件痛苦的事情。

嗯...我讨厌承认...安装 CruiseControl 也是一个好主意，等你从高级课程毕业了再接触比较好。而现在，你已经真正做到熟悉（持续集成）基础（团队同意，快速测试，永不破坏的构建），你已经不大可能回到坏实践中去了。

最后祝你好运！别忘了给我一美元。

### 相关材料：

- [Continuous Integration](http://www.martinfowler.com/articles/continuousIntegration.html) (Martin Fowler)
- [Red-Green-Refactor](http://jamesshore.com/Blog/Red-Green-Refactor.html) (James Shore)
- [Continuous Integration is an Attitude, Not a Tool](http://jamesshore.com/Blog/Continuous-Integration-is-an-Attitude.html) (James Shore)
- [Automated Continuous Integration and the Ambient Orb](http://blogs.msdn.com/mswanson/articles/169058.aspx) (Michael Swanson)
