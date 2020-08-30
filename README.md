# RPC的基本概念

## PRC调用原理

![PRC原理](C:\Users\FangKanghua\Documents\fangkanghua-master\春招笔记\图片\PRC原理.png)



一次完整的RPC调用流程（同步调用）如下：  

1）服务消费方（client）调用以本地调用方式调用服务；

2）client stub接收到调用后负责将方法、参数等组装成能够进行网络传输的消息体； 

3）client stub找到服务地址，并将消息发送到服务端；

4）server stub收到消息后进行解码；

5）server stub根据解码结果调用本地的服务； 

6）本地服务执行并将结果返回给server  stub； 

7）server stub将返回结果打包成消息并发送至消费方； 

8）client stub接收到消息，并进行解码； 

9）服务消费方得到最终结果。

RPC框架的目标就是要2~8这些步骤都封装起来，这些细节对用户来说是透明的，不可见的。  

## PRC调用与HTTP调用的区别

PRC自定义协议，直接使用TCP传输，HTTP是应用层协议，报文中包括了很多冗余信息，效率较低

PRC调用与本地调用基本无差别，由RPC框架封装通信细节，开发简单

# Dubbo的基本原理

## Dubbo架构图

![Dubbo原理](C:\Users\FangKanghua\Documents\fangkanghua-master\春招笔记\图片\Dubbo原理.jpg)





## Dubbo调用的基本流程

1. client一个线程调用远程接口，生成一个唯一的ID（比如一段随机字符串，UUID等），Dubbo是使用AtomicLong从0开始累计数字的
2. 将打包的方法调用信息（如调用的接口名称，方法名称，参数值列表等），和处理结果的回调对象callback，全部封装在一起，组成一个对象object
3. 向专门存放调用信息的全局ConcurrentHashMap里面put(ID, object)
4. 将ID和打包的方法调用信息封装成一对象connRequest，使用IoSession.write(connRequest)异步发送出去
5. 当前线程再使用callback的get()方法试图获取远程返回的结果，在get()内部，则使用synchronized获取回调对象callback的锁， 再先检测是否已经获取到结果，如果没有，然后调用callback的wait()方法，释放callback上的锁，让当前线程处于等待状态。
6. 服务端接收到请求并处理后，将结果（此结果中包含了前面的ID，即回传）发送给客户端，客户端socket连接上专门监听消息的线程收到消息，分析结果，取到ID，再从前面的ConcurrentHashMap里面get(ID)，从而找到callback，将方法调用结果设置到callback对象里。
7. 监听线程接着使用synchronized获取回调对象callback的锁（因为前面调用过wait()，那个线程已释放callback的锁了），再notifyAll()，唤醒前面处于等待状态的线程继续执行（callback的get()方法继续执行就能拿到调用结果了），至此，整个过程结束。

# 我的RPC实现

## 我的RPC架构

支持的功能：

服务注册

负载均衡

同步调用和异步调用

心跳机制

### 服务注册

支持Zookeeper的服务注册

注册中心的数据结构：

```
/RPC/SERVICE/{service_name}/PROVIDERS/{host:port}
```



### 负载均衡



### 同步和异步调用

**同步调用（客户端逻辑）：**

1. 客户端线程通过getService获取远程代理对象，该代理对象是通过JDK动态代理获取的，代理对象中注册了请求远程服务的handler，且该代理对象是目标接口的一个实现类
2. 客户端通过代理类调用目标服务的方法，传递相应的参数，上一步中注册的handler接收代理请求，开始发起远程调用
3. handler中的invoke方法构造请求体（UUID、调用的接口名称、方法名称、参数值列表），如果支持异步调用，再封装一个callback对象到请求体中
4. 将键值对（UUID->requestEntity）存入专门存放调用信息的全局concurrenthashMap
5. 调用send方法，send方法首先根据负载均衡策略选择一个channel，再把调用信息交给该channel发送出去，之后自己调用wait方法阻塞，等待响应数据；同时启动一个计时器，以防止超时情况下线程被长期阻塞饿死
6. Netty线程在收到响应数据后，到concurrentHashMap中根据UUID找到对应的请求信息，调用signal方法
7. send方法被唤醒，返回调用结果；若在超时时间之内没有返回，计时器到期后也会调用signal方法，此时send获取到的调用结果为null

**异步调用（客户端逻辑）：**

### 心跳机制



## 遇到的问题

### 容错处理

原来的设计：对于任何请求，默认重试3次

存在的问题：对于非幂等接口，重试操作可能导致数据不一致的情况

改进方式：应允许在方法级别自定义重试策略

参考实现：Dubbo的容错机制

默认 Failover Cluster，失败自动切换，重试其他服务器，可配置重试次数，通常用于读操作

Failfast Cluster：快速失败，只发起一次调用，失败立即报错，用于非幂等性的写操作

Forking Cluster：并行调用多个服务，通常用于实时性要求较高的场景



### 连接的初始化

原来的设计：使用的是lazy load的方式。对某一个接口调用时，会根据负载均衡策略从ip列表中选择一个。如果调用时还没有和该IP创建连接，就先创建连接，再发送。

存在的问题：容易造成系统启动后第一次调用超时，一个服务存在多个副本时，可能造成开始的连续几次请求都超时

改进方式：系统初始化时，从注册中心获取IP地址后，首先创建连接，避免懒加载的模式



### 服务端如何返回响应消息

原来的设计：netty服务端线程接收到调用信息后，直接解码、反射调用目标方法，得到结果后再编码，发送出去

存在的问题：netty本质上是一个网络通信框架，调用方法获取结果的动作不应该由netty负责，再netty线程中执行方法调用，会阻塞该线程较长的时间，使其无法接收新的调用请求，并发量大的情况下会大大降低吞吐量

改进方式：netty接收到调用请求后，交给server-stub的线程池，由线程池来调用方法，netty线程可以立即结束本次任务，等待接收下一次调用。server-stub接收后，主动通过netty发送响应消息。（注意：server-stub应维护一个requestID->channel的映射，从而将响应消息传输给正确的客户端）



### 心跳设计

