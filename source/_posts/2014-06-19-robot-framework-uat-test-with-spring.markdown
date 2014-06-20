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
  
[UAT][UAT]，(User Acceptance Test),用户接受度测试，即验收测试，这种一般是手工测试，当然重复进行手动测试是一种选择，但如果手工测试很多的话每次执行就比较浪费时间和精力，而且也容易遗漏和出错，所以我们需要将手工测试进行自动化。  
  
## [Robot Framework][Robot Framework]
Robot是一个自动化测试框架，其可以使用的Lib很多，简单地安装即可使用，也可以自己通过Python和Java来开发自己需要的Lib包，不过现在robot的Lib已经比较多了，可以满足大部分的使用场景。  
  
#### 安装
Robot安装十分简单，但首先要安装Python环境（Python安装这里不介绍了，请自行google了解），然后执行以下语句进行安装。
  
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
  
#### Robot脚本
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
	* Setup和Teardow跟单元测试一样，是在跑每个Test Case之前和之后会做的事情。注意上面例子里面打开url指定了浏览器Chrome，如果不指定浏览器的话会默认用Firefox打开，如果想使用Chrome来进行web自动化测试的话，则需要下载Chrome驱动[chromedriver][chromedriver]，下载完后将其解压并设置到PATH路径。
  
* TestCases设置了脚本里的测试案例，每个测试案例可以由Given，When，Then组成，每一行除开这几个关键字就是测试案例的步骤，在robot里面叫关键字，关键字可以带参数，可以把关键字理解为程序里面的方法。

* Keywords就是在Test Case里面定义的关键字了，里面有是每个关键字具体的执行内容。
	* `execute sql`这个关键字的内容是连接数据库，执行sql，关闭数据库连接。
	* `create recipient`这个关键字的内容是在页面输入用户名，输入邮箱地址，点击提交按钮。注意这里的页面元素是通过class来查找的，也可以通过id，tag或其他。
	* `verify create recipient`这个关键字的内容是等待2秒，验证页面是否包含期望的内容。
  
可以看到Keyword的每个内容描述都很简单易懂，甚至还可以写中文，让不懂开发的人也知道你的测试案例是做什么的。
  
#### 执行脚本
因为这个测试案例是对web页面进行测试，所以在执行测试案例之前需要先将应用服务启起来，服务启起来后，执行下面的	语句:  
  
{% codeblock lang:sh%}
pybot /your/robot/scripts/path/create_recipient.robot
{% endcodeblock %} 
  
执行完后会看到系统自动打开浏览器，输入url，自动填写输入框，并验证是否正确，最后会产生几个报告文件，如果测试案例失败，还会自动将失败的页面截图存成文件。  
  
{% codeblock lang:sh%}
Output:  /home/kingzzm/projects/odde-mail-server/output.xml
Log:     /home/kingzzm/projects/odde-mail-server/log.html
Report:  /home/kingzzm/projects/odde-mail-server/report.html
{% endcodeblock %} 
  
另外建议脚本存放到test目录下，建议目录结构如下:  
  
{% img /images/post/2014-6/robot_script_local.png %}  
  
#### 与Gradle集成
每次跑robot测试都需要手动启一个web服务比较麻烦，可以考虑在Gradle中通过任务的方式来执行robot测试，在跑测试之前先通过gradle启一个本地应用，然后开始跑robot测试，跑完测试后通过Gradle停掉本地服务。  
因为Gradle自带jetty容器，所以这一步实现起来也比较简单:  
  
{% codeblock lang:groovy %}
[jettyRun, jettyStop]*.stopPort = 7654
[jettyRun, jettyStop]*.stopKey = 'oddemail'

task uat(type:Exec, dependsOn: jettyRun) {
    commandLine '/usr/local/bin/pybot', 'src/test/uat/testcase'
    doLast {
        jettyStop.execute()
    }
}
{% endcodeblock %} 
  
以后需要跑uat测试就执行`gradle uat`这个命令就可以了，这样还可以结合jenkins进行uat自动化测试。

[UAT]: http://baike.baidu.com/view/1330235.htm?fromTaglist
[Robot Framework]: http://robotframework.org/
[chromedriver]: https://code.google.com/p/selenium/wiki/ChromeDriver