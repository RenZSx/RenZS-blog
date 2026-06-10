# Sa-Token 迁移实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 renzs-blog-satoken 项目从 Spring Security 5.4.x 迁移到 sa-token 1.39.0,前端零改动、数据库零改动。

**Architecture:** 沿用 Cookie 模式(token-name=JSESSIONID)实现前端透明;权限实现采用 SaInterceptor + 自定义认证函数 + ResourceRoleCache 复刻原动态 URL 拦截逻辑;`UserUtils.getLoginUser()` 内部改为读 SaSession,业务侧 30 处调用零改动。

**Tech Stack:** Spring Boot 2.4.1, Java 8, sa-token 1.39.0, sa-token-redis-jackson, Hutool BCrypt, JUnit 5, Mockito, Testcontainers Redis

---

## 重要前置约定

1. **顶层包名**:所有新建类位于 `com.chen.blog.*`(注意是 `com.chen.blog`,不是 `com.chen`)
2. **BCrypt 替换**:移除 Spring Security 后,`UserAuthServiceImpl` 中 3 处使用的 `org.springframework.security.crypto.bcrypt.BCrypt` 会编译失败。采用 **Hutool BCrypt** 作为替代(`cn.hutool.crypto.digest.BCrypt`,API 完全兼容)。
3. **StatusCodeEnum 实际常量名**:
   - 未登录 → `NO_LOGIN`(code=40001)✅
   - 权限不足 → `AUTHORIZED`(code=40300)⚠️ 不是 `NO_PERMISSION`
   - 用户不存在 → `USERNAME_NOT_EXIST`(code=52002)⚠️ 不是 `USER_NOT_EXIST`
   - 密码错误 → 项目无对应常量,沿用硬编码字符串 `"密码错误"`
4. **现有测试影响**:`src/test/java/com/chen/blog/common/config/SessionRedisConfigTest.java` 验证 `spring.session.store-type=redis`,会在 Phase 1 后失败,Phase 1 末尾**改写或删除**。
5. **Hutool 依赖**:项目已隐含使用 hutool(确认在 pom.xml 中存在 hutool-all,否则需在 Phase 1 加入)。

---

## 文件结构总览

### 新增(9 个 Java + 1 个常量类合并到 UserUtils)

```
src/main/java/com/chen/blog/common/satoken/
├── SaTokenConfig.java               # 配置类(注册 SaInterceptor + 全局拦截)
├── StpInterfaceImpl.java            # 角色查询实现
├── ResourceRoleCache.java           # URL→角色映射缓存(替代 FilterInvocationSecurityMetadataSourceImpl)
└── SaTokenExceptionHandler.java     # 异常映射(替代 EntryPoint + AccessDeniedHandler)

src/main/java/com/chen/blog/module/user/
├── controller/LoginController.java        # POST /api/login, POST /api/logout
├── service/LoginService.java              # 登录业务接口
├── service/impl/LoginServiceImpl.java     # 登录业务实现(含异步更新登录信息)
└── vo/LoginVO.java                        # 登录请求 VO(username, password)
```

### 修改(5 个)

```
pom.xml                                                                # 移除 Security/Spring Session,加入 sa-token
src/main/resources/application-dev.yml                                # 移除 spring.session,加入 sa-token
src/main/resources/application-pro.yml                                # 同 dev
src/main/java/com/chen/blog/common/util/UserUtils.java                # 内部改 StpUtil
src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java        # 移除 implements UserDetails
src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java  # 去 Security 依赖
src/main/java/com/chen/blog/module/user/strategy/impl/AbstractSocialLoginStrategyImpl.java  # 79-80 行替换
src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java     # BCrypt → Hutool BCrypt
src/main/java/com/chen/blog/common/handler/ControllerAdviceHandler.java           # 新增 Sa-Token 异常处理(或交给 SaTokenExceptionHandler)
src/test/java/com/chen/blog/common/config/SessionRedisConfigTest.java # 改写/删除
```

### 删除(8 个)

```
src/main/java/com/chen/blog/common/config/WebSecurityConfig.java
src/main/java/com/chen/blog/common/handler/AuthenticationEntryPointImpl.java
src/main/java/com/chen/blog/common/handler/AccessDeniedHandlerImpl.java
src/main/java/com/chen/blog/common/handler/AuthenticationSuccessHandlerImpl.java
src/main/java/com/chen/blog/common/handler/AuthenticationFailHandlerImpl.java
src/main/java/com/chen/blog/common/handler/LogoutSuccessHandlerImpl.java
src/main/java/com/chen/blog/common/handler/FilterInvocationSecurityMetadataSourceImpl.java
src/main/java/com/chen/blog/common/handler/AccessDecisionManagerImpl.java
```

---

# Phase 1: 基础设施层(依赖与配置)

## Task 1.1: 替换 pom.xml 依赖

**Files:**
- Modify: `pom.xml`(行 56-67)

- [ ] **Step 1: 删除 spring-session-data-redis 与 spring-boot-starter-security**

打开 `pom.xml`,删除第 56-59 行和第 64-67 行:

```xml
        <!-- 删除以下 -->
        <dependency>
            <groupId>org.springframework.session</groupId>
            <artifactId>spring-session-data-redis</artifactId>
        </dependency>
        ...
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
```

- [ ] **Step 2: 在依赖列表末尾(`</dependencies>` 前)插入 sa-token + Hutool BCrypt**

```xml
        <!-- sa-token 核心 -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot-starter</artifactId>
            <version>1.39.0</version>
        </dependency>
        <!-- sa-token Redis 持久化(Jackson 序列化) -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-redis-jackson</artifactId>
            <version>1.39.0</version>
        </dependency>
        <!-- Redis 连接池 -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>
        <!-- Hutool BCrypt(替代 spring-security-crypto.BCrypt) -->
        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-crypto</artifactId>
            <version>5.8.25</version>
        </dependency>
```

> 注:如果 pom 已有 `hutool-all`,则不需要单独引入 hutool-crypto。先用 Grep 确认。

- [ ] **Step 3: 验证依赖树**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
mvn dependency:tree -Dincludes=cn.dev33 -DoutputFile=dep.txt
```

**Expected:** dep.txt 包含 `sa-token-spring-boot-starter:jar:1.39.0` 和 `sa-token-redis-jackson:jar:1.39.0`。

```bash
mvn dependency:tree -Dincludes=org.springframework.security 2>&1 | grep -i security
```

**Expected:** 输出为空(没有 spring-security 残留)。

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "chore: 替换 Spring Security 依赖为 sa-token 1.39.0"
```

---

## Task 1.2: 替换 application-dev.yml 与 application-pro.yml 配置

**Files:**
- Modify: `src/main/resources/application-dev.yml`(行 4-15)
- Modify: `src/main/resources/application-pro.yml`(行 4-15)

- [ ] **Step 1: 删除两个 yml 的 spring.session 块和 servlet.session 块**

打开 `application-dev.yml`,删除以下内容(注意保留 `server.port` 与 `spring.datasource`):

```yaml
server:
  port: 8088
  servlet:                  # ← 删除整个 servlet 块
    session:
      timeout: 2592000
      cookie:
        max-age: 2592000

spring:
  session:                  # ← 删除整个 session 块
    store-type: redis
    redis:
      namespace: renzs-blog:session
  datasource:
    ...
```

修改后:

```yaml
server:
  port: 8088

spring:
  datasource:
    ...
```

对 `application-pro.yml` 执行**完全相同**的删除操作(两个文件结构一致)。

- [ ] **Step 2: 在两个 yml 末尾追加 sa-token 配置**

`application-dev.yml` 末尾追加:

```yaml

# sa-token 配置
sa-token:
  token-name: JSESSIONID            # 沿用旧 Cookie 名,前端零感
  timeout: 2592000                   # 30 天
  active-timeout: -1                 # 永不冻结
  is-concurrent: true                # 允许同账号并发登录
  is-share: true                     # 共享同 token(多端同 Cookie)
  max-login-count: 20                # 与原 maximumSessions(20) 等价
  token-style: uuid
  is-log: false
  is-read-cookie: true               # ✅ Cookie 模式
  is-read-header: false
  is-print: false
  # 持久化使用上方 spring.redis 同一实例,由 sa-token-redis-jackson 自动接管
```

`application-pro.yml` 末尾追加**完全相同**的 sa-token 配置块。

- [ ] **Step 3: 验证 yml 格式合法**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
mvn compile -o 2>&1 | head -20
```

**Expected:** 编译可能因为代码层 Security 引用还未删除而失败,但 yml 解析不应报错。如果看到 `org.yaml.snakeyaml.scanner.ScannerException` 则格式有问题。

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/application-dev.yml src/main/resources/application-pro.yml
git commit -m "chore: 配置 sa-token,移除 Spring Session Redis 配置"
```

---

## Task 1.3: 处理失效的 SessionRedisConfigTest

**Files:**
- Modify: `src/test/java/com/chen/blog/common/config/SessionRedisConfigTest.java`(全文)

- [ ] **Step 1: 改写测试为校验 sa-token 配置**

完整覆盖 `SessionRedisConfigTest.java`:

```java
package com.chen.blog.common.config;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 验证 sa-token 关键配置存在且正确
 */
class SaTokenConfigTest {

    @Test
    void devProfileEnablesSaTokenCookieMode() {
        assertSaTokenConfig("application-dev.yml");
    }

    @Test
    void proProfileEnablesSaTokenCookieMode() {
        assertSaTokenConfig("application-pro.yml");
    }

    @SuppressWarnings("unchecked")
    private void assertSaTokenConfig(String resourceName) {
        Yaml yaml = new Yaml();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);
        assertNotNull(inputStream, "配置文件应存在: " + resourceName);
        Map<String, Object> root = yaml.load(inputStream);
        Map<String, Object> saToken = (Map<String, Object>) root.get("sa-token");
        assertNotNull(saToken, "应包含 sa-token 配置块");
        assertEquals("JSESSIONID", saToken.get("token-name"), "Cookie 名应沿用 JSESSIONID");
        assertEquals(2592000, saToken.get("timeout"), "超时应为 30 天");
        assertEquals(true, saToken.get("is-read-cookie"), "应启用 Cookie 模式");
        assertEquals(20, saToken.get("max-login-count"), "最大登录数应为 20");
    }
}
```

- [ ] **Step 2: 重命名测试文件**

将文件从 `SessionRedisConfigTest.java` 重命名为 `SaTokenConfigTest.java`(类名与文件名匹配)。

```bash
cd "D:/桌面/blog-master/renzs-blog-satoken/src/test/java/com/chen/blog/common/config"
git mv SessionRedisConfigTest.java SaTokenConfigTest.java
```

- [ ] **Step 3: 运行测试验证**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
mvn test -Dtest=SaTokenConfigTest -o 2>&1 | tail -30
```

**Expected:** 2 个测试通过(devProfileEnablesSaTokenCookieMode、proProfileEnablesSaTokenCookieMode)。

> 如果 mvn test 因主代码编译失败而中断,跳过本步,等 Phase 2 之后再回头跑。

- [ ] **Step 4: Commit**

```bash
git add src/test/java/com/chen/blog/common/config/
git commit -m "test: 改写 Session 测试为 sa-token 配置验证"
```

---

# Phase 2: 鉴权核心层(sa-token 集成)

## Task 2.1: 改造 UserDetailDTO,移除 UserDetails 接口

**Files:**
- Modify: `src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java`(全文)

- [ ] **Step 1: 完整覆盖 UserDetailDTO.java**

```java
package com.chen.blog.module.user.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * 用户认证详情 DTO
 * (sa-token 迁移后不再实现 UserDetails,改为普通 POJO)
 */
@Data
@Builder
public class UserDetailDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer userInfoId;
    private String email;
    private Integer loginType;
    private String username;
    private String password;
    private List<String> roleList;
    private String nickname;
    private String avatar;
    private String intro;
    private String webSite;
    private Set<Object> articleLikeSet;
    private Set<Object> commentLikeSet;
    private Set<Object> talkLikeSet;
    private String ipAddress;
    private String ipSource;
    private Integer isDisable;
    private String browser;
    private String os;
    private LocalDateTime lastLoginTime;
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "(UserDetailDTO|UserDetails)" | head -10
```

**Expected:** 仍有 `UserDetailsServiceImpl` 等地方报错,但 `UserDetailDTO.java` 本身已无 Security 引用。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java
git commit -m "refactor: UserDetailDTO 移除 UserDetails 接口实现"
```

---

## Task 2.2: 创建 SaTokenConsts 常量到 UserUtils

由于只有一个常量,直接合并到 `UserUtils` 中,**不单独创建 SaTokenConsts 文件**(YAGNI)。常量直接在 `UserUtils` 中定义为 public static。

跳过独立步骤,在 Task 2.3 完成。

---

## Task 2.3: 改造 UserUtils,内部改为读 SaSession

**Files:**
- Modify: `src/main/java/com/chen/blog/common/util/UserUtils.java`(全文)

- [ ] **Step 1: 完整覆盖 UserUtils.java**

```java
package com.chen.blog.common.util;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.dto.UserDetailDTO;
import org.springframework.stereotype.Component;

import static com.chen.blog.common.enums.StatusCodeEnum.NO_LOGIN;

/**
 * 用户工具类(sa-token 版)
 * 对外签名保持不变,业务侧 30 处调用零改动
 */
@Component
public class UserUtils {

    /** SaSession 中存放 UserDetailDTO 的 key,登录时塞入,业务侧通过 getLoginUser() 取回 */
    public static final String LOGIN_USER_KEY = "loginUser";

    public static UserDetailDTO getLoginUser() {
        if (!StpUtil.isLogin()) {
            throw new BizException(NO_LOGIN);
        }
        SaSession session = StpUtil.getTokenSession();
        Object obj = session.get(LOGIN_USER_KEY);
        if (!(obj instanceof UserDetailDTO)) {
            throw new BizException(NO_LOGIN);
        }
        return (UserDetailDTO) obj;
    }
}
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "UserUtils" | head -5
```

**Expected:** UserUtils 本身无报错(其他文件仍有 Security 残留)。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/common/util/UserUtils.java
git commit -m "refactor: UserUtils.getLoginUser 改为读 SaSession"
```

---

## Task 2.4: 创建 ResourceRoleCache(URL→角色映射缓存)

**Files:**
- Create: `src/main/java/com/chen/blog/common/satoken/ResourceRoleCache.java`

- [ ] **Step 1: 创建包目录**

```bash
mkdir -p "D:/桌面/blog-master/renzs-blog-satoken/src/main/java/com/chen/blog/common/satoken"
```

- [ ] **Step 2: 写 ResourceRoleCache.java**

```java
package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import com.chen.blog.module.user.dto.ResourceRoleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 资源-角色映射缓存(替代 FilterInvocationSecurityMetadataSourceImpl)
 * 启动时从 tb_resource 表加载,权限变更后调用 refresh() 刷新
 */
@Component
public class ResourceRoleCache {

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Autowired
    private RoleDao roleDao;

    private volatile List<ResourceRoleDTO> resourceRoleList;

    @PostConstruct
    public void init() {
        refresh();
    }

    /** 权限变更后由角色/资源管理 Controller 调用,等价于原 clearDataSource() */
    public synchronized void refresh() {
        this.resourceRoleList = roleDao.listResourceRoles();
    }

    /**
     * 匹配请求,返回需要的角色列表
     * @return null = 未配置该 URL(默认放行,与原行为一致);empty list = 任意已登录用户可访问;非空 list = 需要其中任一角色
     */
    public List<String> matchRequiredRoles(String url, String method) {
        if (CollectionUtils.isEmpty(resourceRoleList)) {
            return null;
        }
        for (ResourceRoleDTO r : resourceRoleList) {
            if (r.getRequestMethod() != null
                    && r.getRequestMethod().equalsIgnoreCase(method)
                    && MATCHER.match(r.getUrl(), url)) {
                return r.getRoleList();
            }
        }
        return null;
    }
}
```

- [ ] **Step 3: 写 ResourceRoleCacheTest.java(单元测试)**

Create: `src/test/java/com/chen/blog/common/satoken/ResourceRoleCacheTest.java`

```java
package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import com.chen.blog.module.user.dto.ResourceRoleDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResourceRoleCacheTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private ResourceRoleCache cache;

    private ResourceRoleDTO adminResource;
    private ResourceRoleDTO userResource;

    @BeforeEach
    void setUp() {
        adminResource = new ResourceRoleDTO();
        adminResource.setUrl("/admin/**");
        adminResource.setRequestMethod("GET");
        adminResource.setRoleList(Collections.singletonList("admin"));

        userResource = new ResourceRoleDTO();
        userResource.setUrl("/users/current");
        userResource.setRequestMethod("GET");
        userResource.setRoleList(Arrays.asList("admin", "user"));

        when(roleDao.listResourceRoles()).thenReturn(Arrays.asList(adminResource, userResource));
        cache.init();
    }

    @Test
    void matchRequiredRoles_should_return_roles_when_url_matches() {
        List<String> roles = cache.matchRequiredRoles("/admin/users", "GET");
        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertEquals("admin", roles.get(0));
    }

    @Test
    void matchRequiredRoles_should_return_null_when_url_not_match() {
        List<String> roles = cache.matchRequiredRoles("/unknown/path", "GET");
        assertNull(roles);
    }

    @Test
    void matchRequiredRoles_should_distinguish_method() {
        List<String> roles = cache.matchRequiredRoles("/users/current", "POST");
        assertNull(roles);
    }

    @Test
    void refresh_should_reload_from_dao() {
        ResourceRoleDTO newRes = new ResourceRoleDTO();
        newRes.setUrl("/new/path");
        newRes.setRequestMethod("GET");
        newRes.setRoleList(Collections.singletonList("admin"));
        when(roleDao.listResourceRoles()).thenReturn(Collections.singletonList(newRes));

        cache.refresh();

        assertNull(cache.matchRequiredRoles("/admin/users", "GET"));
        assertNotNull(cache.matchRequiredRoles("/new/path", "GET"));
    }
}
```

- [ ] **Step 4: 运行测试**

```bash
mvn test -Dtest=ResourceRoleCacheTest -o 2>&1 | tail -20
```

**Expected:** 4 个测试全部通过。

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/chen/blog/common/satoken/ResourceRoleCache.java \
        src/test/java/com/chen/blog/common/satoken/ResourceRoleCacheTest.java
git commit -m "feat: 新增 ResourceRoleCache 替代 FilterInvocationSecurityMetadataSourceImpl"
```

---

## Task 2.5: 创建 StpInterfaceImpl(角色查询)

**Files:**
- Create: `src/main/java/com/chen/blog/common/satoken/StpInterfaceImpl.java`
- Create: `src/test/java/com/chen/blog/common/satoken/StpInterfaceImplTest.java`

- [ ] **Step 1: 写 StpInterfaceImpl.java**

```java
package com.chen.blog.common.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.chen.blog.module.rbac.dao.RoleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * sa-token 权限/角色查询实现
 * 角色直接复用 RoleDao.listRolesByUserInfoId(),零转换成本
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Autowired
    private RoleDao roleDao;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目按角色鉴权,不使用细粒度权限
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Integer userInfoId = Integer.parseInt(loginId.toString());
        return roleDao.listRolesByUserInfoId(userInfoId);
    }
}
```

- [ ] **Step 2: 写 StpInterfaceImplTest.java**

```java
package com.chen.blog.common.satoken;

import com.chen.blog.module.rbac.dao.RoleDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StpInterfaceImplTest {

    @Mock
    private RoleDao roleDao;

    @InjectMocks
    private StpInterfaceImpl stpInterface;

    @Test
    void getRoleList_should_return_roles_from_dao() {
        when(roleDao.listRolesByUserInfoId(1)).thenReturn(Arrays.asList("admin", "user"));
        List<String> roles = stpInterface.getRoleList(1, "login");
        assertEquals(2, roles.size());
        assertTrue(roles.contains("admin"));
    }

    @Test
    void getPermissionList_should_always_be_empty() {
        List<String> perms = stpInterface.getPermissionList(1, "login");
        assertTrue(perms.isEmpty());
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
mvn test -Dtest=StpInterfaceImplTest -o 2>&1 | tail -15
```

**Expected:** 2 个测试通过。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/chen/blog/common/satoken/StpInterfaceImpl.java \
        src/test/java/com/chen/blog/common/satoken/StpInterfaceImplTest.java
git commit -m "feat: 实现 sa-token StpInterface,接入现有 RoleDao"
```

---

## Task 2.6: 创建 SaTokenExceptionHandler(异常映射)

**Files:**
- Create: `src/main/java/com/chen/blog/common/satoken/SaTokenExceptionHandler.java`

- [ ] **Step 1: 写 SaTokenExceptionHandler.java**

```java
package com.chen.blog.common.satoken;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.common.enums.StatusCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * sa-token 异常映射
 * 替代原 AuthenticationEntryPointImpl + AccessDeniedHandlerImpl
 * Order = 0 保证比 ControllerAdviceHandler 优先级高(后者用默认 Ordered.LOWEST_PRECEDENCE)
 */
@Slf4j
@Order(0)
@RestControllerAdvice
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        log.debug("未登录: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.NO_LOGIN);
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermission(NotPermissionException e) {
        log.debug("权限不足: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.AUTHORIZED);
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNotRole(NotRoleException e) {
        log.debug("角色不足: {}", e.getMessage());
        return Result.fail(StatusCodeEnum.AUTHORIZED);
    }
}
```

- [ ] **Step 2: 写测试**

Create: `src/test/java/com/chen/blog/common/satoken/SaTokenExceptionHandlerTest.java`

```java
package com.chen.blog.common.satoken;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.common.enums.StatusCodeEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SaTokenExceptionHandlerTest {

    private final SaTokenExceptionHandler handler = new SaTokenExceptionHandler();

    @Test
    void notLoginException_should_return_NO_LOGIN() {
        Result<?> r = handler.handleNotLogin(NotLoginException.newInstance("login", "INVALID_TOKEN"));
        assertEquals(StatusCodeEnum.NO_LOGIN.getCode(), r.getCode());
    }

    @Test
    void notPermissionException_should_return_AUTHORIZED() {
        Result<?> r = handler.handleNotPermission(new NotPermissionException("perm", "login"));
        assertEquals(StatusCodeEnum.AUTHORIZED.getCode(), r.getCode());
    }

    @Test
    void notRoleException_should_return_AUTHORIZED() {
        Result<?> r = handler.handleNotRole(new NotRoleException("role", "login"));
        assertEquals(StatusCodeEnum.AUTHORIZED.getCode(), r.getCode());
    }
}
```

- [ ] **Step 3: 运行测试**

```bash
mvn test -Dtest=SaTokenExceptionHandlerTest -o 2>&1 | tail -15
```

**Expected:** 3 个测试通过。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/chen/blog/common/satoken/SaTokenExceptionHandler.java \
        src/test/java/com/chen/blog/common/satoken/SaTokenExceptionHandlerTest.java
git commit -m "feat: 新增 SaTokenExceptionHandler 映射鉴权异常为统一 Result"
```

---

## Task 2.7: 创建 SaTokenConfig(SaInterceptor 注册)

**Files:**
- Create: `src/main/java/com/chen/blog/common/satoken/SaTokenConfig.java`

- [ ] **Step 1: 写 SaTokenConfig.java**

```java
package com.chen.blog.common.satoken;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

/**
 * sa-token 拦截器配置
 * 替代 Spring Security 的过滤器链 + AccessDecisionManagerImpl
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Autowired
    private ResourceRoleCache resourceRoleCache;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            String url = SaRouter.staticContext().getRequest().getRequestPath();
            String method = SaRouter.staticContext().getRequest().getMethod();

            List<String> requiredRoles = resourceRoleCache.matchRequiredRoles(url, method);

            // 未在 tb_resource 中配置 → 默认放行(与原 FilterInvocationSecurityMetadataSourceImpl 行为一致,返回 null 时放行)
            if (requiredRoles == null) {
                return;
            }

            // 已配置但 roleList 为空 → 与原行为一致:返回 "disable" 角色,任何用户都无法满足,等价拒绝
            if (requiredRoles.isEmpty()) {
                throw new NotPermissionException("disable");
            }

            // 校验登录
            StpUtil.checkLogin();

            // 校验角色:用户角色与所需角色有交集即可
            List<String> userRoles = StpUtil.getRoleList();
            if (Collections.disjoint(userRoles, requiredRoles)) {
                throw new NotPermissionException(String.join(",", requiredRoles));
            }
        })).addPathPatterns("/**")
          .excludePathPatterns(
                  "/api/login",
                  "/api/logout",
                  "/error",
                  "/v2/api-docs",
                  "/swagger-resources/**",
                  "/swagger-ui.html",
                  "/swagger-ui/**",
                  "/webjars/**"
          );
    }
}
```

> ⚠️ 注意:`SaRouter.staticContext()` 是 sa-token 提供的获取当前请求上下文的 API。如果 1.39.0 中 API 名为 `SaHolder.getRequest()`,请相应替换。具体见 sa-token 1.39.0 文档。**Phase 2 末尾启动验证时若编译失败,改为:**
> ```java
> String url = cn.dev33.satoken.context.SaHolder.getRequest().getRequestPath();
> String method = cn.dev33.satoken.context.SaHolder.getRequest().getMethod();
> ```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "(SaTokenConfig|SaRouter|SaHolder)" | head -10
```

**Expected:** 无编译错误。如有 `cannot find symbol: method staticContext()`,改用 `SaHolder.getRequest()`。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/common/satoken/SaTokenConfig.java
git commit -m "feat: 注册 SaInterceptor,实现动态 URL 鉴权"
```

---

## Task 2.8: UserAuthServiceImpl 替换 BCrypt 为 Hutool BCrypt

**Files:**
- Modify: `src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java`(第 34、128、146、160、163 行)

- [ ] **Step 1: 替换 import**

将文件中第 34 行的:

```java
import org.springframework.security.crypto.bcrypt.BCrypt;
```

替换为:

```java
import cn.hutool.crypto.digest.BCrypt;
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "UserAuthServiceImpl" | head -5
```

**Expected:** `UserAuthServiceImpl` 不再报错(Hutool BCrypt 的 `hashpw/gensalt/checkpw` 方法签名与 Spring Security 完全兼容,无需改业务逻辑)。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java
git commit -m "refactor: BCrypt 工具类切换为 Hutool 实现"
```

---

## Task 2.9: 改造 UserDetailsServiceImpl(去 Security 依赖)

**Files:**
- Modify: `src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java`

- [ ] **Step 1: 删除 Security 相关 import 和接口**

按以下 diff 修改文件:

```java
// 删除 import:
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

// 类签名:
// 改前
public class UserDetailsServiceImpl implements UserDetailsService {
// 改后
public class UserDetailsServiceImpl {

// loadUserByUsername 方法:
// 改前
@Override
public UserDetails loadUserByUsername(String username) {
// 改后(去 @Override,改返回类型为 UserDetailDTO)
public UserDetailDTO loadUserByUsername(String username) {
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | tail -20
```

**Expected:** UserDetailsServiceImpl 编译通过。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java
git commit -m "refactor: UserDetailsServiceImpl 移除 Spring Security 接口依赖"
```

---

# Phase 3: 业务接口层(登录登出 + 删除旧类)

## Task 3.1: 创建 LoginVO

**Files:**
- Create: `src/main/java/com/chen/blog/module/user/vo/LoginVO.java`

- [ ] **Step 1: 写 LoginVO.java**

```java
package com.chen.blog.module.user.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求 VO
 */
@Data
public class LoginVO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
```

- [ ] **Step 2: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/vo/LoginVO.java
git commit -m "feat: 新增登录请求 VO"
```

---

## Task 3.2: 创建 LoginService 接口与实现

**Files:**
- Create: `src/main/java/com/chen/blog/module/user/service/LoginService.java`
- Create: `src/main/java/com/chen/blog/module/user/service/impl/LoginServiceImpl.java`

- [ ] **Step 1: 写 LoginService.java**

```java
package com.chen.blog.module.user.service;

import com.chen.blog.module.user.dto.UserInfoDTO;
import com.chen.blog.module.user.vo.LoginVO;

/**
 * 登录业务接口
 */
public interface LoginService {

    /**
     * 账密登录
     * @return 登录成功的用户信息
     */
    UserInfoDTO login(LoginVO loginVO);

    /**
     * 注销当前登录
     */
    void logout();
}
```

- [ ] **Step 2: 写 LoginServiceImpl.java**

```java
package com.chen.blog.module.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.common.util.UserUtils;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dto.UserDetailDTO;
import com.chen.blog.module.user.dto.UserInfoDTO;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chen.blog.module.user.vo.LoginVO;

import static com.chen.blog.common.enums.StatusCodeEnum.USERNAME_NOT_EXIST;

/**
 * 登录业务实现
 * 复刻原 AuthenticationSuccessHandlerImpl 的"异步更新登录时间/IP"逻辑
 */
@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserAuthDao userAuthDao;

    @Override
    public UserInfoDTO login(LoginVO loginVO) {
        // 1. 查询用户(沿用 UserDetailsServiceImpl.loadUserByUsername)
        UserDetailDTO userDetail;
        try {
            userDetail = userDetailsService.loadUserByUsername(loginVO.getUsername());
        } catch (BizException e) {
            throw new BizException(USERNAME_NOT_EXIST);
        }
        // 2. 校验密码(Hutool BCrypt)
        if (!BCrypt.checkpw(loginVO.getPassword(), userDetail.getPassword())) {
            throw new BizException("密码错误");
        }
        // 3. sa-token 登录,把 UserDetailDTO 塞入 TokenSession
        StpUtil.login(userDetail.getId());
        StpUtil.getTokenSession().set(UserUtils.LOGIN_USER_KEY, userDetail);
        // 4. 异步更新登录信息(IP / 最后登录时间)
        updateLoginInfo(userDetail);
        // 5. 返回 UserInfoDTO
        return BeanCopyUtils.copyObject(userDetail, UserInfoDTO.class);
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Async
    public void updateLoginInfo(UserDetailDTO userDetail) {
        UserAuth userAuth = UserAuth.builder()
                .id(userDetail.getId())
                .ipAddress(userDetail.getIpAddress())
                .ipSource(userDetail.getIpSource())
                .lastLoginTime(userDetail.getLastLoginTime())
                .build();
        userAuthDao.updateById(userAuth);
    }
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "LoginService" | head -5
```

**Expected:** 无错误。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/service/LoginService.java \
        src/main/java/com/chen/blog/module/user/service/impl/LoginServiceImpl.java
git commit -m "feat: 实现登录业务 LoginService,复刻原异步更新逻辑"
```

---

## Task 3.3: 创建 LoginController(登录/登出 HTTP 接口)

**Files:**
- Create: `src/main/java/com/chen/blog/module/user/controller/LoginController.java`

- [ ] **Step 1: 写 LoginController.java**

```java
package com.chen.blog.module.user.controller;

import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.user.dto.UserInfoDTO;
import com.chen.blog.module.user.service.LoginService;
import com.chen.blog.module.user.vo.LoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录登出接口
 * 自实现,替代原 Spring Security 的 formLogin 过滤器
 */
@Api(tags = "登录登出")
@RestController
@RequestMapping("/api")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @ApiOperation("账号密码登录")
    @PostMapping("/login")
    public Result<UserInfoDTO> login(@Validated LoginVO loginVO) {
        return Result.ok(loginService.login(loginVO));
    }

    @ApiOperation("注销")
    @PostMapping("/logout")
    public Result<?> logout() {
        loginService.logout();
        return Result.ok();
    }
}
```

> ⚠️ 注意 URL:原 Spring Security 配置的 `loginProcessingUrl("/login")` 是 `/login`,前端经过 nginx 转发可能映射到 `/api/login`。**用 Grep 在 nginx 配置或前端代码中确认实际请求路径**。如确为 `/login` 直连,则把 `@RequestMapping("/api")` 改为 `@RequestMapping`,路径写 `/login` `/logout`。

- [ ] **Step 2: 确认登录路径**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
grep -rn "loginProcessingUrl" src/main/java/com/chen/blog 2>&1 | head -5
```

如果项目中之前是 `/login`,而前端通过 nginx 重写为 `/api/login` 后转发为 `/login`,则需要根据实际部署调整。**默认按 `/api/login` 实现,部署时再校准**。

- [ ] **Step 3: 启动应用验证(粗略)**

```bash
mvn spring-boot:run -o 2>&1 | grep -E "(Started|Error|sa-token)" | head -20
```

**Expected:** 看到 `Started Application in X seconds`。Ctrl+C 关掉。

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/controller/LoginController.java
git commit -m "feat: 自实现 LoginController,提供 /api/login 和 /api/logout"
```

---

## Task 3.4: 改造 AbstractSocialLoginStrategyImpl(第三方登录)

**Files:**
- Modify: `src/main/java/com/chen/blog/module/user/strategy/impl/AbstractSocialLoginStrategyImpl.java`(第 24-25 行 import + 第 78-80 行替换)

- [ ] **Step 1: 替换 import 和登录逻辑**

删除文件中第 24-25 行的 Security import:

```java
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;  // 删除
import org.springframework.security.core.context.SecurityContextHolder;                  // 删除
```

新增 import:

```java
import cn.dev33.satoken.stp.StpUtil;
import com.chen.blog.common.util.UserUtils;
```

定位到第 78-80 行原代码:

```java
        // 将登录信息放入springSecurity管理
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetailDTO, null, userDetailDTO.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
```

替换为:

```java
        // 将登录信息放入 sa-token 管理
        StpUtil.login(userDetailDTO.getId());
        StpUtil.getTokenSession().set(UserUtils.LOGIN_USER_KEY, userDetailDTO);
```

- [ ] **Step 2: 验证编译**

```bash
mvn compile -o -pl . 2>&1 | grep -E "AbstractSocialLogin" | head -5
```

**Expected:** 无错误。

- [ ] **Step 3: Commit**

```bash
git add src/main/java/com/chen/blog/module/user/strategy/impl/AbstractSocialLoginStrategyImpl.java
git commit -m "refactor: 第三方登录策略改用 sa-token"
```

---

## Task 3.5: 删除 8 个旧 Spring Security 类

**Files:**
- Delete: `src/main/java/com/chen/blog/common/config/WebSecurityConfig.java`
- Delete: `src/main/java/com/chen/blog/common/handler/AuthenticationEntryPointImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/AccessDeniedHandlerImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/AuthenticationSuccessHandlerImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/AuthenticationFailHandlerImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/LogoutSuccessHandlerImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/FilterInvocationSecurityMetadataSourceImpl.java`
- Delete: `src/main/java/com/chen/blog/common/handler/AccessDecisionManagerImpl.java`

- [ ] **Step 1: 删除 8 个文件**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
rm src/main/java/com/chen/blog/common/config/WebSecurityConfig.java
rm src/main/java/com/chen/blog/common/handler/AuthenticationEntryPointImpl.java
rm src/main/java/com/chen/blog/common/handler/AccessDeniedHandlerImpl.java
rm src/main/java/com/chen/blog/common/handler/AuthenticationSuccessHandlerImpl.java
rm src/main/java/com/chen/blog/common/handler/AuthenticationFailHandlerImpl.java
rm src/main/java/com/chen/blog/common/handler/LogoutSuccessHandlerImpl.java
rm src/main/java/com/chen/blog/common/handler/FilterInvocationSecurityMetadataSourceImpl.java
rm src/main/java/com/chen/blog/common/handler/AccessDecisionManagerImpl.java
```

- [ ] **Step 2: 检查是否有遗留引用**

```bash
grep -rn "AuthenticationEntryPointImpl\|AccessDeniedHandlerImpl\|AuthenticationSuccessHandlerImpl\|AuthenticationFailHandlerImpl\|LogoutSuccessHandlerImpl\|FilterInvocationSecurityMetadataSourceImpl\|AccessDecisionManagerImpl\|WebSecurityConfig" src/main/java 2>&1 | head -10
```

**Expected:** 输出为空。

- [ ] **Step 3: 检查 Spring Security 引用残留**

```bash
grep -rn "org.springframework.security" src/main/java 2>&1
```

**Expected:** 输出为空(0 处引用)。

- [ ] **Step 4: 编译完整项目**

```bash
mvn clean compile -o 2>&1 | tail -20
```

**Expected:** `BUILD SUCCESS`。

- [ ] **Step 5: Commit**

```bash
git add -A src/main/java/com/chen/blog/common/config/ src/main/java/com/chen/blog/common/handler/
git commit -m "refactor: 删除 8 个 Spring Security 配置和 Handler 类"
```

---

## Task 3.6: 启动应用 + Postman 跑通核心链路

**Files:** 无文件改动,仅手动验证

- [ ] **Step 1: 启动应用**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
mvn spring-boot:run -o
```

**Expected:** 控制台显示 `Started BlogApplication in X seconds`,启动 banner 中有 sa-token 相关输出。

- [ ] **Step 2: Postman 测试登录**

```
POST http://localhost:8088/api/login
Content-Type: application/x-www-form-urlencoded
Body: username=admin&password=12345678
```

**Expected:** 200 OK,Response 包含 `flag: true`,Headers 含 `Set-Cookie: JSESSIONID=xxx`。

- [ ] **Step 3: Postman 测试当前用户**

```
GET http://localhost:8088/users/current
Cookie: JSESSIONID=xxx(从上一步取)
```

**Expected:** 200 OK,返回当前用户信息。

- [ ] **Step 4: Postman 测试登出**

```
POST http://localhost:8088/api/logout
Cookie: JSESSIONID=xxx
```

**Expected:** 200 OK。再次请求 `/users/current` 应返回 `code: 40001`。

- [ ] **Step 5: 关闭应用,如有需要修复问题再 Commit**

如步骤 2-4 失败,根据错误信息修复后:

```bash
git add -A
git commit -m "fix: <具体修复说明>"
```

---

# Phase 4: 测试与验证

## Task 4.1: 引入 Testcontainers 与测试基础设施

**Files:**
- Modify: `pom.xml`
- Create: `src/test/java/com/chen/blog/BaseIntegrationTest.java`

- [ ] **Step 1: 在 pom.xml 中加入 testcontainers**

在 `</dependencies>` 前插入:

```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
```

- [ ] **Step 2: 写 BaseIntegrationTest.java**

```java
package com.chen.blog;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 集成测试基类
 * 自动启动 Redis Testcontainer,适配 sa-token 持久化
 */
@SpringBootTest
@Testcontainers
public abstract class BaseIntegrationTest {

    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry r) {
        r.add("spring.redis.host", REDIS::getHost);
        r.add("spring.redis.port", () -> REDIS.getMappedPort(6379));
        r.add("spring.redis.password", () -> "");
    }
}
```

- [ ] **Step 3: 验证编译**

```bash
mvn test-compile -o 2>&1 | tail -10
```

**Expected:** 编译成功。

- [ ] **Step 4: Commit**

```bash
git add pom.xml src/test/java/com/chen/blog/BaseIntegrationTest.java
git commit -m "test: 引入 Testcontainers Redis 作为集成测试基础设施"
```

---

## Task 4.2: 编写 LoginControllerIntegrationTest(集成测试)

**Files:**
- Create: `src/test/java/com/chen/blog/module/user/controller/LoginControllerIntegrationTest.java`

- [ ] **Step 1: 写测试**

```java
package com.chen.blog.module.user.controller;

import com.chen.blog.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录登出集成测试
 * 覆盖 spec 中集成测试清单第 1-6 项
 */
@AutoConfigureMockMvc
class LoginControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void login_with_correct_credentials_should_return_cookie() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(header().string("Set-Cookie", containsString("JSESSIONID")))
                .andReturn();
        Cookie cookie = result.getResponse().getCookie("JSESSIONID");
        assertNotNull(cookie);
    }

    @Test
    void login_with_wrong_password_should_fail() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "wrong_password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    void login_with_nonexistent_user_should_fail_with_52002() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "no_such_user_999")
                        .param("password", "any"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(52002));
    }

    @Test
    void current_user_without_login_should_return_40001() throws Exception {
        mockMvc.perform(get("/users/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(40001));
    }

    @Test
    void logout_should_invalidate_cookie() throws Exception {
        // 1. 登录拿 Cookie
        MvcResult login = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "admin")
                        .param("password", "12345678"))
                .andReturn();
        Cookie cookie = login.getResponse().getCookie("JSESSIONID");

        // 2. 登出
        mockMvc.perform(post("/api/logout").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true));

        // 3. 再访问当前用户应失败
        mockMvc.perform(get("/users/current").cookie(cookie))
                .andExpect(jsonPath("$.code").value(40001));
    }
}
```

> ⚠️ **测试数据前提**:测试要求数据库中存在 `username=admin password=明文 12345678 经过 BCrypt 加密后的密文` 的用户。如果当前数据库 admin 用户密码不匹配,需要在测试前先用 H2 内存数据库或 Testcontainers MySQL 准备初始数据。**简化方案**:在测试前手动用 SQL 插入测试用户,或使用 `@Sql(scripts = "/test-users.sql")` 注解加载脚本。

- [ ] **Step 2: 生成密码哈希**

写一个一次性工具类用于生成 BCrypt 哈希:

Create: `src/test/java/com/chen/blog/util/GenerateBCryptHash.java`(临时工具,生成后可删)

```java
package com.chen.blog.util;

import cn.hutool.crypto.digest.BCrypt;

public class GenerateBCryptHash {
    public static void main(String[] args) {
        String hash = BCrypt.hashpw("12345678", BCrypt.gensalt());
        System.out.println("Hash for '12345678': " + hash);
    }
}
```

运行:
```bash
mvn test-compile -o
java -cp "target/test-classes;target/classes;$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.chen.blog.util.GenerateBCryptHash
```

记下输出的 hash 字符串(每次运行不同,salt 随机)。

- [ ] **Step 3: 创建测试 SQL**

Create: `src/test/resources/test-users.sql`(把 `<PASTE_HASH_HERE>` 替换为上一步生成的 hash):

```sql
-- 测试用户数据
DELETE FROM tb_user_role WHERE user_id = 999;
DELETE FROM tb_user_auth WHERE id = 999;
DELETE FROM tb_user_info WHERE id = 999;

INSERT INTO tb_user_info (id, email, nickname, avatar, is_disable, create_time)
VALUES (999, 'test@test.com', 'admin_test', '', 0, NOW());

INSERT INTO tb_user_auth (id, user_info_id, username, password, login_type, create_time)
VALUES (999, 999, 'admin', '<PASTE_HASH_HERE>', 1, NOW());

INSERT INTO tb_user_role (user_id, role_id) VALUES (999, 1);
```

在每个集成测试类上加注解:

```java
@org.springframework.test.context.jdbc.Sql("/test-users.sql")
```

- [ ] **Step 4: 运行测试**

```bash
mvn test -Dtest=LoginControllerIntegrationTest -o 2>&1 | tail -30
```

**Expected:** 5 个测试通过。

> 如果 Testcontainers 启动失败(Docker 未运行),先启动 Docker Desktop 再重试。

- [ ] **Step 5: Commit**

```bash
git add src/test/java/com/chen/blog/module/user/controller/LoginControllerIntegrationTest.java \
        src/test/resources/test-users.sql
git commit -m "test: 添加 LoginController 集成测试,覆盖核心登录链路"
```

---

## Task 4.3: 编写权限拦截集成测试

**Files:**
- Create: `src/test/java/com/chen/blog/common/satoken/SaInterceptorIntegrationTest.java`

- [ ] **Step 1: 写测试**

```java
package com.chen.blog.common.satoken;

import com.chen.blog.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@Sql("/test-users.sql")
class SaInterceptorIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymous_endpoint_should_pass_without_login() throws Exception {
        // 假设 tb_resource 中 /articles 配置为匿名可访问(请按实际数据库配置调整)
        mockMvc.perform(get("/articles"))
                .andExpect(jsonPath("$.flag").value(true));
    }

    @Test
    void protected_endpoint_should_return_40001_without_login() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(jsonPath("$.code").value(40001));
    }
}
```

> ⚠️ 该测试依赖 `tb_resource` 表中的实际配置。如果数据未准备好,可以在 `test-users.sql` 中追加测试用资源配置。

- [ ] **Step 2: 运行测试**

```bash
mvn test -Dtest=SaInterceptorIntegrationTest -o 2>&1 | tail -20
```

**Expected:** 2 个测试通过。

- [ ] **Step 3: Commit**

```bash
git add src/test/java/com/chen/blog/common/satoken/SaInterceptorIntegrationTest.java
git commit -m "test: 添加 SaInterceptor 集成测试,覆盖匿名与登录拦截"
```

---

## Task 4.4: 准备 Redis 上线迁移脚本

**Files:**
- Create: `scripts/migrate-redis-session.sh`

- [ ] **Step 1: 写脚本**

```bash
#!/bin/bash
# Redis Session Key 迁移脚本
# 用于 sa-token 上线后清空旧 spring-session 数据

set -e

REDIS_HOST=${REDIS_HOST:-127.0.0.1}
REDIS_PORT=${REDIS_PORT:-6379}
REDIS_PASS=${REDIS_PASS:-}

AUTH_PARAM=""
if [ -n "$REDIS_PASS" ]; then
    AUTH_PARAM="-a $REDIS_PASS"
fi

echo "==> Backing up old Session keys..."
redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM --scan --pattern "renzs-blog:session:*" > backup-$(date +%Y%m%d-%H%M%S).txt
echo "Backup saved."

echo "==> Deleting old Session keys..."
redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM --scan --pattern "renzs-blog:session:*" | xargs -L 100 redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM DEL

echo "==> Verifying..."
COUNT=$(redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM --scan --pattern "renzs-blog:session:*" | wc -l)
echo "Remaining old session keys: $COUNT (expected 0)"

echo "==> sa-token keys current:"
redis-cli -h $REDIS_HOST -p $REDIS_PORT $AUTH_PARAM --scan --pattern "satoken:*" | wc -l
```

- [ ] **Step 2: 给执行权限**

```bash
chmod +x scripts/migrate-redis-session.sh
```

- [ ] **Step 3: Commit**

```bash
git add scripts/migrate-redis-session.sh
git commit -m "ops: 添加 Redis Session 迁移脚本"
```

---

## Task 4.5: 跑全部测试验证

**Files:** 无文件改动

- [ ] **Step 1: 跑全部测试**

```bash
cd D:/桌面/blog-master/renzs-blog-satoken
mvn test -o 2>&1 | tail -50
```

**Expected:** 全部测试通过。如有失败,根据日志修复。

- [ ] **Step 2: 跑覆盖率(可选)**

如果项目已配置 jacoco:

```bash
mvn test jacoco:report -o
open target/site/jacoco/index.html
```

**Expected:** `common/satoken` 包覆盖率 ≥ 80%。

- [ ] **Step 3: 如有修复,Commit**

```bash
git add -A
git commit -m "test: 修复测试失败的边缘场景"
```

---

# Phase 5: 文档与收尾

## Task 5.1: 更新 README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: 在 README 中添加鉴权机制章节**

在 README 适当位置(通常在"技术栈"或"功能特性"附近)插入:

```markdown
## 鉴权机制

本项目使用 [sa-token](https://sa-token.cc) 1.39.0 作为鉴权框架。

- **Token 模式**:Cookie 模式(JSESSIONID)
- **会话存储**:Redis(`satoken:*` 前缀)
- **权限模型**:动态 URL 权限,从 `tb_resource` 表加载
- **密码加密**:Hutool BCrypt

### 登录接口

- `POST /api/login` — 账号密码登录
- `POST /api/logout` — 注销
- `GET /users/current` — 获取当前登录用户

### 业务码

- `40001` — 未登录
- `40300` — 权限不足
- `52002` — 用户名不存在
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: 更新 README,添加 sa-token 鉴权说明"
```

---

## Task 5.2: 创建迁移上线手册

**Files:**
- Create: `docs/sa-token-migration.md`

- [ ] **Step 1: 写上线手册**

```markdown
# Sa-Token 上线手册

## 1. 上线前检查

- [ ] 全部测试通过(`mvn test`)
- [ ] 全局无 `org.springframework.security` 引用
- [ ] 已通知用户:上线后需要重新登录

## 2. 上线步骤

### 2.1 备份 Redis Session
```bash
ssh prod-redis "redis-cli --scan --pattern 'renzs-blog:session:*'" > session-backup-$(date +%Y%m%d).txt
```

### 2.2 部署新版本
```bash
mvn clean package -DskipTests
scp target/renzs-blog-*.jar prod-server:/opt/blog/
ssh prod-server "systemctl restart blog"
```

### 2.3 清空旧 Session Key
```bash
bash scripts/migrate-redis-session.sh
```

### 2.4 验证
```bash
curl -X POST http://prod/api/login -d "username=test&password=xxx"
# 应返回 Set-Cookie: JSESSIONID=...
```

## 3. 回滚方案

### 3.1 触发条件
- 登录失败率 > 5%
- 用户反馈"无法登录"超 3 例

### 3.2 回滚步骤
```bash
ssh prod-server "systemctl stop blog"
git checkout v1.x.x
mvn clean package -DskipTests
scp target/renzs-blog-*.jar prod-server:/opt/blog/
ssh prod-server "redis-cli --scan --pattern 'satoken:*' | xargs redis-cli DEL"
ssh prod-server "systemctl start blog"
```

## 4. FAQ

**Q: 上线后所有用户都被踢了?**
A: 这是预期行为。旧 Spring Session 与新 sa-token 数据不兼容,所有用户需重新登录。

**Q: 第三方登录(QQ/微博/Gitee)还能用吗?**
A: 可以。`AbstractSocialLoginStrategyImpl` 已改造为使用 `StpUtil.login()`。
```

- [ ] **Step 2: Commit**

```bash
git add docs/sa-token-migration.md
git commit -m "docs: 添加 sa-token 上线手册与回滚方案"
```

---

## Task 5.3: 创建 CHANGELOG 与打 tag

**Files:**
- Create: `CHANGELOG.md`

- [ ] **Step 1: 写 CHANGELOG**

```markdown
# Changelog

## [2.0.0] - 2026-06-09

### Changed
- **重大变更**:鉴权框架从 Spring Security 5.4.x 迁移到 sa-token 1.39.0
- 密码加密工具从 `spring-security-crypto.BCrypt` 改为 Hutool BCrypt
- 会话存储从 Spring Session(`renzs-blog:session:*`)迁移到 sa-token(`satoken:*`)

### Added
- 自实现登录接口 `POST /api/login` 与 `POST /api/logout`
- 新增 `common/satoken` 包:`SaTokenConfig`、`StpInterfaceImpl`、`ResourceRoleCache`、`SaTokenExceptionHandler`
- 新增登录业务层 `LoginService` / `LoginServiceImpl`
- 新增 Testcontainers 集成测试基础设施

### Removed
- 移除 `spring-boot-starter-security` 依赖
- 移除 `spring-session-data-redis` 依赖
- 删除 8 个 Spring Security 配置和 Handler 类

### Migration Notes
- 上线后所有在线用户会被踢出,需要重新登录(已在迁移手册说明)
- 前端代码、数据库 schema 均无需改动
```

- [ ] **Step 2: Commit 并打 tag**

```bash
git add CHANGELOG.md
git commit -m "docs: 添加 CHANGELOG v2.0.0"
git tag -a v2.0.0 -m "Release v2.0.0: 迁移至 sa-token"
git log --oneline -10
```

**Expected:** 看到完整的迁移 commit 链,最新 tag 为 `v2.0.0`。

---

## 验收清单(全部 Phase 完成后)

- [ ] `mvn clean package` 通过
- [ ] `mvn test` 全部通过
- [ ] `grep -rn "org.springframework.security" src/main/java` 输出为空
- [ ] `grep -rn "SecurityContextHolder" src/main/java` 输出为空
- [ ] 启动应用后 Postman 跑通登录 → 当前用户 → 登出
- [ ] 前端项目运行无报错(浏览器手动验证)
- [ ] 数据库 8 张 RBAC 表数据未变更
- [ ] Redis 中可见 `satoken:*` Key
- [ ] `README.md` 包含 sa-token 章节
- [ ] git tag `v2.0.0` 存在
