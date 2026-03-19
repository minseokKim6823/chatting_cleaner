package taehwa.kakaotalk_chatting_rule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taehwa.kakaotalk_chatting_rule.dto.bridge.AutoReplyDecisionResponse;
import taehwa.kakaotalk_chatting_rule.dto.bridge.AutoReplyRequest;
import taehwa.kakaotalk_chatting_rule.dto.restriction.ViolationJudgmentResult;
import taehwa.kakaotalk_chatting_rule.global.response.BaseResponse;
import taehwa.kakaotalk_chatting_rule.service.NotificationService;
import taehwa.kakaotalk_chatting_rule.service.PolicyJudgmentService;

@RestController
@RequestMapping("/api/bridge")
@RequiredArgsConstructor
public class AutoReplyBridgeController {

    private final PolicyJudgmentService policyJudgmentService;
    private final NotificationService notificationService;

    @GetMapping("/health")
    public ResponseEntity<BaseResponse<String>> health() {
        return ResponseEntity.ok(BaseResponse.success("bridge ok"));
    }

    @PostMapping("/auto-reply")
    public ResponseEntity<BaseResponse<AutoReplyDecisionResponse>> generateAutoReply(
            @Valid @RequestBody AutoReplyRequest request) {

        String userKey = resolveUserKey(request);

        if (request.isFromBot()) {
            return ResponseEntity.ok(BaseResponse.success(AutoReplyDecisionResponse.builder()
                    .shouldReply(false)
                    .violated(false)
                    .room(request.getRoom())
                    .sender(request.getSender())
                    .userKey(userKey)
                    .build()));
        }

        ViolationJudgmentResult result = policyJudgmentService.judge(userKey, request.getMessage());

        if (!result.isViolated()) {
            return ResponseEntity.ok(BaseResponse.success(AutoReplyDecisionResponse.builder()
                    .shouldReply(false)
                    .violated(false)
                    .room(request.getRoom())
                    .sender(request.getSender())
                    .userKey(userKey)
                    .build()));
        }

        String replyMessage = notificationService.buildViolationMessage(
                result.getDeterminedLevel(),
                result.getReason()
        );

        AutoReplyDecisionResponse response = AutoReplyDecisionResponse.builder()
                .shouldReply(true)
                .violated(true)
                .room(request.getRoom())
                .sender(request.getSender())
                .userKey(userKey)
                .replyMessage(replyMessage)
                .violationType(result.getViolationType().name())
                .restrictionLevel(result.getDeterminedLevel().name())
                .reason(result.getReason())
                .build();

        return ResponseEntity.ok(BaseResponse.success("auto reply generated", response));
    }

    private String resolveUserKey(AutoReplyRequest request) {
        if (StringUtils.hasText(request.getSenderId())) {
            return request.getSenderId();
        }
        return request.getRoom() + "::" + request.getSender();
    }
}
