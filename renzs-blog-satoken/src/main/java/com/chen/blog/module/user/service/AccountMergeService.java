package com.chen.blog.module.user.service;

/**
 * 账号合并服务。
 */
public interface AccountMergeService {

    /**
     * 将 QQ 独立账号合并到邮箱主账号。
     *
     * @param emailUserInfoId 邮箱主账号资料ID
     * @param qqUserInfoId    QQ独立账号资料ID
     */
    void mergeQqAccountToEmailAccount(Integer emailUserInfoId, Integer qqUserInfoId);
}
