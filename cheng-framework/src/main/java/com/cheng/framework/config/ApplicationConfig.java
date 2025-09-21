package com.cheng.framework.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.TimeZone;

/**
 * 程序註解配置
 * <p>
 * 表示通過aop框架暴露該代理物件 ,AopContext能夠訪問
 * 指定要掃描的Mapper類的包的路徑
 *
 * @author cheng
 */
@Configuration
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.cheng.**.mapper")
public class ApplicationConfig {
    /**
     * 時區配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());
    }
}
