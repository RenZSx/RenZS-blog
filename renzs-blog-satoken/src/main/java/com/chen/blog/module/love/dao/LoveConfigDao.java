package com.chen.blog.module.love.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.love.entity.LoveConfig;
import org.springframework.stereotype.Repository;

/**
 * 飞书传信配置表 Mapper。
 *
 * 负责单条信件配置记录的数据库读写。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Repository
public interface LoveConfigDao extends BaseMapper<LoveConfig> {

}
