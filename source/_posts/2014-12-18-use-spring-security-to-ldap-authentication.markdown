---
layout: post
title: "使用Spring Security进行LDAP认证"
date: 2014-12-18 08:56
description: 使用Spring Security进行LDAP认证
keywords: spring,spring-security,ldap
comments: true
categories: code
tags: [spring,spring-security,ldap]
---
  
{% img /images/post/2014-12/spring-security.jpeg %}
  
这里介绍一下如何是用Spring Security来做LDAP的认证，LDAP服务器只存放了用户的用户名和密码，没有角色等其他权限，所以这里介绍的是最简单的用户名密码认证。  
  
<!--more-->
  
## 下载spring-security相关JAR包
下面是gradle的脚本配置，需要下载spring-security和ldap相关的JAR包。    
  
{% codeblock build.gradle lang:groovy %}
String springSecurityVersion = "3.2.5.RELEASE"

dependencies {
	...//other spring jars
	
	//security
	compile "org.springframework.security:spring-security-core:" + springSecurityVersion
	compile "org.springframework.security:spring-security-web:" + springSecurityVersion
	compile "org.springframework.security:spring-security-config:" + springSecurityVersion
	compile "org.springframework.security:spring-security-ldap:" + springSecurityVersion
	compile "org.springframework.ldap:spring-ldap-core:2.0.2.RELEASE"
}
{% endcodeblock %}

## 配置web.xml
在web.xml配置`filter`，修改内容如下。  

{% codeblock web.xml lang:xml %}
<filter>
   <filter-name>springSecurityFilterChain</filter-name>
  <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
</filter>
<filter-mapping>
   <filter-name>springSecurityFilterChain</filter-name>
   <url-pattern>/*</url-pattern>
</filter-mapping>

<listener>
   <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
</listener>

<context-param>
   <param-name>contextConfigLocation</param-name>
   <param-value>classpath:spring-security.xml</param-value>
</context-param>
{% endcodeblock %}

## 创建spring-security.xml
在web.xml里面指定了Application启动时需要加载spring-security.xml文件，我们的LDAP认证主要就配置在这个文件里面。  
  
{% codeblock spring-security.xml lang:xml %}
<beans xmlns="http://www.springframework.org/schema/beans"
   xmlns:s="http://www.springframework.org/schema/security" xmlns:context="http://www.springframework.org/schema/context"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
       http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd
    http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-3.2.xsd">

   <context:property-placeholder location="classpath:app.properties" />  //1

   <s:http pattern="/resources/**" security="none" />  //2
   <s:http pattern="/login.html" security="none" />    //2

   <s:http use-expressions="true">
      <s:intercept-url pattern="/**" access="isAuthenticated()" />  //3
      <s:form-login login-page="/login.html"                        //4
         authentication-failure-url="/login.html?error=true"        //5   
         username-parameter="j_username" password-parameter="j_password"/> //6
      <s:anonymous />
      <s:logout logout-success-url="/login.html?logout=true" />  //7
   </s:http>

   <s:ldap-server url="${ldap_server}" manager-dn="${ldap_user}"//8
      manager-password="${ldap_password}" />

   <s:authentication-manager> 
      <s:ldap-authentication-provider
         user-dn-pattern="${ldap_user_dn_pattern}" />  //9
   </s:authentication-manager>

</beans>
{% endcodeblock %}
  
1. 指定properties文件，下面的ldap信息都是从properties文件里面取得。
2. 配置哪些资源和url不需要做认证，比如一些图片，js和css文件等，还有我们的login页面，如果把login页面也拦截的话，就做不了认证了。
3. 指定其他url(`/**`)都需要做认证，isAuthenticated方法表示认证通过了才能访问该url。
4. 指定登陆页面的地址，这里是相对路径，如果不指定login-page，认证时会自动调用spring-security的一个默认登陆页面。
5. 指定认证失败后的url，这里我们使用同一个login页面，只是在url后面加上查询参数作为认证失败的标示。
6. 指定login页面2个作用域，用户名和密码，需要和页面录入框的name相同。
7. 指定登出/注销成功后的页面，这里我们还是使用login页面，在url后面加上logout参数作为标示。
8. ldap服务器的配置信息，包括url, manager-dn和manager-password。
9. 配置ldap的user-dn-pattern。

下面是app.properties的内容。  
  
{% codeblock app.properties lang:properties %}
# Ldap
ldap_server=ldap://your.ldap.server.com.:12356
ldap_user=cn=yourname,cn=Users,dc=ldap,dc=server,dc=com
ldap_password=123456
ldap_user_dn_pattern=uid={0},ou=staff,ou=people,o=ldap.server.com
{% endcodeblock %}

## 创建登陆页面
创建用户登陆的Form，method为`post`，action为`j_spring_security_check`，用户录入框的name为`j_username`，密码录入框的name为`j_password`，这2个值与之前spring-security.xml里面配置的要保持一致。  
  
{% codeblock login.html lang:html %}
<form name="LoginForm" method="post" action="j_spring_security_check">
    <div style="display: block;">
        <div>
            <h1>
                <span class="ui-icon add"></span>用户登录
            </h1>
        </div>
        <div class="content">
            <div id="error" style="display: none; color: #c9302c" align="center">
                <h3>认证失败，请重新登录</h3>
            </div>
            <div id="logout" style="display: none; color: #02547f" align="center">
                <h3>已成功登出</h3>
            </div>

            <ul>
                <li><label>
                    <span>用户名</span>
                    <input type="text" name="j_username">
                </label></li>
                <li><label>
                    <span>密码</span>
                    <input type="password" name="j_password">
                </label></li>
            </ul>
        </div>
        <div>
            <button type="submit" name="logon" value="Logon">登录</button>
        </div>
    </div>
</form>
{% endcodeblock %}
  
相关的js代码如下:
{% codeblock lang:js %}
function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

if(getParameterByName("error")) {
	$("#error").show();
}

if(getParameterByName("logout")) {
	$("#logout").show();
}
{% endcodeblock %}

如果需要的话，可以配置自己的logout页面，只需要一个Form就可以了，方法为`post`，action为`j_spring_security_logout`，只要提交了这个Form就可以成功登出了。

{% codeblock logout.html lang:html %}
<form action="j_spring_security_logout" method="post" id="logoutForm"></form>
{% endcodeblock %}
  
更多Spring Security的信息请查阅: [Spring Security Reference][spring-security]。  

[spring-security]: http://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/
