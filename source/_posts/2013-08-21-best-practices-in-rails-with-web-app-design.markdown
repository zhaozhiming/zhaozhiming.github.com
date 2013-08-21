---
layout: post
title: "《Agile Web Development with Rails》读后感--rails基于web设计的best practices"
date: 2012-08-05 18:18
comments: true
categories: read
tags: [ruby, rails]
---
  
最近看完《Agile Web Development with Rails》一书，受益匪浅。书中先是用一个简单的web应用带你进入Rails的世界，然后在你大致熟悉之后，再带你了解Rails的一些细节，比如ActiveRecord，ActiveController等。最让我觉得Rails美的是其中的一些best practices，这些都可以很好地借鉴到平时的开发中去。下面我简单举几个在Rails中我认为比较好的一些best practies。  
  
###数据库迁移  
  
在Rails中，SQL脚本可以通过命令来生成，生成的脚本以时间戳加意图命名，比如创建表的SQL脚本文件可能是“20120529151027_create_products.rb"，加时间戳可以让人一眼就知道脚本的执行顺序，实际上Rails在也是根据时间戳来执行脚本的。创建完脚本后只要简单的执行"rake db:migrate"命令即可完成脚本的执行，在Rails中会有脚本执行的记录，已经执行过的脚本不会重复执行。另外，在每个SQL脚本中，不仅有执行的操作，还有回滚的操作。比如：  

{% codeblock demo.rb lang:ruby %}
class AddPriceToLineItem < ActiveRecord::Migration
    def self.up
    　　add_column :line_items, :price, :decimal, :precision => 8, :scale => 2, :default => 0

    　　say_with_time "Updating prices..." do
      　　LineItem.find(:all).each do |lineitem|
        　　lineitem.update_attribute :price, lineitem.product.price
         end
    　　end
  　end

    def self.down
    　　remove_column :line_items, :price
    end
end
{% endcodeblock %}  
  
这里的up方法是正常执行时的操作，down方法则是回滚时所做的操作。如果发现执行的脚本有问题，简单地执行“rake db:rollback"即可回退到脚本执行前的状态。  
  
###Convention over Configuration  

在上面创建的脚本中可以看到，Rails的表名都是复数形式，因为Rails认为每张表都会存放很多个同一类型的数据，因此是复数，这种清晰的命名规范不仅体现在数据库，还有其他很多地方。比如，model的文件名都是单数形式存在，controller都是以复数形式存在。Rails将MVC各模块连接起来就是通过COC约定，举个简单例子，product的model文件是product.rb，controller文件是products_controller.rb，而view文件则是放在对应的product文件夹里面，每个view文件名以action方法名开头，这样在写代码的时候就可以不用具体指定要发送给哪个controller，给哪个model存储数据，让哪个页面显示数据，只要你遵守了COC约定，Rails会帮你跳转到最合适的地方。  
  
###分层的页面布局  
  
在书中的例子，作者会建议你将页面分成一个个小的局部文件。实际上Rails也是推荐你这么做的，局部文件只需以下划线开头，Rails就会帮你识别出来。小的页面文件结构简单，容易维护，比如有显示列标题的页面，其中嵌套显示具体行内容的页面，其中再嵌套分页页面文件。对比平时在工作里遇到的一个个硕大无比的jsp文件，rails的页面文件让人有了进入世外桃源的感觉。  
  
Rails还有其他很多的best practices，这里只是简单的介绍。Rails框架集中了很多开发中遇到的常见问题的解决方案，而且是一些最佳的解决方案，像集中了很多经验丰富的大师智慧一样，使用Rails并学习其中的best practices，能让你在web开发上少走很多弯路。   
  
