package com.chen.blog.common.config;


import com.chen.blog.common.handler.PageableHandlerInterceptor;
import com.chen.blog.common.handler.WebSecurityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * web mvc配置
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebSecurityHandler getWebSecurityHandler() {
        return new WebSecurityHandler();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 纯 Header 鉴权后不再依赖浏览器自动携带 Cookie 凭证,allowCredentials 置 false 即可。
        // 配合 allowedOriginPatterns("*") 才符合 CORS 规范(true + "*" 会被浏览器拒绝);
        // Authorization Header 已包含在 allowedHeaders("*") 中,App / Web 端皆可正常送达。
        registry.addMapping("/**")
                .allowCredentials(false)
                .allowedHeaders("*")
                .allowedOriginPatterns("*")
                .allowedMethods("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PageableHandlerInterceptor());
        registry.addInterceptor(getWebSecurityHandler());
    }


}
