package goorm.server.timedeal.repository;

import goorm.server.timedeal.model.Purchase;
import goorm.server.timedeal.model.Review;
import goorm.server.timedeal.model.TimeDeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByPurchaseAndDeletedAtIsNull(Purchase purchase);
    Page<Review> findByTimeDealAndDeletedAtIsNullOrderByCreatedAtDesc(TimeDeal timeDeal, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.timeDeal = :timeDeal AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    Page<Review> findActiveReviewsForTimeDeal(@Param("timeDeal") TimeDeal timeDeal, Pageable pageable);

}
