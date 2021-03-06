

# netty启动源码分析

## 1.端口绑定bootstrap.bind->dobind()

**类AbstractBootstrap**

1）异步任务获取regFuture->regFuture.channel();

2）得到channel进行doBind0绑定操作，dobind0 异步执行**channel.bind**操作并添加关闭监听器

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

2）init(channel);初始化这个NioServerSocketChannel

3）regFuture = config().group().**register**(channel)异步注册channel

​    --3.1.传递调用：MultithreadEventLoopGroup选择next得到SingleThreadEventLoop，Single将channel包装成DefaultChannelPromise后继续调用**register**

​    --3.2.根据eventLoop.inEventLoop()判断进行同步或异步执行**register0**，第一次异步执行的话开启一个新线程

```java
 final ChannelFuture initAndRegister() {
        Channel channel = null;
        try {
             //--1.1通过nio 方法得到jdk的channel，目的让netty 包装JDK的channel
             //--1.2创建一个唯一channelId，创建一个NioMessageUnsafe，用于操作消息，创建一个DefaultChannelPipline管道
             //--1.3创建一个NioServerSocketChannelConfig对象，用于对外展示配置
            channel = channelFactory.newChannel();
            init(channel);
        } catch (Throwable t) {
           //、、、、、
        }
        ChannelFuture regFuture = config().group().register(channel);
        if (regFuture.cause() != null) {
            if (channel.isRegistered()) {
                channel.close();
            } else {
                channel.unsafe().closeForcibly();
            }
        }
        return regFuture;
```

```java
 //在类ServerBootstrap
/**
   --2.1抽象方法由ServerBootstrap实现
   --2.2设置tcp属性
   --2.3addLast 为channelPipline 添加handler
**/
@Override
    void init(Channel channel) {
        //设置serverrchannel属性
        setChannelOptions(channel, newOptionsArray(), logger);
        setAttributes(channel, attrs0().entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY));

        ChannelPipeline p = channel.pipeline();

        final EventLoopGroup currentChildGroup = childGroup;
        final ChannelHandler currentChildHandler = childHandler;
        final Entry<ChannelOption<?>, Object>[] currentChildOptions;
        //this对象中任何成员变量存在被多个线程共享，所以要加同步锁
        synchronized (childOptions) {
            currentChildOptions = childOptions.entrySet().toArray(EMPTY_OPTION_ARRAY);
        }
        final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(EMPTY_ATTRIBUTE_ARRAY);

        p.addLast(new ChannelInitializer<Channel>() {
            @Override
            //ChannelInitializer为入站handler，在handleradd中执行initChannel
            public void initChannel(final Channel ch) {
                final ChannelPipeline pipeline = ch.pipeline();
                ChannelHandler handler = config.handler();
                if (handler != null) {
                    pipeline.addLast(handler);
                }

                ch.eventLoop().execute(new Runnable() {
                    @Override
                    public void run() {
                        pipeline.addLast(new ServerBootstrapAcceptor(
                                ch, currentChildGroup, currentChildHandler, currentChildOptions, currentChildAttrs));
                    }
                });
            }
        });
    }
```



#### 1.1.1addLast

1）在DefaultChannelPipeline类中

2）检查handler是否符合标准

3）创建一个AbstractChannelHandlerContext对象，该对象关联了ChannelHandler和ChannelPipeline

4）添加context到链表

5）最后同步(在当前线程）或异步（非当前线程），或晚点异步（未完成注册）调用callHandlerAdded方法

#### 1.1.2register0

类AbstractChannel

1）在类AbstractNioChannel上的doRegister() 完成对jdk channel的注册到selector上，并注册0，即没有注册任何事件，注册accept事件会在

2） pipeline.fireChannelRegistered()方法内链式执行完fireChannelActive()后，才会Channel主动发起一个outbound的读事件，实际调用了Unsafe的beginRead方法，最后到了AbstractNioChannel来设置SelectionKey的interestOps属性，来最终把Channel绑定到Selector

```java
  
private void register0(ChannelPromise promise) {
            try {
                if (!promise.setUncancellable() || !ensureOpen(promise)) {
                    return;
                }
                boolean firstRegistration = neverRegistered;
                //完成jdk channel的注册到selector上
                doRegister();
                neverRegistered = false;
                registered = true;

               //确保在实际通知承诺之前调用handlerAdded(…)。这是必要的
                //用户可能已经通过ChannelFutureListener中的管道触发事件。
                 //唤醒执行各个入站handler中add的实现方法任务
                pipeline.invokeHandlerAddedIfNeeded();

                safeSetSuccess(promise);
                //链式触发handler中channelRegistered实现方法
                pipeline.fireChannelRegistered();
                // Only fire a channelActive if the channel has never been registered. This prevents firing
                // multiple channel actives if the channel is deregistered and re-registered.
                if (isActive()) {
                    if (firstRegistration) {
                        pipeline.fireChannelActive();
                    } else if (config().isAutoRead()) {
                        // This channel was registered before and autoRead() is set. This means we need to begin read
                        // again so that we process inbound data.
                        //
                        // See https://github.com/netty/netty/issues/4805
                        beginRead();
                    }
                }
            } catch (Throwable t) {
                // Close the channel directly to avoid FD leak.
                closeForcibly();
                closeFuture.setClosed();
                safeSetFailure(promise, t);
            }
        }
```



### 1.2doBind0->dobind

```java
 /*
 该类在AbstractChannelHandlerContext
1）从后往前遍历寻找第一个出站且有MASK_BIND标志的handler
2）按顺序链式同步或异步执行hander中的bind方法（可重写自定义）
3）执行到head时调用DefaultChannelPipeline->unsafe.bind->dobind才真正实现对jdk中channel进行端口绑定
4）完成绑定后晚点异步执行handler中active实现方法
*/
@Override
    public ChannelFuture bind(final SocketAddress localAddress, final ChannelPromise promise) {
        ObjectUtil.checkNotNull(localAddress, "localAddress");
        if (isNotValidPromise(promise, false)) {
            // cancelled
            return promise;
        }

        final AbstractChannelHandlerContext next = findContextOutbound(MASK_BIND);
        EventExecutor executor = next.executor();
        if (executor.inEventLoop()) {
            next.invokeBind(localAddress, promise);
        } else {
            safeExecute(executor, new Runnable() {
                @Override
                public void run() {
                    next.invokeBind(localAddress, promise);
                }
            }, promise, null, false);
        }
        return promise;
    }
```

# 接收连接请求过程

总体流程：在boss线程上的select监听请求事件->执行processSelectKeys->接收连接--》创建一个新的NioSocketChannel--》注册到work EventLoop上----》在该线程执行JDK 的selector Read注册事件

1）服务器轮询Accept事件，获取事件后调用unsafe.read方法，该方法为ServerSocket内部类，该方法由2部分组成

2）doReadMessage用于创建NioSocketChannel对象

3）随后执行pipeline.fireChannelRead方法，并将自己注册到一个新的workerGroup中的一个EventLoop，并且注册一个0，表示注册成功，但没有注册读（1）事件

## unsafe.read

```java
//检查是否当前线程
//执行doReadMessages(readBuf),该变量为list容器，该方式是读取boss线程中NioSeverSocketChannel接收到请求，new 一个nio封装的NioSocketChannel添加到容器内
//循环遍历readBuf容器，传递每个NioSocketChannel链式执行serverSocket中pipeline中channelread实现方法
@Override
        public void read() {
            assert eventLoop().inEventLoop();
            final ChannelConfig config = config();
            final ChannelPipeline pipeline = pipeline();
            final RecvByteBufAllocator.Handle allocHandle = unsafe().recvBufAllocHandle();
            allocHandle.reset(config);

            boolean closed = false;
            Throwable exception = null;
            try {
                try {
                    do {
                        int localRead = doReadMessages(readBuf);
                        if (localRead == 0) {
                            break;
                        }
                        if (localRead < 0) {
                            closed = true;
                            break;
                        }

                        allocHandle.incMessagesRead(localRead);
                    } while (allocHandle.continueReading());
                } catch (Throwable t) {
                    exception = t;
                }

                int size = readBuf.size();
                for (int i = 0; i < size; i ++) {
                    readPending = false;
                    pipeline.fireChannelRead(readBuf.get(i));
                }
                readBuf.clear();
                allocHandle.readComplete();
                pipeline.fireChannelReadComplete();

                if (exception != null) {
                    closed = closeOnReadError(exception);

                    pipeline.fireExceptionCaught(exception);
                }

                if (closed) {
                    inputShutdown = true;
                    if (isOpen()) {
                        close(voidPromise());
                    }
                }
            } finally {
                // Check if there is a readPending which was not processed yet.
                // This could be for two reasons:
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelRead(...) method
                // * The user called Channel.read() or ChannelHandlerContext.read() in channelReadComplete(...) method
                //
                // See https://github.com/netty/netty/issues/2254
                if (!readPending && !config.isAutoRead()) {
                    removeReadOp();
                }
            }
        }
    }
```

## ServerBootstrapAcceptor

将NioSocketChannel注册到worker线程

```java
//msg为NioSocketChannel强转
//管道添加处理器
//设置channel属性
//用worker线程注册socketChannel
public void channelRead(ChannelHandlerContext ctx, Object msg) {
            final Channel child = (Channel) msg;

            child.pipeline().addLast(childHandler);

            setChannelOptions(child, childOptions, logger);
            setAttributes(child, childAttrs);

            try {
                childGroup.register(child).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            forceClose(child, future.cause());
                        }
                    }
                });
            } catch (Throwable t) {
                forceClose(child, t);
            }
        }
```

