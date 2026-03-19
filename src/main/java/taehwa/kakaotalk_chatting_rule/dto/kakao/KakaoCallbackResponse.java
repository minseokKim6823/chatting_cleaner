package taehwa.kakaotalk_chatting_rule.dto.kakao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoCallbackResponse {

    private String version;
    private Template template;

    public static KakaoCallbackResponse of(String text) {
        SimpleText simpleText = new SimpleText(text);
        Output output = new Output(simpleText);
        Template template = new Template(List.of(output));
        return new KakaoCallbackResponse("2.0", template);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Template {
        private List<Output> outputs;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Output {
        private SimpleText simpleText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleText {
        private String text;
    }
}
