package taehwa.kakaotalk_chatting_rule.domain.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;

@Entity
@Table(name = "violation_policy")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ViolationType violationType;

    @Column(nullable = false)
    private int warningThreshold;

    @Column(nullable = false)
    private int temporaryBanThreshold;

    @Column(nullable = false)
    private int permanentBanThreshold;

    @Column(nullable = false)
    @Builder.Default
    private int temporaryBanDurationHours = 24;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
