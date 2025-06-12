package goorm.server.timedeal.config.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result"})
public class BaseResponse<T> { // API 요청 응답 DTO

	// 요청이 성공 여부 (true or false)
	@JsonProperty("isSuccess")
	private final Boolean isSuccess;

	// 응답 메시지 (예: "요청에 성공하였습니다.")
	private final String message;

	// 상태 코드 (예: 1000 - 성공, 2000 - 오류 등)
	private final int code;

	// 요청이 성공하면 반환할 데이터. (없으면 null 가능)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private T result;

	public BaseResponse(T result) { // 요청이 성공했을 때
		this(BaseResponseStatus.SUCCESS, result);  // 기본적으로 성공 응답을 반환
	}

	public BaseResponse(BaseResponseStatus status) { // 요청이 실패했을 때
		this(status, null);
	}

	public BaseResponse(BaseResponseStatus status, T result) {
		this.isSuccess = status.isSuccess();
		this.message = status.getMessage();
		this.code = status.getCode();
		this.result = result;
	}

	// 커스텀 메시지를 위한 생성자
	public BaseResponse(BaseResponseStatus status, String customMessage, T result) {
		this.isSuccess = status.isSuccess();
		this.message = customMessage;  // 커스텀 메시지 사용
		this.code = status.getCode();
		this.result = result;
	}

}

