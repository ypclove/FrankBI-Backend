package com.frank.bi.model.enums;

import lombok.Getter;

/**
 * 图表状态枚举类
 *
 * @author Frank
 */
@Getter
public enum AiAssistantStatusEnum {

    WAIT("等待", "wait"),
    RUNNING("生成中", "running"),
    SUCCEED("成功生成", "succeed"),
    FAILED("生成失败", "failed");

    private final String text;

    private final String value;

    AiAssistantStatusEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
