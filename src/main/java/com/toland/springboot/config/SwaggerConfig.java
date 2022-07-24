package com.toland.springboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.oas.annotations.EnableOpenApi;

@EnableOpenApi
@Configuration
public class SwaggerConfig
{

    /**
     * 创建API应用
     * apiInfo() 增加API相关信息
     * 通过select()函数返回一个ApiSelectorBuilder实例,用来控制哪些接口暴露给Swagger来展现，
     * 本例采用指定扫描的包路径来定义指定要建立API的目录。
     */

    @Bean
    public Docket restApi()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("Standard Interface")
                .apiInfo(apiInfo("Spring Boot-Vue Study Project Interface test APIs", "1.0"))
                .useDefaultResponseMessages(true)
                .forCodeGeneration(false)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.toland.springboot.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 创建该API的基本信息（这些基本信息会展现在文档页面中）
     * 访问地址：http://localhost:9090/swagger-ui/index.html
     */
    private ApiInfo apiInfo(String title, String version)
    {
        return new ApiInfoBuilder()
                .title(title)
                .description("")
                .termsOfServiceUrl("https://github.com/To1and/SpringBoot-Vue")
                .contact(new Contact("Toland", "https://github.com/To1and", "zyzytoland@gmail.com"))
                .version(version)
                .build();
    }
}