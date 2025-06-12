package goorm.server.timedeal.service;

import static goorm.server.timedeal.config.exception.BaseResponseStatus.CACHE_UPDATE_FAILED;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.INSUFFICIENT_STOCK;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.INVALID_STOCK_QUANTITY;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.STOCK_DECREASE_FAILED;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.STOCK_NOT_FOUND;
import static goorm.server.timedeal.config.exception.BaseResponseStatus.TIME_DEAL_NOT_FOUND;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.config.exception.domain.StockUnavailableException;
import goorm.server.timedeal.config.exception.domain.TimeDealException;
import goorm.server.timedeal.config.redis.TimeDealCache;
import goorm.server.timedeal.dto.ReqTimeDeal;
import goorm.server.timedeal.dto.ReqUpdateTimeDeal;
import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.dto.ResPurchaseDto;
import goorm.server.timedeal.dto.ResTimeDealListDto;
import goorm.server.timedeal.model.Product;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.TimeDealStatus;
import goorm.server.timedeal.repository.TimeDealRepository;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeDealService {

    private final UserService userService;
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final PurchaseService purchaseService;

    private final TimeDealRepository timeDealRepository;


    private final TimeDealCache timeDealCache;
    //private final RedissonClient redissonClient;  // RedissonClient 주입
    //private final StringRedisTemplate redisTemplate;  // Redis 캐시 주입

    //	private final SqsMessageSender sqsMessageSender;
    //private final S3Service s3Service;
    //private final EventBridgeRuleService eventBridgeRuleService;

    //	@Value("${cloud.aws.lambda.timedeal-update-arn}")
    //	private String timeDealUpdateLambdaArn;

    /**
     * 타임딜을 생성하는 메서드.
     *
     * @param timeDealRequest 생성할 타임딜의 세부 정보를 담고 있는 `ReqTimeDeal` 객체.
     * @return 생성된 타임딜 객체를 반환.
     * @throws IOException 타임딜 생성 중 외부 리소스와의 연동 시 IO 예외 발생 시 던져짐.
     */
    @Transactional
    public TimeDeal createTimeDeal(ReqTimeDeal timeDealRequest) {
        log.info("Creating time deal with request: {}", timeDealRequest);

        // 1. 유저 확인
        User user = userService.findById(timeDealRequest.userId());

        // 2. 상품 등록
        Product product = productService.createProduct(timeDealRequest);

        // 3. 이미지 업로드 (S3에 저장하고 URL 반환)
        //String imageUrl = s3Service.uploadImageFromUrlWithCloudFront(timeDealRequest.imageUrl());
        String imageUrl = timeDealRequest.imageUrl();

        // 4. 상품 이미지 저장
        productImageService.saveProductImage(product, imageUrl, "thumbnail");

        // 5. 타임딜 예약 생성
        TimeDeal timeDeal = saveTimeDeal(timeDealRequest, product, user);

        // 6. Redis에 재고 캐싱 추가
        timeDealCache.setStock(timeDeal.getTimeDealId(), timeDeal.getStockQuantity());

        // 7. EventBridge Rule 생성
        //createEventBridgeRulesForTimeDeal(timeDeal);

        return timeDeal;
    }


    private TimeDeal saveTimeDeal(ReqTimeDeal timeDealRequest, Product product, User user) {
        TimeDeal timeDeal = new TimeDeal();
        timeDeal.setProduct(product);
        timeDeal.setStartTime(timeDealRequest.startTime());
        timeDeal.setEndTime(timeDealRequest.endTime());
        timeDeal.setDiscountPrice(timeDealRequest.discountPrice());
        timeDeal.setDiscountPercentage(timeDealRequest.discountPercentage());
        timeDeal.setUser(user);
        timeDeal.setStatus(TimeDealStatus.ACTIVE); // 초기 상태는 예약됨
        timeDeal.setStockQuantity(timeDealRequest.stockQuantity());
        timeDeal = timeDealRepository.save(timeDeal);
        return timeDeal;
    }

    public List<TimeDeal> getActiveAndScheduledDeals() {
        return timeDealRepository.findActiveAndScheduledDeals();
    }


    @Transactional
    public TimeDeal updateTimeDeal(Long dealId, ReqUpdateTimeDeal request) {
        TimeDeal timeDeal = findTimeDealById(dealId);
        validateUpdateRequest(request);
        updateTimeDealDetails(timeDeal, request);
        return timeDeal;
    }


    private void validateUpdateRequest(ReqUpdateTimeDeal request) {
        if (request.discountRate() != null) {
            validateDiscountRate(Integer.valueOf(request.discountRate()));
        }
        if (request.startTime() != null && request.endTime() != null) {
            validateTimeRange(request.startTime(), request.endTime());
        }
        if (request.stockQuantity() != null) {
            validateStockQuantity(request.stockQuantity());
        }
    }

    private void validateDiscountRate(Integer rate) {
        if (rate < 0 || rate > 100) {
            throw new BaseException(BaseResponseStatus.INVALID_DISCOUNT_RATE);
        }
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new BaseException(BaseResponseStatus.INVALID_TIME_RANGE);
        }
    }


    private void updateTimeDealDetails(TimeDeal timeDeal, ReqUpdateTimeDeal request) {
        Optional.ofNullable(request.discountRate())
                .ifPresent(rate -> timeDeal.setDiscountPercentage(Double.valueOf(rate)));

        Optional.ofNullable(request.startTime())
                .ifPresent(timeDeal::setStartTime);

        Optional.ofNullable(request.endTime())
                .ifPresent(timeDeal::setEndTime);

        Optional.ofNullable(request.status())
                .ifPresent(status -> timeDeal.setStatus(TimeDealStatus.valueOf(status)));

        Optional.ofNullable(request.stockQuantity())
                .ifPresent(timeDeal::setStockQuantity);
    }


	/*
		private void createEventBridgeRulesForTimeDeal(TimeDeal timeDeal) {
			// KST to UTC conversion
			ZonedDateTime startKST = timeDeal.getStartTime().atZone(ZoneId.of("Asia/Seoul"));
			ZonedDateTime endKST = timeDeal.getEndTime().atZone(ZoneId.of("Asia/Seoul"));

			// System.out.println("startKST"+startKST);
			// System.out.println("endKST"+startKST);


			// 1. UTC 로 변환
			ZonedDateTime startUTC = startKST.withZoneSameInstant(ZoneId.of("UTC"));
			ZonedDateTime endUTC = endKST.withZoneSameInstant(ZoneId.of("UTC"));

			// 2. cron expression 포맷팅
			String startCron = eventBridgeRuleService.convertToCronExpression(startUTC.toLocalDateTime());
			String endCron = eventBridgeRuleService.convertToCronExpression(endUTC.toLocalDateTime());

			String startRuleName = "TimeDealStart-" + timeDeal.getTimeDealId();
			String startPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
				timeDeal.getTimeDealId(), TimeDealStatus.ACTIVE.name());

			String endRuleName = "TimeDealEnd-" + timeDeal.getTimeDealId();
			String endPayload = String.format("{\"time_deal_id\": %d, \"new_status\": \"%s\"}",
				timeDeal.getTimeDealId(), TimeDealStatus.ENDED.name());

			// 3. Create EventBridge Rules using UTC times
			eventBridgeRuleService.createEventBridgeRule(
				startRuleName,
				startCron,
				startPayload,
				timeDealUpdateLambdaArn
			);

			eventBridgeRuleService.createEventBridgeRule(
				endRuleName,
				endCron,
				endPayload,
				timeDealUpdateLambdaArn
			);
		}
		*/

    public ResDetailPageTimeDealDto getTimeDealDetails(Long productId) {
        // 1. 상품에 대한 타임딜 정보 조회
        TimeDeal timeDeal = timeDealRepository.findByProduct_ProductId(productId)
                .orElseThrow(() -> new RuntimeException("해당 상품에 대한 타임딜 정보를 찾을 수 없습니다."));

        // 2. 상품 이미지 조회
        List<String> productImages = productImageService.findImageUrlsByProductId(productId);

        // 3. DTO 생성 및 반환
        return new ResDetailPageTimeDealDto(
                timeDeal.getTimeDealId(),
                timeDeal.getProduct().getProductId(),
                //String.join(",", productImages),
                String.join("", productImages.get(0)), // 단일 이미지로 설정. 나중에 여러 이미지 저장할때는 수정 필요
                removeHtmlTags(timeDeal.getProduct().getTitle()), // HTML 태그 제거
                timeDeal.getProduct().getPrice(),
                timeDeal.getDiscountPrice(),
                String.format("%d%%", timeDeal.getDiscountPercentage().intValue()),
                timeDeal.getStartTime(),
                timeDeal.getEndTime(),
                timeDeal.getStatus().name(),
                timeDeal.getStockQuantity(),
                timeDeal.getProduct().getBrand(),
                timeDeal.getProduct().getMallName()
        );
    }

    // HTML 태그 제거 유틸리티 메서드
    private String removeHtmlTags(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("<[^>]+>", ""); // 정규식으로 HTML 태그 제거
    }


    public List<ResTimeDealListDto> getTimeDealList() {
        List<TimeDeal> timeDeals = timeDealRepository.findAll(); // 예시: 전체 타임딜 조회
        return timeDeals.stream()
                .map(deal -> new ResTimeDealListDto(
                        deal.getTimeDealId(),
                        deal.getProduct().getTitle(),
                        deal.getStartTime(),
                        deal.getEndTime(),
                        deal.getStockQuantity(),
                        deal.getDiscountPercentage(),
                        deal.getDiscountPrice(),
                        mapTimeDealStatus(deal.getStartTime(), deal.getEndTime()) // 상태 변환
                ))
                .collect(Collectors.toList());
    }

    private String mapTimeDealStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(startTime)) {
            return "진행전"; // SCHEDULED
        } else if (now.isAfter(endTime)) {
            return "종료"; // ENDED
        } else {
            return "진행중"; // ACTIVE
        }
    }

    /**
     * 타임딜 구매 메서드VER1: 비관적 락을 사용
     */
    @Transactional
    public String purchaseTimeDealwithDBLock(Long timeDealId, int quantity, Long userId) {
        User user = userService.findById(userId);
        TimeDeal timeDeal = findTimeDealWithLock(timeDealId);

        validateStockAvailability(timeDeal, quantity);
        updateStock(timeDeal, quantity);
        purchaseService.createPurchaseRecord(timeDeal, user, quantity);

        return String.format("구매가 완료되었습니다. 남은 재고: %d개", timeDeal.getStockQuantity());
    }

    private TimeDeal findTimeDealWithLock(Long timeDealId) {
        return timeDealRepository.findByIdWithLock(timeDealId)
                .orElseThrow(() -> new TimeDealException(TIME_DEAL_NOT_FOUND));
    }

    private void validateStockAvailability(TimeDeal timeDeal, int requestedQuantity) {
        if (timeDeal.getStockQuantity() < requestedQuantity) {
            throw new StockUnavailableException(timeDeal.getStockQuantity());
        }
    }

    private void updateStock(TimeDeal timeDeal, int quantity) {
        timeDeal.setStockQuantity(timeDeal.getStockQuantity() - quantity);
    }


    @Transactional
    public void updateTimeDealStatus(Long timeDealId, TimeDealStatus newStatus) {
        int updatedRows = timeDealRepository.updateStatus(timeDealId, newStatus);
        if (updatedRows > 0) {
            System.out.println("TimeDeal ID: " + timeDealId + " updated to status: " + newStatus);
        } else {
            System.out.println("TimeDeal ID: " + timeDealId + " not found or already updated.");
        }
    }


    public int getRemainingStock(Long timeDealId) {
        validateTimeDealId(timeDealId);

        Integer stockQuantity = timeDealCache.getStock(timeDealId);
        if (stockQuantity != null) {
            log.info("캐시에서 재고 조회 성공 - TimeDeal ID: {}, 남은 재고: {}", timeDealId, stockQuantity);
            return stockQuantity;
        }

        // 캐시에 없는 경우 DB에서 조회하고 캐시 갱신
        TimeDeal timeDeal = findTimeDealById(timeDealId);
        int remainingStock = timeDeal.getStockQuantity();

        // 캐시 갱신
        try {
            timeDealCache.setStock(timeDealId, remainingStock);
            log.info("DB에서 재고 조회 후 캐시 갱신 - TimeDeal ID: {}, 남은 재고: {}", timeDealId, remainingStock);
        } catch (Exception e) {
            log.warn("캐시 갱신 실패 - TimeDeal ID: {}", timeDealId, e);
            // 캐시 갱신 실패는 크리티컬하지 않으므로 예외를 던지지 않음
        }

        return remainingStock;
    }

    private void validateTimeDealId(Long timeDealId) {
        if (timeDealId == null) {
            throw new TimeDealException(TIME_DEAL_NOT_FOUND);
        }
    }


    @Transactional
    public TimeDeal updateTimeDealStock(Long dealId, int stockQuantity) {
        // 입력값 검증
        validateStockQuantity(stockQuantity);

        // 타임딜 조회
        TimeDeal timeDeal = findTimeDealById(dealId);

        // DB 재고 수량 업데이트
        timeDeal.setStockQuantity(stockQuantity);
        TimeDeal updatedTimeDeal = timeDealRepository.save(timeDeal);

        // 캐시 재고 수량 업데이트
        updateCacheStock(dealId, stockQuantity);

        return updatedTimeDeal;
    }

    private void validateStockQuantity(int stockQuantity) {
        if (stockQuantity < 0) {
            throw new TimeDealException(INVALID_STOCK_QUANTITY);
        }
    }

    private void updateCacheStock(Long dealId, int stockQuantity) {
        try {
            timeDealCache.setStock(dealId, stockQuantity);
            log.info("Redis 캐시 재고 업데이트 완료. dealId: {}, quantity: {}", dealId, stockQuantity);
        } catch (Exception e) {
            log.error("Redis 캐시 재고 업데이트 실패. dealId: {}, quantity: {}", dealId, stockQuantity, e);
            throw new TimeDealException(CACHE_UPDATE_FAILED);
        }
    }

    /**
    * 타임딜 구매 메서드 VER2: 레디스락 사용
    * */
    //@Transactional
    @Transactional
    public ResPurchaseDto testPurchaseTimeDealByRedis(Long timeDealId, Long userId, int quantity) {
        PerformanceTracker tracker = new PerformanceTracker();
        RLock lock = null;

        try {
            // 1. 유저 확인
            tracker.start("User Lookup");
            //checkUser(userId);
            tracker.end("User Lookup", "timeDealId", timeDealId, "userId", userId);

            // 2. 락 획득
            tracker.start("Lock Acquisition");
            lock = acquireLock(timeDealId);
            tracker.end("Lock Acquisition", "timeDealId", timeDealId);

            // 3. 재고 처리 및 구매
            return processTimeDealPurchase(timeDealId, userId, quantity, tracker);

        } finally {
            // 4. 락 해제
            if (lock != null) {
                tracker.start("Lock Release");
                lock.unlock();
                tracker.end("Lock Release", "timeDealId", timeDealId);
            }
        }
    }

    private RLock acquireLock(Long timeDealId) {
        RLock lock = timeDealCache.getLock(timeDealId);
        lock.lock();
        return lock;
    }

    private ResPurchaseDto processTimeDealPurchase(Long timeDealId, Long userId, int quantity,
                                                   PerformanceTracker tracker) {
        // 1. 재고 조회
        tracker.start("Redis Query");
        Integer currentStock = getStockWithCache(timeDealId);
        tracker.end("Redis Query", "timeDealId", timeDealId, "currentStock", currentStock);

        // 2. 재고 검증
        validateStock(currentStock, quantity);

        // 3. 재고 감소 및 구매 처리
        tracker.start("Stock Update");
        decreaseStock(timeDealId, quantity);
        ResPurchaseDto purchaseResult = createPurchaseResult(userId, quantity);
        tracker.end("Stock Update", "timeDealId", timeDealId, "newStock", currentStock - quantity);

        // 4. 비동기 메시지 전송 (SQS)
		/*
		tracker.start("SQS Message Send");
		sendMessageToSQS(timeDealId, userId, quantity);
		tracker.end("SQS Message Send", "timeDealId", timeDealId, "userId", userId, "quantity", quantity);
		*/
        return purchaseResult;
    }

    private Integer getStockWithCache(Long timeDealId) {
        Integer currentStock = timeDealCache.getStock(timeDealId);
        if (currentStock == null) {
            TimeDeal timeDeal = findTimeDealById(timeDealId);
            currentStock = timeDeal.getStockQuantity();
            timeDealCache.setStock(timeDealId, currentStock);
        }
        return currentStock;
    }

    private void validateStock(Integer currentStock, int requestedQuantity) {
        if (currentStock == null) {
            throw new TimeDealException(STOCK_NOT_FOUND);
        }
        if (currentStock < requestedQuantity) {
            throw new TimeDealException(INSUFFICIENT_STOCK);
        }
    }


    private void decreaseStock(Long timeDealId, int quantity) {
        boolean decreased = timeDealCache.decreaseStockwithLock(timeDealId, quantity);
        if (!decreased) {
            throw new TimeDealException(STOCK_DECREASE_FAILED);
        }
    }


    private ResPurchaseDto createPurchaseResult(Long userId, int quantity) {
        return new ResPurchaseDto(userId, quantity, LocalDateTime.now(), "PURCHASED");
    }

	/*
	@Async
	public void sendMessageToSQS(Long timeDealId, Long userId, int quantity) {
		SQSTimeDealDTO sqsMessage = new SQSTimeDealDTO(timeDealId, userId, quantity, "PURCHASED");
		sqsMessageSender.sendJsonMessage(sqsMessage); // 실제 SQS 메시지 전송 메소드
	}
	 */


    public TimeDeal findTimeDealById(Long timeDealId) {
        return timeDealRepository.findById(timeDealId)
                .orElseThrow(() -> new TimeDealException(TIME_DEAL_NOT_FOUND));
    }


}