---
layout: post
title: "在jenkins和sonar中集成jacoco(一)--使用jacoco收集单元测试的覆盖率"
date: 2012-11-29 14:45
comments: true
categories: code
tags: [jacoco, jenkins, sonar]
---
  
之前系统的持续集成覆盖率工具使用的是cobetura，使用的过程中虽然没什么问题，但感觉配置比较麻烦，现在准备改用jacoco这个覆盖率工具来代替它。接下来我介绍一下jenkins配置jacoco，并且在sonar显示单元测试和集成测试覆盖率的过程。  

用jacoco来实现单元测试的覆盖率比较简单，在ant脚本中先增加下面的任务：  

<!--more-->  
{% codeblock build.xml lang:xml %}
<taskdef uri="antlib:org.jacoco.ant" resource="org/jacoco/ant/antlib.xml">
        <classpath path="${basedir}/jacoco_lib/jacocoant.jar" />
</taskdef>
{% endcodeblock %}  
  
这里要引入jacoco的jar包jacocoant.jar，增加了这个命令之后，将原有的单元测试任务用 jacoco:coverage包括起来，实例代码如下：  
  
{% codeblock build.xml lang:xml %}
<target name="unitTest" depends="test_compile">
        <mkdir dir="${junit.dir}"/>
        <jacoco:coverage destfile="${basedir}/ut.exec">
            <junit fork="true" forkmode="once" printsummary="on" failureproperty="unit.test.failure">
                <classpath>
                    <pathelement location="${build.class}"/>
                    <fileset dir="${build.lib.dir}"/>
                </classpath>

                <formatter type="xml"/>
                <batchtest todir="${junit.dir}">
                    <fileset dir="${src.test.dir}">
                        <include name="**/*Test.java"/>
                    </fileset>
                </batchtest>
            </junit>
        </jacoco:coverage>
　　　　<!-- 其他内容 -->
</target>
{% endcodeblock %}  
  
jacoco:coverage的destfile参数是指生成的覆盖率文件路径，不写默认文件名为jacoco.exec。另外在forkmode这个参数设置为once，可以提高你的单元测试的执行效率。  
  
这样跑完单元测试后，就会在指定路径下生成覆盖率文件ut.exec（或默认的jacoco.exec，在工程根目录下）。  
  
如果要在本地生成jacoco的覆盖率报告，可以增加如下任务：    

{% codeblock build.xml lang:xml %}
<target name="jacocoReport">
        <delete dir="${basedir}/jacoco"/>
        <mkdir dir="${result.jacoco.report.dir}/ut"/>
        <jacoco:report>
            <executiondata>
                <file file="${basedir}/ut.exec"/>
            </executiondata>

            <structure name="jacoco_demo">
                <classfiles>
                    <fileset dir="${build.dir}"/>
                </classfiles>
                <sourcefiles encoding="UTF-8">
                    <fileset dir="${src.dir}"/>
                </sourcefiles>
            </structure>
            <html destdir="${result.jacoco.report.dir}/ut"/>
        </jacoco:report>      
</target>
{% endcodeblock %}  
  
生成覆盖率报告需要覆盖率文件（exec）、源码、编译后的class文件。这里导出的格式是html，还有xml和cvs 2种格式可以导出，具体参考[jacoco:report](http://www.eclemma.org/jacoco/trunk/doc/ant.html#report)。  

注意，这里的class文件和exec文件必须用同一个jvm执行，如果是用JVM A来编译class文件，然后用JVM B来生成覆盖率文件，生成出来的报告覆盖率会为0%。**这一点对生成集成测试的覆盖率特别重要。**  

