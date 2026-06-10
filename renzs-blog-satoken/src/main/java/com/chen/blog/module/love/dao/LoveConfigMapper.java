package com.chen.blog.module.love.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.love.entity.LoveConfig;
import org.springframework.stereotype.Repository;

/**
 * 纪念页基础配置 Mapper。
 *
 * 负责 `tb_love_config` 的单条配置读写。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Repository
public interface LoveConfigMapper extends BaseMapper<LoveConfig> {
}
