package com.chen.blog.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OptLog {

    /**
     * @return 操作类型
     */
    String optType() default "";

    /**
     * 只记录长度、不记录原文的字段名
     * <p>
     * 部分接口的入参是整篇文章(如 AI 生成标签、SEO 信息),正文动辄上万字,
     * 原样入库会迅速撑大日志表。列在这里的字段会被替换为长度提示,既保留可审计的信息量,
     * 又不必存储全文。
     *
     * @return 需要压缩记录的字段名数组
     */
    String[] abbreviateFields() default {};

}
