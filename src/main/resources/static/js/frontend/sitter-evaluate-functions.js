

// ========================================
// 保母評價會員功能區塊
// ========================================

/**
 * 開啟保母評價會員的彈窗（無標籤版本）
 * @param {number} orderId - 訂單 ID
 * @param {string} memName - 會員名稱
 */
window.openSitterEvaluateModal = function (orderId, memName) {
    console.log('🔍 開啟保母評價會員彈窗，訂單 ID:', orderId, '會員:', memName);

    // 建立彈窗 HTML（無標籤選擇）
    const modalHTML = `
        <div id="sitterEvaluateModal" style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        ">
            <div style="
                background: white;
                border-radius: 15px;
                padding: 30px;
                width: 90%;
                max-width: 500px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
            ">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h2 style="margin: 0; color: #2c3e50; font-size: 1.5rem;">
                        <i class="fas fa-paw"></i> 評價會員
                    </h2>
                    <button onclick="closeSitterEvaluateModal()" style="
                        background: none;
                        border: none;
                        font-size: 1.5rem;
                        cursor: pointer;
                        color: #999;
                    ">✕</button>
                </div>

                <div style="margin-bottom: 20px;">
                    <p style="color: #777; font-size: 0.9rem;">會員：<strong>${memName}</strong></p>
                </div>

                <!-- 星級評分 -->
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 10px; color: #555; font-weight: 500;">
                        服務體驗評分：
                    </label>
                    <div id="sitterStarRating" class="star-rating" data-rating="0" style="
                        display: flex;
                        gap: 10px;
                        font-size: 2rem;
                    ">
                        <span class="star-btn" data-value="1" style="cursor: pointer; color: #ddd;">★</span>
                        <span class="star-btn" data-value="2" style="cursor: pointer; color: #ddd;">★</span>
                        <span class="star-btn" data-value="3" style="cursor: pointer; color: #ddd;">★</span>
                        <span class="star-btn" data-value="4" style="cursor: pointer; color: #ddd;">★</span>
                        <span class="star-btn" data-value="5" style="cursor: pointer; color: #ddd;">★</span>
                    </div>
                    <p id="sitterRatingText" style="color: #999; font-size: 0.9rem; margin-top: 5px;">請選擇星級</p>
                </div>

                <!-- 評價內容 -->
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 8px; color: #555; font-weight: 500;">
                        評價內容：
                    </label>
                    <textarea id="sitterEvaluateContent" placeholder="分享您對這位會員的評價..." style="
                        width: 100%;
                        padding: 12px;
                        border: 2px solid #ddd;
                        border-radius: 10px;
                        min-height: 100px;
                        font-family: inherit;
                        transition: 0.3s;
                        resize: vertical;
                    "></textarea>
                </div>

                <!-- 按鈕 -->
                <div style="display: flex; gap: 10px;">
                    <button onclick="closeSitterEvaluateModal()" style="
                        flex: 1;
                        padding: 12px;
                        border: 2px solid #ddd;
                        border-radius: 8px;
                        background: white;
                        color: #666;
                        cursor: pointer;
                        font-size: 0.95rem;
                        font-weight: bold;
                    ">取消</button>
                    <button onclick="submitSitterEvaluate(${orderId})" style="
                        flex: 1;
                        padding: 12px;
                        border: none;
                        border-radius: 8px;
                        background: #4CAF50;
                        color: white;
                        cursor: pointer;
                        font-size: 0.95rem;
                        font-weight: bold;
                    ">送出評價</button>
                </div>
            </div>
        </div>
    `;

    // 插入彈窗到 body
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // 初始化星級評分
    const starContainer = document.getElementById('sitterStarRating');
    initStarRating(starContainer);
}

/**
 * 關閉保母評價會員彈窗
 */
window.closeSitterEvaluateModal = function () {
    const modal = document.getElementById('sitterEvaluateModal');
    if (modal) {
        modal.remove();
    }
}

/**
 * 送出保母對會員的評價
 * @param {number} orderId - 訂單 ID
 */
window.submitSitterEvaluate = function (orderId) {
    const starContainer = document.getElementById('sitterStarRating');
    const rating = parseInt(starContainer.getAttribute('data-rating'));
    const content = document.getElementById('sitterEvaluateContent').value;

    // 驗證
    if (rating === 0) {
        alert('⚠️ 請選擇星級評分');
        return;
    }

    if (!content.trim()) {
        alert('⚠️ 請填寫評價內容');
        return;
    }

    // 確認送出
    if (!confirm('確定要送出評價嗎？')) {
        return;
    }

    // 封裝資料
    const formData = new URLSearchParams();
    formData.append('bookingOrderId', orderId);
    formData.append('starRating', rating);
    formData.append('content', content);

    // 送出到後端
    fetch('/pet/sitter/evaluate/save', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok || response.redirected) {
                alert('✅ 評價成功！感謝您的回饋。');
                closeSitterEvaluateModal();
                // 重新載入頁面
                window.location.reload();
            } else {
                alert('❌ 評價失敗，請稍後再試');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ 系統連線異常');
        });
}
