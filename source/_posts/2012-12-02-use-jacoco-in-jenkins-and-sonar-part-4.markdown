---
layout: post
title: "在jenkins和sonar中集成jacoco(四)--在sonar中集成jacoco"
date: 2012-12-02 14:46
comments: true
categories: code
tags: [jacoco, jenkins, sonar]
---
  
首先要得到之前的单元测试和集成测试的覆盖率文件，还有对应的class文件以及单元测试的覆盖率报告，材料准备齐全之后，使用如下命令：  

<!--more-->
{% codeblock build.xml lang:xml %}
<taskdef uri="antlib:org.sonar.ant" resource="org/sonar/ant/antlib.xml">
        <classpath path="${env.SONAR-ANT-TASK.JAR}"/>
    </taskdef>

    <target name="sonar">
        <sonar:sonar key="${sonar.project.key}" version="${sonar.project.version}" xmlns:sonar="antlib:org.sonar.ant">
            <sources>
                <path location="${src.dir}" />
            </sources>

            <property key="sonar.projectName" value="jacoco_demo" />
            <property key="sonar.sourceEncoding" value="UTF-8" />
            <property key="sonar.dynamicAnalysis" value="reuseReports" />
            <property key="sonar.surefire.reportsPath" value="${junit.dir}" />
            <property key="sonar.core.codeCoveragePlugin" value="jacoco" />
            <property key="sonar.jacoco.reportPath" value="${basedir}/ut.exec" />
            <property key="sonar.jacoco.itReportPath" value="${basedir}/uat.exec" />

            <tests>
                <path location="${src.test.dir}" />
            </tests>

            <binaries>
                <path location="${build.src.class}" />
                <path location="${build.test.class}" />
            </binaries>

            <libraries>
                <path location="${build.lib.dir}" />
            </libraries>
        </sonar:sonar>
    </target>
{% endcodeblock %}    
  
参数解释：  
  
* sonar.dynamicAnalysis是指单元测试报告的生成方式，值为reuseReports是指给出生成好的单元测试报告路径。
* sonar.surefire.reportsPath是指单元测试报告的路径。
* sonar.core.codeCoveragePlugin是覆盖率插件，有jacoco,cobetura等。
* sonar.jacoco.reportPath是单元测试覆盖率文件的路径。
* sonar.jacoco.itReportPath是集成测试覆盖率文件的路径。
  
更多参数可以参考这里：[单元测试](http://docs.codehaus.org/display/SONAR/Code+Coverage+by+Unit+Tests)，[集成测试](http://docs.codehaus.org/display/SONAR/Code+Coverage+by+Integration+Tests)  
  
在sonar 3.3的版本会自动将单元测试和集成测试的覆盖率合并，最后附上sonar上的覆盖率显示：  

{% img /images/post/2012112914305954.png %}  
{% img /images/post/2012112914311753.png %}  
  
