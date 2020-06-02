1.本应用实现的目标是：
    在spring boot UserController中实现service rpc调用（结合netty实现）
    
2.应用分为rpc客户端和netty服务端：
    Ⅰ.rpc客户端spring boot过程讲解(端口9393)：
        ①入口NettyRpcSpringBootApplication注解@EnableNettyRpcClient（类似@EnableScheduling）
        ②EnableNettyRpcClient有两个属性及@Import(NettyRpcClientRegistrar.class)
        ③NettyRpcClientRegistrar方法registerBeanDefinitions  注册@NettyRpcClient注解的UserService到spring容器
        ④NettyRpcClientFactoryBean：使用jdk代理UserService实现
        ⑤在NettyRpcInvocationHandler中实现jdk代理实现并编码rpc客户端
    
    Ⅱ.netty服务端：
        ①NettyRpcServer：启动netty服务端；
        ②NettyRpcServerHandler：根据请求方法调用具体实现类