package com.chen.blog.module.music.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 前端展示的音乐信息。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MusicDTO {

    /**
     * 音乐记录 id。
     */
    private Integer id;

    /**
     * 音乐名称。
     */
    private String musicName;

    /**
     * 文件服务器中的音乐地址。
     */
    private String musicUrl;

    /**
     * 上传时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
