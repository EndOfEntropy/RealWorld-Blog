package io.spring.boot;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .servers(List.of(
                new Server().url("https://realworld-blog-production.up.railway.app").description("Production"),
                new Server().url("http://localhost:3000").description("Local")
            ))
            .info(new Info()
                .title("RealWorld Blog API")
                .version("1.0")
                .description("RESTful API implementation of RealWorld spec"));
    }
}