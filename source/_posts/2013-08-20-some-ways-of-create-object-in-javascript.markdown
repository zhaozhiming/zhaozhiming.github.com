---
layout: post
title: "javascript几种创建对象的方法"
date: 2012-05-23 14:50
comments: true
categories: code
tags: javascript
---
  
##1.工厂方法  
{% codeblock demo.js lang:javascript %}
function createPerson(name, age) {
    var person = new Object();
    person.name = name;
    person.age = age;

    person.sayHi = function() {
        return "name: " + this.name + " age: " + this.age;
    };

    return person;
}
{% endcodeblock %}  
优点：创建相同实例只有一处代码。  
缺点：不知道对象原型。  
  
<!--more-->  
{% codeblock test.js lang:javascript %}
var person1 = createPerson("zhang", 20);
var person2 = createPerson("li", 30);

console.log(person1.sayHi());
console.log(person2.sayHi());

console.log(person1 instanceof Person); //false
console.log(person2 instanceof Person); //false
{% endcodeblock %}  

## 2.构造函数  
{% codeblock demo.js lang:javascript %}
function Person(name, age) {
    this.name = name;
    this.age = age;

    this.sayHi = function() {
        return "name: " + this.name + " age:" + this.age;
    };
}
{% endcodeblock %}  
优点：可以知道实例原型。  
缺点：方法不是同一个方法实例。  

{% codeblock test.js lang:javascript %}
var person1 = new Person("zhang", 20);
var person2 = new Person("li", 30);

console.log(person1.sayHi()); //name: zhang age:20
console.log(person2.sayHi()); //name: li age:30

console.log(person1 instanceof Person); //true
console.log(person2 instanceof Person); //true

console.log(person1.sayHi == person2.sayHi); //false
{% endcodeblock %}  
  
##3.原型方法  
  
{% codeblock demo.js lang:javascript %}
function Person() {}

Person.prototype.name = "zhang";
Person.prototype.age = 20;
Person.prototype.sayHi = function() {
    return "name: " + this.name + " age: " + this.age;
};
{% endcodeblock %}  
优点：共享方法实例对象。  
缺点：每个实例需要定义非方法属性。  
  
{% codeblock test.js lang:javascript %}
var person1 = new Person();
var person2 = new Person();

person2.name = "li";
person2.age = 30;

console.log(person1.sayHi()); //name: zhang age:20
console.log(person2.sayHi()); //name: li age:30

console.log(person1 instanceof Person); //true
console.log(person2 instanceof Person); //true

console.log(person1.sayHi == person2.sayHi); //true
{% endcodeblock %}  

##4.构造函数和原型的组合方法  

{% codeblock demo.js lang:javascript %}
function Person(name, age) {
    this.name = name;
    this.age = age;
}

Person.prototype.sayHi = function() {
    return "name: " + this.name + " age: " + this.age;
};
{% endcodeblock %}  
优点：非方法属性在构造函数中定义，方法属性则在原型中定义。  

**总结：最后一种方法是比较好的创建对象的方式，综合了前面3种方式的优点。每个实例对象都有自己实例属性的一份副本，但同时共享着方法的引用，最大限度节省了内存。**  

