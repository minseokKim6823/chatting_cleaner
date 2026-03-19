package taehwa.kakaotalk_chatting_rule.domain.violation.entity;

public enum ViolationType {

    FORBIDDEN_WORD("금칙어"),
    ADVERTISEMENT("광고"),
    SPAM("도배"),
    PERSONAL_INFO("개인정보 노출");

    private final String description;

    ViolationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
