/**
 * PetGuardian - 賣家管理中心 JavaScript
 */

// ==================== 商品 Modal 相關 ====================

function openProductModal() {
    document.getElementById('productModal').classList.add('active');
    document.getElementById('modalTitle').innerText = '新增商品';
    document.getElementById('proId').value = '';
    document.getElementById('proName').value = '';
    document.getElementById('proPrice').value = '';
    document.getElementById('proDescription').value = '';
    document.getElementById('stockQuantity').value = '';
    document.getElementById('proState').value = '1';
}

function closeProductModal() {
    document.getElementById('productModal').classList.remove('active');
}

function editProduct(btn) {
    document.getElementById('productModal').classList.add('active');
    document.getElementById('modalTitle').innerText = '編輯商品';
    document.getElementById('proId').value = btn.getAttribute('data-id');
    document.getElementById('proName').value = btn.getAttribute('data-name');
    document.getElementById('proTypeId').value = btn.getAttribute('data-type');
    document.getElementById('proPrice').value = btn.getAttribute('data-price');
    document.getElementById('proDescription').value = btn.getAttribute('data-desc');
    document.getElementById('stockQuantity').value = btn.getAttribute('data-stock');
    document.getElementById('proState').value = btn.getAttribute('data-state');
}

// 點擊 Modal 外部區域關閉
document.getElementById('productModal')?.addEventListener('click', function(event) {
    if (event.target === this) {
        closeProductModal();
    }
});

// ==================== 評價 Modal 相關 ====================

/**
 * 顯示評價 Modal
 */
function showReviewModal(orderId) {
    // 發送 AJAX 請求取得評價資料
    fetch(`/api/seller/order/${orderId}/review`)
        .then(response => {
            if (!response.ok) {
                throw new Error('評價不存在');
            }
            return response.json();
        })
        .then(data => {
            const content = `
                <div style="background: #f9f9f9; padding: 1.5rem; border-radius: 8px;">
                    <div style="margin-bottom: 1rem;">
                        <strong>評分：</strong>
                        <span style="color: #ffc107; font-size: 1.2rem;">
                            ${'★'.repeat(data.rating)}${'☆'.repeat(5 - data.rating)}
                        </span>
                        <span style="color: #666; margin-left: 8px;">(${data.rating}/5)</span>
                    </div>
                    <div style="margin-bottom: 1rem;">
                        <strong>評價時間：</strong>
                        <span style="color: #666;">${data.reviewTime}</span>
                    </div>
                    <div>
                        <strong>評價內容：</strong>
                        <p style="margin: 0.5rem 0 0 0; color: #333; line-height: 1.6;">
                            ${data.reviewContent || '(買家未留下文字評價)'}
                        </p>
                    </div>
                </div>
            `;

            document.getElementById('reviewContent').innerHTML = content;
            document.getElementById('reviewModal').classList.add('active');
        })
        .catch(error => {
            console.error('Error:', error);
            alert('載入評價失敗：' + error.message);
        });
}

/**
 * 關閉評價 Modal
 */
function closeReviewModal() {
    document.getElementById('reviewModal').classList.remove('active');
}

// 點擊 Modal 外部區域關閉
document.getElementById('reviewModal')?.addEventListener('click', function(event) {
    if (event.target === this) {
        closeReviewModal();
    }
});

// ==================== 評論列表展開/收合 ====================

/**
 * 切換評論列表顯示
 */
function toggleReviewsList() {
    const reviewsList = document.getElementById('reviewsList');
    if (reviewsList) {
        if (reviewsList.style.display === 'none' || reviewsList.style.display === '') {
            reviewsList.style.display = 'block';
            // 平滑滾動到評論列表
            reviewsList.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
        } else {
            reviewsList.style.display = 'none';
        }
    }
}

// ==================== 頁面載入完成後執行 ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('賣家管理中心 JavaScript 已載入');

    // 如果有成功訊息，3秒後自動隱藏
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s ease';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 3000);
    });
});