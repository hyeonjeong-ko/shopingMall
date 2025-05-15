package goorm.server.timedeal.service;


import goorm.server.timedeal.dto.CommentRequestDto;
import goorm.server.timedeal.dto.CommentResponseDto;
import goorm.server.timedeal.dto.ReviewRequestDto;
import goorm.server.timedeal.dto.ReviewResponseDto;
import goorm.server.timedeal.model.*;
import goorm.server.timedeal.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final TimeDealRepository timeDealRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ReviewCommentRepository commentRepository;


    // ReviewService에서 수정
    public Page<ReviewResponseDto> getReviewsByTimeDeal(Long timeDealId, Pageable pageable) {
        TimeDeal timeDeal = timeDealRepository.findById(timeDealId)
                .orElseThrow(() -> new EntityNotFoundException("해당 타임딜을 찾을 수 없습니다."));

    return reviewRepository.findByTimeDealAndDeletedAtIsNullOrderByCreatedAtDesc(timeDeal, pageable)
            .map(ReviewResponseDto::from);
    }



    public ReviewResponseDto createReview(ReviewRequestDto requestDto, String loginId) {
        // 1. 구매 여부 확인
        TimeDeal timeDeal = timeDealRepository.findById(requestDto.timeDealId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));


        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 해당 사용자의 구매 내역 확인
        Purchase purchase = purchaseRepository.findByUserAndTimeDeal(user, timeDeal)
                .orElseThrow(() -> new IllegalStateException("구매하지 않은 상품에는 리뷰를 작성할 수 없습니다."));

        // 2. 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByPurchaseAndDeletedAtIsNull(purchase)) {
            throw new IllegalStateException("이미 리뷰를 작성하셨습니다.");
        }

        // 3. 리뷰 생성
        Review review = new Review();
        review.setUser(user);
        review.setTimeDeal(timeDeal);
        review.setPurchase(purchase);
        review.setRating(requestDto.rating());
        review.setContent(requestDto.content());

        Review savedReview = reviewRepository.save(review);

        return convertToDto(savedReview);
    }

    private ReviewResponseDto convertToDto(Review review) {
        return ReviewResponseDto.from(review);  // Record의 정적 팩토리 메서드 사용
    }

    // 댓글 작성
    public CommentResponseDto addComment(Long reviewId, CommentRequestDto requestDto, String loginId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        ReviewComment comment = ReviewComment.builder()
                .review(review)
                .user(user)
                .content(requestDto.content())
                .build();

        return CommentResponseDto.from(commentRepository.save(comment));
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByReview(Long reviewId, Pageable pageable) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        return commentRepository.findByReviewAndDeletedAtIsNull(review, pageable)
                .map(CommentResponseDto::from);
    }

    // 댓글 삭제 (soft delete)
    public void deleteComment(Long commentId, String loginId) {
        ReviewComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다."));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

        // 댓글 작성자 또는 리뷰 작성자만 삭제 가능
        if (!comment.getUser().equals(user) && !comment.getReview().getUser().equals(user)) {
            throw new IllegalStateException("댓글을 삭제할 권한이 없습니다.");
        }

        comment.softDelete();
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getComments(Long reviewId, Pageable pageable) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("리뷰를 찾을 수 없습니다."));

        return commentRepository.findByReviewAndDeletedAtIsNull(review, pageable)
                .map(CommentResponseDto::from);
    }


}