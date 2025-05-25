package goorm.server.timedeal.dto;


import goorm.server.timedeal.model.ReviewComment;

import java.time.LocalDateTime;

// CommentResponseDto.java도 record로 구현
public record CommentResponseDto(
        Long commentId,
        String userName,
        String content,
        LocalDateTime createdAt
) {
    public static CommentResponseDto from(ReviewComment comment) {
        return new CommentResponseDto(
                comment.getCommentId(),
                comment.getUser().getUsername(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}
