# Account Binding Design

## Goal

Email login and QQ login become two credentials for the same user account after binding. A user who logs in with email can bind QQ, and a user who logs in with QQ can bind email. After binding, either login method loads the same `tb_user_info` account.

## Current Context

The backend already separates profile data and login credentials:

- `tb_user_info` stores the user profile and is the canonical account.
- `tb_user_auth` stores login credentials and points to `tb_user_info.id` through `user_info_id`.
- Email registration creates both a `tb_user_info` row and an email `tb_user_auth` row.
- QQ login creates a new `tb_user_info` row and a QQ `tb_user_auth` row when the QQ openId is unknown.
- Sa-Token login already uses `userInfoId` as the login id, so multiple `tb_user_auth` rows can safely represent one account if they share the same `user_info_id`.

## Scope

Implement binding between email and QQ only.

Do not merge two existing accounts in this version. If a QQ openId or email already belongs to another `user_info_id`, binding is rejected with a clear message.

## Backend Design

Keep the existing QQ login endpoint unchanged:

- `POST /users/oauth/qq`
- Used by unauthenticated QQ login.
- If the QQ openId is unknown, it can still create a new account as it does today.

Add a QQ binding endpoint:

- `POST /users/oauth/qq/bind`
- Requires the current user to be logged in.
- Accepts the same QQ payload as login.
- Validates the QQ token/openId using the existing QQ validation path.
- Looks up `tb_user_auth` by `username=openId` and `login_type=QQ`.
- If no row exists, insert a new QQ `tb_user_auth` row with `user_info_id=currentUser.userInfoId`.
- If the row belongs to the current user, treat it as already bound and return success.
- If the row belongs to another user, reject binding.

Strengthen email binding:

- `POST /users/email` remains the endpoint used by the frontend.
- It continues to verify the email code.
- If the email auth row does not exist, update `tb_user_info.email` and create an email `tb_user_auth` row for the current `user_info_id`.
- If the email auth row belongs to the current user, update profile email and return success.
- If the email auth row belongs to another user, reject binding.
- When a user without an email login credential binds an email, the request must include a password. The backend stores that password in the new email `tb_user_auth` row so the bound email can immediately be used for normal email/password login.

## Frontend Design

Add account binding actions in the user/account area:

- Show bind email when `userStore.email` is empty.
- Show bind QQ when QQ binding state is absent.

Because the current `UserInfo` payload does not expose QQ binding state, backend adds lightweight binding-state fields to `UserInfoDTO`:

- `qqBound: boolean`
- `emailBound: boolean`

QQ binding flow:

- When logged-in user clicks bind QQ, save a local mode flag such as `oauthMode=bind` and start the same QQ authorization popup/redirect.
- In `OauthLogin.vue`, if the route is QQ and mode is bind, call `/users/oauth/qq/bind` instead of `/users/oauth/qq`.
- On success, call current-user refresh or update the store with binding state.
- On failure, show the backend error message and return to the previous page.

Email binding flow:

- Reuse the existing email binding dialog.
- Add password fields when the current user does not already have an email login credential.
- Call the existing `/users/email` endpoint with the email verification code and password for QQ-only users.

## Error Handling

Use explicit messages for user-facing conflicts:

- "This QQ account is already bound to another account."
- "This email is already bound to another account."
- "QQ is already bound to your account."
- "Email is already bound to your account."

Do not silently reassign credentials across accounts.

## Data Safety

No historical data migration is performed in this version. Articles, comments, likes, messages, and roles stay attached to their existing `user_info_id`.

This avoids unsafe partial merges and keeps the feature small enough to verify.

## Testing

Backend tests or manual verification cover:

- Email account binds an unused QQ, then logs in with QQ and gets the same `userInfoId`.
- QQ account binds an unused email with password, then logs in with email and gets the same `userInfoId`.
- Binding a QQ already owned by another user is rejected.
- Binding an email already owned by another user is rejected.
- Existing QQ login still creates/logs into QQ accounts normally.
- Existing email login still works normally.

Frontend verification covers:

- Login mode still calls the login endpoint.
- Bind mode calls the bind endpoint.
- Successful binding refreshes visible user state.
- Conflict messages are shown.
