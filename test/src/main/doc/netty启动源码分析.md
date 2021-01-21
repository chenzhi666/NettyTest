# netty启动源码分析

## 1.端口绑定bootstrap.bind->dobind()

**类AbstractBootstrap**

1）异步任务获取regFuture->regFuture.channel();

2）得到channel进行doBind0绑定操作

​       --2.1.dobind0 异步执行channel.bind操作

​       --2.2.调用NioServerSocketChannel的doBind对JDK 的channel和端口绑定

​       --2.3.fireChannelActive执行pinpline中的hander中active方法

```java
 private ChannelFuture doBind(final SocketAddress localAddress) {
        //异步任务
        final ChannelFuture regFuture = initAndRegister();
        //异步任务结果获取channel对象
        final Channel channel = regFuture.channel();
        if (regFuture.cause() != null) {
            return regFuture;
        }
      if (regFuture.isDone()) {
            //注册成功进行绑定操作
            ChannelPromise promise = channel.newPromise();
            doBind0(regFuture, channel, localAddress, promise);
            return promise;
        } else {
            // 确保注册已完成后进行的操作，添加监听器监听，注册完成就进行绑定操作
            final PendingRegistrationPromise promise = new PendingRegistrationPromise(channel);
            regFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    Throwable cause = future.cause();
                    if (cause != null) {
                        promise.setFailure(cause);
                    } else {
                        promise.registered();
                        doBind0(regFuture, channel, localAddress, promise);
                    }
                }
            });
            return promise;
        }
 }
```

### 1.1.initAndRegister()

**类AbstractBootstrap**

1）channelFactory.newChannel()反射工厂创建channnel

​    --1.1通过nio 方法得到jdk的channel，目的让netty 包装JDK的channel

​    --1.2创建一个唯一channelId，创建一个NioMessageUnsafe，用于操作消息，创建一个DefaultChannelPipline管道

​    --1.3创建一个NioServerSocketChannelConfig对象，用于对外展示配置

2）init(channel);初始化这个NioServerSocketChannel

​    --2.1抽象方法由ServerBootstrap实现

​    --2.2设置tcp属性

​    --2.3addLast 为channelPipline 添加handler

3）regFuture = config().group().**register**(channel)异步注册channel

​    --3.1.传递调用：MultithreadEventLoopGroup选择next得到SingleThreadEventLoop，Single将channel包装成DefaultChannelPromise后继续调用**register**

​    --3.2.根据eventLoop.inEventLoop()判断进行同步或异步执行**register0**，第一次异步执行的话开启一个新线程

#### 1.1.1register0

类AbstractChannel

1）在类AbstractNioChannel上的doRegister() 完成对jdk channel的注册到selector上获得selectionKey后终止循环

2）

