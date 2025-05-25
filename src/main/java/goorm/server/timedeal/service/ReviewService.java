package goorm.server.timedeal.service;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.*;
import goorm.server.timedeal.model.*;
import goorm.server.timedeal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewValidator reviewValidator;

    private final TimeDealService timeDealService;
    private final UserService userService;


    public ReviewResponseDto registerReview(ReviewRequestDto dto, String loginId) throws BaseException {
        User reviewer = userService.findByLoginId(loginId);
        TimeDeal timeDeal = timeDealService.findTimeDealById(dto.timeDealId());

        reviewValidator.validateReviewRegistration(timeDeal, reviewer);

        Review savedReview = saveReview(dto, reviewer, timeDeal);
        return convertToReviewResponse(savedReview);
    }

    private Review saveReview(ReviewRequestDto dto, User reviewer, TimeDeal timeDeal) {
        Review newReview = Review.builder()
                .user(reviewer)
                .timeDeal(timeDeal)
                .rating(dto.rating())
                .content(dto.content())
                .build();

        return reviewRepository.save(newReview);
    }

    private ReviewResponseDto convertToReviewResponse(Review review) {
        return ReviewResponseDto.from(review);
    }


}