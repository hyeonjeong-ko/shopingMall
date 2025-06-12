package goorm.server.timedeal.config.exception.domain;


import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import lombok.Getter;

@Getter
public class StockUnavailableException extends BaseException {
    private final int remainingStock;

    public StockUnavailableException(int remainingStock) {
        super(BaseResponseStatus.STOCK_UNAVAILABLE);
        this.remainingStock = remainingStock;
    }
}

