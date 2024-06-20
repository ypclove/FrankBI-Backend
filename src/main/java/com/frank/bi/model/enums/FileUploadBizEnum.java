package com.frank.bi.model.enums;

import lombok.Getter;

/**
 * 文件上传业务类型枚举
 *
 * @author Frank
 */
@Getter
public enum FileUploadBizEnum {

    USER_AVATAR("用户头像", "user_avatar");

    private final String text;

    private final String value;

    FileUploadBizEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
