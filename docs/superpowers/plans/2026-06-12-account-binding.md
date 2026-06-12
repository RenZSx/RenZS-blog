# Account Binding Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Bind QQ and email credentials to one `tb_user_info` account so either login method returns the same user after binding.

**Architecture:** Keep the current QQ login endpoint unchanged and add a logged-in QQ binding endpoint. Strengthen email binding so it creates or reuses an email credential for the current `user_info_id`, with conflict checks. Frontend stores binding state, routes QQ OAuth callback either to login or bind mode, and updates the user center controls.

**Tech Stack:** Spring Boot 2.4, MyBatis-Plus, Sa-Token, Hutool BCrypt, Vue 3, Pinia, Vuetify, Vitest.

---

## File Map

- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/vo/EmailVO.java`: add optional password field for QQ-only users binding email login.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java`: add `emailBound` and `qqBound`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserInfoDTO.java`: add `emailBound` and `qqBound`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java`: compute binding state from `tb_user_auth`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/UserAuthService.java`: expose `bindQq(QQLoginVO)`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java`: implement QQ binding and delegate QQ token validation to `QQLoginStrategyImpl`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/strategy/impl/QQLoginStrategyImpl.java`: expose QQ token validation for binding.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/controller/UserAuthController.java`: add `POST /users/oauth/qq/bind`.
- Modify `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserInfoServiceImpl.java`: make `/users/email` create or validate an email credential.
- Create `renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserInfoServiceImplTest.java`: email binding conflict and credential tests.
- Create `renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserAuthServiceImplTest.java`: QQ binding conflict and credential tests.
- Modify `blog-front/blog-vue3/src/api/user.ts`: add `bindQq`, extend `bindEmail` payload with password.
- Modify `blog-front/blog-vue3/src/stores/user.ts`: store `emailBound` and `qqBound`.
- Modify `blog-front/blog-vue3/src/components/model/EmailModel.vue`: show password fields when needed and submit password.
- Modify `blog-front/blog-vue3/src/views/user/User.vue`: add QQ binding button and OAuth bind mode setup.
- Modify `blog-front/blog-vue3/src/components/OauthLogin.vue`: call QQ bind endpoint when in bind mode.
- Create `blog-front/blog-vue3/src/utils/oauthMode.ts`: centralize localStorage keys for OAuth login/bind mode.
- Create `blog-front/blog-vue3/src/utils/oauthMode.spec.ts`: test mode helpers.

## Task 1: Backend DTOs and Binding State

**Files:**
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/vo/EmailVO.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserInfoDTO.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java`

- [ ] **Step 1: Add DTO fields**

In `EmailVO.java`, add:

```java
@ApiModelProperty(name = "password", value = "邮箱登录密码", dataType = "String")
private String password;
```

In `UserDetailDTO.java` and `UserInfoDTO.java`, add:

```java
private Boolean emailBound;
private Boolean qqBound;
```

- [ ] **Step 2: Compute binding state in user detail conversion**

In `UserDetailsServiceImpl.convertUserDetail`, after loading `talkLikeSet`, add:

```java
List<UserAuth> boundAuthList = userAuthDao.selectList(new LambdaQueryWrapper<UserAuth>()
        .select(UserAuth::getLoginType)
        .eq(UserAuth::getUserInfoId, userInfo.getId()));
boolean emailBound = boundAuthList.stream()
        .anyMatch(item -> Objects.equals(item.getLoginType(), LoginTypeEnum.EMAIL.getType()));
boolean qqBound = boundAuthList.stream()
        .anyMatch(item -> Objects.equals(item.getLoginType(), LoginTypeEnum.QQ.getType()));
```

Add the missing import:

```java
import com.chen.blog.common.enums.LoginTypeEnum;
```

Then set the fields in the builder:

```java
.emailBound(emailBound)
.qqBound(qqBound)
```

- [ ] **Step 3: Compile backend**

Run:

```bash
rtk mvn -q -DskipTests compile
```

Expected: build succeeds.

- [ ] **Step 4: Commit**

```bash
rtk git add renzs-blog-satoken/src/main/java/com/chen/blog/module/user/vo/EmailVO.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserDetailDTO.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/dto/UserInfoDTO.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserDetailsServiceImpl.java
rtk git commit -m "feat: expose account binding state"
```

## Task 2: Backend Email Binding Credential

**Files:**
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserInfoServiceImpl.java`
- Test: `renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserInfoServiceImplTest.java`

- [ ] **Step 1: Write focused unit tests**

Create `UserInfoServiceImplTest.java` with Mockito tests for:

```java
@Test
void saveUserEmail_should_reject_email_bound_to_other_user() {
    EmailVO emailVO = EmailVO.builder().email("a@test.com").code("123456").password("abc123").build();
    UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();
    UserAuth existing = UserAuth.builder().id(10).userInfoId(2).username("a@test.com").loginType(LoginTypeEnum.EMAIL.getType()).build();

    when(redisService.get(USER_CODE_KEY + "a@test.com")).thenReturn("123456");
    mockStaticUserUtils(loginUser);
    when(userAuthDao.selectOne(any())).thenReturn(existing);

    BizException ex = assertThrows(BizException.class, () -> service.saveUserEmail(emailVO));
    assertEquals("该邮箱已绑定其他账号", ex.getMessage());
}

@Test
void saveUserEmail_should_insert_email_auth_for_current_user() {
    EmailVO emailVO = EmailVO.builder().email("a@test.com").code("123456").password("abc123").build();
    UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();

    when(redisService.get(USER_CODE_KEY + "a@test.com")).thenReturn("123456");
    mockStaticUserUtils(loginUser);
    when(userAuthDao.selectOne(any())).thenReturn(null);

    service.saveUserEmail(emailVO);

    verify(userInfoDao).updateById(argThat(item ->
            Objects.equals(item.getId(), 1) && Objects.equals(item.getEmail(), "a@test.com")));
    verify(userAuthDao).insert(argThat(item ->
            Objects.equals(item.getUserInfoId(), 1)
                    && Objects.equals(item.getUsername(), "a@test.com")
                    && Objects.equals(item.getLoginType(), LoginTypeEnum.EMAIL.getType())
                    && BCrypt.checkpw("abc123", item.getPassword())));
}
```

Use `mockito-inline` if static mocking of `UserUtils.getLoginUser()` is unavailable; otherwise instantiate the service and set fields by reflection.

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
rtk mvn -q -Dtest=UserInfoServiceImplTest test
```

Expected: tests fail because `saveUserEmail` does not query or insert `tb_user_auth`.

- [ ] **Step 3: Implement email credential binding**

In `UserInfoServiceImpl`, inject `UserAuthDao`:

```java
@Autowired
private UserAuthDao userAuthDao;
```

Add imports:

```java
import cn.hutool.crypto.digest.BCrypt;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.entity.UserAuth;
```

Replace `saveUserEmail` with logic equivalent to:

```java
@Transactional(rollbackFor = Exception.class)
@Override
public void saveUserEmail(EmailVO emailVO) {
    Object cachedCode = redisService.get(USER_CODE_KEY + emailVO.getEmail());
    if (Objects.isNull(cachedCode) || !emailVO.getCode().equals(cachedCode.toString())) {
        throw new BizException("验证码错误！");
    }
    UserDetailDTO loginUser = UserUtils.getLoginUser();
    Integer currentUserInfoId = loginUser.getUserInfoId();
    UserAuth emailAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
            .eq(UserAuth::getUsername, emailVO.getEmail())
            .eq(UserAuth::getLoginType, LoginTypeEnum.EMAIL.getType()));
    if (Objects.nonNull(emailAuth) && !Objects.equals(emailAuth.getUserInfoId(), currentUserInfoId)) {
        throw new BizException("该邮箱已绑定其他账号");
    }
    if (Objects.isNull(emailAuth)) {
        if (StringUtils.isBlank(emailVO.getPassword())) {
            throw new BizException("请设置邮箱登录密码");
        }
        userAuthDao.insert(UserAuth.builder()
                .userInfoId(currentUserInfoId)
                .username(emailVO.getEmail())
                .password(BCrypt.hashpw(emailVO.getPassword(), BCrypt.gensalt()))
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build());
    }
    userInfoDao.updateById(UserInfo.builder()
            .id(currentUserInfoId)
            .email(emailVO.getEmail())
            .build());
}
```

- [ ] **Step 4: Run email binding tests**

Run:

```bash
rtk mvn -q -Dtest=UserInfoServiceImplTest test
```

Expected: tests pass.

- [ ] **Step 5: Commit**

```bash
rtk git add renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserInfoServiceImpl.java renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserInfoServiceImplTest.java
rtk git commit -m "feat: bind email login credential"
```

## Task 3: Backend QQ Binding Endpoint

**Files:**
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/UserAuthService.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/controller/UserAuthController.java`
- Modify: `renzs-blog-satoken/src/main/java/com/chen/blog/module/user/strategy/impl/QQLoginStrategyImpl.java`
- Test: `renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserAuthServiceImplTest.java`

- [ ] **Step 1: Write focused unit tests**

Create `UserAuthServiceImplTest.java` with tests for:

```java
@Test
void bindQq_should_reject_open_id_bound_to_other_user() {
    QQLoginVO qqLoginVO = QQLoginVO.builder().openId("qq-open-id").accessToken("token").build();
    UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();
    UserAuth existing = UserAuth.builder().id(10).userInfoId(2).username("qq-open-id").loginType(LoginTypeEnum.QQ.getType()).build();

    mockStaticUserUtils(loginUser);
    when(userAuthDao.selectOne(any())).thenReturn(existing);

    BizException ex = assertThrows(BizException.class, () -> service.bindQq(qqLoginVO));
    assertEquals("该QQ已绑定其他账号", ex.getMessage());
}

@Test
void bindQq_should_insert_qq_auth_for_current_user() {
    QQLoginVO qqLoginVO = QQLoginVO.builder().openId("qq-open-id").accessToken("token").build();
    UserDetailDTO loginUser = UserDetailDTO.builder().userInfoId(1).build();

    mockStaticUserUtils(loginUser);
    when(userAuthDao.selectOne(any())).thenReturn(null);

    service.bindQq(qqLoginVO);

    verify(userAuthDao).insert(argThat(item ->
            Objects.equals(item.getUserInfoId(), 1)
                    && Objects.equals(item.getUsername(), "qq-open-id")
                    && Objects.equals(item.getPassword(), "token")
                    && Objects.equals(item.getLoginType(), LoginTypeEnum.QQ.getType())));
}
```

Mock `QQLoginStrategyImpl.validateToken(qqLoginVO)` so unit tests do not call external QQ HTTP APIs.

- [ ] **Step 2: Run tests to verify they fail**

Run:

```bash
rtk mvn -q -Dtest=UserAuthServiceImplTest test
```

Expected: tests fail because `bindQq` does not exist.

- [ ] **Step 3: Add service API and implementation**

In `UserAuthService.java`, add:

```java
void bindQq(QQLoginVO qqLoginVO);
```

In `QQLoginStrategyImpl.java`, change `checkQQToken(QQLoginVO qqLoginVO)` from private to public and rename it to:

```java
public void validateToken(QQLoginVO qqLoginVO) {
    // existing QQ token/openId validation body
}
```

Update `getSocialToken` to call `validateToken(qqLoginVO)`.

In `UserAuthServiceImpl.java`, inject:

```java
@Autowired
private QQLoginStrategyImpl qqLoginStrategy;
```

Then add:

```java
@Transactional(rollbackFor = Exception.class)
@Override
public void bindQq(QQLoginVO qqLoginVO) {
    qqLoginStrategy.validateToken(qqLoginVO);
    Integer currentUserInfoId = UserUtils.getLoginUser().getUserInfoId();
    UserAuth qqAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
            .eq(UserAuth::getUsername, qqLoginVO.getOpenId())
            .eq(UserAuth::getLoginType, LoginTypeEnum.QQ.getType()));
    if (Objects.nonNull(qqAuth) && !Objects.equals(qqAuth.getUserInfoId(), currentUserInfoId)) {
        throw new BizException("该QQ已绑定其他账号");
    }
    if (Objects.isNull(qqAuth)) {
        userAuthDao.insert(UserAuth.builder()
                .userInfoId(currentUserInfoId)
                .username(qqLoginVO.getOpenId())
                .password(qqLoginVO.getAccessToken())
                .loginType(LoginTypeEnum.QQ.getType())
                .build());
    }
}
```

- [ ] **Step 4: Add controller endpoint**

In `UserAuthController.java`, add:

```java
@ApiOperation(value = "绑定QQ")
@PostMapping("/users/oauth/qq/bind")
public Result<?> bindQq(@Valid @RequestBody QQLoginVO qqLoginVO) {
    userAuthService.bindQq(qqLoginVO);
    return Result.ok();
}
```

- [ ] **Step 5: Run QQ binding tests**

Run:

```bash
rtk mvn -q -Dtest=UserAuthServiceImplTest test
```

Expected: tests pass.

- [ ] **Step 6: Commit**

```bash
rtk git add renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/UserAuthService.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/service/impl/UserAuthServiceImpl.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/controller/UserAuthController.java renzs-blog-satoken/src/main/java/com/chen/blog/module/user/strategy/impl/QQLoginStrategyImpl.java renzs-blog-satoken/src/test/java/com/chen/blog/module/user/service/impl/UserAuthServiceImplTest.java
rtk git commit -m "feat: bind qq login credential"
```

## Task 4: Frontend API, Store, and OAuth Mode Helper

**Files:**
- Modify: `blog-front/blog-vue3/src/api/user.ts`
- Modify: `blog-front/blog-vue3/src/stores/user.ts`
- Create: `blog-front/blog-vue3/src/utils/oauthMode.ts`
- Test: `blog-front/blog-vue3/src/utils/oauthMode.spec.ts`

- [ ] **Step 1: Write OAuth mode tests**

Create `oauthMode.spec.ts`:

```ts
import { afterEach, describe, expect, it } from 'vitest'
import { clearOauthMode, getOauthMode, saveOauthMode } from './oauthMode'

describe('oauthMode', () => {
  afterEach(() => {
    localStorage.clear()
  })

  it('persists bind mode for qq', () => {
    saveOauthMode({ provider: 'qq', mode: 'bind' })
    expect(getOauthMode()).toEqual({ provider: 'qq', mode: 'bind' })
  })

  it('clears mode after callback', () => {
    saveOauthMode({ provider: 'qq', mode: 'bind' })
    clearOauthMode()
    expect(getOauthMode()).toBeNull()
  })
})
```

- [ ] **Step 2: Implement OAuth mode helper**

Create `oauthMode.ts`:

```ts
export type OauthProvider = 'qq' | 'weibo' | 'gitee'
export type OauthMode = 'login' | 'bind'

export interface SavedOauthMode {
  provider: OauthProvider
  mode: OauthMode
}

const OAUTH_MODE_KEY = 'oauth-mode'

export function saveOauthMode(mode: SavedOauthMode) {
  localStorage.setItem(OAUTH_MODE_KEY, JSON.stringify(mode))
}

export function getOauthMode(): SavedOauthMode | null {
  const raw = localStorage.getItem(OAUTH_MODE_KEY)
  if (!raw) return null

  try {
    const parsed = JSON.parse(raw) as SavedOauthMode
    if (parsed.provider && parsed.mode) return parsed
  } catch (error) {
    clearOauthMode()
  }

  return null
}

export function clearOauthMode() {
  localStorage.removeItem(OAUTH_MODE_KEY)
}
```

- [ ] **Step 3: Update API and store types**

In `api/user.ts`, add:

```ts
export function bindQq(data: { openId: string; accessToken: string }) {
  return request.post('/api/users/oauth/qq/bind', data)
}
```

Change `bindEmail` to:

```ts
export function bindEmail(data: { email: string; code: string; password?: string }) {
  return request.post('/api/users/email', data)
}
```

In `stores/user.ts`, add `emailBound` and `qqBound` to `UserInfo`, state refs, `login`, `logout`, returned values, and persisted paths.

- [ ] **Step 4: Run frontend tests**

Run:

```bash
rtk npm test -- oauthMode
```

Expected: oauth mode tests pass.

- [ ] **Step 5: Commit**

```bash
rtk git add blog-front/blog-vue3/src/api/user.ts blog-front/blog-vue3/src/stores/user.ts blog-front/blog-vue3/src/utils/oauthMode.ts blog-front/blog-vue3/src/utils/oauthMode.spec.ts
rtk git commit -m "feat: track oauth bind mode"
```

## Task 5: Frontend Binding UI and Callback

**Files:**
- Modify: `blog-front/blog-vue3/src/components/model/EmailModel.vue`
- Modify: `blog-front/blog-vue3/src/views/user/User.vue`
- Modify: `blog-front/blog-vue3/src/components/OauthLogin.vue`

- [ ] **Step 1: Update email binding dialog**

In `EmailModel.vue`, add password fields that show when `!userStore.emailBound`:

```vue
<v-text-field
  v-if="!userStore.emailBound"
  v-model="password"
  class="mt-4"
  label="邮箱登录密码"
  placeholder="请设置邮箱登录密码"
  variant="outlined"
  :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
  :type="showPassword ? 'text' : 'password'"
  @click:append="showPassword = !showPassword"
/>
```

Add state:

```ts
const password = ref('')
const showPassword = ref(false)
```

Before submit:

```ts
if (!userStore.emailBound && !password.value.trim()) {
  useToast({ type: 'error', message: '请设置邮箱登录密码' })
  return
}
```

Call:

```ts
const { data } = await bindEmail({
  email: email.value,
  code: code.value,
  password: userStore.emailBound ? undefined : password.value
})
```

On success:

```ts
userStore.email = email.value
userStore.emailBound = true
password.value = ''
```

- [ ] **Step 2: Add QQ binding button in user center**

In `User.vue`, import:

```ts
import config from '@/assets/js/config'
import { saveOauthMode } from '@/utils/oauthMode'
```

Add a `bindQq` function:

```ts
function bindQq() {
  saveOauthMode({ provider: 'qq', mode: 'bind' })
  uiStore.saveLoginUrl(route.fullPath)
  window.open(
    `https://graph.qq.com/oauth2.0/show?which=Login&display=pc&client_id=${config.QQ_APP_ID}&response_type=token&scope=all&redirect_uri=${config.QQ_REDIRECT_URI}`,
    '_self'
  )
}
```

Add a QQ row near the email binding row:

```vue
<div class="mt-4 binding-wrapper">
  <v-text-field
    disabled
    :model-value="userStore.qqBound ? '已绑定' : '未绑定'"
    label="QQ"
    variant="outlined"
  />
  <v-btn
    color="primary"
    variant="text"
    size="small"
    :disabled="userStore.qqBound"
    @click="bindQq"
  >
    {{ userStore.qqBound ? '已绑定' : '绑定QQ' }}
  </v-btn>
</div>
```

- [ ] **Step 3: Route QQ callback to bind endpoint**

In `OauthLogin.vue`, import:

```ts
import { bindQq, qqLogin, weiboLogin, giteeLogin } from '@/api/user'
import { clearOauthMode, getOauthMode } from '@/utils/oauthMode'
import { getCurrentUser } from '@/api/user'
```

When `path.includes('qq')`, branch:

```ts
const oauthMode = getOauthMode()
if (path.includes('qq') && oauthMode?.provider === 'qq' && oauthMode.mode === 'bind') {
  data = await bindQq({ openId: code as string, accessToken: state as string })
  clearOauthMode()
  if (data?.data?.flag) {
    const currentUser = await getCurrentUser()
    if (currentUser.data?.flag && currentUser.data.data) {
      userStore.login({ userInfo: currentUser.data.data, tokenName: '', tokenValue: '', tokenTimeout: 0 })
    } else {
      userStore.qqBound = true
    }
    useToast({ type: 'success', message: '绑定成功' })
  } else {
    useToast({ type: 'error', message: data?.data?.message || '绑定失败' })
  }
} else if (path.includes('qq')) {
  data = await qqLogin({ code: code as string, state: state as string })
}
```

Keep existing Weibo/Gitee login behavior.

- [ ] **Step 4: Compile frontend**

Run:

```bash
rtk npm run build
```

Expected: `vue-tsc` and Vite build succeed.

- [ ] **Step 5: Commit**

```bash
rtk git add blog-front/blog-vue3/src/components/model/EmailModel.vue blog-front/blog-vue3/src/views/user/User.vue blog-front/blog-vue3/src/components/OauthLogin.vue
rtk git commit -m "feat: add account binding controls"
```

## Task 6: Full Verification

**Files:**
- No new files expected.

- [ ] **Step 1: Run backend tests**

Run from `renzs-blog-satoken`:

```bash
rtk mvn test
```

Expected: all backend tests pass.

- [ ] **Step 2: Run frontend tests**

Run from `blog-front/blog-vue3`:

```bash
rtk npm test
```

Expected: all frontend tests pass.

- [ ] **Step 3: Run frontend build**

Run from `blog-front/blog-vue3`:

```bash
rtk npm run build
```

Expected: build succeeds.

- [ ] **Step 4: Manual OAuth checks**

Verify with a configured QQ OAuth environment:

```text
1. Email account logs in.
2. User center shows QQ as unbound.
3. Click bind QQ and complete QQ callback.
4. User center shows QQ as bound.
5. Log out.
6. Log in with the bound QQ.
7. Confirm returned userInfoId matches the email account.
8. Try binding a QQ already bound to another account and confirm the backend rejects it.
9. QQ-only account binds email with password.
10. Log out and log in with that email/password, confirming the same userInfoId.
```

- [ ] **Step 5: Final status**

Run:

```bash
rtk git status --short
```

Expected: only intentional uncommitted user files remain. Existing unrelated `blog-front/blog-app` changes should not be modified or committed.
