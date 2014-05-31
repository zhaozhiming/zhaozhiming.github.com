---
layout: post
title: "最近小项目的一些记录（二）"
date: 2014-05-31 10:40
description: hibernate,angularjs,semantic-ui,jsoup
keywords: hibernate,angularjs
comments: true
categories: code
tags: [hibernate,angularjs,semantic-ui,jsoup]
---

[前一篇post][url3]主要记录了Spring JPA和Hibernate的问题，接着记录其他一些工具遇到的问题。
<!--more-->  

## Jsoup
[Jsoup][url1]是一个比较火的Java解析Html工具，简单易用，功能强大，在github上有不少星星。

### 使用Jsoup连接url时报403
一般使用Jsoup连接url都不会有什么问题，正常的写法如下：  
  
{% codeblock lang:java %}
	Jsoup.connect(url).get();
{% endcodeblock %}  
  
但有些网站会获取用户的浏览器信息，而上面的写法是没有浏览器信息的，这个时候就会报403的错误，需要在连接时加上userAngent。  
  
{% codeblock lang:java %}
	Jsoup.connect(url).userAgent("Mozilla").get();
{% endcodeblock %}  
  
### Css Selector
使用Jsoup来抓取网页信息，最简单的一种方式就是通过Css selector，这样可以让你少写很多代码。比如下面的一段html:  
  
{% codeblock lang:html %}
	<div id="main">
		<div class="header"></div>
	</div>
{% endcodeblock %}  
  
使用css selector可以这样简单的获得。
  
{% codeblock lang:java %}
	doc.select("#main div.header")
{% endcodeblock %}  

## Velocity
项目用到了Velocity来定制邮件模板，这样可以方便的修改邮件格式。在网上可以搜到很多关于Spring和Velocity集成的文章，但大部分是讲如何使用Velocity来生成页面的，而如何生成其他文件的说明比较少，下面我们就来看一下如何配置的。  
  
* 先配置velocityConfigurer的bean，这里定义了模板文件的路径，属性和编码等信息。
{% codeblock lang:xml %}
	<bean id="velocityConfigurer"
          class="org.springframework.web.servlet.view.velocity.VelocityConfigurer">
        <property name="resourceLoaderPath" value="WEB-INF/velocity" />
        <property name="velocityProperties">
            <props>
                <prop key="directive.foreach.counter.name">
                    loopCounter
                </prop>
                <prop key="directive.foreach.counter.initial.value">
                    0
                </prop>
                <prop key="input.encoding">UTF-8</prop>
                <prop key="output.encoding">UTF-8</prop>
            </props>
        </property>
    </bean>
{% endcodeblock %}  
  
* 在需要使用的Sevice里面引入，这样就可以在service里面使用到Velocity的配置信息了。
{% codeblock lang:java %}
@Service
public class MailService {
    @Resource(name = "velocityConfigurer")
    private VelocityConfigurer velocityConfigurer;
 }   
{% endcodeblock %}  
  
* 使用Velocity的工具类来得到生成的文件信息。
	* temple是模板文件的名称。
	* model是生成文件所需的参数。
{% codeblock lang:java %}
String content = VelocityEngineUtils.mergeTemplateIntoString(
                velocityConfigurer.getVelocityEngine(), temple,
                MAIL_ENCODING, model); 
{% endcodeblock %}  
  
### Angularjs
Angularjs功能比较强大，使用它之后基本可以不用使用JQuery了，这里记录一下使用遇到的一个比较奇怪的问题。  
  
* 问题: 使用Angularjs发起一个post请求，带了几个请求的参数，后台接收请求时获取不到参数。开始的写法如下：

{% codeblock lang:js %}
	var queryData = {
                "depGroup": $("#dep_group").val() || "",
                "website": $("#website").val() || "",
                "startDate": $("#start_date").val() || "",
                "endDate": $("#end_date").val() || ""
            };

	$http.post('api/search', queryData).success(function (data) {
                    $scope.blogs = data;
                });
{% endcodeblock %}  
  
* 接收不到参数的原因是angularjs把参数转成了json导致后台接收不到，需要在发起请求时将参数转换一下，修改后的代码如下：  
  
{% codeblock lang:js %}
	var postConfig = {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
        transformRequest: transform
    };

    $http.post('api/search', queryData，postConfig).success(function (data) {
                    $scope.blogs = data;
{% endcodeblock %}  
  
### Semantic-ui
这个是我比较喜欢的一个css框架，这里要注意的地方是有些功能和Angularjs结合起来会发现不起作用，如果不起作用可以尝试使用[angular-semantic][url2]这个工具看看能否解决问题。  
  
* semantic-ui的表单规则和行为设置，简单的示例如下，需要设置规则(rules)和配置(setting即校验通过后的行为)。  

js代码:  
{% codeblock lang:js %}
    var rules = {
    	searchKeyword: {
            identifier: 'searchKeyword',
            rules: [
                {
                    type: 'empty',
                    prompt: '请输入查询关键字'
                }
            ]
        }
    };

    var setting = {
        onSuccess: function () {
            // 发http请求
        }
    };

    $('#searchForm').form(rules, setting);
{% endcodeblock %}  
  
html代码:
{% codeblock lang:html %}
	<div id="searchForm" class="ui form">
        <div class="two fields">
            <div class="field">
                <div class="ui icon input">
                    <input id="searchKeyword" type="text" name="searchKeyword" placeholder="请输入查询关键字...">
                    <i class="search icon"></i>
                </div>
            </div>

            <div class="field">
                <div id="searchBtn" class="ui blue submit button">查询</div>
            </div>
        </div>
        <div class="ui error message"></div>
    </div>
{% endcodeblock %}  
  

[url1]: https://github.com/jhy/jsoup
[url2]: https://github.com/caitp/angular-semantic
[url3]: http://zhaozhiming.github.io/blog/2014/05/31/some-tips-in-my-recent-project/