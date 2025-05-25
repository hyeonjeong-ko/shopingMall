package goorm.server.timedeal.viewcontroller;

import goorm.server.timedeal.model.User;
import goorm.server.timedeal.service.UserService;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import goorm.server.timedeal.dto.ResTimeDealListDto;
import goorm.server.timedeal.service.TimeDealService;

/**
 * 타임딜 관리자 화면
 * */
@RequestMapping("/v1/admin/deals")
@Controller
@RequiredArgsConstructor
public class AdminPageController {

	private final TimeDealService timeDealService;
	private final UserService userService;

	@GetMapping("")
	public String showTimeDealReservationPage(Model model,
											  @AuthenticationPrincipal UserDetails userDetails) {

		User user = userService.findByLoginId(userDetails.getUsername());

		List<ResTimeDealListDto> timeDeals = timeDealService.getTimeDealList();

		model.addAttribute("timeDeals", timeDeals);
		model.addAttribute("userId", user.getUserId());

		return "deal_admin";
	}
}
