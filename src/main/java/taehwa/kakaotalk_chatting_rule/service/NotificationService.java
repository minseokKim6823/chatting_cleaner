package taehwa.kakaotalk_chatting_rule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;

@Service
@RequiredArgsConstructor
public class NotificationService {

    public String buildViolationMessage(RestrictionLevel level, String reason) {
        return switch (level) {
            case WARNING -> String.format(
                    "[경고] %s가 감지되었습니다. 같은 내용이 반복되면 이용이 제한될 수 있습니다.", reason);
            case TEMPORARY_BAN -> String.format(
                    "[일시 제한] %s로 인해 채팅이 일시적으로 제한되었습니다.", reason);
            case PERMANENT_BAN -> String.format(
                    "[영구 제한] %s로 인해 채팅이 영구적으로 제한되었습니다.", reason);
            case NONE -> "정상 메시지입니다.";
        };
    }

    public String buildNormalMessage() {
        return "메시지가 정상적으로 처리되었습니다.";
    }
}
