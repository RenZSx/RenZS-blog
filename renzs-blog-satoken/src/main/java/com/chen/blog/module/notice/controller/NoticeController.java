package com.chen.blog.module.notice.controller;

import com.chen.blog.module.notice.dto.NoticeDTO;
import com.chen.blog.module.notice.vo.NoticeQueryVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 通知控制器
 *
 * @author chen
 * @date 2026/05/09
 */
@Api(tags = "通知模块")
@RestController
@RequestMapping("/notices")
public class NoticeController {
    @Autowired
    private NoticeService noticeService;

    /**
     * 查询通知列表
     *
     * @param noticeQueryVO 通知查询条件
     * @return 通知列表
     */
    @ApiOperation(value = "查询通知列表")
    @GetMapping
    public Result<PageResult<NoticeDTO>> listNotices(NoticeQueryVO noticeQueryVO) {
        return Result.ok(noticeService.listNotices(noticeQueryVO));
    }

    /**
     * 查询未读通知数量
     *
     * @return 未读通知数量
     */
    @ApiOperation(value = "查询未读通知数量")
    @GetMapping("/unread-count")
    public Result<Integer> getUnreadCount() {
        return Result.ok(noticeService.getUnreadCount());
    }

    /**
     * 标记单条通知已读
     *
     * @param noticeId 通知id
     * @return 结果
     */
    @ApiOperation(value = "标记单条通知已读")
    @ApiImplicitParam(name = "noticeId", value = "通知id", required = true, dataType = "Integer")
    @PutMapping("/{noticeId}/read")
    public Result<?> readNotice(@PathVariable("noticeId") Integer noticeId,
                                @RequestParam(value = "noticeType", required = false) String noticeType) {
        noticeService.readNotice(noticeId, noticeType);
        return Result.ok();
    }

    /**
     * 标记全部通知已读
     *
     * @return 结果
     */
    @ApiOperation(value = "标记全部通知已读")
    @PutMapping("/read-all")
    public Result<?> readAllNotices() {
        noticeService.readAllNotices();
        return Result.ok();
    }

}

