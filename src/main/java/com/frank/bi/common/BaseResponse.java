package com.frank.bi.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * 由三部分组成：状态码，响应数据，提示消息
 *
 * @author Frank
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 提示消息
     */
    private String message;

    /**
     * 全参构造器
     *
     * @param code    状态码
     * @param data    响应数据
     * @param message 提示消息
     */
    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    /**
     * 请求成功通用返回
     *
     * @param code 状态码
     * @param data 响应数据
     */
    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    /**
     * 请求失败通用返回
     *
     * @param errorCode 错误码
     */
    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
