package com.chen.blog.common.strategy.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * 上传策略
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
public interface UploadStrategy {

    /**
     * 上传文件
     *
     * @param file 文件
     * @param path 上传路径
     * @return {@link String} 文件地址
     */
    String uploadFile(MultipartFile file, String path);

    /**
     * 上传文件
     *
     * @param fileName    文件名
     * @param inputStream 输入流
     * @param path        路径
     * @return {@link String}
     */
    String uploadFile(String fileName, InputStream inputStream, String path);

    /**
     * 删除已上传的文件。
     *
     * @param fileUrl 文件访问地址
     * @return 当前策略是否识别并处理了该地址
     */
    boolean deleteFile(String fileUrl);

}
