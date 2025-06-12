package goorm.server.timedeal.config.exception.domain;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.model.Purchase;

public class ReviewAlreadyExistsException extends BaseException {
    private final Long purchaseId;
    private final Long userId;
    private final Long timeDealId;

    public ReviewAlreadyExistsException(Purchase purchase) {
        super(BaseResponseStatus.REVIEW_ALREADY_EXISTS);
        this.purchaseId = purchase.getPurchaseId();
        this.userId = purchase.getUser().getUserId();
        this.timeDealId = purchase.getTimeDeal().getTimeDealId();
    }

    @Override
    public String getMessage() {
        return String.format(
            "이미 작성된 리뷰가 존재합니다. (구매 ID: %d, 사용자 ID: %d, 타임딜 ID: %d)", 
            purchaseId, 
            userId, 
            timeDealId
        );
    }

    public Long getPurchaseId() {
        return purchaseId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTimeDealId() {
        return timeDealId;
    }
}