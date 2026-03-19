package taehwa.kakaotalk_chatting_rule.global.config;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import taehwa.kakaotalk_chatting_rule.domain.policy.entity.ViolationPolicy;
import taehwa.kakaotalk_chatting_rule.domain.policy.repository.ViolationPolicyRepository;
import taehwa.kakaotalk_chatting_rule.domain.violation.entity.ViolationType;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ViolationPolicyRepository violationPolicyRepository;

    @Override
    public void run(String... args) {
        if (violationPolicyRepository.count() > 0) {
            return;
        }

        violationPolicyRepository.save(ViolationPolicy.builder()
                .violationType(ViolationType.FORBIDDEN_WORD)
                .warningThreshold(1)
                .temporaryBanThreshold(3)
                .permanentBanThreshold(5)
                .temporaryBanDurationHours(24)
                .build());

        violationPolicyRepository.save(ViolationPolicy.builder()
                .violationType(ViolationType.ADVERTISEMENT)
                .warningThreshold(1)
                .temporaryBanThreshold(2)
                .permanentBanThreshold(4)
                .temporaryBanDurationHours(48)
                .build());

        violationPolicyRepository.save(ViolationPolicy.builder()
                .violationType(ViolationType.SPAM)
                .warningThreshold(1)
                .temporaryBanThreshold(3)
                .permanentBanThreshold(6)
                .temporaryBanDurationHours(12)
                .build());

        violationPolicyRepository.save(ViolationPolicy.builder()
                .violationType(ViolationType.PERSONAL_INFO)
                .warningThreshold(1)
                .temporaryBanThreshold(2)
                .permanentBanThreshold(3)
                .temporaryBanDurationHours(72)
                .build());

        log.info("기본 위반 정책 데이터 초기화 완료");
    }
}
