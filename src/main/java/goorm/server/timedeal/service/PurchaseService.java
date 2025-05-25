package goorm.server.timedeal.service;

import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.ResPurchaseDto;
import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.model.enums.PurchaseStatus;
import goorm.server.timedeal.repository.PurchaseRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;

    /**
     * 구매 기록을 생성하고 저장하는 메서드
     *
     * @param timeDeal 구매한 타임딜
     * @param user     구매한 유저
     * @param quantity 구매 수량
     * @return 구매 완료 메시지
     */
    @Transactional
    public ResPurchaseDto createPurchaseRecord(TimeDeal timeDeal, User user, int quantity) {
        Purchase purchase = savePurchaseRecord(timeDeal, user, quantity);

        return new ResPurchaseDto(
                user.getUserId(),
                quantity,
                purchase.getPurchaseTime(),
                "PURCHASED"
        );
    }

    private Purchase savePurchaseRecord(TimeDeal timeDeal, User user, int quantity) {
        Purchase purchase = new Purchase();
        purchase.setTimeDeal(timeDeal);
        purchase.setUser(user);
        purchase.setQuantity(quantity);
        purchase.setPurchaseTime(LocalDateTime.now());
        purchase.setStatus(PurchaseStatus.PURCHASED);
        purchaseRepository.save(purchase);
        return purchase;
    }

    public Purchase validatePurchaseExists(User user, TimeDeal timeDeal) throws BaseException {
        return purchaseRepository.findByUserAndTimeDeal(user, timeDeal)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.PURCHASE_NOT_FOUND));
    }


}
