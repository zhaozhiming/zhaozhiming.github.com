---
layout: post
title: '如何重构一个圈复杂度超30的类'
date: 2013-10-08 20:43
description: '如何重构一个圈复杂度超30的类'
keywords: refactor
comments: true
categories: code
tags: [java, refactor]
---

下面的类是一个老系统的代码，现在放到 sonar 上面进行扫描，扫出来的结果发现复杂度超过了 30。

代码复杂度是指代码中的分支数量，比如有一个 if 分支，代码复杂度就加 1，如果 if 中有“||”或者“&&”那么代码复杂度就加 2，for 和 while 同理。一般复杂度超过 10 的类就算是比较复杂的了，而这个类的复杂度竟然达到了 30，代码的糟糕程度可见一斑，现在我们就来重构一下这个类的代码。

<!--more-->

原始文件在[这里](https://github.com/zhaozhiming/zhaozhiming.github.com/blob/source/source/file/SomeClient(old).java)。  

重构开始吧！

## 多处 String 类型非空判断

{% codeblock lang:java %}
if (StringUtil.isEmpty(username))
    throw new ICRClientException("username can not be null");
if (StringUtil.isEmpty(password))
    throw new ICRClientException("password can not be null");
if (udto == null)
throw new ICRClientException("ICRUploadDTO can not be null");
{% endcodeblock %}

重构之后：

{% codeblock lang:java %}
//将原来的地方替换为
checkStringParamEmpty(username, "username");
checkStringParamEmpty(password, "password");
checkStringParamEmpty(udto.getUrlPath(), "urlPath");
...
//新增一个方法
private void checkStringParamEmpty(String value, String name) throws ICRClientException {
    if (StringUtil.isEmpty(value)) {
        throw new ICRClientException(name + " can not be null");
    }
}
{% endcodeblock %}

原代码中不止这 3 个参数的校验，还有很多，越多参数的校验，我们重构后的复杂度就会越低。

**代码复杂度变化：原来是 3，修改后为 1。**

## 多 String 值判断

{% codeblock lang:java %}
if (!udto.getPriority().equals("0") && !udto.getPriority().equals("1")
&& !udto.getPriority().equals("2") && !udto.getPriority().equals("3"))
    throw new ICRClientException("priority must be 0/1/2/3");
{% endcodeblock %}

重构之后：

{% codeblock lang:java %}
//将原来代码替换为
checkValueWithinList(udto.getPriority());
...
//新增一个方法：
private void checkValueWithinList(String priority) throws ICRClientException {
    if (!Arrays.asList("0", "1", "2", "3").contains(priority)) {
        throw new ICRClientException("priority must be 0/1/2/3");
    }
}
{% endcodeblock %}

**代码复杂度变化：原来是 4，修改后为 1。**

## 对 list 的非空判断

{% codeblock lang:java %}
if (list == null || list.size() == 0)
    throw new ICRClientException("list can not be null");
{% endcodeblock %}

重构之后：

{% codeblock lang:java %}
//将原来的代码替换为
checkValueWithinList(udto.getPriority());
...
//新增一个方法
private void checkListNoNull(List list) throws ICRClientException {
    if (list.isEmpty()) throw new ICRClientException("list can not be null");
}
{% endcodeblock %}

**代码复杂度变化：原来是 2，修改后为 1。**

## 多个 catch 的内容相同

{% codeblock lang:java %}
int code = 0;
try {
    code = httpClient.executeMethod(post);
} catch (HttpException e) {
    throw new ICRClientException(e.getMessage(), e);
} catch (IOException e) {
    throw new ICRClientException(e.getMessage(), e);
}
{% endcodeblock %}

重构之后：

{% codeblock lang:java %}
//将原来的地方替换为
int code = executeHttpClient(httpClient, post);
...
//新增一个方法
private int executeHttpClient(HttpClient httpClient, PostMethod post) throws ICRClientException {
    int code;
    try {
        code = httpClient.executeMethod(post);
    } catch (Exception e) {
        throw new ICRClientException(e.getMessage(), e);
    }
    return code;
}
{% endcodeblock %}

**代码复杂度变化：原来是 2，修改后为 1。**

## if 判断结果复杂化

{% codeblock lang:java %}
if (code == 200) {
try {
    if (post.getResponseBodyAsString().equals("ok")) {
    return true;
}
} catch (IOException e) {
    throw new ICRClientException(e.getMessage(), e);
}
return false;
} else if (code == 500) {
    throw new ICRClientException(post.getResponseBodyAsString());
} else {
    throw new ICRClientException(code + ":" + post.getStatusText());
}  
{% endcodeblock %}

重构之后:

{% codeblock lang:java %}
//将原来代码替换为
return returnFinialResult(post, code);
...
//新增一个方法
private boolean returnFinialResult(PostMethod post, int code) throws ICRClientException, IOException {
if (code == 500) throw new ICRClientException(post.getResponseBodyAsString());
if (code != 200) throw new ICRClientException(code + ":" + post.getStatusText());

    try {
        return post.getResponseBodyAsString().equals("ok");
    } catch (IOException e) {
        throw new ICRClientException(e.getMessage(), e);
    }

}
{% endcodeblock %}

**代码复杂度变化：原来是 4，修改后为 3。**

## 本地变量始终不为 null

{% codeblock lang:java %}
public boolean uploadToICR(String username, String password, ICRUploadDTO udto) throws ICRClientException {
    HttpClient httpClient = null;
    PostMethod post = null;
    httpClient = new HttpClient();
    //some code here
    …
    } finally {
        if (post != null) {
            post.releaseConnection();
        }
    if (httpClient != null) {
        httpClient.getHttpConnectionManager().closeIdleConnections(0);
    }
}
{% endcodeblock %}

重构之后：

{% codeblock lang:java %}
public boolean uploadToICR(String username, String password, ICRUploadDTO udto) throws ICRClientException {
    HttpClient httpClient = new HttpClient();
    PostMethod post = null;
    //some code here
    …
    } finally {
        if (post != null) {
            post.releaseConnection();
        }
    }
}
{% endcodeblock %}

**代码复杂度变化：原来是 1，修改后为 0。**

## 读取 IO 流的方法，为什么要自己实现？

{% codeblock lang:java %}
private byte[] readData(InputStream ins) throws IOException {
    byte[] buf = new byte[2048];
    int count = 0;
    int len = 0;
    byte data[] = new byte[2048];
    byte[] result = null;
    try {
        while ((len = ins.read(data, 0, 2048)) != -1) {
            int newcount = count + len;
            if (newcount > buf.length) {
            byte newbuf[] = new byte[Math
            .max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(data, 0, buf, count, len);
        count = newcount;
        }
        result = new byte[count];
        System.arraycopy(buf, 0, result, 0, count);
    } finally {
        ins.close();
    }
    return result;

}
{% endcodeblock %}

在原代码里面自己实现了一个对读取 IO 流字节的方法，这个可以使用 apache-io 或者 guava 的 API 代替：

{% codeblock lang:java %}
//使用 apache io API 的实现：
byte[] bytes = IOUtils.toByteArray(inputStream);
//使用 guava API 的实现：
byte[] bytes1 = ByteStreams.toByteArray(inputStream);
{% endcodeblock %}

**代码复杂度变化：原来是很多，修改后为 0。**

最终重构后的版本见[这里](<https://github.com/zhaozhiming/zhaozhiming.github.com/blob/source/source/file/SomeClient(new).java>)，最后的代码复杂度从原来的 30 降到了 3。

代码写的比较仓促，没有写单元测试，其实最好的做法是在重构之前先写好单元测试，然后再慢慢修改原来的代码，每修改一处地方跑一遍单元测试，这样可以保证你的重构没有破坏原来的代码逻辑。
