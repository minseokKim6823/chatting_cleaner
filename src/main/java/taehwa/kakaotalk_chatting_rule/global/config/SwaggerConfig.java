package taehwa.kakaotalk_chatting_rule.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("Kakaotalk Chatting Rule API")
                        .description("카카오톡 채팅 제재 및 웹훅 처리 API 문서")
                        .version("v1"));
    }
}
