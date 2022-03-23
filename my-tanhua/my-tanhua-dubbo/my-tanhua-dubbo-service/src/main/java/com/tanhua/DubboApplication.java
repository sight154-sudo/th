package com.tanhua;

import com.alibaba.dubbo.config.spring.context.annotation.EnableDubbo;
import com.aliyun.oss.OSSClient;
import com.tanhua.common.config.AliyunOssConfig;
import com.tanhua.common.service.PicUploadService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(
        type= FilterType.ASSIGNABLE_TYPE,classes = {AliyunOssConfig.class, OSSClient.class, PicUploadService.class}
))
@EnableDubbo
//开启异步执行
@EnableAsync
public class DubboApplication {

    public static void main(String[] args) {
        SpringApplication.run(DubboApplication.class, args);
    }
}