package com.chen.blog.module.love.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.love.dao.LoveConfigMapper;
import com.chen.blog.module.love.dto.LoveConfigDTO;
import com.chen.blog.module.love.entity.LoveConfig;
import com.chen.blog.module.love.service.LoveConfigService;
import com.chen.blog.module.love.vo.LoveConfigVO;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 纪念页基础配置服务实现。
 *
 * 负责将固定配置记录转换成前台和后台对象，并保存后台提交内容。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Service
public class LoveConfigServiceImpl extends ServiceImpl<LoveConfigMapper, LoveConfig> implements LoveConfigService {

    /**
     * 纪念页固定配置记录主键。
     */
    private static final Integer DEFAULT_LOVE_CONFIG_ID = 1;

    @Override
    public LoveConfigDTO getLoveConfig() {
        // 读取固定记录并组装成前台对象。
        LoveConfig loveConfig = getCurrentLoveConfig();
        return LoveConfigDTO.builder()
                .title(loveConfig.getTitle())
                .subtitle(loveConfig.getSubtitle())
                .background(loveConfig.getBackground())
                .startTime(loveConfig.getStartTime())
                .anniversaryTime(loveConfig.getAnniversaryTime())
                .anniversaryTitle(loveConfig.getAnniversaryTitle())
                .isEnabled(loveConfig.getIsEnabled())
                .build();
    }

    @Override
    public LoveConfigVO getAdminLoveConfig() {
        // 读取固定记录并组装成后台对象。
        LoveConfig loveConfig = getCurrentLoveConfig();
        return LoveConfigVO.builder()
                .title(loveConfig.getTitle())
                .subtitle(loveConfig.getSubtitle())
                .background(loveConfig.getBackground())
                .startTime(loveConfig.getStartTime())
                .anniversaryTime(loveConfig.getAnniversaryTime())
                .anniversaryTitle(loveConfig.getAnniversaryTitle())
                .isEnabled(loveConfig.getIsEnabled())
                .build();
    }

    @Override
    public void updateLoveConfig(LoveConfigVO loveConfigVO) {
        // 只维护固定记录；如果表里还没有这条记录，就直接插入一条。
        LoveConfig loveConfig = LoveConfig.builder()
                .id(DEFAULT_LOVE_CONFIG_ID)
                .title(loveConfigVO.getTitle())
                .subtitle(loveConfigVO.getSubtitle())
                .background(loveConfigVO.getBackground())
                .startTime(loveConfigVO.getStartTime())
                .anniversaryTime(loveConfigVO.getAnniversaryTime())
                .anniversaryTitle(loveConfigVO.getAnniversaryTitle())
                .isEnabled(loveConfigVO.getIsEnabled())
                .build();
        this.saveOrUpdate(loveConfig);
    }

    /**
     * 读取固定配置记录，如果不存在则返回空对象。
     *
     * @return 纪念页配置实体
     */
    private LoveConfig getCurrentLoveConfig() {
        return Optional.ofNullable(this.getById(DEFAULT_LOVE_CONFIG_ID))
                .orElseGet(() -> LoveConfig.builder()
                        .id(DEFAULT_LOVE_CONFIG_ID)
                        .title("")
                        .subtitle("")
                        .background("")
                        .startTime(null)
                        .anniversaryTime(null)
                        .anniversaryTitle("")
                        .isEnabled(1)
                        .build());
    }
}
