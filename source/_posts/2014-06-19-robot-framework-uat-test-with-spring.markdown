---
layout: post
title: "使用Robot Framework结合Spring进行uat测试"
date: 2014-06-19 21:50
description: 使用Robot Framework结合Spring进行uat测试
keywords: robot,spring,uat
comments: true
categories: code
tags: [robot,spring,uat]
---

在做项目过程中，单元测试是大家经常接触的也是了解的比较多的，但单元测试有时候为了更快的运行，会mock掉数据库或者关联系统来执行测试，这样的话整体的功能就得不到验证，另外单元测试也无法进行页面比如html的测试，这个时候就需要进行UAT自动化测试了。  
<!--more-->  
  
[UAT][UAT]，(User Acceptance Test),用户接受度测试 即验收测试，这种一般是手工测试，当然重复进行手动测试是一种选择，但如果手工测试很多的话每次执行就比较浪费时间和精力，而且也容易遗漏和出错，所以我们需要将手工测试进行自动化。  
  
## [Robot Framework][Robot Framework]
Robot是一个自动化测试框架，其可以使用的Lib很多，简单的安装即可使用，也可以自己通过Python和Java来开发自己需要的Lib包，不过现在robot的Lib已经比较多，满足大部分的使用场景。  
  
#### 安装
Robot安装十分简单，但首先要安装Python环境（Python安装这里不介绍了，请自行google了解），然后执行以下语句即可进行安装。
  
{% codeblock lang:sh %}
sudo pip install robotframework
{% endcodeblock %} 
  
安装完成后验证是否安装成功。  
  
{% codeblock lang:sh %}
pybot --version
Robot Framework 2.8.5 (Python 2.7.3 on linux2)
{% endcodeblock %} 
  
如果需要安装其他Lib包，同样是通过pip进行安装，下面以安装Selenium2Library为例。  
  
{% codeblock lang:sh %}
sudo pip install robotframework_Selenium2Library
{% endcodeblock %} 
  
#### 测试脚本
Robot安装完成后，就可以开始编写测试脚本了，下面是测试脚本和Resource文件的例子:  
  
{% codeblock lang:robotframework create_recipient.robot%}
*** Settings ***
Library    Selenium2Library
Library    DatabaseLibrary
Resource   resource.robot
Test Setup      Open Browser  ${baseurl}/#/recipients  ${browser}
Test Teardown   Close Browser

*** TestCases ***
create new recipient
    Given execute sql  delete from recipients where email='zhaozhiming003@gmail.com'
    When create recipient
    Then verify create recipient  添加用户成功

create exist recipient
    Given execute sql  delete from recipients where email='zhaozhiming003@gmail.com'
    Given execute sql  insert into recipients(username,email) values('zhaozhiming','zhaozhiming003@gmail.com')
    When create recipient
    Then verify create recipient  添加用户失败

*** Keywords ***
execute sql
    [Arguments]  ${sql}
    Connect To Database Using Custom Params      cymysql    db='${dbname}',user='${dbuser}',passwd='${dbpassword}', host='${dbhost}',port=${dbport}
    Execute Sql String    ${sql}
    Disconnect from database

create recipient
    Input text    username    zhaozhiming
    Input text    email    zhaozhiming003@gmail.com
    click element  css=.submit

verify create recipient
    [Arguments]  ${expectContent}
    sleep   2s
    page should contain  ${expectContent}
{% endcodeblock %} 
  
{% codeblock lang:robotframework resource.robot%}
*** Variables ***
${baseurl}  http://localhost:9898/oddemail
${dbhost}  localhost
${dbport}  3306
${dbname}  oms
${dbuser}  root
${dbpassword}  root
${browser}  chrome
{% endcodeblock %} 
* Settings下面是测试脚本需要引用的Lib包名，Resource文件，Setup和Teardown方法。
	* Resource文件可以用来存放一些可以复用的变量，当然也可以将这一部分放到测试脚本中。
	* Setup和Teardow跟单元测试一样，是在跑每个Test Case之前和之后会做的事情。注意上面例子里面打开url指定了浏览器Chrome，如果不指定浏览器的话会默认用Firefox打开，如果想使用Chrome来进行web自动化测试的话，则需要下载Chrome驱动，下载完后将其解压并设置到PATH路径。




[UAT]: http://baike.baidu.com/view/1330235.htm?fromTaglist
[Robot Framework]: http://robotframework.org/