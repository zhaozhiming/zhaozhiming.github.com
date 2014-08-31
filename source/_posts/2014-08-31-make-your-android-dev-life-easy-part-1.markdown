---
layout: post
title: "让你的安卓开发更容易(一)——Intellij IDEA"
date: 2014-08-31 10:59
description: 让你的安卓开发更容易
keywords: android,idea
comments: true
categories: code
tags: [android,idea]
---
  
{% img /images/post/2014-8/intellijidea_android.gif %}  
  
{% blockquote 孔子 ——《春秋》 %}
工欲善其事，必先利其器
{% endblockquote %}

介绍一下安卓开发的一些好用工具，可以让你的开发事半功倍。如果你是一个安卓新手，那么准备一套高效率的开发工具，会让你从一开始就养成使用好工具的习惯，从开始就比别人更快；如果你是一名安卓开发高手，也可以看看这些工具，说不定你已经在使用其中的一些工具了。
<!--more-->

## Intellij IDEA
虽然IntelliJ IDEA是一款收费的软件，但收费有它收费的道理，跟eclipse相比，它多了更多重构，代码错误提示，与更多工具集成的功能。IntelliJ IDEA被认为是当前Java开发效率最快的IDE工具，它集成了开发过程中实用的众多功能，几乎可以不用鼠标可以方便的完成你要做的任何事情，最大程度的加快开发的速度。简单而又功能强大。  
  
从版本10开始，IntelliJ IDEA就集成了Android的开发功能，发展到现在版本13，不仅具备了流畅开发Android项目的能力，而且还集成了最新的构建工具Gradle，Android lint等工具。  
  
### Android Hello World
现在我们使用IntelliJ IDEA来创建一个使用Gradle构建的Android项目。   
  
* 首先点击创建新工程，在左边的项目类型栏中选择`Android`，可以看到右边有4个选项可以选，我们选择`Gradle: Android Mondule`，然后点击下面的Next;  
  
{% img /images/post/2014-8/idea-android-project-1.png %}  
  
* 进入项目信息配置页面，可以看到有如下的选项，填写后点击Next；
	* Application name: 应用名
	* Module name: 模块名
	* Package name: 包名
	* Minimum required SDK: 可支持的最小Android SDK版本
	* Target SDK: 可支持的最大Android SDK版本
	* Compile with: 用哪个Android SDK版本编译
	* Theme: app主题，全黑，全白，半黑半百，是否要GridLayou，是否要action bar等
  
{% img /images/post/2014-8/idea-android-project-2.png %}  
  
* 选择main_activity的样式，有9种可以选择，样式的效果在右边可以预览，我们可以选择最简单的`Blank Activity`，选择好了Next；
  
{% img /images/post/2014-8/idea-android-project-3.png %}  
  
* 填写Activity名字和对应的展示层layout名字，填完Next；
  
{% img /images/post/2014-8/idea-android-project-4.png %}  
  
* 填写工程名和选择工程文件路径，注意最下面的`Project format`，有2种格式，一种是`.idea`文件夹，一个是ipr文件，选择ipr文件的方式可以减少很多文件的生成，最后Finish；
  
{% img /images/post/2014-8/idea-android-project-5.png %}  
  
生成的代码如下:  
  
{% codeblock MainActivity.java lang:java %}
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
{% endcodeblock %}   
  
{% codeblock activity_main.xml lang:xml %}
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingLeft="@dimen/activity_horizontal_margin"
    android:paddingRight="@dimen/activity_horizontal_margin"
    android:paddingTop="@dimen/activity_vertical_margin"
    android:paddingBottom="@dimen/activity_vertical_margin"
    tools:context="com.github.zzm.myapplication1.app.MainActivity">

    <TextView
        android:text="@string/hello_world"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

</RelativeLayout>
{% endcodeblock %}   
  
运行的效果如下:  
  
{% img /images/post/2014-8/idea-android-project-6.png 250 350%}  
  
### Gradle脚本
可以看到工程目录下有2个build.gradle脚本，一个是根目录的构建文件(如下)，如果想提高构建速度，可以将脚本中的mavenCentral()改为`maven {url "http://maven.oschina.net/content/groups/public/"}`，就是将maven的国外镜像库改成国内的库。  
  
{% codeblock root/build.gradle lang:groovy %}
// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.9.+'
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
{% endcodeblock %}   
  
另外一个是app目录下的构建文件，可以看到指定了sdk的最小最大版本，需要的依赖包等。  
{% codeblock app/build.gradle lang:groovy %}
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:0.9.+'
    }
}
apply plugin: 'android'

repositories {
    mavenCentral()
}

android {
    compileSdkVersion 20
    buildToolsVersion "20.0.0"

    defaultConfig {
        minSdkVersion 8
        targetSdkVersion 20
        versionCode 1
        versionName "1.0"
    }
    buildTypes {
        release {
            runProguard false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
        }
    }
}

dependencies {
    compile fileTree(dir: 'libs', include: ['*.jar'])
    compile 'com.android.support:appcompat-v7:20.0.0'
}

{% endcodeblock %}   


