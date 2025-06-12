package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	// User 생성 API
	@PostMapping
	public ResponseEntity<User> createUser(@Validated @RequestBody User user) {
		User createdUser = userService.createUser(user);
		return ResponseEntity.ok(createdUser);
	}

	// User 수정 API
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @Validated @RequestBody User updatedUser) {
		User user = userService.updateUser(id, updatedUser);
		return ResponseEntity.ok(user);
	}

	// 테스트용 엔드포인트 추가
	@PostMapping("/test-invalid")
	public ResponseEntity<User> testInvalidInput(@Validated @RequestBody User user) {
		// 이메일이 test@test.com인 경우 강제로 INVALID_INPUT 예외 발생
		if (user.getEmail().equals("test-ex")) {
			throw new BaseException(BaseResponseStatus.INVALID_INPUT);
		}
		return ResponseEntity.ok(user);
	}

	@GetMapping("/params")
	public ResponseEntity<String> testParams(
			@RequestParam Long userId,      // required=true가 기본값
			@RequestParam String userName) {  // required=true가 기본값

		return ResponseEntity.ok(
				String.format("userId: %d, userName: %s", userId, userName));
	}


}
