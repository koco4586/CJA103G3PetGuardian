// ==========================================
// 1. 分頁切換邏輯 (Tabs)
// ==========================================

// 主分頁切換
function switchTab(tabName) {
    const event = window.event;
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));

    if (event) {
        event.target.closest('.tab-btn').classList.add('active');
    }

    const targetPanel = document.getElementById('panel-' + tabName);
    if (targetPanel) {
        targetPanel.classList.add('active');
    }
}

// 訂單子分頁切換
function switchOrderTab(tabName) {
    const event = window.event;
    document.querySelectorAll('.sub-tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.sub-panel').forEach(panel => panel.classList.remove('active'));

    if (event) {
        event.target.closest('.sub-tab-btn').classList.add('active');
    }

    const targetSubPanel = document.getElementById('order-' + tabName);
    if (targetSubPanel) {
        targetSubPanel.classList.add('active');
    }
}

// ==========================================
// 2. 類別 Modal 相關 (Category)
// ==========================================

function openCategoryModal() {
    document.getElementById('categoryModalTitle').innerText = '新增類別';
    document.getElementById('proTypeIdEdit').value = '';
    document.getElementById('proTypeName').value = '';
    document.getElementById('categoryForm').action = '/admin/store/protype/add';
    document.getElementById('categoryModal').style.display = 'flex';
}

function closeCategoryModal() {
    document.getElementById('categoryModal').style.display = 'none';
}

function editCategory(btn) {
    const id = btn.getAttribute('data-id');
    const name = btn.getAttribute('data-name');
    document.getElementById('categoryModalTitle').innerText = '編輯類別';
    document.getElementById('proTypeIdEdit').value = id;
    document.getElementById('proTypeName').value = name;
    document.getElementById('categoryForm').action = '/admin/store/protype/update';
    document.getElementById('categoryModal').style.display = 'flex';
}

// ==========================================
// 3. 退貨詳情 Modal 相關 (Return Detail)
// ==========================================

function openModal(returnId) {
    fetch('/admin/store/return/' + returnId)
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.success) {
                document.getElementById('modal-returnId').innerText = '#REF-' + data.returnId;
                document.getElementById('modal-orderId').innerText = '#' + data.orderId;
                document.getElementById('modal-buyerName').innerText = data.buyerName || '-';
                document.getElementById('modal-sellerName').innerText = data.sellerName || '-';
                document.getElementById('modal-refundAmount').innerText = '$' + data.refundAmount;
                document.getElementById('modal-applyTime').innerText = (data.applyTime ? data.applyTime.replace('T', ' ') : '-');                document.getElementById('modal-returnReason').innerText = data.returnReason || '無';

                // 處理退貨圖片
                var imagesContainer = document.getElementById('modal-images-container');
                var imagesDiv = document.getElementById('modal-images');
                if (data.returnImages && data.returnImages.length > 0) {
                    imagesContainer.style.display = 'block';
                    imagesDiv.innerHTML = '';
                    data.returnImages.forEach(function(url) {
                        var img = document.createElement('img');
                        img.src = url;
                        img.alt = '退貨圖片';
                        img.style.cssText = 'width: 120px; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer;';
                        img.onclick = function() {
                            window.open(url, '_blank');
                        };
                        imagesDiv.appendChild(img);
                    });
                } else {
                    imagesContainer.style.display = 'none';
                }

                document.getElementById('returnDetailModal').classList.add('active');
            } else {
                alert('無法取得退貨詳情');
            }
        })
        .catch(function(error) {
            console.error('Error:', error);
            alert('取得退貨詳情失敗');
        });
}

function closeModal() {
    document.getElementById('returnDetailModal').classList.remove('active');
}

// ==========================================
// 4. 後台訂單詳情 Modal 邏輯（新增）
// ==========================================

// 開啟訂單詳情 Modal，透過 AJAX 取得資料
function openAdminOrderDetail(orderId) {
    var modal = document.getElementById('adminOrderDetailModal');
    var loading = document.getElementById('adminOrderLoading');
    var content = document.getElementById('adminOrderContent');

    // 顯示 Modal 及載入狀態
    modal.classList.add('active');
    loading.style.display = 'block';
    content.style.display = 'none';

    // 設定訂單編號
    document.getElementById('adminDetailOrderId').innerText = '#' + orderId;

    // 呼叫後台 API 取得訂單詳情
    fetch('/admin/store/order/' + orderId + '/detail')
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (!data.success) {
                alert('取得訂單詳情失敗：' + (data.message || '未知錯誤'));
                closeAdminOrderDetail();
                return;
            }

            // 填入訂單狀態
            fillAdminOrderStatus(data.orderStatus);

            // 填入訂單基本資訊
            document.getElementById('adminDetailOrderTime').innerText = (data.orderTime ? data.orderTime.replace('T', ' ') : '-');            document.getElementById('adminDetailBuyerName').innerText = data.buyerName || '-';
            document.getElementById('adminDetailOrderTotal').innerText = '$' + (data.orderTotal || 0);

            // 填入收件資訊
            document.getElementById('adminDetailReceiverName').innerText = data.receiverName || '-';
            document.getElementById('adminDetailReceiverPhone').innerText = data.receiverPhone || '-';
            document.getElementById('adminDetailReceiverAddress').innerText = data.receiverAddress || '-';

            // 備註欄位（有備註才顯示）
            var specialRow = document.getElementById('adminDetailSpecialRow');
            if (data.specialInstructions && data.specialInstructions.trim() !== '') {
                specialRow.style.display = 'flex';
                document.getElementById('adminDetailSpecialInstructions').innerText = data.specialInstructions;
            } else {
                specialRow.style.display = 'none';
            }

            // 填入商品明細
            fillAdminOrderItems(data.items || []);

            // 隱藏載入、顯示內容
            loading.style.display = 'none';
            content.style.display = 'block';
        })
        .catch(function(error) {
            console.error('取得訂單詳情失敗:', error);
            alert('取得訂單詳情失敗，請稍後再試');
            closeAdminOrderDetail();
        });
}

// 關閉訂單詳情 Modal
function closeAdminOrderDetail() {
    document.getElementById('adminOrderDetailModal').classList.remove('active');
}

// 根據訂單狀態碼產生對應的狀態標籤
function fillAdminOrderStatus(status) {
    var el = document.getElementById('adminDetailStatus');
    var map = {
        0: { text: '已付款', bg: '#fff3cd', color: '#856404' },
        1: { text: '已出貨', bg: '#cce5ff', color: '#004085' },
        2: { text: '已完成', bg: '#d4edda', color: '#155724' },
        3: { text: '已取消', bg: '#f8d7da', color: '#721c24' },
        4: { text: '申請退貨中', bg: '#fff3cd', color: '#856404' },
        5: { text: '退貨完成', bg: '#f8d7da', color: '#721c24' },
        6: { text: '已撥款', bg: '#d4edda', color: '#155724' }
    };

    var info = map[status] || { text: '未知', bg: '#eee', color: '#666' };
    el.innerHTML = '<span style="background:' + info.bg + '; color:' + info.color
        + '; padding:4px 10px; border-radius:20px; font-size:0.85rem; font-weight:600;">'
        + info.text + '</span>';
}

// 動態產生訂單商品明細列表
function fillAdminOrderItems(items) {
    var container = document.getElementById('adminOrderItemsContainer');
    container.innerHTML = '';

    if (!items || items.length === 0) {
        container.innerHTML = '<p style="color: #999; text-align: center;">無商品明細</p>';
        return;
    }

    items.forEach(function(item) {
        var itemDiv = document.createElement('div');
        itemDiv.style.cssText = 'display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0;';

        // 商品圖片
        var img = document.createElement('img');
        img.src = item.productImage || '/images/default-product.png';
        img.alt = item.productName || '';
        img.style.cssText = 'width: 50px; height: 50px; object-fit: cover; border-radius: 8px;';
        img.onerror = function() {
            this.src = '/images/default-product.png';
        };

        // 商品名稱和單價
        var info = document.createElement('div');
        info.style.cssText = 'flex: 1;';
        info.innerHTML = '<div style="font-weight: 600;">' + (item.productName || '-') + '</div>'
            + '<div style="color: #999; font-size: 0.85rem;">$' + (item.proPrice || 0) + ' x ' + (item.quantity || 0) + '</div>';

        // 小計金額
        var subtotal = document.createElement('div');
        subtotal.style.cssText = 'font-weight: 700; color: var(--orange, #e67e22);';
        subtotal.innerText = '$' + (item.subtotal || 0);

        itemDiv.appendChild(img);
        itemDiv.appendChild(info);
        itemDiv.appendChild(subtotal);
        container.appendChild(itemDiv);
    });
}