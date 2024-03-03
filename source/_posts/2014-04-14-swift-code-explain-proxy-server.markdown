---
layout: post
title: "swift源码详解（二）——proxy/server.py"
date: 2014-04-20 20:52
description: swift源码详解
keywords: swift
comments: true
categories: code
tags: swift
---

## [回swift代码结构目录][url3]

### int方法

<!--more-->  
{% codeblock lang:python %}
    def __init__(self, conf, memcache=None, logger=None, account_ring=None,
                 container_ring=None, object_ring=None):
        if conf is None:
            conf = {}
        if logger is None:
            self.logger = get_logger(conf, log_route='proxy-server')
        else:
            self.logger = logger
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
proxy server的初始化函数，具体配置的说明可以参考[这里][url2]。
  
### check_config

{% codeblock lang:python %}
    def check_config(self):
        """
        Check the configuration for possible errors
        """
        if self._read_affinity and self.sorting_method != 'affinity':
            self.logger.warn("sorting_method is set to '%s', not 'affinity'; "
                             "read_affinity setting will have no effect." %
                             self.sorting_method)
{% endcodeblock %}  
proxy server初始化后被调用的方法，检查proxy的read_affinity配置和排序方法设置不一致时，记录警告日志。  
  
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
  
### get_controller

{% codeblock lang:python %}
    def get_controller(self, path):
        """
        Get the controller to handle a request.

        :param path: path from request
        :returns: tuple of (controller class, path dictionary)

        :raises: ValueError (thrown by split_path) if given invalid path
        """
        if path == '/info':
            d = dict(version=None,
                     expose_info=self.expose_info,
                     disallowed_sections=self.disallowed_sections,
                     admin_key=self.admin_key)
            return InfoController, d

        version, account, container, obj = split_path(path, 1, 4, True)
        d = dict(version=version,
                 account_name=account,
                 container_name=container,
                 object_name=obj)
        if obj and container and account:
            return ObjectController, d
        elif container and account:
            return ContainerController, d
        elif account and not container and not obj:
            return AccountController, d
        return None, d
{% endcodeblock %}  
* 10～15: 如果url是'info'，则返回InController和controller字典参数，expose_info表示是否暴露信息，disallowed_sections表示不允许暴露的字段列表，比如container_qutoas, tempurl等。
* 17～28: 根据url判断是account、container还是object，返回对应的controller和字典参数。  
  
### sort_nodes

{% codeblock lang:python %}
    def sort_nodes(self, nodes):
        '''
        Sorts nodes in-place (and returns the sorted list) according to
        the configured strategy. The default "sorting" is to randomly
        shuffle the nodes. If the "timing" strategy is chosen, the nodes
        are sorted according to the stored timing data.
        '''
        # In the case of timing sorting, shuffling ensures that close timings
        # (ie within the rounding resolution) won't prefer one over another.
        # Python's sort is stable (http://wiki.python.org/moin/HowTo/Sorting/)
        shuffle(nodes)
        if self.sorting_method == 'timing':
            now = time()

            def key_func(node):
                timing, expires = self.node_timings.get(node['ip'], (-1.0, 0))
                return timing if expires > now else -1.0
            nodes.sort(key=key_func)
        elif self.sorting_method == 'affinity':
            nodes.sort(key=self.read_affinity_sort_key)
        return nodes
{% endcodeblock %}  
节点的排序方法，将节点根据配置的排序策略进行排序。  
* 11: 将节点顺序打乱，确保节点不会按照时间排好序。
* 12～18: 如果配置的排序策略是按时间排序，则定义一个（节点）按时间排序的方法让节点按照这个方法排序，如果节点已过期则timing为-0.1，即会被排到最后。
* 19～20: 如果配置的排序策略是按亲和力排序，则节点按照亲和力方法排序。  
  
### set_node_timing

{% codeblock lang:python %}
    def set_node_timing(self, node, timing):
        if self.sorting_method != 'timing':
            return
        now = time()
        timing = round(timing, 3)  # sort timings to the millisecond
        self.node_timings[node['ip']] = (timing, now + self.timing_expiry)
{% endcodeblock %}  
* 2～3: 如果配置的排序策略不是'timing'，则直接返回不做设置。
* 4～6: 设置单个节点的排序时间过期时间。
  
### error_limited

{% codeblock lang:python %}
    def error_limited(self, node):
        """
        Check if the node is currently error limited.

        :param node: dictionary of node to check
        :returns: True if error limited, False otherwise
        """
        now = time()
        if 'errors' not in node:
            return False
        if 'last_error' in node and node['last_error'] < \
                now - self.error_suppression_interval:
            del node['last_error']
            if 'errors' in node:
                del node['errors']
            return False
        limited = node['errors'] > self.error_suppression_limit
        if limited:
            self.logger.debug(
                _('Node error limited %(ip)s:%(port)s (%(device)s)'), node)
        return limited
{% endcodeblock %}  
* 8～10: 如果节点里面没有'errors'选项，则返回false。
* 11～10: 如果节点里面的'last_error'选项不正确，则删除该选项和errors选项，并返回false。
* 12～15: 判断节点的错误个数是否超过配置的错误限制，如果超过则记录日志，并返回是否超限制的结果。
  
### error_limit

{% codeblock lang:python %}
    def error_limit(self, node, msg):
        """
        Mark a node as error limited. This immediately pretends the
        node received enough errors to trigger error suppression. Use
        this for errors like Insufficient Storage. For other errors
        use :func:`error_occurred`.

        :param node: dictionary of node to error limit
        :param msg: error message
        """
        node['errors'] = self.error_suppression_limit + 1
        node['last_error'] = time()
        self.logger.error(_('%(msg)s %(ip)s:%(port)s/%(device)s'),
                          {'msg': msg, 'ip': node['ip'],
                          'port': node['port'], 'device': node['device']})
{% endcodeblock %}  
* 11～14: 记录一个节点的错误信息:错误个数，最后错误的时间，并记录日志。
  
### error_occurred

{% codeblock lang:python %}
    def error_occurred(self, node, msg):
        """
        Handle logging, and handling of errors.

        :param node: dictionary of node to handle errors for
        :param msg: error message
        """
        node['errors'] = node.get('errors', 0) + 1
        node['last_error'] = time()
        self.logger.error(_('%(msg)s %(ip)s:%(port)s/%(device)s'),
                          {'msg': msg, 'ip': node['ip'],
                          'port': node['port'], 'device': node['device']})
{% endcodeblock %}  
* 8～12: 与前面的方法类似，唯一区别是记录节点错误个数是取当前的错误个数，然后+1。
  
### iter_nodes

{% codeblock lang:python %}
    def iter_nodes(self, ring, partition, node_iter=None):
        """
        Yields nodes for a ring partition, skipping over error
        limited nodes and stopping at the configurable number of
        nodes. If a node yielded subsequently gets error limited, an
        extra node will be yielded to take its place.

        Note that if you're going to iterate over this concurrently from
        multiple greenthreads, you'll want to use a
        swift.common.utils.GreenthreadSafeIterator to serialize access.
        Otherwise, you may get ValueErrors from concurrent access. (You also
        may not, depending on how logging is configured, the vagaries of
        socket IO and eventlet, and the phase of the moon.)

        :param ring: ring to get yield nodes from
        :param partition: ring partition to yield nodes for
        :param node_iter: optional iterable of nodes to try. Useful if you
            want to filter or reorder the nodes.
        """
        part_nodes = ring.get_part_nodes(partition)
        if node_iter is None:
            node_iter = itertools.chain(part_nodes,
                                        ring.get_more_nodes(partition))
        num_primary_nodes = len(part_nodes)

        # Use of list() here forcibly yanks the first N nodes (the primary
        # nodes) from node_iter, so the rest of its values are handoffs.
        primary_nodes = self.sort_nodes(
            list(itertools.islice(node_iter, num_primary_nodes)))
        handoff_nodes = node_iter
        nodes_left = self.request_node_count(len(primary_nodes))
{% endcodeblock %}  
* 20～24: 根据partition获取相应的节点，如果node_iter为空，则将之前取到的节点和get_more_nodes节点连接起来为node_iter赋值，并取得节点个数。
* 28～31: 将node_iter的节点重新排序，并取前面部分作为主要nodes，handoff_nodes为node_iter剩下的nodes， nodes_left为剩下的节点个数。
  
{% codeblock lang:python %}
        for node in primary_nodes:
            if not self.error_limited(node):
                yield node
                if not self.error_limited(node):
                    nodes_left -= 1
                    if nodes_left <= 0:
                        return
        handoffs = 0
        for node in handoff_nodes:
            if not self.error_limited(node):
                handoffs += 1
                if self.log_handoffs:
                    self.logger.increment('handoff_count')
                    self.logger.warning(
                        'Handoff requested (%d)' % handoffs)
                    if handoffs == len(primary_nodes):
                        self.logger.increment('handoff_all_count')
                yield node
                if not self.error_limited(node):
                    nodes_left -= 1
                    if nodes_left <= 0:
                        return
{% endcodeblock %}  
* 1～7: 遍历每个主节点，如果节点没有错误则返回该节点，剩余节点数-1,如果剩余节点数<=0,则直接返回。
* 8～22: 如果主节点中都有错误，则从剩余节点中查找满足条件的节点，查找方法和主节点查找方法雷同，只是多了一些日志的记录。
  
### exception_occurred

{% codeblock lang:python %}
    def exception_occurred(self, node, typ, additional_info):
        """
        Handle logging of generic exceptions.

        :param node: dictionary of node to log the error for
        :param typ: server type
        :param additional_info: additional information to log
        """
        self.logger.exception(
            _('ERROR with %(type)s server %(ip)s:%(port)s/%(device)s re: '
              '%(info)s'),
            {'type': typ, 'ip': node['ip'], 'port': node['port'],
             'device': node['device'], 'info': additional_info})
{% endcodeblock %}  
* 9～13: 当异常发生的时候，记录异常日志。

### modify_wsgi_pipeline

{% codeblock lang:python %}
    def modify_wsgi_pipeline(self, pipe):
        """
        Called during WSGI pipeline creation. Modifies the WSGI pipeline
        context to ensure that mandatory middleware is present in the pipeline.

        :param pipe: A PipelineWrapper object
        """
        pipeline_was_modified = False
        for filter_spec in reversed(required_filters):
            filter_name = filter_spec['name']
            if filter_name not in pipe:
                afters = filter_spec.get('after_fn', lambda _junk: [])(pipe)
                insert_at = 0
                for after in afters:
                    try:
                        insert_at = max(insert_at, pipe.index(after) + 1)
                    except ValueError:  # not in pipeline; ignore it
                        pass
                self.logger.info(
                    'Adding required filter %s to pipeline at position %d' %
                    (filter_name, insert_at))
                ctx = pipe.create_filter(filter_name)
                pipe.insert_filter(ctx, index=insert_at)
                pipeline_was_modified = True

        if pipeline_was_modified:
            self.logger.info("Pipeline was modified. New pipeline is \"%s\".",
                             pipe)
        else:
            self.logger.debug("Pipeline is \"%s\"", pipe)
{% endcodeblock %}  
* 8～24: 遍历定义好的中间件required_filters，如果该中间件没有在pipeline中，则将该中间件插入到pipeline，插入位置根据中间件的atfer_fn方法得到。
* 26～31: 记录人日志信息。
  
### required_filters
  
{% codeblock lang:python %}
# List of entry points for mandatory middlewares.
#
# Fields:
#
# "name" (required) is the entry point name from setup.py.
#
# "after_fn" (optional) a function that takes a PipelineWrapper object as its
# single argument and returns a list of middlewares that this middleware
# should come after. Any middlewares in the returned list that are not present
# in the pipeline will be ignored, so you can safely name optional middlewares
# to come after. For example, ["catch_errors", "bulk"] would install this
# middleware after catch_errors and bulk if both were present, but if bulk
# were absent, would just install it after catch_errors.

required_filters = [
    {'name': 'catch_errors'},
    {'name': 'gatekeeper',
     'after_fn': lambda pipe: (['catch_errors']
                               if pipe.startswith("catch_errors")
                               else [])},
    {'name': 'dlo', 'after_fn': lambda _junk: ['catch_errors', 'gatekeeper',
                                               'proxy_logging']}]
{% endcodeblock %}  
* modify_wsgi_pipeline方法用到的required_filters。
  
### app_factory
  
{% codeblock lang:python %}
def app_factory(global_conf, **local_conf):
    """paste.deploy app factory for creating WSGI proxy apps."""
    conf = global_conf.copy()
    conf.update(local_conf)
    app = Application(conf)
    app.check_config()
    return app
{% endcodeblock %}  
* proxy server的工厂方法，初始化server对象并检查配置，然后返回创建好的对象。  
  

[url1]: https://github.com/zhaozhiming/swift
[url2]: http://docs.openstack.org/havana/config-reference/content/proxy-server-conf.html
[url3]: http://zhaozhiming.github.io/2014/04/19/swift-code-explain-total/
