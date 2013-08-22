---
layout: post
title: "单例模式的5种形式"
date: 2012-12-20 17:53
comments: true
categories: code
tags: [java,design pattern]
---
  
有过编程经验的朋友都知道设计模式中的单例模式，最近又重新看了一遍设计模式，今天将单例模式的几种形式介绍一下：  
  
###1、懒汉形式（延迟加载）  
  
{% codeblock Singleton.java lang:java %}
public class Singleton{
    private static Singleton singleton;
    
    private Singleton() {
    }
    
    public synchronized static Single newInstance() {
        if (singleton== null) {
            singleton= new Singleton();
        }
        return singleton;
    }
}
{% endcodeblock %}   
  
这个是标准的单例模式，通过newInstance里面的判断来进行延迟加载单例对象，这里加了synchronized关键字可以避免多线程问题，但会影响程序性能。  
  
###2、饿汉形式（贪婪加载）  
  
{% codeblock Singleton.java lang:java %}
public class Singleton {
    private static Singleton singleton= new Singleton();

    private singleton() {
    }
    
    public static Singleton newInstance() {
        return singleton;
    }
}
{% endcodeblock %}   
  
在单例对象声明的时候就直接初始化对象，可以避免多线程问题，但是如果对象初始化比较复杂，会导致程序初始化缓慢。  

###3、双重检查加锁  
  
{% codeblock Singleton.java lang:java %}
public class Singleton {
    private volatile static Singleton singleton;

    private Singleton() {
    }

    public static Singleton newInstance() {
        if (singleton == null) {
            synchronized (Singleton.class) {
                if (singleton == null) {
                    singleton = new Singleton();
                }
            }
        }
        return singleton;
    }
}
{% endcodeblock %}   
  
这个是懒汉形式的加强版，将synchronized关键字移到了newInstance方法里面，同时将singleton对象加上volatile关键字，这种方式既可以避免多线程问题，又不会降低程序的性能。但volatile关键字也有一些性能问题，不建议大量使用。  
  
###4、Lazy initialization holder class  
  
{% codeblock Singleton.java lang:java %}
public class Singleton {
    private static class SingletonHolder {
        private static Singleton singleton = new Singleton();
    }

    private Singleton() {
    }

    public static Singleton newInstance() {
        return SingletonHolder.singleton;
    }
}
{% endcodeblock %}   
  
这里创建了一个内部静态类，通过内部类的机制使得单例对象可以延迟加载，同时内部类相当于是外部类的静态部分，所以可以通过jvm来保证其线程安全。这种形式比较推荐。  

###5、枚举  
  
{% codeblock Singleton.java lang:java %}
public enum Singleton {
    singleton
}
{% endcodeblock %}   

单因素的枚举类已经实现了单例，这种方法更加简单。  
  
