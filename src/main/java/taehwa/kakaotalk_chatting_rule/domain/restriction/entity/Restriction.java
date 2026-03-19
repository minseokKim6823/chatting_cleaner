package taehwa.kakaotalk_chatting_rule.domain.restriction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taehwa.kakaotalk_chatting_rule.domain.member.entity.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "restriction")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Restriction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RestrictionLevel level;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public boolean isExpired() {
        if (this.level == RestrictionLevel.PERMANENT_BAN) {
            return false;
        }
        return this.expiresAt != null && LocalDateTime.now().isAfter(this.expiresAt);
    }
}
