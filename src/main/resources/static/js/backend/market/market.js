// 主要分頁切換
function switchMainTab(tabName) {
    // 切換按鈕樣式
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.closest('.tab-btn').classList.add('active');

    // 切換面板
    document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));
    document.getElementById('panel-' + tabName).classList.add('active');
}

// 訂單子分頁切換
function switchOrderTab(tabName) {
    // 切換按鈕樣式
    document.querySelectorAll('.sub-tab-btn').forEach(btn => btn.classList.remove('active'));
    event.target.closest('.sub-tab-btn').classList.add('active');

    // 切換子面板
    document.querySelectorAll('.sub-panel').forEach(panel => panel.classList.remove('active'));
    document.getElementById('order-' + tabName).classList.add('active');
}

// 顯示退貨詳情 Modal
function showReturnDetail(returnId) {
    fetch('/admin/store/return/' + returnId)
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                document.getElementById('modal-returnId').textContent = '#REF-' + data.returnId;
                document.getElementById('modal-orderId').textContent = '#' + data.orderId;
                document.getElementById('modal-buyerName').textContent = data.buyerName || '-';
                document.getElementById('modal-sellerName').textContent = data.sellerName || '-';
                document.getElementById('modal-orderTotal').textContent = '$' + (data.orderTotal || 0).toLocaleString();
                document.getElementById('modal-refundAmount').textContent = '$' + (data.refundAmount || 0).toLocaleString();
                document.getElementById('modal-applyTime').textContent = data.applyTime || '-';
                document.getElementById('modal-returnReason').textContent = data.returnReason || '無';

                // 顯示 Modal
                document.getElementById('returnDetailModal').classList.add('active');
            } else {
                alert('載入退貨詳情失敗：' + (data.error || '未知錯誤'));
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

// 點擊 Modal 外部關閉
document.getElementById('returnDetailModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeModal();
    }
});

// 根據 URL 參數設定預設分頁
document.addEventListener('DOMContentLoaded', function() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderTab = urlParams.get('orderTab');

    if (orderTab) {
        // 模擬點擊對應的子分頁按鈕
        const buttons = document.querySelectorAll('.sub-tab-btn');
        buttons.forEach(btn => {
            if (btn.textContent.includes(orderTab === 'pending' ? '待完成' :
                orderTab === 'closed' ? '結案訂單' : '退貨')) {
                btn.click();
            }
        });
    }
});