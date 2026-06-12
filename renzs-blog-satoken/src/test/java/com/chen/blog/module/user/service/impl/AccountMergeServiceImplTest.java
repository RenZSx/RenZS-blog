package com.chen.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.entity.UserAuth;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.session.SqlSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 账号合并服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AccountMergeServiceImplTest {

    @Mock
    private SqlSessionTemplate sqlSessionTemplate;

    @Mock
    private UserAuthDao userAuthDao;

    @Mock
    private UserInfoDao userInfoDao;

    @InjectMocks
    private AccountMergeServiceImpl accountMergeService;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), UserAuth.class);
    }

    /**
     * QQ账号合并到邮箱账号时，应迁移凭证、业务数据，并删除无凭证的QQ资料账号。
     */
    @Test
    void mergeQqAccountToEmailAccount_should_move_auth_and_business_rows_to_email_user() {
        when(userAuthDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());

        accountMergeService.mergeQqAccountToEmailAccount(1, 2);

        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.deleteDuplicateArticleCollects");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.deleteDuplicateArticleHistories");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.deleteDuplicateSystemNoticeReads");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.deleteDuplicateUserRoles");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeArticleOwners");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeTalkOwners");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeCommentOwners");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeCommentReplyUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeArticleCollectUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeArticleHistoryUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeChatRecordUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeOperationLogUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeSystemNoticeReadUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeUserNoticeUsers");
        verifySqlUpdate("com.chen.blog.module.user.mapper.AccountMergeMapper.mergeUserRoleUsers");
        verify(userAuthDao).update(any(UserAuth.class), any(LambdaUpdateWrapper.class));
        verify(userInfoDao).deleteById(2);
    }

    /**
     * 源账号还有其他登录凭证时，只迁移业务数据和QQ凭证，不删除资料账号。
     */
    @Test
    void mergeQqAccountToEmailAccount_should_keep_source_profile_when_other_auth_remains() {
        UserAuth otherAuth = UserAuth.builder().id(30).userInfoId(2).username("gitee").build();
        when(userAuthDao.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.singletonList(otherAuth));

        accountMergeService.mergeQqAccountToEmailAccount(1, 2);

        verify(userInfoDao, never()).deleteById(2);
    }

    /**
     * 相同账号不允许执行合并。
     */
    @Test
    void mergeQqAccountToEmailAccount_should_reject_same_account() {
        assertThrows(BizException.class, () -> accountMergeService.mergeQqAccountToEmailAccount(1, 1));
    }

    private void verifySqlUpdate(String statement) {
        verify(sqlSessionTemplate).update(eq(statement), any(Map.class));
    }
}
