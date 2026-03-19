package taehwa.kakaotalk_chatting_rule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import taehwa.kakaotalk_chatting_rule.dto.restriction.RestrictionCreateRequest;
import taehwa.kakaotalk_chatting_rule.dto.restriction.RestrictionDetailResponse;
import taehwa.kakaotalk_chatting_rule.global.response.BaseResponse;
import taehwa.kakaotalk_chatting_rule.service.RestrictionService;

import java.util.List;

@RestController
@RequestMapping("/api/admin/restrictions")
@RequiredArgsConstructor
public class RestrictionController {

    private final RestrictionService restrictionService;

    @PostMapping
    public BaseResponse<RestrictionDetailResponse> createRestriction(
            @Valid @RequestBody RestrictionCreateRequest request) {
        RestrictionDetailResponse response = restrictionService.createRestriction(request);
        return BaseResponse.success("제재가 적용되었습니다.", response);
    }

    @GetMapping("/{restrictionId}")
    public BaseResponse<RestrictionDetailResponse> getRestrictionDetail(
            @PathVariable Long restrictionId) {
        RestrictionDetailResponse response = restrictionService.getRestrictionDetail(restrictionId);
        return BaseResponse.success(response);
    }

    @GetMapping
    public BaseResponse<List<RestrictionDetailResponse>> getRestrictions(
            @RequestParam String kakaoUserId) {
        List<RestrictionDetailResponse> response = restrictionService.getRestrictionsByKakaoUserId(kakaoUserId);
        return BaseResponse.success(response);
    }
}
