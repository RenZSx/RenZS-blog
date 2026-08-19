package com.chen.blog.module.talk.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.talk.dao.TalkDao;
import com.chen.blog.module.comment.dto.CommentCountDTO;
import com.chen.blog.module.talk.dto.TalkBackDTO;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.module.notice.service.NoticeService;
import com.chen.blog.common.service.RedisService;
import com.chen.blog.module.talk.service.TalkService;
import com.chen.blog.module.comment.dao.CommentDao;
import com.chen.blog.module.talk.dto.TalkDTO;
import com.chen.blog.module.talk.entity.Talk;
import com.chen.blog.common.util.*;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.service.FileReferenceService;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.talk.vo.TalkVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.chen.blog.common.constant.RedisPrefixConst.*;
import static com.chen.blog.common.enums.CommentTypeEnum.TALK;
import static com.chen.blog.common.enums.TalkStatusEnum.PUBLIC;

/**
 * 说说服务
 *
 * @author chenfuyun
 * @date 2022/01/23
 */
@Service
public class TalkServiceImpl extends ServiceImpl<TalkDao, Talk> implements TalkService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TalkServiceImpl.class);

    @Autowired
    private TalkDao talkDao;
    @Autowired
    private CommentDao commentDao;
    @Autowired
    private RedisService redisService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private FileReferenceService fileReferenceService;

    @Override
    public List<String> listHomeTalks() {
        // 查询最新10条说说
        return talkDao.selectList(new LambdaQueryWrapper<Talk>()
                        .eq(Talk::getStatus, PUBLIC.getStatus())
                        .orderByDesc(Talk::getIsTop)
                        .orderByDesc(Talk::getId)
                        .last("limit 10"))
                .stream()
                .map(item -> item.getContent().length() > 200 ? HTMLUtils.deleteHMTLTag(item.getContent().substring(0, 200)) : HTMLUtils.deleteHMTLTag(item.getContent()))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<TalkDTO> listTalks() {
        // 查询说说总量
        Integer count = talkDao.selectCount((new LambdaQueryWrapper<Talk>()
                .eq(Talk::getStatus, PUBLIC.getStatus())));
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询说说
        List<TalkDTO> talkDTOList = talkDao.listTalks(PageUtils.getLimitCurrent(), PageUtils.getSize());
        if (talkDTOList.isEmpty()) {
            return new PageResult<>(talkDTOList, count);
        }
        // 查询说说评论量
        List<Integer> talkIdList = talkDTOList.stream()
                .map(TalkDTO::getId)
                .collect(Collectors.toList());
        Map<Integer, Integer> commentCountMap = commentDao.listCommentCountByTopicIds(talkIdList, TALK.getType())
                .stream()
                .collect(Collectors.toMap(CommentCountDTO::getId, CommentCountDTO::getCommentCount));
        // 查询说说点赞量
        Map<String, Object> likeCountMap = redisService.hGetAll(TALK_LIKE_COUNT);
        talkDTOList.forEach(item -> {
            item.setLikeCount((Integer) likeCountMap.get(item.getId().toString()));
            item.setCommentCount(commentCountMap.get(item.getId()));
            item.setImgList(parseTalkImages(item.getImages(), item.getId()));
        });
        return new PageResult<>(talkDTOList, count);
    }

    @Override
    public TalkDTO getTalkById(Integer talkId) {
        // 查询说说信息
        TalkDTO talkDTO = talkDao.getTalkById(talkId);
        if (Objects.isNull(talkDTO)) {
            throw new BizException("说说不存在");
        }
        // 查询说说点赞量
        talkDTO.setLikeCount((Integer) redisService.hGet(TALK_LIKE_COUNT, talkId.toString()));
        talkDTO.setImgList(parseTalkImages(talkDTO.getImages(), talkDTO.getId()));
        return talkDTO;
    }

    @Override
    public void saveTalkLike(Integer talkId) {
        Talk talk = talkDao.selectById(talkId);
        if (Objects.isNull(talk)) {
            throw new BizException("说说不存在");
        }
        // 判断是否点赞
        String talkLikeKey = TALK_USER_LIKE + UserUtils.getLoginUser().getUserInfoId();
        if (redisService.sIsMember(talkLikeKey, talkId)) {
            // 点过赞则删除说说id
            redisService.sRemove(talkLikeKey, talkId);
            // 说说点赞量-1
            redisService.hDecr(TALK_LIKE_COUNT, talkId.toString(), 1L);
        } else {
            // 未点赞则增加说说id
            redisService.sAdd(talkLikeKey, talkId);
            // 说说点赞量+1
            redisService.hIncr(TALK_LIKE_COUNT, talkId.toString(), 1L);
            Integer receiveUserId = talk.getUserId();
            noticeService.saveLikeNotice(UserUtils.getLoginUser().getUserInfoId(), talkId, "talk", receiveUserId);
        }
    }

    @Override
    public void saveOrUpdateTalk(TalkVO talkVO) {
        Talk talk = BeanCopyUtils.copyObject(talkVO, Talk.class);
        talk.setUserId(UserUtils.getLoginUser().getUserInfoId());
        this.saveOrUpdate(talk);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteTalks(List<Integer> talkIdList) {
        if (talkIdList == null || talkIdList.isEmpty()) {
            LOGGER.info("删除说说请求为空，不执行图片清理");
            return;
        }

        LOGGER.info("开始删除说说，talkIds={}", talkIdList);
        List<Talk> talkList = talkDao.selectBatchIds(talkIdList);
        Set<String> removableUrls = talkList.stream()
                .flatMap(talk -> parseTalkImages(talk.getImages(), talk.getId()).stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(url -> !url.isEmpty())
                .collect(Collectors.toCollection(HashSet::new));
        LOGGER.info("说说查询完成，requestedCount={}, foundCount={}, imageUrlCount={}",
                talkIdList.size(), talkList.size(), removableUrls.size());

        if (!removableUrls.isEmpty()) {
            Set<String> referencedUrls = fileReferenceService.findReferencedUrls(
                    removableUrls, Collections.emptyList(), talkIdList, Collections.emptyList());
            removableUrls.removeAll(referencedUrls);
            LOGGER.info("说说图片引用检查完成，skippedReferencedCount={}, deleteCandidateCount={}",
                    referencedUrls.size(), removableUrls.size());
            removableUrls.forEach(url -> {
                LOGGER.info("开始删除说说图片，url={}", url);
                uploadStrategyContext.deleteFile(url);
            });
        } else {
            LOGGER.info("待删除说说没有可识别的图片 URL");
        }

        talkDao.deleteBatchIds(talkIdList);
        LOGGER.info("说说数据库记录删除完成，talkIds={}", talkIdList);
    }

    @Override
    public PageResult<TalkBackDTO> listBackTalks(ConditionVO conditionVO) {
        // 查询说说总量
        Integer count = talkDao.selectCount(new LambdaQueryWrapper<Talk>()
                .eq(Objects.nonNull(conditionVO.getStatus()), Talk::getStatus, conditionVO.getStatus()));
        if (count == 0) {
            return new PageResult<>();
        }
        // 分页查询说说
        List<TalkBackDTO> talkDTOList = talkDao.listBackTalks(PageUtils.getLimitCurrent(), PageUtils.getSize(), conditionVO);
        talkDTOList.forEach(item -> {
            item.setImgList(parseTalkImages(item.getImages(), item.getId()));
        });
        return new PageResult<>(talkDTOList, count);
    }

    @Override
    public TalkBackDTO getBackTalkById(Integer talkId) {
        TalkBackDTO talkBackDTO = talkDao.getBackTalkById(talkId);
        talkBackDTO.setImgList(parseTalkImages(talkBackDTO.getImages(), talkBackDTO.getId()));
        return talkBackDTO;
    }

    /**
     * 安全解析说说图片，避免单条异常数据导致整个接口失败。
     *
     * @param images 图片 JSON 字符串
     * @param talkId 说说 id
     * @return 图片列表
     */
    private List<String> parseTalkImages(String images, Integer talkId) {
        if (images == null || images.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return CommonUtils.castList(JSON.parseObject(images, List.class), String.class);
        } catch (Exception exception) {
            LOGGER.warn("解析说说图片失败，talkId={}, images={}", talkId, images, exception);
            return Collections.emptyList();
        }
    }

}
