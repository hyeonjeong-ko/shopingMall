package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.CurrentUser;
import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.dto.ReviewRequestDto;
import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.service.ReviewQueryService;
import goorm.server.timedeal.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewQueryService reviewQueryService;

    @PostMapping
    public ResponseEntity<BaseResponse<ReviewResponseDto>> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            @CurrentUser String loginId) {
        log.info("리뷰 생성 요청 - loginId: {}", loginId);
        ReviewResponseDto response = reviewService.registerReview(requestDto, loginId);
        return ResponseEntity.ok(new BaseResponse<>(response));
    }



    @GetMapping("/time-deals/{timeDealId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByTimeDeal(
            @PathVariable Long timeDealId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> reviews = reviewQueryService.getReviewsByTimeDeal(timeDealId, pageable);
        return ResponseEntity.ok(reviews);
    }
}