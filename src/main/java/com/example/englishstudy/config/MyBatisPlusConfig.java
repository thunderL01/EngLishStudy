package com.example.englishstudy.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类，用于配置 MyBatis-Plus 的相关插件和设置
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器，主要用于添加分页插件
     *
     * @return MyBatis-Plus 拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 创建 MyBatis-Plus 拦截器实例，用于管理多个内部拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 创建分页插件拦截器，指定数据库类型为 MySQL，确保分页功能在 MySQL 数据库中正常工作
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 将分页插件拦截器添加到 MyBatis-Plus 拦截器中，使其生效
        interceptor.addInnerInterceptor(paginationInnerInterceptor);
        return interceptor;
    }



}