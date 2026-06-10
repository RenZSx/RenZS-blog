package com.chen.blog.module.message.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chen.blog.module.message.entity.Message;
import org.springframework.stereotype.Repository;


/**
 * 留言
 *
 * @author chenfuyun
 * @date 2021/08/10
 */
@Repository
public interface MessageDao extends BaseMapper<Message> {

}
