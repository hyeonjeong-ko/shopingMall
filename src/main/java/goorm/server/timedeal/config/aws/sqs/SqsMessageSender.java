package goorm.server.timedeal.config.aws.sqs;

//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.MediaType;
//import org.springframework.messaging.Message;
//import org.springframework.messaging.MessageHeaders;
//import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import goorm.server.timedeal.logging.AppLogger;
//import goorm.server.timedeal.dto.SQSTimeDealDTO;
//import io.awspring.cloud.sqs.operations.SendResult;
//import io.awspring.cloud.sqs.operations.SqsTemplate;
//import lombok.extern.slf4j.Slf4j;
//import software.amazon.awssdk.services.sqs.SqsAsyncClient;
//
//import java.time.Duration;
//import java.time.Instant;

//@Slf4j
//@Component
//public class SqsMessageSender {
//
//	private final SqsTemplate queueMessagingTemplate;
//	private final ObjectMapper objectMapper;
//
//
//	@Value("${cloud.aws.sqs.queue-name}")
//	private String queueName;
//
//	public SqsMessageSender(SqsAsyncClient sqsAsyncClient, ObjectMapper objectMapper) {
//		this.queueMessagingTemplate = SqsTemplate.newTemplate(sqsAsyncClient);
//		this.objectMapper = objectMapper;
//	}
//
//	public SendResult<String> sendMessage(String messageBody) {
//		Instant enqueueTime = Instant.now(); // 메시지 전송 시작 시간 기록
//		AppLogger.logBusinessEvent("SQS Message Sending Started",
//				"QueueName", queueName,
//				"MessageBody", messageBody,
//				"EnqueueTime", enqueueTime.toString());
//
//		try{
//			// SQS 전송할 메시지
//			Message<String> message = MessageBuilder.withPayload(messageBody).build();
//
//			SendResult<String> result = queueMessagingTemplate.send(queueName, message);
//			Instant dequeueTime = Instant.now(); // 메시지 전송 완료 시간 기록
//
//			// 성공 로그 기록
//			AppLogger.logBusinessEvent("SQS Message Sent Successfully",
//					"QueueName", queueName,
//					"MessageID", result.messageId(),
//					"Endpoint", result.endpoint(),
//					"Status", "SUCCESS",
//					"EnqueueTime", enqueueTime.toString(),
//					"DequeueTime", dequeueTime.toString(),
//					"DurationMs", Duration.between(enqueueTime, dequeueTime).toMillis());
//			return result;
//
//			// 메시지를 큐에 전송하고, 전송 결과를 반환
//			//return queueMessagingTemplate.send(queueName, message);
//	}catch(Exception e){
//			Instant dequeueTime = Instant.now(); // 실패 시에도 완료 시간 기록
//
//			// 실패 로그 기록
//			AppLogger.logError("SQS Message Sending Failed",
//					e,
//					"QueueName", queueName,
//					"MessageBody", messageBody,
//					"Status", "FAILURE",
//					"EnqueueTime", enqueueTime.toString(),
//					"DequeueTime", dequeueTime.toString(),
//					"DurationMs", Duration.between(enqueueTime, dequeueTime).toMillis(),
//					"ErrorMessage", e.getMessage());
//			throw e;
//		}
//	}
//
//	public void sendJsonMessage(SQSTimeDealDTO payload) {
//		Instant enqueueTime = Instant.now(); // 메시지 전송 시작 시간 기록
//		AppLogger.logBusinessEvent("SQS JSON Message Sending Started",
//				"QueueName", queueName,
//				"Payload", payload,
//				"EnqueueTime", enqueueTime.toString());
//		try {
//			// JSON 형식으로 변환
//			String jsonPayload = objectMapper.writeValueAsString(payload);
//
//			// 메시지 생성
//			Message<String> message = MessageBuilder
//				.withPayload(jsonPayload)
//				.setHeader(MessageHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//				.build();
//
//			// 메시지 전송
//			queueMessagingTemplate.send(queueName, message);
//			//log.info("메시지가 성공적으로 전송되었습니다: " + jsonPayload);
//
//			Instant dequeueTime = Instant.now(); // 메시지 전송 완료 시간 기록
//
//			// 성공 로그 기록
//			AppLogger.logBusinessEvent("SQS JSON Message Sent Successfully",
//					"QueueName", queueName,
//					"Payload", jsonPayload,
//					"Status", "SUCCESS",
//					"EnqueueTime", enqueueTime.toString(),
//					"DequeueTime", dequeueTime.toString(),
//					"DurationMs", Duration.between(enqueueTime, dequeueTime).toMillis());
//		} catch (Exception e) {
//			Instant dequeueTime = Instant.now(); // 실패 시에도 완료 시간 기록
//			// 에러 로그 기록
//			AppLogger.logError("SQS JSON Message Sending Failed",
//					e,
//					"QueueName", queueName,
//					"Payload", payload,
//					"Status", "FAILURE",
//					"EnqueueTime", enqueueTime.toString(),
//					"DequeueTime", dequeueTime.toString(),
//					"DurationMs", Duration.between(enqueueTime, dequeueTime).toMillis(),
//					"ErrorMessage", e.getMessage());
//			System.err.println("메시지 전송 중 오류 발생: " + e.getMessage());
//			e.printStackTrace();
//		}
//	}
//}
