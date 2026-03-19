package taehwa.kakaotalk_chatting_rule.dto.restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionDetailResponse {

    private Long restrictionId;
    private String kakaoUserId;
    private String level;
    private String reason;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private boolean active;
}
