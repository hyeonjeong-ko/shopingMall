package goorm.server.timedeal.controller.test_controller;

import goorm.server.timedeal.model.Test;
import goorm.server.timedeal.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final TestRepository testRepository;

    @GetMapping("/test")
    public List<Test> getAll() {
        return testRepository.findAll();
    }

    @PostMapping("/test")
    public void create(@RequestParam String name) {
        testRepository.save(
                Test.builder()
                        .name(name)
                        .createdAt(java.time.LocalDateTime.now())
                        .build()
        );
    }
}
