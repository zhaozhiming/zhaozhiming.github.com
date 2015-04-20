---
layout: post
title: "使用Openfire和Smack进行即时通讯消息开发"
date: 2015-04-20 09:41
description: 使用Openfire和Smack进行即时通讯消息开发
keywords: openfire,smack
comments: true
categories: code
tags: [openfire,smack]
---

  
{% img /images/post/2015-4/openfire.png %}  
  
[Openfire][openfire]是由Ignite Realtime公司用Java开发的一个开源即时通讯服务器，基于XMPP协议（Jabber）进行消息交互，最新版本是3.9.3。该公司旗下有多个Java客户端可供使用，较常使用的是[Smack][smack]，最新的版本是4.1.0，最新的版本与以前的版本相比有较大改动，下面我们就来介绍一下Openfire和Smack的使用。  
  
<!--more-->  
  
## Openfire安装

Openfire的安装非常简单，你可以通过下面几种方式进行安装。  
  
#### docker镜像
  
最简单的方式你可以通过docker下载[Openfire的镜像][openfire_docker]，然后执行下面的命令启动openfire容器。  
  
{% codeblock lang:sh %}
docker run --name=myopenfire -d \ 
	-p 9090:9090 -p 5222:5222 -p 5223:5223 \ 
	-p 7777:7777 -p 7070:7070 -p 7443:7443 \ 
	-p 5229:5229 -p 5269:5269 sameersbn/openfire:3.9.3-2
{% endcodeblock %} 
  
#### 直接安装
  
这里是openfire的[安装向导][openfire_install]，有各个平台的指南，不过首先要安装JDK1.5+，然后按照指南进行安装即可。  
  
## Smack 

最新的Smack版本是4.1.0，跟之前版本的API有很大区别，据说4.2.0的API差别会更大，[这里][smack_example]是Smack4.1.0的代码使用示例。  

#### 创建连接

不同于以前版本的是ConnectionConfig不再是new出来的，而是通过builder来创建。这个导致Spring的integration-xmpp组件不支持使用，只能自己写连接的类了。  
  
{% codeblock lang:java %}
	public XMPPTCPConnection createConnection() throws Exception {
        XMPPTCPConnectionConfiguration connectionConfig = XMPPTCPConnectionConfiguration.builder()
                .setUsernameAndPassword("username", "password")
                .setServiceName("your.server.name")
                .setHost("your.server.ip")
                .setConnectTimeout(3000)
                .setSendPresence(false) // 设置用户是否上线
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled) //不使用安全模式
                .build();

        XMPPTCPConnection connection = new XMPPTCPConnection(connectionConfig);
        connection.connect().login(); 
        //这里的login方法如果没传username和password，就是以之前set的用户登录，传了的话就是以传入的用户登录
        return  connection;
    }
{% endcodeblock %} 
  
#### 创建用户
  
像AccountManager这类对象也不再是new出来的，而是通过getInstance传入connection对象得到。  
  
{% codeblock lang:java %}
    public void createUser(XMPPTCPConnectionConfiguration connection) throws Exception {
        AccountManager accountManager = AccountManager.getInstance(connection);
        accountManager.createAccount("username", "password");
    }
{% endcodeblock %} 
  

#### 发送消息
  
{% codeblock lang:java %}
    public void sendMessage(XMPPTCPConnectionConfiguration connection) throws Exception {
        ChatManager chatManager = ChatManager.getInstanceFor(connection);
        Chat chat = chatManager.createChat("jid like username@your.server.name");
        chat.sendMessage("Hello word1!");
    }
{% endcodeblock %} 
  
#### 获取离线消息
  
注意在创建连接时如果setSendPresence没有设为false，那么在获取离线消息时是始终获取不到的，因为setSendPresence表示已上线，一旦用户上线离线消息就没有了。  
  
{% codeblock lang:java %}
    public void receiveOfflineMessages(XMPPTCPConnectionConfiguration connection) throws Exception {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);
        List<Message> messages = offlineMessageManager.getMessages();
        for (Message message : messages) {
            System.out.println(message);
        }
    }
{% endcodeblock %} 
  
#### 删除离线消息
  
离线消息是根据时间来删除的，我们可以在header中获取到消息的时间戳。  
  
{% codeblock lang:java %}
    public void deleteOfflineMessages(XMPPTCPConnectionConfiguration connection) throws Exception {
        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);
        List<OfflineMessageHeader> headers = offlineMessageManager.getHeaders();
        List<String> stamps = new ArrayList<>();
        for (OfflineMessageHeader header : headers) {
            stamps.add(header.getStamp());
        }

        offlineMessageManager.deleteMessages(stamps);
    }
{% endcodeblock %} 
  
消息的时间我们还可以通过这种方式获取，这里获取到的时间是一个Date对象，而上面的方式是获取一个时间的String。  
  
{% codeblock lang:java %}
    public long getMessageTime(Message message) {
        DelayInformation delay = message.getExtension(DelayInformation.ELEMENT, DelayInformation.NAMESPACE);
        if (delay == null) return 0;
        if (delay.getStamp() == null) return 0;

        return delay.getStamp().getTime();
    }
{% endcodeblock %} 
  
#### 创建聊天室
  
Smack还可以创建多人聊天，openfire服务器搭建好后会默认创建一个名为conference的分组聊天服务，我们可以在上面创建聊天室。  
  
{% codeblock lang:java %}
    public void createRoom(XMPPTCPConnectionConfiguration connection) throws Exception {
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat muc = multiUserChatManager.getMultiUserChat("room jid like roomname@conference.your.server.name");
        muc.createOrJoin("nickname");

        Form form = muc.getConfigurationForm();
        Form submitForm = form.createAnswerForm();
        List<FormField> fields = form.getFields();
        for (FormField field : fields) {
            if (!FormField.Type.hidden.equals(field.getType()) && field.getVariable() != null) {
                submitForm.setDefaultAnswer(field.getVariable());
            }
        }
        // 这里设置聊天室为公共聊天室
        submitForm.setAnswer("muc#roomconfig_publicroom", true);
        // 这里设置聊天室是永久存在的
        submitForm.setAnswer("muc#roomconfig_persistentroom", true);
        muc.sendConfigurationForm(submitForm);
    }
{% endcodeblock %} 
  
#### 发送多人聊天消息
  
注意: 在发送聊天室消息之前，必须先加入聊天室(调用join方法)，否则发送的消息实际上是没有发送成功的。  
  
{% codeblock lang:java %}
    public void sendRoomMessage(XMPPTCPConnectionConfiguration connection) throws Exception {
    	MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat muc = multiUserChatManager.getMultiUserChat("room jid like roomname@conference.your.server.name");
        muc.createOrJoin("nickname");
        muc.sendMessage("hello world");
    }
{% endcodeblock %} 
  
#### 获取聊天室消息

获取聊天室消息，是在加入聊天室时传入一个DiscussionHistory对象，这个对象可以设置需要获取多少条聊天记录，或者从什么时候开始的聊天记录等。  
注意: nextMessage方法如果不带超时参数，会使用默认的连接超时时间，一般是5S，加入时间参数可以缩短整个方法的执行时间。  
  
{% codeblock lang:java %}
    public List<Message> getRoomChat(XMPPTCPConnectionConfiguration connection) throws Exception {
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
        MultiUserChat muc = multiUserChatManager.getMultiUserChat("room jid like roomname@conference.your.server.name");
        DiscussionHistory discussionHistory = new DiscussionHistory();
        //取某个时间点开始的聊天室消息
        discussionHistory.setSince(new Date(1427090003460L));
        muc.createOrJoin("nick", null, discussionHistory, connection.getPacketReplyTimeout());
        List<Message> messages = new ArrayList<>();
        while (true) {
        	//这里超时时间设置为100毫秒
            Message message = muc.nextMessage(100);
            if (message == null) break;

            System.out.println(message);
            messages.add(message);
        }
        return messages;
    }
{% endcodeblock %} 
  
## 保存聊天消息
  
Openfire一般是不保存历史消息的，包括P2P(个人对个人)或MUC(多人聊天)的都不保存，离线消息会暂时保存在`ofOffline`这张表中，如果离线消息已读就会从该表中删除。  

如果我们需要保存历史消息可以通过添加插件的方式来记录。  

* 在Openfire控制台，进入`插件`页面，选择`有效的插件`，在里面选择Monitoring Service进行添加。
  
{% img /images/post/2015-4/monitoring_service.png %}  
  
* 安装完成后可以在`插件`页面看到已经安装好的插件。  
  
{% img /images/post/2015-4/openfire_plugin.png %}  
  
* 在`服务器`页面会看到新增了2个子页面，`归档文件`和`统计表`，进入`归档文件`页面的存档设置勾上`Archive one-to-one chats`和`Archive group chats`选项。
  
{% img /images/post/2015-4/openfire_archive.png %}  
  
* 设置好后以后不管是个人聊天还是聊天室的聊天记录都会记录到数据库的`ofMessageArchive`表中，不过消息是异步保存的，大概会在消息发送后1分钟左右再存到数据库。  
  
[openfire]: http://www.igniterealtime.org/index.jsp
[smack]: http://www.igniterealtime.org/projects/smack/index.jsp
[openfire_docker]: https://registry.hub.docker.com/u/sameersbn/openfire/dockerfile/
[openfire_install]: http://www.igniterealtime.org/builds/openfire/docs/latest/documentation/install-guide.html
[smack_example]: https://www.igniterealtime.org/builds/smack/docs/latest/documentation/