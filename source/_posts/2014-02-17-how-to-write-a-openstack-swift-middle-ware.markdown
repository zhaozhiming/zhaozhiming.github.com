---
layout: post
title: "openstack swift中间件编写"
date: 2014-02-17 20:33
description: openstack swift中间件编写
keywords: openstack,swift,middleware
comments: true
categories: code
tags: [openstack, swift, middleware]
---

{% img /images/post/2014-2/swift.jpg %}

关于openstack swift的资料可以看[这里][url1]，[这里][url2]还有[这里][url3]。  
<!--more-->  

## 准备环境
从零开始接触的同学可以先从swift的[all in one][url4]部署开始学习，在本机搭建好swift环境就可以进行简单的测试了。由于swift是用Python语言写的，如果要开发swift的中间件的还需要在本地安装Pythone的IDE，我比较喜欢JETBRAIN（他们比较出名的是JAVA的IDE——IDEA）公司的IDE——Pycharm。准备环境如下:  

* Ububutn 12.04 LTS 64bit
* Python2.7(虽然现在已经有Python3了，但swift是用2.x的Python写的，Python3不向后兼容Python2) 
* Pycharm3
  
## 中间件介绍
swift通过提供基于HTTP协议的API给外界调用来完成对象存储的功能，我们从swift的各个部署说明里面可以看到，proxy server和storage node的配置文件里面都有一个`[pipeline:main]`，这个是swift各个服务的请求链，由多个中间件组成的一个中间件集合。pipeline有点像J2EE里面filter，每个http请求需要经过各个服务的pipeline。
  
{% codeblock proxy-server.conf lang:xml %}
...
[pipeline:main]
# Yes, proxy-logging appears twice. This is so that
# middleware-originated requests get logged too.
pipeline = catch_errors healthcheck proxy-logging bulk ratelimit crossdomain slo cache tempurl tempauth staticweb account-quotas container-quotas proxy-logging proxy-server
...
{% endcodeblock %} 
  
{% codeblock account-server.conf lang:xml %}
...
[pipeline:main]
pipeline = recon account-server
...
{% endcodeblock %} 
  
## 中间件编写
了解了swift的基本功能流程后，我们就可以来写自己的中间件了。  
  
没有写过中间件的同学可以通过学习其他中间件开始，在swift的源码中配置了很多中间件，有一些功能非常简单。比如name_check中间件，这个中间件的作用是拿来分析请求的url，判断url中是否有特殊字符，长度是否超出规定长度等。这个中间件没有配置在swift的标准配置中，有需要的可以自行加上本机的swift环境做测试。  
  
我们先来看一下name_check中间件的配置信息：
  
{% codeblock proxy-server.conf lang:xml %}
[pipeline:main]
pipeline = catch_errors healthcheck name_check cache ratelimit tempauth sos
           proxy-logging proxy-server

[filter:name_check]
use = egg:swift#name_check
forbidden_chars = '"`<>
maximum_length = 255
{% endcodeblock %} 
在上面的例子中，name_check中间件加在healthcheck这个中间件后面，filter:name_check下面的配置信息是name_check的一些配置参数。

* forbidden_chars: 指url中不能包含的特殊字符
* maximum_length: 指url的最大长度
  
我们再来看name_check的单元测试：
  
{% codeblock test_name_check.py lang:python %}
class FakeApp(object):

    def __call__(self, env, start_response):
        return Response(body="OK")(env, start_response)


class TestNameCheckMiddleware(unittest.TestCase):

    def setUp(self):
        self.conf = {'maximum_length': MAX_LENGTH, 'forbidden_chars':
                     FORBIDDEN_CHARS, 'forbidden_regexp': FORBIDDEN_REGEXP}
        self.test_check = name_check.filter_factory(self.conf)(FakeApp())

    def test_valid_length_and_character(self):
        path = '/V1.0/' + 'c' * (MAX_LENGTH - 6)
        resp = Request.blank(path, environ={'REQUEST_METHOD': 'PUT'}
                             ).get_response(self.test_check)
        self.assertEquals(resp.body, 'OK')

    ...... # other test cases    
if __name__ == '__main__':
    unittest.main()
{% endcodeblock %} 
看源码先从单元测试看起，可以以最快的速度了解源代码的功能。在这个测试案例中，测试先mock了一个虚拟的app，这个app不会真实的调用swift，而是会将http response返回预设好的值。  
再看其中的一个测试案例，这里给定了一个最大长度url，然后通过调用name_check中间件，期望请求可以正常通过。

最后我们再来看name_check中间件的[源码][url5]几个方法：  
  
* __init__: 中间件的初始化方法
* __call__: 中间件被调用时触发的方法
* filter_factory: 这个是类以外的方法，在swift服务启动时会创建中间件实例，并加入到pipeline中。
  
学习完这个简单的中间件后，相信大家都可以依葫芦画瓢开始写自己的中间件了。  
  
## 修改配置文件
编写完中间件之后，还需要将中间件配置到swift中，这样才算真正完成中间件的创建。  
  
#### 首先先停止swift的服务
{% codeblock shell lang:xml %}
swift@ubuntu:~$ swift-init main stop
{% endcodeblock %} 
  
#### 接着修改conf文件
假设你增加的中间件是proxy server的中间件，就修改proxy-server.conf，自行决定要放到pipeline中的哪个位置，具体要看你的中间件是执行什么功能。
{% codeblock proxy-server.conf lang:xml %}
[pipeline:main]
pipeline = catch_errors healthcheck your_middleware cache ratelimit tempauth sos
           proxy-logging proxy-server

[filter:your_middleware]
use = egg:swift#your_middleware
your_middleware_config1 = value1
your_middleware_config1 = value2
{% endcodeblock %} 

####要修改swift的根目录下的setup.cfg文件
{% codeblock setup.cfg lang:xml %}
paste.filter_factory =
	#这里加入一行自己的中间件，可以看下name_check中间件是怎么写的
	name_check = swift.common.middleware.name_check:filter_factory
{% endcodeblock %} 

#### 执行命令重新安装swift
{% codeblock shell lang:xml %}
swift@ubuntu:~$ cd swift目录
swift@ubuntu:~$ sudo python setyp.py develop
{% endcodeblock %} 

#### 最后重启swift服务
{% codeblock shell lang:xml %}
swift@ubuntu:~$ swift-init main start
{% endcodeblock %} 


[url1]: http://zh.wikipedia.org/wiki/OpenStack
[url2]: http://www.programmer.com.cn/12403/
[url3]: http://www.ibm.com/developerworks/cn/cloud/library/1310_zhanghua_openstackswift/
[url4]: http://docs.openstack.org/developer/swift/development_saio.html
[url5]: https://github.com/openstack/swift/blob/master/swift/common/middleware/name_check.py