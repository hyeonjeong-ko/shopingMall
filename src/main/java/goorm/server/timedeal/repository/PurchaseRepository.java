package goorm.server.timedeal.repository;

import goorm.server.timedeal.model.TimeDeal;
import goorm.server.timedeal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import goorm.server.timedeal.model.Purchase;

import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>{
    Optional<Purchase> findByUserAndTimeDeal(User user, TimeDeal timeDeal);

}