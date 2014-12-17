---
layout: post
title: "使用TPP来控制TDD的节奏做好单元测试"
date: 2014-12-14 06:58
description: 使用TPP来控制TDD的节奏做好单元测试
keywords: TDD, TPP
comments: true
categories: code
tags: [TPP,TDD]
---
  
{% img /images/post/2014-12/tpp.jpeg %}
  
最近参加了一次编程活动，大家在一起讨论单元测试和TDD(测试驱动开发)，有人提到了[Uncle Bob][uncle_bob]的TPP(Transformation Priority Premise)——变形动作的优先顺序，可以帮助我们控制好TDD的节奏。  

<!--more-->
  
关于TPP的详细信息可以查看Uncle Bob的[这篇博文][tpp_blog]，核心的思想如下:  
  
* ({}–>nil) no code at all->code that employs nil
* (nil->constant)
* (constant->constant+) a simple constant to a more complex constant
* (constant->scalar) replacing a constant with a variable or an argument
* (statement->statements) adding more unconditional statements.
* (unconditional->if) splitting the execution path
* (scalar->array)
* (array->container)
* (statement->recursion)
* (if->while)
* (expression->function) replacing an expression with a function or algorithm
* (variable->assignment) replacing the value of a variable.
  
为了验证一下TPP是否有效，我用[PrimeFactorsKata][prime_factors_kata]来做练习，然后使用go语言来写，这样可以顺便练练go的语法。  
  
## ({}–>nil) no code at all->code that employs nil
首先我们写第一个测试，当传入`1`时返回一个空的集合，用最简单的代码实现功能，直接返回`nil`，这样就完成了从没有代码到`nil`的过程。  
PS: 这里我使用了[`stretchr/testify`][stretchr_testify]这个单元测试第三方包，它的使用方法就跟Java的Junit一样简单。  
  
{% codeblock prime_test.go lang:go %}
package prime

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_given_1_then_return_empty_list(t *testing.T) {
	assert.Equal(t, []int{}, Prime(1))
}
{% endcodeblock %}
  
{% codeblock prime.go lang:go %}
package prime

func Prime(num int) []int {
	return nil
}
{% endcodeblock %}
  
## (nil->constant)
执行测试我们发现测试案例是不通过的，所以我们需要让测试变绿，让方法返回一个空的集合。  
  
{% codeblock prime.go lang:go %}
func Prime(num int) []int {
	return []int{}
}
{% endcodeblock %}
  

## (constant->constant+) a simple constant to a more complex constant 
## (unconditional->if) splitting the execution path
接着我们写第二个测试，当传入`2`时返回只有`2`的集合，同时修改实现代码，让原来返回的空集合变成包含`2`的一个集合，同时加上判断，如果`num`小于2时还是返回空集合。  
  
{% codeblock prime_test.go lang:go %}
func Test_given_2_then_return_2(t *testing.T) {
	assert.Equal(t, []int{2}, Prime(2))
}
{% endcodeblock %}
  
{% codeblock prime.go lang:go %}
func Prime(num int) []int {
	if num < 2 {
		return []int{}
	}
	return []int{2}
}
{% endcodeblock %}
  
## constant->scalar replacing a constant with a variable or an argument
通过了前面2个测试之后，我们接着写第三个测试，要返回一个包含`3`的集合，需要将原来写死的常量`2`变成`num`。  
  
{% codeblock prime_test.go lang:go %}
func Test_given_3_then_return_3(t *testing.T) {
	assert.Equal(t, []int{3}, Prime(3))
}
{% endcodeblock %}
  
{% codeblock prime.go lang:go %}
func Prime(num int) []int {
	if num < 2 {
		return []int{}
	}
	return []int{num}
}
{% endcodeblock %}
  
## (statement->statements) adding more unconditional statements
继续增加测试，传入`4`返回包含`2`和`2`的集合，这次实现代码的改动比较大，基本算法已经出来了，使用`num`来对`2`求余数，然后同时添加除数和被除数，这个时候单元测试的效果就出来了，如果实现代码没有写对，以前的测试会失败，需要你不断修改，保证通过所有测试。  

{% codeblock prime_test.go lang:go %}
func Test_given_4_then_return_2_2(t *testing.T) {
	assert.Equal(t, []int{2, 2}, Prime(4))
}
{% endcodeblock %}


{% codeblock prime.go lang:go %}
func Prime(num int) []int {
	if num < 2 {
		return []int{}
	}

	result := []int{}
	mod := num % 2
	if mod == 0 && num > 2 {
		result = append(result, 2)
		result = append(result, num/2)
	} else {
		result = append(result, num)
	}

	return result
}
{% endcodeblock %}
  
## 添加测试
增加测试，传入`6`得到`2`和`3`的集合，实现代码没有改动。  
  
{% codeblock prime_test.go lang:go %}
func Test_given_6_then_return_2_3(t *testing.T) {
	assert.Equal(t, []int{2, 3}, Prime(6))
}
{% endcodeblock %}

## (if->while)
再增加一个参数`8`，返回`2-2-2`集合的测试，这个测试迫使我们的实现代码做循环，所以这是一个if到while的过程。  
  
{% codeblock prime_test.go lang:go %}
func Test_given_8_then_return_2_2_2(t *testing.T) {
	assert.Equal(t, []int{2, 2, 2}, Prime(8))
}
{% endcodeblock %}


{% codeblock prime.go lang:go %}
func Prime(num int) []int {
	if num < 2 {
		return []int{}
	}

	result := []int{}
	for div := 2; div <= num; {
		if num%div == 0 {
			result = append(result, div)
			num /= div
		} else {
			div++
		}
	}
	return result
}
{% endcodeblock %}
  
## 代码写完了吗？
这个时候实际上我们的功能已经实现了，如果不放心我们就继续增加几个测试案例，结果验证都是通过的。  
  
{% codeblock prime_test.go lang:go %}
func Test_given_9_then_return_3_3(t *testing.T) {
	assert.Equal(t, []int{3, 3}, Prime(9))
}

func Test_given_20_then_return_2_2_5(t *testing.T) {
	assert.Equal(t, []int{2, 2, 5}, Prime(20))
}

func Test_given_30_then_return_2_3_5(t *testing.T) {
	assert.Equal(t, []int{2, 3, 5}, Prime(30))
}

func Test_given_64_then_return_2_2_2_2_2_2(t *testing.T) {
	assert.Equal(t, []int{2, 2, 2, 2, 2, 2}, Prime(64))
}

func Test_given_10984_then_return_2_2_2_1373(t *testing.T) {
	assert.Equal(t, []int{2, 2, 2, 1373}, Prime(10984))
}
{% endcodeblock %}
  
## 总结
虽然看了Uncle Bob的TPP，但是觉得根据他的核心思想还是不容易控制TDD的节奏，实际上在做4-2-2的测试的时候我自己就想好了算法，如果没有想好算法要驱动出实际代码来比较难，可能还需要更多的练习才能达到TPP的效果吧。  


[uncle_bob]: http://en.wikipedia.org/wiki/Robert_Cecil_Martin
[tpp_blog]: http://blog.8thlight.com/uncle-bob/2013/05/27/TheTransformationPriorityPremise.html
[prime_factors_kata]: http://butunclebob.com/ArticleS.UncleBob.ThePrimeFactorsKata
[stretchr_testify]: https://github.com/stretchr/testify







