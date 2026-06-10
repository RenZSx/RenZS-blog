package com.chen.blog.module.comment.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.chen.blog.module.article.dao.ArticleDao;
import com.chen.blog.module.article.entity.Article;
import com.chen.blog.common.strategy.email.context.EmailStrategyContext;
import com.chen.blog.module.comment.dao.ReplyCountDTO;
import com.chen.blog.module.comment.dao.ReplyDTO;
import com.chen.blog.module.comment.dto.CommentBackDTO;
import com.chen.blog.module.comment.dto.CommentDTO;
import com.chen.blog.module.comment.dto.MyCommentDTO;
import com.chen.blog.module.comment.vo.CommentVO;
import com.chen.blog.module.talk.dao.TalkDao;
import com.chen.blog.module.talk.entity.Talk;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.blogInfo.service.BlogInfoService;
import com.chen.blog.module.blogInfo.vo.WebsiteConfigVO;
import com.chen.blog.module.comment.service.CommentService;
import com.chen.blog.module.notice.service.NoticeService;
import com.chen.blog.module.user.dao.UserInfoDao;
import com.chen.blog.module.user.dto.EmailDTO;
import com.chen.blog.module.user.entity.UserInfo;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.module.comment.entity.Comment;
import com.chen.blog.module.comment.dao.CommentDao;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.common.util.HTMLUtils;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.CommonConst.*;
import static com.chen.blog.common.constant.RedisPrefixConst.COMMENT_LIKE_COUNT;
import static com.chen.blog.common.constant.RedisPrefixConst.COMMENT_USER_LIKE;
import static com.chen.blog.common.enums.CommentTypeEnum.*;
import static com.chen.blog.common.enums.StatusCodeEnum.VALID_ERROR;

/**
 * 评论服务
 *
 * @author chenfuyun
 * @date 2021/07/31
 * @since 2020-05-18
 */
@Service
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private TalkDao talkDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private EmailStrategyContext emailStrategyContext;
    @Autowired
    private BlogInfoService blogInfoService;
    @Autowired
    private NoticeService noticeService;

    /**
     * 网站网址
     */
    @Value("${website.url}")
    private String websiteUrl;

    @Override
    public PageResult<CommentDTO> listComments(CommentVO commentVO) {
        // 查询评论量
        Integer commentCount = commentDao.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Objects.nonNull(commentVO.getTopicId()), Comment::getTopicId, commentVO.getTopicId())
                .eq(Comment::getType, commentVO.getType())
                .isNull(Comment::getParentId)
                .eq(Comment::getIsReview, TRUE));
        if (commentCount == 0) {
            return new PageResult<>();
        }
        // 分页查询评论数据
        List<CommentDTO> commentDTOList = commentDao.listComments(PageUtils.getLimitCurrent(), PageUtils.getSize(), commentVO);
        if (CollectionUtils.isEmpty(commentDTOList)) {
            return new PageResult<>();
        }
        // 查询redis的评论点赞数据
        Map<String, Object> likeCountMap = redisService.hGetAll(COMMENT_LIKE_COUNT);
        // 提取评论id集合
        List<Integer> commentIdList = commentDTOList.stream()
                .map(CommentDTO::getId)
                .collect(Collectors.toList());
        // 根据评论id集合查询回复数据
        List<ReplyDTO> replyDTOList = commentDao.listReplies(commentIdList);
        // 封装回复点赞量
        replyDTOList.forEach(item -> item.setLikeCount((Integer) likeCountMap.get(item.getId().toString())));
        // 根据评论id分组回复数据
        Map<Integer, List<ReplyDTO>> replyMap = replyDTOList.stream()
                .collect(Collectors.groupingBy(ReplyDTO::getParentId));
        // 根据评论id查询回复量
        Map<Integer, Integer> replyCountMap = commentDao.listReplyCountByCommentId(commentIdList)
                .stream().collect(Collectors.toMap(ReplyCountDTO::getCommentId, ReplyCountDTO::getReplyCount));
        // 封装评论数据
        commentDTOList.forEach(item -> {
            item.setLikeCount((Integer) likeCountMap.get(item.getId().toString()));
            item.setReplyDTOList(replyMap.get(item.getId()));
            item.setReplyCount(replyCountMap.get(item.getId()));
        });
        return new PageResult<>(commentDTOList, commentCount);
    }

    @Override
    public List<ReplyDTO> listRepliesByCommentId(Integer commentId) {
        // 转换页码查询评论下的回复
        List<ReplyDTO> replyDTOList = commentDao.listRepliesByCommentId(PageUtils.getLimitCurrent(), PageUtils.getSize(), commentId);
        // 查询redis的评论点赞数据
        Map<String, Object> likeCountMap = redisService.hGetAll(COMMENT_LIKE_COUNT);
        // 封装点赞数据
        replyDTOList.forEach(item -> item.setLikeCount((Integer) likeCountMap.get(item.getId().toString())));
        return replyDTOList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveComment(CommentVO commentVO) {
        if (Objects.isNull(getCommentEnum(commentVO.getType()))) {
            throw new BizException(VALID_ERROR);
        }
        // 判断是否需要审核
        WebsiteConfigVO websiteConfig = blogInfoService.getWebsiteConfig();
        Integer isReview = websiteConfig.getIsCommentReview();
        // 过滤标签
        commentVO.setCommentContent(HTMLUtils.filter(commentVO.getCommentContent()));
        Comment comment = Comment.builder()
                .userId(UserUtils.getLoginUser().getUserInfoId())
                .replyUserId(commentVO.getReplyUserId())
                .topicId(commentVO.getTopicId())
                .commentContent(commentVO.getCommentContent())
                .parentId(commentVO.getParentId())
                .type(commentVO.getType())
                .isReview(isReview == TRUE ? FALSE : TRUE)
                .build();
        commentDao.insert(comment);
        noticeService.saveReplyNotice(comment);
        // 判断是否开启邮箱通知,通知用户
        if (websiteConfig.getIsEmailNotice().equals(TRUE)) {
            /*
             * 这段代码使用Java中的CompletableFuture类创建一个异步任务，通过runAsync方法执行传入的Lambda表达式，
             * 即执行notice(comment)方法。这样可以在后台线程中执行notice(comment)方法，而不会阻塞当前线程。
             */
            //通知用户
            CompletableFuture.runAsync(() -> notice(comment));
        }
    }

    @Override
    public void saveCommentLike(Integer commentId) {
        // 判断是否点赞
        String commentLikeKey = COMMENT_USER_LIKE + UserUtils.getLoginUser().getUserInfoId();
        if (redisService.sIsMember(commentLikeKey, commentId)) {
            // 点过赞则删除评论id
            redisService.sRemove(commentLikeKey, commentId);
            // 评论点赞量-1
            redisService.hDecr(COMMENT_LIKE_COUNT, commentId.toString(), 1L);
        } else {
            // 未点赞则增加评论id
            redisService.sAdd(commentLikeKey, commentId);
            // 评论点赞量+1
            redisService.hIncr(COMMENT_LIKE_COUNT, commentId.toString(), 1L);
        }
    }

    @Override
    public void updateCommentsReview(ReviewVO reviewVO) {
        // 修改评论审核状态
        List<Comment> commentList = reviewVO.getIdList().stream().map(item -> Comment.builder()
                        .id(item)
                        .isReview(reviewVO.getIsReview())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(commentList);
    }

    @Override
    public PageResult<CommentBackDTO> listCommentBackDTO(ConditionVO condition) {
        // 统计后台评论量
        Integer count = commentDao.countCommentDTO(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 查询后台评论集合
        List<CommentBackDTO> commentBackDTOList = commentDao.listCommentBackDTO(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(commentBackDTOList, count);
    }

    /**
     * 查询最新评论
     * @return 评论信息
     */
    @Override
    public List<CommentDTO> listNewComments() {
        // 1. 查询前15条有效评论（仅查必要字段）
        List<Comment> comments = commentDao.selectList(new LambdaQueryWrapper<Comment>()
                .select(Comment::getCommentContent, Comment::getCreateTime, Comment::getUserId)
                .eq(Comment::getType, ARTICLE.getType())
                .eq(Comment::getIsDelete, FALSE)
                .orderByDesc(Comment::getCreateTime)
                .last("limit 15"));

        // 2. 批量查询用户信息（解决N+1问题）
        // 提取所有评论的用户ID
        Set<Integer> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        // 批量查询用户信息，转成Map（key=userId，value=UserInfo）
        Map<Integer, UserInfo> userInfoMap = userInfoDao.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(UserInfo::getId, userInfo -> userInfo));

        // 3. 遍历评论，逐个创建CommentDTO并赋值
        List<CommentDTO> commentDTOList = new ArrayList<>();
        for (Comment item : comments) {
            CommentDTO commentDTO = new CommentDTO(); // 每次循环创建新的DTO对象
            commentDTO.setCommentContent(item.getCommentContent());
            commentDTO.setUserId(item.getUserId());
            commentDTO.setCreateTime(item.getCreateTime());
            // 从批量查询的Map中取用户信息（避免循环查库）
            UserInfo userInfo = userInfoMap.get(item.getUserId());
            if (userInfo != null) { // 防止用户信息不存在导致空指针
                commentDTO.setAvatar(userInfo.getAvatar());
                commentDTO.setNickname(userInfo.getNickname());
            }
            commentDTOList.add(commentDTO); // 把当前DTO加入集合
        }
        // 4. 返回多元素的集合（而非单元素）
        return commentDTOList;
    }

    /**
     * 通知评论用户
     *
     * @param comment 评论信息
     */
    public void notice(Comment comment) {
        // 查询回复用户邮箱号
        Integer userId = BLOGGER_ID;
        //文章id
        String id = Objects.nonNull(comment.getTopicId()) ? comment.getTopicId().toString() : "";
        //回复的用户id
        if (Objects.nonNull(comment.getReplyUserId())) {
            userId = comment.getReplyUserId();
        } else {
            //如果没有回复的用户，根据文章获取发布人的id
            switch (Objects.requireNonNull(getCommentEnum(comment.getType()))) {
                case ARTICLE:
                    userId = articleDao.selectById(comment.getTopicId()).getUserId();
                    break;
                case TALK:
                    userId = talkDao.selectById(comment.getTopicId()).getUserId();
                    break;
                default:
                    break;
            }
        }
        String email = userInfoDao.selectById(userId).getEmail();
        //如果用户回复自己则不做处理
        if (StringUtils.isNotBlank(email)) {
            // 发送消息
            EmailDTO emailDTO = new EmailDTO();
            if (comment.getIsReview().equals(TRUE)) {
                // 评论提醒
                emailDTO.setEmail(email);
                emailDTO.setSubject("评论提醒");
                // 获取评论路径
                String url = websiteUrl + getCommentPath(comment.getType()) + id;
                emailDTO.setContent("您收到了一条新的回复，请前往" + url + "\n页面查看");
                emailDTO.setCommentContent(comment.getCommentContent());
            } else {
                // 管理员审核提醒
                String adminEmail = userInfoDao.selectById(BLOGGER_ID).getEmail();
                emailDTO.setEmail(adminEmail);
                emailDTO.setSubject("审核提醒");
                emailDTO.setContent("您收到了一条新的回复，请前往后台管理页面审核");
            }
//            emailSendService.sendEmails(emailDTO);
            emailStrategyContext.executeEmailStrategy(emailDTO);
        }


    }

    /**
     * 我的评论列表
     * <p>
     * 实现策略:
     *   1. 用 MyBatis-Plus LambdaQueryWrapper 按 user_id 查 Comment 表(含审核中、按时间倒序)
     *   2. 按 (type, topicId) 分组,批量查文章 / 说说标题
     *   3. 留言/友链类型直接用 commentContent 截断作为 topicTitle
     *   4. 组装 MyCommentDTO 返回
     */
    @Override
    public PageResult<MyCommentDTO> listMyComments(Long current, Long size) {
        // 1. 当前登录用户
        Integer userInfoId = UserUtils.getLoginUser().getUserInfoId();

        Long pageCurrent = current == null || current < 1 ? 1L : current;
        Long pageSize = size == null || size < 1 ? 10L : size;

        // 2. 分页查 Comment(LambdaQueryWrapper + Page 都是 MP 自带能力)
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Comment> page =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageCurrent, pageSize);

        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<Comment>()
                .eq(Comment::getUserId, userInfoId)
                .eq(Comment::getIsDelete, FALSE)
                .orderByDesc(Comment::getCreateTime);

        com.baomidou.mybatisplus.core.metadata.IPage<Comment> commentPage = this.page(page, wrapper);

        List<Comment> records = commentPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PageResult<>(Collections.emptyList(), (int) commentPage.getTotal());
        }

        // 3. 按 type 分组收集 topicId
        Set<Integer> articleIds = new HashSet<>();
        Set<Integer> talkIds = new HashSet<>();
        for (Comment c : records) {
            if (c.getType() == null || c.getTopicId() == null) continue;
            if (c.getType().equals(ARTICLE.getType())) articleIds.add(c.getTopicId());
            else if (c.getType().equals(TALK.getType())) talkIds.add(c.getTopicId());
            // 友链/留言/关于 topicId 可能为 null,无需查标题
        }

        // 4. 批量查标题
        Map<Integer, String> articleTitleMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleDao.selectBatchIds(articleIds);
            for (Article a : articles) {
                articleTitleMap.put(a.getId(), a.getArticleTitle());
            }
        }

        Map<Integer, String> talkContentMap = new HashMap<>();
        if (!talkIds.isEmpty()) {
            List<Talk> talks = talkDao.selectBatchIds(talkIds);
            for (Talk t : talks) {
                String text = HTMLUtils.deleteHMTLTag(t.getContent());
                if (text != null && text.length() > 20) text = text.substring(0, 20) + "...";
                talkContentMap.put(t.getId(), text == null ? "" : text);
            }
        }

        // 5. 组装 MyCommentDTO
        List<MyCommentDTO> dtoList = records.stream().map((c) -> {
            String topicTitle = resolveTopicTitle(c, articleTitleMap, talkContentMap);
            return MyCommentDTO.builder()
                    .id(c.getId())
                    .topicId(c.getTopicId())
                    .type(c.getType())
                    .topicTitle(topicTitle)
                    .commentContent(c.getCommentContent())
                    .parentId(c.getParentId())
                    .replyUserId(c.getReplyUserId())
                    .isReview(c.getIsReview())
                    .createTime(c.getCreateTime())
                    .build();
        }).collect(Collectors.toList());

        return new PageResult<>(dtoList, (int) commentPage.getTotal());
    }

    /**
     * 根据评论 type 解析主题标题
     */
    private String resolveTopicTitle(Comment c,
                                     Map<Integer, String> articleTitleMap,
                                     Map<Integer, String> talkContentMap) {
        if (c.getType() == null) return "未知主题";
        Integer t = c.getType();
        if (t.equals(ARTICLE.getType())) {
            return articleTitleMap.getOrDefault(c.getTopicId(), "文章已删除");
        } else if (t.equals(TALK.getType())) {
            return talkContentMap.getOrDefault(c.getTopicId(), "说说已删除");
        } else if (t.equals(LINK.getType())) {
            return "友情链接";
        }
        // 留言板/关于 等未在 CommentTypeEnum 枚举内的类型,用通用兜底
        return "其他评论";
    }

}

