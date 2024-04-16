package com.hc.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @Author: hec
 * @Date: 2024/4/16
 * @Email: 2740860037@qq.com
 * @Description: swagger接口文档
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class SwaggerConfig {

    @Value("${swagger.enabled}")
    private boolean enabled;

    /**
     * swagger配置
     *
     */
    @Bean
    public Docket docket() {
        return new Docket(DocumentationType.SWAGGER_2)
                .enable(enabled)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * apiInfo配置
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("云盘")
                .version("v1.0")
                .contact(new Contact("何超","https://github.com/He0306","2740860037@qq.com"))
                .description("基于Springboot + Vue 开发，一个仿百度云盘面向C端用户的网盘项目，包括用户注册，QQ快捷登录，文件上传，分片上传，断点续传，秒传，文件在线预览，包括文本，图片，视频，音频，excel，word ，pdf 等文件在线预览，文件分享等功能。")
                .build();
    }
}
