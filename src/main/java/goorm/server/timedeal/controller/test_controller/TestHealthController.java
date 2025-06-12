package goorm.server.timedeal.controller.test_controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller for TimeDeal Project.
 * Provides health check & dummy endpoints for testing.
 */
@RestController
@RequestMapping("/api/test")
public class TestHealthController {

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello, TimeDeal!";
	}


	@GetMapping("/status")
	public String statusCheck() {
		return "TimeDeal Project is up and running 1 / 15일!";
	}
}
