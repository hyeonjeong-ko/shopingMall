package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.CurrentUser;
import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.dto.CommentRequestDto;
import goorm.server.timedeal.dto.CommentResponseDto;
import goorm.server.timedeal.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class ReviewCommentController {
    private final CommentService commentService;

    @PostMapping("/reviews/{reviewId}")
    public BaseResponse<CommentResponseDto> addComment(
            @PathVariable Long reviewId,
            @Valid @RequestBody CommentRequestDto requestDto,
            @CurrentUser String loginId) {
        try {
            CommentResponseDto response = commentService.addComment(reviewId, requestDto, loginId);
            return new BaseResponse<>(response);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @GetMapping("/reviews/{reviewId}")
    public BaseResponse<Page<CommentResponseDto>> getComments(
            @PathVariable Long reviewId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        try {
            Page<CommentResponseDto> comments = commentService.getCommentsByReview(reviewId, pageable);
            return new BaseResponse<>(comments);
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }

    @DeleteMapping("/{commentId}")
    public BaseResponse<String> deleteComment(
            @PathVariable Long commentId,
            @CurrentUser String loginId) {
        try {
            commentService.deleteComment(commentId, loginId);
            return new BaseResponse<>("댓글이 삭제되었습니다.");
        } catch (BaseException e) {
            return new BaseResponse<>(e.getStatus());
        }
    }
}