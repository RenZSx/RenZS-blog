package com.chen.blog.module.notice.controller;

import com.chen.blog.common.annotation.OptLog;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.notice.vo.SystemNoticeVO;
import com.chen.blog.module.notice.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.chen.blog.common.constant.OptTypeConst.SAVE;

/**
 * 后台通知控制器
 *
 * @author chen
 * @date 2026/05/10
 */
@Api(tags = "后台通知模块")
@RestController
@RequestMapping("/admin/notices")
public class AdminNoticeController {
    @Autowired
    private NoticeService noticeService;

    /**
     * 发布系统通知
     *
     * @param systemNoticeVO 系统通知
     * @return 新增系统通知数量
     */
    @OptLog(optType = SAVE)
    @ApiOperation(value = "发布系统通知")
    @PostMapping("/system")
    public Result<Integer> publishSystemNotice(@Valid @RequestBody SystemNoticeVO systemNoticeVO) {
        return Result.ok(noticeService.publishSystemNotice(systemNoticeVO));
    }

}

