---
layout: post
title: "swift源码详解（一）——开始"
date: 2014-04-19 17:19
description: swift源码详解
keywords: swift
comments: true
categories: code
tags: swift
---

从今天开始准备仔细再看一下swift的源码，然后把理解的内容记录下来。下面是swift源码的代码结构，准备每天更新1～2个文件的代码理解，更新好的在文件名上会在下面的代码结构上有链接出现。  
  
<!--more-->  
swift的源码因为不断在更新，笔记记录的代码就以2014-4-18的为准，我已经fork了一份swift源码到我github上，地址是：[https://github.com/zhaozhiming/swift][url1]，代码结构如下：  
  
* ### swift 
	* ### account
		* auditor.py
		* backend.py
		* reaper.py
		* replicator.py
		* server.py
		* utils.py
	* ### cli
		* info.py
		* recon.py
		* ringbuilder.py
		* ringbuilder.py
	* ### common
		* ### middleware
			* account_quotas.py
			* acl.py
			* bulk.py
			* catch_errors.py
			* cname_lookup.py
			* container_quotas.py
			* container_sync.py
			* crossdomain.py
			* dlo.py
			* domain_remap.py
			* formpost.py
			* gatekeeper.py
			* healthcheck.py
			* list_endpoints.py
			* memcache.py
			* name_check.py
			* proxy_logging.py
			* ratelimit.py
			* recon.py
			* slo.py
			* staticweb.py
			* tempauth.py
			* tempurl.py
		* ### ring
			* builder.py
			* ring.py
			* utils.py
		* bufferedhttp.py
		* constraints.py
		* container_sync_realms.py
		* daemon.py
		* db.py
		* db_replicator.py
		* direct_client.py
		* exceptions.py
		* http.py
		* internal_client.py
		* manager.py
		* memcached.py
		* request_helpers.py
		* swob.py
		* swob.py
		* utils.py
		* wsgi.py
	* ### container
		* auditor.py	
		* backend.py	
		* replicator.py	
		* server.py	
		* sync.py	
		* updater.py
	* ### obj
		* auditor.py	
		* diskfile.py
		* expirer.py
		* mem_diskfile.py
		* mem_server.py
		* replicator.py
		* server.py
		* ssync_receiver.py
		* ssync_sender.py
		* updater.py
	* ### proxy
		* controllers
			* account.py
			* [base.py][url3]
			* container.py
			* obj.py
		* [server.py][url2]

[url1]: https://github.com/zhaozhiming/swift
[url2]: http://zhaozhiming.github.io/blog/2014/04/20/swift-code-explain-proxy-server/
[url3]: http://zhaozhiming.github.io/blog/2014/05/04/swift-code-explain-3-proxy-controllers-base/