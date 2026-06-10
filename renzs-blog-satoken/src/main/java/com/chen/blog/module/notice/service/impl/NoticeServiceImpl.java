package com.chen.blog.module.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.common.enums.CommentTypeEnum;
import com.chen.blog.module.notice.dao.SystemNoticeDao;
import com.chen.blog.module.notice.dao.SystemNoticeReadDao;
import com.chen.blog.module.notice.websocket.NoticeWebSocketServiceImpl;
import com.chen.blog.module.talk.dao.TalkDao;
import com.chen.blog.module.notice.dao.UserNoticeDao;
import com.chen.blog.module.comment.entity.Comment;
import com.chen.blog.module.notice.entity.SystemNotice;
import com.chen.blog.module.talk.entity.Talk;
import com.chen.blog.module.notice.entity.UserNotice;
import com.chen.blog.module.notice.dto.NoticeDTO;
import com.chen.blog.module.notice.vo.NoticeQueryVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.module.notice.vo.SystemNoticeVO;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.notice.service.NoticeService;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

import static com.chen.blog.common.constant.CommonConst.TRUE;
import static com.chen.blog.common.enums.CommentTypeEnum.getCommentEnum;
import static com.chen.blog.common.enums.StatusCodeEnum.VALID_ERROR;

/**
 * 通知服务实现
 *
 * @author chen
 * @date 2026/05/09
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<UserNoticeDao, UserNotice> implements NoticeService {
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private TalkDao talkDao;
    @Autowired
    private SystemNoticeDao systemNoticeDao;
    @Autowired
    private SystemNoticeReadDao systemNoticeReadDao;

    @Override
    public PageResult<NoticeDTO> listNotices(NoticeQueryVO noticeQueryVO) {
        Integer userId = getCurrentUserId();
        Integer count = baseMapper.countNotices(userId, noticeQueryVO);
        if (count == 0) {
            return new PageResult<>();
        }
        List<NoticeDTO> noticeDTOList = baseMapper.listNotices(PageUtils.getLimitCurrent(), PageUtils.getSize(),
                userId, noticeQueryVO);
        return new PageResult<>(noticeDTOList, count);
    }

    @Override
    public Integer getUnreadCount() {
        return getUnreadCountByUserId(getCurrentUserId());
    }

    @Override
    public Integer getUnreadCountByUserId(Integer userId) {
        if (Objects.isNull(userId)) {
            return 0;
        }
        Integer count = baseMapper.getUnreadCount(userId);
        return Objects.nonNull(count) ? count : 0;
    }

    @Override
    public void readNotice(Integer noticeId, String noticeType) {
        Integer userId = getCurrentUserId();
        if ("system".equals(noticeType)) {
            systemNoticeReadDao.readSystemNotice(noticeId, userId);
            return;
        }
        this.update(new LambdaUpdateWrapper<UserNotice>()
                .eq(UserNotice::getId, noticeId)
                .eq(UserNotice::getUserId, userId)
                .set(UserNotice::getIsRead, 1));
    }

    @Override
    public void readAllNotices() {
        Integer userId = getCurrentUserId();
        this.update(new LambdaUpdateWrapper<UserNotice>()
                .eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getIsRead, 0)
                .set(UserNotice::getIsRead, 1));
        systemNoticeReadDao.readAllSystemNotices(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer publishSystemNotice(SystemNoticeVO systemNoticeVO) {
        String jumpPath = StringUtils.isBlank(systemNoticeVO.getJumpPath())
                ? null
                : systemNoticeVO.getJumpPath().trim();
        String content = systemNoticeVO.getContent().trim();
        SystemNotice systemNotice = SystemNotice.builder()
                .content(content)
                .jumpPath(jumpPath)
                .build();
        int result = systemNoticeDao.insert(systemNotice);
        if (result > 0) {
            log.info("系统通知保存成功，noticeId={}, jumpPath={}", systemNotice.getId(), systemNotice.getJumpPath());
            // 系统通知只落一条库记录，推送延迟到事务提交后，避免回滚时前端先看到幽灵通知。
            executeAfterCommit(() -> {
                log.info("系统通知开始推送在线用户，noticeId={}, onlineUserIds={}",
                        systemNotice.getId(), NoticeWebSocketServiceImpl.listOnlineUserIds());
                for (Integer userId : NoticeWebSocketServiceImpl.listOnlineUserIds()) {
                    NoticeWebSocketServiceImpl.sendNoticeCreated(userId,
                            getUnreadCountByUserId(userId),
                            buildSystemNoticeDTO(systemNotice, userId));
                }
            });
        }
        return result;
    }

    @Override
    public void saveNotice(UserNotice userNotice) {
        if (Objects.isNull(userNotice)) {
            return;
        }
        log.info("用户通知准备保存，userId={}, noticeType={}, sourceId={}, targetId={}",
                userNotice.getUserId(), userNotice.getNoticeType(), userNotice.getSourceId(), userNotice.getTargetId());
        boolean isSaved = this.save(userNotice);
        if (!isSaved || Objects.isNull(userNotice.getId()) || Objects.isNull(userNotice.getUserId())) {
            log.warn("用户通知保存失败或主键缺失，userId={}, noticeType={}, noticeId={}",
                    Objects.nonNull(userNotice) ? userNotice.getUserId() : null,
                    Objects.nonNull(userNotice) ? userNotice.getNoticeType() : null,
                    Objects.nonNull(userNotice) ? userNotice.getId() : null);
            return;
        }
        log.info("用户通知保存成功，noticeId={}, userId={}, noticeType={}",
                userNotice.getId(), userNotice.getUserId(), userNotice.getNoticeType());
        // 统一回查最终通知投影视图，保证 websocket 负载和通知列表展示结构一致。
        executeAfterCommit(() -> {
            NoticeDTO noticeDTO = baseMapper.getNoticeByUserIdAndNoticeId(userNotice.getUserId(), userNotice.getId());
            if (Objects.isNull(noticeDTO)) {
                log.warn("用户通知推送前回查失败，noticeId={}, userId={}", userNotice.getId(), userNotice.getUserId());
                return;
            }
            log.info("用户通知开始推送，noticeId={}, userId={}, noticeType={}",
                    noticeDTO.getId(), noticeDTO.getUserId(), noticeDTO.getNoticeType());
            NoticeWebSocketServiceImpl.sendNoticeCreated(userNotice.getUserId(),
                    getUnreadCountByUserId(userNotice.getUserId()), noticeDTO);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveLikeNotice(Integer actorUserId, Integer targetId, String targetType, Integer receiveUserId) {
        if (Objects.isNull(receiveUserId) || Objects.isNull(actorUserId) || Objects.isNull(targetId)
                || actorUserId.equals(receiveUserId)) {
            return;
        }
        String noticeType;
        String jumpPath;
        String sourceType;
        switch (targetType) {
            case "article":
                noticeType = "article_like";
                jumpPath = "/articles/" + targetId;
                sourceType = "article";
                break;
            case "talk":
                noticeType = "talk_like";
                jumpPath = "/talks/" + targetId;
                sourceType = "talk";
                break;
            default:
                throw new BizException(VALID_ERROR);
        }
        String lockName = "notice:like:" + receiveUserId + ":" + noticeType + ":" + targetType + ":" + targetId;
        Integer lockResult = baseMapper.acquireNoticeLock(lockName, 3);
        if (!Objects.equals(lockResult, 1)) {
            throw new BizException("通知处理繁忙，请稍后重试");
        }
        try {
            int count = this.count(new LambdaQueryWrapper<UserNotice>()
                    .eq(UserNotice::getUserId, receiveUserId)
                    .eq(UserNotice::getNoticeType, noticeType)
                    .eq(UserNotice::getTargetId, targetId)
                    .eq(UserNotice::getTargetType, targetType)
                    .eq(UserNotice::getIsRead, 0));
            if (count > 0) {
                return;
            }
            UserNotice userNotice = UserNotice.builder()
                    .userId(receiveUserId)
                    .noticeType(noticeType)
                    .sourceId(targetId)
                    .sourceType(sourceType)
                    .targetId(targetId)
                    .targetType(targetType)
                    .jumpPath(jumpPath)
                    .anchorKey(null)
                    .content("您的内容收到了一次新的点赞")
                    .replyContent(null)
                    .isRead(0)
                    .build();
            saveNotice(userNotice);
        } finally {
            try {
                baseMapper.releaseNoticeLock(lockName);
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public void saveReplyNotice(Comment comment) {
        if (Objects.isNull(comment) || Objects.isNull(comment.getId()) || Objects.isNull(comment.getTopicId())) {
            return;
        }
        if (!Objects.equals(comment.getIsReview(), TRUE)) {
            return;
        }
        CommentTypeEnum commentType = getCommentEnum(comment.getType());
        if (Objects.isNull(commentType)) {
            return;
        }
        Integer receiveUserId = comment.getReplyUserId();
        String noticeType;
        String targetType;
        String jumpPath;
        switch (commentType) {
            case ARTICLE:
                noticeType = "comment_reply";
                targetType = "article";
                jumpPath = "/articles/" + comment.getTopicId();
                if (Objects.isNull(receiveUserId)) {
                    Article article = articleDao.selectById(comment.getTopicId());
                    receiveUserId = Objects.nonNull(article) ? article.getUserId() : null;
                }
                break;
            case TALK:
                noticeType = "talk_reply";
                targetType = "talk";
                jumpPath = "/talks/" + comment.getTopicId();
                if (Objects.isNull(receiveUserId)) {
                    Talk talk = talkDao.selectById(comment.getTopicId());
                    receiveUserId = Objects.nonNull(talk) ? talk.getUserId() : null;
                }
                break;
            case LINK:
                return;
            default:
                return;
        }
        if (Objects.isNull(receiveUserId) || receiveUserId.equals(comment.getUserId())) {
            return;
        }
        UserNotice userNotice = UserNotice.builder()
                .userId(receiveUserId)
                .noticeType(noticeType)
                .sourceId(comment.getId())
                .sourceType("comment")
                .targetId(comment.getTopicId())
                .targetType(targetType)
                .jumpPath(jumpPath)
                .anchorKey("comment-" + comment.getId())
                .content("您收到了一条新的回复")
                .replyContent(comment.getCommentContent())
                .isRead(0)
                .build();
        saveNotice(userNotice);
    }

    /**
     * 当前通知逻辑统一使用用户信息 id，和文章、说说、评论的 user_id 语义保持一致。
     */
    private Integer getCurrentUserId() {
        return UserUtils.getLoginUser().getUserInfoId();
    }

    /**
     * 系统通知没有用户通知表记录，需要按通知列表字段结构手动补齐 websocket 负载。
     */
    private NoticeDTO buildSystemNoticeDTO(SystemNotice systemNotice, Integer userId) {
        return NoticeDTO.builder()
                .id(systemNotice.getId())
                .userId(userId)
                .noticeType("system")
                .sourceId(null)
                .sourceType("system")
                .targetId(null)
                .targetType("system")
                .jumpPath(systemNotice.getJumpPath())
                .anchorKey(null)
                .content(systemNotice.getContent())
                .replyContent(null)
                .isRead(0)
                .createTime(systemNotice.getCreateTime())
                .build();
    }

    /**
     * 通知推送必须晚于数据库提交，否则回滚时前端会短暂看到幽灵通知。
     */
    private void executeAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
    }

}

