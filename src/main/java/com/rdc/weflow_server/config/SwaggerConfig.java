package com.rdc.weflow_server.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weflow API 명세")
                        .description("웹 에이전시 프로젝트 관리 시스템 API")
                        .version("v1.0.0"));
    }
}

