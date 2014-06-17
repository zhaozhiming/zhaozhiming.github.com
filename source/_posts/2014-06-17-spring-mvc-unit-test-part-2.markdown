---
layout: post
title: "基于Spring MVC做单元测试（二）——使用JMockit"
date: 2014-06-17 12:24
description: 基于Spring Mvc做单元测试（二）——使用JMockit
keywords: spring mvc,unit test,JMockit
comments: true
categories: code
tags: [spring,unit test,JMockit]
---

上一篇Post讲了如何使用Spring的Test框架来进行单元测试，但在运行单元测试的时候有一个问题，就是每次跑单元测试都需要加载一下配置文件，或者启动web容器，这样的单元测试跑起来就不能达到快的目的。下面再介绍一下通过JMockit这个Java Mock工具来进行spring的单元测试，其特点是不需指定spring的配置文件，任何对象都可以mock出来并进行关联。
<!--more-->  

## Controller
首先我们还是来看一下使用了JMockit的Controller单元测试是怎么写的，Controller的功能代码可以查看上一篇post。  
  
{% codeblock lang:java %}
import com.odde.mail.model.Result;
import com.odde.mail.service.MailService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(JMockit.class)
public class MailControllerTest {
    @Tested
    MailController mailController;

    @Injectable
    private MailService mailService;

    @Test
    public void should_return_status_success_when_send_mail_success() throws Exception {
        new Expectations() { {
            mailService.send("test@test.com", "test", "test");
            result = new Result("成功");
        } };

        String result = mailController.send("test@test.com", "test", "test");

        assertThat(result, is("{\"status\":\"成功\"}"));
    }
}
{% endcodeblock %}  
  
* @RunWith(JMockit.class): 指定单元测试的执行类为JMockit.class;
* @Tested: 这个Annotate是指被测试类，在这个测试案例中我们要测试的是MailController，所以我们给其打上这个标签;
* @Injectable: 这个Annotate可以将对象进行mock并自动关联到被测试类，而不需要通过其他文件类似spring的配置文件等来进行关联;
* @Expectations: mock对象mailService的send方法，让其返回一个Result对象;
  
做完上面这些基本就可以了，后面的被测方法调用和验证都跟原来的一样。这样看起来是不是比原来的单元测试代码少了一些，也更简洁了一些，最重要的一点是这样的单元测试不依赖spring的bean定义文件，不需要启动web服务，执行起来速度很快。  
  
## Service
再来看一下Service的单元测试要怎么改写，同样Service的功能代码可以看上一篇Post。  
  
{% codeblock lang:java %}
import com.odde.mail.model.Recipient;
import com.odde.mail.model.Result;
import com.odde.mail.repo.RecipientRepository;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(JMockit.class)
public class RecipientServiceTest {

    @Tested
    private RecipientService recipientService;

    @Injectable
    private RecipientRepository recipientRepository;

    @Test
    public void should_return_success_when_add_recipient_not_exist() throws Exception {
        Result result = recipientService.add("Tom", "test@test.com");
        assertThat(result.getStatus(), is("成功"));
    }
}
{% endcodeblock %}  
  
相对Controller Test这里少了一步对recipientRepository对象findByEmail方法的mock，因为如果不通过Expectations进行方法mock的话，方法会默认返回null，而我们要测试的场景正是需要findByEmail方法返回null，所以mock方法这一步我们也省了。  
改写后的整体代码也比原来的少了很多，而且速度更快。  
  
## 适当使用Mock框架
JMockit功能非常强大，不仅可以轻松处理上面的这些测试场景，还可以对static,final,private等方法进行mock，可以让你的单元测试毫无阻碍的进行。  
但是如果过度的使用Mock框架，会让功能代码的坏味道被掩盖。本来单元测试的设计可以让你发现功能代码上的一些设计是否合理，比如有没有紧耦合等，但使用JMockit可以让你在设计不合理的代码上也可以轻松地进行单元测试，这样你就很难发现功能代码上的问题了。  
所以建议JMockit等类似的mock框架还是要谨慎使用，首先要保证功能代码设计合理，满足面向对象设计的要求，再来考虑提高单元测试效率的问题。
  


