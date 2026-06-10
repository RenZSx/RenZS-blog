package com.chen.blog.module.chat.service.impl;

import com.chen.blog.module.chat.entity.ChatRecord;
import com.chen.blog.common.util.IpUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 聊天记录安全守卫
 * 负责消息标准化和撤回权限校验
 */
public final class ChatRecordGuard {

    private ChatRecordGuard() {
    }

    /**
     * 标准化发出的消息记录
     * 兜底填充关键字段，防止前端收到空值或脏数据
     *
     * @param record        聊天记录
     * @param defaultAvatar 默认头像
     */
    public static void normalizeOutgoingRecord(ChatRecord record, String defaultAvatar) {
        if (record == null) {
            return;
        }

        // 昵称兜底
        record.setNickname(StringUtils.isBlank(record.getNickname())
                ? "匿名用户"
                : record.getNickname().trim());

        // 头像兜底
        record.setAvatar(StringUtils.isBlank(record.getAvatar())
                ? defaultAvatar
                : record.getAvatar().trim());

        // 内容去除首尾空白
        record.setContent(StringUtils.defaultString(record.getContent()).trim());

        // IP 地址清洗
        if (isUnknown(record.getIpAddress())) {
            record.setIpAddress("");
        }

        // IP 来源解析兜底
        if (isUnknown(record.getIpSource()) && StringUtils.isNotBlank(record.getIpAddress())) {
            record.setIpSource(IpUtils.getIpSource(record.getIpAddress()));
        }
        if (isUnknown(record.getIpSource())) {
            record.setIpSource("");
        }
    }

    /**
     * 判断是否可以撤回消息
     *
     * @param record          聊天记录
     * @param currentUserId   当前用户ID
     * @param currentIpAddress 当前用户IP
     * @return 是否可以撤回
     */
    public static boolean canRecall(ChatRecord record, Integer currentUserId, String currentIpAddress) {
        if (record == null) {
            return false;
        }

        // 登录用户通过 userId 判断所有权
        if (record.getUserId() != null && currentUserId != null) {
            return record.getUserId().equals(currentUserId);
        }

        // 匿名用户通过 IP 判断所有权
        return StringUtils.isNotBlank(currentIpAddress)
                && currentIpAddress.equals(record.getIpAddress());
    }

    /**
     * 判断 IP 是否未知
     */
    private static boolean isUnknown(String value) {
        return StringUtils.isBlank(value)
                || "unknown".equalsIgnoreCase(value)
                || "未知ip".equalsIgnoreCase(value)
                || "未知IP".equalsIgnoreCase(value);
    }
}
