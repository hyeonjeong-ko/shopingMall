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
    public static ReviewResponseDto from(Review review) {
        return new ReviewResponseDto(
                review.getReviewId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getComments().stream()
                        .map(CommentResponseDto::from)
                        .toList()
        );
    }
}
