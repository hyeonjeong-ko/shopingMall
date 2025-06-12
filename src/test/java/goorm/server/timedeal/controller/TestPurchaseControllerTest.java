package goorm.server.timedeal.controller.test_controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ResPurchaseDto;
import goorm.server.timedeal.service.TimeDealService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TestPurchaseController.class)
class TestPurchaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeDealService timeDealService;

    @Nested
    @DisplayName("DB Lock을 사용한 타임딜 구매 테스트")
    class PurchaseWithDBLock {
        
        @Test
        @DisplayName("정상적인 구매 요청시 성공 응답을 반환해야 한다")
        void successfulPurchase_ShouldReturnOkResponse() throws Exception {
            // given
            given(timeDealService.purchaseTimeDealwithDBLock(anyLong(), anyInt(), anyLong()))
                .willReturn("구매 성공");

            // when & then
            mockMvc.perform(post("/api/test/1/purchases")
                    .param("userId", "1")
                    .param("quantity", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(BaseResponseStatus.SUCCESS.getCode()));
        }

        @Test
        @DisplayName("재고 부족시 BAD_REQUEST와 함께 실패 응답을 반환해야 한다")
        void outOfStock_ShouldReturnBadRequest() throws Exception {
            // given
            doThrow(new IllegalStateException("재고 부족"))
                .when(timeDealService)
                .purchaseTimeDealwithDBLock(anyLong(), anyInt(), anyLong());

            // when & then
            mockMvc.perform(post("/api/test/1/purchases")
                    .param("userId", "1")
                    .param("quantity", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(BaseResponseStatus.STOCK_UNAVAILABLE.getCode()));
        }
    }

    @Nested
    @DisplayName("Redis를 사용한 타임딜 구매 테스트")
    class PurchaseWithRedis {

        @Test
        @DisplayName("Redis를 통한 정상 구매시 성공 응답을 반환해야 한다")
        void successfulPurchase_ShouldReturnOkResponse() throws Exception {
            // given
            ResPurchaseDto mockResponse = new ResPurchaseDto(/* 필요한 데이터 설정 */);
            given(timeDealService.testPurchaseTimeDealByRedis(anyLong(), anyLong(), anyInt()))
                .willReturn(mockResponse);

            // when & then
            mockMvc.perform(post("/api/test/1/purchases/redis")
                    .param("userId", "1")
                    .param("quantity", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.code").value(BaseResponseStatus.SUCCESS.getCode()));
        }

        @Test
        @DisplayName("Redis에서 재고 부족시 BAD_REQUEST와 함께 실패 응답을 반환해야 한다")
        void outOfStock_ShouldReturnBadRequest() throws Exception {
            // given
            doThrow(new IllegalStateException("Redis 재고 부족"))
                .when(timeDealService)
                .testPurchaseTimeDealByRedis(anyLong(), anyLong(), anyInt());

            // when & then
            mockMvc.perform(post("/api/test/1/purchases/redis")
                    .param("userId", "1")
                    .param("quantity", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false))
                .andExpect(jsonPath("$.code").value(BaseResponseStatus.STOCK_UNAVAILABLE.getCode()));
        }
    }
}