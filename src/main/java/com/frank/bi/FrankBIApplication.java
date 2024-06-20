package com.frank.bi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Frank
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@MapperScan("com.frank.bi.mapper")
@ComponentScan("com.frank")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class FrankBIApplication {

    public static void main(String[] args) {
        SpringApplication.run(FrankBIApplication.class, args);
    }

}
