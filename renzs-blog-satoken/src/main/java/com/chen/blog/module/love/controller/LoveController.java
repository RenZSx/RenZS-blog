package com.chen.blog.module.love.controller;

import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.love.dto.LoveLetterDTO;
import com.chen.blog.module.love.service.LoveLetterService;
import com.chen.blog.module.love.vo.LoveLetterVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 飞书传信相关接口。
 *
 * 负责对外提供信件内容读取接口，以及后台信件内容维护接口。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Api(tags = "飞书传信模块")
@RestController
public class LoveController {

    @Autowired
    private LoveLetterService loveLetterService;

    /**
     * 获取前台展示的信件内容。
     *
     * @return 当前信件标题和正文
     */
    @ApiOperation(value = "获取信件内容")
    @GetMapping("/love/letter")
    public Result<LoveLetterDTO> getLoveLetter() {
        return Result.ok(loveLetterService.getLoveLetter());
    }

    /**
     * 获取后台可编辑的信件内容。
     *
     * @return 当前信件标题和正文
     */
    @ApiOperation(value = "获取后台信件内容")
    @GetMapping("/admin/love/letter")
    public Result<LoveLetterVO> getAdminLoveLetter() {
        return Result.ok(loveLetterService.getAdminLoveLetter());
    }

    /**
     * 保存单篇固定信件内容。
     *
     * @param loveLetterVO 后台提交的信件内容
     * @return 空成功结果
     */
    @ApiOperation(value = "更新后台信件内容")
    @PutMapping("/admin/love/letter")
    public Result<?> updateLoveLetter(@Valid @RequestBody LoveLetterVO loveLetterVO) {
        loveLetterService.updateLoveLetter(loveLetterVO);
        return Result.ok();
    }

}
