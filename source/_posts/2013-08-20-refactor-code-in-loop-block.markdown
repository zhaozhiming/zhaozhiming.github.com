---
layout: post
title: "for循环重复代码的重构"
date: 2012-06-16 16:39
comments: true
categories: code
tags: refactor
---
  
**DRY（don't repeat yourself）**，重复往往是代码腐烂的开始，我们一般的处理手法是将重复的代码提取成一个方法，然后用新方法替换掉原来的代码。  
  
但是对于for循环里面的重复代码要如何处理呢？比如有下面2个方法：  

{% codeblock A.class lang:java %}
    public boolean methodA(List<String> list) {
        for(String string : list) {
            if (string.startWith("A")) {
                return true;
            } 
        }
        return false;
    }

    public boolean methodB(List<String> list) {
        for(String string : list) {
            if (string.endWith("B")) {
                return true;
            } 
        }
        return false;
    }
{% endcodeblock %}  
  
2个方法极其相似，唯一不同的地方只有 if 里面的判断条件。要如何去除这里的重复呢？提取for里面的代码？虽然能去除一部分，但for这个循环体的代码不能一起去掉，而且要是for里面的代码复杂一点，这个方法就不适用了。我今天想介绍自己最近用的比较多的一种重构手法：**用模板方法去除for循环的重复代码。**  
  
模板方法大家都知道，我可以先建1个抽象类和2个子类要代替这2个方法，但在java1.5+以后，我们可以用enum（枚举）来轻易实现模板方法。重构后的代码如下：  

{% codeblock For.class lang:java %}
enum For {
        METHOD_A {
            @Override
            protected boolean match(String string) {
                return string.startsWith("A");
            }
        }, METHOD_B {
            @Override
            protected boolean match(String string) {
                return string.endsWith("B");
            }
        };

        public boolean method(List<String> list) {
            for(String string : list) {
                if(match(string)) {
                    return true;
                }
            }
            return false;
        }

        protected abstract boolean match(String string);
}
{% endcodeblock %}  
  
这里将相同的代码写成一个公共方法，不同的代码提取成一个抽象方法，让子类去具体实现。这样就实现了相同代码和不同代码的分离了。由于使用了enum，使得我们的创建类的代价为最小，只需一个enum类就搞定整个模板方法。  
  
这里只是抛砖引玉，我相信肯定其他人一定还有更好的方法，希望和大家一起共同探讨。  
