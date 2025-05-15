package goorm.server.timedeal.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDto(
        @NotNull(message = "별점을 입력해주세요")
        @Min(1) @Max(5)
        Integer rating,

        @NotNull(message = "리뷰 내용을 입력해주세요")
        @Size(min = 10, max = 1000, message = "리뷰는 10자 이상 1000자 이하로 작성해주세요")
        String content,

        @NotNull(message = "상품 ID가 필요합니다")
        Long timeDealId
) {}
