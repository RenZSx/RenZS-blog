package com.chen.blog.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chen.blog.common.enums.LoginTypeEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.user.dao.UserAuthDao;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.entity.UserAuth;
import com.chen.blog.module.user.service.AccountMergeService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 账号合并服务实现。
 */
@Service
public class AccountMergeServiceImpl implements AccountMergeService {

    private static final String MAPPER_NAMESPACE = "com.chen.blog.module.user.mapper.AccountMergeMapper.";

    @Autowired
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    private UserAuthDao userAuthDao;

    @Autowired
    private UserInfoDao userInfoDao;

    /**
     * 将 QQ 独立账号合并到邮箱主账号。
     *
     * @param emailUserInfoId 邮箱主账号资料ID
     * @param qqUserInfoId    QQ独立账号资料ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void mergeQqAccountToEmailAccount(Integer emailUserInfoId, Integer qqUserInfoId) {
        if (Objects.isNull(emailUserInfoId) || Objects.isNull(qqUserInfoId)
                || Objects.equals(emailUserInfoId, qqUserInfoId)) {
            throw new BizException("账号合并参数错误");
        }
        Map<String, Object> params = new HashMap<>(2);
        params.put("targetUserInfoId", emailUserInfoId);
        params.put("sourceUserInfoId", qqUserInfoId);

        deleteDuplicateRows(params);
        mergeBusinessRows(params);
        mergeQqAuth(emailUserInfoId, qqUserInfoId);
        deleteSourceUserInfoIfNoAuthLeft(qqUserInfoId);
    }

    private void deleteDuplicateRows(Map<String, Object> params) {
        update("deleteDuplicateArticleCollects", params);
        update("deleteDuplicateArticleHistories", params);
        update("deleteDuplicateSystemNoticeReads", params);
        update("deleteDuplicateUserRoles", params);
    }

    private void mergeBusinessRows(Map<String, Object> params) {
        update("mergeArticleOwners", params);
        update("mergeTalkOwners", params);
        update("mergeCommentOwners", params);
        update("mergeCommentReplyUsers", params);
        update("mergeArticleCollectUsers", params);
        update("mergeArticleHistoryUsers", params);
        update("mergeChatRecordUsers", params);
        update("mergeOperationLogUsers", params);
        update("mergeSystemNoticeReadUsers", params);
        update("mergeUserNoticeUsers", params);
        update("mergeUserRoleUsers", params);
    }

    private void mergeQqAuth(Integer emailUserInfoId, Integer qqUserInfoId) {
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getUserInfoId, emailUserInfoId)
                .eq(UserAuth::getUserInfoId, qqUserInfoId)
                .eq(UserAuth::getLoginType, LoginTypeEnum.QQ.getType()));
    }

    private void deleteSourceUserInfoIfNoAuthLeft(Integer qqUserInfoId) {
        List<UserAuth> remainAuthList = userAuthDao.selectList(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getId)
                .eq(UserAuth::getUserInfoId, qqUserInfoId));
        if (remainAuthList.isEmpty()) {
            userInfoDao.deleteById(qqUserInfoId);
        }
    }

    private void update(String statementId, Map<String, Object> params) {
        sqlSessionTemplate.update(MAPPER_NAMESPACE + statementId, params);
    }
}
