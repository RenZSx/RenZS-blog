package com.chen.blog.module.love.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.love.dto.LoveConfigDTO;
import com.chen.blog.module.love.entity.LoveConfig;
import com.chen.blog.module.love.vo.LoveConfigVO;

/**
 * 纪念页基础配置服务。
 *
 * 负责单条纪念页配置的读取和保存。
 *
 * @author Chen
 * @date 2026/05/17
 */
public interface LoveConfigService extends IService<LoveConfig> {

    /**
     * 获取前台展示的纪念页配置。
     *
     * @return 前台配置对象
     */
    LoveConfigDTO getLoveConfig();

    /**
     * 获取后台可编辑的纪念页配置。
     *
     * @return 后台配置对象
     */
    LoveConfigVO getAdminLoveConfig();

    /**
     * 保存纪念页基础配置。
     *
     * @param loveConfigVO 后台提交的配置
     */
    void updateLoveConfig(LoveConfigVO loveConfigVO);
}
