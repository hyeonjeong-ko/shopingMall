package goorm.server.timedeal.model;

import goorm.server.timedeal.dto.ReviewRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.sql.ConnectionBuilder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review")
@Getter @Setter
@NoArgsConstructor
@Builder @AllArgsConstructor
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_review_user_id"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_deal_id", foreignKey = @ForeignKey(name = "fk_review_time_deal_id"))
    private TimeDeal timeDeal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id", foreignKey = @ForeignKey(name = "fk_review_purchase_id"))
    private Purchase purchase;

    @Column(nullable = false)
    private Integer rating;  // 1-5 별점

    @Column(nullable = false, length = 1000)
    private String content;

    @Builder.Default
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReviewComment> comments = new ArrayList<>();

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public static Review createReview(User user, TimeDeal timeDeal, ReviewRequestDto requestDto) {
        return Review.builder()
                .user(user)
                .timeDeal(timeDeal)
                .rating(requestDto.rating())
                .content(requestDto.content())
                .build();
    }


}