package com.chen.blog.module.music.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.chen.blog.common.domain.vo.Result;
import com.chen.blog.common.enums.FileExtEnum;
import com.chen.blog.common.enums.FilePathEnum;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.strategy.upload.context.UploadStrategyContext;
import com.chen.blog.common.util.FileUtils;
import com.chen.blog.common.enums.RoleEnum;
import com.chen.blog.module.music.dto.MusicDTO;
import com.chen.blog.module.music.service.MusicService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * 音乐上传接口。
 */
@Api(tags = "音乐模块")
@RestController
public class MusicController {

    private static final Set<FileExtEnum> MUSIC_EXTENSIONS = EnumSet.of(
            FileExtEnum.MP3,
            FileExtEnum.WAV,
            FileExtEnum.M4A
    );

    @Autowired
    private UploadStrategyContext uploadStrategyContext;

    @Autowired
    private MusicService musicService;

    /**
     * 获取公开的音乐列表。
     */
    @ApiOperation(value = "获取音乐列表")
    @GetMapping("/music")
    public Result<List<MusicDTO>> listMusic() {
        return Result.ok(musicService.listMusic());
    }

    /**
     * 将音乐文件上传到配置的文件服务器 music/ 目录。
     */
    @ApiOperation(value = "上传音乐")
    @ApiImplicitParam(name = "file", value = "音乐文件", required = true, dataType = "MultipartFile")
    @PostMapping("/admin/music")
    public Result<MusicDTO> uploadMusic(@RequestParam("file") MultipartFile file) {
        StpUtil.checkLogin();
        StpUtil.checkRole(RoleEnum.ADMIN.getLabel());

        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的音乐文件");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = FileUtils.getExtName(originalFilename).toLowerCase();
        FileExtEnum fileExt = FileExtEnum.getFileExt(extension);
        if (fileExt == null || !MUSIC_EXTENSIONS.contains(fileExt)) {
            throw new BizException("仅支持 MP3、WAV、M4A 格式的音乐文件");
        }

        String url = uploadStrategyContext.executeUploadStrategy(file, FilePathEnum.MUSIC.getPath());
        return Result.ok(musicService.saveMusic(getMusicName(originalFilename), url));
    }

    /**
     * 从原文件名中提取不带扩展名的展示名称。
     */
    private String getMusicName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "未命名音乐";
        }
        String fileName = originalFilename.replace('\\', '/');
        int pathSeparator = fileName.lastIndexOf('/');
        if (pathSeparator >= 0) {
            fileName = fileName.substring(pathSeparator + 1);
        }
        int extensionSeparator = fileName.lastIndexOf('.');
        return extensionSeparator > 0 ? fileName.substring(0, extensionSeparator) : fileName;
    }
}
