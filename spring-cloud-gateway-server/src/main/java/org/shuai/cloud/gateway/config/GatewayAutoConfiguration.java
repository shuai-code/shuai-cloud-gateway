package org.shuai.cloud.gateway.config;

import org.shuai.cloud.gateway.support.StringToZonedDateTimeConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration标记此类是一个配置类, proxyBeanMethods设置为false表示不开启代理模式, 类内的每个bean都是非单例的
 * @author Yangs
 */
@Configuration(proxyBeanMethods = false)
public class GatewayAutoConfiguration {

    /**
     * 时间转换工具类, 字符串转DateTime格式
     * 有可能使用在时间规则的路由判断
     */
    @Bean
    public StringToZonedDateTimeConverter stringToZonedDateTimeConverter() {
        return new StringToZonedDateTimeConverter();
    }
}
