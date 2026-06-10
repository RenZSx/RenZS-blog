# renzs-blog 鉴权迁移设计文档：Spring Security → sa-token

**版本**：1.0
**日期**：2026-06-09
**负责人**：ChenFY
**状态**：待评审

---

## 1. 背景与目标

### 1.1 背景

renzs-blog 当前使用 Spring Security 5.4.x 实现鉴权，主要特征：

- **会话机制**：基于 Servlet Session + Spring Session Redis（非 JWT）
- **授权模式**：动态 URL 权限（`tb_resource` 表存储 URL ↔ 角色映射）
- **权限注解使用量**：0 处（未使用 `@PreAuthorize` 等）
- **业务侧耦合点**：仅 `UserUtils.getLoginUser()` 一个工具方法（30 处调用）
- **直接使用 `SecurityContextHolder` 的代码**：仅 3 处

### 1.2 目标

将鉴权框架从 Spring Security 迁移到 sa-token 1.39.0，达成：

- **代码简化**：删除 8 个 Spring Security 配置/Handler 类（约 300 行模板代码）
- **学习成本下降**：sa-token API 更直观，新成员上手更快
- **扩展性增强**：未来支持 JWT、SSO、临时 Token 等场景更容易
- **零业务侵入**：业务代码、数据库表、前端代码均不改动

### 1.3 非目标

本次迁移**不做**以下事情：

- 不引入 JWT（保持 Cookie 模式）
- 不重构现有 RBAC 数据模型
- 不重构 Controller 接口签名
- 不优化业务代码中的其他历史问题

---

## 2. 现状分析

### 2.1 Spring Security 使用清单

| 类别 | 文件 | 行数定位 |
|---|---|---|
| 配置 | `common/config/WebSecurityConfig.java` | 全文件 |
| 元数据源 | `common/handler/FilterInvocationSecurityMetadataSourceImpl.java` | 25-85 |
| 决策器 | `common/handler/AccessDecisionManagerImpl.java` | 22-47 |
| 入口异常 | `common/handler/AuthenticationEntryPointImpl.java` | 23-31 |
| 拒绝异常 | `common/handler/AccessDeniedHandlerImpl.java` | 23-31 |
| 登录成功 | `common/handler/AuthenticationSuccessHandlerImpl.java` | 29-56 |
| 登录失败 | `common/handler/AuthenticationFailHandlerImpl.java` | 22-29 |
| 登出成功 | `common/handler/LogoutSuccessHandlerImpl.java` | 22-30 |
| UserDetails 实现 | `module/user/dto/UserDetailDTO.java` | 24-169 |
| UserDetailsService | `module/user/service/impl/UserDetailsServiceImpl.java` | 37-110 |
| SecurityContext 写入 | `module/user/strategy/impl/AbstractSocialLoginStrategyImpl.java` | 79-80 |
| SecurityContext 读取 | `common/util/UserUtils.java` | 17-33 |

### 2.2 业务调用统计

- `UserUtils.getLoginUser()` 调用：**30 处**
- `SecurityContextHolder` 直接调用：**3 处**（其中 2 处在 `UserUtils`、1 处在第三方登录）
- 权限注解（`@PreAuthorize` 等）：**0 处**

### 2.3 数据库现状

8 张 RBAC 相关表均无 Spring Security 专属字段：

| 表 | Security 专属字段 |
|---|---|
| `tb_user_auth` | 无 |
| `tb_user_info` | 无（`is_disable` 为业务字段） |
| `tb_role` | 无 |
| `tb_resource` | 无（`is_anonymous` 为业务字段） |
| `tb_menu` / `tb_user_role` / `tb_role_resource` / `tb_role_menu` | 无 |

**结论**：数据库 schema 零改动。

### 2.4 前端契约现状

- **Cookie 名**：`JSESSIONID`
- **登录接口**：`POST /api/login`，`application/x-www-form-urlencoded`
- **登出接口**：`POST /api/logout`
- **当前用户**：`GET /api/users/current`
- **第三方登录**：`POST /api/users/oauth/{qq|weibo|gitee}`
- **业务码**：`40001 = NO_LOGIN`，触发前端 `handleExpiredSession()`
- **CORS**：`withCredentials: true` + `allowCredentials(true)`

**结论**：前端代码零改动（采用 Cookie 模式后）。

---

## 3. 关键决策

| 决策项 | 选择 | 理由 |
|---|---|---|
| Token 模式 | Cookie 模式 | 前端零改动，最小化变更范围 |
| Redis Key 迁移 | 上线时清空旧 Key | 简单安全，所有用户重新登录即可 |
| 代码仓库 | 新仓库 `renzs-blog-sa-token` | 隔离风险，便于对比 |
| 仓库创建方式 | 拷贝代码新起项目 | 不保留原 Git 历史，干净起步 |
| 项目路径 | `D:\桌面\blog-master\renzs-blog-sa-token` | 与原项目并列，方便对比 |
| sa-token 版本 | 1.39.0（最新稳定） | 与 Spring Boot 2.4.1 兼容，长期维护 |
| 权限实现 | 路由拦截器 + 动态鉴权 | 与现状逻辑一致，沿用 `tb_resource` |
| 验证方式 | 补充自动化测试 | 8 个单元 + 12 个集成 |

---

## 4. 架构设计

### 4.1 整体拓扑

```
┌────────────────────────────────────────────────────────┐
│  renzs-blog-sa-token (新仓库 / Spring Boot 2.4.1)      │
│                                                        │
│  ┌──────────────┐   ┌────────────────────────────┐    │
│  │  Controller  │──▶│ SaInterceptor (路由拦截)   │    │
│  │  (业务接口)  │   │ + 动态 URL 鉴权             │    │
│  └──────┬───────┘   └────────────┬───────────────┘    │
│         │                        │                     │
│         │   ┌────────────────────▼──────────────┐     │
│         │   │  StpInterface 实现                │     │
│         │   │  - getPermissionList(读 tb_resource) │  │
│         │   │  - getRoleList(读 tb_role)        │     │
│         │   └────────────────────┬──────────────┘     │
│         │                        │                     │
│  ┌──────▼───────────┐    ┌──────▼────────┐            │
│  │ UserUtils        │    │ RoleDao (复用) │            │
│  │ - getLoginUser() │    └────────────────┘            │
│  │ (内部改 StpUtil) │                                   │
│  └──────────────────┘                                   │
└──────────────┬─────────────────────────────────────────┘
               │
               ▼
       ┌───────────────┐
       │ Redis         │
       │ satoken:* Key │  (sa-token 持久化)
       └───────────────┘
```

### 4.2 核心架构原则

1. **对外契约不变**：所有 HTTP 接口、Cookie 名（沿用 JSESSIONID）、响应格式保持原状 → 前端零改动
2. **业务代码不变**：`UserUtils.getLoginUser()` 签名保持不变，内部改用 `StpUtil` → 30 处业务调用零改动
3. **数据模型不变**：8 张 RBAC 表结构、`UserDetailDTO` 字段全部保留
4. **隔离边界清晰**：sa-token 配置和 StpInterface 实现集中在 `common/satoken/` 新包

### 4.3 文件变更清单

#### 删除（8 个）

```
common/config/WebSecurityConfig.java
common/handler/AuthenticationEntryPointImpl.java
common/handler/AccessDeniedHandlerImpl.java
common/handler/AuthenticationSuccessHandlerImpl.java
common/handler/AuthenticationFailHandlerImpl.java
common/handler/LogoutSuccessHandlerImpl.java
common/handler/FilterInvocationSecurityMetadataSourceImpl.java
common/handler/AccessDecisionManagerImpl.java
```

#### 新增（9 个）

```
common/satoken/SaTokenConsts.java            (常量定义)
common/satoken/SaTokenConfig.java            (SaInterceptor 注册 + 路由规则)
common/satoken/StpInterfaceImpl.java         (动态权限/角色查询)
common/satoken/ResourceRoleCache.java        (URL-角色映射缓存)
common/satoken/ResourceRoleDTO.java          (缓存条目 DTO)
common/satoken/SaTokenExceptionHandler.java  (NotLoginException 等映射)
module/user/controller/LoginController.java  (自实现 /login /logout)
module/user/service/LoginService.java        (登录业务接口)
module/user/service/impl/LoginServiceImpl.java (登录业务实现)
```

#### 改造（4 个）

```
common/util/UserUtils.java                          (内部改 StpUtil, 对外签名不变)
module/user/dto/UserDetailDTO.java                  (移除 implements UserDetails)
module/user/service/impl/UserDetailsServiceImpl.java (改名为 UserAuthQueryServiceImpl)
module/user/strategy/impl/AbstractSocialLoginStrategyImpl.java (第 79-80 行替换)
```

---

## 5. 鉴权流程详细设计

### 5.1 登录流程

```
前端                                后端
 │  POST /api/login                  │
 │  username=xxx&password=xxx        │
 ├──────────────────────────────────▶│ LoginController.login()
 │                                   │  ├─ UserAuthService.loadByUsername()
 │                                   │  │  └─ 查 tb_user_auth + tb_user_info
 │                                   │  ├─ BCrypt.checkpw(明文, 数据库密文)
 │                                   │  ├─ ❌ 失败 → throw BizException
 │                                   │  ├─ ✅ 成功:
 │                                   │  │   ├─ StpUtil.login(userInfoId)
 │                                   │  │   ├─ StpUtil.getSession().set("userDetail", dto)
 │                                   │  │   └─ 异步更新 last_login_time / ip
 │  Set-Cookie: JSESSIONID=xxx       │  │
 │  Result<UserInfoDTO>              │◀─┘
 │◀──────────────────────────────────│
```

**关键点**：
- Cookie 名沿用 `JSESSIONID`（sa-token 配置 `token-name: JSESSIONID`）
- `StpUtil.login(userInfoId)` 第一参数用 `userInfoId`（Integer），与 `UserDetailDTO.userInfoId` 对齐
- 登录成功后整个 `UserDetailDTO` 塞到 sa-token Session，供 `UserUtils.getLoginUser()` 取回
- 响应不带 token 字段，沿用现有 `Result<UserInfoDTO>` 结构

### 5.2 第三方登录改造

`AbstractSocialLoginStrategyImpl.java:79-80` 仅改 2 行：

```java
// 改前
UsernamePasswordAuthenticationToken auth =
    new UsernamePasswordAuthenticationToken(userDetailDTO, null, userDetailDTO.getAuthorities());
SecurityContextHolder.getContext().setAuthentication(auth);

// 改后
StpUtil.login(userDetailDTO.getUserInfoId());
StpUtil.getSession().set(SaTokenConsts.USER_DETAIL_KEY, userDetailDTO);
```

### 5.3 鉴权拦截流程

```java
// SaTokenConfig.java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new SaInterceptor(handler -> {
        SaRouter.match("/**")
            .notMatch("/login", "/logout", "/api/users/register", "/error", "/swagger-ui/**")
            .check(r -> {
                String url = SaHolder.getRequest().getRequestPath();
                String method = SaHolder.getRequest().getMethod();

                ResourceRoleDTO resource = resourceRoleCache.match(url, method);

                // 匿名资源放行
                if (resource == null || resource.isAnonymous()) return;

                // 非匿名: 校验登录
                StpUtil.checkLogin();

                // 校验角色: 用户角色 ∩ 资源角色 ≠ ∅
                List<String> userRoles = StpUtil.getRoleList();
                List<String> requiredRoles = resource.getRoleLabels();
                if (Collections.disjoint(userRoles, requiredRoles)) {
                    throw new NotPermissionException("权限不足");
                }
            });
    })).addPathPatterns("/**");
}
```

### 5.4 StpInterface 实现

```java
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource private RoleDao roleDao;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Integer userInfoId = Integer.parseInt(loginId.toString());
        // 复用现有 SQL: RoleDao.listRolesByUserInfoId
        return roleDao.listRolesByUserInfoId(userInfoId)
                .stream().map(Role::getRoleLabel).collect(toList());
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本项目按角色鉴权,不用细粒度权限,返回空
        return Collections.emptyList();
    }
}
```

### 5.5 资源-角色映射缓存

```java
@Component
public class ResourceRoleCache {
    @Resource private RoleDao roleDao;
    private volatile List<ResourceRoleDTO> cache;

    @PostConstruct
    public void load() {
        this.cache = roleDao.listResourceRoles();
    }

    public ResourceRoleDTO match(String url, String method) {
        AntPathMatcher matcher = new AntPathMatcher();
        return cache.stream()
                .filter(r -> r.getRequestMethod().equalsIgnoreCase(method))
                .filter(r -> matcher.match(r.getUrl(), url))
                .findFirst().orElse(null);
    }

    public void clear() {
        load();   // 权限变更时调用,与原 clearDataSource() 等价
    }
}
```

### 5.6 异常映射

```java
@RestControllerAdvice
public class SaTokenExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.fail(StatusCodeEnum.NO_LOGIN);  // 业务码 40001
    }

    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNoPermission(NotPermissionException e) {
        return Result.fail(StatusCodeEnum.NO_PERMISSION);
    }

    @ExceptionHandler(NotRoleException.class)
    public Result<?> handleNoRole(NotRoleException e) {
        return Result.fail(StatusCodeEnum.NO_PERMISSION);
    }
}
```

### 5.7 UserUtils 改造（关键）

```java
// 改前
public static UserDetailDTO getLoginUser() {
    return (UserDetailDTO) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
}

// 改后
public static UserDetailDTO getLoginUser() {
    return (UserDetailDTO) StpUtil.getSession()
            .get(SaTokenConsts.USER_DETAIL_KEY);
}
```

**业务侧 30 处调用零改动**。

---

## 6. 配置与依赖

### 6.1 sa-token 配置（application-dev.yml）

**删除**：

```yaml
spring:
  session:
    store-type: redis
    timeout: 2592000
    redis:
      namespace: renzs-blog:session
```

**新增**：

```yaml
sa-token:
  token-name: JSESSIONID          # 沿用旧 Cookie 名
  timeout: 2592000                 # 30 天
  active-timeout: -1               # 永不冻结
  is-concurrent: true              # 允许并发登录
  is-share: true                   # 共享同 token
  max-login-count: 20              # 与原 maximumSessions(20) 等价
  token-style: uuid
  is-log: false
  is-read-cookie: true             # Cookie 模式
  is-read-header: false
  is-print: false

  alone-redis:
    database: 0
    host: ${spring.redis.host}
    port: ${spring.redis.port}
    password: ${spring.redis.password}
    timeout: 10s
```

### 6.2 pom.xml 依赖

**移除**：

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.session</groupId>
  <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

**新增**：

```xml
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-spring-boot-starter</artifactId>
  <version>1.39.0</version>
</dependency>
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-redis-jackson</artifactId>
  <version>1.39.0</version>
</dependency>
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-pool2</artifactId>
</dependency>
```

**保留**：

- `spring-boot-starter-data-redis`（业务侧仍在用 RedisTemplate）
- `BCrypt` 工具类（密码加密继续使用）

### 6.3 Redis Key 迁移

- sa-token 默认 Key 前缀：`satoken:login:token:*`、`satoken:login:session:*`
- 与原 `renzs-blog:session:*` Key 天然不冲突
- 复用同一 Redis 实例

**上线步骤**：

```bash
# 1. 部署前: 备份 Session Key
redis-cli --scan --pattern "renzs-blog:session:*" > backup.txt

# 2. 部署新版本

# 3. 部署后: 清空旧 Session Key
redis-cli --scan --pattern "renzs-blog:session:*" | xargs redis-cli DEL

# 4. 用户访问任意需要登录的接口 → 收到 40001 → 重新登录
```

---

## 7. 测试设计

### 7.1 测试金字塔

```
        ┌───────────────────┐
        │ E2E 手动验证 (3)   │
        ├───────────────────┤
        │ 集成测试 (12)      │   MockMvc + Testcontainers Redis
        ├───────────────────┤
        │ 单元测试 (8)       │   Mockito
        └───────────────────┘
```

### 7.2 单元测试清单

| # | 测试类 | 验证内容 |
|---|---|---|
| 1 | `StpInterfaceImplTest` | 角色查询调用 RoleDao,返回 roleLabel 列表 |
| 2 | `StpInterfaceImplTest` | 权限列表返回空集合 |
| 3 | `ResourceRoleCacheTest` | 启动加载,匹配 URL+Method |
| 4 | `ResourceRoleCacheTest` | clear() 刷新缓存 |
| 5 | `ResourceRoleCacheTest` | 匿名资源识别 |
| 6 | `UserUtilsTest` | 已登录返回 UserDetailDTO |
| 7 | `UserUtilsTest` | 未登录返回 null |
| 8 | `SaTokenExceptionHandlerTest` | 三种异常 → 业务码映射 |

### 7.3 集成测试清单

`@SpringBootTest` + `MockMvc` + `@Testcontainers`(Redis):

| # | 场景 | 期望 |
|---|---|---|
| 1 | POST /api/login 正确账密 | 200 + Set-Cookie JSESSIONID |
| 2 | POST /api/login 错误密码 | 业务码 PASSWORD_ERROR |
| 3 | POST /api/login 不存在用户 | 业务码 USER_NOT_EXIST |
| 4 | GET /api/users/current 已登录 | 200 + 用户信息 |
| 5 | GET /api/users/current 未登录 | 业务码 40001 |
| 6 | POST /api/logout | Cookie 失效 |
| 7 | 普通用户访问 admin 接口 | 业务码 NO_PERMISSION |
| 8 | admin 用户访问 admin 接口 | 200 |
| 9 | 匿名接口未登录访问 | 200 |
| 10 | 第三方登录 QQ 模拟 | StpUtil.isLogin()=true |
| 11 | 并发登录 20 次同账号 | 全部成功 |
| 12 | 并发登录第 21 次同账号 | 最早登录被踢 |

### 7.4 手动 E2E 验证

```
场景1: 完整登录链路
  浏览器 → 登录页输入账密 → 跳首页 → 刷新仍登录 → 退出 → 跳回登录页

场景2: 第三方登录
  点击 QQ 登录 → 回调 → Session 建立 → 后续接口正常

场景3: 权限拦截
  普通用户访问 /admin/** → 无权限提示 → 切 admin → 成功
```

---

## 8. Phase 拆分与产出物

### Phase 1: 基础设施层（0.5 人天）

**产出物**：

- `D:/桌面/blog-master/renzs-blog-sa-token/` 目录已创建
- git init + .gitignore（沿用原项目）
- 首次 commit: `chore: 初始化项目,拷贝自 renzs-blog`
- pom.xml 替换依赖
- application-dev.yml 替换配置

**验收标准**：`mvn dependency:tree` 不再包含 spring-security 和 spring-session-data-redis。

### Phase 2: 鉴权核心层（0.5 人天）

**产出物**：

- `common/satoken/` 包及 6 个类
- `UserUtils.java` 改造
- `UserDetailDTO.java` 改造
- commit: `feat: 引入 sa-token 核心配置与鉴权实现`

**验收标准**：

- `mvn clean compile` 通过
- 项目能成功启动
- 单元测试 8 个全绿

### Phase 3: 业务接口层（0.3 人天）

**产出物**：

- `LoginController` / `LoginService` 自实现
- 第三方登录策略改造
- 删除 8 个旧 Security 类
- commit: `feat: 自实现登录接口,删除 Spring Security 相关类`

**验收标准**：

- 全局搜索 `org.springframework.security` 零结果
- 全局搜索 `SecurityContextHolder` 零结果
- Postman 跑通登录 → 当前用户 → 登出全链路

### Phase 4: 测试与验证（0.5 人天）

**产出物**：

- testcontainers 依赖
- `BaseIntegrationTest.java`
- 4 个单元测试类（8 用例）
- 2 个集成测试类（12 用例）
- `scripts/migrate-redis-session.sh` 清理脚本
- commit: `test: 补充 sa-token 核心鉴权的单元与集成测试`

**验收标准**：

- `mvn test` 全绿,新增代码覆盖率 ≥ 80%
- 手动 E2E 三大场景全通过

### Phase 5: 文档与收尾（0.2 人天）

**产出物**：

- README.md 鉴权章节更新
- `docs/sa-token-migration.md` 上线手册
- `docs/api/login-api.md` 接口契约
- CHANGELOG.md
- git tag v2.0.0
- commit: `docs: 更新 sa-token 迁移相关文档`

**验收标准**：README 明确说明使用 sa-token 1.39.0。

---

## 9. 风险评估与缓解

| # | 风险项 | 概率 | 影响 | 缓解措施 |
|---|---|---|---|---|
| 1 | sa-token 1.39.0 与 Spring Boot 2.4.1 兼容性 | 低 | 高 | Phase 1 首先验证 `mvn compile` 与项目启动 |
| 2 | StpUtil.getSession() 反序列化 UserDetailDTO 失败 | 中 | 高 | 使用 sa-token-redis-jackson，Phase 2 单元测试覆盖 |
| 3 | 第三方登录回调获取不到 Session | 低 | 中 | Phase 3 SocialLoginIntegrationTest 覆盖 |
| 4 | Cookie 模式跨域问题 | 中 | 中 | 保留 `allowCredentials(true)`，验证 OPTIONS 预检 |
| 5 | 动态权限缓存与原行为不一致 | 中 | 高 | Phase 4 准备测试数据，对比新旧行为 |
| 6 | 上线后清空 Redis Key 导致所有用户被踢 | 高(必然) | 低 | 维护公告 + 周末凌晨上线 |
| 7 | 权限变更后缓存未刷新 | 中 | 中 | 角色/资源管理 Controller 调用 `ResourceRoleCache.clear()` |

---

## 10. 回滚方案

### 触发条件（上线后 30 分钟内任一发生）

- 登录接口失败率 > 5%
- 用户反馈"无法登录"超 3 例
- 关键业务接口 5xx 错误率 > 1%

### 回滚步骤

```bash
# 1. 部署回退到旧版本
git checkout v1.x.x && mvn package && 部署

# 2. 清空 sa-token Key
redis-cli --scan --pattern "satoken:*" | xargs redis-cli DEL

# 3. 用户重新登录, 旧版本写回 renzs-blog:session:* Key
```

**关键设计**：新旧 Redis Key 前缀不冲突,回滚无残留风险。

---

## 11. 验收标准（Definition of Done）

完成全部 5 个 Phase 后,需满足:

- [ ] **代码**：全局零 `org.springframework.security` 引用
- [ ] **构建**：`mvn clean package -DskipTests=false` 通过
- [ ] **测试**：单元 8 个 + 集成 12 个全绿,新增代码覆盖率 ≥ 80%
- [ ] **手动验证**：3 大 E2E 场景在本地前后端联调通过
- [ ] **前端**：未做任何代码改动,登录/登出/权限拦截正常
- [ ] **数据库**：未做任何 schema 变更,8 张 RBAC 表数据未动
- [ ] **Redis**：新 Key 前缀 `satoken:*` 正常生成
- [ ] **文档**：README + 迁移文档 + API 文档齐全
- [ ] **版本**：git tag v2.0.0 已打

---

## 12. 时间线总览

```
Day 1 上午: Phase 1 + Phase 2 启动     (基础设施 + sa-token 配置)
Day 1 下午: Phase 2 收尾 + Phase 3     (业务接口改造)
Day 2 上午: Phase 4                    (测试补全)
Day 2 下午: Phase 5 + 手动验证 + 上线  (文档收尾)
```

**合计：2 人天**

---

## 附录 A：术语表

| 术语 | 说明 |
|---|---|
| sa-token | 国产轻量级 Java 鉴权框架，[官网](https://sa-token.cc) |
| StpUtil | sa-token 核心工具类，提供登录/登出/校验 API |
| StpInterface | sa-token 权限/角色查询扩展接口 |
| SaInterceptor | sa-token 路由拦截器 |
| SaRouter | sa-token 链式路由匹配工具 |
| UserDetails | Spring Security 的用户详情接口（迁移后弃用） |
| SecurityContextHolder | Spring Security 上下文持有者（迁移后弃用） |

## 附录 B：参考资料

- sa-token 官方文档：https://sa-token.cc
- sa-token GitHub：https://github.com/dromara/sa-token
- 原项目 Spring Security 配置：`common/config/WebSecurityConfig.java`
