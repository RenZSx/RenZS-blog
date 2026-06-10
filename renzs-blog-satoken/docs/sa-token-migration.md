# sa-token 上线手册

本手册指导从 Spring Security 版本(`renzs-blog`)迁移到 sa-token 版本(`renzs-blog-satoken`)的上线操作。

## 1. 上线前检查

- [ ] 全部测试通过(`mvn test`)
- [ ] 全局无 `org.springframework.security` 真实代码引用
- [ ] 已通知用户上线后需要重新登录
- [ ] 选定上线时间窗(建议非高峰时段)
- [ ] 准备好回滚方案和旧版本 jar 包

## 2. 上线步骤

### 2.1 备份 Redis Session

```bash
ssh prod-redis "redis-cli --scan --pattern 'renzs-blog:session:*'" \
  > session-backup-$(date +%Y%m%d-%H%M%S).txt
```

### 2.2 部署新版本

```bash
mvn clean package -DskipTests
scp target/renzs-blog-*.jar prod-server:/opt/blog/renzs-blog-satoken.jar
ssh prod-server "systemctl restart blog"
```

观察启动日志,确认看到:
- `Started BlogApplication in X seconds`
- 无 `org.springframework.security` 报错
- sa-token banner 输出

### 2.3 清空旧 Spring Session Key

```bash
bash scripts/migrate-redis-session.sh
# 或在生产环境:
REDIS_HOST=8.137.86.224 REDIS_PASS=169832 bash scripts/migrate-redis-session.sh
```

脚本会:
- 备份旧 Key 到 `backup-spring-session-{timestamp}.txt`
- 删除所有 `renzs-blog:session:*` Key
- 验证清理结果

### 2.4 功能验证

```bash
# 1. 登录测试
curl -X POST http://prod/login \
  -d "username=test_user&password=12345678" \
  -c cookies.txt -v

# 应返回:
# - HTTP 200
# - Set-Cookie: JSESSIONID=xxx
# - Response: {"flag":true,"code":20000,"data":{...}}

# 2. 当前用户测试(带 Cookie)
curl http://prod/users/current -b cookies.txt
# 应返回用户信息

# 3. 登出测试
curl -X POST http://prod/logout -b cookies.txt
# 应返回 {"flag":true,"code":20000}

# 4. 验证 Redis 中已有 satoken:* Key
ssh prod-redis "redis-cli --scan --pattern 'satoken:*' | head -5"
```

### 2.5 监控指标

观察前 30 分钟:
- 登录接口失败率(预期 < 1%)
- HTTP 5xx 错误率(预期 < 0.1%)
- 用户投诉(预期为 0,迁移已通知)

## 3. 回滚方案

### 3.1 触发条件

上线后 30 分钟内任一发生:
- 登录失败率 > 5%
- 用户反馈"无法登录"超 3 例
- 关键业务接口 5xx 错误率 > 1%

### 3.2 回滚步骤

```bash
# 1. 停服
ssh prod-server "systemctl stop blog"

# 2. 切回旧版本 jar
ssh prod-server "ln -sf /opt/blog/renzs-blog.jar.backup /opt/blog/renzs-blog.jar"

# 3. 清空 sa-token Key,避免新旧并存
ssh prod-redis "redis-cli --scan --pattern 'satoken:*' | xargs redis-cli DEL"

# 4. 启动旧版本
ssh prod-server "systemctl start blog"

# 5. 验证旧版本登录正常
curl -X POST http://prod/login -d "username=test_user&password=12345678" -v
# 应看到 Set-Cookie: JSESSIONID=xxx (会被旧版本 Spring Session 接管)
```

### 3.3 回滚后清理

旧版本回滚后:
- 用户重新登录,旧版本会写入 `renzs-blog:session:*`
- sa-token Key 已被清空,不会产生干扰
- 后续修复 sa-token 版本问题后再次上线

## 4. FAQ

**Q: 上线后所有用户都被踢了?**
A: 这是预期行为。旧 Spring Session 数据与新 sa-token 数据不兼容,所有用户需重新登录。已在 README 和上线公告中说明。

**Q: 第三方登录(QQ/微博/Gitee)还能用吗?**
A: 可以。`AbstractSocialLoginStrategyImpl` 已改造为使用 `StpUtil.login()`。前端代码无需改动。

**Q: 业务侧的 `UserUtils.getLoginUser()` 行为有变化吗?**
A: 没有变化。`UserUtils` 对外签名保持不变,内部改为读 SaSession。业务侧 30+ 处调用零改动。

**Q: WebSocket 通知功能受影响吗?**
A: 不受影响。WebSocket 握手阶段从 Cookie 中读 sa-token 值,通过 `StpUtil.getLoginIdByToken` 反查 loginId,行为与原 Spring Security 版本一致。

**Q: Redis Key 是否会和其他业务冲突?**
A: 不会。sa-token Key 前缀为 `satoken:*`,业务侧使用的是 `article:*`、`comment:*` 等不同前缀,无冲突。

**Q: 如何在权限变更后刷新缓存?**
A: 调用 `ResourceRoleCache.refresh()`。`RoleServiceImpl` 和 `ResourceServiceImpl` 已经在更新角色/资源时自动调用刷新。

## 5. 后续优化建议

迁移完成后,可以考虑的优化项(非本次迁移范围):
- `LoginServiceImpl.updateLoginInfo()` 的 `@Async` 自调用陷阱(继承自原 `AuthenticationSuccessHandlerImpl` 的相同问题)
- 集成测试覆盖完整登录链路(目前轻量集成测试只覆盖配置加载)
- 引入 Spring Boot Actuator + Prometheus 监控
