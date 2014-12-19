---
layout: post
title: "RequireJS的简单使用"
date: 2014-12-19 11:05
description: RequireJS的简单使用
keywords: requirejs
comments: true
categories: code
tags: requirejs
---
  
{% img /images/post/2014-12/requirejs.png %}
  
在没有[RequireJS][requirejs]以前，我们的html页面都需要配置很多js文件，有了RequireJS以后，我们只需要简单的一个RequireJS的文件就可以了。  
  
<!--more-->
## 工程目录
先看下我们的工程目录，主要引入了Jqury、AngularJS和RequireJS这几个js文件，login.html、main.js和login.js是我们自己的文件。  
  
{% codeblock lang:sh %}
--webapp
  |--resources
    |--scripts
      |--vendor
        |--jquery.js
        |--angularjs.js
        |--requirejs.js
      |--login.js
      |--main.js  
  |--WEB-INF
  |--login.html	
{% endcodeblock %}
  
## 在html页面中加入RequireJS
首先在html页面我们先引入requirejs，可以看到script标签中有个data-main属性，这个是RequireJS的属性标签，指向我们工程里面的main.js文件，注意这里不需要写`.js`后缀。  
  
{% codeblock index.html lang:html %}
<!DOCTYPE html>
<html lang="en" ng-app="app">
<head>
	<meta charset="UTF-8">
	<title>My App</title>
	<script src="resources/scripts/vendor/require.js" data-main="resources/srcipts/main"></script>
</head>
<body>

<div>
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
</div>

</body>
</html>
{% endcodeblock %}
  
## main.js
下面是我们的main.js。  

{% codeblock main.js lang:js %}
require.config({
   paths:{
      angular: 'vendor/angular',
      jquery: 'vendor/jquery'
   },
   shim:{
      angular:{
         deps: ['jquery'],
         exports: 'angular'
      }
   }   
});

require(['angular', 'login'], function(angular) {    
});
{% endcodeblock %}
  
* `paths`里面指定了jqury和angularjs对应的js文件路径，同样不需要写js后缀，并给他们起了对应的别名。  
* `shim`属性里面配置了deps数组，表明angular依赖jqury，还有exports值，表明这个模块外部调用时的名称。  
* 最后一部分代码表示我们的html页面需要使用哪些js文件，比如我们使用了angular和login这2个js文件的功能，同时angular依赖了jqurey，所以html页面加载的时候就会同时将jquery.js、angularjs.js和login.js这几个js文件加载进来。  
  
## login.js
最后看一下我们的login.js，通过之前的main.js我们已经加载好了Jquery和AngularJS这些第三方JS库，要使用它们的话需要通过`define`的方式来引用。  
  
比如下面的js文件使用了jquery的功能，我们可以在`define`后面添加`angular`这个名称，因为前面在`shim`属性里面已经定义了`angular`依赖`jquery`，所以使用`angular`也可以用到`jquery`的功能。(当然我们也可以单独添加`jquery`，但这样就使用不到`angular`的功能了)  
  
{% codeblock login.js lang:js %}
define(['angular'], function(angular) {
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
});
{% endcodeblock %}
  

[requirejs]: http://requirejs.org/
