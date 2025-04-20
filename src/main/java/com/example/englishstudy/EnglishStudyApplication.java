package com.example.englishstudy;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.englishstudy.mapper")
@EnableAspectJAutoProxy(exposeProxy = true) // 开启暴露代理对象
@EnableAsync
public class EnglishStudyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EnglishStudyApplication.class, args);
    }

}
