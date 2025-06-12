package goorm.server.timedeal.config.exception.domain;


import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;

public class ReviewWithoutPurchaseException extends BaseException {
    private final Long userId;
    private final Long timeDealId;

    public ReviewWithoutPurchaseException(Long userId, Long timeDealId) {
        super(BaseResponseStatus.PURCHASE_NOT_FOUND);
        this.userId = userId;
        this.timeDealId = timeDealId;
    }

    @Override
    public String getMessage() {
        return String.format("사용자(ID: %d)가 상품(TimeDeal ID: %d)을 구매하지 않아 리뷰를 작성할 수 없습니다.",
            userId, timeDealId);
    }
}