package com.frank.bi.service.impl;

import cn.hutool.core.date.DateTime;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.frank.bi.constant.FileConstant;
import com.frank.bi.service.FileService;
import com.frank.bi.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

/**
 * @author Frank
 */
@Service
public class FileServiceImpl implements FileService {

    /**
     * 上传头像到 OSS
     *
     * @param file 文件
     * @return 上传成功之后的文件路径
     */
    @Override
    public String uploadFileAvatar(MultipartFile file) {
        // 工具类获取值
        String endpoint = FileUtils.END_POINT;
        String accessKeyId = FileUtils.KEY_ID;
        String accessKeySecret = FileUtils.KEY_SECRET;
        String bucketName = FileUtils.BUCKET_NAME;
        InputStream inputStream;
        try {
            // 创建 OSS 实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 把文件按照日期分类，获取当前日期
            String datePath = new DateTime().toString("yyyy-MM-dd");
            // 获取上传文件的输入流
            inputStream = file.getInputStream();
            // 获取文件名称
            String originalFileName = file.getOriginalFilename();
            // 拼接日期和文件路径
            String fileName = datePath + "/" + originalFileName;
            // 判断文件是否存在
            boolean exists = ossClient.doesObjectExist(bucketName, fileName);
            if (exists) {
                // 如果文件已存在，则先删除原来的文件再进行覆盖
                ossClient.deleteObject(bucketName, fileName);
            }
            // 创建上传 Object 的 Metadata
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(inputStream.available());
            objectMetadata.setContentType(getContentType(fileName.substring(fileName.lastIndexOf("."))));
            objectMetadata.setContentDisposition("attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));

            // 调用 oss 实例中的方法实现上传
            // 参数 1：Bucket名称
            // 参数 2：上传到 oss 文件路径和文件名称 /aa/bb/1.jpg
            // 参数 3：上传文件的输入流
            ossClient.putObject(bucketName, fileName, inputStream);

            // 关闭 OSSClient
            ossClient.shutdown();

            // 把上传后文件路径返回，需要把上传到阿里云 oss 路径手动拼接出来
            return "https://" + bucketName + "." + endpoint + "/" + fileName;
        } catch (IOException e) {
            return String.valueOf(new IOException(e));
        }
    }

    /**
     * 判断文件上传时的 contentType
     *
     * @param filenameExtension 文件后缀
     * @return 文件类型
     */
    public static String getContentType(String filenameExtension) {
        if (FileConstant.BMP.equalsIgnoreCase(filenameExtension)) {
            return "image/bmp";
        }
        if (FileConstant.GIF.equalsIgnoreCase(filenameExtension)) {
            return "image/gif";
        }
        if (FileConstant.JPEG.equalsIgnoreCase(filenameExtension) || FileConstant.JPG.equalsIgnoreCase(filenameExtension)
                || FileConstant.PNG.equalsIgnoreCase(filenameExtension)) {
            return "image/jpeg";
        }
        if (FileConstant.HTML.equalsIgnoreCase(filenameExtension)) {
            return "text/html";
        }
        if (FileConstant.TXT.equalsIgnoreCase(filenameExtension)) {
            return "text/plain";
        }
        if (FileConstant.VSD.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.visio";
        }
        if (FileConstant.PPTX.equalsIgnoreCase(filenameExtension) || FileConstant.PPT.equalsIgnoreCase(filenameExtension)) {
            return "application/vnd.ms-powerpoint";
        }
        if (FileConstant.DOCX.equalsIgnoreCase(filenameExtension) || FileConstant.DOC.equalsIgnoreCase(filenameExtension)) {
            return "application/msword";
        }
        if (FileConstant.XML.equalsIgnoreCase(filenameExtension)) {
            return "text/xml";
        }
        return "application/octet-stream";
    }
}