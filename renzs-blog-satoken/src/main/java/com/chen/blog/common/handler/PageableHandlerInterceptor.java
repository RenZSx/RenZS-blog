package com.chen.blog.common.handler;

import com.aliyun.oss.common.utils.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.blog.common.exception.BizException;
import com.chen.blog.common.constant.CommonConst;
import com.chen.blog.common.util.PageUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Optional;

import static com.chen.blog.common.enums.StatusCodeEnum.VALID_ERROR;

/**
 * 分页拦截器
 *
 * @author chenfuyun
 * @date 2021/07/18
 **/
public class PageableHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String currentPage = request.getParameter(CommonConst.CURRENT);
        String pageSize = Optional.ofNullable(request.getParameter(CommonConst.SIZE)).orElse(CommonConst.DEFAULT_SIZE);
        if (!StringUtils.isNullOrEmpty(currentPage)) {
            PageUtils.setCurrentPage(new Page<>(parsePositiveLong(currentPage), parsePositiveLong(pageSize)));
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        PageUtils.remove();
    }

    private Long parsePositiveLong(String value) {
        try {
            long parsedValue = Long.parseLong(value);
            if (parsedValue <= 0) {
                throw new BizException(VALID_ERROR);
            }
            return parsedValue;
        } catch (NumberFormatException e) {
            throw new BizException(VALID_ERROR);
        }
    }

}
