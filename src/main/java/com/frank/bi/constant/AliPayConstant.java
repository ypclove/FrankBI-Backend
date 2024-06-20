package com.frank.bi.constant;

/**
 * @author Frank
 */
public interface AliPayConstant {

    /**
     * 充值一次调用次数的价格
     */
    Double PRICE = 0.1;

    /**
     * URL
     */
    String URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
 
    /**
     * 字符编码格式
     */
    String CHARSET = "UTF-8";

    /**
     * 签名算法
     */
    String SIGNTYPE = "RSA2";
}
