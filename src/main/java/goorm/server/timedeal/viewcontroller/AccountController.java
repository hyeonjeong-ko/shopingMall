package goorm.server.timedeal.viewcontroller;

import goorm.server.timedeal.model.User;
import goorm.server.timedeal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final UserRepository userRepository;

    @GetMapping("/account")
    public String accountPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String loginId = userDetails.getUsername();  // 여기의 username == loginId
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        model.addAttribute("user", user);
        return "account";
    }
}