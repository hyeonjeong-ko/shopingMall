//package goorm.server.timedeal.config.aws.sqs;
//
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class TimeDealSqsListener {
//
// 	private final NotificationService notificationService;
// 	private final ObjectMapper objectMapper;
//
//
// 	@SqsListener(value = "${cloud.aws.sqs.queue-name}")
// 	public void handleTimeDealUpdate(String message) throws JsonProcessingException {
// 		try {
// 			// 이중 래핑 제거
// 			String decodedMessage = message.startsWith("\"")
// 				? message.substring(1, message.length() - 1).replace("\\\"", "\"")
// 				: message;
//
// 			log.info("decodedMessage = " + decodedMessage);
//
//
// 			SQSTimeDealDTO timeDealDTO = objectMapper.readValue(decodedMessage, SQSTimeDealDTO.class);
// 			log.info("타임딜 이벤트 정보 변경이 일어났습니다: " + timeDealDTO.getTimeDealId());
// 			log.info("타일딜 이벤트 정보 변경이 일어난 메시지 타입: " + timeDealDTO.getMessageType());
//
// 			// 메시지를 NotificationService 로 전달하여 Thymeleaf 로 알림 처리
// 			notificationService.sendUpdateToThymeleaf(timeDealDTO);
// 		} catch (Exception e) {
// 			System.err.println("메시지 처리 중 오류 발생: " + e.getMessage());
// 			e.printStackTrace();
// 		}
// 	}
// }
//
