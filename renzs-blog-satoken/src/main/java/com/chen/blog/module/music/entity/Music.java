package com.chen.blog.module.music.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 音乐记录。
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_music")
public class Music {

    /**
     * 音乐记录 id。
     */
    @TableId(type = IdType.AUTO)
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
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
