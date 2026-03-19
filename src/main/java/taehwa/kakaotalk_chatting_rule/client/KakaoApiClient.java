package taehwa.kakaotalk_chatting_rule.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KakaoApiClient {

    private static final Logger log = LoggerFactory.getLogger(KakaoApiClient.class);

    @Value("${kakao.api.base-url}")
    private String baseUrl;

    @Value("${kakao.api.admin-key}")
    private String adminKey;

    public void sendMessage(String kakaoUserId, String message) {
        log.info("카카오 메시지 전송 요청 - 대상: [MASKED], 메시지 길이: {}", message.length());
        // TODO: 실제 카카오 API 연동 구현
        // RestTemplate 또는 WebClient를 사용하여 카카오 메시지 전송 API 호출
    }
}
