package com.atguigu.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {

            @Override
            public void apply(RequestTemplate requestTemplate) {
                System.out.println("feign远程之前先进行RequestTemplate.apply");
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    //老请求
                    HttpServletRequest request = attributes.getRequest();
                    if (request != null) {
                        String cookie = request.getHeader("Cookie");
                        // 新请求同步了老请求的cookie
                        requestTemplate.header("Cookie", cookie);
                    }
                }
            }
        };
    }
}
