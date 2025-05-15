package goorm.server.timedeal.controller;

import goorm.server.timedeal.dto.CommentRequestDto;
import goorm.server.timedeal.dto.CommentResponseDto;
import goorm.server.timedeal.dto.ReviewRequestDto;
import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();  // loginId를 가져옴
        ReviewResponseDto response = reviewService.createReview(requestDto, loginId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/time-deals/{timeDealId}")
    public ResponseEntity<Page<ReviewResponseDto>> getReviewsByTimeDeal(
            @PathVariable Long timeDealId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ReviewResponseDto> reviews = reviewService.getReviewsByTimeDeal(timeDealId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        CommentResponseDto response = reviewService.addComment(reviewId, requestDto, loginId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long reviewId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CommentResponseDto> comments = reviewService.getComments(reviewId, pageable);
        return ResponseEntity.ok(comments);
    }




    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        String loginId = userDetails.getUsername();
        reviewService.deleteComment(commentId, loginId);
        return ResponseEntity.noContent().build();
    }

}