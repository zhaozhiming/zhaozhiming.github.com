---
layout: post
title: "使用Array类处理基本数组对象"
date: 2012-05-22 14:35
comments: true
categories: code
tags: java
---
  
java里面的Arrays类有个asList方法，参数是1或多个Object对象，如果传入一个Object数组，则可以将该数组转化为List，但如果传入的是一个基本类型的数据（int,long,short 等），则无法将数组转换成正确的list，测试代码如下：  
  
{% codeblock Main.java lang:java %}
    public static void main(String[] args) {
        List<int[]> ints = asList(new int[]{1, 2});
        System.out.println(ints.size());  //1

        List<Integer> integers = asList(new Integer[]{1, 2});
        System.out.println(integers.size()); //2
    }
{% endcodeblock %}  
  
asList会将整个基本类型数组作为一个Object对象放到list里面，所以第一个List的size只有1。  
  
如何将基本类型数组装换为一个list呢？最简单暴力的方法就是遍历整个基本类型数组，再逐一往list里面添加。但这种方法不适用所有基本类型，需要为每种基本类型写一个方法。  
  
{% codeblock Main.java lang:java %}
    public List<Integer> arrayToList(int[] ints) {
        List<Integer> intList = new ArrayList<Integer>();
        for (int anInt : ints) {
            intList.add(anInt);
        }
        return intList;
    }
    
    public List<Long> arrayToList(Long[] longs) {
        List<Long> longList = new ArrayList<Long>();
        for (long anLong : longs) {
            longList.add(anLong);
        }
        return longList;
    }

　　.....other arrayToList method
{% endcodeblock %}  
  
重复代码会很多是吧，如何消除呢？下面就是帖子的重点了，利用Array类进行操作。  
  
Array类有几个方法比较常用：  
  
* public static int getLength(java.lang.Object array)：获取数组对象的长度。  
* public static java.lang.Object get(java.lang.Object array, int index)：根据下标获取数组对象的元素。  
* public static void set(java.lang.Object array, int index, java.lang.Object value)：根据下标插入数组对象元素。    
  
于是上面那些方法就可以用一个方法搞定了，如下：    
  
{% codeblock Main.java lang:java %}
    public static void main(String[] args) {
        int[] ints = {1, 2};
        long[] longs = {1L, 2L, 3L};
        char[] chars = {'a', 'b', 'c', 'd'};

        System.out.println(arrayToList(ints).size()); //2
        System.out.println(arrayToList(longs).size()); //3
        System.out.println(arrayToList(chars).size()); //4
    }

    public static List<Object> arrayToList(Object array) {
        List<Object> list = new ArrayList<Object>();
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            list.add(Array.get(array, i));
        }
        return list;
    }
{% endcodeblock %}  
  
