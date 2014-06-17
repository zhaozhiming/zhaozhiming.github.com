---
layout: post
title: "基于Spring MVC做单元测试（一）——使用Spring Test框架"
date: 2014-06-16 10:54
description: 基于Spring Mvc做单元测试
keywords: spring mvc,unit test
comments: true
categories: code
tags: [spring,unit test]
---

最近用Spring Mvc框架做了几个小项目，但都没有做单元测试，最近想恶补一下这方面的东西，包括基于Spring的单元测试，自动化测试和JS单元测试。今天先讲一下基于Spring框架的单元测试，测试使用的是Spring自带的test组件，再结合Mockito一起编写测试案例，以下示例会包括Controller和Service，由于Repository是基于Spring JPA，没有自己的逻辑，所以这里就不涉及Repository的单元测试，以后有需要再介绍。
<!--more-->  

## Controller
首先看一下Controller的代码（如下），代码比较简单，就是接收前端发过来的一些参数，通过这些参数直接调用Service的方法。  
  
{% codeblock lang:java %}
import com.odde.mail.model.Result;
import com.odde.mail.service.MailService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.lang.String.format;

@Controller
@RequestMapping("/mail")
public class MailController {
    private static final Log log = LogFactory.getLog(MailController.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MailService mailService;

    @RequestMapping(value = "/send", method = RequestMethod.POST, produces = "text/plain;charset=UTF-8")
    public
    @ResponseBody
    String send(@RequestParam("recipients") String recipients,
                @RequestParam("subject") String subject,
                @RequestParam("content") String content) throws Exception {
        log.debug("mail controller send start");
        log.debug(format("recipients:%s", recipients));
        log.debug(format("subject:%s", subject));
        log.debug(format("content:%s", content));
        Result mailResult = mailService.send(recipients, subject, content);
        String result = mapper.writeValueAsString(mailResult);
        log.debug(format("result:%s", result));
        log.debug("mail controller send finish");
        return result;
    }
}
{% endcodeblock %}  
  
再来看对应的单元测试:  

{% codeblock lang:java %}
import com.odde.mail.model.Result;
import com.odde.mail.service.MailService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml")
public class MailControllerTest {
    private MockMvc mockMvc;

    @Mock
    private MailService mailService;

    @InjectMocks
    MailController mailController;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(mailController).build();
    }

    @Test
    public void should_return_status_success_when_send_mail_success() throws Exception {
        when(mailService.send("test@test.com", "test", "test")).thenReturn(new Result("成功"));

        mockMvc.perform(post("/mail/send")
                .param("recipients", "test@test.com")
                .param("subject", "test")
                .param("content", "test"))
                .andDo(print())
                .andExpect(status().isOk()).andExpect(content().string(is("{\"status\":\"" + result + "\"}")));

        verify(mailService).send("test@test.com", "test", "test");
    }
{% endcodeblock %}  
  
#### 首先是Spring的几个Annotate  
* RunWith(SpringJUnit4ClassRunner.class): 表示使用Spring Test组件进行单元测试;
* WebAppConfiguration: 使用这个Annotate会在跑单元测试的时候真实的启一个web服务，然后开始调用Controller的Rest API，待单元测试跑完之后再将web服务停掉;
* ContextConfiguration: 指定Bean的配置文件信息，可以有多种方式，这个例子使用的是文件路径形式，如果有多个配置文件，可以将括号中的信息配置为一个字符串数组来表示;
  
#### 然后是Mockito的Annotate
* Mock: 如果该对象需要mock，则加上此Annotate;
* InjectMocks: 使mock对象的使用类可以注入mock对象，在上面这个例子中，mock对象是MailService，使用了MailService的是MailController，所以在Controller加上该Annotate;
  
#### Setup方法
* `MockitoAnnotations.initMocks(this)`: 将打上Mockito标签的对象起作用，使得Mock的类被Mock，使用了Mock对象的类自动与Mock对象关联。
* `mockMvc`: 细心的朋友应该注意到了这个对象，这个对象是Controller单元测试的关键，它的初始化也是在setup方法里面。
  
#### Test Case
* 首先mock了MailService的send方法，让其返回一个成功的Result对象。
* `mockMvc.perform`: 发起一个http请求。
* `post(url)`: 表示一个post请求，url对应的是Controller中被测方法的Rest url。
* `param(key, value)`: 表示一个request parameter，方法参数是key和value。
* `andDo（print()）`: 表示打印出request和response的详细信息，便于调试。
* `andExpect（status().isOk()）`: 表示期望返回的Response Status是200。
* `andExpect（content().string(is（expectstring））`: 表示期望返回的Response Body内容是期望的字符串。
  
使用print打印处理的信息类似下面显示的内容:
{% codeblock lang:xml %}
MockHttpServletRequest:
         HTTP Method = POST
         Request URI = /mail/send
          Parameters = {recipients=[test@test.com], subject=[test], content=[test]}
             Headers = {}

             Handler:
                Type = com.odde.mail.controller.MailController
              Method = public java.lang.String com.odde.mail.controller.MailController.send(java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception

               Async:
   Was async started = false
        Async result = null

  Resolved Exception:
                Type = null

        ModelAndView:
           View name = null
                View = null
               Model = null

            FlashMap:

MockHttpServletResponse:
              Status = 200
       Error message = null
             Headers = {Content-Type=[text/plain;charset=UTF-8], Content-Length=[19]}
        Content type = text/plain;charset=UTF-8
                Body = {"status":"成功"}
       Forwarded URL = null
      Redirected URL = null
             Cookies = []
{% endcodeblock %} 
  
## Service

照例我们先看一下Service的功能代码，代码也比较简单，就是调用Repository做一些增删改查的动作。  
  
{% codeblock lang:java %}
import com.odde.mail.model.Recipient;
import com.odde.mail.model.Result;
import com.odde.mail.repo.RecipientRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipientService {

    @Autowired
    private RecipientRepository recipientRepository;

    public Result add(String username, String email) {
        Recipient recipient = recipientRepository.findByEmail(email);
        Result result;
        if (recipient == null) {
            recipientRepository.save(new Recipient(username, email));
            result = new Result("成功");
        } else {
            result = new Result("失败");
        }
        return result;
    }
}
{% endcodeblock %} 
  
再来看对应的测试代码:  
  
{% codeblock lang:java %}
import com.odde.mail.model.Recipient;
import com.odde.mail.repo.RecipientRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml")
public class RecipientServiceTest {

    @Mock
    private RecipientRepository recipientRepository;

    @InjectMocks
    private RecipientService recipientService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void should_return_success_when_add_recipient_not_exist() throws Exception {
        when(recipientRepository.findByEmail(anyString())).thenReturn(null);
        when(recipientRepository.save(any(Recipient.class))).thenReturn(null);

        assertThat(recipientService.add("Tom", "test@test.com").getStatus(), is("成功"));
        verify(recipientRepository).findByEmail(anyString());
        verify(recipientRepository).save(any(Recipient.class));
    }
{% endcodeblock %} 
  
Service的单元测试就比较简单了，大部分内容都在Controller里面讲过，不同的地方就是Controller是使用mockMvc对象来模拟Controler的被测方法，而在Service的单元测试中则是直接调用Service的方法（比如上面例子中的findByEmail和add）。

## Reponsitory
最后再说一下Reponsitory的单元测试，刚才讲过这里不涉及这块的介绍，因为Reponsitory没有具体的实现代码，基本上调用的是Spring JPA的功能。  
  
{% codeblock lang:java %}
import com.odde.mail.model.Recipient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientRepository extends JpaRepository<Recipient, Long> {

    public Recipient findByEmail(String email);
}
{% endcodeblock %} 
  
如果你的项目里面有自定义的Reponsitory具体实现，则需要做单元测试，这个可以上网自行搜索相关资料。