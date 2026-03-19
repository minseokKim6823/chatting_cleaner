package taehwa.kakaotalk_chatting_rule.domain.member.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String kakaoUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RestrictionLevel currentRestrictionLevel = RestrictionLevel.NONE;

    @Column(nullable = false)
    @Builder.Default
    private int totalViolationCount = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void incrementViolationCount() {
        this.totalViolationCount++;
    }

    public void applyRestriction(RestrictionLevel level) {
        if (level.isStrongerThan(this.currentRestrictionLevel)) {
            this.currentRestrictionLevel = level;
        }
    }
}
