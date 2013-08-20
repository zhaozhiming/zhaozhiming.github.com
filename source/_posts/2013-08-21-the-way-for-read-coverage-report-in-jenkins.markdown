---
layout: post
title: "jenkins无法读取覆盖率报告的解决方法"
date: 2012-06-19 16:01
comments: true
categories: code
tags: jenkins
---
  
报错信息如下：  

{% codeblock log lang:text %}
coverage-report:    

[mkdir] Created dir: D:\JK\workspace\d9_rm3_engine_dev1.0.0_cud\view\rmv3_engine\engine_j2ee\doc\coveragereport

[cobertura-report] Cobertura 1.9.4.1 - GNU GPL License (NO WARRANTY) - See COPYRIGHT file

[cobertura-report] Cobertura: Loaded information on 114 classes.

[cobertura-report] Report time: 913ms

BUILD SUCCESSFUL

Total time: 1 minute 44 seconds

Publishing Cobertura coverage report...

No coverage results were found using the pattern 'view/rmv3_engine/engine_j2ee/doc/coveragereport/coverage.xml' relative to 'D:\JK\workspace\d9_rm3_engine_dev1.0.0_cud\view'.  Did you enter a pattern relative to the correct directory?  Did you generate the XML report(s) for Cobertura?

Build step 'Publish Cobertura Coverage Report' changed build result to FAILURE

Recording test results

Finished: FAILURE
{% endcodeblock %}  
  
覆盖率报告已经生成，在工作区也可以找到，路径是：'view/rmv3_engine/engine_j2ee/doc/coveragereport/coverage.xml'，但jenkins对于这个路径似乎不认，以前用svn的时候可以找到，改成CC后就找不到了。  
  
解决方法：job配置里面将覆盖率的路径设置为：/coverage.xml 就可以了。很奇怪的一个问题。  
