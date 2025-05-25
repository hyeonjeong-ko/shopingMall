package goorm.server.timedeal.service;


import goorm.server.timedeal.config.exception.BaseException;
import goorm.server.timedeal.config.exception.BaseResponseStatus;
import goorm.server.timedeal.dto.CommentRequestDto;
import goorm.server.timedeal.dto.CommentResponseDto;
import goorm.server.timedeal.model.Review;
import goorm.server.timedeal.model.ReviewComment;
import goorm.server.timedeal.model.User;
import goorm.server.timedeal.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final ReviewCommentRepository commentRepository;
    private final ReviewQueryService reviewQueryService;
    private final UserService userService;
    private final ReviewValidator reviewValidator;

    public CommentResponseDto addComment(Long reviewId, CommentRequestDto requestDto, String loginId)
            throws BaseException {
        Review review = reviewQueryService.findReviewById(reviewId);
        User user = userService.findByLoginId(loginId);

        ReviewComment comment = createNewComment(review, user, requestDto);
        ReviewComment savedComment = commentRepository.save(comment);

        return convertToCommentResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByReview(Long reviewId, Pageable pageable) throws BaseException {
        Review review = reviewQueryService.findReviewById(reviewId);
        return fetchActiveComments(review, pageable);
    }

    public void deleteComment(Long commentId, String loginId) throws BaseException {
        ReviewComment comment = findCommentById(commentId);
        User user = userService.findByLoginId(loginId);

        reviewValidator.validateCommentDeletion(comment, user);
        comment.softDelete();
    }

    private ReviewComment findCommentById(Long commentId) throws BaseException {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.COMMENT_NOT_FOUND));
    }

    private ReviewComment createNewComment(Review review, User user, CommentRequestDto requestDto) {
        return ReviewComment.builder()
                .review(review)
                .user(user)
                .content(requestDto.content())
                .build();
    }

    private Page<CommentResponseDto> fetchActiveComments(Review review, Pageable pageable) {
        return commentRepository.findByReviewAndDeletedAtIsNull(review, pageable)
                .map(this::convertToCommentResponse);
    }

    private CommentResponseDto convertToCommentResponse(ReviewComment comment) {
        return CommentResponseDto.from(comment);
    }
}