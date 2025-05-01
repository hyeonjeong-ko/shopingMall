package goorm.server.timedeal.controller;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResIndexPageTimeDealDto;
import goorm.server.timedeal.dto.ReqUpdateTimeDeal;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.UserRole;
import goorm.server.timedeal.service.TimeDealService;
import goorm.server.timedeal.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/time-deals")
public class TimeDealController {

	private final TimeDealService timeDealService;
	private final UserService userService;

	private boolean isAdminUser(Long userId){
		System.out.println("userId = " + userId);
		System.out.println("userService.isUserRoleByUserId:" + userService.isUserRoleByUserId(userId, UserRole.TIME_DEAL_MANAGER));

		return userService.isUserRoleByUserId(userId, UserRole.TIME_DEAL_MANAGER);
	}
	/**
	 * 새로운 타임딜을 생성하는 API.
	 *
	 * @param timeDealRequest 생성할 타임딜의 정보를 담고 있는 `ReqTimeDeal`.
	 * @return 생성된 타임딜을 포함한 응답을 반환.
	 */

	@PostMapping
	public ResponseEntity<BaseResponse<TimeDeal>> createTimeDeal(@RequestBody ReqTimeDeal timeDealRequest) {
		System.out.println("나 실행돼?***********************");
		System.out.println("timeDealRequest = " + timeDealRequest);

		BaseResponse<TimeDeal> response;
    		Logger logger = LoggerFactory.getLogger(getClass());
		System.out.println("timeDealRequest = " + timeDealRequest.startTime());

		try {
			// 관리자 여부 확인
			if (isAdminUser(timeDealRequest.userId())) {

				// 타임딜 생성
				TimeDeal timeDeal = timeDealService.createTimeDeal(timeDealRequest);

				// 성공 응답 반환
				response = new BaseResponse<>(BaseResponseStatus.SUCCESS, timeDeal);
				return new ResponseEntity<>(response, HttpStatus.CREATED);  // 성공적으로 생성
			} else {
				// 권한이 없는 경우
				response = new BaseResponse<>(BaseResponseStatus.FORBIDDEN);
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);  // 403 Forbidden
			}
		} catch (Exception e) { // 예외 발생 시 실패 응답
			logger.error("TimeDeal creation failed: {}", e.getMessage(), e);
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 타임딜의 정보를 수정하는 API.
	 *
	 * @param dealId 수정할 타임딜의 고유 ID.
	 * @param timeDealUpdateRequest 수정할 타임딜의 정보를 담고 있는 `UpdateReqTimeDeal` 객체.
	 * @return 수정된 타임딜을 포함한 응답을 반환.
	 */
	@PatchMapping("/{dealId}")
	public ResponseEntity<BaseResponse<TimeDeal>> updateTimeDeal(
		@PathVariable Long dealId,
		@RequestBody ReqUpdateTimeDeal timeDealUpdateRequest) {

		BaseResponse<TimeDeal> response;

		try {
			// 관리자 여부 확인
			if (isAdminUser(timeDealUpdateRequest.userId())) {

				// 타임딜 상태 수정
				TimeDeal updatedTimeDeal = timeDealService.updateTimeDeal(dealId, timeDealUpdateRequest);

				// 성공 응답 반환
				response = new BaseResponse<>(BaseResponseStatus.SUCCESS, updatedTimeDeal);
				return new ResponseEntity<>(response, HttpStatus.OK);  // 수정 성공

			} else {
				// 권한이 없는 경우
				response = new BaseResponse<>(BaseResponseStatus.FORBIDDEN);
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);  // 403 Forbidden
			}
		} catch (Exception e) { // 예외 발생 시 실패 응답
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 상품 상세 정보를 조회하는 Json API.
	 *
	 * @param productId 조회할 상품의 ID.
	 * @return 상품 상세 정보를 포함한 응답.
	 */
	@GetMapping("/{productId}/details")
	public ResponseEntity<BaseResponse<ResDetailPageTimeDealDto>> getTimeDealDetails(@PathVariable Long productId) {
		BaseResponse<ResDetailPageTimeDealDto> response;

		try {
			// 상품 상세 정보 조회
			ResDetailPageTimeDealDto timeDealDetails = timeDealService.getTimeDealDetails(productId);

			// 성공 응답
			response = new BaseResponse<>(BaseResponseStatus.SUCCESS, timeDealDetails);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) { // 예외 발생 시 실패 응답
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 타임딜 구매 API
	 * @param dealId 구매할 타임딜 ID
	 * @param quantity 구매 수량
	 * @return 구매 성공 여부
	 */
	@PostMapping("/{dealId}/purchases")
	public ResponseEntity<BaseResponse<String>> purchaseTimeDeal(
		@PathVariable Long dealId,
		@RequestParam int quantity) {

		BaseResponse<String> response;

		try {
			String resultMessage = timeDealService.purchaseTimeDeal(dealId, quantity);
			response = new BaseResponse<>(BaseResponseStatus.SUCCESS, resultMessage);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 재고 부족 등의 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.STOCK_UNAVAILABLE, e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			// 기타 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.ERROR, "구매 실패: " + e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	@GetMapping("/deals/{timeDealId}")
	public ResponseEntity<ResIndexPageTimeDealDto> getDealById(@PathVariable Long timeDealId) {
		Optional<TimeDeal> timeDealOpt = timeDealService.findById(timeDealId);

		if (timeDealOpt.isPresent()) {
			TimeDeal deal = timeDealOpt.get();
			ResIndexPageTimeDealDto dto = new ResIndexPageTimeDealDto(
				deal.getProduct().getProductId(),
				deal.getProduct().getProductImages().get(0).getImageUrl(),
				deal.getProduct().getTitle(),
				deal.getProduct().getPrice(),
				deal.getDiscountPrice(),
				deal.getDiscountPercentage() != null ? String.valueOf(Math.round(deal.getDiscountPercentage())) : "",
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStatus().name(),
				deal.getStockQuantity()
			);

			return ResponseEntity.ok(dto);
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
	}

	/**
	 * 특정 타임딜 ID의 수량을 변경하는 API
	 *
	 * @param dealId 수정할 타임딜의 고유 ID
	 * @param stockQuantity 변경할 수량
	 * @param userId 요청을 보낸 사용자의 ID (관리자 1로 임시 고정.)
	 * @return 변경된 타임딜 정보를 포함한 응답
	 */
	@PatchMapping("/{dealId}/stocks")
	public ResponseEntity<BaseResponse<TimeDeal>> updateTimeDealStock(
		@PathVariable Long dealId, @RequestParam int stockQuantity) {
		Long userId = 1L; //(관리자 1로 임시 고정.)

		BaseResponse<TimeDeal> response;

		try {
			// 관리자 여부 확인
			if (isAdminUser(userId)) {
				// 타임딜 수량 변경
				TimeDeal updatedTimeDeal = timeDealService.updateTimeDealStock(dealId, stockQuantity);

				// 성공 응답 반환
				response = new BaseResponse<>(BaseResponseStatus.SUCCESS, updatedTimeDeal);
				return new ResponseEntity<>(response, HttpStatus.OK);
			} else {
				// 권한이 없는 경우
				response = new BaseResponse<>(BaseResponseStatus.FORBIDDEN);
				return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);  // 403 Forbidden
			}
		}  catch (IllegalArgumentException e) {
			// 잘못된 입력 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.INVALID_INPUT);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);  // 400 Bad Request
		} catch (EntityNotFoundException e) {
			// 존재하지 않는 타임딜 예외 처리
			response = new BaseResponse<>(BaseResponseStatus.TIME_DEAL_NOT_FOUND);
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);  // 404 Not Found
		} catch (Exception e) {
			// 기타 서버 오류 처리
			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
