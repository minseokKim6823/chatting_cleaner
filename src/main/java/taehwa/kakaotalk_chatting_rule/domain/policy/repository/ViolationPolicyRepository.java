package taehwa.kakaotalk_chatting_rule.domain.policy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taehwa.kakaotalk_chatting_rule.domain.policy.entity.ViolationPolicy;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;

import java.util.Optional;

public interface ViolationPolicyRepository extends JpaRepository<ViolationPolicy, Long> {

    Optional<ViolationPolicy> findByViolationTypeAndActiveTrue(ViolationType violationType);
}
