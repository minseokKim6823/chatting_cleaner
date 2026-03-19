package taehwa.kakaotalk_chatting_rule.domain.violation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taehwa.kakaotalk_chatting_rule.domain.member.entity.Member;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationRecord;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;

public interface ViolationRecordRepository extends JpaRepository<ViolationRecord, Long> {

    long countByMemberAndViolationType(Member member, ViolationType violationType);
}
