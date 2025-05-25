package goorm.server.timedeal.config.exception;

import lombok.Getter;

@Getter
public enum BaseResponseStatus {

	SUCCESS(true, 1000, "요청에 성공하였습니다."),
	ERROR(false, 2000, "서버 오류가 발생했습니다."),
	FORBIDDEN(false, 2003, "권한이 없는 유저의 접근입니다."),

	STOCK_UNAVAILABLE(false, 2010, "재고가 부족합니다."),

	INVALID_INPUT(false, 2011, "잘못된 입력입니다."),
	TIME_DEAL_NOT_FOUND(false, 2012, "해당 타임딜을 찾을 수 없습니다."),


	// Review 관련 상태
	PURCHASE_NOT_FOUND(false, 2020, "구매 내역이 없는 상품입니다."),
	REVIEW_ALREADY_EXISTS(false, 2021, "이미 리뷰를 작성하셨습니다."),
	REVIEW_NOT_FOUND(false, 2022, "리뷰를 찾을 수 없습니다."),
	COMMENT_NOT_FOUND(false, 2023, "댓글을 찾을 수 없습니다."),
	UNAUTHORIZED_COMMENT_DELETE(false, 2024, "댓글을 삭제할 권한이 없습니다."),
	USER_NOT_FOUND(false, 2025, "사용자를 찾을 수 없습니다.");


	/*
	REQUEST_ERROR(false, 2000, "입력값을 확인해주세요."),
	INVALID_USER_JWT(false, 2003, "권한이 없는 유저의 접근입니다."),
	USERS_EMPTY_USER_ID(false, 2010, "유저 아이디 값을 확인해주세요."),
	POST_USERS_EMPTY_EMAIL(false, 2015, "이메일을 입력해주세요."),
	POST_USERS_INVALID_EMAIL(false, 2016, "이메일 형식을 확인해주세요."),
	POST_USERS_EXISTS_EMAIL(false, 2017, "중복된 이메일입니다."),
	RESPONSE_ERROR(false, 3000, "값을 불러오는데 실패하였습니다."),
	DUPLICATED_EMAIL(false, 3013, "중복된 이메일입니다."),
	FAILED_TO_LOGIN(false, 3014, "없는 아이디거나 비밀번호가 틀렸습니다."),
	DATABASE_ERROR(false, 4000, "데이터베이스 연결에 실패하였습니다."),
	SERVER_ERROR(false, 4001, "서버와의 연결에 실패하였습니다."),
	MODIFY_FAIL_USERNAME(false, 4014, "유저네임 수정 실패"),
	PASSWORD_ENCRYPTION_ERROR(false, 4011, "비밀번호 암호화에 실패하였습니다."),
	PASSWORD_DECRYPTION_ERROR(false, 4012, "비밀번호 복호화에 실패하였습니다.");
	*/
	private final boolean isSuccess;
	private final int code;
	private final String message;

	private BaseResponseStatus(boolean isSuccess, int code, String message) {
		this.isSuccess = isSuccess;
		this.code = code;
		this.message = message;
	}
}
