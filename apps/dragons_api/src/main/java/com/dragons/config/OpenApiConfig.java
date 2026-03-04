package com.dragons.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI couponOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Dragon Coupon API")
            .description("선착순 쿠폰 발급 시스템 API 문서")
            .version("v1"));
  }
}

