package goorm.server.timedeal.config;

import goorm.server.timedeal.logging.AppLogger;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;
//
//@Component
//public class HttpLoggingFilter implements Filter {
//
//    @Override
//    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//            throws IOException, ServletException {
//        HttpServletRequest httpRequest = (HttpServletRequest) request;
//        HttpServletResponse httpResponse = (HttpServletResponse) response;
//
//        // TraceID 생성
//        String traceId = UUID.randomUUID().toString();
//        long startTime = System.currentTimeMillis(); // 요청 시작 시간
//
//        try {
//            // 필터 체인 실행
//            chain.doFilter(request, response);
//        } catch (Exception e) {
//            // 에러 발생 시 로그 기록
//            AppLogger.logError("Request processing failed", e, "TraceID", traceId);
//            throw e;
//        } finally {
//            // 요청/응답 정보 수집
//            long duration = System.currentTimeMillis() - startTime;
//
//            AppLogger.logBusinessEvent("HTTP Request",
//                    "TraceID", traceId,
//                    "Method", httpRequest.getMethod(),
//                    "URI", httpRequest.getRequestURI(),
//                    "QueryString", httpRequest.getQueryString(),
//                    "Status", httpResponse.getStatus(),
//                    "Duration", duration + "ms");
//        }
//    }
//}