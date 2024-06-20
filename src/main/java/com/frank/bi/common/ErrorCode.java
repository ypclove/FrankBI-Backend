package com.frank.bi.common;

import lombok.Getter;

/**
 * 自定义错误码
 *
 * @author Frank
 */
@Getter
public enum ErrorCode {
    /**
     * 成功
     */
    SUCCESS(0, "ok"),

    /**
     * 请求参数错误
     */
    PARAMS_ERROR(40000, "请求参数错误"),

    /**
     * 未登录
     */
    NOT_LOGIN_ERROR(40100, "未登录"),

    /**
     * 无权限
     */
    NO_AUTH_ERROR(40101, "无权限"),

    /**
     * 请求数据不存在
     */
    NOT_FOUND_ERROR(40400, "请求数据不存在"),

    /**
     * 40001 数据为空
     */
    NULL_ERROR(40001, "请求数据为空"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUEST(42900, "请求过于频繁"),

    /**
     * 禁止访问
     */
    FORBIDDEN_ERROR(40300, "禁止访问"),

    /**
     * 系统内部异常
     */
    SYSTEM_ERROR(50000, "系统内部异常"),

    /**
     * 操作失败
     */
    OPERATION_ERROR(50001, "操作失败");

    /**
     * 错误码
     */
    private final int code;

    /**
     * 提示信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
