package taehwa.kakaotalk_chatting_rule.dto.bridge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoReplyDecisionResponse {

    private boolean shouldReply;
    private boolean violated;
    private String room;
    private String sender;
    private String userKey;
    private String replyMessage;
    private String violationType;
    private String restrictionLevel;
    private String reason;
}
