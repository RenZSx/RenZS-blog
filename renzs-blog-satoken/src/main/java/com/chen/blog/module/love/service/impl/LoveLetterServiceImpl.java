package com.chen.blog.module.love.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.love.dao.LoveLetterDao;
import com.chen.blog.module.love.dto.LoveLetterDTO;
import com.chen.blog.module.love.entity.LoveLetter;
import com.chen.blog.module.love.service.LoveLetterService;
import com.chen.blog.module.love.vo.LoveLetterVO;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 飞书传信服务实现。
 * 负责独立读写 tb_love_letter，不再依赖纪念页配置表。
 *
 * @author Chen
 * @date 2026/05/17
 */
@Service
public class LoveLetterServiceImpl extends ServiceImpl<LoveLetterDao, LoveLetter> implements LoveLetterService {

    /**
     * 默认信件记录主键。
     */
    private static final Integer DEFAULT_LOVE_LETTER_ID = 1;

    @Override
    public LoveLetterDTO getLoveLetter() {
        LoveLetter loveLetter = getCurrentLoveLetter();
        return LoveLetterDTO.builder()
                .letterTitle(loveLetter.getLetterTitle())
                .letterContent(loveLetter.getLetterContent())
                .build();
    }

    @Override
    public LoveLetterVO getAdminLoveLetter() {
        LoveLetter loveLetter = getCurrentLoveLetter();
        return LoveLetterVO.builder()
                .letterTitle(loveLetter.getLetterTitle())
                .letterContent(loveLetter.getLetterContent())
                .build();
    }

    @Override
    public void updateLoveLetter(LoveLetterVO loveLetterVO) {
        LoveLetter loveLetter = LoveLetter.builder()
                .id(DEFAULT_LOVE_LETTER_ID)
                .letterTitle(loveLetterVO.getLetterTitle())
                .letterContent(loveLetterVO.getLetterContent())
                .build();
        this.saveOrUpdate(loveLetter);
    }

    /**
     * 获取当前信件记录，不存在时返回空白默认值。
     *
     * @return 信件实体
     */
    private LoveLetter getCurrentLoveLetter() {
        return Optional.ofNullable(this.getById(DEFAULT_LOVE_LETTER_ID))
                .orElseGet(() -> LoveLetter.builder()
                        .id(DEFAULT_LOVE_LETTER_ID)
                        .letterTitle("")
                        .letterContent("")
                        .build());
    }
}
