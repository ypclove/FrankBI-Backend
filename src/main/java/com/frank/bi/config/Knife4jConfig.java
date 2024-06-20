package com.frank.bi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Knife4j 接口文档配置
 *
 * @author Frank
 * {@link <a href="https://doc.xiaominfo.com/knife4j/documentation/get_start.html">Knife4j 相关配置</a>}
 */
@Configuration
@EnableSwagger2
@Profile({"dev", "prod"})  // 版本控制访问
public class Knife4jConfig {
    @Bean
    public Docket defaultApi2() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                // 指定 Controller 扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.frank.bi.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 自定义接口文档信息
     *
     * @return ApiInfoBuilder
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                // 接口文档的标题
                .title("智能BI系统")
                // 接口文档的描述信息
                .description("用户只需要导入最原始的数据集，输入想要进行分析的目标（比如帮我分析一下网站的增长趋势)，就能利用 AI 自动生成一个符合要求的图表以及分析结论。此外，还会有图表管理、异步生成等功能。")
                // 提供服务者
                .termsOfServiceUrl("https://github.com/ypclove")
                .contact(new Contact("Frank", "https://blog.franksteven.me/", "franklove521126@163.com"))
                // 版本
                .version("1.0")
                // 构建
                .build();
    }
}