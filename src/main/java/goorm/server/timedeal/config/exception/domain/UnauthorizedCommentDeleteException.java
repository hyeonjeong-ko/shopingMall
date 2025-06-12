package goorm.server.timedeal.config.exception.domain;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;

public class UnauthorizedCommentDeleteException extends BaseException {
    private final Long commentId;
    private final Long userId;

    public UnauthorizedCommentDeleteException(Long commentId, Long userId) {
        super(BaseResponseStatus.UNAUTHORIZED_COMMENT_DELETE);
        this.commentId = commentId;
        this.userId = userId;
    }
}