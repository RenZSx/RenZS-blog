package com.chen.blog.module.love.entity;

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
 * 纪念页基础配置实体。
 * 对应表 tb_love_config，只保存纪念页基础信息，不包含信件内容。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_love_config")
public class LoveConfig {

    /**
     * 配置主键。
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 页面标题。
     */
    private String title;

    /**
     * 页面副标题。
     */
    private String subtitle;

    /**
     * 背景图地址。
     */
    private String background;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 纪念日时间。
     */
    private LocalDateTime anniversaryTime;

    /**
     * 纪念日标题。
     */
    private String anniversaryTitle;

    /**
     * 是否启用。
     */
    private Integer isEnabled;

    /**
     * 创建时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
