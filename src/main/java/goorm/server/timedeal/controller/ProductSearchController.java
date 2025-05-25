package goorm.server.timedeal.controller;

import goorm.server.timedeal.config.exception.BaseResponse;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 네이버 쇼핑 API 기반 상품 검색 컨트롤러
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private static final int ITEMS_PER_PAGE = 10;
    private static final int MAX_SEARCHABLE_ITEMS = 1000;

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.api-url}")
    private String naverApiUrl;

    private final RestTemplate restTemplate;

    /**
     * 네이버 쇼핑 상품 검색 API
     * 
     * @apiNote 네이버 API 정책상 최대 1000개 항목까지만 조회 가능
     */
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "1") int page) {
        
        Map<String, Object> searchResult = executeNaverApiSearch(query, page);
        return ResponseEntity.ok(new BaseResponse<>(BaseResponseStatus.SUCCESS, searchResult));
    }

    private Map<String, Object> executeNaverApiSearch(String query, int page) {
        Map<String, Object> initialSearchResult = performInitialSearch(query);
        int totalResults = extractTotalResults(initialSearchResult);
        int totalPages = calculateTotalPages(totalResults);
        int adjustedPage = adjustPageNumber(page, totalPages);
        int startIndex = calculateStartIndex(adjustedPage);

        Map<String, Object> finalSearchResult = performPagedSearch(query, startIndex);
        return createSearchResponse(finalSearchResult, adjustedPage, totalResults, totalPages);
    }

    private Map<String, Object> performInitialSearch(String query) {
        String initialApiUrl = buildApiUrl(query, 1);
        return executeApiCall(initialApiUrl);
    }

    private Map<String, Object> performPagedSearch(String query, int startIndex) {
        String pagedApiUrl = buildApiUrl(query, startIndex);
        return executeApiCall(pagedApiUrl);
    }

    private String buildApiUrl(String query, int startIndex) {
        return String.format("%s?query=%s&display=%d&start=%d", 
                naverApiUrl, query, ITEMS_PER_PAGE, startIndex);
    }

    private Map<String, Object> executeApiCall(String apiUrl) {
        HttpEntity<String> requestEntity = createRequestHeader();
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl, 
                HttpMethod.GET, 
                requestEntity, 
                Map.class
        );
        return response.getBody();
    }

    private HttpEntity<String> createRequestHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);
        return new HttpEntity<>(headers);
    }

    private int extractTotalResults(Map<String, Object> searchResult) {
        return (int) searchResult.getOrDefault("total", 0);
    }

    private int calculateTotalPages(int totalResults) {
        return (int) Math.ceil((double) totalResults / ITEMS_PER_PAGE);
    }

    private int adjustPageNumber(int requestedPage, int totalPages) {
        return Math.min(requestedPage, totalPages);
    }

    private int calculateStartIndex(int page) {
        int startIndex = (page - 1) * ITEMS_PER_PAGE + 1;
        return Math.min(startIndex, MAX_SEARCHABLE_ITEMS);
    }

    private Map<String, Object> createSearchResponse(
            Map<String, Object> searchResult, 
            int currentPage, 
            int totalResults, 
            int totalPages) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("items", searchResult.get("items"));
        response.put("currentPage", currentPage);
        response.put("total", totalResults);
        response.put("itemsPerPage", ITEMS_PER_PAGE);
        response.put("totalPages", totalPages);
        
        return response;
    }
}