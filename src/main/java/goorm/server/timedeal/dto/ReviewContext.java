package goorm.server.timedeal.dto;

import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.service.TimeDealService;
import goorm.server.timedeal.service.UserService;
import lombok.Getter;

@Getter
public class ReviewContext {
    private final TimeDeal timeDeal;
    private final User reviewer;
    private final ReviewRequestDto requestDto;

    private ReviewContext(TimeDeal timeDeal, User reviewer, ReviewRequestDto requestDto) {
        this.timeDeal = timeDeal;
        this.reviewer = reviewer;
        this.requestDto = requestDto;
    }

    public static ReviewContext of(
            ReviewRequestDto dto,
            TimeDealService timeDealService,
            UserService userService,
            String loginId) {
        TimeDeal timeDeal = timeDealService.findTimeDealById(dto.timeDealId());
        User reviewer = userService.findByLoginId(loginId);
        return new ReviewContext(timeDeal, reviewer, dto);
    }

    public Integer getRating() {
        return requestDto.rating();
    }

    public String getContent() {
        return requestDto.content();
    }
}
