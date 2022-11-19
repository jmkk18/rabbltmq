# 一. 消息队列 - 介绍
## 1.MQ的相关概念
### 1.1 什么是MQ
>MQ(message queue)，从字面意思上看，本质是个队列，FIFO 先入先出，只不过队列中存放的内容是 message 而已，还是一种跨进程的通信机制，用于上下游传递消息。在互联网架构中，MQ 是一种非常常 见的上下游“逻辑解耦+物理解耦”的消息通信服务。使用了 MQ 之后，消息发送上游只需要依赖 MQ，不 用依赖其他服务。
### 1.2 为什么要用MQ
- 流量消峰
>举个例子，如果订单系统最多能处理一万次订单，这个处理能力应付正常时段的下单时绰绰有余，正常时段我们下单一秒后就能返回结果。但是在高峰期，如果有两万次下单操作系统是处理不了的，只能限制订单超过一万后不允许用户下单。使用消息队列做缓冲，我们可以取消这个限制，把一秒内下的订单分散成一段时间来处理，这时有些用户可能在下单十几秒后才能收到下单成功的操作，但是比不能下单的体验要好。
- 应用解耦
>以电商应用为例，应用中有订单系统、库存系统、物流系统、支付系统。用户创建订单后，如果耦合 调用库存系统、物流系统、支付系统，任何一个子系统出了故障，都会造成下单操作异常。当转变成基于 消息队列的方式后，系统间调用的问题会减少很多，比如物流系统因为发生故障，需要几分钟来修复。在 这几分钟的时间里，物流系统要处理的内存被缓存在消息队列中，用户的下单操作可以正常完成。当物流 系统恢复后，继续处理订单信息即可，中单用户感受不到物流系统的故障，提升系统的可用性。

![在这里插入图片描述](https://img-blog.csdnimg.cn/9881e3e1c7414722a3459c97290243f1.png)
- 异步处理
>有些服务间调用是异步的，例如 A 调用 B，B 需要花费很长时间执行，但是 A 需要知道 B 什么时候可以执行完。
<span>
以前一般有两种方式，A 过一段时间去调用 B 的查询 api 查询。或者 A 提供一个 callback api， B 执行完之后调用 api 通知 A 服务。这两种方式都不是很优雅。
<span>
使用消息总线，可以很方便解决这个问题， A 调用 B 服务后，只需要监听 B 处理完成的消息，当 B 处理完成后，会发送一条消息给 MQ，MQ 会将此消息转发给 A 服务。这样 A 服务既不用循环调用 B 的查询 api，也不用提供 callback api。同样B 服务也不用 做这些操作。A 服务还能及时的得到异步处理成功的消息。

![在这里插入图片描述](https://img-blog.csdnimg.cn/55df7478a09243fb85bed3e18db9b572.png)

### 1.3 MQ 的分类
>ActiveMQ

==优点==：单机吞吐量万级，时效性 ms 级，可用性高，基于主从架构实现高可用性，消息可靠性较 低的概率丢失数据

==缺点==：官方社区现在对 ActiveMQ 5.x 维护越来越少，高吞吐量场景较少使用。

>Kafka

大数据的杀手锏，谈到大数据领域内的消息传输，则绕不开 Kafka，这款为**大数据**而生的消息中间件， 以其百万级 TPS 的吞吐量名声大噪，迅速成为大数据领域的宠儿，在数据采集、传输、存储的过程中发挥 着举足轻重的作用。目前已经被 LinkedIn，Uber, Twitter, Netflix 等大公司所采纳。

==优点==：性能卓越，单机写入 TPS 约在百万条/秒，最大的优点，就是**吞吐量高**。时效性 ms 级可用性非 常高，kafka 是分布式的，一个数据多个副本，少数机器宕机，不会丢失数据，不会导致不可用,消费者采 用 Pull 方式获取消息, 消息有序, 通过控制能够保证所有消息被消费且仅被消费一次;有优秀的第三方Kafka Web 管理界面 Kafka-Manager；在日志领域比较成熟，被多家公司和多个开源项目使用；功能支持： 功能 较为简单，主要支持简单的 MQ 功能，在大数据领域的实时计算以及日志采集被大规模使用

==缺点==：Kafka 单机超过 64 个队列/分区，Load 会发生明显的飙高现象，队列越多，load 越高，发送消 息响应时间变长，使用短轮询方式，实时性取决于轮询间隔时间，消费失败不支持重试；支持消息顺序， 但是一台代理宕机后，就会产生消息乱序，**社区更新较慢**

>RocketMQ

RocketMQ 出自阿里巴巴的开源产品，用 Java 语言实现，在设计时参考了 Kafka，并做出了自己的一 些改进。被阿里巴巴广泛应用在订单，交易，充值，流计算，消息推送，日志流式处理，binglog 分发等场 景。

==优点==：**单机吞吐量十万级**,可用性非常高，分布式架构，消息**可以做到 0 丢失**,MQ 功能较为完善，还是分 布式的，扩展性好,**支持 10 亿级别的消息堆积**，不会因为堆积导致性能下降,源码是 java 我们可以自己阅 读源码，定制自己公司的 MQ

==缺点==：**支持的客户端语言不多**，目前是 java 及 c++，其中 c++不成熟；社区活跃度一般,没有在MQ 核心中去实现 JMS 等接口,有些系统要迁移需要修改大量代码

>RabbitMQ

2007 年发布，是一个在AMQP(高级消息队列协议)基础上完成的，可复用的企业消息系统，**是当前最主流的消息中间件之一**。

==优点==：由于 erlang 语言的高并发特性，性能较好；吞吐量到万级，MQ 功能比较完备,健壮、稳定、易 用、跨平台、支持多种语言 如：Python、Ruby、.NET、Java、JMS、C、PHP、ActionScript、XMPP、STOMP 等，支持 AJAX 文档齐全；开源提供的管理界面非常棒，用起来很好用,社区活跃度高；更新频率相当高

官网更新：[https://www.rabbitmq.com/news.html](https://www.rabbitmq.com/news.html%28opens%20new%20window%29)

==缺点==：商业版需要收费,学习成本较高

### 1.4 MQ 的选择
- Kafka

	Kafka 主要特点是基于Pull 的模式来处理消息消费，追求高吞吐量，一开始的目的就是用于日志收集 和传输，适合产生大量数据的互联网服务的数据收集业务。大型公司建议可以选用，如果有日志采集功能， 肯定是首选 kafka 了。
- RocketMQ

	天生为**金融互联网**领域而生，对于可靠性要求很高的场景，尤其是电商里面的订单扣款，以及业务削 峰，在大量交易涌入时，后端可能无法及时处理的情况。RoketMQ 在稳定性上可能更值得信赖，这些业务 场景在阿里双 11 已经经历了多次考验，如果你的业务有上述并发场景，建议可以选择 RocketMQ。
- RabbitMQ

	结合 erlang 语言本身的并发优势，性能好时效性微秒级，社区活跃度也比较高，管理界面用起来十分 方便，如果你的**数据量没有那么大**，中小型公司优先选择功能比较完备的 RabbitMQ。

## 2. RabbitMQ

### 2.1 RabbitMQ 的概念
RabbitMQ 是一个消息中间件：它接受并转发消息。

你可以把它当做一个快递站点，当你要发送一个包裹时，你把你的包裹放到快递站，快递员最终会把你的快递送到收件人那里，按照这种逻辑 RabbitMQ 是 一个快递站，一个快递员帮你传递快件。

RabbitMQ 与快递站的主要区别在于，它不处理快件而是接收， 存储和转发消息数据。

![在这里插入图片描述](https://img-blog.csdnimg.cn/317b6b1f0ada4bf4aeec5c9cc93d6ad0.png)
官网：[https://www.rabbitmq.com/#features](https://www.rabbitmq.com/#features)
### 2.2 四大核心概念
- 生产者

	产生数据发送消息的程序是生产者

- 交换机

	交换机是 RabbitMQ 非常重要的一个部件，一方面它接收来自生产者的消息，另一方面它将消息 推送到队列中。交换机必须确切知道如何处理它接收到的消息，是将这些消息推送到特定队列还是推 送到多个队列，亦或者是把消息丢弃，这个得有交换机类型决定

- 队列

	队列是 RabbitMQ 内部使用的一种数据结构，尽管消息流经 RabbitMQ 和应用程序，但它们只能存 储在队列中。队列仅受主机的内存和磁盘限制的约束，本质上是一个大的消息缓冲区。许多生产者可 以将消息发送到一个队列，许多消费者可以尝试从一个队列接收数据。这就是我们使用队列的方式

- 消费者

	消费与接收具有相似的含义。消费者大多时候是一个等待接收消息的程序。请注意生产者，消费 者和消息中间件很多时候并不在同一机器上。同一个应用程序既可以是生产者又是可以是消费者。
### 2.3 各个名词介绍
![在这里插入图片描述](https://img-blog.csdnimg.cn/8b042ccb3d874ef48825e6c34070085d.png)
- Broker

	接收和分发消息的应用，RabbitMQ Server 就是 Message Broker
	
- Virtual host

	出于多租户和安全因素设计的，把 AMQP 的基本组件划分到一个虚拟的分组中，类似 于网络中的 namespace 概念。当多个不同的用户使用同一个 RabbitMQ server 提供的服务时，可以划分出 多个 vhost，每个用户在自己的 vhost 创建 exchange／queue 等
- Connection

	publisher／consumer 和 broker 之间的 TCP 连接
- Channel

	如果每一次访问 RabbitMQ 都建立一个 Connection，在消息量大的时候建立 TCP Connection 的开销将是巨大的，效率也较低。Channel 是在 connection 内部建立的逻辑连接，如果应用程 序支持多线程，通常每个 thread 创建单独的 channel 进行通讯，AMQP method 包含了 channel id 帮助客 户端和 message broker 识别 channel，所以 channel 之间是完全隔离的。Channel 作为轻量级的 Connection 极大减少了操作系统建立 TCP connection 的开销
- Exchange

	message 到达 broker 的第一站，根据分发规则，匹配查询表中的 routing key，分发 消息到 queue 中去。常用的类型有：direct (point-to-point), topic (publish-subscribe) and fanout (multicast)
- Queue

	消息最终被送到这里等待 consumer 取走
- Binding

	exchange 和 queue 之间的虚拟连接，binding 中可以包含 routing key，Binding 信息被保 存到 exchange 中的查询表中，用于 message 的分发依据


# 二.RabbitMQ - 安装


## 1. 安装RabbitMQ
### 1.1 下载
官网下载地址：[https://www.rabbitmq.com/download.html](https://www.rabbitmq.com/download.html)
- rabbitmq-server-3.8.8-1.el7.noarch.rpm
链接：[https://packagecloud.io/rabbitmq/rabbitmq-server/packages/el/7/rabbitmq-server-3.8.8-1.el7.noarch.rpm](https://packagecloud.io/rabbitmq/rabbitmq-server/packages/el/7/rabbitmq-server-3.8.8-1.el7.noarch.rpm)
- erlang-21.3.8.21-1.el7.x86_64.rpm
链接：[https://packagecloud.io/rabbitmq/erlang/packages/el/7/erlang-21.3.8.21-1.el7.x86_64.rpm](https://packagecloud.io/rabbitmq/erlang/packages/el/7/erlang-21.3.8.21-1.el7.x86_64.rpm)

Red Hat 8, CentOS 8 和 modern Fedora 版本，把 “el7” 替换成 “el8”
### 1.2 安装
上传到 `/usr/local/software `目录下(如果没有 software 需要自己创建)
```sh
rpm -ivh erlang-21.3.8.21-1.el7.x86_64.rpm
yum install socat -y
rpm -ivh rabbitmq-server-3.8.8-1.el7.noarch.rpm
```
### 1.3 启动
```sh
# 启动服务
systemctl start rabbitmq-server
# 查看服务状态
systemctl status rabbitmq-server
# 开机自启动
systemctl enable rabbitmq-server
# 停止服务
systemctl stop rabbitmq-server
# 重启服务
systemctl restart rabbitmq-server
```
## 2. Web管理界面及授权操作
### 2.1 安装
默认情况下，是没有安装web端的客户端插件，需要安装才可以生效
```sh
rabbitmq-plugins enable rabbitmq_management
```
安装完毕以后，重启服务即可
```sh
systemctl restart rabbitmq-server
```

访问 http://虚拟机ip:15672 ，用默认账号密码(guest)登录，出现权限问题

默认情况只能在 localhost 本机下访问，所以需要添加一个远程登录的用户

### 2.2 添加用户
```sh
# 创建账号和密码
rabbitmqctl add_user admin 123456

# 设置用户角色
rabbitmqctl set_user_tags admin administrator

# 为用户添加资源权限
# set_permissions [-p <vhostpath>] <user> <conf> <write> <read>
# 添加配置、写、读权限
rabbitmqctl set_permissions -p "/" admin ".*" ".*" ".*"
```
用户级别：
1. administrator：可以登录控制台、查看所有信息、可以对 rabbitmq 进行管理
2. monitoring：监控者 登录控制台，查看所有信息
3. policymaker：策略制定者 登录控制台，指定策略
4. managment：普通管理员 登录控制台

再次登录，用 admin 用户

>重置命令

关闭应用的命令为：rabbitmqctl stop_app

清除的命令为：rabbitmqctl reset

重新启动命令为：rabbitmqctl start_app


# 三.RabbitMQ - 简单案例

## 1. Hello world
我们将用 Java 编写两个程序。发送单个消息的生产者和接收消息并打印出来的消费者

在下图中，“ P” 是我们的生产者，“ C” 是我们的消费者。中间的框是一个队列 RabbitMQ 代表使用者保留的消息缓冲区

![在这里插入图片描述](https://img-blog.csdnimg.cn/de135659766b4883b8fff2d31ba32199.png)

连接的时候，需要开启 15672 端口

![在这里插入图片描述](https://img-blog.csdnimg.cn/7c57ed6fb1044005b843c1ee4ad57061.png)

- 依赖

pom.xml
```xml
  <dependencies>
    <!--rabbitmq 依赖客户端-->
    <dependency>
      <groupId>com.rabbitmq</groupId>
      <artifactId>amqp-client</artifactId>
      <version>5.8.0</version>
    </dependency>
    <!--操作文件流的一个依赖-->
    <dependency>
      <groupId>commons-io</groupId>
      <artifactId>commons-io</artifactId>
      <version>2.6</version>
    </dependency>
  </dependencies>
```

- 消息生产者

发送消息
```java
package com.jm.rabbitmq.one;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 生产者 ：发消息
 */
public class Producer {
    //队列名称
    public static final String QUEUE_NAME="hello";

    //发消息
    public static void main(String[] args) throws Exception {
        //创建一个连接工厂
        ConnectionFactory factory=new ConnectionFactory();
        //工厂IP 连接rabbidmq的队列
        factory.setHost("192.168.36.100");
        //用户名
        factory.setUsername("admin");
        //密码
        factory.setPassword("123");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();
        /**
         * 生成一个队列
         * 1.队列名称
         * 2.队列里的名称是否持久化(磁盘)，默认情况存储在内存中
         * 3.该队列是否提供一个消费者进行消费，是否进行消费共享，true可以多个消费者消费 false:只能一个消费者消费
         * 4.是否自动删除 最后一个消费者断开连接之后，该队列是否自动删除，true自动删除，false不自动删除
         * 5.其他参数
         */
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //发消息
        String message="hello world!";
        /**
         * 发送一个消费
         * 1.发送到哪个交换机
         * 2.路由的Key值是哪个，本次队列的名称
         * 3.其他参数
         * 4.发送消息的消息体
         */
        channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
        System.out.println("消息发送完毕");

    }
}

```

- 消息消费者

获取“生产者”发出的消息

```java
package com.jm.rabbitmq.one;

import com.rabbitmq.client.*;

/**
 * 消费者 ：接收消息的
 */
public class Consumer {
    //队列的名称
    public static final String QUEUE_NAME="hello";

    //接收消息
    public static void main(String[] args) throws Exception{
        //创建连接工厂
        ConnectionFactory factory=new ConnectionFactory();
        factory.setHost("192.168.36.100");
        factory.setUsername("admin");
        factory.setPassword("123");
        //创建连接
        Connection connection = factory.newConnection();
        //获取信道
        Channel channel = connection.createChannel();

        //声明 接收消息
        DeliverCallback deliverCallback= (consumerTag,message) ->{
            System.out.println(new String(message.getBody()));
        };
        //取消消息时的回调
        CancelCallback cancelCallback= consumerTag ->{
            System.out.println("消息消费被中断");
        };

        /**
         * 消费者 接收消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答 true 代表的是自动应答 false 代表手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取消消费的回调
         */
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}

```

## 2. Work Queues

Work Queues——工作队列(又称任务队列)的主要思想是避免立即执行资源密集型任务，而不得不等待它完成。 相反我们安排任务在之后执行。我们把任务封装为消息并将其发送到队列。在后台运行的工作进 程将弹出任务并最终执行作业。当有多个工作线程时，这些工作线程将一起处理这些任务。

### 轮训分发消息
在这个案例中我们会启动两个工作线程，一个消息发送线程，我们来看看他们两个工作线程是如何工作的。

#### (1) 抽取工具类
```java
package com.jm.rabbitmq.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMqUtils {
    //得到一个连接的 channel
    public static Channel getChannel() throws Exception{
        //创建一个连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.36.100");
        factory.setUsername("admin");
        factory.setPassword("123");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        return channel;
    }
}

```
#### (2) 启动两个工作线程来接受消息
```java
package com.jm.rabbitmq.two;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 这是一个工作线程（相当于消费者）
 */
public class Worker01 {
    //对立的名称
    public static final String QUEUE_NAME="hello";

    //接收消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        //消息的接收
        DeliverCallback deliverCallback= (consumerTag,message) -> {
            System.out.println("接收到的消息："+new String(message.getBody()));
        };

        //消息接收被取消时，执行下面的内容
        CancelCallback cancelCallback= (consumerTag) -> {
            System.out.println(consumerTag+"->消息消费被中断");
        };
        /**
         * 消费者 接收消息
         * 1.消费哪个队列
         * 2.消费成功之后是否要自动应答 true 代表的是自动应答 false 代表手动应答
         * 3.消费者未成功消费的回调
         * 4.消费者取消消费的回调
         */
        System.out.println("C2等待接收消息......");
        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}
```
#### (3) 启动一个发送消息线程
```java
package com.jm.rabbitmq.two;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;

import java.util.Queue;
import java.util.Scanner;

/**
 * 生产者 ：发送大量消息
 */
public class Task01 {
    //队列名称
    public static final String QUEUE_NAME="hello";

    //发送大量消息
    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        /**
         * 生成一个队列
         * 1.队列名称
         * 2.队列里的名称是否持久化(磁盘)，默认情况存储在内存中
         * 3.该队列是否提供一个消费者进行消费，是否进行消费共享，true可以多个消费者消费 false:只能一个消费者消费
         * 4.是否自动删除 最后一个消费者断开连接之后，该队列是否自动删除，true自动删除，false不自动删除
         * 5.其他参数
         */
        //声明队列
        channel.queueDeclare(QUEUE_NAME,false,false,false,null);
        //从控制台当中输入接收消息
        Scanner scanner=new Scanner(System.in);
        while (scanner.hasNext()){
            String message = scanner.next();
            /**
             * 发送一个消费
             * 1.发送到哪个交换机
             * 2.路由的Key值是哪个，本次队列的名称
             * 3.其他参数
             * 4.发送消息的消息体
             */
            channel.basicPublish("", QUEUE_NAME,null,message.getBytes());
            System.out.println("发送消息完成："+message);
        }

    }
}
```
- 结果展示

通过程序执行发现生产者总共发送 4 个消息，消费者 1 和消费者 2 分别分得两个消息，并且是按照有序的一个接收一次消息

![在这里插入图片描述](https://img-blog.csdnimg.cn/9ffb266b0bf74f5aae34ee7c75b7f739.png)

## 3. 消息应答
消费者完成一个任务可能需要一段时间，如果其中一个消费者处理一个长的任务并仅只完成了部分突然它挂掉了，会发生什么情况。RabbitMQ 一旦向消费者传递了一条消息，便立即将该消息标记为删除。在这种情况下，突然有个消费者挂掉了，我们将丢失正在处理的消息。以及后续发送给该消费这的消息，因为它无法接收到。

为了保证消息在发送过程中不丢失，引入消息应答机制，消息应答就是：**消费者在接收到消息并且处理该消息之后，告诉 rabbitmq 它已经处理了，rabbitmq 可以把该消息删除了**。

### 3.1 自动应答
消息发送后立即被认为已经传送成功，这种模式需要在**高吞吐量和数据传输安全性方面做权衡**,因为这种模式如果消息在接收到之前，消费者那边出现连接或者 channel 关闭，那么消息就丢失 了,当然另一方面这种模式消费者那边可以传递过载的消息，**没有对传递的消息数量进行限制**，当然这样有可能使得消费者这边由于接收太多还来不及处理的消息，导致这些消息的积压，最终使 得内存耗尽，最终这些消费者线程被操作系统杀死，**所以这种模式仅适用在消费者可以高效并以 某种速率能够处理这些消息的情况下使用**。

### 3.2 手动消息应答的方法
- Channel.basicAck(用于肯定确认)

	RabbitMQ 已知道该消息并且成功的处理消息，可以将其丢弃了

- Channel.basicNack(用于否定确认)

- Channel.basicReject(用于否定确认)

	与 Channel.basicNack 相比少一个参数，不处理该消息了直接拒绝，可以将其丢弃了

**Multiple 的解释：**

手动应答的好处是可以批量应答并且减少网络拥堵


![在这里插入图片描述](https://img-blog.csdnimg.cn/94b6d5b4d7754ebcb7b265417ee33b89.png)
- true 代表批量应答 channel 上未应答的消息

	比如说 channel 上有传送 tag 的消息 5,6,7,8 当前 tag 是8 那么此时5-8 的这些还未应答的消息都会被确认收到消息应答

- false 同上面相比只会应答 tag=8 的消息 5,6,7 这三个消息依然不会被确认收到消息应答

	![在这里插入图片描述](https://img-blog.csdnimg.cn/9b3527d4e31d401e9221c1f9a0648c06.png)

### 3.3 消息自动重新入队
如果消费者由于某些原因失去连接(其通道已关闭，连接已关闭或 TCP 连接丢失)，导致消息未发送 ACK 确认，RabbitMQ 将了解到消息未完全处理，并将对其重新排队。如果此时其他消费者可以处理，它将很快将其重新分发给另一个消费者。这样，即使某个消费者偶尔死亡，也可以确保不会丢失任何消息。

![在这里插入图片描述](https://img-blog.csdnimg.cn/3e5a2f7063144070a1810dc4ab764c73.png)

### 3.4 消息手动应答代码
默认消息采用的是自动应答，所以我们要想实现消息消费过程中不丢失，需要把自动应答改为手动应答

消费者在上面代码的基础上增加了以下内容

```java
channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
```
**消息生产者：**
```java
package com.jm.rabbitmq.three;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

/**
 * 消息在手动应答时不丢失、放回队列中重新消费
 */
public class Task02 {
    //队列名称
    public static final String TASK_QUEUE_NAME="ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明队列
        boolean durable=true;//需要让队列持久化
        channel.queueDeclare(TASK_QUEUE_NAME,durable,false,false,null);
        //从控制台输入消息
        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNext()){
            String message = scanner.next();
            channel.basicPublish("",TASK_QUEUE_NAME,MessageProperties.PERSISTENT_TEXT_PLAIN,message.getBytes("UTF-8"));
            System.out.println("消费者发送消息成功："+message);
        }
    }
}
```
**消费者 01：**
```java
package com.jm.rabbitmq.three;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.jm.rabbitmq.util.SleepUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 消息在手动应答时不丢失、放回队列中重新消费
 */
public class Worker03 {
    //队列名称
    public static final String TASK_QUEUE_NAME="ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        System.out.println("C1等待接收消息处理时间较短");

        DeliverCallback deliverCallback=(consumerTag,message)->{
            //沉睡1s
            SleepUtils.sleep(1);
            System.out.println("接收到的消息："+new String(message.getBody(),"UTF-8"));
            //手动应答
            /**
             * 1.消息的标记 tag
             * 2.是否批量应答 false：不批量应答信道中的消息 true：批量
             */
            channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
        };
        //设置不公平分发
        //channel.basicQos(1);
        //设置预取值2
        channel.basicQos(2);
        //采用手动应答
        channel.basicConsume(TASK_QUEUE_NAME,false,deliverCallback,consumerTag -> {
            System.out.println(consumerTag+"消费者取消消费接口回调逻辑");
        });
    }
}
```

**消费者 02：**

​ 把时间改成30秒

>手动应答效果演示

正常情况下消息发送方发送两个消息 C1 和 C2 分别接收到消息并进行处理

![在这里插入图片描述](https://img-blog.csdnimg.cn/16c95d4e1cd04ba9bbeed7a265df8a3d.png)

在发送者发送消息 dd，发出消息之后的把 C2 消费者停掉，按理说该 C2 来处理该消息，但是由于它处理时间较长，在还未处理完，也就是说 C2 还没有执行 ack 代码的时候，C2 被停掉了， 此时会看到消息被 C1 接收到了，说明消息 dd 被重新入队，然后分配给能处理消息的 C1 处理了

![在这里插入图片描述](https://img-blog.csdnimg.cn/50878f1469c14005a82b50799e89a7dd.png)


## 4. RabbitMQ 持久化
当 RabbitMQ 服务停掉以后，消息生产者发送过来的消息不丢失要如何保障？默认情况下 RabbitMQ 退出或由于某种原因崩溃时，它忽视队列和消息，除非告知它不要这样做。确保消息不会丢失需要做两件事：**我们需要将队列和消息都标记为持久化。**

>队列如何实现持久化

之前我们创建的队列都是非持久化的，rabbitmq 如果重启的化，该队列就会被删除掉，如果要队列实现持久化需要在声明队列的时候把 durable 参数设置为持久化

```java
//让队列持久化
boolean durable = true;
//声明队列
channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
```
注意：如果之前声明的队列不是持久化的，需要把原先队列先删除，或者重新创建一个持久化的队列，不然就会出现错误

![在这里插入图片描述](https://img-blog.csdnimg.cn/0c69da52383d4d33ab2bd1158e432728.png)

以下为控制台中持久化与非持久化队列的 UI 显示区、

![在这里插入图片描述](https://img-blog.csdnimg.cn/de5e84c6cbc04bcf86487d69a9267a42.png)

>消息实现持久化

需要在消息**生产者**修改代码，`MessageProperties.PERSISTENT_TEXT_PLAIN`添加这个属性。

![在这里插入图片描述](https://img-blog.csdnimg.cn/b91218468e744aa3a2cabc33a6b10a43.png)
将消息标记为持久化并不能完全保证不会丢失消息。尽管它告诉 RabbitMQ 将消息保存到磁盘，但是这里依然存在当消息刚准备存储在磁盘的时候 但是还没有存储完，消息还在缓存的一个间隔点。此时并没 有真正写入磁盘。持久性保证并不强，但是对于我们的简单任务队列而言，这已经绰绰有余了。

如果需要更强有力的持久化策略，参考后边课件发布确认章节。

## 5. 不公平分发
在最开始的时候我们学习到 RabbitMQ 分发消息采用的轮训分发，但是在某种场景下这种策略并不是很好，比方说有两个消费者在处理任务，其中有个**消费者 1** 处理任务的速度非常快，而另外一个**消费者 2** 处理速度却很慢，这个时候我们还是采用轮训分发的化就会到这处理速度快的这个消费者很大一部分时间处于空闲状态，而处理慢的那个消费者一直在干活，这种分配方式在这种情况下其实就不太好，但是 RabbitMQ 并不知道这种情况它依然很公平的进行分发。

为了避免这种情况，在消费者中消费之前，我们可以设置参数 `channel.basicQos(1);`

```java
//不公平分发
int prefetchCount = 1;
channel.basicQos(prefetchCount);
//采用手动应答
boolean autoAck = false;
channel.basicConsume(TASK_QUEUE_NAME, autoAck, deliverCallback, cancelCallback);
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/dce046fb176d4fa0a38fc8833a20fa7a.png)

意思就是如果这个任务我还没有处理完或者我还没有应答你，你先别分配给我，我目前只能处理一个 任务，然后 rabbitmq 就会把该任务分配给没有那么忙的那个空闲消费者，当然如果所有的消费者都没有完 成手上任务，队列还在不停的添加新任务，队列有可能就会遇到队列被撑满的情况，这个时候就只能添加 新的 worker 或者改变其他存储任务的策略。

## 6. 预取值分发

带权的消息分发

本身消息的发送就是异步发送的，所以在任何时候，channel 上肯定不止只有一个消息另外来自消费 者的手动确认本质上也是异步的。因此这里就存在一个未确认的消息缓冲区，因此希望开发人员能**限制此缓冲区的大小，以避免缓冲区里面无限制的未确认消息问题**。这个时候就可以通过使用 `basic.qos` 方法设 置“预取计数”值来完成的。

该值定义通道上允许的未确认消息的最大数量。一旦数量达到配置的数量， RabbitMQ 将停止在通道上传递更多消息，除非至少有一个未处理的消息被确认，例如，假设在通道上有未确认的消息 5、6、7，8，并且通道的预取计数设置为 4，此时RabbitMQ 将不会在该通道上再传递任何消息，除非至少有一个未应答的消息被 ack。比方说 tag=6 这个消息刚刚被确认 ACK，RabbitMQ 将会感知 这个情况到并再发送一条消息。消息应答和 QoS 预取值对用户吞吐量有重大影响。

通常，增加预取将提高 向消费者传递消息的速度。**虽然自动应答传输消息速率是最佳的，但是，在这种情况下已传递但尚未处理的消息的数量也会增加，从而增加了消费者的 RAM 消耗**(随机存取存储器)应该小心使用具有无限预处理的自动确认模式或手动确认模式，消费者消费了大量的消息如果没有确认的话，会导致消费者连接节点的 内存消耗变大，所以找到合适的预取值是一个反复试验的过程，不同的负载该值取值也不同 100 到 300 范 围内的值通常可提供最佳的吞吐量，并且不会给消费者带来太大的风险。

预取值为 1 是最保守的。当然这将使吞吐量变得很低，特别是消费者连接延迟很严重的情况下，特别是在消费者连接等待时间较长的环境 中。对于大多数应用来说，稍微高一点的值将是最佳的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/06d9fa3f4e9d4cc28c543c15a6a114ae.png)



# 四.RabbitMQ - 发布确认
## 1. 发布确认逻辑
生产者将信道设置成 confirm 模式，一旦信道进入 confirm 模式，所有在该信道上面发布的消息都将会被指派一个唯一的 ID(从 1 开始)，一旦消息被投递到所有匹配的队列之后，broker 就会发送一个确认给生产者(包含消息的唯一 ID)，这就使得生产者知道消息已经正确到达目的队列了，如果消息和队列是可持久化的，那么确认消息会在将消息写入磁盘之后发出，broker 回传给生产者的确认消息中 delivery-tag 域包含了确认消息的序列号，此外 broker 也可以设置basic.ack 的 multiple 域，表示到这个序列号之前的所有消息都已经得到了处理。

**confirm 模式最大的好处在于他是异步的**，一旦发布一条消息，生产者应用程序就可以在等信道返回确认的同时继续发送下一条消息，当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果RabbitMQ 因为自身内部错误导致消息丢失，就会发送一条 nack 消息， 生产者应用程序同样可以在回调方法中处理该 nack 消息。

## 2. 发布确认的策略
开启发布确认的方法:

发布确认默认是没有开启的，如果要开启需要调用方法 confirmSelect，每当你要想使用发布确认，都需要在 channel 上调用该方法

```java
//开启发布确认
channel.confirmSelect();
```

### 2.1 单个确认发布
这是一种简单的确认方式，它是一种**同步确认发布**的方式，也就是发布一个消息之后只有它被确认发布，后续的消息才能继续发布，`waitForConfirmsOrDie(long)` 这个方法只有在消息被确认的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。

这种确认方式有一个最大的缺点就是：**发布速度特别的慢**，因为如果没有确认发布的消息就会阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某些应用程序来说这可能已经足够了。

```java
//单个确认
public static void publishMessageIndividually() throws Exception {
    Channel channel = RabbitMqUtils.getChannel();
    //队列的声明
    String queueName= UUID.randomUUID().toString();
    channel.queueDeclare(queueName,true,false,false,null);
    //开启发布确认
    channel.confirmSelect();
    //开始时间
    long beginTime = System.currentTimeMillis();

    //批量发消息，单个确认
    for (int i = 0; i < MESSAGE_COUNT; i++) {
        String message=i+"";
        channel.basicPublish("",queueName,null,message.getBytes());
        //单个消息马上进行发布确认
        boolean flag = channel.waitForConfirms();
        if(flag){
            System.out.println("消息发送成功");
        }
    }

    //结束时间
    long endTime = System.currentTimeMillis();
    System.out.println("发布"+MESSAGE_COUNT+"个单独确认消息，耗时："+(endTime-beginTime)+"ms");
}
```

### 2.2 批量确认发布
上面那种方式非常慢，与单个等待确认消息相比，先发布一批消息然后一起确认可以极大地提高吞吐量，当然这种方式的缺点就是：当发生故障导致发布出现问题时，不知道是哪个消息出 问题了，我们必须将整个批处理保存在内存中，以记录重要的信息而后重新发布消息。当然这种方案仍然是同步的，也一样阻塞消息的发布。

```java
//批量确认
public static void publishMessageBatch() throws Exception {
     Channel channel = RabbitMqUtils.getChannel();
     //队列的声明
     String queueName= UUID.randomUUID().toString();
     channel.queueDeclare(queueName,true,false,false,null);
     //开启发布确认
     channel.confirmSelect();
     //开始时间
     long beginTime = System.currentTimeMillis();

     //批量确认消息大小
     int batchSize=100;

     //批量发消息，批量确认
     for (int i = 0; i < MESSAGE_COUNT; i++) {
         String message=i+"";
         channel.basicPublish("",queueName,null,message.getBytes());
         if(i%batchSize==0){
             //发布确认
             channel.waitForConfirms();
         }
     }
     //余条消息，发布确认
     channel.waitForConfirms();

     //结束时间
     long endTime = System.currentTimeMillis();
     System.out.println("发布"+MESSAGE_COUNT+"个批量确认消息，耗时："+(endTime-beginTime)+"ms");
 }
```

### 2.3 异步确认发布
异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都没得说， 他是利用回调函数来达到消息可靠性传递的，这个中间件也是通过函数回调来保证是否投递成功， 下面就让我们来详细讲解异步确认是怎么实现的。

![在这里插入图片描述](https://img-blog.csdnimg.cn/17fa05134b444ae68e59fbb9f0f9cf63.png)

如何处理异步未确认消息?

最好的解决的解决方案就是把未确认的消息放到一个基于内存的能被发布线程访问的队列， 比如说用 ConcurrentLinkedQueue 这个队列在 confirm callbacks 与发布线程之间进行消息的传递。
```java
//异步批量确认
public static void publishMessageAsync() throws Exception {
    Channel channel = RabbitMqUtils.getChannel();
    //队列的声明
    String queueName= UUID.randomUUID().toString();
    channel.queueDeclare(queueName,true,false,false,null);
    //开启发布确认
    channel.confirmSelect();

    /**
     * 线程安全有序的一个哈希表 适用于高并发的情况下
     * 1.轻松的将序号与消息进行关联
     * 2.轻松的批量删除条目 只要给序号
     * 3.支持高并发（多线程）
     */
    ConcurrentSkipListMap<Long,String> outstandingConfirms=
            new ConcurrentSkipListMap<>();

    //消息确认成功 回调函数
    ConfirmCallback ackCallback= (deliveryTag,multiple)->{
        if(multiple){
            //2.删除掉已经确认的消息 剩下的就是未确认的消息
            ConcurrentNavigableMap<Long, String> confirmed =
                    outstandingConfirms.headMap(deliveryTag);
            confirmed.clear();
        }else{
            outstandingConfirms.remove(deliveryTag);
        }
        System.out.println("确认的消息："+deliveryTag);
    };
    //消息确认失败 回调函数
    /**
     * 1.消息的标识
     * 2.是否为批量确认
     */
    ConfirmCallback nackCallback= (deliveryTag,multiple)->{
        //3.打印一下未确认的消息都有哪些
        String message = outstandingConfirms.get(deliveryTag);
        System.out.println("未确认的消息："+message+":::未确认的消息tag："+deliveryTag);
    };
    //准备消息的监听器 监听哪些消息成功了，哪些消息失败了
    /**
     * 1.监听哪些消息成功了
     * 2.监听哪些消息失败了
     */
    channel.addConfirmListener(ackCallback,nackCallback);//异步通知

    //开始时间
    long beginTime = System.currentTimeMillis();
    //批量发送消息
    for(int i=0;i<MESSAGE_COUNT;i++){
        String message="消息"+i;
        channel.basicPublish("",queueName,null,message.getBytes());
        //1.此处记录下所有要发送的消息 消息的总和
        outstandingConfirms.put(channel.getNextPublishSeqNo(),message);
    }
    //结束时间
    long endTime = System.currentTimeMillis();
    System.out.println("发布"+MESSAGE_COUNT+"个批量确认消息，耗时："+(endTime-beginTime)+"ms");
}
```

## 3. 以上 3 种发布确认速度对比 
- 单独发布消息

	同步等待确认，简单，但吞吐量非常有限。

- 批量发布消息

	批量同步等待确认，简单，合理的吞吐量，一旦出现问题但很难推断出是那条消息出现了问题。

- 异步处理

	最佳性能和资源使用，在出现错误的情况下可以很好地控制，但是实现起来稍微难些




# 五.RabbitMQ - 交换机


## 1. Exchanges
RabbitMQ 消息传递模型的核心思想是: **生产者生产的消息从不会直接发送到队列**。实际上，通常生产者甚至都不知道这些消息传递传递到了哪些队列中。

相反，**生产者只能将消息发送到交换机(exchange)**，交换机工作的内容非常简单，一方面它接收来自生产者的消息，另一方面将它们推入队列。交换机必须确切知道如何处理收到的消息。是应该把这些消息放到特定队列还是说把他们到许多队列中还是说应该丢弃它们。这就的由交换机的类型来决定。

![在这里插入图片描述](https://img-blog.csdnimg.cn/346a90953b964a0c968c12a1a7352014.png)

**Exchanges 的类型：**

​ 直接(direct), 主题(topic) ,标题(headers) , 扇出(fanout)

**无名exchange：**

​ 在前面部分我们对 exchange 一无所知，但仍然能够将消息发送到队列。之前能实现的 原因是因为我们使用的是默认交换，我们通过空字符串(“”)进行标识。

![在这里插入图片描述](https://img-blog.csdnimg.cn/754a496009524f8eab2ab9afdb90e594.png)

第一个参数是交换机的名称。空字符串表示默认或无名称交换机：消息能路由发送到队列中其实是由 routingKey(bindingkey)绑定 key 指定的，如果它存在的话

## 2. 临时队列
每当我们连接到 Rabbit 时，我们都需要一个全新的空队列，为此我们可以创建一个具有**随机名称的队列**，或者能让服务器为我们选择一个随机队列名称那就更好了。其次一旦我们断开了消费者的连接，队列将被自动删除。

创建临时队列的方式如下:
```java
String queueName = channel.queueDeclare().getQueue();
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/936396b2de87491ca8e388797e248a46.png)

## 3. 绑定 bindings
什么是 bingding 呢，binding 其实是 exchange 和 queue 之间的桥梁，它告诉我们 exchange 和那个队列进行了绑定关系。比如说下面这张图告诉我们的就是 X 与 Q1 和 Q2 进行了绑定

![在这里插入图片描述](https://img-blog.csdnimg.cn/6ad3eac579754356869b9cdf6db1efd5.png)

![在这里插入图片描述](https://img-blog.csdnimg.cn/1f8ab309167441c6a3d5823c1a801d20.png)
## 4. Fanout exchange
### 4.1 Fanout 介绍
Fanout 这种类型非常简单。正如从名称中猜到的那样，它是将接收到的所有消息广播到它知道的 所有队列中。系统中默认有些 exchange 类型

![在这里插入图片描述](https://img-blog.csdnimg.cn/6d860df3ad3c42f997f1713af9ed33ed.png)

### 4.2 Fanout 实战

![在这里插入图片描述](https://img-blog.csdnimg.cn/09aa26fbe94d404a860c15db8eb7f176.png)

Logs 和临时队列的绑定关系如下图

![在这里插入图片描述](https://img-blog.csdnimg.cn/ad1788113d50463bb3516e06bb588113.png)

为了说明这种模式，我们将构建一个简单的日志系统。它将由两个程序组成:第一个程序将发出日志消 息，第二个程序是消费者。其中我们会启动两个消费者，其中一个消费者接收到消息后把日志存储在磁盘

ReceiveLogs01 将接收到的消息打印在控制台
```java
package com.jm.rabbitmq.five;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

/**
 * 消息接收
 */
public class ReceiveLogs01 {
    //交换机的名称
    public static final String EXCHANGE_NAME="logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列的名称是随机的
         * 当消费者断开与队列的连接的时候，队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上...");

        //接收消息
        DeliverCallback deliverCallback= (consumerTag,message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息:"+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,consumerTag -> {});

    }
}
```
ReceiveLogs02 将接收到的消息打印在控制台
```java
package com.jm.rabbitmq.five;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * 消息接收
 */
public class ReceiveLogs02 {
    //交换机的名称
    public static final String EXCHANGE_NAME="logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME,"fanout");
        //声明一个队列 临时队列
        /**
         * 生成一个临时队列，队列的名称是随机的
         * 当消费者断开与队列的连接的时候，队列就自动删除
         */
        String queueName = channel.queueDeclare().getQueue();
        /**
         * 绑定交换机与队列
         */
        channel.queueBind(queueName,EXCHANGE_NAME,"");
        System.out.println("等待接收消息，把接收到的消息打印在屏幕上...");

        //接收消息
        DeliverCallback deliverCallback= (consumerTag,message)->{
            System.out.println("ReceiveLogs02控制台打印接收到的消息:"+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,consumerTag -> {});

    }
}
```

EmitLog 发送消息给两个消费者接收：
```java
package com.jm.rabbitmq.five;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;

import java.util.Scanner;

/**
 * 发消息
 */
public class EmitLog {
    //交换机的名称
    public static final String EXCHANGE_NAME="logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //channel.exchangeDeclare(EXCHANGE_NAME,"fanout");

        Scanner scanner=new Scanner(System.in);

        while(scanner.hasNext()){
            String message=scanner.next();
            channel.basicPublish(EXCHANGE_NAME,"",null,message.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+message);
        }

    }
}
```

## 5. Direct exchange
在上一节中，我们构建了一个简单的日志记录系统。我们能够向许多接收者广播日志消息。在本节我们将向其中添加一些特别的功能——让某个消费者订阅发布的部分消息。例如我们只把严重错误消息定向存储到日志文件(以节省磁盘空间)，同时仍然能够在控制台上打印所有日志消息。

我们再次来回顾一下什么是 bindings，绑定是交换机和队列之间的桥梁关系。也可以这么理解： **队列只对它绑定的交换机的消息感兴趣**。绑定用参数：routingKey 来表示也可称该参数为 binding key， 创建绑定我们用代码:`channel.queueBind(queueName, EXCHANGE_NAME, "routingKey");`

绑定之后的意义由其交换类型决定。

### 5.1 Direct 介绍

上一节中的我们的日志系统将所有消息广播给所有消费者，对此我们想做一些改变，例如我们希 望将日志消息写入磁盘的程序仅接收严重错误(errros)，而不存储哪些警告(warning)或信息(info)日志 消息避免浪费磁盘空间。Fanout 这种交换类型并不能给我们带来很大的灵活性-它只能进行无意识的 广播，在这里我们将使用 direct 这种类型来进行替换，这种类型的工作方式是，消息只去到它绑定的 routingKey 队列中去。

![在这里插入图片描述](https://img-blog.csdnimg.cn/c85bac9a4a134eb6840b62129815105f.png)

在上面这张图中，我们可以看到 X 绑定了两个队列，绑定类型是 direct。队列Q1 绑定键为 orange， 队列 Q2 绑定键有两个:一个绑定键为 black，另一个绑定键为 green.

在这种绑定情况下，生产者发布消息到 exchange 上，绑定键为 orange 的消息会被发布到队列 Q1。绑定键为 black和green的消息会被发布到队列 Q2，其他消息类型的消息将被丢弃。

### 5.2 多重绑定
![在这里插入图片描述](https://img-blog.csdnimg.cn/6d7780706ec44809bdbef153a808a597.png)
当然如果 exchange 的绑定类型是direct，**但是它绑定的多个队列的 key 如果都相同**，在这种情况下虽然绑定类型是 direct **但是它表现的就和 fanout 有点类似了**，就跟广播差不多，如上图所示。

### 5.3 Direct 实战
关系：

![在这里插入图片描述](https://img-blog.csdnimg.cn/fee242649e43431c8c1032b69640b405.png)

交换机：

![在这里插入图片描述](https://img-blog.csdnimg.cn/e03075402c7f4bb8b04dad54b0fb2f61.png)

c2：绑定disk，routingKey为error

c1：绑定console，routingKey为info、warning

消费者01
```java
package com.jm.rabbitmq.six;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class ReceiveLogsDirect01 {
    //交换机名称
    public static final String EXCHANGE_NAME="direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //声明一个队列
        channel.queueDeclare("console",false,false,false,null);

        //绑定队列和交换机(但routingKey各不相同)
        channel.queueBind("console",EXCHANGE_NAME,"info");
        channel.queueBind("console",EXCHANGE_NAME,"warning");

        //接收消息
        DeliverCallback deliverCallback= (consumerTag, message)->{
            System.out.println("ReceiveLogsDirect01控制台打印接收到的消息:"+new String(message.getBody(),"UTF-8"));
        };

        channel.basicConsume("console",true,deliverCallback, consumerTag -> {});

    }
}

```

消费者02
```java
package com.jm.rabbitmq.six;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

public class ReceiveLogsDirect02 {
    //交换机名称
    public static final String EXCHANGE_NAME="direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        //声明一个队列
        channel.queueDeclare("disk",false,false,false,null);

        //绑定队列和交换机
        channel.queueBind("disk",EXCHANGE_NAME,"error");


        //接收消息
        DeliverCallback deliverCallback= (consumerTag, message)->{
            System.out.println("ReceiveLogsDirect02控制台打印接收到的消息:"+new String(message.getBody(),"UTF-8"));
        };

        channel.basicConsume("disk",true,deliverCallback, consumerTag -> {});

    }
}
```

生产者
```java
package com.jm.rabbitmq.six;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.Channel;

import java.util.Scanner;

public class EmitLogDirect {
    //交换机的名称
    public static final String EXCHANGE_NAME="direct_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        Scanner scanner=new Scanner(System.in);
        while(scanner.hasNext()){
            String message=scanner.next();
            channel.basicPublish(EXCHANGE_NAME,"error",null,message.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+message);
        }
    }
}
```

## 6. Topics exchange


### 6.1 Topic 的介绍
尽管使用 direct 交换机改进了我们的系统，但是它仍然存在局限性——比方说我们想接收的日志类型有 info.base 和 info.advantage，某个队列只想 info.base 的消息，那这个时候direct 就办不到了。这个时候就只能使用 topic 类型

>Topic的要求

发送到类型是 topic 交换机的消息的 routing_key 不能随意写，必须满足一定的要求，它必须是**一个单词列表，以点号分隔开**。这些单词可以是任意单词

比如说："stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit".这种类型的。

当然这个单词列表最多不能超过 255 个字节。

在这个规则列表中，其中有两个替换符是大家需要注意的：

- *(星号)可以代替一个单词
- #(井号)可以替代零个或多个单词

### 6.2 Topic 匹配案例
下图绑定关系如下

![在这里插入图片描述](https://img-blog.csdnimg.cn/067b63516cf048e98fd89e91b2fe437b.png)

- Q1-->绑定的是

	中间带 orange 带 3 个单词的字符串 (*.orange.*)
- Q2-->绑定的是

	最后一个单词是 rabbit 的 3 个单词 (*.*.rabbit)
第一个单词是 lazy 的多个单词 (lazy.#)


注意：
- 当一个队列绑定键是#，那么这个队列将接收所有数据，就有点像 fanout 了
- 如果队列绑定键当中没有#和*出现，那么该队列绑定类型就是 direct 了

### 6.3 Topic 实战

![在这里插入图片描述](https://img-blog.csdnimg.cn/44211c8164b9491d81668bd5ae913b02.png)

代码如下：

生产者
```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;

import java.util.HashMap;
import java.util.Map;

public class EmitLogTopic {
    //交换机的名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        /**
         * Q1-->绑定的是
         * 中间带 orange 带 3 个单词的字符串(*.orange.*)
         * Q2-->绑定的是
         * 最后一个单词是 rabbit 的 3 个单词(*.*.rabbit)
         * 第一个单词是 lazy 的多个单词(lazy.#)
         */
        Map<String,String> bindingKeyMap=new HashMap<>();
        bindingKeyMap.put("quick.orange.rabbit","被队列 Q1Q2 接收到");
        bindingKeyMap.put("lazy.orange.elephant","被队列 Q1Q2 接收到");
        bindingKeyMap.put("quick.orange.fox","被队列 Q1 接收到");
        bindingKeyMap.put("lazy.brown.fox","被队列 Q2 接收到");
        bindingKeyMap.put("lazy.pink.rabbit","虽然满足两个绑定但只被队列 Q2 接收一次");
        bindingKeyMap.put("quick.brown.fox","不匹配任何绑定不会被任何队列接收到会被丢弃");
        bindingKeyMap.put("quick.orange.male.rabbit","是四个单词不匹配任何绑定会被丢弃");
        bindingKeyMap.put("lazy.orange.male.rabbit","是四个单词但匹配 Q2");

        for (Map.Entry<String, String> stringStringEntry : bindingKeyMap.entrySet()) {
            String routingKey=stringStringEntry.getKey();
            String message=stringStringEntry.getValue();
            channel.basicPublish(EXCHANGE_NAME,routingKey,null,message.getBytes("UTF-8"));
            System.out.println("生产者发出消息："+message);
        }
    }
}
```

消费者01
```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;


public class ReceiveLogsTopic01 {
    //交换机的名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //声明交换机和队列的绑定
        String queueName="Q1";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueBind(queueName,EXCHANGE_NAME,"*.orange.*");

        System.out.println("等待接收消息...");
       //接收消息
        DeliverCallback deliverCallback= (consumerTag, message)->{
            System.out.println("接收队列："+queueName+" 绑定键："+message.getEnvelope().getRoutingKey()+
                    " 消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,consumerTag -> {});
    }
}
```

消费者02

```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;


public class ReceiveLogsTopic02 {
    //交换机的名称
    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        //声明交换机和队列的绑定
        String queueName="Q2";
        channel.queueDeclare(queueName,false,false,false,null);
        channel.queueBind(queueName,EXCHANGE_NAME,"*.*.rabbit");
        channel.queueBind(queueName,EXCHANGE_NAME,"lazy.#");

        System.out.println("等待接收消息...");
       //接收消息
        DeliverCallback deliverCallback= (consumerTag, message)->{
            System.out.println("接收队列："+queueName+" 绑定键："+message.getEnvelope().getRoutingKey()+
                    " 消息："+new String(message.getBody(),"UTF-8"));
        };
        channel.basicConsume(queueName,true,deliverCallback,consumerTag -> {});
    }
}
```


# 六.RabbitMQ - 死信队列



## 1. 死信的概念
先从概念解释上搞清楚这个定义，死信，顾名思义就是无法被消费的消息，字面意思可以这样理 解，一般来说，producer 将消息投递到 broker 或者直接到queue 里了，consumer 从 queue 取出消息 进行消费，但某些时候由于特定的原因**导致 queue 中的某些消息无法被消费**，这样的消息如果没有后续的处理，就变成了死信，有死信自然就有了死信队列。

应用场景：为了保证订单业务的消息数据不丢失，需要使用到 RabbitMQ 的死信队列机制，当消息消费发生异常时，将消息投入死信队列中。还有比如说：用户在商城下单成功并点击去支付后在指定时间未支付时自动失效

## 2. 死信的来源
- 消息 TTL 过期

	TTL是Time To Live的缩写, 也就是生存时间

- 队列达到最大长度

	队列满了，无法再添加数据到 mq 中

- 消息被拒绝

	(basic.reject 或 basic.nack) 并且 requeue=false.

## 3. 死信实战

![在这里插入图片描述](https://img-blog.csdnimg.cn/708b8c6bb6f941fea71c32492529e691.png)

### 3.1 死信之TTl
消费者 C1 
```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列
 *
 * 消费者01
 */
public class Consumer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE="normal_exchange";
    //死信交换机的名称
    public static final String DEAD_EXCHANGE="dead_exchange";
    //普通队列的名称
    public static final String NORMAL_QUEUE="normal_queue";
    //死信队列的名称
    public static final String DEAD_QUEUE="dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明 普通和死信交换机 的类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        //声明 普通队列
        Map<String, Object> arguments=new HashMap<>();
        //正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //声明 死信队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);

        //绑定 普通交换机与普通队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        //绑定 死信交换机与死信队列
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息.....");

        DeliverCallback deliverCallback= (consumerTag,message)->{
            System.out.println("Consumer01接收的消息："+new String(message.getBody(),"UTF-8"));
        };

        //消费
        channel.basicConsume(NORMAL_QUEUE,false,deliverCallback,consumerTag -> {});
    }
}
```

生产者代码
```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

/**
 * 死信队列
 *
 * 生产者
 */
public class Producer {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE="normal_exchange";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //死信消息 设置TTL （time to live） 单位是ms  10s=10000ms
        AMQP.BasicProperties properties=
                new AMQP.BasicProperties()
                        .builder().expiration("10000").build();
        for (int i = 1; i < 11; i++) {
            String message="info"+i;
            channel.basicPublish(NORMAL_EXCHANGE,"zhangsan",properties,message.getBytes("UTF-8"));
        }


    }
}
```

启动 C1 ，之后关闭消费者，模拟其接收不到消息。再启动 Producer


![在这里插入图片描述](https://img-blog.csdnimg.cn/ba7a6b2a1b3845628a1eb23d7d31a2d1.png)

消费者 C2 代码：

以上步骤完成后，启动 C2 消费者，它消费死信队列里面的消息

```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列
 *
 * 消费者02
 */
public class Consumer02 {
    //死信队列的名称
    public static final String DEAD_QUEUE="dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();

        System.out.println("等待接收消息.....");

        DeliverCallback deliverCallback= (consumerTag,message)->{
            System.out.println("Consumer02接收的消息："+new String(message.getBody(),"UTF-8"));
        };

        //消费
        channel.basicConsume(DEAD_QUEUE,true,deliverCallback,consumerTag -> {});
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/9a7d475199214cf69b237d6531c3e089.png)

### 3.2 死信之最大长度
1、消息生产者代码去掉 TTL 属性

![在这里插入图片描述](https://img-blog.csdnimg.cn/353133e6c29b4cd1bfb43dd6f4093a9e.png)

2、C1 消费者修改以下代码(**启动之后关闭该消费者 模拟其接收不到消息**)

![在这里插入图片描述](https://img-blog.csdnimg.cn/4940d72b8cdd413d8094f8605804f238.png)
```java
//设置正常队列的长度限制，例如发10个，4个则为死信
params.put("x-max-length",6);
```

==注意此时需要把原先队列删除 因为参数改变了==

3、C2 消费者代码不变(启动 C2 消费者)

![在这里插入图片描述](https://img-blog.csdnimg.cn/a9b61c7acb724cc5aef8e90a8628f13b.png)


### 3.3 死信之消息被拒
1、消息生产者代码同上生产者一致

2、C1 消费者代码(启动之后关闭该消费者 模拟其接收不到消息)

拒收消息 "info5"
```java
package com.jm.rabbitmq.eight;

import com.jm.rabbitmq.util.RabbitMqUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列
 *
 * 消费者01
 */
public class Consumer01 {
    //普通交换机的名称
    public static final String NORMAL_EXCHANGE="normal_exchange";
    //死信交换机的名称
    public static final String DEAD_EXCHANGE="dead_exchange";
    //普通队列的名称
    public static final String NORMAL_QUEUE="normal_queue";
    //死信队列的名称
    public static final String DEAD_QUEUE="dead_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //声明 普通和死信交换机 的类型为direct
        channel.exchangeDeclare(NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);

        //声明 普通队列
        Map<String, Object> arguments=new HashMap<>();
        //正常队列设置死信交换机
        arguments.put("x-dead-letter-exchange",DEAD_EXCHANGE);
        //设置死信RoutingKey
        arguments.put("x-dead-letter-routing-key","lisi");
        channel.queueDeclare(NORMAL_QUEUE,false,false,false,arguments);
        //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        //声明 死信队列
        channel.queueDeclare(DEAD_QUEUE,false,false,false,null);

        //绑定 普通交换机与普通队列
        channel.queueBind(NORMAL_QUEUE,NORMAL_EXCHANGE,"zhangsan");
        //绑定 死信交换机与死信队列
        channel.queueBind(DEAD_QUEUE,DEAD_EXCHANGE,"lisi");

        System.out.println("等待接收消息.....");

        DeliverCallback deliverCallback= (consumerTag,message)->{
            String msg=new String(message.getBody(),"UTF-8");
            if("info5".equals(msg)){
                System.out.println("Consumer01接收的消息："+msg+"：此消息是被C1拒绝的");
                channel.basicReject(message.getEnvelope().getDeliveryTag(),false);
            }else{
                System.out.println("Consumer01接收的消息："+msg);
                channel.basicAck(message.getEnvelope().getDeliveryTag(),false);
            }
        };

        //消费
        channel.basicConsume(NORMAL_QUEUE,false,deliverCallback,consumerTag -> {});
    }
}
```

![在这里插入图片描述](https://img-blog.csdnimg.cn/e64d4ba6a3184880b96c3bab30c18104.png)

3、C2 消费者代码不变

启动消费者 1 然后再启动消费者 2

![在这里插入图片描述](https://img-blog.csdnimg.cn/37c8a4dc10404cf6a964299c8e5898bd.png)



# 七.RabbitMQ - 发布确认高级



在生产环境中由于一些不明原因，导致 RabbitMQ 重启，在 RabbitMQ 重启期间生产者消息投递失败， 导致消息丢失，需要手动处理和恢复。于是，我们开始思考，如何才能进行 RabbitMQ 的消息可靠投递呢？

## 1. 发布确认 springboot 版本

确认机制方案：


![在这里插入图片描述](https://img-blog.csdnimg.cn/e8d00ee5986440eabd447583f9bcbc47.png)


代码架构图：



![在这里插入图片描述](https://img-blog.csdnimg.cn/e69dc0bbc76b4e70be9fb14e24304745.png)


在配置文件当中需要添加

```xml
spring.rabbitmq.publisher-confirm-type=correlated
```

- `NONE` 值是禁用发布确认模式，是默认值

- `CORRELATED` 值是发布消息成功到交换器后会触发回调方法

- `SIMPLE` 值经测试有两种效果，其一效果和 CORRELATED 值一样会触发回调方法，其二在发布消息成功后使用 rabbitTemplate 调用 waitForConfirms 或 waitForConfirmsOrDie 方法等待 broker 节点返回发送结果，根据返回结果来判定下一步的逻辑，要**注意的点是 waitForConfirmsOrDie 方法如果返回 false 则会关闭 channel，则接下来无法发送消息到 broker**;

>代码

 1. 添加配置类：

```java
@Configuration
public class ConfirmConfig {
    public static final String CONFIRM_EXCHANGE_NAME = "confirm.exchange";
    public static final String CONFIRM_QUEUE_NAME = "confirm.queue";

    //声明业务 Exchange
    @Bean("confirmExchange")
    public DirectExchange confirmExchange() {
        return new DirectExchange(CONFIRM_EXCHANGE_NAME);
    }

    // 声明确认队列
    @Bean("confirmQueue")
    public Queue confirmQueue() {
        return QueueBuilder.durable(CONFIRM_QUEUE_NAME).build();
    }

    // 声明确认队列绑定关系
    @Bean
    public Binding queueBinding(@Qualifier("confirmQueue") Queue queue,
                                @Qualifier("confirmExchange") DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("key1");
    }
}

```

2. 消息生产者的回调接口

```java
package com.jm.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @PostConstruct 解释：
     *    @PostConstruct该注解被用来修饰一个非静态的void（）方法。
     *    被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     *    PostConstruct在构造函数之后执行，init（）方法之前执行。
     */
    @PostConstruct
    public void init(){
        //注入
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 交换机确认回调方法
     * 1.发消息 交换机接收成功了 回调
     *      1.1 correlationData 保存回调消息的id和相关信息
     *      1.2 ack=true 交换机接收到消息了
     *      1.3 cause null
     * 2.发消息 交换机接受失败了 回调
     *      2.1 correlationData 保存回调消息的id和相关信息
     *      2.2 ack=false 交换机没有收到消息了
     *      2.3 cause 失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId() != null ? correlationData.getId() : "";
        if(ack){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else{
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,cause);
        }
    }
}
```
3. 消息生产者

```java
@RestController
@RequestMapping("/confirm")
@Slf4j
public class ProducerController {
    public static final String CONFIRM_EXCHANGE_NAME = "confirm.exchange";
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private MyCallBack myCallBack;

    //依赖注入 rabbitTemplate 之后再设置它的回调对象
    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(myCallBack);
    }
    
    /**
     * 消息回调和退回
     *
     * @param message
     */
    @GetMapping("sendMessage/{message}")
    public void sendMessage(@PathVariable String message) {

        //指定消息 id 为 1
        CorrelationData correlationData1 = new CorrelationData("1");
        String routingKey = "key1";
        rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME, routingKey, message + routingKey, correlationData1);
        log.info(routingKey + "发送消息内容:{}", message + routingKey);

        CorrelationData correlationData2 = new CorrelationData("2");
        routingKey = "key2";
        rabbitTemplate.convertAndSend(CONFIRM_EXCHANGE_NAME, routingKey, message + routingKey, correlationData2);
        log.info(routingKey + "发送消息内容:{}", message + routingKey);

    }

}

```

4. 消息消费者

```java
package com.jm.rabbitmq.consumer;

import com.jm.rabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 发布确认高级 接收消息
 */
@Slf4j
@Component
public class Consumer {
    @RabbitListener(queues = ConfirmConfig.CONFIRM_QUEUE_NAME)
    public void receiveConfirmMessage(Message message){
        String msg = new String(message.getBody());
        log.info("接收到队列confirm.queue的消息：{}",msg);
    }
}

```

访问：[ http://localhost:8080/confirm/sendMessage/你好](http://localhost:8080/confirm/sendMessage/%E4%BD%A0%E5%A5%BD)


![在这里插入图片描述](https://img-blog.csdnimg.cn/632de4251dc24c3c9fdd4e20511fa061.png)

可以看到，发送了两条消息，第一条消息的 RoutingKey 为 "key1"，第二条消息的 RoutingKey 为 "key2"，两条消息都成功被交换机接收，也收到了交换机的确认回调，但消费者只收到了一条消息，因为**第二条消息的 RoutingKey 与队列的 BindingKey 不一致**，也没有其它队列能接收这个消息，所有**第二条消息被直接丢弃**了。

**丢弃的消息交换机是不知道的，需要解决告诉生产者消息传送失败**

## 2. 回退消息
Mandatory 参数

```java
rabbitTemplate.setReturnsCallback(myCallBack);
```

在仅开启了生产者确认机制的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息，如果发现该消息不可路由，那么消息会被直接丢弃，此时生产者是不知道消息被丢弃这个事件的。

那么如何让无法被路由的消息帮我想办法处理一下？最起码通知我一声，我好自己处理啊。通过设置 mandatory 参数可以在当消息传递过程中不可达目的地时将消息返回给生产者。


1. 修改配置

```xml
#消息退回
spring.rabbitmq.publisher-returns=true
```

2. 修改回调接口
```java
package com.jm.rabbitmq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class MyCallBack implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @PostConstruct 解释：
     *    @PostConstruct该注解被用来修饰一个非静态的void（）方法。
     *    被@PostConstruct修饰的方法会在服务器加载Servlet的时候运行，并且只会被服务器执行一次。
     *    PostConstruct在构造函数之后执行，init（）方法之前执行。
     */
    @PostConstruct
    public void init(){
        //注入
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    /**
     * 交换机确认回调方法
     * 1.发消息 交换机接收成功了 回调
     *      1.1 correlationData 保存回调消息的id和相关信息
     *      1.2 ack=true 交换机接收到消息了
     *      1.3 cause null
     * 2.发消息 交换机接受失败了 回调
     *      2.1 correlationData 保存回调消息的id和相关信息
     *      2.2 ack=false 交换机没有收到消息了
     *      2.3 cause 失败的原因
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId() != null ? correlationData.getId() : "";
        if(ack){
            log.info("交换机已经收到ID为：{}的消息",id);
        }else{
            log.info("交换机还未收到ID为：{}的消息，由于原因：{}",id,cause);
        }
    }

    //当消息无法路由的时候的回调方法
    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("消息{}，被交换机{}给回退了，退回的原因：{}，路由key：{}",
                new String(returnedMessage.getMessage().getBody()),
                returnedMessage.getExchange(),returnedMessage.getReplyText(),
                returnedMessage.getRoutingKey());
    }
}
```

低版本可能没有 `RabbitTemplate.ReturnsCallback` 请用 `RabbitTemplate.ReturnCallback`
```java
@Override
public void returnedMessage(Message message, int replyCode, String replyText, String
exchange, String routingKey) {
	log.info("消息:{}被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}",new String(message.getBody()),replyText, exchange, routingKey);
}
```

访问：[http://localhost:8080/confirm/sendMessage/你好](http://localhost:8080/confirm/sendMessage/%E4%BD%A0%E5%A5%BD)


结果分析：



![在这里插入图片描述](https://img-blog.csdnimg.cn/5b8f31a7dbb34873ade7b5abd7a8123e.png)

## 3. 备份交换机

有了 mandatory 参数和回退消息，我们获得了对无法投递消息的感知能力，有机会在生产者的消息无法被投递时发现并处理。但有时候，我们并不知道该如何处理这些无法路由的消息，最多打个日志，然后触发报警，再来手动处理。而通过日志来处理这些无法路由的消息是很不优雅的做法，特别是当生产者所在的服务有多台机器的时候，手动复制日志会更加麻烦而且容易出错。而且设置 mandatory 参数会增加生产者的复杂性，需要添加处理这些被退回的消息的逻辑。如果既不想丢失消息，又不想增加生产者的复杂性，该怎么做呢？

前面在设置死信队列的文章中，我们提到，可以为队列设置死信交换机来存储那些处理失败的消息，可是这些不可路由消息根本没有机会进入到队列，因此无法使用死信队列来保存消息。 在 RabbitMQ 中，有一种备份交换机的机制存在，可以很好的应对这个问题。

什么是备份交换机呢？备份交换机可以理解为 RabbitMQ 中交换机的“备胎”，当我们为某一个交换机声明一个对应的备份交换机时，就是为它创建一个备胎，当交换机接收到一条不可路由消息时，将会把这条消息转发到备份交换机中，由备份交换机来进行转发和处理，通常备份交换机的类型为 Fanout ，这样就能把所有消息都投递到与其绑定的队列中，然后我们在备份交换机下绑定一个队列，这样所有那些原交换机无法被路由的消息，就会都进 入这个队列了。当然，我们还可以建立一个报警队列，用独立的消费者来进行监测和报警。

- 代码架构图

![在这里插入图片描述](https://img-blog.csdnimg.cn/8e0b9b7bbba04f7bb7fc0dde53060853.png)

1. 修改配置类

```java
package com.jm.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类  发布确认（高级）
 */
@Configuration
public class ConfirmConfig {
    //交换机
    public static final String CONFIRM_EXCHANGE_NAME="confirm_exchange";
    //队列
    public static final String CONFIRM_QUEUE_NAME="confirm_queue";
    //routingKey
    public static final String CONFIRM_ROUTING_KEY="key1";
    //备份交换机
    public static final String BACKUP_EXCHANGE_NAME="backup_exchange";
    //备份队列
    public static final String BACKUP_QUEUE_NAME="backup_queue";
    //报警队列
    public static final String WARNING_QUEUE_NAME="warning_queue";

    //交换机声明
    @Bean("confirmExchange")
    public DirectExchange confirmExchange(){
        return ExchangeBuilder.directExchange(CONFIRM_EXCHANGE_NAME).durable(true)
                .withArgument("alternate-exchange",BACKUP_EXCHANGE_NAME).build();
    }

    ///队列声明
    @Bean("confirmQueue")
    public Queue confirmQueue(){
        return QueueBuilder.durable(CONFIRM_QUEUE_NAME).build();
    }

    //绑定
    @Bean
    public Binding queueBindingExchange(@Qualifier("confirmExchange") DirectExchange confirmExchange,
                                        @Qualifier("confirmQueue") Queue confirmQueue){
        return BindingBuilder.bind(confirmQueue).to(confirmExchange).with(CONFIRM_ROUTING_KEY);
    }

    //备份交换机
    @Bean("backupExchange")
    public FanoutExchange backupExchange(){
        return new FanoutExchange(BACKUP_EXCHANGE_NAME);
    }

    //备份队列
    @Bean("backupQueue")
    public Queue backupQueue(){
        return QueueBuilder.durable(BACKUP_QUEUE_NAME).build();
    }

    //报警队列
    @Bean("warningQueue")
    public Queue warningQueue(){
        return QueueBuilder.durable(WARNING_QUEUE_NAME).build();
    }

    //绑定
    @Bean
    public Binding backupQueueBindingBackupExchange(@Qualifier("backupExchange") FanoutExchange backupExchange,
                                        @Qualifier("backupQueue") Queue backupQueue){
        return BindingBuilder.bind(backupQueue).to(backupExchange);
    }
    @Bean
    public Binding warningQueueBindingBackupExchange(@Qualifier("backupExchange") FanoutExchange backupExchange,
                                                    @Qualifier("warningQueue") Queue warningQueue){
        return BindingBuilder.bind(warningQueue).to(backupExchange);
    }
}
```

2. 报警消费者
```java
package com.jm.rabbitmq.consumer;

import com.jm.rabbitmq.config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 报警消费者
 */
@Slf4j
@Component
public class WarningConsumer {
    //接受报警消息
    @RabbitListener(queues = ConfirmConfig.WARNING_QUEUE_NAME)
    public void receiveWarningMsg(Message message){
        String msg = new String(message.getBody());
        log.info("报警发现不可路由消息：{}",msg);
    }
}
```

之前已写过 `confirm.exchange` 交换机，由于更改配置，需要删掉，不然会报错


访问： [http://localhost:8080/confirm/sendMessage/你好](http://localhost:8080/confirm/sendMessage/%E4%BD%A0%E5%A5%BD)

![在这里插入图片描述](https://img-blog.csdnimg.cn/a8d778f3c9ab44c4bf051b87a13fcfba.png)

**mandatory 参数与备份交换机可以一起使用的时候**，如果两者同时开启，消息究竟何去何从？谁优先级高，经过上面结果显示答案是**备份交换机优先级高**。

# 八.RabbitMQ - 幂等性、优先级、惰性



## 1. 幂等性
>概念

用户对于同一操作发起的一次请求或者多次请求的结果是一致的，不会因为多次点击而产生了副作用。 举个最简单的例子，那就是支付，用户购买商品后支付，支付扣款成功，但是返回结果的时候网络异常， 此时钱已经扣了，用户再次点击按钮，此时会进行第二次扣款，返回结果成功，用户查询余额发现多扣钱 了，流水记录也变成了两条。在以前的单应用系统中，我们只需要把数据操作放入事务中即可，发生错误立即回滚，但是再响应客户端的时候也有可能出现网络中断或者异常等等


>消息重复消费

消费者在消费 MQ 中的消息时，MQ 已把消息发送给消费者，消费者在给 MQ 返回 ack 时网络中断， 故 MQ 未收到确认信息，该条消息会重新发给其他的消费者，或者在网络重连后再次发送给该消费者，但实际上该消费者已成功消费了该条消息，造成消费者消费了重复的消息。

>解决思路

MQ 消费者的幂等性的解决一般使用全局 ID 或者写个唯一标识比如时间戳 或者 UUID 或者订单消费者消费 MQ 中的消息也可利用 MQ 的该 id 来判断，或者可按自己的规则生成一个全局唯一 id，每次消费消息时用该 id 先判断该消息是否已消费过。


>消费端的幂等性保障

在海量订单生成的业务高峰期，生产端有可能就会重复发生了消息，这时候消费端就要实现幂等性， 这就意味着我们的消息永远不会被消费多次，即使我们收到了一样的消息。

业界主流的幂等性有两种操作:a. 唯一 ID+指纹码机制,利用数据库主键去重, b.利用 redis 的原子性去实现

- 唯一ID+指纹码机制

指纹码：我们的一些规则或者时间戳加别的服务给到的唯一信息码,它并不一定是我们系统生成的，基本都是由我们的业务规则拼接而来，但是一定要保证唯一性，然后就利用查询语句进行判断这个 id 是否存在数据库中，优势就是实现简单就一个拼接，然后查询判断是否重复；劣势就是在高并发时，如果是单个数据库就会有写入性能瓶颈当然也可以采用分库分表提升性能，但也不是我们最推荐的方式。

- note Redis 原子性

利用 redis 执行 setnx 命令，天然具有幂等性。从而实现不重复消费

## 2. 优先级队列

- **使用场景**

在我们系统中有一个订单催付的场景，我们的客户在天猫下的订单，淘宝会及时将订单推送给我们，如果在用户设定的时间内未付款那么就会给用户推送一条短信提醒，很简单的一个功能对吧。

但是，tmall 商家对我们来说，肯定是要分大客户和小客户的对吧，比如像苹果，小米这样大商家一年起码能给我们创造很大的利润，所以理应当然，他们的订单必须得到优先处理，而曾经我们的后端系统是使用 redis 来存放的定时轮询，大家都知道 redis 只能用 List 做一个简简单单的消息队列，并不能实现一个优先级的场景，所以订单量大了后采用 RabbitMQ 进行改造和优化，如果发现是大客户的订单给一个相对比较高的优先级， 否则就是默认优先级。

- **如何添加？**

1. 控制台页面添加

![在这里插入图片描述](https://img-blog.csdnimg.cn/c4c36316442f454ab60b3e8a0a68797f.png)

2. 队列中代码添加优先级
```java
Map<String, Object> params = new HashMap();
params.put("x-max-priority", 10);
channel.queueDeclare("hello", true, false, false, params);
```

3. 消息中代码添加优先级

```java
AMQP.BasicProperties properties = new AMQP.BasicProperties().builder().priority(10).build();
```


**注意事项：**


要让队列实现优先级需要做的事情有如下事情：队列需要设置为优先级队列，消息需要设置消息的优先级，消费者需要等待消息已经发送到队列中才去消费因为，这样才有机会对消息进行排序

>实战

生产者：

```java
package com.jm.rabbitmq.one;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 生产者 ：发消息
 */
public class Producer {
    //队列名称
    public static final String QUEUE_NAME="hello";

    //发消息
    public static void main(String[] args) throws Exception {
		Channel channel = RabbitMqUtils.getChannel();
		
        Map<String, Object> arguments=new HashMap<>();
        arguments.put("x-max-priority",10);//官方允许0-255 此处设置10 允许优先级范围为0-10 不要设置过大，浪费cpu和内存
        channel.queueDeclare(QUEUE_NAME,true,false,false,arguments);

        for (int i = 1; i < 11; i++) {
            String message="info"+i;
            if(i==5){
                AMQP.BasicProperties properties=
                        new AMQP.BasicProperties().builder().priority(5).build();
                channel.basicPublish("",QUEUE_NAME,properties,message.getBytes());
            }else{
                channel.basicPublish("",QUEUE_NAME,null,message.getBytes());
            }
        }
        
        System.out.println("消息发送完毕");
    }
}
```

消费者：

```java
package com.jm.rabbitmq.one;

import com.rabbitmq.client.*;

/**
 * 消费者 ：接收消息的
 */
public class Consumer {
    //队列的名称
    public static final String QUEUE_NAME="hello";

    //接收消息
    public static void main(String[] args) throws Exception{
		Channel channel = RabbitMqUtils.getChannel();

        //声明 接收消息
        DeliverCallback deliverCallback= (consumerTag,message) ->{
            System.out.println(new String(message.getBody()));
        };
        //取消消息时的回调
        CancelCallback cancelCallback= consumerTag ->{
            System.out.println("消息消费被中断");
        };

        channel.basicConsume(QUEUE_NAME,true,deliverCallback,cancelCallback);
    }
}

```

![在这里插入图片描述](https://img-blog.csdnimg.cn/5d810c1eb68249c9bba0b51140d476f2.png)


## 3. 惰性队列
- **使用场景**

RabbitMQ 从 3.6.0 版本开始引入了惰性队列的概念。惰性队列会尽可能的将消息存入磁盘中，而在消费者消费到相应的消息时才会被加载到内存中，它的一个重要的设计目标是能够支持更长的队列，即支持更多的消息存储。当消费者由于各种各样的原因(比如消费者下线、宕机亦或者是由于维护而关闭等)而致使长时间内不能消费消息造成堆积时，惰性队列就很有必要了。

默认情况下，当生产者将消息发送到 RabbitMQ 的时候，队列中的消息会尽可能的存储在内存之中， 这样可以更加快速的将消息发送给消费者。即使是持久化的消息，在被写入磁盘的同时也会在内存中驻留一份备份。当RabbitMQ 需要释放内存的时候，会将内存中的消息换页至磁盘中，这个操作会耗费较长的时间，也会阻塞队列的操作，进而无法接收新的消息。虽然 RabbitMQ 的开发者们一直在升级相关的算法， 但是效果始终不太理想，尤其是在消息量特别大的时候。

- **两种模式**


队列具备两种模式：default 和 lazy。默认的为default 模式，在3.6.0 之前的版本无需做任何变更。lazy 模式即为惰性队列的模式，可以通过调用 channel.queueDeclare 方法的时候在参数中设置，也可以通过 Policy 的方式设置，如果一个队列同时使用这两种方式设置的话，那么 Policy 的方式具备更高的优先级。 如果要通过声明的方式改变已有队列的模式的话，那么只能先删除队列，然后再重新声明一个新的。

在队列声明的时候可以通过“x-queue-mode”参数来设置队列的模式，取值为“default”和“lazy”。下面示例中演示了一个惰性队列的声明细节：

```java
Map<String, Object> args = new HashMap<String, Object>();
args.put("x-queue-mode", "lazy");
channel.queueDeclare("myqueue", false, false, false, args);
```


- **内存开销对比**

![在这里插入图片描述](https://img-blog.csdnimg.cn/08e9e9f51aa74fa29482c6e81ee749dd.png)

在发送 1 百万条消息，每条消息大概占 1KB 的情况下，普通队列占用内存是 1.2GB，而惰性队列仅仅 占用 1.5MB

