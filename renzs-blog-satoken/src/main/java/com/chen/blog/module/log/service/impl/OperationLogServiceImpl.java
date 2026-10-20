package com.chen.blog.module.log.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.blog.module.log.dao.OperationLogDao;
import com.chen.blog.module.log.dto.OperationLogDTO;
import com.chen.blog.module.log.entity.OperationLog;
import com.chen.blog.module.log.service.OperationLogService;
import com.chen.blog.common.util.PageUtils;
import com.chen.blog.common.domain.vo.ConditionVO;
import com.chen.blog.common.domain.vo.PageResult;
import com.chen.blog.common.util.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务
 *
 * @author chenfuyun
 * @date 2021/08/08
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogDao, OperationLog> implements OperationLogService {

    @Override
    public PageResult<OperationLogDTO> listOperationLogs(ConditionVO conditionVO) {
        Page<OperationLog> page = new Page<>(PageUtils.getCurrent(), PageUtils.getSize());
        String keywords = conditionVO.getKeywords();
        boolean hasKeywords = StringUtils.isNotBlank(keywords);
        // 查询日志列表:关键字匹配模块或描述,状态筛选(1成功 0失败)独立生效
        // 关键字用 and(...) 包一层,避免 or() 与状态条件串成 (A or B and C) 的错误优先级
        Page<OperationLog> operationLogPage = this.page(page, new LambdaQueryWrapper<OperationLog>()
                .and(hasKeywords, wrapper -> wrapper
                        .like(OperationLog::getOptModule, keywords)
                        .or()
                        .like(OperationLog::getOptDesc, keywords))
                .eq(conditionVO.getStatus() != null, OperationLog::getStatus, conditionVO.getStatus())
                .orderByDesc(OperationLog::getId));
        List<OperationLogDTO> operationLogDTOList = BeanCopyUtils.copyList(operationLogPage.getRecords(), OperationLogDTO.class);
        return new PageResult<>(operationLogDTOList, (int) operationLogPage.getTotal());
    }

}

