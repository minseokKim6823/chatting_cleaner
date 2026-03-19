package taehwa.kakaotalk_chatting_rule.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public String buildViolationMessage(RestrictionLevel level, String reason) {
        return switch (level) {
            case WARNING -> String.format(
                    "[경고] %s이(가) 감지되었습니다. 반복 시 이용이 제한될 수 있습니다.", reason);
            case TEMPORARY_BAN -> String.format(
                    "[일시 제한] %s(으)로 인해 채팅이 일시적으로 제한되었습니다.", reason);
            case PERMANENT_BAN -> String.format(
                    "[영구 제한] %s(으)로 인해 채팅이 영구적으로 제한되었습니다.", reason);
            case NONE -> "정상 메시지입니다.";
        };
    }

    public String buildNormalMessage() {
        return "메시지가 정상적으로 처리되었습니다.";
    }
}
