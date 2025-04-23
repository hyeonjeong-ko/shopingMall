package goorm.server.timedeal.service.aws;
//
//import java.time.LocalDateTime;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import lombok.extern.slf4j.Slf4j;
//import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
//import software.amazon.awssdk.services.eventbridge.model.*;
//import software.amazon.awssdk.services.lambda.LambdaClient;
//import software.amazon.awssdk.services.lambda.model.AddPermissionRequest;
//import software.amazon.awssdk.services.lambda.model.AddPermissionResponse;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class EventBridgeRuleService {
//
//	private final EventBridgeClient eventBridgeClient = EventBridgeClient.create();
//	private final LambdaClient lambdaClient = LambdaClient.create();
//
//
//	/**
//	 * EventBridge Rule 생성 및 Lambda 트리거 연결
//	 *
//	 * @param ruleName Rule 이름 (고유)
//	 * @param scheduleExpression CRON 표현식
//	 * @param inputPayload Lambda로 전달할 데이터 (JSON 문자열)
//	 * @param lambdaArn 연결할 Lambda의 ARN
//	 */
//	@Transactional
//	public void createEventBridgeRule(String ruleName, String scheduleExpression, String inputPayload, String lambdaArn) {
//		// Step 1: EventBridge Rule 생성
//		PutRuleRequest ruleRequest = PutRuleRequest.builder()
//			.name(ruleName)
//			.scheduleExpression(scheduleExpression)
//			.state(RuleState.ENABLED)
//			.build();
//
//		String ruleArn = eventBridgeClient.putRule(ruleRequest).ruleArn();
//
//		// Step 2: Lambda Permission 추가
//		try {
//			AddPermissionRequest permissionRequest = AddPermissionRequest.builder()
//				.functionName(lambdaArn) // Lambda ARN
//				.statementId("AllowEventBridgeInvoke-" + ruleName) // 고유 Statement ID
//				.action("lambda:InvokeFunction") // Invoke 권한
//				.principal("events.amazonaws.com") // EventBridge 서비스
//				.sourceArn(ruleArn) // Rule ARN
//				.build();
//
//			AddPermissionResponse permissionResponse = lambdaClient.addPermission(permissionRequest);
//			log.info("Permission added: " + permissionResponse.statement());
//		} catch (Exception e) {
//			log.error("Permission already exists or failed: " + e.getMessage());
//		}
//
//		// Step 3: Lambda를 EventBridge Target으로 연결
//		Target target = Target.builder()
//			.id("1")
//			.arn(lambdaArn) // Lambda ARN
//			.input(inputPayload) // Lambda에 전달할 JSON 데이터
//			.build();
//
//		PutTargetsRequest targetsRequest = PutTargetsRequest.builder()
//			.rule(ruleName) // EventBridge Rule 이름
//			.targets(target) // 연결할 Target
//			.build();
//
//		PutTargetsResponse targetsResponse = eventBridgeClient.putTargets(targetsRequest);
//
//		if (targetsResponse.failedEntryCount() > 0) {
//			log.error("Failed to add targets: " + targetsResponse.failedEntries());
//		} else {
//			log.error("Target successfully added to the rule.");
//		}
//	}
//
//	/**
//	 * CRON 표현식 생성
//	 *
//	 * @param dateTime 실행 시간
//	 * @return CRON 표현식
//	 */
//	public String convertToCronExpression(LocalDateTime dateTime) {
//		return String.format("cron(%d %d %d %d ? %d)",
//			dateTime.getMinute(),
//			dateTime.getHour(),
//			dateTime.getDayOfMonth(),
//			dateTime.getMonthValue(),
//			dateTime.getYear());
//	}
//}
