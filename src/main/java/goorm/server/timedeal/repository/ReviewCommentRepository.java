package goorm.server.timedeal.repository;

import goorm.server.timedeal.model.Review;
import goorm.server.timedeal.model.ReviewComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    // 삭제되지 않은 특정 리뷰의 댓글들을 페이지네이션하여 조회
    Page<ReviewComment> findByReviewAndDeletedAtIsNull(Review review, Pageable pageable);

    boolean existsByReviewAndDeletedAtIsNull(Review review);
    long countByReviewAndDeletedAtIsNull(Review review);

}
