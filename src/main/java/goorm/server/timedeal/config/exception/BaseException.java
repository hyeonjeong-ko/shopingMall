package goorm.server.timedeal.config.exception;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {
    private final BaseResponseStatus status;
    private final String customMessage;

    public BaseException(BaseResponseStatus status) {
        this.status = status;
        this.customMessage = null;
    }
}
