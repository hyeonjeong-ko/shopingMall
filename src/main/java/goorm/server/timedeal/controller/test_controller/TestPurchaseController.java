package goorm.server.timedeal.controller.test_controller;

import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ResPurchaseDto;
import goorm.server.timedeal.service.TimeDealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestPurchaseController {

	private final TimeDealService timeDealService;

	@Autowired
	public TestPurchaseController(TimeDealService timeDealService) {
		this.timeDealService = timeDealService;
	}

	/**
	 * 테스트용 타임딜 구매 API
	 * @param dealId 구매할 타임딜 ID
	 * @param userId 구매할 유저 ID
	 * @param quantity 구매  수량
	 * @return 구매 성공 여부
	 */
	@PostMapping("/{dealId}/purchases")
	public ResponseEntity<BaseResponse<ResPurchaseDto>> testPurchaseTimeDeal(
		@PathVariable Long dealId,
		@RequestParam Long userId,
		@RequestParam int quantity) {

		BaseResponse<ResPurchaseDto> response;

		try {
			String purchaseResponse = timeDealService.purchaseTimeDealwithDBLock(dealId, quantity, userId);
			response = new BaseResponse<ResPurchaseDto>(BaseResponseStatus.SUCCESS);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 재고 부족 예외
			log.error("Stock unavailable for dealId: {}, userId: {}, quantity: {}. Error: {}", dealId, userId, quantity, e.getMessage(), e);
			response = new BaseResponse<>(BaseResponseStatus.STOCK_UNAVAILABLE);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			// 기타 예외
			log.error("Unexpected error occurred during purchase for dealId: {}, userId: {}, quantity: {}. Error: {}", dealId, userId, quantity, e.getMessage(), e);

			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * 테스트용 타임딜 구매 API
	 * @param dealId 구매할 타임딜 ID
	 * @param userId 구매할 유저 ID
	 * @param quantity 구매  수량
	 * @return 구매 성공 여부
	 */
	@PostMapping("/{dealId}/purchases/redis")
	public ResponseEntity<BaseResponse<ResPurchaseDto>> testPurchaseTimeDealByRedis(
		@PathVariable Long dealId,
		@RequestParam Long userId,
		@RequestParam int quantity) {

		BaseResponse<ResPurchaseDto> response;

		try {
			ResPurchaseDto purchaseResponse = timeDealService.testPurchaseTimeDealByRedis(dealId, userId, quantity);
			response = new BaseResponse<>(BaseResponseStatus.SUCCESS, purchaseResponse);
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (IllegalStateException e) {
			// 재고 부족 예외
			log.error("Stock unavailable for dealId: {}, userId: {}, quantity: {}. Error: {}", dealId, userId, quantity, e.getMessage(), e);
			response = new BaseResponse<>(BaseResponseStatus.STOCK_UNAVAILABLE);
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			// 기타 예외
			log.error("Unexpected error occurred during purchase for dealId: {}, userId: {}, quantity: {}. Error: {}", dealId, userId, quantity, e.getMessage(), e);

			response = new BaseResponse<>(BaseResponseStatus.ERROR);
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

