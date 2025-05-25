package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.CurrentUser;
import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponse;

import goorm.server.timedeal.dto.ReviewRequestDto;
import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.service.ReviewQueryService;
import goorm.server.timedeal.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping
    public BaseResponse<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            @CurrentUser String loginId) {
        try {
            ReviewResponseDto response = reviewService.registerReview(requestDto, loginId);
            return new BaseResponse<>(response);  // 성공 시 SUCCESS 상태로 응답
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());  // 실패 시 해당 에러 상태로 응답
        }
    }


    @GetMapping("/time-deals/{timeDealId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByTimeDeal(
            @PathVariable Long timeDealId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> reviews = reviewQueryService.getReviewsByTimeDeal(timeDealId, pageable);
        return ResponseEntity.ok(reviews);
    }
}