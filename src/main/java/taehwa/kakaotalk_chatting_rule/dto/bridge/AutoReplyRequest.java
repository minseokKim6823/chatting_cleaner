package taehwa.kakaotalk_chatting_rule.dto.bridge;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoReplyRequest {

    @NotBlank(message = "room is required")
    private String room;

    @NotBlank(message = "sender is required")
    private String sender;

    private String senderId;

    @NotBlank(message = "message is required")
    private String message;

    @Builder.Default
    private boolean fromBot = false;
}
