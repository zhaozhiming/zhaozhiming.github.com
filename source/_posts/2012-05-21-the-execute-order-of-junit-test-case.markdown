---
layout: post
title: "junit里面Test case的执行顺序"
date: 2012-05-21 16:02
comments: true
categories: code
tags: [ant, junit]
---
这里讨论的是junit在ant运行的情况，其他build工具应该也适用，但具体没试验过。  
  
首先运行junit时是按照脚本中文件夹的顺序执行，如下脚本会先执行test1目录下的测试，其实是test2目录的，最后是test3目录的。  
  
<!--more-->  
{% codeblock build.xml lang:xml %}
<batchtest todir="${junit.dir}">
    <fileset dir="${test1.dir}">
        <include name="**/*Test.java"/>
    </fileset>
    <fileset dir="${test2.dir}">
        <include name="**/*Test.java"/>
    </fileset>
    <fileset dir="${test3.dir}">
        <include name="**/*Test.java"/>
    </fileset>
</batchtest>
{% endcodeblock %}  
  
其次在同一个目录下，test case 的运行顺序是根据包名的顺序来执行的。比如：a.a.a这个包名里面的test case会比a.a.b这个包名的test case先跑。  
  
再次如果是同一个目录，且在同一个包名下的，会根据类名的顺序来执行test case。比如：a.a.a.A.java会比a.a.a.B.java先执行。  
  
最后如果是同个类的test case，则依照TestClass里面test case的顺序从上往下执行。  
  
Junit Test case 的执行顺序有时候比较重要，可能一些test case会依赖与其他test case产生的结果才能执行，这个时候把要先执行的类按照上面的规则放在前面即可保证整个测试执行正确。  
