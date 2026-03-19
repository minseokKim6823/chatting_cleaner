package taehwa.kakaotalk_chatting_rule.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import taehwa.kakaotalk_chatting_rule.domain.member.entity.Member;
import taehwa.kakaotalk_chatting_rule.domain.member.repository.MemberRepository;
import taehwa.kakaotalk_chatting_rule.domain.policy.entity.ViolationPolicy;
import taehwa.kakaotalk_chatting_rule.domain.policy.repository.ViolationPolicyRepository;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.Restriction;
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;
import taehwa.kakaotalk_chatting_rule.domain.restriction.repository.RestrictionRepository;
import taehwa.kakaotalk_chatting_rule.dto.restriction.RestrictionCreateRequest;
import taehwa.kakaotalk_chatting_rule.dto.restriction.RestrictionDetailResponse;
import taehwa.kakaotalk_chatting_rule.global.exception.BusinessException;
import taehwa.kakaotalk_chatting_rule.global.response.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestrictionService {

    private static final Logger log = LoggerFactory.getLogger(RestrictionService.class);
    private static final int DEFAULT_TEMPORARY_BAN_HOURS = 24;

    private final RestrictionRepository restrictionRepository;
    private final MemberRepository memberRepository;
    private final ViolationPolicyRepository violationPolicyRepository;

    @Transactional
    public void applyRestriction(Member member, RestrictionLevel level, String reason) {
        deactivateExistingRestrictions(member);

        LocalDateTime expiresAt = null;
        if (level == RestrictionLevel.TEMPORARY_BAN) {
            int durationHours = DEFAULT_TEMPORARY_BAN_HOURS;
            expiresAt = LocalDateTime.now().plusHours(durationHours);
        }

        if (level == RestrictionLevel.NONE) {
            return;
        }

        Restriction restriction = Restriction.builder()
                .member(member)
                .level(level)
                .reason(reason)
                .expiresAt(expiresAt)
                .build();

        restrictionRepository.save(restriction);
        log.info("제재 적용 - 사용자: [MASKED], 수준: {}", level);
    }

    @Transactional
    public RestrictionDetailResponse createRestriction(RestrictionCreateRequest request) {
        Member member = memberRepository.findByKakaoUserId(request.getKakaoUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        RestrictionLevel level = RestrictionLevel.valueOf(request.getLevel());

        if (member.getCurrentRestrictionLevel() == RestrictionLevel.PERMANENT_BAN) {
            throw new BusinessException(ErrorCode.ALREADY_PERMANENTLY_BANNED);
        }

        applyRestriction(member, level, request.getReason());
        member.applyRestriction(level);
        memberRepository.save(member);

        Restriction latest = restrictionRepository
                .findTopByMemberAndActiveTrueOrderByStartedAtDesc(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTRICTION_NOT_FOUND));

        return toDetailResponse(latest);
    }

    @Transactional(readOnly = true)
    public RestrictionDetailResponse getRestrictionDetail(Long restrictionId) {
        Restriction restriction = restrictionRepository.findById(restrictionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESTRICTION_NOT_FOUND));
        return toDetailResponse(restriction);
    }

    @Transactional(readOnly = true)
    public List<RestrictionDetailResponse> getRestrictionsByKakaoUserId(String kakaoUserId) {
        Member member = memberRepository.findByKakaoUserId(kakaoUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return restrictionRepository.findByMemberAndActiveTrue(member).stream()
                .map(this::toDetailResponse)
                .toList();
    }

    private void deactivateExistingRestrictions(Member member) {
        List<Restriction> activeRestrictions = restrictionRepository.findByMemberAndActiveTrue(member);
        for (Restriction restriction : activeRestrictions) {
            restriction.setActive(false);
        }
        restrictionRepository.saveAll(activeRestrictions);
    }

    private RestrictionDetailResponse toDetailResponse(Restriction restriction) {
        return RestrictionDetailResponse.builder()
                .restrictionId(restriction.getId())
                .kakaoUserId(restriction.getMember().getKakaoUserId())
                .level(restriction.getLevel().name())
                .reason(restriction.getReason())
                .startedAt(restriction.getStartedAt())
                .expiresAt(restriction.getExpiresAt())
                .active(restriction.isActive())
                .build();
    }
}
