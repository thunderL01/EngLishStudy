package com.example.englishstudy.utils;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {

    // 获取 ApplicationContext
    @Getter
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextUtil.applicationContext == null) {
            SpringContextUtil.applicationContext = applicationContext;
        }
    }

    // 通过名称获取 Bean
    public static Object getBean(String name) {
        return getApplicationContext().getBean(name);
    }

    // 通过类型获取 Bean
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    // 通过名称和类型获取 Bean
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }
}