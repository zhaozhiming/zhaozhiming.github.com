---
layout: post
title: "微信公众账号开发part1——开发者验证"
date: 2015-02-04 13:51
description: 微信公众账号开发part1——开发者验证
keywords: wechat,java
comments: true
categories: code
tags: [wechat,java]
---

{% img /images/post/2015-2/wechat.jpg %}  
  
最近在了解微信公众账号的开发，准备边学边写一些文章来记录学习的过程，主要是基于微信的开发者模式来进行公共账号的开发，服务器选择新浪云SAE，语言还是选择比较熟悉的JAVA。  
  
<!--more-->  

## 基本准备

* 登陆微信公众平台网站: `https://mp.weixin.qq.com`，进行账号注册，具体可以参考[青龙老贼的这篇文章][wechat_register]，虽然内容有点老跟现在的不大一样，但不影响参考。
* 在SAE上面新建一个JAVA应用，这里还是可以参照[青龙老贼的文章][sae]，跟里面不同的是我们要创建一个JAVA的应用，而不是PHP的。

## 修改开发者中心的配置
  
* 登陆进到微信公众平台后，点击左下角的开发者中心，再点击图中的修改配置按钮，就可以进到修改配置页面。
  
{% img /images/post/2015-2/wechat_config_1.png %}  
  
* 填写配置项
  * 输入你的SAE的应用URL，比如:`http://xxx.sinaapp.com`，不一定要写应用的基本URL，可以在上面加一些扩展，比如`http://xxx.sinaapp.com/xxx`，这个要看你的应用的restful怎么定了。  
  * TOKEN随便输入一个字符串就可以，这个值后面是要配置到java应用里面的，可以理解为一个加密的密钥。
  * EncodingAESKey随机生成。
  * 消息加解密方式暂时选择明文模式。  
  
{% img /images/post/2015-2/wechat_config_2.png %}  
  
## 微信服务端开发

* 新建一个spring mvc工程，可以参照[这个文章][spring_mvc]，但我们暂时不需要数据库和页面，只需要定义restful接口就可以了。  

* 新建Controller并定义认证的api，可以参考微信公众平台开发者文档里面的[接入指南][wechat_new]，里面有段php代码是指导服务端怎么开发的，我们要做的只是把它翻译成JAVA。
  
{% codeblock MainController.java lang:java %}
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
@Controller
public class MainController {
    private static final Log log = LogFactory.getLog(MainController.class);

    //从配置文件获取的token值，就是刚才在修改配置项里面定义的那个Token
    @Value("${token}")
    private String token;

    //定义一个GET请求，url为xxx/index
    @RequestMapping(value = "/index", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    public
    @ResponseBody
    //接收新手指南里面提到的那4个参数
    ResponseEntity<String> auth(@RequestParam("signature") String signature,
                                @RequestParam("timestamp") String timestamp,
                                @RequestParam("nonce") String nonce,
                                @RequestParam("echostr") String echostr) throws Exception {
        log.info("wechat auth start");
        log.info(String.format("signature:%s, timestamp:%s, nonce:%s, echostr:%s",
                signature, timestamp, nonce, echostr));

        //如果认证通过，原样返回echostr值，并返回200的response
        if (wechatAuth(signature, timestamp, nonce)) {
            log.info("wechat auth success");
            return new ResponseEntity<String>(echostr, HttpStatus.OK);
        }

        //如果失败，则返回400，并提示认证失败
        log.info("wechat auth failed");
        return new ResponseEntity<String>("wechat auth failed.", HttpStatus.BAD_REQUEST);
    }

    private boolean wechatAuth(String signature, String timestamp, String nonce) {
    	//将这3个string放到一个list里
        ArrayList<String> strings = Lists.newArrayList(token, timestamp, nonce);
        log.info(String.format("before sort array:%s", strings));
        //按字母顺序做一下排序
        Collections.sort(strings);
        log.info(String.format("after sort array:%s", strings));

        //将list里面所有string组合成一个string，这里用到了guava的Joiner
        String groupString = Joiner.on("").join(strings);
        log.info(String.format("groupString string:%s", groupString));

        //用SHA1加密该string
        String result = sha1(groupString);
        log.info(String.format("sha1:%s", result));
        //加密后的值和signature进行比较，注意用java加密后都是字母都是大写的，而传过来的signature是小写字母，所以要大小写转换一下
        boolean compareResult = result.equals(signature.toUpperCase());
        log.info(String.format("compare result:%b", compareResult));
        return compareResult;
    }

    //类似php的sha1方法
    private String sha1(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                hexString.append(String.format("%02X", 0xFF & aMessageDigest));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("sha1 failed");
        }
    }
}
{% endcodeblock %} 

* 将工程打包成war，上传到SAE完成部署，启动应用
  
## 启用开发者模式
  
* 进到微信公众平台的开发者中心，点击服务器配置那一行后面的启用按钮，如果服务器正常启动的话，就可以看到启用成功的提示了。
  
{% img /images/post/2015-2/wechat_start.png %}  
  
更多代码可以看这里: https://github.com/zhaozhiming/wechat-blog，觉得好的话请Star一下吧，谢谢  
  


[wechat_register]: http://segmentfault.com/blog/zetd/1190000000356021
[sae]: http://segmentfault.com/blog/zetd/1190000000356067
[spring_mvc]: https://confluence.jetbrains.com/display/IntelliJIDEA/Getting+Started+with+Spring+MVC,+Hibernate+and+JSON
[wechat_new]: http://mp.weixin.qq.com/wiki/17/2d4265491f12608cd170a95559800f2d.html
