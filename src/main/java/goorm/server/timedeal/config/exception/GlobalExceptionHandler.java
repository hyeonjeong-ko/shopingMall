package goorm.server.timedeal.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Object>> handleBaseException(BaseException ex) {
        BaseResponse<Object> response = new BaseResponse<>(ex.getStatus());
        return ResponseEntity
                .status(ex.getStatus().getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        BaseResponseStatus status = BaseResponseStatus.INVALID_INPUT;
        BaseResponse<Object> response = new BaseResponse<>(status);
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Object>> handleMissingParams(
            MissingServletRequestParameterException ex) {
        log.error("필수 파라미터 누락: {}", ex.getMessage());

        String message = String.format("필수 파라미터가 누락되었습니다. '%s' (%s 타입)",
                ex.getParameterName(),
                ex.getParameterType());

        BaseResponse<Object> response = new BaseResponse<>(
                BaseResponseStatus.INVALID_INPUT,
                message,
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleUnexpected(Exception ex) {
        ex.printStackTrace();
        BaseResponseStatus status = BaseResponseStatus.ERROR;
        BaseResponse<Object> response = new BaseResponse<>(status);
        return ResponseEntity
                .status(status.getHttpStatus())
                .body(response);
    }
}