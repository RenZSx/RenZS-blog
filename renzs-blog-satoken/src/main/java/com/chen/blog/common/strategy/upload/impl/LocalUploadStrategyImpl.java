package com.chen.blog.common.strategy.upload.impl;

import com.chen.blog.common.enums.FileExtEnum;
import com.chen.blog.common.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.URI;
import java.util.Objects;

/**
 * 本地上传策略
 *
 * @author chenfuyun
 * @date 2021/07/28
 */
@Service("localUploadStrategyImpl")
public class LocalUploadStrategyImpl extends AbstractUploadStrategyImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalUploadStrategyImpl.class);

    /**
     * 本地路径
     */
    @Value("${upload.local.path}")
    private String localPath;

    /**
     * 访问url
     */
    @Value("${upload.local.url}")
    private String localUrl;

    @Override
    public Boolean exists(String filePath) {
        return new File(localPath + filePath).exists();
    }

    @Override
    public void upload(String path, String fileName, InputStream inputStream) throws IOException {
        // 判断目录是否存在
        File directory = new File(localPath + path);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new BizException("创建目录失败");
            }
        }
        // 写入文件
        File file = new File(localPath + path + fileName);
        String ext = "." + fileName.split("\\.")[1];
        switch (Objects.requireNonNull(FileExtEnum.getFileExt(ext))) {
            case MD:
            case TXT:
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                while (reader.ready()) {
                    writer.write((char) reader.read());
                }
                writer.flush();
                writer.close();
                reader.close();
                break;
            default:
                BufferedInputStream bis = new BufferedInputStream(inputStream);
                BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()));
                byte[] bytes = new byte[1024];
                int length;
                while ((length = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, length);
                }
                bos.flush();
                bos.close();
                bis.close();
                break;
        }
        inputStream.close();
    }


    @Override
    public String getFileAccessUrl(String filePath) {
        return localUrl + filePath;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        String baseUrl = trimTrailingSlash(localUrl);
        if (fileUrl == null || !fileUrl.startsWith(baseUrl + "/")) {
            LOGGER.debug("本地策略不匹配文件地址，baseUrl={}, url={}", baseUrl, fileUrl);
            return false;
        }

        String relativePath;
        try {
            relativePath = new URI(fileUrl).getPath().substring(new URI(baseUrl).getPath().length());
        } catch (Exception e) {
            throw new BizException("本地文件地址无效");
        }
        relativePath = relativePath.replaceFirst("^/+", "");

        Path root = Paths.get(localPath).toAbsolutePath().normalize();
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            LOGGER.error("本地文件路径越界，root={}, target={}, url={}", root, target, fileUrl);
            throw new BizException("文件路径无效");
        }
        LOGGER.info("本地文件开始删除，path={}, url={}", target, fileUrl);
        try {
            boolean deleted = Files.deleteIfExists(target);
            LOGGER.info("本地文件删除完成，deleted={}, path={}", deleted, target);
            return true;
        } catch (IOException e) {
            LOGGER.error("本地文件删除失败，path={}, url={}", target, fileUrl, e);
            throw new BizException("本地文件删除失败");
        }
    }

    private String trimTrailingSlash(String value) {
        return value == null ? "" : value.replaceFirst("/+$", "");
    }

}
