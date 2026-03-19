package taehwa.kakaotalk_chatting_rule.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import taehwa.kakaotalk_chatting_rule.dto.kakao.KakaoCallbackRequest;
import taehwa.kakaotalk_chatting_rule.dto.kakao.KakaoCallbackResponse;
import taehwa.kakaotalk_chatting_rule.dto.restriction.ViolationJudgmentResult;
import taehwa.kakaotalk_chatting_rule.service.NotificationService;
import taehwa.kakaotalk_chatting_rule.service.PolicyJudgmentService;

@RestController
@RequestMapping("/api/kakao")
@RequiredArgsConstructor
public class KakaoWebhookController {

    private final PolicyJudgmentService policyJudgmentService;
    private final NotificationService notificationService;

    @PostMapping("/webhook")
    public ResponseEntity<KakaoCallbackResponse> handleWebhook(
            @RequestBody KakaoCallbackRequest request) {

        String kakaoUserId = request.getUserRequest().getUser().getId();
        String utterance = request.getUserRequest().getUtterance();

        ViolationJudgmentResult result = policyJudgmentService.judge(kakaoUserId, utterance);

        String responseMessage;
        if (result.isViolated()) {
            responseMessage = notificationService.buildViolationMessage(
                    result.getDeterminedLevel(), result.getReason());
        } else {
            responseMessage = notificationService.buildNormalMessage();
        }

        return ResponseEntity.ok(KakaoCallbackResponse.of(responseMessage));
    }
}
