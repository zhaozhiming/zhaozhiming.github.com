---
layout: post
title: "swift源码详解（四）——common/swob.py"
date: 2014-05-04 21:48
description: swift源码详解
keywords: swift
comments: true
categories: code
tags: swift
---

## [回swift代码结构目录][url1]

### RESPONSE_REASONS(常量)
  
<!--more-->  
{% codeblock lang:python %}
RESPONSE_REASONS = {
    100: ('Continue', ''),
    200: ('OK', ''),
    201: ('Created', ''),
    202: ('Accepted', 'The request is accepted for processing.'),
    204: ('No Content', ''),
    206: ('Partial Content', ''),
    301: ('Moved Permanently', 'The resource has moved permanently.'),
    302: ('Found', 'The resource has moved temporarily.'),
    303: ('See Other', 'The response to the request can be found under a '
          'different URI.'),
    304: ('Not Modified', ''),
    307: ('Temporary Redirect', 'The resource has moved temporarily.'),
    400: ('Bad Request', 'The server could not comply with the request since '
          'it is either malformed or otherwise incorrect.'),
    401: ('Unauthorized', 'This server could not verify that you are '
          'authorized to access the document you requested.'),
    402: ('Payment Required', 'Access was denied for financial reasons.'),
    403: ('Forbidden', 'Access was denied to this resource.'),
    404: ('Not Found', 'The resource could not be found.'),
    405: ('Method Not Allowed', 'The method is not allowed for this '
          'resource.'),
    406: ('Not Acceptable', 'The resource is not available in a format '
          'acceptable to your browser.'),
    408: ('Request Timeout', 'The server has waited too long for the request '
          'to be sent by the client.'),
    409: ('Conflict', 'There was a conflict when trying to complete '
          'your request.'),
    410: ('Gone', 'This resource is no longer available.'),
    411: ('Length Required', 'Content-Length header required.'),
    412: ('Precondition Failed', 'A precondition for this request was not '
          'met.'),
    413: ('Request Entity Too Large', 'The body of your request was too '
          'large for this server.'),
    414: ('Request URI Too Long', 'The request URI was too long for this '
          'server.'),
    415: ('Unsupported Media Type', 'The request media type is not '
          'supported by this server.'),
    416: ('Requested Range Not Satisfiable', 'The Range requested is not '
          'available.'),
    417: ('Expectation Failed', 'Expectation failed.'),
    422: ('Unprocessable Entity', 'Unable to process the contained '
          'instructions'),
    499: ('Client Disconnect', 'The client was disconnected during request.'),
    500: ('Internal Error', 'The server has either erred or is incapable of '
          'performing the requested operation.'),
    501: ('Not Implemented', 'The requested method is not implemented by '
          'this server.'),
    502: ('Bad Gateway', 'Bad gateway.'),
    503: ('Service Unavailable', 'The server is currently unavailable. '
          'Please try again at a later time.'),
    504: ('Gateway Timeout', 'A timeout has occurred speaking to a '
          'backend server.'),
    507: ('Insufficient Storage', 'There was not enough space to save the '
          'resource. Drive: %(drive)s'),
}
{% endcodeblock %}  
* 定义了各个状态码的定义和详细描述信息。
  
### 类UTC
  
{% codeblock lang:python %}
class _UTC  (tzinfo):
    """
    A tzinfo class for datetime objects that returns a 0 timedelta (UTC time)
    """
    def dst(self, dt):
        return timedelta(0)
    utcoffset = dst

    def tzname(self, dt):
        return 'UTC'
UTC = _UTC()  
{% endcodeblock %}  
* 定义了一个UTC的timezone类。
  
### _datetime_property
  
{% codeblock lang:python %}
def _datetime_property(header):
    """
    Set and retrieve the datetime value of self.headers[header]
    (Used by both request and response)
    The header is parsed on retrieval and a datetime object is returned.
    The header can be set using a datetime, numeric value, or str.
    If a value of None is given, the header is deleted.

    :param header: name of the header, e.g. "Content-Length"
    """
    def getter(self):
        value = self.headers.get(header, None)
        if value is not None:
            try:
                parts = parsedate(self.headers[header])[:7]
                return datetime(*(parts + (UTC,)))
            except Exception:
                return None

    def setter(self, value):
        if isinstance(value, (float, int, long)):
            self.headers[header] = time.strftime(
                "%a, %d %b %Y %H:%M:%S GMT", time.gmtime(value))
        elif isinstance(value, datetime):
            self.headers[header] = value.strftime("%a, %d %b %Y %H:%M:%S GMT")
        else:
            self.headers[header] = value

    return property(getter, setter,
                    doc=("Retrieve and set the %s header as a datetime, "
                         "set it with a datetime, int, or str") % header)
{% endcodeblock %}  
* 定义dateime属性的getter和setter方法。
  
### _header_property
  
{% codeblock lang:python %}
def _header_property(header):
    """
    Set and retrieve the value of self.headers[header]
    (Used by both request and response)
    If a value of None is given, the header is deleted.

    :param header: name of the header, e.g. "Transfer-Encoding"
    """
    def getter(self):
        return self.headers.get(header, None)

    def setter(self, value):
        self.headers[header] = value

    return property(getter, setter,
                    doc="Retrieve and set the %s header" % header)
{% endcodeblock %}  
* 定义header属性的getter和setter方法。
  
### _header_int_property
  
{% codeblock lang:python %}
def _header_int_property(header):
    """
    Set and retrieve the value of self.headers[header]
    (Used by both request and response)
    On retrieval, it converts values to integers.
    If a value of None is given, the header is deleted.

    :param header: name of the header, e.g. "Content-Length"
    """
    def getter(self):
        val = self.headers.get(header, None)
        if val is not None:
            val = int(val)
        return val

    def setter(self, value):
        self.headers[header] = value

    return property(getter, setter,
                    doc="Retrieve and set the %s header as an int" % header)
{% endcodeblock %}  
* 定义整数型header属性的getter和setter方法。
  



[url1]: http://zhaozhiming.github.io/blog/2014/04/19/swift-code-explain-total/