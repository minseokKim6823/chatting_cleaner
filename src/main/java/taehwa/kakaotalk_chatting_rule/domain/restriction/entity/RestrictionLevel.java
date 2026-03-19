package taehwa.kakaotalk_chatting_rule.domain.restriction.entity;

public enum RestrictionLevel {

    NONE(0),
    WARNING(1),
    TEMPORARY_BAN(2),
    PERMANENT_BAN(3);

    private final int severity;

    RestrictionLevel(int severity) {
        this.severity = severity;
    }

    public int getSeverity() {
        return severity;
    }

    public boolean isStrongerThan(RestrictionLevel other) {
        return this.severity > other.severity;
    }
}
