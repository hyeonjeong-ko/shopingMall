package goorm.server.timedeal.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TestHealthControllerTest.class)
public class TestHealthControllerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("헬스 체크 엔드포인트 '/hello'는 정상적인 인사 메시지를 반환해야 한다")
    void sayHello_ShouldReturnGreetingMessage() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/test/hello"));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, TimeDeal!"));
    }

    @Test
    @DisplayName("상태 체크 엔드포인트 '/status'는 프로젝트 상태 메시지를 반환해야 한다")
    void statusCheck_ShouldReturnStatusMessage() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/test/status"));

        // then
        result
                .andExpect(status().isOk())
                .andExpect(content().string("TimeDeal Project is up and running 1 / 15일!"));
    }



}
