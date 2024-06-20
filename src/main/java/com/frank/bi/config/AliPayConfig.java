package com.frank.bi.config;


/**
 * @author Frank
 */
public class AliPayConfig {
    // 商户 appid
    // public static String APPID = "";

    // 私钥，格式：pkcs8 格式
    // public static String RSA_PRIVATE_KEY = "";

    // 服务器异步通知页面路径：需 http:// 或者 https:// 格式的完整路径，不能加 ?id=123 这类自定义参数，必须外网可以正常访问
    public static String notify_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径：需 http:// 或者 https:// 格式的完整路径，不能加 ?id=123 这类自定义参数，必须外网可以正常访问，商户可以自定义同步跳转地址
    public static String return_url = "http://商户网关地址/alipay.trade.wap.pay-JAVA-UTF-8/return_url.jsp";

    // 请求网关地址
    // public static String URL = "https://openapi.alipaydev.com/gateway.do";

    public static String URL = "  https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 编码
    public static String CHARSET = "UTF-8";

    // 返回格式
    public static String FORMAT = "json";

    // 支付宝公钥
    // public static String ALIPAY_PUBLIC_KEY = "";

    // 日志记录目录
    public static String log_path = "/log";

    // RSA2
    public static String SIGNTYPE = "RSA2";
}