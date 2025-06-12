package goorm.server.timedeal.config.exception.domain;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;

public class TimeDealException extends BaseException {

    public TimeDealException(BaseResponseStatus status) {
        super(status);
    }
}
