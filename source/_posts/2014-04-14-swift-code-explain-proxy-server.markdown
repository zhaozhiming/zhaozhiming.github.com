---
layout: post
title: "swift源码详解（一）——proxy/server.py"
date: 2014-04-14 20:52
published: false
description: swift源码详解
keywords: swift
comments: true
categories: code
tags: swift
---

### int方法

{% codeblock lang:python %}
    def __init__(self, conf, memcache=None, logger=None, account_ring=None,
                 container_ring=None, object_ring=None):
        if conf is None:
            conf = {}
        if logger is None:
            self.logger = get_logger(conf, log_route='proxy-server')
        else:
            self.logger = logger
{% endcodeblock %}
* 1~2: proxy server的初始化方法。
* 3~4: 配置参数字典初始化。
* 5~7: logger对象初始化。
  
{% codeblock lang:python %}
        swift_dir = conf.get('swift_dir', '/etc/swift')
        self.node_timeout = int(conf.get('node_timeout', 10))
        self.recoverable_node_timeout = int(
            conf.get('recoverable_node_timeout', self.node_timeout))
        self.conn_timeout = float(conf.get('conn_timeout', 0.5))
        self.client_timeout = int(conf.get('client_timeout', 60))
        self.put_queue_depth = int(conf.get('put_queue_depth', 10))
        self.object_chunk_size = int(conf.get('object_chunk_size', 65536))
        self.client_chunk_size = int(conf.get('client_chunk_size', 65536))
        self.trans_id_suffix = conf.get('trans_id_suffix', '')
        self.post_quorum_timeout = float(conf.get('post_quorum_timeout', 0.5))
        self.error_suppression_interval = \
            int(conf.get('error_suppression_interval', 60))
        self.error_suppression_limit = \
            int(conf.get('error_suppression_limit', 10))
{% endcodeblock %}
* swift_dir: swift的文件夹路径，一般存放ring文件和配置文件。
* node_timeout: proxy server等待a/c/o server的响应超时时间，默认10秒。
* recoverable_node_timeout: proxy server等待一个GET或HEAD object请求去读取一个数据块的初始化响应超时时间，默认值是node_time。
* conn_timeout: 连接超时时间，默认0.5秒。
* client_timeout: 客户端超时时间，默认60秒。
* put_queue_depth: proxy的put队列深度，默认10。
* object_chunk_size: object每次上传的字节大小，默认65536。
* client_chunk_size: 客户端每次读取的字节大小，默认65536。
* trans_id_suffix: trans_id的后缀，默认是空。
* post_quorum_timeout: How long to wait for requests to finish after a quorum has been established。
* error_suppression_interval: How long without an error before a node's error count is reset. This will also be how long before a node is reenabled after suppression is triggered。
* error_suppression_limit: 在一个node被临时忽略之前有多少个errors可以堆积。

{% codeblock lang:python %}
        self.recheck_container_existence = \
            int(conf.get('recheck_container_existence', 60))
        self.recheck_account_existence = \
            int(conf.get('recheck_account_existence', 60))
        self.allow_account_management = \
            config_true_value(conf.get('allow_account_management', 'no'))
        self.object_post_as_copy = \
            config_true_value(conf.get('object_post_as_copy', 'true'))
        self.object_ring = object_ring or Ring(swift_dir, ring_name='object')
        self.container_ring = container_ring or Ring(swift_dir,
                                                     ring_name='container')
        self.account_ring = account_ring or Ring(swift_dir,
                                                 ring_name='account')
{% endcodeblock %}

{% codeblock lang:python %}
        self.memcache = memcache
        mimetypes.init(mimetypes.knownfiles +
                       [os.path.join(swift_dir, 'mime.types')])
        self.account_autocreate = \
            config_true_value(conf.get('account_autocreate', 'no'))
        self.expiring_objects_account = \
            (conf.get('auto_create_account_prefix') or '.') + \
            (conf.get('expiring_objects_account_name') or 'expiring_objects')
        self.expiring_objects_container_divisor = \
            int(conf.get('expiring_objects_container_divisor') or 86400)
        self.max_containers_per_account = \
            int(conf.get('max_containers_per_account') or 0)
        self.max_containers_whitelist = [
            a.strip()
            for a in conf.get('max_containers_whitelist', '').split(',')
            if a.strip()]
{% endcodeblock %}
  
{% codeblock lang:python %}
        self.deny_host_headers = [
            host.strip() for host in
            conf.get('deny_host_headers', '').split(',') if host.strip()]
        self.rate_limit_after_segment = \
            int(conf.get('rate_limit_after_segment', 10))
        self.rate_limit_segments_per_sec = \
            int(conf.get('rate_limit_segments_per_sec', 1))
        self.log_handoffs = config_true_value(conf.get('log_handoffs', 'true'))
        self.cors_allow_origin = [
            a.strip()
            for a in conf.get('cors_allow_origin', '').split(',')
            if a.strip()]
        self.strict_cors_mode = config_true_value(
            conf.get('strict_cors_mode', 't'))
        self.node_timings = {}
        self.timing_expiry = int(conf.get('timing_expiry', 300))
        self.sorting_method = conf.get('sorting_method', 'shuffle').lower()
        self.max_large_object_get_time = float(
            conf.get('max_large_object_get_time', '86400'))
{% endcodeblock %}
  
{% codeblock lang:python %}
        value = conf.get('request_node_count', '2 * replicas').lower().split()
        if len(value) == 1:
            value = int(value[0])
            self.request_node_count = lambda replicas: value
        elif len(value) == 3 and value[1] == '*' and value[2] == 'replicas':
            value = int(value[0])
            self.request_node_count = lambda replicas: value * replicas
        else:
            raise ValueError(
                'Invalid request_node_count value: %r' % ''.join(value))
{% endcodeblock %}
  
{% codeblock lang:python %}
        try:
            self._read_affinity = read_affinity = conf.get('read_affinity', '')
            self.read_affinity_sort_key = affinity_key_function(read_affinity)
        except ValueError as err:
            # make the message a little more useful
            raise ValueError("Invalid read_affinity value: %r (%s)" %
                             (read_affinity, err.message))
  
        try:
            write_affinity = conf.get('write_affinity', '')
            self.write_affinity_is_local_fn \
                = affinity_locality_predicate(write_affinity)
        except ValueError as err:
            # make the message a little more useful
            raise ValueError("Invalid write_affinity value: %r (%s)" %
                             (write_affinity, err.message))
{% endcodeblock %}
  
{% codeblock lang:python %}
        value = conf.get('write_affinity_node_count',
                         '2 * replicas').lower().split()
        if len(value) == 1:
            value = int(value[0])
            self.write_affinity_node_count = lambda replicas: value
        elif len(value) == 3 and value[1] == '*' and value[2] == 'replicas':
            value = int(value[0])
            self.write_affinity_node_count = lambda replicas: value * replicas
        else:
            raise ValueError(
                'Invalid write_affinity_node_count value: %r' % ''.join(value))
{% endcodeblock %}
  
{% codeblock lang:python %}
        # swift_owner_headers are stripped by the account and container
        # controllers; we should extend header stripping to object controller
        # when a privileged object header is implemented.
        swift_owner_headers = conf.get(
            'swift_owner_headers',
            'x-container-read, x-container-write, '
            'x-container-sync-key, x-container-sync-to, '
            'x-account-meta-temp-url-key, x-account-meta-temp-url-key-2, '
            'x-account-access-control')
        self.swift_owner_headers = [
            name.strip().title()
            for name in swift_owner_headers.split(',') if name.strip()]
{% endcodeblock %}
  
{% codeblock lang:python %}
        # Initialization was successful, so now apply the client chunk size
        # parameter as the default read / write buffer size for the network
        # sockets.
        #
        # NOTE WELL: This is a class setting, so until we get set this on a
        # per-connection basis, this affects reading and writing on ALL
        # sockets, those between the proxy servers and external clients, and
        # those between the proxy servers and the other internal servers.
        #
        # ** Because it affects the client as well, currently, we use the
        # client chunk size as the govenor and not the object chunk size.
        socket._fileobject.default_bufsize = self.client_chunk_size
        self.expose_info = config_true_value(
            conf.get('expose_info', 'yes'))
        self.disallowed_sections = list_from_csv(
            conf.get('disallowed_sections'))
        self.admin_key = conf.get('admin_key', None)
        register_swift_info(
            version=swift_version,
            strict_cors_mode=self.strict_cors_mode,
            **constraints.EFFECTIVE_CONSTRAINTS)
{% endcodeblock %}
  
### call方法

{% codeblock lang:python %}
def __call__(self, env, start_response):
        """
        WSGI entry point.
        Wraps env in swob.Request object and passes it down.

        :param env: WSGI environment dictionary
        :param start_response: WSGI callable
        """
        try:
            if self.memcache is None:
                self.memcache = cache_from_env(env)
            req = self.update_request(Request(env))
            return self.handle_request(req)(env, start_response)
        except UnicodeError:
            err = HTTPPreconditionFailed(
                request=req, body='Invalid UTF8 or contains NULL')
            return err(env, start_response)
        except (Exception, Timeout):
            start_response('500 Server Error',
                           [('Content-Type', 'text/plain')])
            return ['Internal server error.\n']
{% endcodeblock %}  
* 9~10: 检查memcache缓存是否为空，如果为空的话就从上下文中获取，由于proxy-server在pipeline中是最后面，如果pipeline前面配置了memcache中间件的话，就可以从上下文中取到。  
* 12: 调用update_request方法，后面会介绍。  
* 13: 调用handle_request方法，后面会介绍，最后返回response。  
* 14~17: 捕获UnicodeError并返回412。  
* 18~21: 捕获Timeout和其他异常并返回500。  
  
### update_request
{% codeblock lang:python %}
    def update_request(self, req):
        if 'x-storage-token' in req.headers and \
                'x-auth-token' not in req.headers:
            req.headers['x-auth-token'] = req.headers['x-storage-token']
        return req
{% endcodeblock %}  
该方法是将requeset中的x-auth-token的header替换为x-storage-token的header。  
  
### handle_request
  
{% codeblock lang:python %}
def handle_request(self, req):
        """
        Entry point for proxy server.
        Should return a WSGI-style callable (such as swob.Response).

        :param req: swob.Request object
        """
        try:
            self.logger.set_statsd_prefix('proxy-server')
            if req.content_length and req.content_length < 0:
                self.logger.increment('errors')
                return HTTPBadRequest(request=req,
                                      body='Invalid Content-Length')

            try:
                if not check_utf8(req.path_info):
                    self.logger.increment('errors')
                    return HTTPPreconditionFailed(
                        request=req, body='Invalid UTF8 or contains NULL')
            except UnicodeError:
                self.logger.increment('errors')
                return HTTPPreconditionFailed(
                    request=req, body='Invalid UTF8 or contains NULL')
{% endcodeblock %}  
* 8: 在log中设置'proxy-server'前缀。  
* 10~13: 检查request中content length如果有且长度为0，则返回500。  
* 15~23  : 检查url格式是否utf-8，如果不是则返回412。  
  
{% codeblock lang:python %}
            try:
                controller, path_parts = self.get_controller(req.path)
                p = req.path_info
                if isinstance(p, unicode):
                    p = p.encode('utf-8')
            except ValueError:
                self.logger.increment('errors')
                return HTTPNotFound(request=req)
            if not controller:
                self.logger.increment('errors')
                return HTTPPreconditionFailed(request=req, body='Bad URL')
            if self.deny_host_headers and \
                    req.host.split(':')[0] in self.deny_host_headers:
                return HTTPForbidden(request=req, body='Invalid host header')
{% endcodeblock %}  
* 2～5: 调用get_controller方法(后面会介绍)，通过url获取对应的controller类和url中通过'/'符号分割的各个部分。  
* 6~8: 捕获ValueError并返回404。  
* 9~11: 如果controller类为空则返回404。  
* 12~14: 如果proxy中有定义deny_host_headers(禁止访问的ip），并且request的ip与禁止访问的ip一致，则返回403。  
  
{% codeblock lang:python %}
            self.logger.set_statsd_prefix('proxy-server.' +
                                          controller.server_type.lower())
            controller = controller(self, **path_parts)
            if 'swift.trans_id' not in req.environ:
                # if this wasn't set by an earlier middleware, set it now
                trans_id = generate_trans_id(self.trans_id_suffix)
                req.environ['swift.trans_id'] = trans_id
                self.logger.txn_id = trans_id
            req.headers['x-trans-id'] = req.environ['swift.trans_id']
            controller.trans_id = req.environ['swift.trans_id']
            self.logger.client_ip = get_remote_client(req)
{% endcodeblock %}  
* 1～2: 日志加上controller名字前缀。
* 3: 通过controller类实例化controller对象。
* 4~10: 如果swift.trans_id没有在request的上下文中，则重新生成trans_id，并设置在上下文、日志、header和controller中。  
* 11: 调用get_remote_client方法(后面介绍)，先判断header中是否有'x-cluster-client-ip'，如果没有再去获取header中的'x-forwarded-for'，还是没有的话就从request中的remote_addr取值，得到client_ip。  
  
{% codeblock lang:python %}
            try:
                handler = getattr(controller, req.method)
                getattr(handler, 'publicly_accessible')
            except AttributeError:
                allowed_methods = getattr(controller, 'allowed_methods', set())
                return HTTPMethodNotAllowed(
                    request=req, headers={'Allow': ', '.join(allowed_methods)})
{% endcodeblock %}  
* 2~3: 通过request的method，在controller得到一个名字相同，并且有'public'标签的方法对象handler。
* 4~7: 如果获取不到对应的public方法，则打印出controller中所有public方法并返回405。  
  
{% codeblock lang:python %}
            if 'swift.authorize' in req.environ:
                # We call authorize before the handler, always. If authorized,
                # we remove the swift.authorize hook so isn't ever called
                # again. If not authorized, we return the denial unless the
                # controller's method indicates it'd like to gather more
                # information and try again later.
                resp = req.environ['swift.authorize'](req)
                if not resp:
                    # No resp means authorized, no delayed recheck required.
                    del req.environ['swift.authorize']
                else:
                    # Response indicates denial, but we might delay the denial
                    # and recheck later. If not delayed, return the error now.
                    if not getattr(handler, 'delay_denial', None):
                        return resp
{% endcodeblock %}  
如果request的上下文中有swift.authorize，则调用这个方法进行认证。  
如果没有返回结果证明之前已经认证通过了，后面的请求不需要再认证，将'swift.authorize'从上下文去掉。  
如果有Response返回则表示认证不通过，会先检查是否有延迟禁止的配置，如果没有返回认证不通过的response，如果有则会等后面再重新确认。  
{% codeblock lang:python %}
            # Save off original request method (GET, POST, etc.) in case it
            # gets mutated during handling.  This way logging can display the
            # method the client actually sent.
            req.environ['swift.orig_req_method'] = req.method
            return handler(req)
        except HTTPException as error_response:
            return error_response
        except (Exception, Timeout):
            self.logger.exception(_('ERROR Unhandled exception in request'))
            return HTTPServerError(request=req)
{% endcodeblock %}  
* 4~5: 在日志中记录原始的request方法，防止请求在传播过程中发生突变http请求方法发生改变。
* 6~10: 捕获异常，记录日志。



