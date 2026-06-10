package com.chen.blog.common.satoken;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * SaTokenConfig 集成测试
 * <p>
 * 不启动完整 Spring 上下文,仅验证 {@link SaTokenConfig#addInterceptors(InterceptorRegistry)}
 * 在 Spring MVC 装配流程中可正常执行、能完成拦截器注册,且不会触发 ResourceRoleCache 的真实查询。
 * <p>
 * 设计动机:
 * <ul>
 *   <li>项目主上下文依赖 MySQL、Redis、Elasticsearch、RocketMQ、AMQP 等外部组件,
 *       直接用 {@code @SpringBootTest} 启动整个应用在 CI/本地无外部依赖时会失败。</li>
 *   <li>因此采用纯 Mockito 单元/集成混合测试,聚焦验证 SaTokenConfig 这一 Spring MVC 配置类
 *       与拦截器 API 的契约能正常对接。</li>
 *   <li>{@link ResourceRoleCache} 用作 lambda 闭包变量被 SaInterceptor 引用,
 *       注册阶段不会触发任何方法调用,只在请求到来时才被命中。</li>
 * </ul>
 *
 * @author chen
 */
@ExtendWith(MockitoExtension.class)
class SaTokenConfigIntegrationTest {

    /**
     * SaTokenConfig 通过 @Autowired 字段注入 ResourceRoleCache,
     * MockitoExtension + @InjectMocks 会自动完成字段注入。
     */
    @Mock
    private ResourceRoleCache resourceRoleCache;

    @InjectMocks
    private SaTokenConfig saTokenConfig;

    /**
     * 验证拦截器注册流程不抛异常,且能向 InterceptorRegistry 中追加一项注册。
     * <p>
     * Spring 5.3 的 {@code InterceptorRegistry} 内部以
     * {@code private final List<InterceptorRegistration> registrations}
     * 存储所有 addInterceptor 的结果,通过反射读取即可断言注册数量。
     */
    @Test
    void addInterceptors_should_register_one_interceptor() {
        InterceptorRegistry registry = new InterceptorRegistry();

        // 调用配置类的拦截器注册方法,不应抛任何异常
        saTokenConfig.addInterceptors(registry);

        // 反射读取内部 registrations 列表,确认 SaInterceptor 已被加入
        @SuppressWarnings("unchecked")
        List<Object> registrations = (List<Object>) ReflectionTestUtils.getField(registry, "registrations");
        assertNotNull(registrations, "InterceptorRegistry 内部 registrations 不应为 null");
        assertEquals(1, registrations.size(), "应注册 1 个拦截器(SaInterceptor)");
    }

    /**
     * 验证注册阶段不会触发 ResourceRoleCache 的任何方法调用。
     * <p>
     * SaInterceptor 持有的 lambda 仅在 HTTP 请求到来时才执行 matchRequiredRoles 等查询,
     * 配置阶段必须是纯静态注册,这一边界由本用例保障。
     */
    @Test
    void addInterceptors_should_not_invoke_resourceRoleCache() {
        InterceptorRegistry registry = new InterceptorRegistry();
        saTokenConfig.addInterceptors(registry);

        // 注册阶段不应触发任何 ResourceRoleCache 方法
        verifyNoInteractions(resourceRoleCache);
    }

    /**
     * 验证 SaTokenConfig 可独立实例化(无需 Spring 容器即可构造),
     * 并实现了 WebMvcConfigurer 契约,保证在 Spring MVC 装配时会被发现。
     */
    @Test
    void saTokenConfig_should_implement_WebMvcConfigurer() {
        assertNotNull(saTokenConfig, "SaTokenConfig 实例应存在");
        assertFalse(
                !(saTokenConfig instanceof org.springframework.web.servlet.config.annotation.WebMvcConfigurer),
                "SaTokenConfig 必须实现 WebMvcConfigurer 才会被 Spring MVC 装配流程发现"
        );
    }
}
