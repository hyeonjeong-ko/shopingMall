package goorm.server.timedeal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentRequestDto(
        @NotBlank(message = "댓글 내용을 입력해주세요")
        @Size(min = 1, max = 500, message = "댓글은 1자 이상 500자 이하로 작성해주세요")
        String content
) {}
