package taehwa.kakaotalk_chatting_rule.global.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import taehwa.kakaotalk_chatting_rule.global.response.BaseResponse;
import taehwa.kakaotalk_chatting_rule.global.response.ErrorCode;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        log.warn("필수 파라미터 누락: {}", e.getParameterName());
        BaseResponse<Void> response = BaseResponse.error(400, "필수 파라미터가 누락되었습니다: " + e.getParameterName());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("유효성 검증에 실패했습니다.");
        log.warn("유효성 검증 실패: {}", message);
        BaseResponse<Void> response = BaseResponse.error(400, message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAuthorizationDenied(AuthorizationDeniedException e) {
        log.warn("권한 거부");
        BaseResponse<Void> response = BaseResponse.error(ErrorCode.FORBIDDEN);
        return ResponseEntity.status(403).body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("비즈니스 예외: {}", errorCode.getMessage());
        BaseResponse<Void> response = BaseResponse.error(errorCode);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpected(Exception e) {
        log.error("예상하지 못한 예외 발생", e);
        BaseResponse<Void> response = BaseResponse.error(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.status(500).body(response);
    }
}
