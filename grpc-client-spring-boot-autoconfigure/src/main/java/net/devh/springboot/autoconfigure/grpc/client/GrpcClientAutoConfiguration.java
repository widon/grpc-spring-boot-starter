/*
 * Copyright (c) 2016-2018 Michael Zhang <yidongnan@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.devh.springboot.autoconfigure.grpc.client;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.sleuth.autoconfig.TraceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.nepxion.discovery.plugin.framework.adapter.PluginAdapter;
import com.nepxion.discovery.plugin.strategy.context.StrategyContextHolder;
import com.nepxion.discovery.plugin.strategy.service.context.ServiceStrategyContextHolder;

import brave.Tracing;
import brave.grpc.GrpcTracing;
import io.grpc.LoadBalancer;
//import io.grpc.util.RoundRobinLoadBalancerFactory;

/**
 * The auto configuration used by Spring-Boot that contains all beans to create and inject grpc clients into beans.
 *
 * @author Michael (yidongnan@gmail.com)
 * @since 5/17/16
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnClass({GrpcChannelFactory.class})
@AutoConfigureAfter(name = {"org.springframework.cloud.client.CommonsClientAutoConfiguration"})
public class GrpcClientAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean
    public GrpcChannelsProperties grpcChannelsProperties() {
        return new GrpcChannelsProperties();
    }

    @Bean
    public GlobalClientInterceptorRegistry globalClientInterceptorRegistry() {
        return new GlobalClientInterceptorRegistry();
    }

    @Bean
    public AnnotationGlobalClientInterceptorConfigurer annotationGlobalClientInterceptorConfigurer() {
        return new AnnotationGlobalClientInterceptorConfigurer();
    }

    @ConditionalOnMissingBean
    @Bean
    @ConditionalOnBean(DiscoveryClient.class)
    public LoadBalancer.Factory grpcLoadBalancerFactory(
    		PluginAdapter pluginAdapter,
    		ServiceStrategyContextHolder serviceStrategyContextHolder) {
//        return RoundRobinLoadBalancerFactory.getInstance();
    	return new RoundRobinLoadBalancerFactory(pluginAdapter,serviceStrategyContextHolder);
    }

    @ConditionalOnMissingBean(value = GrpcChannelFactory.class,
            type = "org.springframework.cloud.client.discovery.DiscoveryClient")
    @Bean
    public GrpcChannelFactory addressChannelFactory(final GrpcChannelsProperties channels,
            final LoadBalancer.Factory loadBalancerFactory,
            final GlobalClientInterceptorRegistry globalClientInterceptorRegistry) {
        return new AddressChannelFactory(channels, loadBalancerFactory, globalClientInterceptorRegistry);
    }

    @Bean
    @ConditionalOnClass(GrpcClient.class)
    public GrpcClientBeanPostProcessor grpcClientBeanPostProcessor() {
        return new GrpcClientBeanPostProcessor();
    }

    @Configuration
    @ConditionalOnBean(DiscoveryClient.class)
    protected static class DiscoveryGrpcClientAutoConfiguration {

        @ConditionalOnMissingBean
        @Bean
        public GrpcChannelFactory discoveryClientChannelFactory(
                final GrpcChannelsProperties channels,
                final LoadBalancer.Factory loadBalancerFactory,
                final DiscoveryClient discoveryClient,
                final GlobalClientInterceptorRegistry globalClientInterceptorRegistry,
                 PluginAdapter pluginAdapter,
                 ServiceStrategyContextHolder serviceStrategyContextHolder) {
            return new DiscoveryClientChannelFactory(channels, loadBalancerFactory, discoveryClient,
                    globalClientInterceptorRegistry,pluginAdapter,serviceStrategyContextHolder);
        }
    }

    @Configuration
    @ConditionalOnProperty(value = "spring.sleuth.scheduled.enabled", matchIfMissing = true)
    @AutoConfigureAfter({TraceAutoConfiguration.class})
    @ConditionalOnClass(value = {Tracing.class, GrpcTracing.class, TraceAutoConfiguration.class})
    protected static class TraceClientAutoConfiguration {

        @Bean
        public GrpcTracing grpcTracing(final Tracing tracing) {
            return GrpcTracing.create(tracing);
        }

        @Bean
        public GlobalClientInterceptorConfigurer globalTraceClientInterceptorConfigurerAdapter(
                final GrpcTracing grpcTracing) {
            return registry -> registry.addClientInterceptors(grpcTracing.newClientInterceptor());
        }

    }

}
