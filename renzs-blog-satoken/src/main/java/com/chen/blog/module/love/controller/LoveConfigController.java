package com.chen.blog.module.love.controller;

import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.love.dto.LoveConfigDTO;
import com.chen.blog.module.love.service.LoveConfigService;
import com.chen.blog.module.love.vo.LoveConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 纪念页基础配置接口。
 *
 * 提供前台读取接口和后台编辑接口。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Api(tags = "纪念页基础配置模块")
@RestController
public class LoveConfigController {

    /**
     * 纪念页配置服务。
     */
    @Autowired
    private LoveConfigService loveConfigService;

    /**
     * 获取前台纪念页配置。
     *
     * @return 前台配置结果
     */
    @ApiOperation(value = "获取纪念页配置")
    @GetMapping("/love/config")
    public Result<LoveConfigDTO> getLoveConfig() {
        return Result.ok(loveConfigService.getLoveConfig());
    }

    /**
     * 获取后台纪念页配置。
     *
     * @return 后台配置结果
     */
    @ApiOperation(value = "获取后台纪念页配置")
    @GetMapping("/admin/love/config")
    public Result<LoveConfigVO> getAdminLoveConfig() {
        return Result.ok(loveConfigService.getAdminLoveConfig());
    }

    /**
     * 更新后台纪念页配置。
     *
     * @param loveConfigVO 后台提交的配置
     * @return 空成功结果
     */
    @ApiOperation(value = "更新后台纪念页配置")
    @PutMapping("/admin/love/config")
    public Result<?> updateLoveConfig(@Valid @RequestBody LoveConfigVO loveConfigVO) {
        loveConfigService.updateLoveConfig(loveConfigVO);
        return Result.ok();
    }
}
