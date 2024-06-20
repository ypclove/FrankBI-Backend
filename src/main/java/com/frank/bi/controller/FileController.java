package com.frank.bi.controller;

import com.frank.bi.common.BaseResponse;
import com.frank.bi.common.ErrorCode;
import com.frank.bi.common.ResultUtils;
import com.frank.bi.exception.BusinessException;
import com.frank.bi.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 文件上传接口
 *
 * @author Frank
 */
@RestController
@Api(tags = "文件管理")
@RequestMapping("/oss")
public class FileController {

    @Resource
    private FileService ossService;

    /**
     * 上传头像
     *
     * @param file 上传的文件
     * @return 上传成功之后的文件路径
     */
    @PostMapping("/upload")
    @ApiOperation(value = "文件上传")
    public BaseResponse<String> uploadOssFile(@RequestPart("file") MultipartFile file) {
        // 获取上传的文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "上传文件为空");
        }
        // 返回上传到 oss 的路径
        String url = ossService.uploadFileAvatar(file);
        return ResultUtils.success(url);
    }
}
