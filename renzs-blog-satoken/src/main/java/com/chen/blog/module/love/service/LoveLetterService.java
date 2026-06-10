package com.chen.blog.module.love.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.module.love.dto.LoveLetterDTO;
import com.chen.blog.module.love.entity.LoveLetter;
import com.chen.blog.module.love.vo.LoveLetterVO;

/**
 * 飞书传信信件服务。
 *
 * 负责单篇固定信件的读取和保存。
 *
 * @author Chen
 * @date 2026/05/17
 */
public interface LoveLetterService extends IService<LoveLetter> {

    /**
     * 获取前台展示的信件内容。
     *
     * @return 当前信件内容
     */
    LoveLetterDTO getLoveLetter();

    /**
     * 获取后台可编辑的信件内容。
     *
     * @return 当前可编辑的信件内容
     */
    LoveLetterVO getAdminLoveLetter();

    /**
     * 保存单篇固定信件内容。
     *
     * @param loveLetterVO 后台提交的信件内容
     */
    void updateLoveLetter(LoveLetterVO loveLetterVO);
}
