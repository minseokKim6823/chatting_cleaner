package taehwa.kakaotalk_chatting_rule.domain.restriction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taehwa.kakaotalk_chatting_rule.domain.member.entity.Member;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.Restriction;

import java.util.List;
import java.util.Optional;

public interface RestrictionRepository extends JpaRepository<Restriction, Long> {

    List<Restriction> findByMemberAndActiveTrue(Member member);

    Optional<Restriction> findTopByMemberAndActiveTrueOrderByStartedAtDesc(Member member);
}
