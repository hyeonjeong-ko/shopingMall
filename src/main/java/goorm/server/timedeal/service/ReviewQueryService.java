package goorm.server.timedeal.service;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.Review;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)  // 조회 전용이므로 readOnly
@RequiredArgsConstructor
public class ReviewQueryService {
    private final ReviewRepository reviewRepository;
    private final TimeDealService timeDealService;


    private Page<ReviewResponseDto> findActiveReviews(TimeDeal timeDeal, Pageable pageable) {
        return reviewRepository.findActiveReviewsForTimeDeal(timeDeal, pageable)
                .map(ReviewResponseDto::from);
    }
    public boolean hasActiveReview(Purchase purchase) {
        return reviewRepository.existsByPurchaseAndDeletedAtIsNull(purchase);
    }

    public boolean existsActiveReview(Purchase purchase) {
        return reviewRepository.existsByPurchaseAndDeletedAtIsNull(purchase);
    }

    public Review findById(Long reviewId) throws BaseException {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.REVIEW_NOT_FOUND));
    }

    public Page<ReviewResponseDto> findReviewsByTimeDeal(TimeDeal timeDeal, Pageable pageable) {
        return reviewRepository.findActiveReviewsForTimeDeal(timeDeal, pageable)
                .map(ReviewResponseDto::from);
    }

    public Page<ReviewResponseDto> getReviewsByTimeDeal(Long timeDealId, Pageable pageable) {
        TimeDeal timeDeal = timeDealService.findTimeDealById(timeDealId);
        return findActiveReviews(timeDeal, pageable);
    }

    // 헬퍼 메서드들
    public Review findReviewById(Long reviewId) throws BaseException {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.REVIEW_NOT_FOUND));
    }

}