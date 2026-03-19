package taehwa.kakaotalk_chatting_rule.dto.restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationJudgmentResult {

    private boolean violated;
    private ViolationType violationType;
    private RestrictionLevel determinedLevel;
    private String reason;
}
