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
import taehwa.kakaotalk_chatting_rule.domain.restriction.entity.RestrictionLevel;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationRecord;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;
import taehwa.kakaotalk_chatting_rule.domain.violation.repository.ViolationRecordRepository;
import taehwa.kakaotalk_chatting_rule.dto.restriction.ViolationJudgmentResult;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PolicyJudgmentService {

    private static final Logger log = LoggerFactory.getLogger(PolicyJudgmentService.class);

    private static final List<String> ADVERTISEMENT_KEYWORDS = List.of(
            "http://", "https://", "무료상담", "부업", "수익보장", "카톡추가"
    );

    private static final Pattern PERSONAL_INFO_PATTERN = Pattern.compile(
            "(\\d{3}-\\d{3,4}-\\d{4})|(\\d{6}-[1-4]\\d{6})"
    );

    private final MemberRepository memberRepository;
    private final ViolationRecordRepository violationRecordRepository;
    private final ViolationPolicyRepository violationPolicyRepository;
    private final RestrictionService restrictionService;
    private final ForbiddenWordProvider forbiddenWordProvider;

    @Transactional
    public ViolationJudgmentResult judge(String kakaoUserId, String message) {
        ViolationType detectedType = detectViolation(message);

        if (detectedType == null) {
            return ViolationJudgmentResult.builder()
                    .violated(false)
                    .build();
        }

        Member member = memberRepository.findByKakaoUserId(kakaoUserId)
                .orElseGet(() -> memberRepository.save(
                        Member.builder().kakaoUserId(kakaoUserId).build()
                ));

        RestrictionLevel determinedLevel = determineRestrictionLevel(member, detectedType);
        String reason = detectedType.getDescription() + " 정책 위반";

        member.incrementViolationCount();
        member.applyRestriction(determinedLevel);
        memberRepository.save(member);

        ViolationRecord record = ViolationRecord.builder()
                .member(member)
                .violationType(detectedType)
                .reason(reason)
                .resultLevel(determinedLevel)
                .build();
        violationRecordRepository.save(record);

        restrictionService.applyRestriction(member, determinedLevel, reason);

        log.info("위반 감지 - 사용자 [MASKED], 유형: {}, 결정: {}", detectedType, determinedLevel);

        return ViolationJudgmentResult.builder()
                .violated(true)
                .violationType(detectedType)
                .determinedLevel(determinedLevel)
                .reason(reason)
                .build();
    }

    private ViolationType detectViolation(String message) {
        if (containsPersonalInfo(message)) {
            return ViolationType.PERSONAL_INFO;
        }
        if (containsForbiddenWord(message)) {
            return ViolationType.FORBIDDEN_WORD;
        }
        if (containsAdvertisement(message)) {
            return ViolationType.ADVERTISEMENT;
        }
        return null;
    }

    private boolean containsForbiddenWord(String message) {
        return forbiddenWordProvider.contains(message);
    }

    private boolean containsAdvertisement(String message) {
        String lowerMessage = message.toLowerCase();
        return ADVERTISEMENT_KEYWORDS.stream().anyMatch(lowerMessage::contains);
    }

    private boolean containsPersonalInfo(String message) {
        return PERSONAL_INFO_PATTERN.matcher(message).find();
    }

    private RestrictionLevel determineRestrictionLevel(Member member, ViolationType violationType) {
        if (member.getCurrentRestrictionLevel() == RestrictionLevel.PERMANENT_BAN) {
            return RestrictionLevel.PERMANENT_BAN;
        }

        ViolationPolicy policy = violationPolicyRepository
                .findByViolationTypeAndActiveTrue(violationType)
                .orElse(null);

        long violationCount = violationRecordRepository
                .countByMemberAndViolationType(member, violationType) + 1;

        if (policy != null) {
            return determineLevelByPolicy(violationCount, policy);
        }

        return determineLevelByDefault(violationCount);
    }

    private RestrictionLevel determineLevelByPolicy(long violationCount, ViolationPolicy policy) {
        if (violationCount >= policy.getPermanentBanThreshold()) {
            return RestrictionLevel.PERMANENT_BAN;
        }
        if (violationCount >= policy.getTemporaryBanThreshold()) {
            return RestrictionLevel.TEMPORARY_BAN;
        }
        if (violationCount >= policy.getWarningThreshold()) {
            return RestrictionLevel.WARNING;
        }
        return RestrictionLevel.WARNING;
    }

    private RestrictionLevel determineLevelByDefault(long violationCount) {
        if (violationCount >= 5) {
            return RestrictionLevel.PERMANENT_BAN;
        }
        if (violationCount >= 3) {
            return RestrictionLevel.TEMPORARY_BAN;
        }
        return RestrictionLevel.WARNING;
    }
}
