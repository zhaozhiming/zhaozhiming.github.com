---
layout: post
title: "微信公众账号开发part2——用户消息接收"
date: 2015-02-04 16:03
description: 微信公众账号开发part2——用户消息接收
keywords: wechat,java
comments: true
categories: code
tags: [wechat,java]
---

{% img /images/post/2015-2/wechat_part2.jpg %}  
  
上一篇写了如何通过微信开发者认证，今天来讲下如何接收用户的消息，我们以接收用户的订阅消息为例。  
  
<!--more-->  
  
## 微信用户消息格式
  
在开发者文档的[接收事件推送][receive_doc]文档中，说明了用户订阅消息的请求实体，内容如下:  
  
{% codeblock lang:xml %}
<xml>
	<ToUserName><![CDATA[toUser]]></ToUserName>
	<FromUserName><![CDATA[FromUser]]></FromUserName>
	<CreateTime>123456789</CreateTime>
	<MsgType><![CDATA[event]]></MsgType>
	<Event><![CDATA[subscribe]]></Event>
</xml>
{% endcodeblock %} 
  
* ToUserName: 开发者微信号
* FromUserName: 用户微信账号的OpenID
* CreateTime: 消息发送时间，秒数
* MsgType: 消息类型，事件消息为event
* Event: 事件类型，订阅消息为subscribe

## 消息真实性验证

{% blockquote 微信公众平台开发者文档 http://mp.weixin.qq.com/wiki/4/2ccadaef44fe1e4b0322355c2312bfa8.html 验证消息真实性 %}
每次开发者接收用户消息的时候，微信也都会带上前面三个参数（signature、timestamp、nonce）访问开发者设置的URL，开发者依然通过对签名的效验判断此条消息的真实性。效验方式与首次提交验证申请一致。
{% endblockquote %}
  
所以每个订阅消息的http请求都会带有（signature、timestamp、nonce）这3个参数和上面的xml请求实体，服务端可以选择是否校验消息的真实性，建议校验，这样会比较安全。  
  
## 接收消息后的响应内容
  
了解了消息请求的入参后，还需要知道我们处理请求后，需要返回什么样的内容给用户，这个在开发者文档里面好像没有提及，参考各方资料后知道需要返回一段xml内容，格式如下:  
  
{% codeblock lang:xml %}
<xml>
    <Content>感谢您关注我的公众账号[愉快]</Content>
    <CreateTime>1423022113</CreateTime>
    <FromUserName>zzm</FromUserName>
    <FuncFlag>0</FuncFlag>
    <MsgType>text</MsgType>
    <ToUserName>zzm</ToUserName>
</xml>
{% endcodeblock %} 
  
* ToUserName: `用户微信账号的OpenID`
* FromUserName: `开发者微信号`
* CreateTime: 消息发送时间，秒数
* FuncFlag: 这个暂时不知道是什么，默认值为0
* MsgType: 消息类型，文档消息可以为text和其他，这里我们以最简单的text文本消息为例
* Content: 返回给订阅用户的消息内容，可以加表情
  
PS: ToUserName和FromUserName这2个参数和请求的xml实体要相反，这个也比较好理解，用户发了条消息过来，你要发个消息回去，ToUserName就变成了用户，FromUserName变成了你自己的公众账号了。  
  
## 服务端开发
  
* 了解了http请求的入参和出参，我们可以来开发我们的API了，`talk is cheap, show me code`。  

{% codeblock MainController.java lang:java %}
	//这里我们定义跟之前认证api相同的url，但方法是POST
	@RequestMapping(value = "/index", method = RequestMethod.POST)
    public
    @ResponseBody
    //3个校验消息真实性的参数，还有一个request实体body，里面是xml文本
    ResponseEntity<String> receive(@RequestParam("signature") String signature,
                                   @RequestParam("timestamp") String timestamp,
                                   @RequestParam("nonce") String nonce,
                                   @RequestBody String body) throws Exception {
        log.info("receive message start");
        log.info(String.format("signature:%s, timestamp:%s, nonce:%s", signature, timestamp, nonce));

        //先校验消息的真实性，如果校验失败，则返回400
        if (!wechatAuth(signature, timestamp, nonce)) {
            log.info("wechat auth failed");
            return new ResponseEntity<String>("wechat auth failed.", HttpStatus.BAD_REQUEST);
        }

        log.info(String.format("body:%s", body));
        //我们定义了一个util来解析xml，将其转换为一个object
        TextMessage requestMessage = XmlUtil.toTextMessage(body);
        log.info(String.format("requestMessage:%s", requestMessage));

        TextMessage textMessage = null;
        String msgType = requestMessage.getMsgType();
        String toUserName = requestMessage.getToUserName();
        String fromUserName = requestMessage.getFromUserName();
        //判断消息类型，如果是event，且事件类型为subscribe，则新建一个文本消息
        if (MessageType.event.name().equals(msgType)) {
            if (EventType.subscribe.name().equals(requestMessage.getEvent())) {
                String message = "感谢您关注我的公众账号[愉快]";
                textMessage = new TextMessage(toUserName, fromUserName,
                        MessageType.text.name(), message, TimeUtil.currentSeconds());
            }
        }

        //将文本消息转换为xml文本
        String responseMessage = XmlUtil.toXml(textMessage);
        HttpHeaders responseHeaders = new HttpHeaders();
        //设置返回实体的编码，不设置的话可能会变成乱码
        responseHeaders.add("Content-Type", "text/html; charset=utf-8");
        log.info(String.format("response message: %s", responseMessage));
        log.info("receive message finish");
        return new ResponseEntity<String>(responseMessage, responseHeaders, HttpStatus.OK);
    }
{% endcodeblock %} 
  
* 这里使用java原生的JAXB来解析xml。  
  
{% codeblock XmlUtil.java lang:java %}
import com.zzm.wechat.model.TextMessage;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlUtil {

    public static String toXml(TextMessage textMessage) throws Exception {
        if (textMessage == null) return "";

        JAXBContext context = JAXBContext.newInstance(TextMessage.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_FRAGMENT, true);

        StringWriter sw = new StringWriter();
        m.marshal(textMessage, sw);
        return sw.toString();
    }

    public static TextMessage toTextMessage(String xml) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(TextMessage.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        TextMessage textMessage = (TextMessage) jaxbUnmarshaller.unmarshal(reader);
        IOUtils.closeQuietly(reader);
        return textMessage;
    }
}
{% endcodeblock %} 
  
* 定义消息的model类，这里需要用到xml的一些annotation。  
  
{% codeblock XmlUtil.java lang:java %}
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

//定义命名空间，如果不写的话，xml会以类名开头: <TextMessage>...</TextMessage>，写了就会以xml开头: <xml>...</xml>
@XmlRootElement(name = "xml")
public class TextMessage {
    private String fromUserName;
    private String toUserName;
    private String msgType;
    private int funcFlag = 0;
    private String content;
    private String event;
    private long createTime;

    public TextMessage() {
    }

    public TextMessage(String fromUserName, String toUserName, String msgType, String content, long createTime) {
        this.fromUserName = fromUserName;
        this.toUserName = toUserName;
        this.msgType = msgType;
        this.content = content;
        this.createTime = createTime;
    }

    public String getToUserName() {
        return toUserName;
    }

    //定义xml子项的名称，不写这个annotation的话，转换后的xml是: <toUserName>xxx</toUserName>，首字母变小写了，会导致消息传输错误
    @XmlElement(name = "ToUserName")
    public void setToUserName(String toUserName) {
        this.toUserName = toUserName;
    }

    //other setter and getter

    @Override
    public String toString() {
        //...
    }
}
{% endcodeblock %} 

* 方法写完以后，同样的打包，部署SAE。  

* 打开手机，关注你的公众账号后，就可以看到服务端传过来的消息内容了。  
  
{% img /images/post/2015-2/wechat_subscribe.png %}  
  
我的公众账号是`赵芝明的公账号`，有兴趣的也可以加一下，以后这个公共账号的功能肯定会慢慢丰富的。  
  


[receive_doc]: http://mp.weixin.qq.com/wiki/2/5baf56ce4947d35003b86a9805634b1e.html
