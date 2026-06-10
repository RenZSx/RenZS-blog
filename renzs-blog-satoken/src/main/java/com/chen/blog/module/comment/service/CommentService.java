package com.chen.blog.module.comment.service;

import com.chen.blog.module.comment.dto.CommentBackDTO;
import com.chen.blog.module.comment.dto.CommentDTO;
import com.chen.blog.module.comment.dto.MyCommentDTO;
import com.chen.blog.module.comment.vo.CommentVO;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.domain.vo.ReviewVO;

import com.chen.blog.module.comment.dao.ReplyDTO;
import com.chen.blog.module.comment.entity.Comment;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 评论服务
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
public interface CommentService extends IService<Comment> {

    /**
     * 查看评论
     *
     * @param commentVO 评论信息
     * @return 评论列表
     */
    PageResult<CommentDTO> listComments(CommentVO commentVO);

    /**
     * 查看评论下的回复
     *
     * @param commentId 评论id
     * @return 回复列表
     */
    List<ReplyDTO> listRepliesByCommentId(Integer commentId);

    /**
     * 添加评论
     *
     * @param commentVO 评论对象
     */
    void saveComment(CommentVO commentVO);

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     */
    void saveCommentLike(Integer commentId);

    /**
     * 审核评论
     *
     * @param reviewVO 审核信息
     */
    void updateCommentsReview(ReviewVO reviewVO);

    /**
     * 查询后台评论
     *
     * @param condition 条件
     * @return 评论列表
     */
    PageResult<CommentBackDTO> listCommentBackDTO(ConditionVO condition);

    /**
     * 查询最新评论
     * @return 评论列表
     */
    List<CommentDTO> listNewComments();

    /**
     * 我的评论列表(当前登录用户发的所有评论,按时间倒序)
     *
     * @param current 页码,从 1 开始
     * @param size    每页大小
     * @return 我的评论分页结果(携带 topicTitle 让前端能展示评论在《XXX》下)
     */
    PageResult<MyCommentDTO> listMyComments(Long current, Long size);
}

