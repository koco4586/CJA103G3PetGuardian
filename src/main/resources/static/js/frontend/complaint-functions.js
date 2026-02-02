/**
 * ========================================
 * 檢舉功能 JavaScript
 * ========================================
 * 功能說明：
 * 1. openComplaintModal() - 開啟檢舉彈窗
 * 2. submitComplaint() - 送出檢舉
 * ========================================
 */

/**
 * 開啟檢舉彈窗
 * @param {number} bookingOrderId - 訂單 ID
 */
window.openComplaintModal = function (bookingOrderId) {
    // 建立彈窗 HTML
    const modalHTML = `
        <div id="complaintModal" style="
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
                max-width: 600px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
            ">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                    <h2 style="margin: 0; color: #2c3e50; font-size: 1.8rem;">
                        <i class="fa-solid fa-flag"></i> 評價檢舉
                    </h2>
                    <button onclick="closeComplaintModal()" style="
                        background: none;
                        border: none;
                        font-size: 1.5rem;
                        cursor: pointer;
                        color: #999;
                    ">✕</button>
                </div>

                <div style="margin-bottom: 20px;">
                    <p style="color: #777; font-size: 0.9rem;">處理保母申訴</p>
                </div>

                <!-- 檢舉類型選擇 -->
                <div style="display: flex; gap: 15px; margin-bottom: 25px;">
                    <div class="complaint-type-btn" data-type="sitter" style="
                        flex: 1;
                        padding: 20px;
                        border: 2px solid #ddd;
                        border-radius: 12px;
                        background: #fff;
                        cursor: pointer;
                        text-align: center;
                        transition: 0.3s;
                    ">
                        <i class="fa-solid fa-calendar-check"></i><br>檢舉保母內容
                    </div>
                    <div class="complaint-type-btn" data-type="order" style="
                        flex: 1;
                        padding: 20px;
                        border: 2px solid #ddd;
                        border-radius: 12px;
                        background: #fff;
                        cursor: pointer;
                        text-align: center;
                        transition: 0.3s;
                    ">
                        <i class="fa-solid fa-shop"></i><br>檢舉訂單
                    </div>
                </div>

                <!-- 檢舉內容 -->
                <div style="margin-bottom: 20px;">
                    <label style="display: block; margin-bottom: 8px; color: #555; font-weight: 500;">
                        檢舉詳細內容：
                    </label>
                    <textarea id="complaintReason" placeholder="請描述發生經過..." style="
                        width: 100%;
                        padding: 12px;
                        border: 2px solid #ddd;
                        border-radius: 10px;
                        min-height: 120px;
                        font-family: inherit;
                        transition: 0.3s;
                        resize: vertical;
                    "></textarea>
                </div>

                <!-- 按鈕 -->
                <div style="text-align: center; display: flex; gap: 10px;">
                    <button onclick="closeComplaintModal()" style="
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
                    <button onclick="submitComplaint(${bookingOrderId})" style="
                        flex: 1;
                        padding: 12px;
                        border: none;
                        border-radius: 8px;
                        background: #ff6b6b;
                        color: white;
                        cursor: pointer;
                        font-size: 0.95rem;
                        font-weight: bold;
                    ">提交申訴</button>
                </div>
            </div>
        </div>
    `;

    // 插入彈窗到 body
    document.body.insertAdjacentHTML('beforeend', modalHTML);

    // 綁定檢舉類型按鈕事件
    const typeBtns = document.querySelectorAll('.complaint-type-btn');
    typeBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            typeBtns.forEach(b => {
                b.style.borderColor = '#ddd';
                b.style.background = '#fff';
                b.style.color = '#000';
                b.style.boxShadow = 'none';
            });
            this.style.borderColor = '#c38d9e';
            this.style.color = '#c38d9e';
            this.style.background = '#fff5f5';
            this.style.boxShadow = '0 5px 15px rgba(195, 141, 158, 0.2)';
        });
    });

    // 預設選擇第一個
    if (typeBtns.length > 0) {
        typeBtns[0].click();
    }
}

/**
 * 關閉檢舉彈窗
 */
window.closeComplaintModal = function () {
    const modal = document.getElementById('complaintModal');
    if (modal) {
        modal.remove();
    }
}

/**
 * 送出檢舉
 * @param {number} bookingOrderId - 訂單 ID
 */
window.submitComplaint = function (bookingOrderId) {
    const reason = document.getElementById('complaintReason').value;

    if (!reason.trim()) {
        alert('⚠️ 請填寫檢舉詳細內容');
        return;
    }

    // 確認提交
    if (!confirm('確定要提交嗎？\n提交後將進入審核流程，請確認內容無誤。')) {
        return;
    }

    // 封裝資料
    const formData = new URLSearchParams();
    formData.append('reportReason', reason);
    formData.append('bookingOrderId', bookingOrderId);

    // 送出到後端
    fetch('/pet/submitComplaint', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok || response.redirected) {
                alert('✅ 申訴成功！\n您的申訴已收到，請耐心等待管理員審核。');
                closeComplaintModal();
                // 可選：重新載入頁面
                // window.location.reload();
            } else {
                alert('❌ 提交失敗，請稍後再試');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ 系統連線異常');
        });
}

/**
 * 修改 reportReview 函數，改為開啟檢舉彈窗
 * @param {number} orderId - 訂單 ID
 */
window.reportReview = function (orderId) {
    openComplaintModal(orderId);
}
