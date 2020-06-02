package com.nettyRpc.client.nettyClientScan;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by SUXH.
 *
 * 高级用法，模拟 FeignClientsRegistrar 注册方式
 *      注册@NettyRpcClient修饰bean
 */
public class NettyRpcClientRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {


    private ClassLoader classLoader;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * <b>概要：</b>:
     *      注册@NettyRpcClient对应UserService到容器
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/6/2 16:01 </br>
     * @param importingClassMetadata 对应注解下的所有注解信息
     * @param registry Bean定义的注册表
     * @return:
     */
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        ClassPathScanningCandidateComponentProvider scan = getScanner();

        //1.指定扫描NettyRpcClient注解，类似于Feign注解
        scan.addIncludeFilter(new AnnotationTypeFilter(NettyRpcClient.class));

        //2.获取@NettyRpcClient注解的BeanDefinition集合
        Set<BeanDefinition> candidateComponents = new HashSet<>();
        for (String basePackage : getNettyAppScanPackages(importingClassMetadata)) {
            candidateComponents.addAll(scan.findCandidateComponents(basePackage));
        }
        candidateComponents.stream().forEach(beanDefinition -> {
            if (!registry.containsBeanDefinition(beanDefinition.getBeanClassName())) {
                if (beanDefinition instanceof AnnotatedBeanDefinition) {
                    AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
                    //1.注解元数据信息
                    AnnotationMetadata annotationMetadata = annotatedBeanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "@NettyRpcClient can only be specified on an interface");
                    //2.获取@NettyRpcClient注解属性
                    Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(NettyRpcClient.class.getCanonicalName());

                    //3.动态注册由@NettyRpcClient注解的类到Spring容器
                    this.registerNettyRpcClient(registry, annotationMetadata,attributes);
            }
            }
        });
    }

    /**
     * <b>概要：</b>:
     *      动态注册由@NettyRpcClient注解的类到NettyRpcClientFactoryBean去创建代理bean
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/4/28 17:59 </br>
     * @param registry BeanDefinition注册容器
     * @param annotationMetadata 注解类（如：@NettyRpcClient）
     * @param attributes 注解属性（如：@NettyRpcClient注解的属性）
     * @return:
     */
    private void registerNettyRpcClient(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
        //获取注解对应的类（如：@NettyRpcClient 下的UserService）
        String className = annotationMetadata.getClassName();
        //设置BeanDefinition type为UserService 注册UserService实例到spring bean容器

        //将NettyRpcClientFactoryBean注册到BeanDefinition
        BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(NettyRpcClientFactoryBean.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        definition.addPropertyValue("type", className);
        String name = attributes.get("name") == null ? "" :(String)(attributes.get("name"));
        String alias = name + "NettyRpcClient";
        AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
        beanDefinition.setPrimary(true);
        BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, className,
                new String[] { alias });
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
    }



    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                if (beanDefinition.getMetadata().isIndependent()) {
                    // 判断接口是否继承了 Annotation注解
                    if (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata()
                            .getInterfaceNames().length == 1 && Annotation.class.getName().equals(beanDefinition.getMetadata().getInterfaceNames()[0])) {
                        try {
                            Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(),
                                    NettyRpcClientRegistrar.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error(
                                    "Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);
                        }
                    }
                    return true;
                }
                return false;

            }
        };
    }


    /**
     * <b>概要：</b>:
     *      获取@EnableNettyRpcClient basePackages和basePackageClasses属性配置
     * <b>作者：</b>SUXH</br>
     * <b>日期：</b>2020/6/2 15:44 </br>
     * @param importingClassMetadata 启动类的注解元数据信息
     * @return @EnableNettyRpcClient扫描的包
     */
    protected Set<String> getNettyAppScanPackages(AnnotationMetadata importingClassMetadata) {
       //1.获取EnableNettyRpcClient注解的属性
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableNettyRpcClient.class.getCanonicalName());

        //2.获取basePackages属性值
        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }
}
