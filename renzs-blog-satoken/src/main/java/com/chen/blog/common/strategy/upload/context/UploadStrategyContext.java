package com.chen.blog.common.strategy.upload.context;

import com.chen.blog.common.strategy.upload.UploadStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

import static com.chen.blog.common.enums.UploadModeEnum.getStrategy;


/**
 * 上传策略上下文
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service
public class UploadStrategyContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadStrategyContext.class);
    /**
     * 上传模式
     */
    @Value("${upload.mode}")
    private String uploadMode;

    @Autowired
    private Map<String, UploadStrategy> uploadStrategyMap;

    /**
     * 执行上传策略
     *
     * @param file 文件
     * @param path 路径
     * @return {@link String} 文件地址
     */
    public String executeUploadStrategy(MultipartFile file, String path) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).uploadFile(file, path);
    }


    /**
     * 执行上传策略
     *
     * @param fileName    文件名称
     * @param inputStream 输入流
     * @param path        路径
     * @return {@link String} 文件地址
     */
    public String executeUploadStrategy(String fileName, InputStream inputStream, String path) {
        return uploadStrategyMap.get(getStrategy(uploadMode)).uploadFile(fileName, inputStream, path);
    }

    /**
     * 根据文件访问地址匹配历史上传策略并删除文件，兼容运行期间切换上传模式的情况。
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            LOGGER.warn("跳过空文件地址删除请求");
            return;
        }
        LOGGER.info("收到文件删除请求，url={}, strategyCount={}", fileUrl, uploadStrategyMap.size());
        for (UploadStrategy uploadStrategy : uploadStrategyMap.values()) {
            LOGGER.debug("尝试使用上传策略删除文件，strategy={}, url={}",
                    uploadStrategy.getClass().getSimpleName(), fileUrl);
            if (uploadStrategy.deleteFile(fileUrl)) {
                LOGGER.info("文件删除策略匹配成功，strategy={}, url={}",
                        uploadStrategy.getClass().getSimpleName(), fileUrl);
                return;
            }
        }
        LOGGER.warn("未找到可处理该文件地址的上传策略，url={}", fileUrl);
    }

}
