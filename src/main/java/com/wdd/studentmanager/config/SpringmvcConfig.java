package com.wdd.studentmanager.config;

import com.wdd.studentmanager.interceptors.LoginInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Classname SpringmvcConfig
 * @Description 扩展springmvc功能
 * @Date 2019/6/24 19:34
 * @Created by WDD
 */
@Component
public class SpringmvcConfig implements WebMvcConfigurer {
    //页面跳转
    //自己的配置，访问/跳转到login.html
    //Spring Boot的自动配置将“/”映射至index.html
    //自己的配置和Spring Boot的自动配置同时有效
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("/login");
        registry.addViewController("/").setViewName("system/login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");

    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).addPathPatterns("/**").excludePathPatterns("/hello","/system/login","/system/checkCode","/easyui/**","/h-ui/**","/upload/**");
    }
}
