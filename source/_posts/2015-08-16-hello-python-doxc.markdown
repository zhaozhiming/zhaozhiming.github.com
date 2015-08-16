---
layout: post
title: "python-docx使用简介"
date: 2015-08-16 20:18
description: python-docx使用简介
keywords: python-docx
comments: true
categories: code
tags: python-docx
---
  
{% img /images/post/2015-8/python-docx.png 400 300 %}  
  
使用word文档来展现内容是很多企业使用的方式之一，但如果文档内容比较多，或者需要制作很多重复文档的话，编写文档的过程会十分枯燥无聊，如果这个时候有一个程序可以帮助你来完成文档制作工作的话那就太好了，这就是[python-docx][python-docx]可以做的事情，下面我们就来看看python-docx的强大功能。  
  
<!--more-->  
  
## 安装
  
python-docx安装非常简单，可以使用`pip install python-docx`进行安装。  
  
## 创建文档
  
使用python-docx创建一个文档非常简单，只需要2行代码就可以搞定，代码如下：  
  
{% codeblock lang:python %}
document = Document()
document.save('foo.docx')
{% endcodeblock %}
  
## 编写段落
  
word文档最基本的内容就是一段段的文字信息，使用python-docx可以轻松的生成各种不同风格的段落。  
  
{% codeblock lang:python %}
# 普通段落
document.add_paragraph(u'我是普通文档')
# 带风格的段落
document.add_paragraph(u'我是好看的文档', style='IntenseQuote')
{% endcodeblock %}
  
style属性是使用一个定制好的风格，名称必须是document对象包含了这个style名称才能使用，否则在生成文档的过程会报错或者style没有生效。  
  
add_paragraph方法会返回一个paragraph对象，这个对象中有一个ParagraphFormat属性，通过设置ParagraphFormat属性的内容可以展示不同的风格：  
  
* alignment: 段落对齐方式，值为`WD_PARAGRAPH_ALIGNMENT`常量中的一个。
* line_spacing: 行间隔，单位可以有多种，详情参见[这里][length-unit]。
* first_line_indent: 首行缩进，值为整数，单位与line_spacing相同。
  
还有其他更多的属性就不一一介绍了，我们还可以更加细粒度地控制一个段落的风格，比如前两句话我们使用粗体展示，后两句使用斜体，这就需要用到Run对象的属性设置了，Run可以看成是段落中的一句话，一个段落包含了n个Run。  
  
{% codeblock lang:python %}
p = document.add_paragraph('')
run1 = p.add_run(u'句子一')
run1.bold = True

run2 = p.add_run(u'句子二')
run1.italic = True
{% endcodeblock %}
  
在document中我们还可以添加标题`Heading`，新建的文档有`0~9`级的标题让你选择（默认是1级），其实标题也是段落的一种，我们可以使用设置段落风格的方式来设置标题的风格。  
  
{% codeblock lang:python %}
h = document.add_heading(u'标题', 0)
h.paragraph_format.alignment = WD_ALIGN_PARAGRAPH.LEFT
{% endcodeblock %}
  
## 绘制表格
  
在文档中我们可能会添加一些表格作为数据内容的展示，在python-docx里面可以轻松的绘制不同的表格。  
  
{% codeblock lang:python %}
# 绘制一个2 * 2的表格
t = document.add_table(rows=2, cols=2)
t.cell(0, 0).text = u'单元1'
t.cell(0, 1).text = u'单元2'
t.cell(1, 0).text = u'单元3'
t.cell(1, 1).text = u'单元4'
{% endcodeblock %}
  
还可以对单元格进行合并：  
  
{% codeblock lang:python %}
t = document.add_table(rows=2, cols=2)
# 将第一行的2个单元格合并
merge_cell = t.cell(0, 0).merge(t.cell(0, 1))
merge_cell.text = u'合并单元格'
{% endcodeblock %}
  
默认的表格是不带边框的，如果想让表格有边框，可以使用`Table Grid`这种表格的style。  
  
{% codeblock lang:python %}
t = document.add_table(rows=2, cols=2)
t.style = 'Table Grid'
{% endcodeblock %}
  
## 添加图片
  
添加图片也非常简单，在添加图片的方法中输入图片的相对路径即可。  
  
{% codeblock lang:python %}
document.add_picture('demo.png')
{% endcodeblock %}
  
甚至可以添加网络上的图片到文档中，但前提是把图片下载到内存。  
  
{% codeblock lang:python %}
import urllib2
import StringIO

# img_url为网络图片url地址
image_from_url = urllib2.urlopen(img_url)
io_url = StringIO.StringIO()
io_url.write(image_from_url.read())
io_url.seek(0)
document.add_picture(io_url)
{% endcodeblock %}
  
## 文档风格
  
每个document对象里面都有一个styles对象，styles对象包含了文档可以使用的style，包括段落，表格，图片等的风格。  
  
{% codeblock lang:python %}
document = Document()
print len(document.styles)
for s in document.styles:
    print s.name

console---------------
160
Normal
Heading 1
Heading 2
Heading 3
Heading 4
Heading 5
Heading 6
Heading 7
Heading 8
Heading 9
Default Paragraph Font
Normal Table
...
{% endcodeblock %}
  
这是新建的文档所包含的style，比较齐全，但如果是基于已有文档的document就只能使用已有文档的styles，可能会比新建的文档少很多。  
  
{% codeblock lang:python %}
document = Document('exist.docx')
print len(document.styles)

console---------------
12
{% endcodeblock %}
  
python-docx是没有设置页眉页脚功能的，但是通过打开一个已经存在页眉页脚的文档，我们就可以拥有包含页眉页脚的document了。  
  
这些是python-docx的一些基本使用方法，更多的内容请参考官方文档，谢谢。  
  

[python-docx]: https://python-docx.readthedocs.org/en/latest/
[length-unit]: https://python-docx.readthedocs.org/en/latest/api/shared.html
