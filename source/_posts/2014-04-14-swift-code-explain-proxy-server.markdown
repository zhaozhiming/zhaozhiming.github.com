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



