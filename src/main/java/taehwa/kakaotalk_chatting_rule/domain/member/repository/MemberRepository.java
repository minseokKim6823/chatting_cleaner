package taehwa.kakaotalk_chatting_rule.domain.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import taehwa.kakaotalk_chatting_rule.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoUserId(String kakaoUserId);
}
