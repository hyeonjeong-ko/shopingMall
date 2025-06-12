function startCountdown() {
    const countdownElement = document.getElementById('deal-countdown');

    const timerElement = document.getElementById('deal-timer');
    const endTime = new Date(timerElement.getAttribute('data-end-time'));
    // console.log("endTime:",endTime)

    let intervalId; // `intervalId`를 함수 스코프에서 선언

    function updateTimer() {
        const now = new Date();
        const timeLeft = endTime - now;
        // console.log("현재시간:",now,"남은 시간:",timeLeft);

        if (timeLeft > 0) {
            const days = Math.floor(timeLeft / (1000 * 60 * 60 * 24));

            const hours = Math.floor((timeLeft / (1000 * 60 * 60)) % 24);
            const minutes = Math.floor((timeLeft / (1000 * 60)) % 60);
            const seconds = Math.floor((timeLeft / 1000) % 60);

            // deal-countdown에 D-ㅇ 표시 또는 당일 처리
            if (days === 0) {
                countdownElement.textContent = "D-Day"; // "D-Day"로 표시
            } else {
                countdownElement.textContent = `D-${days}`;
            }

            // timerElement.textContent = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;

            // "일, 시간, 분, 초" 형식으로 표시
            // "D-ㅇ HH:MM:SS" 형식으로 표시
            timerElement.textContent =
                days > 0
                    ? `D-${days} ` +
                    `${hours.toString().padStart(2, '0')}:` +
                    `${minutes.toString().padStart(2, '0')}:` +
                    `${seconds.toString().padStart(2, '0')}`
                    : `D-Day ` +
                    `${hours.toString().padStart(2, '0')}:` +
                    `${minutes.toString().padStart(2, '0')}:` +
                    `${seconds.toString().padStart(2, '0')}`;

        } else {
            timerElement.textContent = "타임딜 종료";
            clearInterval(intervalId);
        }
    }

    updateTimer();
    intervalId = setInterval(updateTimer, 1000);
}

document.addEventListener('DOMContentLoaded', startCountdown);


// 구매하기 버튼 이벤트 리스너 추가
document.addEventListener('DOMContentLoaded', () => {
    const buyButton = document.querySelector('.buy-button');

    if (buyButton) {
        buyButton.addEventListener('click', async () => {
            const dealId = buyButton.getAttribute('data-deal-id');
            const quantity = 1; // 구매 수량 (여기서는 기본적으로 1로 설정)

            try {
                // API 호출
                const response = await fetch(`/api/time-deals/${dealId}/purchases?quantity=${quantity}`, {
                    // const response = await fetch(`/api/test/${dealId}/purchases?quantity=${quantity}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (response.ok) {
                    const result = await response.json();
                    alert(result.message || '구매가 완료되었습니다.');
                    // 페이지 재로딩 또는 재고 업데이트 처리
                    location.reload(); // 새로고침으로 UI 업데이트
                } else {
                    const error = await response.json();
                    alert(error.message || '구매에 실패했습니다.');
                }
            } catch (err) {
                console.error('구매 요청 중 오류 발생:', err);
                alert('서버와 통신 중 문제가 발생했습니다.');
            }
        });
    }
});

function openReviewModal() {
    document.getElementById('reviewModal').style.display = 'block';
    document.body.style.overflow = 'hidden'; // 배경 스크롤 방지
}

function closeReviewModal() {
    document.getElementById('reviewModal').style.display = 'none';
    document.body.style.overflow = 'auto'; // 배경 스크롤 복구
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function (event) {
    if (event.key === 'Escape') {
        closeReviewModal();
    }
});

// 모달 외부 클릭시 닫기
document.getElementById('reviewModal').addEventListener('click', function (event) {
    if (event.target === this) {
        closeReviewModal();
    }
});

// 글자수 카운트
const textarea = document.querySelector('.review-input textarea');
const charCount = document.querySelector('.char-count');

textarea.addEventListener('input', function () {
    const length = this.value.length;
    charCount.textContent = `${length} / 500`;
});
