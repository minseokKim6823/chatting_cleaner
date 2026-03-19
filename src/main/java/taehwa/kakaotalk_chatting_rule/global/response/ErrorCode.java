package taehwa.kakaotalk_chatting_rule.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(400, "잘못된 입력입니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),
    FORBIDDEN(403, "권한이 없습니다."),
    NOT_FOUND(404, "리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),

    // Member
    MEMBER_NOT_FOUND(404, "회원을 찾을 수 없습니다."),

    // Restriction
    RESTRICTION_NOT_FOUND(404, "제재 정보를 찾을 수 없습니다."),
    ALREADY_PERMANENTLY_BANNED(400, "이미 영구 제한된 사용자입니다."),

    // Policy
    POLICY_NOT_FOUND(404, "정책 정보를 찾을 수 없습니다."),

    // Kakao
    KAKAO_API_ERROR(502, "카카오 API 호출에 실패했습니다.");

    private final int status;
    private final String message;
}
