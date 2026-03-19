package taehwa.kakaotalk_chatting_rule.global.exception;

import lombok.Getter;
import taehwa.kakaotalk_chatting_rule.global.response.ErrorCode;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
