package com.frank.bi.config;


/**
 * @author Frank
 */
public class AliPayConfig {
    // 服务器异步通知页面路径：需 http:// 或者 https:// 格式的完整路径，不能加 ?id=123 这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径：需 http:// 或者 https:// 格式的完整路径，不能加 ?id=123 这类自定义参数，必须外网可以正常访问，商户可以自定义同步跳转地址
    public static String return_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp";

    public static String URL = "  https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 日志记录目录
    public static String log_path = "/log";

    // RSA2
    public static String SIGNTYPE = "RSA2";
}