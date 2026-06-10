# Changelog

本项目所有显著变更将记录在此文件中。

格式参考 [Keep a Changelog](https://keepachangelog.com/),版本号遵循 [Semantic Versioning](https://semver.org/)。

## [2.0.0] - 2026-06-10

### ⚠️ Breaking Changes

- **鉴权框架**: Spring Security 5.4.x → sa-token 1.39.0
- **会话存储**: Spring Session Redis(`renzs-blog:session:*`)→ sa-token Redis(`satoken:*`)
- **上线影响**: 所有在线用户会被踢出,需重新登录(详见 `docs/sa-token-migration.md`)

### Added

- 新增 `common/satoken` 包,包含 sa-token 核心集成:
  - `SaTokenConfig`: SaInterceptor 注册,实现动态 URL 鉴权
  - `StpInterfaceImpl`: 角色查询实现,接入现有 `RoleDao`
  - `ResourceRoleCache`: URL-角色映射缓存,替代 `FilterInvocationSecurityMetadataSourceImpl`
  - `SaTokenExceptionHandler`: 鉴权异常映射为统一 `Result`
- 自实现登录接口:
  - `LoginController`: `POST /login` + `POST /logout`(URL 与原 Spring Security 一致)
  - `LoginService` + `LoginServiceImpl`: 登录业务逻辑(含密码校验、禁用检查、异步更新)
  - `LoginVO`: 登录请求 VO
- 新增测试基础设施:
  - Testcontainers Redis 依赖
  - 14 个测试用例覆盖核心鉴权逻辑
- 运维脚本: `scripts/migrate-redis-session.sh`(Redis Session Key 迁移)
- 文档:
  - `README.md`: 项目说明
  - `docs/sa-token-migration.md`: 上线手册与回滚方案
  - `docs/superpowers/specs/`: 迁移设计文档
  - `docs/superpowers/plans/`: 实施计划

### Changed

- 密码加密工具: `spring-security-crypto.BCrypt` → `cn.hutool.crypto.digest.BCrypt`(API 完全兼容)
- `UserDetailDTO`: 移除 `implements UserDetails`,改为普通 `Serializable` POJO
- `UserUtils.getLoginUser()`: 对外签名不变,内部从读 `SecurityContextHolder` 改为读 `StpUtil.getTokenSession()`
- `UserDetailsServiceImpl`: 移除 `implements UserDetailsService`,`loadUserByUsername` 返回类型改为 `UserDetailDTO`
- `AbstractSocialLoginStrategyImpl`: 第三方登录改用 `StpUtil.login()`
- `NoticeWebSocketServiceImpl`: WebSocket 握手鉴权改用 sa-token API(`StpUtil.getLoginIdByToken`)
- `UserInfoServiceImpl`: 在线用户查询改用 sa-token(`StpUtil.searchTokenValue` + `kickout`)
- `RoleServiceImpl` / `ResourceServiceImpl`: 清理对 `FilterInvocationSecurityMetadataSourceImpl` 的依赖
- sa-token 登录 id 统一为 `userInfoId`(与 `StpInterfaceImpl.getRoleList` 入参对齐)

### Removed

- Maven 依赖:
  - `spring-boot-starter-security`
  - `spring-session-data-redis`
- 配置:
  - `spring.session.store-type=redis`
  - `server.servlet.session.timeout/cookie.max-age`
- 8 个 Spring Security 类:
  - `common/config/WebSecurityConfig.java`
  - `common/handler/AuthenticationEntryPointImpl.java`
  - `common/handler/AccessDeniedHandlerImpl.java`
  - `common/handler/AuthenticationSuccessHandlerImpl.java`
  - `common/handler/AuthenticationFailHandlerImpl.java`
  - `common/handler/LogoutSuccessHandlerImpl.java`
  - `common/handler/FilterInvocationSecurityMetadataSourceImpl.java`
  - `common/handler/AccessDecisionManagerImpl.java`

### Migration Notes

- **前端代码**: 零改动(Cookie 名沿用 `JSESSIONID`,登录 URL 与响应格式不变)
- **数据库 schema**: 零改动(8 张 RBAC 表全部沿用)
- **业务代码**: 30+ 处 `UserUtils.getLoginUser()` 调用零改动
- **第三方登录**: QQ/微博/Gitee 接口签名与响应格式不变

详见 `docs/sa-token-migration.md`。

## [1.x.x] - 2026-06-08 及之前

继承自 `renzs-blog`(Spring Security 版本),不在本仓库追溯。
