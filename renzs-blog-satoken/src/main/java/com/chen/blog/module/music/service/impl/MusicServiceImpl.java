package com.chen.blog.module.music.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.common.util.BeanCopyUtils;
import com.chen.blog.module.music.dao.MusicDao;
import com.chen.blog.module.music.dto.MusicDTO;
import com.chen.blog.module.music.entity.Music;
import com.chen.blog.module.music.service.MusicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 音乐服务实现。
 */
@Service
public class MusicServiceImpl extends ServiceImpl<MusicDao, Music> implements MusicService {

    /**
     * 保存音乐元数据，文件地址已经由上传策略写入文件服务器。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public MusicDTO saveMusic(String musicName, String musicUrl) {
        Music music = Music.builder()
                .musicName(musicName)
                .musicUrl(musicUrl)
                .build();
        save(music);
        return BeanCopyUtils.copyObject(music, MusicDTO.class);
    }

    @Override
    public List<MusicDTO> listMusic() {
        List<Music> musicList = baseMapper.selectList(new LambdaQueryWrapper<Music>()
                .orderByDesc(Music::getCreateTime)
                .orderByDesc(Music::getId));
        return BeanCopyUtils.copyList(musicList, MusicDTO.class);
    }
}
