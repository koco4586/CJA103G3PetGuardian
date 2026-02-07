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
 * 開啟檢舉彈窗 (用於保母詳情、主頁)
 * @param {number} bookingOrderId - 訂單 ID
 */
window.openComplaintModal = function (bookingOrderId) {
    // 如果已經有彈窗，先移除
    const oldModal = document.getElementById('complaintModal');
    if (oldModal) oldModal.remove();

    // 建立彈窗 HTML (樣式與 injectReportBox 一致，但包在 Modal 內)
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
            z-index: 11000;
            backdrop-filter: blur(3px);
        ">
            <div style="
                background: white;
                border-radius: 15px;
                padding: 30px;
                width: 90%;
                max-width: 500px;
                box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
                position: relative;
                animation: modalFadeIn 0.3s ease;
            ">
                <style>
                    @keyframes modalFadeIn {
                        from { opacity: 0; transform: translateY(-20px); }
                        to { opacity: 1; transform: translateY(0); }
                    }
                    /* 🔥 動態注入檢舉標籤樣式，確保所有頁面皆可正常顯示 */
                    .report-tag {
                        display: inline-block;
                        padding: 6px 14px;
                        margin: 5px;
                        background: #fff;
                        border: 1px solid #ffcdd2;
                        border-radius: 20px;
                        cursor: pointer;
                        font-size: 0.9rem;
                        color: #c62828;
                        transition: all 0.2s;
                    }
                    .report-tag.selected {
                        background: #ffcdd2;
                        font-weight: bold;
                        color: #b71c1c;
                    }
                    .report-tag:hover {
                        background: #ffebee;
                    }
                </style>

                <h4 style="color: #c62828; margin-bottom: 20px; font-weight: bold; font-size: 1.4rem;">
                    <i class="fas fa-flag"></i> 為什麼要檢舉此評價？
                </h4>
                
                <div class="tag-container" style="margin-bottom: 20px;">
                    <span class="report-tag">不實評價</span>
                    <span class="report-tag">惡意攻擊</span>
                    <span class="report-tag">垃圾訊息</span>
                    <span class="report-tag">其他</span>
                </div>

                <textarea class="report-content" id="modalReportContent" style="
                    width: 100%; 
                    height: 120px; 
                    border: 1px solid #ffcdd2; 
                    border-radius: 8px; 
                    padding: 12px; 
                    background:#fff; 
                    font-size: 1rem; 
                    font-family: inherit; 
                    resize: vertical;
                    margin-bottom: 20px;
                    outline: none;
                " placeholder="輸入詳細檢舉理由..."></textarea>
                
                <div style="text-align: right; display: flex; gap: 10px; justify-content: flex-end;">
                    <button onclick="closeComplaintModal()" style="
                        background: #95a5a6; 
                        color: white; 
                        border: none; 
                        padding: 10px 25px; 
                        border-radius: 50px; 
                        cursor: pointer; 
                        font-weight: bold; 
                        transition: all 0.2s;
                    ">
                        <i class="fas fa-times"></i> 取消
                    </button>
                    <button class="submit-report-btn" id="modalSubmitBtn" style="
                        background: #ff6b6b; 
                        color: white; 
                        border: none; 
                        padding: 10px 25px; 
                        border-radius: 50px; 
                        cursor: pointer; 
                        font-weight: bold; 
                        transition: all 0.2s; 
                        display: inline-flex; 
                        align-items: center; 
                        gap: 8px;
                    ">
                        提交檢舉 <i class="fas fa-paper-plane"></i>
                    </button>
                </div>
            </div>
        </div>
    `;

    document.body.insertAdjacentHTML('beforeend', modalHTML);

    const modal = document.getElementById('complaintModal');
    const textarea = modal.querySelector('.report-content');
    const submitBtn = modal.querySelector('#modalSubmitBtn');

    // 標籤點擊邏輯 (僅切換狀態，不填入 textarea)
    modal.querySelectorAll('.report-tag').forEach(tag => {
        tag.onclick = function () {
            this.classList.toggle('selected');
        };
    });

    // 送出按鈕點擊邏輯
    submitBtn.onclick = function () {
        const content = textarea.value.trim();
        const selectedTags = Array.from(modal.querySelectorAll('.report-tag.selected'))
            .map(t => t.innerText);

        if (selectedTags.length === 0 && !content) {
            alert('❌ 請選擇標籤或輸入檢舉理由！');
            return;
        }

        // 合併標籤與內容
        const fullReason = (selectedTags.length > 0 ? `[${selectedTags.join(', ')}] ` : '') + content;
        sendReportToBackend(bookingOrderId, fullReason, null, true);
    };
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

    const base = typeof contextPath !== 'undefined' ? contextPath : '';
    // 送出到後端
    fetch(base + '/pet/submitComplaint', {
        method: 'POST',
        body: formData
    })
        .then(response => {
            if (response.ok || response.redirected) {
                alert('✅ 檢舉已送出！\n您的檢舉已收到，管理員將進行審核。\n評論將立即隱藏。');
                closeComplaintModal();
                // 立即刷新以更新狀態
                window.location.reload();
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
 * 修改 reportReview 函數，改為開啟內嵌式檢舉框
 * @param {HTMLElement} button - 觸發按鈕
 * @param {number} orderId - 訂單 ID
 */
window.reportReview = function (button, orderId) {
    // 統一改為彈窗模式 (不論第一個參數是按鈕還是 ID)
    const finalOrderId = typeof button === 'number' ? button : orderId;
    openComplaintModal(finalOrderId);
}

/**
 * 內嵌式檢舉輸入框（與評價輸入框樣式一致，包在白色卡片內）
 * @param {HTMLElement} button - 檢舉按鈕元素
 * @param {number} orderId - 訂單 ID
 */
window.injectReportBox = function (button, orderId) {
    // 找到評價卡片容器 - 支援多種可能的父容器
    let parentCard = button.closest('.order-review-card')
        || button.closest('.review-card')
        || button.closest('.member-eval-container')
        || button.closest('div[style*="background"]');

    if (!parentCard) {
        console.error('找不到評價卡片容器');
        return;
    }

    // 找到檢舉輸入框容器 (如果已經存在)
    let reportBox = parentCard.querySelector('.dynamic-report-wrapper');

    // 如果已經展開，則收合
    if (reportBox && reportBox.classList.contains('active')) {
        const textarea = reportBox.querySelector('.report-content');
        if (textarea && textarea.value.trim().length > 0) {
            const keepContent = confirm('您的檢舉尚未送出，是否要保留目前內容？\n\n點擊「確定」保留內容\n點擊「取消」直接關閉並移除');
            if (keepContent) {
                reportBox.classList.remove('active');
            } else {
                reportBox.classList.remove('active');
                setTimeout(() => reportBox.remove(), 500);
            }
        } else {
            reportBox.classList.remove('active');
            setTimeout(() => reportBox.remove(), 500);
        }
        return;
    }

    // 如果不存在，則建立
    if (!reportBox) {
        reportBox = document.createElement('div');
        reportBox.className = 'dynamic-report-wrapper';

        // 樣式對齊 injectEvalBox
        reportBox.innerHTML = `
            <h4 style="color: #c62828; margin-bottom: 15px; font-weight: bold;">為什麼要檢舉此評價？</h4>
            
            <div class="tag-container" style="margin-bottom: 15px;">
                <span class="report-tag">不實評價</span>
                <span class="report-tag">惡意攻擊</span>
                <span class="report-tag">垃圾訊息</span>
                <span class="report-tag">其他</span>
            </div>

            <textarea class="report-content" style="width: 100%; height: 100px; border: 1px solid #ffcdd2; border-radius: 8px; padding: 12px; background:#fff; font-size: 1rem; font-family: inherit; resize: vertical;" placeholder="點擊標籤或輸入詳細檢舉理由..."></textarea>
            
            <div style="text-align: right; margin-top: 15px;">
                <button class="cancel-report-btn" style="background: #95a5a6; color: white; border: none; padding: 10px 25px; border-radius: 50px; cursor: pointer; font-weight: bold; margin-right: 10px; transition: all 0.2s;">
                    <i class="fas fa-times"></i> 取消
                </button>
                <button class="submit-report-btn" style="background: #ff6b6b; color: white; border: none; padding: 10px 25px; border-radius: 50px; cursor: pointer; font-weight: bold; transition: all 0.2s; display: inline-flex; align-items: center; gap: 8px;">
                    提交檢舉 <i class="fas fa-paper-plane"></i>
                </button>
            </div>
        `;

        // 插入到日期下方，確保在同一個容器內
        const timeStamp = parentCard.querySelector('.time-stamp')
            || parentCard.querySelector('small')
            || parentCard.querySelector('div[style*="text-align: right"]')
            || parentCard.querySelector('div[style*="color: #888"]');

        if (timeStamp) {
            timeStamp.insertAdjacentElement('afterend', reportBox);
        } else {
            // 如果找不到日期，就插入到容器最後
            parentCard.appendChild(reportBox);
        }

        // 標籤點擊邏輯 (僅切換選取狀態，不自動填入文字框)
        const textarea = reportBox.querySelector('.report-content');
        reportBox.querySelectorAll('.report-tag').forEach(tag => {
            tag.onclick = function () {
                this.classList.toggle('selected');
            };
        });

        // 取消按鈕邏輯
        reportBox.querySelector('.cancel-report-btn').onclick = function () {
            reportBox.classList.remove('active');
            setTimeout(() => reportBox.remove(), 500);
        };

        // 送出按鈕點擊邏輯
        reportBox.querySelector('.submit-report-btn').onclick = function () {
            const content = textarea.value.trim();
            const selectedTags = Array.from(reportBox.querySelectorAll('.report-tag.selected'))
                .map(t => t.innerText);

            if (selectedTags.length === 0 && !content) {
                alert('❌ 請選擇標籤或輸入檢舉理由！');
                return;
            }

            // 合併標籤與內容
            const fullReason = (selectedTags.length > 0 ? `[${selectedTags.join(', ')}] ` : '') + content;
            sendReportToBackend(orderId, fullReason, reportBox);
        };
    }

    // 展開輸入框
    setTimeout(() => {
        reportBox.classList.add('active');
    }, 10);
};

/**
 * 送出檢舉到後端
 * @param {number} orderId - 訂單 ID
 * @param {string} reason - 檢舉理由
 * @param {HTMLElement} reportBox - 檢舉輸入框元素 (如果是彈窗則傳 null)
 * @param {boolean} isModal - 是否為彈窗模式
 */
function sendReportToBackend(orderId, reason, reportBox, isModal = false) {
    const formData = new URLSearchParams();
    formData.append('reportReason', reason);
    formData.append('bookingOrderId', orderId);

    const base = typeof contextPath !== 'undefined' ? contextPath : '';
    let finalBase = base;
    if (finalBase === '/') finalBase = '';

    if (!confirm('確定要提交您的檢舉嗎？')) return;

    fetch(finalBase + '/pet/submitComplaint', {
        method: 'POST',
        body: formData
    })
        .then(async response => {
            if (response.ok || response.redirected) {
                alert('✅ 檢舉已送出！\n您的檢舉已收到，管理員將進行審核。\n評論將立即隱藏。');

                if (isModal) {
                    closeComplaintModal();
                } else if (reportBox) {
                    reportBox.classList.remove('active');
                    const textarea = reportBox.querySelector('.report-content');
                    if (textarea) textarea.value = '';
                    reportBox.querySelectorAll('.report-tag.selected').forEach(tag => tag.classList.remove('selected'));
                }

                // 🔥 檢舉功能：延遲刷新以確保 alert 完全關閉
                setTimeout(() => {
                    window.location.reload();
                }, 100);
            } else {
                const errorMsg = await response.text();
                alert('❌ 送出失敗：' + (errorMsg || '請稍後再試'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('❌ 系統連線異常');
        });
}
