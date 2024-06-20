package com.frank.bi.model.enums;

import lombok.Getter;

/**
 * 图表状态枚举类
 *
 * @author Frank
 */
@Getter
public enum PayOrderEnum {

    WAIT_PAY("待付款", "0"),
    COMPLETE("已完成", "1"),
    TIMEOUT_ORDER("超时订单", "2"),
    CANCEL_ORDER("取消订单", "3");

    private final String text;

    private final String value;

    PayOrderEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }
}
