package goorm.server.timedeal.dto;

import goorm.server.timedeal.model.Review;

import java.time.LocalDateTime;
import java.util.List;

// ReviewResponseDto.java
public record ReviewResponseDto(
        Long reviewId,
        String userName,
        Integer rating,
        String content,
        LocalDateTime createdAt,
        List<CommentResponseDto> comments
) {
    // 빌더 패턴이 필요한 경우 정적 메서드로 구현
    public static ReviewResponseDto of(Review review, boolean includeComments) {
        return new ReviewResponseDto(
                review.getReviewId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                includeComments ? getComments(review) : List.of() // 댓글포함여부결정
        );

    }

    public static ReviewResponseDto from(Review review) {
        return of(review, false);  // 기본적으로 댓글은 제외
    }

    public static ReviewResponseDto withComments(Review review) {
        return of(review, true);   // 댓글 포함
    }

    private static List<CommentResponseDto> getComments(Review review) {
        return review.getComments().stream()
                .filter(comment -> comment.getDeletedAt() == null)  // 삭제되지 않은 댓글만
                .map(CommentResponseDto::from)
                .toList();
    }

}
