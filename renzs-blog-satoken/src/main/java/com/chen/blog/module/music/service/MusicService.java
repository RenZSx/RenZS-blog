package com.chen.blog.module.music.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.music.dto.MusicDTO;
import com.chen.blog.module.music.entity.Music;

import java.util.List;

/**
 * 音乐服务。
 */
public interface MusicService extends IService<Music> {

    /**
     * 保存上传成功的音乐记录。
     *
     * @param musicName 音乐名称
     * @param musicUrl 文件服务器地址
     * @return 保存后的音乐信息
     */
    MusicDTO saveMusic(String musicName, String musicUrl);

    /**
     * 按上传时间倒序获取音乐列表。
     *
     * @return 音乐列表
     */
    List<MusicDTO> listMusic();
}
