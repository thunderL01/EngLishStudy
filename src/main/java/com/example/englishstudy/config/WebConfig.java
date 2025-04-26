package com.example.englishstudy.config;

import com.example.englishstudy.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 将 JwtInterceptor 注册为一个 Bean
    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/project_uploads/");
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除登录接口，因为登录接口不需要验证 JWT
                .excludePathPatterns("/api/user/register/wechat")
                .excludePathPatterns("/api/user/avatar")
                .excludePathPatterns("/uploads/**")
                .excludePathPatterns("/api/AI/chat"); // 排除头像资源路径
    }


}