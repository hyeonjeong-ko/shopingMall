package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.CurrentUser;
import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ReqUpdateTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResIndexPageTimeDealDto;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.enums.UserRole;
import goorm.server.timedeal.service.TimeDealService;
import goorm.server.timedeal.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/time-deals")
public class TimeDealController {

	private final TimeDealService timeDealService;
	private final UserService userService;

	private boolean isAdminUser(Long userId){
		boolean isAdmin = userService.isUserRoleByUserId(userId, UserRole.TIME_DEAL_MANAGER);
		log.info("권한 확인 - userId: {}, isTimeDealManager: {}", userId, isAdmin);
		return isAdmin;

	}
	/**
	 * 새로운 타임딜을 생성하는 API.
	 *
	 * @param timeDealRequest 생성할 타임딜의 정보를 담고 있는 `ReqTimeDeal`.
	 * @return 생성된 타임딜을 포함한 응답을 반환.
	 */

	@PostMapping
	public ResponseEntity<BaseResponse<TimeDeal>> createTimeDeal(@RequestBody ReqTimeDeal timeDealRequest) {
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.info("Creating time deal with request: {}", timeDealRequest);

		// 관리자 권한 체크 - 권한이 없으면 예외 발생
		if (!isAdminUser(timeDealRequest.userId())) {
			throw new BaseException(BaseResponseStatus.FORBIDDEN);
		}

		TimeDeal timeDeal = timeDealService.createTimeDeal(timeDealRequest);

		// 성공시 응답
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new BaseResponse<>(timeDeal));
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

		// 관리자 여부 확인
		if (!isAdminUser(timeDealUpdateRequest.userId())) {
			throw new BaseException(BaseResponseStatus.FORBIDDEN);
		}

		// 타임딜 상태 수정
		TimeDeal updatedTimeDeal = timeDealService.updateTimeDeal(dealId, timeDealUpdateRequest);

		// 성공 응답 반환
		return ResponseEntity.ok(new BaseResponse<>(updatedTimeDeal));
	}


	/**
	 * 상품 상세 정보를 조회하는 Json API.
	 *
	 * @param productId 조회할 상품의 ID.
	 * @return 상품 상세 정보를 포함한 응답.
	 */
	@GetMapping("/{productId}/details")
	public ResponseEntity<BaseResponse<ResDetailPageTimeDealDto>> getTimeDealDetails(@PathVariable Long productId) {
		log.info("타임딜 상세 정보 조회 요청 - productId: {}", productId);
		ResDetailPageTimeDealDto timeDealDetails = timeDealService.getTimeDealDetails(productId);
		return ResponseEntity.ok(new BaseResponse<>(timeDealDetails));
	}


	/**
	 * 타임딜 구매 API
	 * @param dealId 구매할 타임딜 ID
	 * @param quantity 구매 수량
	 * @param userId 현재 로그인한 사용자 ID
	 * @return 구매 성공 여부 메시지
	 */
	@PostMapping("/{dealId}/purchases")
	public ResponseEntity<BaseResponse<String>> purchaseTimeDeal(
			@PathVariable Long dealId,
			@RequestParam int quantity,
			@CurrentUser String userId) {

		String resultMessage = timeDealService.purchaseTimeDealwithDBLock(dealId, quantity, Long.parseLong(userId));
		return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, resultMessage));
	}


	@GetMapping("/deals/{timeDealId}")
	public ResponseEntity<ResIndexPageTimeDealDto> getDealById(@PathVariable Long timeDealId) {
		TimeDeal deal = timeDealService.findTimeDealById(timeDealId);
		return ResponseEntity.ok(convertToDto(deal));
	}


	private ResIndexPageTimeDealDto convertToDto(TimeDeal deal) {
		return new ResIndexPageTimeDealDto(
				deal.getProduct().getProductId(),
				getFirstImageUrl(deal.getProduct()),
				deal.getProduct().getTitle(),
				deal.getProduct().getPrice(),
				deal.getDiscountPrice(),
				formatDiscountPercentage(deal.getDiscountPercentage()),
				deal.getStartTime(),
				deal.getEndTime(),
				deal.getStatus().name(),
				deal.getStockQuantity()
		);
	}

	private String getFirstImageUrl(Product product) {
		return product.getProductImages().get(0).getImageUrl();
	}

	private String formatDiscountPercentage(Double percentage) {
		return percentage != null ? String.valueOf(Math.round(percentage)) : "";
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