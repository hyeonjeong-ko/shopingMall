package goorm.server.timedeal.repository;

import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.Review;
import goorm.server.timedeal.model.TimeDeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByPurchaseAndDeletedAtIsNull(Purchase purchase);
    Page<Review> findByTimeDealAndDeletedAtIsNullOrderByCreatedAtDesc(TimeDeal timeDeal, Pageable pageable);

}
