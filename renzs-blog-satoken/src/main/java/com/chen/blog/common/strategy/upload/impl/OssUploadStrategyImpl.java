package com.chen.blog.common.strategy.upload.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.chen.blog.common.config.OssConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * oss上传策略
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service("ossUploadStrategyImpl")
public class OssUploadStrategyImpl extends AbstractUploadStrategyImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(OssUploadStrategyImpl.class);
    @Autowired
    private OssConfigProperties ossConfigProperties;

    /**
     * 判断文件是否存在
     * @param filePath 文件路径
     * @return
     */
    @Override
    public Boolean exists(String filePath) {
        return getOssClient().doesObjectExist(ossConfigProperties.getBucketName(), filePath);
    }

    /**
     * 上传文件
     * @param path        路径
     * @param fileName    文件名
     * @param inputStream 输入流
     */
    @Override
    public void upload(String path, String fileName, InputStream inputStream) {
        getOssClient().putObject(ossConfigProperties.getBucketName(), path + fileName, inputStream);
    }

    @Override
    public String getFileAccessUrl(String filePath) {
        return ossConfigProperties.getUrl() + filePath;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        String baseUrl = trimTrailingSlash(ossConfigProperties.getUrl());
        if (fileUrl == null || !fileUrl.startsWith(baseUrl + "/")) {
            LOGGER.debug("OSS 策略不匹配文件地址，baseUrl={}, url={}", baseUrl, fileUrl);
            return false;
        }

        String objectKey = fileUrl.substring(baseUrl.length() + 1);
        LOGGER.info("OSS 开始删除对象，bucket={}, objectKey={}, url={}",
                ossConfigProperties.getBucketName(), objectKey, fileUrl);
        OSS ossClient = getOssClient();
        try {
            ossClient.deleteObject(ossConfigProperties.getBucketName(), objectKey);
            LOGGER.info("OSS 对象删除成功，bucket={}, objectKey={}",
                    ossConfigProperties.getBucketName(), objectKey);
            return true;
        } catch (RuntimeException exception) {
            LOGGER.error("OSS 对象删除失败，bucket={}, objectKey={}, url={}",
                    ossConfigProperties.getBucketName(), objectKey, fileUrl, exception);
            throw exception;
        } finally {
            ossClient.shutdown();
        }
    }

    private String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceFirst("/+$", "");
    }

    /**
     * 获取ossClient
     *
     * @return {@link OSS} ossClient
     */
    private OSS getOssClient() {
        return new OSSClientBuilder().build(ossConfigProperties.getEndpoint(), ossConfigProperties.getAccessKeyId(), ossConfigProperties.getAccessKeySecret());
    }

}
