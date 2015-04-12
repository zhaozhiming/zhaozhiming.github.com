---
layout: post
title: "使用Spring-data进行Redis操作"
date: 2015-04-12 15:15
description: "使用Spring-data进行Redis操作"
keywords: spring,redis
comments: true
categories: code
tags: [spring,redis]
---
  
{% img /images/post/2015-4/spring-redis.jpg %}  
  
[Redis][redis]相信大家都听说过，它是一个开源的key-value缓存数据库，有很多Java的客户端支持，比较有名的有Jedis，JRedis等（见[这里][redis_java_client]）。当然我们可以使用客户端的原生代码实现redis的操作，但实际上在spring中就已经集成了这些客户端的使用，下面我们就以Jedis为例来介绍一下Spring中关于Redis的配置。  
  
<!--more-->  
  
## 下载相关依赖包

首先要下载spring和redis相关的依赖包，最新的jedis版本是2.6.2，还需要下载jackson的包，这个后面会介绍为什么需要，以gradle脚本示例如下。  
  
{% codeblock build.gradle lang:sh %}
    compile("redis.clients:jedis:" + jedisVersion)
    compile "org.springframework.data:spring-data-redis:" + springDataRedisVersion

    //json
    compile "com.fasterxml.jackson.core:jackson-databind:" + jacksonDatabindVersion
    compile "org.codehaus.jackson:jackson-mapper-asl:" + jacksonVersion
    compile "org.codehaus.jackson:jackson-core-asl:" + jacksonVersion
{% endcodeblock %} 
  
## spring配置jedis
在spring的xml配置文件中，做如下配置。
  
{% codeblock lang:xml %}
	<!-- 配置redis池，依次为最大实例数，最大空闲实例数，(创建实例时)最大等待时间，(创建实例时)是否验证 -->
    <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
        <property name="maxTotal" value="${redis.maxTotal}"/>
        <property name="maxIdle" value="${redis.maxIdle}"/>
        <property name="maxWaitMillis" value="${redis.maxWaitMillis}"/>
        <property name="testOnBorrow" value="${redis.testOnBorrow}"/>
    </bean>

    <!-- redis连接配置，依次为主机ip，端口，是否使用池，(usePool=true时)redis的池配置 -->
    <bean id="jedisFactory" class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory">
        <property name="hostName" value="${redis.host}"/>
        <property name="port" value="${redis.port}"/>
        <property name="usePool" value="true"/>
        <property name="poolConfig" ref="jedisPoolConfig"/>
    </bean>

	<!-- redis模板配置 -->
    <bean id="redisTemplate" class="org.springframework.data.redis.core.RedisTemplate">
        <property name="connectionFactory" ref="jedisFactory"/>
        <property name="defaultSerializer">
            <bean class="org.springframework.data.redis.serializer.StringRedisSerializer"/>
        </property>
    </bean>
{% endcodeblock %} 
  
## 序列化

在spring中进行redis存储，如果没有对key和value进行序列化，保存到redis中会出现乱码。注意看上面的redis模板配置，有个配置项是defaultSerializer，这里表示redis中的key和value遇到需要序列化的时候，都默认使用StringRedisSerializer这个类来进行序列化。如果不指定序列化的话，内容会带乱码。  
  
spring-data-redis的序列化类有下面这几个:  
  
* GenericToStringSerializer: 可以将任何对象泛化为字符串并序列化
* Jackson2JsonRedisSerializer: 跟JacksonJsonRedisSerializer实际上是一样的
* JacksonJsonRedisSerializer: 序列化object对象为json字符串
* JdkSerializationRedisSerializer: 序列化java对象
* StringRedisSerializer: 简单的字符串序列化
  
一般如果key-value都是string的话，使用StringRedisSerializer就可以了，如果需要保存对象为json的话推荐使用JacksonJsonRedisSerializer，它不仅可以将对象序列化，还可以将对象转换为json字符串并保存到redis中，但需要和jackson配合一起使用。  
  
## 简单的redis操作
  
代码示例如下，使用redis进行set和get操作。  
  
{% codeblock AccessTokenRepository.java lang:java %}
@Repository
public class AccessTokenRepository {
	//直接使用autowire就可以引用到配置文件中的redis-template
    @Autowired
    private RedisTemplate<String, AccessToken> template;

    private ValueOperations<String, AccessToken> operations;

    @PostConstruct
    public void init() {
    	//这里设置value的序列化方式为JacksonJsonRedisSerializer
        template.setValueSerializer(new JacksonJsonRedisSerializer<>(AccessToken.class));
        operations = template.opsForValue();
    }

    public void set(String key, AccessToken value) {
        operations.set(key, value);
    }

    public AccessToken get(String key) {
        return operations.get(key);
    }
}
{% endcodeblock %} 
  
调用set方法后，就可以在redis里面看到保存后的json字符串了。  
  
[redis]: http://redis.io/
[redis_java_client]: http://redis.io/clients#java
