package com.chen.blog.module.comment.controller;

import com.chen.blog.common.annotation.OptLog;
import com.chen.blog.module.comment.dto.CommentBackDTO;
import com.chen.blog.module.comment.dto.CommentDTO;
import com.chen.blog.module.comment.dto.MyCommentDTO;
import com.chen.blog.common.domain.vo.*;
import com.chen.blog.module.comment.dao.ReplyDTO;
import com.chen.blog.module.comment.service.CommentService;

import com.chen.blog.module.comment.vo.CommentVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.chen.blog.common.constant.OptTypeConst.*;

/**
 * 评论控制器
 *
 * @author xiaojie
 * @date 2021/07/29
 */
@Api(tags = "评论模块")
@RestController
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 查询评论
     *
     * @param commentVO 评论信息
     * @return {@link Result <CommentDTO>}
     */
    @ApiOperation(value = "查询评论")
    @GetMapping("/comments")
    public Result<PageResult<CommentDTO>> listComments(CommentVO commentVO) {
        return Result.ok(commentService.listComments(commentVO));
    }
    /**
     * 查询评论
     * @return {@link  CommentDTO
     */
    @ApiOperation(value = "查询最新评论")
    @GetMapping("/newComments")
    public Result<List<CommentDTO>> listNewComments() {
        return Result.ok(commentService.listNewComments());
    }
    /**
     * 添加评论
     *
     * @param commentVO 评论信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "添加评论")
    @PostMapping("/comments")
    public Result<?> saveComment(@Valid @RequestBody CommentVO commentVO) {
        commentService.saveComment(commentVO);
        return Result.ok();
    }

    /**
     * 查询评论下的回复
     *
     * @param commentId 评论id
     * @return {@link Result<ReplyDTO>} 回复列表
     */
    @ApiOperation(value = "查询评论下的回复")
    @ApiImplicitParam(name = "commentId", value = "评论id", required = true, dataType = "Integer")
    @GetMapping("/comments/{commentId}/replies")
    public Result<List<ReplyDTO>> listRepliesByCommentId(@PathVariable("commentId") Integer commentId) {
        return Result.ok(commentService.listRepliesByCommentId(commentId));
    }

    /**
     * 评论点赞
     *
     * @param commentId 评论id
     * @return {@link Result<>}
     */
    @ApiOperation(value = "评论点赞")
    @PostMapping("/comments/{commentId}/like")
    public Result<?> saveCommentLike(@PathVariable("commentId") Integer commentId) {
        commentService.saveCommentLike(commentId);
        return Result.ok();
    }

    /**
     * 我的评论列表(当前登录用户)
     *
     * @param current 页码,默认 1
     * @param size    每页大小,默认 10
     * @return {@link Result<PageResult<MyCommentDTO>>} 携带 topicTitle 让前端能展示评论所属主题
     */
    @ApiOperation(value = "我的评论列表")
    @GetMapping("/users/comments")
    public Result<PageResult<MyCommentDTO>> listMyComments(
            @RequestParam(value = "current", required = false, defaultValue = "1") Long current,
            @RequestParam(value = "size", required = false, defaultValue = "10") Long size) {
        return Result.ok(commentService.listMyComments(current, size));
    }

    /**
     * 审核评论
     *
     * @param reviewVO 审核信息
     * @return {@link Result<>}
     */
    @OptLog(optType = UPDATE)
    @ApiOperation(value = "审核评论")
    @PutMapping("/admin/comments/review")
    public Result<?> updateCommentsReview(@Valid @RequestBody ReviewVO reviewVO) {
        commentService.updateCommentsReview(reviewVO);
        return Result.ok();
    }

    /**
     * 删除评论
     *
     * @param commentIdList 评论id列表
     * @return {@link Result<>}
     */
    @OptLog(optType = REMOVE)
    @ApiOperation(value = "删除评论")
    @DeleteMapping("/admin/comments")
    public Result<?> deleteComments(@RequestBody List<Integer> commentIdList) {
        commentService.removeByIds(commentIdList);
        return Result.ok();
    }

    /**
     * 查询后台评论
     *
     * @param condition 条件
     * @return {@link Result<CommentBackDTO>} 后台评论
     */
    @ApiOperation(value = "查询后台评论")
    @GetMapping("/admin/comments")
    public Result<PageResult<CommentBackDTO>> listCommentBackDTO(ConditionVO condition) {
        return Result.ok(commentService.listCommentBackDTO(condition));
    }

}


