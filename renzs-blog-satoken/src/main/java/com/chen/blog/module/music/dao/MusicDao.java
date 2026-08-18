package com.chen.blog.module.music.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.music.entity.Music;
import org.springframework.stereotype.Repository;

/**
 * 音乐数据访问层。
 */
@Repository
public interface MusicDao extends BaseMapper<Music> {
}
