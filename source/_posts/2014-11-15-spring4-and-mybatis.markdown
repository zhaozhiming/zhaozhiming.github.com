---
layout: post
title: 如何在Spring4中配置Mybatis
date: 2014-11-15 21:09
description: 如何在Spring4中配置Mybatis
keywords: spring,mybatis
comments: true
categories: code
tags: [spring,mybatis]
---
  
{% img /images/post/2014-11/mybatis-spring.png %}  
  
[Spring4][spring]已经不支持Ibatis了，但Ibatis的升级版[Mybatis][mybatis]封装了支持Spring4的组件[mybatis-spring]，通过使用它们可以让你在Spring4中轻松地使用Mybatis。  
  
<!--more-->

## gradle 设置
现在基本上新兴的java项目包括android都使用[gradle][gradle]来做构建工具，gradle相比[ant][ant]来讲多了定义好的task，不需要每次都copy-paste相同的task到构建文件中，而相比[maven][maven]来说gradle比较灵活，可以像ant那样写简单的命令来进行copy或者mv等操作，总的来讲，gradle是集ant和maven优点于一身的新时代的构建工具。  
  
要在工程中引入Mybatis的组件，需要现在gradle的构建文件中增加Mybatis的依赖包。  
  
{% codeblock build.gradle lang:groovy %}
dependencies {
    compile 'org.mybatis:mybatis:3.2.8'
    compile 'org.mybatis:mybatis-spring:1.2.2'
}
{% endcodeblock %}   
  
## 在spring中配置Mybatis
引入依赖包之后，需要在spring的配置文件中进行Mybatis的配置。  
  
* 首先我们定义一个datasource，使用C3PO数据库连接池来进行管理。    
  
{% codeblock spring.xml lang:xml %}
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource " destroy-method="close">
        <property name="driverClass" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://192.168.36.10:3306/pebms"/>
        <property name="user" value="root"/>
        <property name="password" value="root"/>
        <property name="acquireIncrement" value="1"/>
        <property name="initialPoolSize" value="5"/>
        <property name="maxPoolSize" value="20"/>
        <property name="minPoolSize" value="5"/>
        <property name="maxStatements" value="100"/>
        <property name="testConnectionOnCheckout" value="true"/>
    </bean>
{% endcodeblock %}   
    
* 接着定义Mybatis的SessionFactory。  
	* dataSource: 我们之前定义的数据源
	* transactionFactory: 事务管理配置
	* configLocation: Mybatis的具体文件地址
	* mapperLocations: Mybatis的SQL映射文件  
  
{% codeblock spring.xml lang:xml %}
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource" />
        <property name="transactionFactory">
            <bean class="org.apache.ibatis.transaction.managed.ManagedTransactionFactory" />
        </property>
        <property name="configLocation" value="classpath:sql-map-config.xml"/>
        <property name="mapperLocations" value="classpath:sql-mapping/farmer.xml" />
    </bean>
{% endcodeblock %}   
    
* sql-map-config.xml简单示例如下，设置了缓存，延迟加载，超时时间等属性，更多的配置可以参照[这里][mybatis-config]。  
  
{% codeblock sql-map-config.xml lang:xml %}
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <settings>
        <setting name="cacheEnabled" value="true" />
        <setting name="lazyLoadingEnabled" value="true" />
        <setting name="multipleResultSetsEnabled" value="true" />
        <setting name="useColumnLabel" value="true" />
        <setting name="defaultExecutorType" value="REUSE" />
        <setting name="defaultStatementTimeout" value="25000" />
    </settings>
</configuration>
{% endcodeblock %}   
    
* sql的映射文件简单示例如下。  
  
{% codeblock farmer.xml lang:xml %}
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.farmer.baton.repo.FarmerMapper">
    <insert id="add-new-farmer" parameterType="com.farmer.baton.model.Farmer">
      insert into farmers(id, name, age) values (
        #{id},
        #{name},
        #{age}
      )
    </insert>
</mapper>
{% endcodeblock %}   
  
* 继续在spring.xml文件里进行Mybatis的配置，定义Mybatis的DAO(数据库访问对象)和事务控制，这里配置了DAO的包路径。  
  
{% codeblock spring.xml lang:xml %}
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <property name="basePackage" value="com.farmer.baton.repo" />
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory" />
    </bean>

    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource" />
    </bean>
{% endcodeblock %}   
  
## Mybatis responsitory编写
在前面的spring里面配置了DAO的包路径，我们下面要做的东西就比较就简单了。  

* 先在DAO包路径下定义一个DAO接口，这里不需要实现具体的内容，具体的sql在我们的映射文件里面体现。  
  
{% codeblock FarmerRepository.java lang:java %}
package com.farmer.baton.repo;

import com.farmer.baton.model.Farmer;

import java.util.List;

public interface FarmerRepository {
    List<Farmer> findAll();
}

{% endcodeblock %}   
  
* 在xml映射文件里面实现findAll方法，这里要注意方法的签名必须和映射文件的sql的id一致，包括方法名和id一致，方法参数类型和sql的parameterType一致，方法返回类型和sql的resultType或resultMap类型一致。  
  
{% codeblock farmer.xml lang:xml %}
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="com.farmer.baton.repo.FarmerRepository">
    <resultMap id="baseResultMap" type="com.farmer.baton.model.Farmer">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="age" property="age"/>
    </resultMap>

    <select id="findAll" resultMap="baseResultMap">
        select id as id,
        name as name,
        age as age
        from farmers
    </select>
</mapper>
{% endcodeblock %}   
  
* 写好Repository和映射SQL就可以了，程序在调用Repository方法的时候就会自动执行到相关的SQL。  

## 事务控制
* Mybatis的事务控制使用Spring的事务配置即可，配置如下:
 
{% codeblock spring.xml lang:xml %}
<beans xmlns:tx="http://www.springframework.org/schema/tx"
        http://www.springframework.org/schema/tx http://www.springframework.org/schema/tx/spring-tx-4.0.xsd">
    ...
    <tx:annotation-driven/>
    ...
{% endcodeblock %}   
  
* 然后在调用Repository的方面前面加上Transactional标签，如下所示:  
  
{% codeblock FarmerService.java lang:java %}
@Service
public class FarmerService {
    @Autowired
    private FarmerRepository farmerRepository;

    @Transactional
    public void updateTwoFarmers(Farmer farmer1, Farmer farmer2) {
        farmerRepository.updateZhangsan(farmer1);
        farmerRepository.updateWangwu(farmer2);
    }
}
{% endcodeblock %}   
  
## Mybatis语法
Mybatis的语法在功能上有了很大的改进，具体体现在SQL映射文件中。  

* 批量插入多条记录。  
  
{% codeblock lang:xml %}
    <insert id="add-new-farmer" parameterType="com.farmer.baton.model.Farmer">
      insert into farmers(id, name, age) values 
      <foreach collection="farmers" item="farmer" separator=",">	
      (
        #{id},
        #{name},
        #{age}
      )
      </foreach>
    </insert>
{% endcodeblock %}   
  
{% codeblock FarmerRepository lang:java %}
    void addFarmers(@Param("farmers") List<Farmer> farmers);
{% endcodeblock %}  
  
* 多参数SQL映射
  
{% codeblock lang:xml %}
    <resultMap id="farmer" type="com.farmer.baton.model.Farmer">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="age" property="age"/>
    </resultMap>

    <select id="selectFarmersByNameAndAge" parameterType="map" resultMap="farmer">
      select id as id,
        name as name,
        age as age
        from farmers
        where name = #{name}
        and age = #{age}
    </select>
{% endcodeblock %}   
  
{% codeblock FarmerRepository.java lang:java %}
    List<Farmer> selectFarmersByNameAndAge(@Param("name") String name, @Param("age") int age);
{% endcodeblock %}  
  
* 返回对象属性包含List
  
{% codeblock lang:xml %}
    <resultMap id="farmer" type="com.farmer.baton.model.Farmer">
        <id column="id" property="id"/>
        <result column="name" property="name"/>
        <result column="age" property="age"/>
        <collection property="farmland" ofType="com.farmer.baton.model.Farmland">
        	<result column="size" property="size"/>
        </collection>
    </resultMap>

    <select id="selectFarmersAndFarmlands" resultMap="farmer">
      select id as id,
        name as name,
        age as age
        from farmers a 
        left outer join farmerlands b on a.id = b.farmer_id
    </select>
{% endcodeblock %}   
  
{% codeblock FarmerRepository.java lang:java %}
    List<Farmer> selectFarmersAndFarmlands();
{% endcodeblock %}  
    
{% codeblock Farmer.java lang:java %}
    private String name;
    private int age;
    private List<Farmland> farmlands;
{% endcodeblock %}  
  
具体的Demo可以参考我的github工程[spring4-mybatis][spring4-mybatis-demo]。  
  
[spring]: http://spring.io/
[mybatis]: http://mybatis.github.io/mybatis-3/zh/index.html
[mybatis-spring]: http://mybatis.github.io/spring/zh/
[gradle]: http://www.gradle.org/
[ant]: http://ant.apache.org/
[maven]: http://maven.apache.org/
[mybatis-config]: http://mybatis.github.io/mybatis-3/zh/configuration.html#setting
[spring4-mybatis-demo]: https://github.com/zhaozhiming/spring4-mybatis