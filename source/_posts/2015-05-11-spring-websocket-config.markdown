---
layout: post
title: "如何在Spring中配置Websocket"
date: 2015-05-11 09:41
description: 如何在Spring中配置Websocket
keywords: spring,websocket
comments: true
categories: code
tags: [spring,websocket]
---
  
{% img /images/post/2015-5/websockets.png %}  
  
Websocket是HTML5的一项新技术，可以让服务端和客户端进行实时的通信，主要的使用场景有: 实时的聊天系统，对实时性要求比较高的游戏，或者金融行业对股票市场数据的及时获取等。在Spring3的时候就已经有了对Websocket的支持，不过需要一些高版本的web容器来运行，比如Tomcat7.0.47+，Jetty9等。  
  
<!--more-->  
  
在Spring的官网上有关于Websocket的示例工程，[https://spring.io/guides/gs/messaging-stomp-websocket/][spring-websocket]，里面简单介绍了如何通过Spring-boot来进行Websocket系统的构建。我们的例子将基于这个例子进行修改，但是是使用传统的Spring的方式进行配置。  
  
## 依赖包
首先我们需要添加相关的依赖包:   
  
* Websocket需要servlet3.1的版本
* spring-websocket和spring-messaging是Spring关于Websocket的组件
* 使用Jackson进行json数据的处理
  
{% codeblock build.gradle lang:groovy %}
String springVersion = "4.1.4.RELEASE"
String jacksonDatabindVersion = "2.5.0"
String jacksonVersion = "1.9.13"
dependencies {

    //websocket
    compile("javax.websocket:javax.websocket-api:1.1")
    compile("javax.servlet:javax.servlet-api:3.1.0")

	//spring
    compile("org.springframework:spring-messaging:" + springVersion)
    compile("org.springframework:spring-websocket:" + springVersion)

    //json
    compile "com.fasterxml.jackson.core:jackson-databind:" + jacksonDatabindVersion
    compile "org.codehaus.jackson:jackson-mapper-asl:" + jacksonVersion
    compile "org.codehaus.jackson:jackson-core-asl:" + jacksonVersion
}
{% endcodeblock %} 
  
## xml配置（类配置）
我们有两种方式进行Websocket的配置，一种是通过xml文件的方式，在这里我们定义了websocket的配置信息，这样服务器往客户端发送消息就可以通过`/topic/xx`来发送，客户端则可以通过`/app/hello`来发送消息到服务端。  
  
{% codeblock lang:xml %}
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:websocket="http://www.springframework.org/schema/websocket"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
        http://www.springframework.org/schema/websocket http://www.springframework.org/schema/websocket/spring-websocket.xsd">

    ...... // other configurations

    <websocket:message-broker application-destination-prefix="/app">
        <websocket:stomp-endpoint path="/hello">
            <websocket:sockjs/>
        </websocket:stomp-endpoint>
        <websocket:simple-broker prefix="/topic"/>
    </websocket:message-broker>
</beans>
{% endcodeblock %} 
  
另外一种方式是通过类的方式，代码如下，功能与上面的xml配置相同:  
  
{% codeblock WebSocketConfig.java lang:java %}
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/app");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/hello").withSockJS();
	}
}
{% endcodeblock %} 
  
## 消息类和Controller定义
Controller定义:  
  
{% codeblock WebSocketConfig.java lang:java %}
import com.zzm.wechat.model.Greeting;
import com.zzm.wechat.model.HelloMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GreetingController {
    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(3000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }
}
{% endcodeblock %} 
  
消息model的定义:  
  
{% codeblock WebSocketConfig.java lang:java %}
public class Greeting {
    private String content;

    public Greeting(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}

public class HelloMessage {
    private String name;
    
    public String getName() {
        return name;
    }
}
{% endcodeblock %} 
  
在web.xml中设置controller的url前缀，这样可以避免一些页面的url被controller拦截。  
  
{% codeblock web.xml lang:xml %}
    <servlet>
		<servlet-name>mvc-dispatcher</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
	</servlet>

	<servlet-mapping>
		<servlet-name>mvc-dispatcher</servlet-name>
		<url-pattern>/api/*</url-pattern>
	</servlet-mapping>
{% endcodeblock %} 
  
## 客户端页面
首先下载`stomp.js`和`sockjs.js`，然后编写一个html页面进行客户端websocket的连接，并实现发送消息和接收消息的功能。我们使用SockJS的方式来创建Websocket连接，注意url要加上domain名称(这里是`server`)和`api`前缀。  
  
{% codeblock demo.html lang:html %}
<!DOCTYPE html>
<html>
<head>
    <title>Hello WebSocket</title>
    <script src="resources/sockjs-0.3.4.js"></script>
    <script src="resources/stomp.js"></script>
    <script type="text/javascript">
        var stompClient = null;

        function setConnected(connected) {
            document.getElementById('connect').disabled = connected;
            document.getElementById('disconnect').disabled = !connected;
            document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
            document.getElementById('response').innerHTML = '';
        }

        function connect() {
            var socket = new SockJS('/server/api/hello');
            stompClient = Stomp.over(socket);
            stompClient.connect({}, function(frame) {
                setConnected(true);
                console.log('Connected: ' + frame);
                stompClient.subscribe('/topic/greetings', function(greeting){
                    showGreeting(JSON.parse(greeting.body).content);
                });
            });
        }

        function disconnect() {
            if (stompClient != null) {
                stompClient.disconnect();
            }
            setConnected(false);
            console.log("Disconnected");
        }

        function sendName() {
            var name = document.getElementById('name').value;
            stompClient.send("/app/hello", {}, JSON.stringify({ 'name': name }));
        }

        function showGreeting(message) {
            var response = document.getElementById('response');
            var p = document.createElement('p');
            p.style.wordWrap = 'break-word';
            p.appendChild(document.createTextNode(message));
            response.appendChild(p);
        }
    </script>
</head>
<body onload="disconnect()">
<noscript><h2 style="color: #ff0000">Seems your browser doesn't support Javascript! Websocket relies on Javascript being enabled. Please enable
    Javascript and reload this page!</h2></noscript>
<div>
    <div>
        <button id="connect" onclick="connect();">Connect</button>
        <button id="disconnect" disabled="disabled" onclick="disconnect();">Disconnect</button>
    </div>
    <div id="conversationDiv">
        <label>What is your name?</label><input type="text" id="name" />
        <button id="sendName" onclick="sendName();">Send</button>
        <p id="response"></p>
    </div>
</div>
</body>
</html>
{% endcodeblock %} 
  
运行结果：  
  
{% img /images/post/2015-5/websocket-run.png %}  
  
浏览器console信息:   
  
{% codeblock lang:sh %}
Disconnected
chrome-extension://fhhdlnnepfjhlhilgmeepgkhjmhhhjkh/js/detector.js:505 detector
chrome-extension://fhhdlnnepfjhlhilgmeepgkhjmhhhjkh/js/detector.js:506 Object
stomp.js:130 Opening Web Socket...
stomp.js:130 Web Socket Opened...
stomp.js:130 >>> CONNECT
accept-version:1.1,1.0
heart-beat:10000,10000

<<< CONNECTED
version:1.1
heart-beat:0,0

connected to server undefined
demo.html:22 Connected: CONNECTED
heart-beat:0,0
version:1.1

>>> SUBSCRIBE
id:sub-0
destination:/topic/greetings

>>> SEND
destination:/app/hello
content-length:14

{"name":"zzm"}
<<< MESSAGE
destination:/topic/greetings
content-type:application/json;charset=UTF-8
subscription:sub-0
message-id:3657pj5u-0
content-length:25

{"content":"Hello, zzm!"}
{% endcodeblock %} 
  
## gradle运行jetty9
gradle内置的Jetty版本是Jetty6，由于版本较低不支持websocket，所以我们测试的话需要打包并部署到Jetty9或Tomcat7.0.47+上，但我们可以通过其他gradle插件来把我们的本地服务运行到Jetty9上。这里介绍2个插件，[Gretty][gretty]和[Cargo][cargo]。  
  
#### Gretty
在`build.gradle`中添加如下脚本:   
  
{% codeblock build.gradle lang:groovy %}
buildscript {
    repositories {
        maven {
            url "http://maven.oschina.net/content/groups/public/"
        }
    }

    dependencies {
        classpath 'org.akhikhl.gretty:gretty:+'
    }
}

apply plugin: 'org.akhikhl.gretty'
// apply plugin: 'jetty' 注意要注释掉原来的jetty插件

gretty {
    httpPort = 9898 // 指定web服务的http端口
    servletContainer = 'jetty9' // 这里可以指定tomcat,jetty的几个版本
}
{% endcodeblock %} 
  
然后运行`gradle appRun`即可。  
  
#### Cargo
在`build.gradle`中添加如下脚本，注意要先下载jetty9的安装包并解压:   
  
{% codeblock build.gradle lang:groovy %}
buildscript {
    repositories {
        maven {
            url "http://maven.oschina.net/content/groups/public/"
        }
    }

    dependencies {
        classpath 'com.bmuschko:gradle-cargo-plugin:2.1'
    }
}

apply plugin: 'com.bmuschko.cargo'
cargo {
    containerId = 'jetty9x'
    port = 9898
    local {
        homeDir = file('/Users/zhaozhiming/tools/jetty-distribution-9.2.10.v20150310')
    }
}
{% endcodeblock %} 
  
然后运行`gradle war CargoRunLocal`，注意首先要打出war包，然后插件会自动部署war包到Jetty9的安装目录下，这种方式不大灵活，比如一些页面的修改都需要重新部署才能进行测试。  
  

[spring-websocket]: https://spring.io/guides/gs/messaging-stomp-websocket/
[gretty]: https://github.com/akhikhl/gretty
[cargo]: https://github.com/bmuschko/gradle-cargo-plugin
