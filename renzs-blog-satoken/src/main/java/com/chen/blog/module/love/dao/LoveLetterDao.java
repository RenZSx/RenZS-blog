package com.chen.blog.module.love.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.love.entity.LoveLetter;
import org.springframework.stereotype.Repository;

/**
 * 飞书传信 Mapper。
 * 负责 tb_love_letter 的单条信件读写。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Repository
public interface LoveLetterDao extends BaseMapper<LoveLetter> {
}
