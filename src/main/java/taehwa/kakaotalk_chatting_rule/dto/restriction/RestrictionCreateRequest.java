package taehwa.kakaotalk_chatting_rule.dto.restriction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionCreateRequest {

    @NotBlank(message = "카카오 사용자 ID는 필수입니다.")
    private String kakaoUserId;

    @NotNull(message = "제재 수준은 필수입니다.")
    private String level;

    @NotBlank(message = "제재 사유는 필수입니다.")
    private String reason;
}
