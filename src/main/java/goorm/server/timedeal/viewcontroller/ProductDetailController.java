package goorm.server.timedeal.viewcontroller;

import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import goorm.server.timedeal.dto.ResDetailPageTimeDealDto;
import goorm.server.timedeal.service.TimeDealService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/v1")
@RequiredArgsConstructor
public class ProductDetailController {

	private final TimeDealService timeDealService;
	private final ReviewService reviewService;

	@GetMapping("/products/{productId}")
	public String getProductDetailPage(@PathVariable Long productId, Model model) {
    ResDetailPageTimeDealDto productDetails = timeDealService.getTimeDealDetails(productId);
    Page<ReviewResponseDto> reviews = reviewService.getReviewsByTimeDeal(productId,
            PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));

    model.addAttribute("productDetails", productDetails);
    model.addAttribute("reviews", reviews);

    return "deal_detail";
}
}