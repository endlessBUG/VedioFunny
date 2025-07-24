package com.vediofun.model.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger API文档配置
 * 
 * @author VedioFun Team
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VedioFun 模型服务 API")
                        .description("🤖 VedioFun AI模型管理与推理服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("VedioFun Team")
                                .email("admin@vediofun.com")
                                .url("https://vediofun.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
} 