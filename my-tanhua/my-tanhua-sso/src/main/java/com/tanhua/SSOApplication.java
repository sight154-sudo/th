package com.tanhua;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;

/**
 * @author: tang
 * @date: Create in 18:52 2021/8/5
 * @description:
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@MapperScan(basePackages = "com.tanhua.common.mapper")
public class SSOApplication {

    public static void main(String[] args) {
        SpringApplication.run(SSOApplication.class,args);
    }
}