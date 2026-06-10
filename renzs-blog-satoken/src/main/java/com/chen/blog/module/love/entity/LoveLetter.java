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
 * 飞书传信实体。
 * 对应表 tb_love_letter，仅保存信件正文内容。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "tb_love_letter")
public class LoveLetter {

    /**
     * 主键。
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 信件标题。
     */
    private String letterTitle;

    /**
     * 信件正文。
     */
    private String letterContent;

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
