package com.chen.blog.module.blogInfo.controller;


import com.chen.blog.common.annotation.OptLog;
import com.chen.blog.common.enums.FilePathEnum;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.module.blogInfo.dto.BlogBackInfoDTO;
import com.chen.blog.module.blogInfo.dto.BlogHomeInfoDTO;
import com.chen.blog.module.blogInfo.service.BlogInfoService;
import com.chen.blog.module.blogInfo.vo.BlogInfoVO;
import com.chen.blog.module.chat.webSocket.WebSocketServiceImpl;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.module.blogInfo.vo.VoiceVO;
import com.chen.blog.module.blogInfo.vo.WebsiteConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import javax.validation.Valid;

import static com.chen.blog.common.constant.OptTypeConst.UPDATE;

/**
 * 博客信息控制器
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Api(tags = "博客信息模块")
@RestController
public class BlogInfoController {
    @Autowired
    private BlogInfoService blogInfoService;
    @Autowired
    private WebSocketServiceImpl webSocketService;
    @Autowired
    private UploadStrategyContext uploadStrategyContext;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查看博客信息
     *
     * @return {@link Result<BlogHomeInfoDTO>} 博客信息
     */
    @ApiOperation(value = "查看博客信息")
    @GetMapping("/")
    public Result<BlogHomeInfoDTO> getBlogHomeInfo() {
        return Result.ok(blogInfoService.getBlogHomeInfo());
    }

    /**
     * 查看后台信息
     *
     * @return {@link Result<BlogBackInfoDTO>} 后台信息
     */
    @ApiOperation(value = "查看后台信息")
    @GetMapping("/admin")
    public Result<BlogBackInfoDTO> getBlogBackInfo() {
        return Result.ok(blogInfoService.getBlogBackInfo());
    }

    /**
     * 上传博客配置图片
     *
     * @param file 文件
     * @return {@link Result<String>} 博客配置图片
     */
    @ApiOperation(value = "上传博客配置图片")
    @ApiImplicitParam(name = "file", value = "图片", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/config/images")
    public Result<String> savePhotoAlbumCover(MultipartFile file) {
        return Result.ok(uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.CONFIG.getPath()));
    }

    /**
     * 更新网站配置
     *
     * @param websiteConfigVO 网站配置信息
     * @return {@link Result}
     */
    @ApiOperation(value = "更新网站配置")
    @PutMapping("/admin/website/config")
    public Result<?> updateWebsiteConfig(@Valid @RequestBody WebsiteConfigVO websiteConfigVO) {
        blogInfoService.updateWebsiteConfig(websiteConfigVO);
        return Result.ok();
    }

    /**
     * 获取网站配置
     *
     * @return {@link Result<WebsiteConfigVO>} 网站配置
     */
    @ApiOperation(value = "获取网站配置")
    @GetMapping("/admin/website/config")
    public Result<WebsiteConfigVO> getWebsiteConfig() {
        return Result.ok(blogInfoService.getWebsiteConfig());
    }

    /**
     * 查看关于我信息
     *
     * @return {@link Result<String>} 关于我信息
     */
    @ApiOperation(value = "查看关于我信息")
    @GetMapping("/about")
    public Result<String> getAbout() {
        return Result.ok(blogInfoService.getAbout());
    }

    /**
     * 修改关于我信息
     *
     * @param blogInfoVO 博客信息
     * @return {@link Result<>}
     */
    @OptLog(optType = UPDATE)
    @ApiOperation(value = "修改关于我信息")
    @PutMapping("/admin/about")
    public Result<?> updateAbout(@Valid @RequestBody BlogInfoVO blogInfoVO) {
        blogInfoService.updateAbout(blogInfoVO);
        return Result.ok();
    }

    /**
     * 保存语音信息
     *
     * @param voiceVO 语音信息
     * @return {@link Result<String>} 语音地址
     */
    @ApiOperation(value = "上传语音")
    @PostMapping("/voice")
    public Result<String> sendVoice(VoiceVO voiceVO) {
        webSocketService.sendVoice(voiceVO);
        return Result.ok();
    }

    /**
     * 上传访客信息
     *
     * @return {@link Result}
     */
    @PostMapping("/report")
    public Result<?> report() {
        blogInfoService.report();
        return Result.ok();
    }

    /**
     * 代理获取客户端IP
     *
     * @return {@link Result<String>} 客户端IP
     */
    @ApiOperation(value = "代理获取客户端IP")
    @GetMapping("/ip")
    public Result<String> getClientIp() {
        return requestIpplusValue("https://www.ipplus360.com/getIP");
    }

    /**
     * 代理获取客户端地理位置
     *
     * @return {@link Result<String>} 客户端地理位置
     */
    @ApiOperation(value = "代理获取客户端地理位置")
    @GetMapping("/location")
    public Result<String> getClientLocation() {
        return requestIpplusValue("https://www.ipplus360.com/getLocation");
    }

    private Result<String> requestIpplusValue(String url) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map body = response.getBody();
            if (body == null) {
                return Result.fail("远程服务未返回数据");
            }
            Object success = body.get("success");
            Object data = body.get("data");
            if (Boolean.TRUE.equals(success) && data != null) {
                return Result.ok(String.valueOf(data));
            }
            Object message = body.get("msg");
            return Result.fail(message == null ? "远程服务返回失败" : String.valueOf(message));
        } catch (Exception e) {
            return Result.fail("获取远程信息失败");
        }
    }

}

