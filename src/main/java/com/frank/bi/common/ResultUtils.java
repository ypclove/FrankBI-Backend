package com.frank.bi.common;

/**
 * 通用返回工具类
 *
 * @author Frank
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 成功数据
     * @param <T>  数据类型
     * @return ok
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败：只有错误码
     *
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败：自定义错误码和消息
     *
     * @param code    错误码
     * @param message 错误消息
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败：错误码和消息
     *
     * @param errorCode 错误码
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
