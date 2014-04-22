---
layout: post
title: "swift源码详解（三）——proxy/controllers/base.py"
date: 2014-04-20 21:36
description: swift源码详解
keywords: 
comments: true
categories: code
tags: swift
---

### update_headers
  
<!--more-->  
{% codeblock lang:python %}
def update_headers(response, headers):
    """
    Helper function to update headers in the response.

    :param response: swob.Response object
    :param headers: dictionary headers
    """
    if hasattr(headers, 'items'):
        headers = headers.items()
    for name, value in headers:
        if name == 'etag':
            response.headers[name] = value.replace('"', '')
        elif name not in ('date', 'content-length', 'content-type',
                          'connection', 'x-put-timestamp', 'x-delete-after'):
            response.headers[name] = value
{% endcodeblock %}  
* 更新response的header。
* 查看headers中是否'items'属性，有的话给headers赋值。
* 遍历headers中的每个header,如果是'etag'，则去除值中的双引号，并写到response的header中，如果header不是特殊的header，则写到response的header中。
  
### source_key
  
{% codeblock lang:python %}
def source_key(resp):
    """
    Provide the timestamp of the swift http response as a floating
    point value.  Used as a sort key.

    :param resp: bufferedhttp response object
    """
    return float(resp.getheader('x-put-timestamp') or
                 resp.getheader('x-timestamp') or 0)
{% endcodeblock %}  
* 依次获取response中的header'x-put-timestamp'和'x-timestamp'的值，如果有值则返回，没有则返回0。
  
### delay_denial
  
{% codeblock lang:python %}
def delay_denial(func):
    """
    Decorator to declare which methods should have any swift.authorize call
    delayed. This is so the method can load the Request object up with
    additional information that may be needed by the authorization system.

    :param func: function for which authorization will be delayed
    """
    func.delay_denial = True

    @functools.wraps(func)
    def wrapped(*a, **kw):
        return func(*a, **kw)
    return wrapped
{% endcodeblock %}  
* 方法标签，标识了该标签的方法表示与swift.authorize有关，会延迟认证。
  
### get_account_memcache_key
  
{% codeblock lang:python %}
def get_account_memcache_key(account):
    cache_key, env_key = _get_cache_key(account, None)
    return cache_key
{% endcodeblock %}  
* 获取account的缓存key。
  
### get_container_memcache_key
  
{% codeblock lang:python %}
def get_container_memcache_key(account, container):
    if not container:
        raise ValueError("container not provided")
    cache_key, env_key = _get_cache_key(account, container)
    return cache_key
{% endcodeblock %}  
* 获取container的缓存key。
  
### _prep_headers_to_info
  
{% codeblock lang:python %}
def _prep_headers_to_info(headers, server_type):
    """
    Helper method that iterates once over a dict of headers,
    converting all keys to lower case and separating
    into subsets containing user metadata, system metadata
    and other headers.
    """
    meta = {}
    sysmeta = {}
    other = {}
    for key, val in dict(headers).iteritems():
        lkey = key.lower()
        if is_user_meta(server_type, lkey):
            meta[strip_user_meta_prefix(server_type, lkey)] = val
        elif is_sys_meta(server_type, lkey):
            sysmeta[strip_sys_meta_prefix(server_type, lkey)] = val
        else:
            other[lkey] = val
    return other, meta, sysmeta
{% endcodeblock %}  
* 将header根据server_type进行分类，以x-*-meta开头的为用户信息类，以x-*-sysmeta开头的为系统信息类，其他的为other类。
  
### headers_to_account_info
  
{% codeblock lang:python %}
def headers_to_account_info(headers, status_int=HTTP_OK):
    """
    Construct a cacheable dict of account info based on response headers.
    """
    headers, meta, sysmeta = _prep_headers_to_info(headers, 'account')
    return {
        'status': status_int,
        # 'container_count' anomaly:
        # Previous code sometimes expects an int sometimes a string
        # Current code aligns to str and None, yet translates to int in
        # deprecated functions as needed
        'container_count': headers.get('x-account-container-count'),
        'total_object_count': headers.get('x-account-object-count'),
        'bytes': headers.get('x-account-bytes-used'),
        'meta': meta,
        'sysmeta': sysmeta
    }
{% endcodeblock %}  
* 将account的header进行分类，返回包含account信息的字典。
  
### headers_to_container_info
  
{% codeblock lang:python %}
def headers_to_container_info(headers, status_int=HTTP_OK):
    """
    Construct a cacheable dict of container info based on response headers.
    """
    headers, meta, sysmeta = _prep_headers_to_info(headers, 'container')
    return {
        'status': status_int,
        'read_acl': headers.get('x-container-read'),
        'write_acl': headers.get('x-container-write'),
        'sync_key': headers.get('x-container-sync-key'),
        'object_count': headers.get('x-container-object-count'),
        'bytes': headers.get('x-container-bytes-used'),
        'versions': headers.get('x-versions-location'),
        'cors': {
            'allow_origin': meta.get('access-control-allow-origin'),
            'expose_headers': meta.get('access-control-expose-headers'),
            'max_age': meta.get('access-control-max-age')
        },
        'meta': meta,
        'sysmeta': sysmeta
    }
{% endcodeblock %}  
* 将container的header进行分类，返回包含container信息的字典。
  
### headers_to_object_info
  
{% codeblock lang:python %}
def headers_to_object_info(headers, status_int=HTTP_OK):
    """
    Construct a cacheable dict of object info based on response headers.
    """
    headers, meta, sysmeta = _prep_headers_to_info(headers, 'object')
    info = {'status': status_int,
            'length': headers.get('content-length'),
            'type': headers.get('content-type'),
            'etag': headers.get('etag'),
            'meta': meta
            }
    return info
{% endcodeblock %}  
* 将object的header进行分类，返回包含object信息的字典。
  
### cors_validation
  
{% codeblock lang:python %}
def cors_validation(func):
    """
    Decorator to check if the request is a CORS request and if so, if it's
    valid.

    :param func: function to check
    """
    @functools.wraps(func)
    def wrapped(*a, **kw):
        controller = a[0]
        req = a[1]

        # The logic here was interpreted from
        #    http://www.w3.org/TR/cors/#resource-requests

        # Is this a CORS request?
        req_origin = req.headers.get('Origin', None)
        if req_origin:
            # Yes, this is a CORS request so test if the origin is allowed
            container_info = \
                controller.container_info(controller.account_name,
                                          controller.container_name, req)
            cors_info = container_info.get('cors', {})

            # Call through to the decorated method
            resp = func(*a, **kw)

            if controller.app.strict_cors_mode and \
                    not controller.is_origin_allowed(cors_info, req_origin):
                return resp
{% endcodeblock %}  
* 方法标签，对CORS请求进行验证。
* 先判断该请求是否是一个跨域资源共享（CORS）请求，是的话先获取container的信息，再根据container信息获取cors信息。
* 如果controller的cors mode存在就判断原请求是否被允许，允许的话返回response。
  
{% codeblock lang:python %}
            # Expose,
            #  - simple response headers,
            #    http://www.w3.org/TR/cors/#simple-response-header
            #  - swift specific: etag, x-timestamp, x-trans-id
            #  - user metadata headers
            #  - headers provided by the user in
            #    x-container-meta-access-control-expose-headers
            if 'Access-Control-Expose-Headers' not in resp.headers:
                expose_headers = [
                    'cache-control', 'content-language', 'content-type',
                    'expires', 'last-modified', 'pragma', 'etag',
                    'x-timestamp', 'x-trans-id']
                for header in resp.headers:
                    if header.startswith('X-Container-Meta') or \
                            header.startswith('X-Object-Meta'):
                        expose_headers.append(header.lower())
                if cors_info.get('expose_headers'):
                    expose_headers.extend(
                        [header_line.strip()
                         for header_line in
                         cors_info['expose_headers'].split(' ')
                         if header_line.strip()])
                resp.headers['Access-Control-Expose-Headers'] = \
                    ', '.join(expose_headers)
{% endcodeblock %}  
* 方法标签，对CORS请求进行验证。
* 先判断该请求是否是一个跨域资源共享（CORS）请求，是的话先获取container的信息，再根据container信息获取cors信息。
* 根据controller的cors mode判断cors请求是否被允许，是的话返回response。
  
{% codeblock lang:python %}
            # The user agent won't process the response if the Allow-Origin
            # header isn't included
            if 'Access-Control-Allow-Origin' not in resp.headers:
                if cors_info['allow_origin'] and \
                        cors_info['allow_origin'].strip() == '*':
                    resp.headers['Access-Control-Allow-Origin'] = '*'
                else:
                    resp.headers['Access-Control-Allow-Origin'] = req_origin

            return 	resp
        else:
            # Not a CORS request so make the call as normal
            return func(*a, **kw)

    return wrapped
{% endcodeblock %}  
* 如果response里面不包含'Access-Control-Allow-Origin' header，则加上该header。
  
### get_object_info
  
{% codeblock lang:python %}
def get_object_info(env, app, path=None, swift_source=None):
    """
    Get the info structure for an object, based on env and app.
    This is useful to middlewares.

    .. note::

        This call bypasses auth. Success does not imply that the request has
        authorization to the object.
    """
    (version, account, container, obj) = \
        split_path(path or env['PATH_INFO'], 4, 4, True)
    info = _get_object_info(app, env, account, container, obj,
                            swift_source=swift_source)
    if not info:
        info = headers_to_object_info({}, 0)
    return info
{% endcodeblock %}  
* 根据env和app获取object的结构信息。
  
### get_container_info
  
{% codeblock lang:python %}
def get_container_info(env, app, swift_source=None):
    """
    Get the info structure for a container, based on env and app.
    This is useful to middlewares.

    .. note::

        This call bypasses auth. Success does not imply that the request has
        authorization to the container.
    """
    (version, account, container, unused) = \
        split_path(env['PATH_INFO'], 3, 4, True)
    info = get_info(app, env, account, container, ret_not_found=True,
                    swift_source=swift_source)
    if not info:
        info = headers_to_container_info({}, 0)
    return info
{% endcodeblock %}  
* 根据env和app获取container的结构信息。
  
