// 主要分頁切換
function switchMainTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));

    event.target.classList.add('active');
    document.getElementById('panel-' + tabName).classList.add('active');
}

// 訂單子分頁切換
function switchOrderTab(tabName) {
    document.querySelectorAll('.sub-tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.sub-panel').forEach(panel => panel.classList.remove('active'));

    event.target.classList.add('active');
    document.getElementById('order-' + tabName).classList.add('active');
}

// 格式化時間，將 LocalDateTime 格式轉為易讀格式
function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    // 將 "2025-12-31T10:30:00" 轉為 "2025-12-31 10:30:00"
    return dateTimeStr.replace('T', ' ').substring(0, 19);
}

// 顯示退貨詳情 Modal
function showReturnDetail(returnId) {
    fetch('/admin/store/return/' + returnId)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('modal-returnId').textContent = '#' + data.returnId;
                document.getElementById('modal-orderId').textContent = '#' + data.orderId;
                document.getElementById('modal-buyerName').textContent = data.buyerName || '-';
                document.getElementById('modal-sellerName').textContent = data.sellerName || '-';
                document.getElementById('modal-refundAmount').textContent = '$' + (data.refundAmount || 0);
                // 使用格式化函數處理申請時間
                document.getElementById('modal-applyTime').textContent = formatDateTime(data.applyTime);
                document.getElementById('modal-returnReason').textContent = data.returnReason || '-';

                const imagesContainer = document.getElementById('modal-images-container');
                const imagesDiv = document.getElementById('modal-images');

                if (data.hasImages && data.returnImages && data.returnImages.length > 0) {
                    imagesDiv.innerHTML = '';
                    data.returnImages.forEach((imageBase64, index) => {
                        const img = document.createElement('img');
                        img.src = imageBase64;
                        img.alt = '退貨圖片 ' + (index + 1);
                        img.style.cssText = 'width: 150px; height: 150px; object-fit: cover; border-radius: 8px; border: 1px solid #ddd; cursor: pointer;';
                        img.onclick = function() {
                            window.open(imageBase64, '_blank');
                        };
                        imagesDiv.appendChild(img);
                    });
                    imagesContainer.style.display = 'block';
                } else {
                    imagesContainer.style.display = 'none';
                }

                document.getElementById('returnDetailModal').classList.add('active');
            } else {
                alert('載入退貨詳情失敗: ' + (data.error || '未知錯誤'));
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('載入退貨詳情失敗');
        });
}

// 關閉 Modal
function closeModal() {
    document.getElementById('returnDetailModal').classList.remove('active');
}

// 頁面載入完成後執行
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('returnDetailModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });
    }
});