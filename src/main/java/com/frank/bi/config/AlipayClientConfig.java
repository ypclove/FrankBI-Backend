package com.frank.bi.config;

import com.alipay.api.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * @author Frank
 * @date 2024/6/20
 */
@Configuration
@PropertySource("classpath:application.yml")
public class AlipayClientConfig {

    @Resource
    private Environment config;

    @Bean
    public AlipayClient alipayClient() throws AlipayApiException {
        AlipayConfig alipayConfig = new AlipayConfig();
        // 设置网关地址
        alipayConfig.setServerUrl(config.getProperty("gateway-url"));
        // 设置应用 APPID
        alipayConfig.setAppId(config.getProperty("app-id"));
        // 设置应用私钥
        alipayConfig.setPrivateKey(config.getProperty("merchant-private-key"));
        // 设置请求格式，固定值 json
        alipayConfig.setFormat(AlipayConstants.FORMAT_JSON);
        // 设置字符集
        alipayConfig.setCharset(AlipayConstants.CHARSET_UTF8);
        // 设置支付宝公钥
        alipayConfig.setAlipayPublicKey(config.getProperty("alipay-public-key"));
        // 设置签名类型
        alipayConfig.setSignType(AlipayConstants.SIGN_TYPE_RSA2);
        // 构造client
        return new DefaultAlipayClient(alipayConfig);
    }
}
