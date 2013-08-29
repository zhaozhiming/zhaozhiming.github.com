---
layout: post
title: "在ant中将依赖jar包一并打包的方法"
date: 2012-03-16 11:10
description: "在ant中将依赖jar包一并打包的方法"
keywords: ant,依赖jar包,打包
comments: true
categories: code
tags: ant
---
  
一般jar包里面是不包含jar文件的，如果自己的类有依赖其他jar包，可以通过ant命令将这些jar包解析，然后和自己的class文件打在一起，命令如下：    
{% codeblock build.xml lang:xml %}
        <jar basedir="${build.class}" destfile="${build.out}/myjar.jar">
            <manifest>
                <attribute name="Main-Class" value="Main"/>
            </manifest>

            <zipfileset excludes="META-INF/*.SF" src="${project.lib}/1.jar" />
            <zipfileset excludes="META-INF/*.SF" src="${project.lib}/2.jar" />
        </jar>
{% endcodeblock %}  
执行之后，依赖jar包就会解压到要打包的jar文件里面，只要依赖包里面的class和自己的class没有冲突，一般是没有问题的。  
