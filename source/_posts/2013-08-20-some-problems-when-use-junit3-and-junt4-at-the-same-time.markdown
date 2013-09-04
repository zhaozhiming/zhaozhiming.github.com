---
layout: post
title: "junit4与junt3并存时产生的问题"
date: 2012-04-16 15:54
description: "junit4与junt3并存时产生的问题"
keywords: junit3,junit,并存,问题
comments: true
categories: code
tags: [java, junit]
---
目前的项目里用junit写单元测试，使用的是junit4，由于大部分开发之前使用的都是junit3，对junit4还不是很熟悉，所以出现了junit3和4混合使用的情况，导致发生了一些问题，这里列举一下。  
  
**1.测试类继承了TestCase，方法名是test开头，加Ignore标签，这时Ignore标签失效。错误代码示例：**  
  
{% codeblock test.java lang:java %}
public MyClassTest extends TestCase {
    
    @Test
    @Ignore
    public void testMyMethod() throws Exception {
        // some code
    }
}
{% endcodeblock %}  
在junit3里，测试类必须继承TestCase，方法必须是以test开头；而在junit4里面，无需继承TestCase类，方法名也不需要test开头，只需要在每个方法前加上@Test标签即可。如果是继承TestCase，方法名以test开头，则junit会认为是junt3的写法，而使得junit4的标签失效。  
  

**2.测试类继承了TestCase，方法前加上@Test标签，方法名不以test开头，该方法不会被junit执行。**  
  
{% codeblock test.java lang:java %}
public MyClassTest extends TestCase {
    
    @Test
    public void should_test_my_method() throws Exception {
        // some code
    }
}
{% endcodeblock %}    
用于使用了@Test标签，则方法名可以不需以test开头，但该测试类由于继承了TestCase，所以默认是使用了junit3的契约，所以方法名必须以test开头，否则junit不认，即使是加上了@Test标签。  
