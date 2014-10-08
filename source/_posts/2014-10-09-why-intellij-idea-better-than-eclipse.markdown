---
layout: post
title: "为什么Intellij-IDEA比Eclipse好"
date: 2014-10-09 02:36
description: 为什么Intellij-IDEA比Eclipse好
keywords: intellij-idea,eclipse
comments: true
categories: code
tags: [intellij-idea,eclipse]
---
  
{% img /images/post/2014-10/idea-vs-eclipse.jpeg %}  
  
经常有人问我Intellij IDEA比Eclipse好用在哪里？问我的人大部分都是没用过IDEA的，因为用过IDEA的大部分人都知道好在哪里。IDEA和Eclipse之争不像Vim vs. Emacs、IOS vs. Android、Java vs. C++等，因为后面这些`vs`都没有绝对的优胜者，都各自有各自的优缺点，但IDEA vs. Eclipse是有结果的，那就是:  
  
{% blockquote %}
Intellij IDEA明显比Eclipse好...很多。
{% endblockquote %}
  
<!--more-->
## 为什么我从Eclipse转向IDEA
我在开始接触Java的时候就使用Eclipse，用了将近6~7年，那个时候几乎熟悉了Eclipse的所有快捷键，当时感觉用Eclipse写Java，JSP神马的都挺方便的。后来我加入一个新项目，项目强制要求我们使用IDEA做为开发工具，使用一段时间后发现IDEA各方面都比Eclipse强大，让你写Java代码更加行云流水，我从此欲罢不能的爱上IDEA，一直使用至今。因为这两个IDE我都使用过蛮长时间，所以还是可以讲讲两者的一些不同。
  
## IDEA的优点
IDEA的优点有很多，在开始使用到慢慢熟悉的过程中，基本每隔一段时间你就会被IDEA的一些神奇功能震惊到，经常让你惊喜不断，从而慢慢爱上它。  
  
#### 自动补全 
IDEA的自动补全很强大，不仅仅是Java代码可以补全，还可以补全其他代码比如Html，JavaScript等，敲打每个字母IDEA都会马上列出各种可能需要补全的代码。  
  
比如下面这段代码:  
  
{% codeblock lang:java %}
	@Test
	public void should_() throws Exception {
		Assert.null
	}
{% endcodeblock %}     
  
IDEA会提示assertNull, assertNotnull等方法，而Eclipse则完全没有提示。  

{% img /images/post/2014-10/idea-auto-complete-2.png idea %}
  
  
{% img /images/post/2014-10/eclipse-auto-complete-2.png eclipse %}  
  
又比如下面这段代码:  
 
{% codeblock lang:java %}
	@Test
	public void should_() throws Exception {
		assertThat();
	}
{% endcodeblock %}   
  
IDEA按下`alt + Enter`IDEA会自动提示你是否要静态导入`assertThat`这个方法，而Eclipse按`ctrl + 1`只会提示你创建新方法。  
  
{% img /images/post/2014-10/idea-auto-complete.png idea %}
  
  
{% img /images/post/2014-10/eclipse-auto-complete.png eclipse %}  
  
IDEA不仅对Java有自动补全，对其他类型的文件也有自动补全的功能，而Eclipse我只能呵呵了。  
  
{% img /images/post/2014-10/idea-auto-complete-xml.png %}
  
  
{% img /images/post/2014-10/idea-auto-complete-html.png %}
  
#### 重构
IDEA从一开始就拥有很强大的重构功能，而Eclipse以前基本上没有什么重构的功能，后面才慢慢加上的。  
  
比如我们要抽取下面name和age那2行为一个方法。  
  
{% codeblock lang:java %}
	@Test
	public void should_() throws Exception {
		String name = "Tom";
		int age = 11;
		
		System.out.println(name + age);
	}
{% endcodeblock %}   
  
IDEA可以自动将其封装成为一个对象。
  
{% img /images/post/2014-10/idea-refactor-1.png %}
{% img /images/post/2014-10/idea-refactor-2.png %}
  
重构后的结果:  
  
{% codeblock lang:java %}
    @Test
    public void should_() throws Exception {
        Person person = new Person().invoke();
        String name = person.getName();
        int age = person.getAge();

        System.out.println(name + age);
    }

    private class Person {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public Person invoke() {
            name = "Tom";
            age = 11;
            return this;
        }
    }
{% endcodeblock %}   
  
而Eclipse则告诉你我办不到。  
  
{% img /images/post/2014-10/eclipse-refactor-1.png %}
  
IDEA还可以通过重构自动创建工厂方法、builder，Eclipse则无能为力。  
  
{% img /images/post/2014-10/idea-refactor-3.png %}
  

{% img /images/post/2014-10/eclipse-refactor-2.png %}
  
#### 导航
IDEA可以从任何地方导航到你想要去的地方，`ctrl + shift + A`可以进到任何你要去的地方(包括配置)，最新的功能连续2次`shift`可以选择跳转到相关的文件。  
  
{% img /images/post/2014-10/idea-navigate-1.png %}
{% img /images/post/2014-10/idea-navigate-2.png %}
  
IDEA文件间的跳转不限于Java，XML、JavaScript等文件也可以通过`ctrl + 鼠标左键`进入目标，而Eclipse只能在Java文件里面做到。  
  
{% img /images/post/2014-10/idea-navigate-3.png %}
  
#### 静态代码检查
IDEA有很强大的静态代码检查功能，能帮助你改掉一些不好的编码习惯，比如下面的代码IDEA会提示if分支可以简化，直接返回equal结果就可以，但Eclipse则是持着你代码烂关我P事的态度对待你的代码。  
  
{% codeblock lang:java %}
	public boolean check() {
        if ("".equals(name)) {
            return true;
        }
        return false;
    }
{% endcodeblock %}   
  
  
{% img /images/post/2014-10/idea-static-code-1.png %}
  
方法没有被其他类用到也会有提示。  
  
{% img /images/post/2014-10/idea-static-code-2.png %}
  
老的for循环提示使用foreach。
  
{% img /images/post/2014-10/idea-static-code-3.png %}
  
#### 集成众多成熟插件
IDEA不像Eclipse需要安装很多插件，标准的安装已经包含了很多成熟的插件，比如版本管理工具就包含了SVN，GIT，ClearCase等。这有点像苹果的个人电脑，不需要用户了解其中的各种细节，安装好之后就能舒舒服服的使用，但IDEA又不像苹果那么封闭，它还是可以安装插件，但其本身的插件就已经很够用了，没有太大必要再去安装其他插件。  
  
{% img /images/post/2014-10/idea-plugin.png %}
    
## Eclipse的优点
黑了这么多Eclipse，说实话Eclipse还是有其优点的，比如:  
  
* 免费。这个是Eclipse最大的优势，也是大部分Java开发还在使用Eclipse的原因，虽然IDEA有免费的社区版，但如果要用到更多高级功能，还是推荐使用无限制版本。
* 插件多。Eclipse的插件多如牛毛，各种需要的功能都可以通过搜索相关插件获得，而且其插件的开发也相对比较简单，如果找不到想要的可以自己开发。
* 占用内存少。这个可以从进程管理工具看出来，但从我实际的使用结果来看，Eclipse经常会卡顿，而IDEA则大部分时间都很流畅。
* 可以一个窗口同时显示几个工程。IDEA一个窗口只能显示一个工程，多个项目需要多开几个IDEA窗口，但可以通过加载module的方式在一个项目里面关联多个工程。

## 总结
IDEA和Eclipse的定位本身是不一样的，Eclipse将其定位为一个平台，可以通过安装各种插件来编写各种语言的代码，包括C++等，而IDEA将自己定位为*最智能的Java集成开发编辑器*，如果你不是开发Java代码的，建议不要选用IDEA。 
