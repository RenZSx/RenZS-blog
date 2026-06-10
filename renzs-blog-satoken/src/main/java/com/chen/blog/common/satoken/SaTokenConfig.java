package com.chen.blog.common.satoken;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * sa-token 拦截器配置
 * <p>
 * 替代原 Spring Security 的过滤器链与 AccessDecisionManagerImpl,
 * 负责对全部 HTTP 请求进行动态 URL 鉴权,并放行登录登出、错误页与 Swagger 相关路径。
 * <p>
 * 鉴权流程:
 * <ol>
 *   <li>从 {@link ResourceRoleCache} 查询当前 URL+Method 所需角色</li>
 *   <li>未在 tb_resource 中配置(返回 null) → 默认放行,与原
 *       FilterInvocationSecurityMetadataSourceImpl 行为一致</li>
 *   <li>已配置但所需角色列表为空 → 抛出 {@link NotPermissionException},
 *       等价于原行为返回 "disable" 角色任何用户均无法满足</li>
 *   <li>否则先校验登录,再校验用户角色与所需角色是否存在交集</li>
 * </ol>
 *
 * @author chen
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 资源-角色映射缓存,提供 URL+Method 到所需角色列表的查询
     */
    @Autowired
    private ResourceRoleCache resourceRoleCache;

    /**
     * 注册 sa-token 全局拦截器
     * <p>
     * 拦截全部路径,但排除登录登出、错误页与 Swagger 相关路径。
     * 登录登出由 LoginController 自行处理,无需经过鉴权拦截器。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            String url = SaHolder.getRequest().getRequestPath();
            String method = SaHolder.getRequest().getMethod();

            List<String> requiredRoles = resourceRoleCache.matchRequiredRoles(url, method);

            // 未在 tb_resource 中配置 → 默认放行(与原 FilterInvocationSecurityMetadataSourceImpl 行为一致)
            if (requiredRoles == null) {
                return;
            }

            // 配置过的资源一律先校验登录,确保未登录访问返回 NO_LOGIN(40001) 而不是 AUTHORIZED(40300),
            // 语义比"empty roleList 直接抛 NotPermissionException"更准确
            StpUtil.checkLogin();

            // 已配置但 roleList 为空 → 与原行为一致:返回 "disable" 角色,任何用户都无法满足,等价拒绝
            if (requiredRoles.isEmpty()) {
                throw new NotPermissionException("disable");
            }

            // 校验角色:用户角色与所需角色有交集即可
            List<String> userRoles = StpUtil.getRoleList();
            if (Collections.disjoint(userRoles, requiredRoles)) {
                throw new NotPermissionException(String.join(",", requiredRoles));
            }
        })).addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/logout",
                        "/register",
                        "/users/code",
                        "/users/oauth/**",
                        "/error",
                        "/v2/api-docs",
                        "/swagger-resources/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**"
                );
    }
}
