package com.chen.blog.module.notice.entity;

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
 * 系统通知
 *
 * @author chen
 * @date 2026/05/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_system_notice")
public class SystemNotice {

    /**
     * 通知id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 跳转路径
     */
    private String jumpPath;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
