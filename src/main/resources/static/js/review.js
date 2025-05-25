class ReviewManager {
    constructor() {
        this.modal = document.getElementById('reviewModal');
        this.submitBtn = document.querySelector('.submit-btn');
        this.textarea = document.querySelector('.review-input textarea');
        this.charCount = document.querySelector('.char-count');
        this.reviewContainer = document.getElementById('reviewContainer');
        this.paginationContainer = document.getElementById('pagination');
        this.currentPage = 0;
        
        this.initializeEventListeners();
        this.loadReviews(0); // 초기 리뷰 로드
    }

    initializeEventListeners() {
        // 기존 이벤트 리스너들...

        // 리뷰 작성 버튼
        document.querySelector('.write-review-btn').addEventListener('click', () => this.openModal());

        // 모달 닫기 버튼
        document.querySelector('.close-modal').addEventListener('click', () => this.closeModal());

        // ESC 키로 모달 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') this.closeModal();
        });

        // 모달 외부 클릭시 닫기
        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) this.closeModal();
        });

        // 글자수 카운트
        this.textarea.addEventListener('input', () => this.updateCharCount());

        // 리뷰 제출
        this.submitBtn.addEventListener('click', () => this.submitReview());
    }

    async loadReviews(page) {
        const timeDealId = this.getTimeDealId();
        try {
            const response = await fetch(`/api/reviews/time-deals/${timeDealId}?page=${page}`);
            const data = await response.json();
            this.renderReviews(data);
            this.renderPagination(data);
        } catch (err) {
            console.error('리뷰 로드 중 오류 발생:', err);
        }
    }

renderReviews(data) {
    this.reviewContainer.innerHTML = data.content.map(review => `
        <div class="review-item">
            <div class="review-meta">
                <div class="review-user">${review.userName}</div>
                <div class="review-rating">
                    ${'★'.repeat(review.rating)}
                    ${'<span class="empty-star">★</span>'.repeat(5 - review.rating)}
                </div>
                <div class="review-date">${new Date(review.createdAt).toLocaleDateString()}</div>
            </div>
            <div class="review-content">${review.content}</div>
            
            <!-- 댓글 섹션 -->
            <div class="review-comments">
                <div class="comments-container" id="comments-container-${review.reviewId}">
                    ${review.comments ? review.comments.map(comment => `
                        <div class="comment-item">
                            <div class="comment-meta">
                                <span class="comment-author">${comment.userName}</span>
                                <span class="comment-date">${new Date(comment.createdAt).toLocaleDateString()}</span>
                            </div>
                            <div class="comment-content">${comment.content}</div>
                        </div>
                    `).join('') : ''}
                </div>
                
                <!-- 댓글 입력 영역 -->
                <div class="comment-input-area">
                    <input type="text" 
                           class="comment-input" 
                           data-review-id="${review.reviewId}"
                           placeholder="댓글을 입력하세요...">
                    <button class="comment-submit" 
                            data-review-id="${review.reviewId}"
                            onclick="reviewManager.submitComment('${review.reviewId}', this)">등록</button>
                </div>
            </div>
        </div>
    `).join('');
}

// submitComment 메서드 수정
async submitComment(reviewId, button) {
    if (!reviewId) {
        console.error('리뷰 ID가 없습니다.');
        return;
    }

    const inputElement = button.previousElementSibling;
    const content = inputElement.value.trim();

    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    try {
        const response = await fetch(`/api/reviews/${reviewId}/comments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ content })
        });

        if (response.ok) {
            // 입력 필드 초기화
            inputElement.value = '';
            // 현재 페이지 새로고침
            this.loadReviews(this.currentPage);
        } else {
            const error = await response.json();
            alert(error.message || '댓글 등록에 실패했습니다.');
        }
    } catch (err) {
        console.error('댓글 등록 중 오류 발생:', err);
        alert('서버와 통신 중 문제가 발생했습니다.');
    }
}

// ReviewManager 클래스에 다음 메서드 추가
initializeCommentEventListeners() {
    // 댓글 보기/숨기기 버튼
    document.querySelectorAll('.show-comments-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const reviewId = e.target.getAttribute('data-review-id');
            const commentsContainer = document.getElementById(`comments-${reviewId}`);
            if (commentsContainer) {
                const isHidden = commentsContainer.style.display === 'none';
                commentsContainer.style.display = isHidden ? 'block' : 'none';
                e.target.textContent = isHidden ? '댓글 접기' : 
                    `댓글 ${commentsContainer.querySelectorAll('.comment-item').length}개 모두 보기`;
            }
        });
    });

    // 댓글 등록 버튼
    document.querySelectorAll('.comment-submit').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const reviewId = e.target.getAttribute('data-review-id');
            const inputElement = e.target.parentElement.querySelector('.comment-input');
            const content = inputElement.value.trim();

            if (!content) {
                alert('댓글 내용을 입력해주세요.');
                return;
            }

            try {
                const response = await fetch(`/api/reviews/${reviewId}/comments`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({ content })
                });

                if (response.ok) {
                    alert('댓글이 등록되었습니다.');
                    this.loadReviews(this.currentPage); // 현재 페이지 새로고침
                } else {
                    const error = await response.json();
                    alert(error.message || '댓글 등록에 실패했습니다.');
                }
            } catch (err) {
                console.error('댓글 등록 중 오류 발생:', err);
                alert('서버와 통신 중 문제가 발생했습니다.');
            }
        });
    });
}

    renderPagination(data) {
        if (data.totalPages <= 1) return;

        this.paginationContainer.innerHTML = `
            ${data.first ? '' : `<button class="load-more" onclick="reviewManager.loadReviews(${data.number - 1})">이전</button>`}
            ${data.last ? '' : `<button class="load-more" onclick="reviewManager.loadReviews(${data.number + 1})">다음</button>`}
        `;
    }

    // 기존 메서드들...

    openModal() {
        this.modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }

    closeModal() {
        this.modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }

    updateCharCount() {
        const length = this.textarea.value.length;
        this.charCount.textContent = `${length} / 500`;
    }

    async submitReview() {
        const rating = document.querySelector('input[name="rating"]:checked')?.value;
        const content = this.textarea.value;

        if (!rating) {
            alert('별점을 선택해주세요.');
            return;
        }

        if (content.length < 10) {
            alert('리뷰는 최소 10자 이상 작성해주세요.');
            return;
        }

        try {
            const response = await fetch('/api/reviews', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    rating: parseInt(rating),
                    content: content,
                    timeDealId: this.getTimeDealId()
                })
            });

            const result = await response.json();

            if (response.ok) {
                alert('리뷰가 성공적으로 등록되었습니다.');
                this.closeModal();
                this.loadReviews(0); // 리뷰 목록 새로고침
            } else {
                alert(result.message || '리뷰 등록에 실패했습니다.');
            }
        } catch (err) {
            console.error('리뷰 등록 중 오류 발생:', err);
            alert('서버와 통신 중 문제가 발생했습니다.');
        }
    }

    getTimeDealId() {
        return document.querySelector('.buy-button').getAttribute('data-deal-id');
    }
}

// 전역 변수로 ReviewManager 인스턴스 생성
let reviewManager;
document.addEventListener('DOMContentLoaded', () => {
    reviewManager = new ReviewManager();
});

// 댓글 등록 함수
function submitComment(reviewId) {
    const commentInput = document.querySelector(`#comment-input-${reviewId}`);
    if (!commentInput) {
        console.error('댓글 입력 필드를 찾을 수 없습니다.');
        return;
    }

    const content = commentInput.value.trim();
    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    if (!reviewId || reviewId === 'undefined') {
        console.error('유효하지 않은 리뷰 ID');
        return;
    }

    fetch(`/api/reviews/${reviewId}/comments`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            content: content
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('댓글 등록에 실패했습니다.');
        }
        return response.json();
    })
    .then(data => {
        // 댓글 입력 필드 초기화
        commentInput.value = '';
        // 댓글 목록 새로고침
        loadComments(reviewId);
    })
    .catch(error => {
        console.error('Error:', error);
        alert('댓글 등록에 실패했습니다.');
    });
}

// 댓글 목록 로드 함수
function loadComments(reviewId) {
    if (!reviewId || reviewId === 'undefined') {
        console.error('유효하지 않은 리뷰 ID');
        return;
    }

    fetch(`/api/reviews/${reviewId}/comments`)
        .then(response => response.json())
        .then(data => {
            // 댓글 목록 업데이트 로직
            updateCommentsList(reviewId, data);
        })
        .catch(error => console.error('Error:', error));
}

// 댓글 목록 업데이트 함수
function updateCommentsList(reviewId, commentsData) {
    const commentsContainer = document.querySelector(`#comments-container-${reviewId}`);
    if (!commentsContainer) return;

    // 댓글 목록 HTML 생성 및 업데이트
    // ...
}