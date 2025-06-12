package goorm.server.timedeal.service;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.config.exception.domain.ReviewAlreadyExistsException;
import goorm.server.timedeal.config.exception.domain.ReviewWithoutPurchaseException;
import goorm.server.timedeal.config.exception.domain.UnauthorizedCommentDeleteException;
import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.ReviewComment;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewValidator {
    private final PurchaseService purchaseService;
    private final ReviewQueryService reviewQueryService;

    public void validateReviewRegistration(TimeDeal timeDeal, User user) {
        Purchase purchase = validatePurchaseAndGetDetails(timeDeal, user);
        validateNoDuplicateReview(purchase);
    }

    private Purchase validatePurchaseAndGetDetails(TimeDeal timeDeal, User user) {
        try {
            return purchaseService.validatePurchaseExists(user, timeDeal);
        } catch (ReviewWithoutPurchaseException e) {
            throw new ReviewWithoutPurchaseException(user.getUserId(), timeDeal.getTimeDealId());
        }
    }

    private void validateNoDuplicateReview(Purchase purchase) {
        if (reviewQueryService.hasActiveReview(purchase)) {
            throw new ReviewAlreadyExistsException(purchase);
        }
    }


    public void validateCommentDeletion(ReviewComment comment, User user) {
        if (!canDeleteComment(comment, user)) {
            throw new UnauthorizedCommentDeleteException(comment.getCommentId(), user.getUserId());
        }
    }

    private boolean canDeleteComment(ReviewComment comment, User user) {
        return isCommentAuthor(comment, user) || isReviewAuthor(comment, user);
    }

    private boolean isCommentAuthor(ReviewComment comment, User user) {
        return comment.getUser().equals(user);
    }

    private boolean isReviewAuthor(ReviewComment comment, User user) {
        return comment.getReview().getUser().equals(user);
    }

    // 예외 처리 메서드들을 분리하여 한 곳에서 관리
    private void throwDuplicateReviewError() throws BaseException {
        throw new BaseException(BaseResponseStatus.REVIEW_ALREADY_EXISTS);
    }
}