package com.frank.bi.model.enums;

import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author Frank
 */
@Getter
public enum UserStatusEnum {

    USER("正常", "0"),
    ADMIN("注销", "1"),
    BAN("封号", "2");

    private final String text;

    private final String value;

    UserStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
