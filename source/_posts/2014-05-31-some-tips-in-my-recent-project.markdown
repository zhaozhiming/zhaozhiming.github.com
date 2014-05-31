---
layout: post
title: "最近小项目的一些记录（一）"
date: 2014-05-31 09:09
description: hibernate,angularjs,semantic-ui,jsoup
keywords: hibernate,angularjs
comments: true
categories: code
tags: [hibernate,angularjs,semantic-ui,jsoup]
---

最近花了2个星期的时间做了一个小网站，用来统计部门同事发布的博客情况。需求比较简单，做这个项目的时候就顺便把以前学到的东西整合到了一起，从前端到后台，从编码到部署（“全栈工程师”？呵呵），虽然事情比较琐碎但也学到了不少东西，下面就记录一下开发过程中遇到的一些问题。  
<!--more-->  
  
## 技术栈
这里先列举一下项目用到的一些技术，其实这些东西就是自己的工具箱，要慢慢丰富，要及时更新，这样才能做出来好东西。

* Spring4 MVC
* Hibernate orm
* Spring Data JPA
* AngularJS(Javascript MVW Framework)
* Semantic UI(CSS Framework)
* Velocity(for mail)
* Gradle
* SAE(Sina App Engine)
  
## Spring JPA
项目遇到不少的问题都来自JPA，也有一部分原因是由于SAE的MySql数据库是读写分离的两个库，所以要配置多个数据源才能在上面正常读写数据。  
JPA有个好处就是操作数据库时不用写太多代码，不用像以前一样写一个接口再写一个实现，只需要一个接口就可以完成基本的操作了，如果有特殊的操作则可以通过标签的方式来写sql。  
  
### Spring JPA配置多个persistence-unit(或多个数据源）

* 首先增加JPA的多persistence-unit的管理Bean。
{% codeblock lang:xml %}
	<bean id="persistenceUnitManager"
          class="org.springframework.orm.jpa.persistenceunit.DefaultPersistenceUnitManager">
        <property name="persistenceXmlLocation" value="classpath:META-INF/persistence.xml"/>
        <!--  comment dataSourceLooup to use jndi -->
        <property name="dataSourceLookup">
            <bean class="org.springframework.jdbc.datasource.lookup.BeanFactoryDataSourceLookup"/>
        </property>
    </bean>
{% endcodeblock %}  
  
* 然后配置多套DataSource，EntityManagerFactory，TransactionManger和jpa:repository，这里要重点注意jps:repository的配置也要有多套，否则启动就会报`No unique bean of type [javax.persistence.EntityManagerFactory] is defined: expected single bean but found 2`的错误。
{% codeblock lang:xml %}
	<!--write persistence unit config-->
    <bean id="writeJpaVendor"
          class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter">
        <property name="showSql" value="true"/>
        <property name="generateDdl" value="true"/>
    </bean>

    <bean id="writeDataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource " destroy-method="close">
        <property name="driverClass" value="com.mysql.jdbc.Driver"/>
        <property name="jdbcUrl" value="jdbc:mysql://localhost:3306/deblog"/>
        <property name="user" value="root"/>
        <property name="password" value="root"/>
        <property name="acquireIncrement" value="1"/>
        <property name="initialPoolSize" value="5"/>
        <property name="maxPoolSize" value="20"/>
        <property name="minPoolSize" value="5"/>
        <property name="maxStatements" value="100"/>
        <property name="testConnectionOnCheckout" value="true"/>
    </bean>

    <bean id="writeEntityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="persistenceUnitManager" ref="persistenceUnitManager" />
        <property name="persistenceUnitName" value="mainPersistenceUnit"/>
        <property name="jpaVendorAdapter" ref="writeJpaVendor" />
        <property name="loadTimeWeaver">
            <bean class="org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver" />
        </property>
        <property name="jpaDialect">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaDialect"/>
        </property>
    </bean>

    <bean id="writeTransactionManager" class="org.springframework.orm.jpa.JpaTransactionManager">
        <property name="entityManagerFactory" ref="writeEntityManagerFactory"/>
        <qualifier value="writeEm" />
        <property name="jpaDialect">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaDialect" />
        </property>
    </bean>

    <jpa:repositories base-package="com.github.dba.repo.write"
                      entity-manager-factory-ref="writeEntityManagerFactory"
                      transaction-manager-ref="writeTransactionManager" />

    <tx:annotation-driven transaction-manager="writeTransactionManager"/>
{% endcodeblock %}  
  
* 在persistence.xml文件中增加多个unit,这里以一个unit为例，多个的话只要persistence-unit的name不一样就可以了。下面的例子引用了之前的datasource的配置，可以不需要再配置一次jdbc信息。
{% codeblock lang:xml %}
    <persistence-unit name="mainPersistenceUnit" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.ejb.HibernatePersistence</provider>
        <non-jta-data-source>writeDataSource</non-jta-data-source>
        <class>com.github.dba.model.Blog</class>
        <class>com.github.dba.model.DepGroup</class>
        <class>com.github.dba.model.DepMember</class>
        <class>com.github.dba.model.BlogView</class>
        <properties>
            <property name="javax.persistence.jdbc.driver" value="com.mysql.jdbc.Driver" />
            <property name="javax.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/deblog" />
            <property name="javax.persistence.jdbc.user" value="root" />
            <property name="javax.persistence.jdbc.password" value="root" />

            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect" />
            <property name="hibernate.show_sql" value="true" />
            <property name="hibernate.hbm2ddl.auto" value="update" />
        </properties>
    </persistence-unit>
{% endcodeblock %}  
  
### Spring JPA动态查询
* 首先在model类中增加一个静态方法，用来生成本次查询的动态条件。
	* 下面的例子假设depGroup, website, startDate, endDate都可能有值。
	* where中的"=","<",">"可以在CriteriaBuilder中找到相应的方法，还有比如like等。
	* 如果是嵌套对象的话，比如Blog对象包含Author对象，要对比Author对象的值，则可以用这种方式来取值: `root.<Author>get("author").<String>get("groupName")`。

{% codeblock lang:java %}
    public static Specification<Blog> querySpecification(final String depGroup, final String website,
                                                         final String startDate, final String endDate) {
        return Specifications.where(new Specification<Blog>() {
            @Override
            public Predicate toPredicate(Root<Blog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                Predicate predicate = cb.conjunction();

                if (!Strings.isNullOrEmpty(depGroup) && !"*".equals(depGroup)) {
                    predicate.getExpressions().add(
                            cb.equal(root.<Author>get("author").<String>get("groupName"), depGroup));
                }

                if (!Strings.isNullOrEmpty(website) && !"*".equals(website)) {
                    predicate.getExpressions().add(
                            cb.equal(root.<String>get("website"), website));
                }

                if (!Strings.isNullOrEmpty(startDate)) {
                    try {
                        long time = DbaUtil.parseTimeStringToLong(startDate, PAGE_DATE_FORMAT);
                        predicate.getExpressions().add(cb.ge(root.<Long>get("time"), time));
                    } catch (ParseException e) {
                        throw new RuntimeException(format("%s parse to date error:", startDate));
                    }
                }

                if (!Strings.isNullOrEmpty(endDate)) {
                    try {
                        long time = DbaUtil.parseTimeStringToLong(endDate, PAGE_DATE_FORMAT);
                        predicate.getExpressions().add(cb.le(root.<Long>get("time"), time));
                    } catch (ParseException e) {
                        throw new RuntimeException(format("%s parse to date error:", endDate));
                    }
                }

                return predicate;
            }
        });
    }
{% endcodeblock %}  
  
* 写好了动态查询条件后，就要把它放到查询语句里面了，比如要查询所有数据，示例如下，例子还加了一个对时间的排序条件。  
{% codeblock lang:java %}
        List<Blog> blogs = blogReadRepository.findAll(
                Blog.querySpecification(depGroup, website, startDate, endDate),
                new Sort(Sort.Direction.DESC, "time"));
{% endcodeblock %}  

### 嵌套对象
这个可能跟JPA没有多大关系，更多是跟Hibernate有关，但都属于db层面的，就写在一起了。  
  
比如有张表是Blog，这样用Hibernate-orm对应到程序就有一个Blog类，如果Blog属性比较多的话，后续就会变成了一个大类。我们想在数据库只对应一张表的情况下，可以对应到程序的多个类，比如Blog类下面有个Author的类，要怎么做呢？可以用`@Embedded`标签来解决这个问题。  
{% codeblock lang:java %}
@Entity(name = "blogs")
public class Blog {
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    private Author author;
}

@Embeddable
public class Author {
    @Basic
    private String name;
}
{% endcodeblock %}  
分离成多个对象的话，有个好处就是可以在不同的model添加不同的逻辑计算，避免把所有逻辑都放在一个类里面，这也是面向对象设计时要考虑的一个问题。但数据库始终只对应一张表，操作简单。  
  

下一篇: [最近小项目的一些记录（二）][url1]

[url1]: http://zhaozhiming.github.io/blog/2014/05/31/some-tips-in-my-recent-project-2/