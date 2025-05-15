package goorm.server.timedeal.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review_comment")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", foreignKey = @ForeignKey(name = "fk_comment_review_id"))
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_comment_user_id"))
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }


    @Builder
    public ReviewComment(Review review, User user, String content) {
        this.review = review;
        this.user = user;
        this.content = content;
    }


}