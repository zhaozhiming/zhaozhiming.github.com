---
layout: post
title: "swift源码详解（三）——proxy/controllers/base.py"
date: 2014-05-04 21:36
description: swift源码详解
keywords: swift
comments: true
categories: code
tags: swift
---

## [回swift代码结构目录][url1]

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
  
### get_account_info
  
{% codeblock lang:python %}
def get_account_info(env, app, swift_source=None):
    """
    Get the info structure for an account, based on env and app.
    This is useful to middlewares.

    .. note::

        This call bypasses auth. Success does not imply that the request has
        authorization to the account.
    """
    (version, account, _junk, _junk) = \
        split_path(env['PATH_INFO'], 2, 4, True)
    info = get_info(app, env, account, ret_not_found=True,
                    swift_source=swift_source)
    if not info:
        info = headers_to_account_info({}, 0)
    if info.get('container_count') is None:
        info['container_count'] = 0
    else:
        info['container_count'] = int(info['container_count'])
    return info
{% endcodeblock %}  
* 根据env和app获取account的结构信息。
  
### _get_cache_key

{% codeblock lang:python %}
def _get_cache_key(account, container):
    """
    Get the keys for both memcache (cache_key) and env (env_key)
    where info about accounts and containers is cached
    :param   account: The name of the account
    :param container: The name of the container (or None if account)
    :returns a tuple of (cache_key, env_key)
    """

    if container:
        cache_key = 'container/%s/%s' % (account, container)
    else:
        cache_key = 'account/%s' % account
    # Use a unique environment cache key per account and one container.
    # This allows caching both account and container and ensures that when we
    # copy this env to form a new request, it won't accidentally reuse the
    # old container or account info
    env_key = 'swift.%s' % cache_key
    return cache_key, env_key
{% endcodeblock %}  
* 获取account和container的缓存key，account是'account/account名'，container是'container/account名/container名'，还有env_key，值为'swift.缓存key'。
  
### get_object_env_key

{% codeblock lang:python %}
def get_object_env_key(account, container, obj):
    """
    Get the keys for env (env_key) where info about object is cached
    :param   account: The name of the account
    :param container: The name of the container
    :param obj: The name of the object
    :returns a string env_key
    """
    env_key = 'swift.object/%s/%s/%s' % (account,
                                         container, obj)
    return env_key
{% endcodeblock %}  
* 得到object的env_key，值为'swift.object/account名/container名/object名。
  
### set_info_cache

{% codeblock lang:python %}
def _set_info_cache(app, env, account, container, resp):
    """
    Cache info in both memcache and env.

    Caching is used to avoid unnecessary calls to account & container servers.
    This is a private function that is being called by GETorHEAD_base and
    by clear_info_cache.
    Any attempt to GET or HEAD from the container/account server should use
    the GETorHEAD_base interface which would than set the cache.

    :param  app: the application object
    :param  account: the unquoted account name
    :param  container: the unquoted container name or None
    :param resp: the response received or None if info cache should be cleared
    """

    if container:
        cache_time = app.recheck_container_existence
    else:
        cache_time = app.recheck_account_existence
    cache_key, env_key = _get_cache_key(account, container)

    if resp:
        if resp.status_int == HTTP_NOT_FOUND:
            cache_time *= 0.1
        elif not is_success(resp.status_int):
            cache_time = None
    else:
        cache_time = None

    # Next actually set both memcache and the env chache
    memcache = getattr(app, 'memcache', None) or env.get('swift.cache')
    if not cache_time:
        env.pop(env_key, None)
        if memcache:
            memcache.delete(cache_key)
        return

    if container:
        info = headers_to_container_info(resp.headers, resp.status_int)
    else:
        info = headers_to_account_info(resp.headers, resp.status_int)
    if memcache:
        memcache.set(cache_key, info, time=cache_time)
    env[env_key] = info
{% endcodeblock %}  
* 信息在缓存和env都各存一份，缓存一般用来避免对account和container没必要的调用，这是一个私有方法，主要被GETorHEAD_base和clear_info_cache方法调用。如果想通过HEAD和GET获取container/account信息，建议使用GETorHEAD_base方法，因为该方法会设置缓存信息。
* 检查container和account是否存在，再通过account和container获取缓存key。
* 根据response状态码设置缓存时间，如果缓存时间设置为None，则在env和缓存中移除cache_key缓存信息。
* 最后在缓存和env中设置container或account的info信息。
  
### _set_object_info_cache

{% codeblock lang:python %}
def _set_object_info_cache(app, env, account, container, obj, resp):
    """
    Cache object info env. Do not cache object informations in
    memcache. This is an intentional omission as it would lead
    to cache pressure. This is a per-request cache.

    Caching is used to avoid unnecessary calls to object servers.
    This is a private function that is being called by GETorHEAD_base.
    Any attempt to GET or HEAD from the object server should use
    the GETorHEAD_base interface which would then set the cache.

    :param  app: the application object
    :param  account: the unquoted account name
    :param  container: the unquoted container name or None
    :param  object: the unquoted object name or None
    :param resp: the response received or None if info cache should be cleared
    """

    env_key = get_object_env_key(account, container, obj)

    if not resp:
        env.pop(env_key, None)
        return

    info = headers_to_object_info(resp.headers, resp.status_int)
    env[env_key] = info
{% endcodeblock %}  
* object的信息只缓存在env中，没有缓存在memcache中是因为缓存起来的话会对缓存造成压力，这是前一次请求的缓存。缓存为了避免那些对object没必要的调用，这是一个私有方法，主要被GETorHEAD_base和clear_info_cache方法调用。如果想通过HEAD和GET获取container/account信息，建议使用GETorHEAD_base方法，因为该方法会设置缓存信息。
* 先获取object的env_key，如果response没有则在env中移除env_key的信息，最后在env中添加object的info信息。
  
### clear_info_cache

{% codeblock lang:python %}
def clear_info_cache(app, env, account, container=None):
    """
    Clear the cached info in both memcache and env

    :param  app: the application object
    :param  account: the account name
    :param  container: the containr name or None if setting info for containers
    """
    _set_info_cache(app, env, account, container, None)
{% endcodeblock %}  
* 在memcache和env中清除account或container的缓存信息。
  
### _get_info_cache

{% codeblock lang:python %}
def _get_info_cache(app, env, account, container=None):
    """
    Get the cached info from env or memcache (if used) in that order
    Used for both account and container info
    A private function used by get_info

    :param  app: the application object
    :param  env: the environment used by the current request
    :returns the cached info or None if not cached
    """

    cache_key, env_key = _get_cache_key(account, container)
    if env_key in env:
        return env[env_key]
    memcache = getattr(app, 'memcache', None) or env.get('swift.cache')
    if memcache:
        info = memcache.get(cache_key)
        if info:
            for key in info:
                if isinstance(info[key], unicode):
                    info[key] = info[key].encode("utf-8")
            env[env_key] = info
        return info
    return Noner, None)
{% endcodeblock %}  
* 私有方法，被get_info调用，在env和memcache中获取account和container信息，顺序是先env再memcache。
* 获取env_key和cache_keyi，如果env_key在env中存在，则返回env中的值。
* 如果env中没有，再从memcache中获取信息，将获取到的信息放到env中。
  
### _prepare_pre_auth_info_request

{% codeblock lang:python %}
def _prepare_pre_auth_info_request(env, path, swift_source):
    """
    Prepares a pre authed request to obtain info using a HEAD.

    :param env: the environment used by the current request
    :param path: The unquoted request path
    :param swift_source: value for swift.source in WSGI environment
    :returns: the pre authed request
    """
    # Set the env for the pre_authed call without a query string
    newenv = make_pre_authed_env(env, 'HEAD', path, agent='Swift',
                                 query_string='', swift_source=swift_source)
    # This is a sub request for container metadata- drop the Origin header from
    # the request so the it is not treated as a CORS request.
    newenv.pop('HTTP_ORIGIN', None)
    # Note that Request.blank expects quoted path
    return Request.blank(quote(path), environ=newenv)
{% endcodeblock %}  
* 准备一个做过认证的HEAD请求来获取信息。
  
### get_info

{% codeblock lang:python %}
def get_info(app, env, account, container=None, ret_not_found=False,
             swift_source=None):
    """
    Get the info about accounts or containers

    Note: This call bypasses auth. Success does not imply that the
          request has authorization to the info.

    :param app: the application object
    :param env: the environment used by the current request
    :param account: The unquoted name of the account
    :param container: The unquoted name of the container (or None if account)
    :returns: the cached info or None if cannot be retrieved
    """
    info = _get_info_cache(app, env, account, container)
    if info:
        if ret_not_found or is_success(info['status']):
            return info
        return None
    # Not in cache, let's try the account servers
    path = '/v1/%s' % account
    if container:
        # Stop and check if we have an account?
        if not get_info(app, env, account):
            return None
        path += '/' + container

    req = _prepare_pre_auth_info_request(
        env, path, (swift_source or 'GET_INFO'))
    # Whenever we do a GET/HEAD, the GETorHEAD_base will set the info in
    # the environment under environ[env_key] and in memcache. We will
    # pick the one from environ[env_key] and use it to set the caller env
    resp = req.get_response(app)
    cache_key, env_key = _get_cache_key(account, container)
    try:
        info = resp.environ[env_key]
        env[env_key] = info
        if ret_not_found or is_success(info['status']):
            return info
    except (KeyError, AttributeError):
        pass
    return None
{% endcodeblock %}  
* 从缓存中获取info信息，如果缓存中有且状态是success，则返回info。如果缓存没有，则发起1个不用认证的请求获取account和container的info信息。
  
### _get_object_info

{% codeblock lang:python %}
def _get_object_info(app, env, account, container, obj, swift_source=None):
    """
    Get the info about object

    Note: This call bypasses auth. Success does not imply that the
          request has authorization to the info.

    :param app: the application object
    :param env: the environment used by the current request
    :param account: The unquoted name of the account
    :param container: The unquoted name of the container
    :param obj: The unquoted name of the object
    :returns: the cached info or None if cannot be retrieved
    """
    env_key = get_object_env_key(account, container, obj)
    info = env.get(env_key)
    if info:
        return info
    # Not in cached, let's try the object servers
    path = '/v1/%s/%s/%s' % (account, container, obj)
    req = _prepare_pre_auth_info_request(env, path, swift_source)
    # Whenever we do a GET/HEAD, the GETorHEAD_base will set the info in
    # the environment under environ[env_key]. We will
    # pick the one from environ[env_key] and use it to set the caller env
    resp = req.get_response(app)
    try:
        info = resp.environ[env_key]
        env[env_key] = info
        return info
    except (KeyError, AttributeError):
        pass
    return None
{% endcodeblock %}  
* 先从env中获取object的info信息，如果没有则发起请求不认证的请求重新获取。
  
### close_swift_conn

{% codeblock lang:python %}
def close_swift_conn(src):
    """
    Force close the http connection to the backend.

    :param src: the response from the backend
    """
    try:
        # Since the backends set "Connection: close" in their response
        # headers, the response object (src) is solely responsible for the
        # socket. The connection object (src.swift_conn) has no references
        # to the socket, so calling its close() method does nothing, and
        # therefore we don't do it.
        #
        # Also, since calling the response's close() method might not
        # close the underlying socket but only decrement some
        # reference-counter, we have a special method here that really,
        # really kills the underlying socket with a close() syscall.
        src.nuke_from_orbit()  # it's the only way to be sure
    except Exception:
        pass
{% endcodeblock %}  
* 关闭swift连接，用了很底层的一个关闭socket连接的方法。
  
## GetOrHeadHandler类
  
### init方法

{% codeblock lang:python %}
	def __init__(self, app, req, server_type, ring, partition, path,
                 backend_headers):
        self.app = app
        self.ring = ring
        self.server_type = server_type
        self.partition = partition
        self.path = path
        self.backend_headers = backend_headers
        self.used_nodes = []
        self.used_source_etag = ''

        # stuff from request
        self.req_method = req.method
        self.req_path = req.path
        self.req_query_string = req.query_string
        self.newest = config_true_value(req.headers.get('x-newest', 'f'))

        # populated when finding source
        self.statuses = []
        self.reasons = []
        self.bodies = []
        self.source_headers = []
{% endcodeblock %}  
* GetOrHeadHandler类的初始化方法。  
  
### fast_forward

{% codeblock lang:python %}
    def fast_forward(self, num_bytes):
        """
        Will skip num_bytes into the current ranges.

        :params num_bytes: the number of bytes that have already been read on
                           this request. This will change the Range header
                           so that the next req will start where it left off.

        :raises NotImplementedError: if this is a multirange request
        :raises ValueError: if invalid range header
        :raises HTTPRequestedRangeNotSatisfiable: if begin + num_bytes
                                                  > end of range
        """
        if 'Range' in self.backend_headers:
            req_range = Range(self.backend_headers['Range'])

            if len(req_range.ranges) > 1:
                raise NotImplementedError()
            begin, end = req_range.ranges.pop()
            if begin is None:
                # this is a -50 range req (last 50 bytes of file)
                end -= num_bytes
            else:
                begin += num_bytes
            if end and begin > end:
                raise HTTPRequestedRangeNotSatisfiable()
            req_range.ranges = [(begin, end)]
            self.backend_headers['Range'] = str(req_range)
        else:
            self.backend_headers['Range'] = 'bytes=%d-' % num_bytes
{% endcodeblock %}  
* 先判断Range是否在后台进程的header中，如果没有，则在后台进程header中增加Range，值为'bytes='加num_bytes。  
* 如果有，先创建一个Range对象，判断如果Range对象的ranges如果大于1,则报NotImplementedError的异常。  
* 从rangs中取到开始和结束字节数，先检查两个字节数是否正确，不正确抛异常，正确的话将其重新放入到后台进程header中。  
  
### is_good_source
  
{% codeblock lang:python %}
    def is_good_source(self, src):
        """
        Indicates whether or not the request made to the backend found
        what it was looking for.

        :param src: the response from the backend
        :returns: True if found, False if not
        """
        if self.server_type == 'Object' and src.status == 416:
            return True
        return is_success(src.status) or is_redirection(src.status)
{% endcodeblock %}  
* 如果是一个Object请求，并且返回状态码是416，则返回True，否则返回状态码是否200～399。
  
### _make_app_iter
  
{% codeblock lang:python %}
    def _make_app_iter(self, req, node, source):
        """
        Returns an iterator over the contents of the source (via its read
        func).  There is also quite a bit of cleanup to ensure garbage
        collection works and the underlying socket of the source is closed.

        :param req: incoming request object
        :param source: The httplib.Response object this iterator should read
                       from.
        :param node: The node the source is reading from, for logging purposes.
        """
        try:
            nchunks = 0
            bytes_read_from_source = 0
            node_timeout = self.app.node_timeout
            if self.server_type == 'Object':
                node_timeout = self.app.recoverable_node_timeout
{% endcodeblock %}  
* 初始化本地变量，如果是object请求，则将节点超时时间设置为object的recoverable_node_timeout。
  
{% codeblock lang:python %}
            while True:
                try:
                    with ChunkReadTimeout(node_timeout):
                        chunk = source.read(self.app.object_chunk_size)
                        nchunks += 1
                        bytes_read_from_source += len(chunk)
                except ChunkReadTimeout:
                    exc_type, exc_value, exc_traceback = exc_info()
                    if self.newest or self.server_type != 'Object':
                        raise exc_type, exc_value, exc_traceback
                    try:
                        self.fast_forward(bytes_read_from_source)
                    except (NotImplementedError, HTTPException, ValueError):
                        raise exc_type, exc_value, exc_traceback
                    new_source, new_node = self._get_source_and_node()
                    if new_source:
                        self.app.exception_occurred(
                            node, _('Object'),
                            _('Trying to read during GET (retrying)'))
                        # Close-out the connection as best as possible.
                        if getattr(source, 'swift_conn', None):
                            close_swift_conn(source)
                        source = new_source
                        node = new_node
                        bytes_read_from_source = 0
                        continue
                    else:
                        raise exc_type, exc_value, exc_traceback
                if not chunk:
                    break
                with ChunkWriteTimeout(self.app.client_timeout):
                    yield chunk                        
{% endcodeblock %}  
* 通过一个无限循环，不断读取response的数据，累加读取的块数大小和字节总长度。
* 如果读取数据超时，则处理异常，如果请求不是Object则抛出最近的异常信息。
* 记录已读的字节范围，错误抛异常。
* 获取新的source和节点，如果source存在的话，则创建一个异常并关闭连接重新初始化，否则抛出异常。
* 如果读取不到数据，则跳出循环。
  
{% codeblock lang:python %}
                # This is for fairness; if the network is outpacing the CPU,
                # we'll always be able to read and write data without
                # encountering an EWOULDBLOCK, and so eventlet will not switch
                # greenthreads on its own. We do it manually so that clients
                # don't starve.
                #
                # The number 5 here was chosen by making stuff up. It's not
                # every single chunk, but it's not too big either, so it seemed
                # like it would probably be an okay choice.
                #
                # Note that we may trampoline to other greenthreads more often
                # than once every 5 chunks, depending on how blocking our
                # network IO is; the explicit sleep here simply provides a
                # lower bound on the rate of trampolining.
                if nchunks % 5 == 0:
                    sleep()

        except ChunkReadTimeout:
            self.app.exception_occurred(node, _('Object'),
                                        _('Trying to read during GET'))
            raise
        except ChunkWriteTimeout:
            self.app.logger.warn(
                _('Client did not read from proxy within %ss') %
                self.app.client_timeout)
            self.app.logger.increment('client_timeouts')
        except GeneratorExit:
            if not req.environ.get('swift.non_client_disconnect'):
                self.app.logger.warn(_('Client disconnected on read'))
        except Exception:
            self.app.logger.exception(_('Trying to send to client'))
            raise
        finally:
            # Close-out the connection as best as possible.
            if getattr(source, 'swift_conn', None):
                close_swift_conn(source)
{% endcodeblock %}  
* 每读取5个字节块，休眠一次。
* 读取数据超时抛异常。
* 写入数据超时记日志。
* 抛出各种异常后关闭连接。
  
### _get_source_and_node
  
{% codeblock lang:python %}
    def _get_source_and_node(self):
        self.statuses = []
        self.reasons = []
        self.bodies = []
        self.source_headers = []
        sources = []

        node_timeout = self.app.node_timeout
        if self.server_type == 'Object' and not self.newest:
            node_timeout = self.app.recoverable_node_timeout
{% endcodeblock %}  
* 初始化本地变量，设置node_timeout时间。

{% codeblock lang:python %}
        for node in self.app.iter_nodes(self.ring, self.partition):
            if node in self.used_nodes:
                continue
            start_node_timing = time.time()
            try:
                with ConnectionTimeout(self.app.conn_timeout):
                    conn = http_connect(
                        node['ip'], node['port'], node['device'],
                        self.partition, self.req_method, self.path,
                        headers=self.backend_headers,
                        query_string=self.req_query_string)
                self.app.set_node_timing(node, time.time() - start_node_timing)

                with Timeout(node_timeout):
                    possible_source = conn.getresponse()
                    # See NOTE: swift_conn at top of file about this.
                    possible_source.swift_conn = conn
            except (Exception, Timeout):
                self.app.exception_occurred(
                    node, self.server_type,
                    _('Trying to %(method)s %(path)s') %
                    {'method': self.req_method, 'path': self.req_path})
                continue
{% endcodeblock %}  
* 循环取节点，如果节点已经被使用了，则跳过该节点，否则封装http连接，设置节点时间。
* 获取请求结果，如果超时，则抛异常，跳出此次循环。

{% codeblock lang:python %}
            if self.is_good_source(possible_source):
                # 404 if we know we don't have a synced copy
                if not float(possible_source.getheader('X-PUT-Timestamp', 1)):
                    self.statuses.append(HTTP_NOT_FOUND)
                    self.reasons.append('')
                    self.bodies.append('')
                    self.source_headers.append('')
                    close_swift_conn(possible_source)
                else:
                    if self.used_source_etag:
                        src_headers = dict(
                            (k.lower(), v) for k, v in
                            possible_source.getheaders())
                        if src_headers.get('etag', '').strip('"') != \
                                self.used_source_etag:
                            self.statuses.append(HTTP_NOT_FOUND)
                            self.reasons.append('')
                            self.bodies.append('')
                            self.source_headers.append('')
                            continue

                    self.statuses.append(possible_source.status)
                    self.reasons.append(possible_source.reason)
                    self.bodies.append('')
                    self.source_headers.append('')
                    sources.append((possible_source, node))
                    if not self.newest:  # one good source is enough
                        break
{% endcodeblock %}  
* 如果返回结果合理，则判断返回结果中的PUT时间是否存在，不存在证明还没有同步，则返回404并关闭连接。
* 如果时间存在，则继续判断已用etag是否存在，存在的话从返回结果中取出etag值与之比较，不相等就返回404并关闭连接。
* 已用etag不存在，则将返回结果设置到自身属性中，并判断是否最新，是则跳出循环，取一个good source就足够了。

{% codeblock lang:python %}
            else:
                self.statuses.append(possible_source.status)
                self.reasons.append(possible_source.reason)
                self.bodies.append(possible_source.read())
                self.source_headers.append(possible_source.getheaders())
                if possible_source.status == HTTP_INSUFFICIENT_STORAGE:
                    self.app.error_limit(node, _('ERROR Insufficient Storage'))
                elif is_server_error(possible_source.status):
                    self.app.error_occurred(
                        node, _('ERROR %(status)d %(body)s '
                                'From %(type)s Server') %
                        {'status': possible_source.status,
                         'body': self.bodies[-1][:1024],
                         'type': self.server_type})

        if sources:
            sources.sort(key=lambda s: source_key(s[0]))
            source, node = sources.pop()
            for src, _junk in sources:
                close_swift_conn(src)
            self.used_nodes.append(node)
            src_headers = dict(
                (k.lower(), v) for k, v in
                possible_source.getheaders())
            self.used_source_etag = src_headers.get('etag', '').strip('"')
            return source, node
        return None, None
{% endcodeblock %}  
* 如果返回结果不是一个good source，则将返回结果信息设置到自身属性，如果返回状态是507,则将节点加入到错误列表，如果返回状态是其他500以上的数字，则抛出异常。
* 循环结束后，如果取到了source，则先将sources进行排序然后取第一个，接着关闭剩下的source。
* 添加节点到已用节点，设置易用etag，返回结果，如果取不到source，则返回空。
  
### get_working_response

{% codeblock lang:python %}
    def get_working_response(self, req):
        source, node = self._get_source_and_node()
        res = None
        if source:
            res = Response(request=req)
            if req.method == 'GET' and \
                    source.status in (HTTP_OK, HTTP_PARTIAL_CONTENT):
                res.app_iter = self._make_app_iter(req, node, source)
                # See NOTE: swift_conn at top of file about this.
                res.swift_conn = source.swift_conn
            res.status = source.status
            update_headers(res, source.getheaders())
            if not res.environ:
                res.environ = {}
            res.environ['swift_x_timestamp'] = \
                source.getheader('x-timestamp')
            res.accept_ranges = 'bytes'
            res.content_length = source.getheader('Content-Length')
            if source.getheader('Content-Type'):
                res.charset = None
                res.content_type = source.getheader('Content-Type')
        return res
{% endcodeblock %}
* 先获取source和node，如果有的话，则根据req参数封装response，如果请求是'GET'并且source的状态是200或206,则设置response的app_iter和conn。
* 将source的状态码和header设置进response，再分别根据source的内容设置返回的response的值。
  
## Controller类
  
### init方法

{% codeblock lang:python %}
    """Base WSGI controller class for the proxy"""
    server_type = 'Base'

    # Ensure these are all lowercase
    pass_through_headers = []

    def __init__(self, app):
        """
        Creates a controller attached to an application instance

        :param app: the application instance
        """
        self.account_name = None
        self.app = app
        self.trans_id = '-'
        self._allowed_methods = None
{% endcodeblock %}  
* 设置类型为base，初始化方法，创建controller时使用。
  
### allowed_methods

{% codeblock lang:python %}
    @property
    def allowed_methods(self):
        if self._allowed_methods is None:
            self._allowed_methods = set()
            all_methods = inspect.getmembers(self, predicate=inspect.ismethod)
            for name, m in all_methods:
                if getattr(m, 'publicly_accessible', False):
                    self._allowed_methods.add(name)
        return self._allowed_methods
{% endcodeblock %}  
* 类属性变量allowed_methods的初始化方法。
  
### transfer_headers

{% codeblock lang:python %}
    def transfer_headers(self, src_headers, dst_headers):
        """
        Transfer legal headers from an original client request to dictionary
        that will be used as headers by the backend request

        :param src_headers: A dictionary of the original client request headers
        :param dst_headers: A dictionary of the backend request headers
        """
        st = self.server_type.lower()

        x_remove = 'x-remove-%s-meta-' % st
        dst_headers.update((k.lower().replace('-remove', '', 1), '')
                           for k in src_headers
                           if k.lower().startswith(x_remove) or
                           k.lower() in self._x_remove_headers())

        dst_headers.update((k.lower(), v)
                           for k, v in src_headers.iteritems()
                           if k.lower() in self.pass_through_headers or
                           is_sys_or_user_meta(st, k))
{% endcodeblock %}  
* 将一个原始客户端请求的遗留header转换为新的header，给后台进程使用。
  
### transfer_headers

{% codeblock lang:python %}
    def generate_request_headers(self, orig_req=None, additional=None,
                                 transfer=False):
        """
        Create a list of headers to be used in backend requets

        :param orig_req: the original request sent by the client to the proxy
        :param additional: additional headers to send to the backend
        :param transfer: If True, transfer headers from original client request
        :returns: a dictionary of headers
        """
        # Use the additional headers first so they don't overwrite the headers
        # we require.
        headers = HeaderKeyDict(additional) if additional else HeaderKeyDict()
        if transfer:
            self.transfer_headers(orig_req.headers, headers)
        headers.setdefault('x-timestamp', normalize_timestamp(time.time()))
        if orig_req:
            referer = orig_req.as_referer()
        else:
            referer = ''
        headers['x-trans-id'] = self.trans_id
        headers['connection'] = 'close'
        headers['user-agent'] = 'proxy-server %s' % os.getpid()
        headers['referer'] = referer
        return headers
{% endcodeblock %}  
* 生成一组headers为后台进程使用。
  
### account_info

{% codeblock lang:python %}
    def account_info(self, account, req=None):
        """
        Get account information, and also verify that the account exists.

        :param account: name of the account to get the info for
        :param req: caller's HTTP request context object (optional)
        :returns: tuple of (account partition, account nodes, container_count)
                  or (None, None, None) if it does not exist
        """
        partition, nodes = self.app.account_ring.get_nodes(account)
        if req:
            env = getattr(req, 'environ', {})
        else:
            env = {}
        info = get_info(self.app, env, account)
        if not info:
            return None, None, None
        if info.get('container_count') is None:
            container_count = 0
        else:
            container_count = int(info['container_count'])
        return partition, nodes, container_count
{% endcodeblock %}  
* 获取account信息，正常返回分区号，节点和容器数量，获取不到返回3个None。
  
### account_info

{% codeblock lang:python %}
    def container_info(self, account, container, req=None):
        """
        Get container information and thusly verify container existence.
        This will also verify account existence.

        :param account: account name for the container
        :param container: container name to look up
        :param req: caller's HTTP request context object (optional)
        :returns: dict containing at least container partition ('partition'),
                  container nodes ('containers'), container read
                  acl ('read_acl'), container write acl ('write_acl'),
                  and container sync key ('sync_key').
                  Values are set to None if the container does not exist.
        """
        part, nodes = self.app.container_ring.get_nodes(account, container)
        if req:
            env = getattr(req, 'environ', {})
        else:
            env = {}
        info = get_info(self.app, env, account, container)
        if not info:
            info = headers_to_container_info({}, 0)
            info['partition'] = None
            info['nodes'] = None
        else:
            info['partition'] = part
            info['nodes'] = nodes
        return info
{% endcodeblock %}  
* 获取container信息，会顺便校验container是否存在，也会校验account是否存在。
  
### make_request(私有方法)

{% codeblock lang:python %}
    def _make_request(self, nodes, part, method, path, headers, query,
                      logger_thread_locals):
        """
        Iterates over the given node iterator, sending an HTTP request to one
        node at a time.  The first non-informational, non-server-error
        response is returned.  If no non-informational, non-server-error
        response is received from any of the nodes, returns None.

        :param nodes: an iterator of the backend server and handoff servers
        :param part: the partition number
        :param method: the method to send to the backend
        :param path: the path to send to the backend
                     (full path ends up being /<$device>/<$part>/<$path>)
        :param headers: a list of dicts, where each dict represents one
                        backend request that should be made.
        :param query: query string to send to the backend.
        :param logger_thread_locals: The thread local values to be set on the
                                     self.app.logger to retain transaction
                                     logging information.
        :returns: a swob.Response object, or None if no responses were received
        """
        self.app.logger.thread_locals = logger_thread_locals
        for node in nodes:
            try:
                start_node_timing = time.time()
                with ConnectionTimeout(self.app.conn_timeout):
                    conn = http_connect(node['ip'], node['port'],
                                        node['device'], part, method, path,
                                        headers=headers, query_string=query)
                    conn.node = node
                self.app.set_node_timing(node, time.time() - start_node_timing)
                with Timeout(self.app.node_timeout):
                    resp = conn.getresponse()
                    if not is_informational(resp.status) and \
                            not is_server_error(resp.status):
                        return resp.status, resp.reason, resp.getheaders(), \
                            resp.read()
                    elif resp.status == HTTP_INSUFFICIENT_STORAGE:
                        self.app.error_limit(node,
                                             _('ERROR Insufficient Storage'))
            except (Exception, Timeout):
                self.app.exception_occurred(
                    node, self.server_type,
                    _('Trying to %(method)s %(path)s') %
                    {'method': method, 'path': path})
{% endcodeblock %}  
* 遍历每个节点，根据节点信息发起请求，如果请求不是100+和500+，则返回请求结果。
* 如果请求状态码为507，则加入node到异常node列表。
* 其他异常抛出异常信息。
  
### make_requests

{% codeblock lang:python %}
    def make_requests(self, req, ring, part, method, path, headers,
                      query_string=''):
        """
        Sends an HTTP request to multiple nodes and aggregates the results.
        It attempts the primary nodes concurrently, then iterates over the
        handoff nodes as needed.

        :param req: a request sent by the client
        :param ring: the ring used for finding backend servers
        :param part: the partition number
        :param method: the method to send to the backend
        :param path: the path to send to the backend
                     (full path ends up being  /<$device>/<$part>/<$path>)
        :param headers: a list of dicts, where each dict represents one
                        backend request that should be made.
        :param query_string: optional query string to send to the backend
        :returns: a swob.Response object
        """
        start_nodes = ring.get_part_nodes(part)
        nodes = GreenthreadSafeIterator(self.app.iter_nodes(ring, part))
        pile = GreenAsyncPile(len(start_nodes))
        for head in headers:
            pile.spawn(self._make_request, nodes, part, method, path,
                       head, query_string, self.app.logger.thread_locals)
        response = []
        statuses = []
        for resp in pile:
            if not resp:
                continue
            response.append(resp)
            statuses.append(resp[0])
            if self.have_quorum(statuses, len(start_nodes)):
                break
        # give any pending requests *some* chance to finish
        pile.waitall(self.app.post_quorum_timeout)
        while len(response) < len(start_nodes):
            response.append((HTTP_SERVICE_UNAVAILABLE, '', '', ''))
        statuses, reasons, resp_headers, bodies = zip(*response)
        return self.best_response(req, statuses, reasons, bodies,
                                  '%s %s' % (self.server_type, req.method),
                                  headers=resp_headers)
{% endcodeblock %}  
* 先通过partition获取node节点，再根据节点个数创建线程发起每个节点请求。
* 获取每个线程的返回结果，将状态码和响应结果记录保存到列表中，如果状态码列表个数超过节点的一半，则跳出循环。
* 将剩下的response设置为503，最后通过best_response方法获取response。
  
### have_quorum

{% codeblock lang:python %}
    def have_quorum(self, statuses, node_count):
        """
        Given a list of statuses from several requests, determine if
        a quorum response can already be decided.

        :param statuses: list of statuses returned
        :param node_count: number of nodes being queried (basically ring count)
        :returns: True or False, depending on if quorum is established
        """
        quorum = quorum_size(node_count)
        if len(statuses) >= quorum:
            for hundred in (HTTP_OK, HTTP_MULTIPLE_CHOICES, HTTP_BAD_REQUEST):
                if sum(1 for s in statuses
                       if hundred <= s < hundred + 100) >= quorum:
                    return True
        return False
{% endcodeblock %}  
* 通过节点个数和一组状态码判断响应是否已经满足限额。
  
### best_response

{% codeblock lang:python %}
    def best_response(self, req, statuses, reasons, bodies, server_type,
                      etag=None, headers=None):
        """
        Given a list of responses from several servers, choose the best to
        return to the API.

        :param req: swob.Request object
        :param statuses: list of statuses returned
        :param reasons: list of reasons for each status
        :param bodies: bodies of each response
        :param server_type: type of server the responses came from
        :param etag: etag
        :param headers: headers of each response
        :returns: swob.Response object with the correct status, body, etc. set
        """
        resp = Response(request=req)
        if len(statuses):
            for hundred in (HTTP_OK, HTTP_MULTIPLE_CHOICES, HTTP_BAD_REQUEST):
                hstatuses = \
                    [s for s in statuses if hundred <= s < hundred + 100]
                if len(hstatuses) >= quorum_size(len(statuses)):
                    status = max(hstatuses)
                    status_index = statuses.index(status)
                    resp.status = '%s %s' % (status, reasons[status_index])
                    resp.body = bodies[status_index]
                    if headers:
                        update_headers(resp, headers[status_index])
                    if etag:
                        resp.headers['etag'] = etag.strip('"')
                    return resp
        self.app.logger.error(_('%(type)s returning 503 for %(statuses)s'),
                              {'type': server_type, 'statuses': statuses})
        resp.status = '503 Internal Server Error'
        return resp
{% endcodeblock %}  
* 给定一组response，返回最佳的response。
* 比如副本数是3,response列表是[201,201,503],则返回201。
  
### autocreate_account

{% codeblock lang:python %}
    def autocreate_account(self, env, account):
        """
        Autocreate an account

        :param env: the environment of the request leading to this autocreate
        :param account: the unquoted account name
        """
        partition, nodes = self.app.account_ring.get_nodes(account)
        path = '/%s' % account
        headers = {'X-Timestamp': normalize_timestamp(time.time()),
                   'X-Trans-Id': self.trans_id,
                   'Connection': 'close'}
        resp = self.make_requests(Request.blank('/v1' + path),
                                  self.app.account_ring, partition, 'PUT',
                                  path, [headers] * len(nodes))
        if is_success(resp.status_int):
            self.app.logger.info('autocreate account %r' % path)
            clear_info_cache(self.app, env, account)
        else:
            self.app.logger.warning('Could not autocreate account %r' % path)
{% endcodeblock %}  
* 发起一个PUT请求自动创建account，创建失败记录警告信息。
  
### GETorHEAD_base

{% codeblock lang:python %}
    def GETorHEAD_base(self, req, server_type, ring, partition, path):
        """
        Base handler for HTTP GET or HEAD requests.

        :param req: swob.Request object
        :param server_type: server type used in logging
        :param ring: the ring to obtain nodes from
        :param partition: partition
        :param path: path for the request
        :returns: swob.Response object
        """
        backend_headers = self.generate_request_headers(
            req, additional=req.headers)

        handler = GetOrHeadHandler(self.app, req, self.server_type, ring,
                                   partition, path, backend_headers)
        res = handler.get_working_response(req)

        if not res:
            res = self.best_response(
                req, handler.statuses, handler.reasons, handler.bodies,
                '%s %s' % (server_type, req.method),
                headers=handler.source_headers)
        try:
            (vrs, account, container) = req.split_path(2, 3)
            _set_info_cache(self.app, req.environ, account, container, res)
        except ValueError:
            pass
        try:
            (vrs, account, container, obj) = req.split_path(4, 4, True)
            _set_object_info_cache(self.app, req.environ, account,
                                   container, obj, res)
        except ValueError:
            pass
        return res
{% endcodeblock %}  
* 基类controller的get或head请求处理方法，首先构造header和handler发起一个http请求。
* 如果请求没有响应，则调用best_response方法取到response。
* 如果请求有响应，则根据request分割出account、container和object信息，设置到缓存中，最后返回response。
  
### is_origin_allowed

{% codeblock lang:python %}
    def is_origin_allowed(self, cors_info, origin):
        """
        Is the given Origin allowed to make requests to this resource

        :param cors_info: the resource's CORS related metadata headers
        :param origin: the origin making the request
        :return: True or False
        """
        allowed_origins = set()
        if cors_info.get('allow_origin'):
            allowed_origins.update(
                [a.strip()
                 for a in cors_info['allow_origin'].split(' ')
                 if a.strip()])
        if self.app.cors_allow_origin:
            allowed_origins.update(self.app.cors_allow_origin)
        return origin in allowed_origins or '*' in allowed_origins
{% endcodeblock %}  
* 判断该请求方法是否允许发起请求，先从header中获取'allow_origin'的值，如果有的花，更新允许访问列表。
* 如果原请求方法在允许访问列表中，或者允许访问列表中有'*'，则返回True。
  
### OPTIONS

{% codeblock lang:python %}
    @public
    def OPTIONS(self, req):
        """
        Base handler for OPTIONS requests

        :param req: swob.Request object
        :returns: swob.Response object
        """
        # Prepare the default response
        headers = {'Allow': ', '.join(self.allowed_methods)}
        resp = Response(status=200, request=req, headers=headers)

        # If this isn't a CORS pre-flight request then return now
        req_origin_value = req.headers.get('Origin', None)
        if not req_origin_value:
            return resp
{% endcodeblock %}  
* options请求的基本handler，准备一个默认的response，如果不是一个CORS请求，则返回默认的response。
  
{% codeblock lang:python %}
        # This is a CORS preflight request so check it's allowed
        try:
            container_info = \
                self.container_info(self.account_name,
                                    self.container_name, req)
        except AttributeError:
            # This should only happen for requests to the Account. A future
            # change could allow CORS requests to the Account level as well.
            return resp

        cors = container_info.get('cors', {})

        # If the CORS origin isn't allowed return a 401
        if not self.is_origin_allowed(cors, req_origin_value) or (
                req.headers.get('Access-Control-Request-Method') not in
                self.allowed_methods):
            resp.status = HTTP_UNAUTHORIZED
            return resp
{% endcodeblock %}  
* 如果对account进行操作的CORS请求，则返回默认reponse，否则获取container信息。
* 如果CORS请求不允许，则返回401。
  
{% codeblock lang:python %}
        # Allow all headers requested in the request. The CORS
        # specification does leave the door open for this, as mentioned in
        # http://www.w3.org/TR/cors/#resource-preflight-requests
        # Note: Since the list of headers can be unbounded
        # simply returning headers can be enough.
        allow_headers = set()
        if req.headers.get('Access-Control-Request-Headers'):
            allow_headers.update(
                list_from_csv(req.headers['Access-Control-Request-Headers']))

        # Populate the response with the CORS preflight headers
        if cors.get('allow_origin', '').strip() == '*':
            headers['access-control-allow-origin'] = '*'
        else:
            headers['access-control-allow-origin'] = req_origin_value
        if cors.get('max_age') is not None:
            headers['access-control-max-age'] = cors.get('max_age')
        headers['access-control-allow-methods'] = \
            ', '.join(self.allowed_methods)
        if allow_headers:
            headers['access-control-allow-headers'] = ', '.join(allow_headers)
        resp.headers = headers

        return resp
{% endcodeblock %}  
* 在response的header中增加相关header，分别有'access-control-allow-origin','access-control-max-age','access-control-allow-methods'和'access-control-allow-headers'。
  
[url1]: http://zhaozhiming.github.io/2014/04/19/swift-code-explain-total/

