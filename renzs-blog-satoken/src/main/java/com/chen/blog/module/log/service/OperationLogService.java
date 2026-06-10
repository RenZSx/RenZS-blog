package com.chen.blog.module.log.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.module.log.dto.OperationLogDTO;
import com.chen.blog.module.log.entity.OperationLog;

/**
 * 操作日志服务
 *
 * @author chenfuyun
 * @date 2021/07/29
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 查询日志列表
     *
     * @param conditionVO 条件
     * @return 日志列表
     */
    PageResult<OperationLogDTO> listOperationLogs(ConditionVO conditionVO);

}

