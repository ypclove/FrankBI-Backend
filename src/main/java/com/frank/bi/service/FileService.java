package com.frank.bi.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author Frank
 */
public interface FileService {

    /**
     * 上传头像到 OSS
     *
     * @param file 文件
     * @return 上传成功之后的文件路径
     */
    String uploadFileAvatar(MultipartFile file);
}
