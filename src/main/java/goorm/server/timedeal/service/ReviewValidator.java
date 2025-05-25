package goorm.server.timedeal.service;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
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

    public void validateReviewRegistration(TimeDeal timeDeal, User user) throws BaseException {
        Purchase purchase = purchaseService.validatePurchaseExists(user, timeDeal);
        validateNoExistingReview(purchase);
    }

    public void validateCommentDeletion(ReviewComment comment, User user) throws BaseException {
        if (canDeleteComment(comment, user)) {
            return;
        }
        throwUnauthorizedCommentDelete();
    }

    private void validateNoExistingReview(Purchase purchase) throws BaseException {
        if (reviewQueryService.hasActiveReview(purchase)) {
            throwDuplicateReviewError();
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

    private void throwUnauthorizedCommentDelete() throws BaseException {
        throw new BaseException(BaseResponseStatus.UNAUTHORIZED_COMMENT_DELETE);
    }
}