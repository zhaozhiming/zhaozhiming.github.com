---
layout: post
title: "swift源码详解（X）——common/swob.py"
date: 2014-04-28 21:26
published: false
description: 
keywords: 
comments: true
categories: 
tags: 
---


## Match类
  
{% codeblock lang:python %}
class Match(object):
    """
    Wraps a Request's If-[None-]Match header as a friendly object.

    :param headerval: value of the header as a str
    """
    def __init__(self, headerval):
        self.tags = set()
        for tag in headerval.split(', '):
            if tag.startswith('"') and tag.endswith('"'):
                self.tags.add(tag[1:-1])
            else:
                self.tags.add(tag)

    def __contains__(self, val):
        return '*' in self.tags or val in self.tags
{% endcodeblock %}  
* 处理header值的一个类，如果header值有双引号将移除后再添加。
  
## Accept类
  
{% codeblock lang:python %}
class Accept(object):
    """
    Wraps a Request's Accept header as a friendly object.

    :param headerval: value of the header as a str
    """

    # RFC 2616 section 2.2
    token = r'[^()<>@,;:\"/\[\]?={}\x00-\x20\x7f]+'
    qdtext = r'[^"]'
    quoted_pair = r'(?:\\.)'
    quoted_string = r'"(?:' + qdtext + r'|' + quoted_pair + r')*"'
    extension = (r'(?:\s*;\s*(?:' + token + r")\s*=\s*" + r'(?:' + token +
                 r'|' + quoted_string + r'))')
    acc = (r'^\s*(' + token + r')/(' + token +
           r')(' + extension + r'*?\s*)$')
    acc_pattern = re.compile(acc)

    def __init__(self, headerval):
        self.headerval = headerval
{% endcodeblock %}  
* Accept类主要用来封装request的header，上面是一些属性变量和初始化方法。

### _get_types
  
{% codeblock lang:python %}
    def _get_types(self):
        types = []
        if not self.headerval:
            return []
        for typ in self.headerval.split(','):
            type_parms = self.acc_pattern.findall(typ)
            if not type_parms:
                raise ValueError('Invalid accept header')
            typ, subtype, parms = type_parms[0]
            parms = [p.strip() for p in parms.split(';') if p.strip()]

            seen_q_already = False
            quality = 1.0

            for parm in parms:
                name, value = parm.split('=')
                name = name.strip()
                value = value.strip()
                if name == 'q':
                    if seen_q_already:
                        raise ValueError('Multiple "q" params')
                    seen_q_already = True
                    quality = float(value)

            pattern = '^' + \
                (self.token if typ == '*' else re.escape(typ)) + '/' + \
                (self.token if subtype == '*' else re.escape(subtype)) + '$'
            types.append((pattern, quality, '*' not in (typ, subtype)))
        # sort candidates by quality, then whether or not there were globs
        types.sort(reverse=True, key=lambda t: (t[1], t[2]))
        return [t[0] for t in types]
{% endcodeblock %}  
* 如果header的值为空，则返回空的list。
* 将header的值按','号分割并且遍历，再按acc的正则表达式去查找，如果没有返回ValueError。
* 如果查找到，将找到的第一个type_parms分为3个变量，parms变量再按';'号分割成1个list。
* 遍历params这个list，每个元素再按'='分割，分别取name和value，如果name等于'q'且seen_q_already为True，则抛异常，否则seen_q_already为True，quality为value的浮点数值。
* 拼装一个正则表达式给pattern赋值，types添加一个有pattern，quality和布尔值的元素。
* 最后将types按quality排序，最后返回每个之前拼装好的pattern列表。

### best_match
  
{% codeblock lang:python %}
    def best_match(self, options):
        """
        Returns the item from "options" that best matches the accept header.
        Returns None if no available options are acceptable to the client.

        :param options: a list of content-types the server can respond with
        """
        try:
            types = self._get_types()
        except ValueError:
            return None
        if not types and options:
            return options[0]
        for pattern in types:
            for option in options:
                if re.match(pattern, option):
                    return option
        return None
{% endcodeblock %}  
* 先取types变量，types就是一个正则表达式的列表，如果取值的过程中抛异常则返回None。
* 如果types没有但options有，则返回options的第一个值。
* 如果types和options都有，则遍历types和options，如果option能在按正则表达式找到，则返回option，全部找不到返回None。
  
### _req_environ_property
  
{% codeblock lang:python %}
def _req_environ_property(environ_field):
    """
    Set and retrieve value of the environ_field entry in self.environ.
    (Used by both request and response)
    """
    def getter(self):
        return self.environ.get(environ_field, None)

    def setter(self, value):
        if isinstance(value, unicode):
            self.environ[environ_field] = value.encode('utf-8')
        else:
            self.environ[environ_field] = value

    return property(getter, setter, doc=("Get and set the %s property "
                    "in the WSGI environment") % environ_field)
{% endcodeblock %}  
* 封装了一对setter和getter方法，适用与request和respons。
  
### _req_body_property
  
{% codeblock lang:python %}
def _req_body_property():
    """
    Set and retrieve the Request.body parameter.  It consumes wsgi.input and
    returns the results.  On assignment, uses a StringIO to create a new
    wsgi.input.
    """
    def getter(self):
        body = self.environ['wsgi.input'].read()
        self.environ['wsgi.input'] = StringIO(body)
        return body

    def setter(self, value):
        self.environ['wsgi.input'] = StringIO(value)
        self.environ['CONTENT_LENGTH'] = str(len(value))

    return property(getter, setter, doc="Get and set the request body str")
{% endcodeblock %}  
* 封装了一对setter和getter方法来处理request body。
  
### _host_url_property
  
{% codeblock lang:python %}
def _host_url_property():
    """
    Retrieves the best guess that can be made for an absolute location up to
    the path, for example: https://host.com:1234
    """
    def getter(self):
        if 'HTTP_HOST' in self.environ:
            host = self.environ['HTTP_HOST']
        else:
            host = '%s:%s' % (self.environ['SERVER_NAME'],
                              self.environ['SERVER_PORT'])
        scheme = self.environ.get('wsgi.url_scheme', 'http')
        if scheme == 'http' and host.endswith(':80'):
            host, port = host.rsplit(':', 1)
        elif scheme == 'https' and host.endswith(':443'):
            host, port = host.rsplit(':', 1)
        return '%s://%s' % (scheme, host)

    return property(getter, doc="Get url for request/response up to path")
{% endcodeblock %}  
* 。
  