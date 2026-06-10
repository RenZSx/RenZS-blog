package com.chen.blog.module.message.service;

import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.common.domain.vo.ReviewVO;
import com.chen.blog.module.message.entity.Message;
import com.chen.blog.module.message.dto.MessageBackDTO;
import com.chen.blog.module.message.dto.MessageDTO;
import com.chen.blog.module.message.vo.MessageVO;

import java.util.List;

/**
 * 留言服务
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
public interface MessageService extends IService<Message> {

    /**
     * 添加留言弹幕
     *
     * @param messageVO 留言对象
     */
    void saveMessage(MessageVO messageVO);

    /**
     * 查看留言弹幕
     *
     * @return 留言列表
     */
    List<MessageDTO> listMessages();

    /**
     * 审核留言
     *
     * @param reviewVO 审查签证官
     */
    void updateMessagesReview(ReviewVO reviewVO);

    /**
     * 查看后台留言
     *
     * @param condition 条件
     * @return 留言列表
     */
    PageResult<MessageBackDTO> listMessageBackDTO(ConditionVO condition);

}

