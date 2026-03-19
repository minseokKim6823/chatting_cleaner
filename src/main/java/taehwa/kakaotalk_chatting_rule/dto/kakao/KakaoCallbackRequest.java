package taehwa.kakaotalk_chatting_rule.dto.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCallbackRequest {

    private UserRequest userRequest;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserRequest {
        private String timezone;
        private String utterance;
        private String lang;
        private User user;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class User {
        private String id;
        private String type;
    }
}
