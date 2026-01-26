/**
 * PetGuardian - 賣家管理中心 JavaScript
 * 商品管理、訂單管理、營運概況
 */

// 全域變數
let existingImagesList = [];
let deleteImageIdsList = [];

// ==================== 商品管理 ====================

// 開啟新增商品 Modal
function openProductModal() {
    document.getElementById('productModal').classList.add('active');
    document.getElementById('modalTitle').innerText = '新增商品';

    // 清空表單
    document.getElementById('productForm').reset();
    document.getElementById('proId').value = '';

    // 清空圖片相關
    existingImagesList = [];
    deleteImageIdsList = [];
    document.getElementById('existingImages').innerHTML = '';
    document.getElementById('newImagePreview').innerHTML = '';
    document.getElementById('deleteImageInputs').innerHTML = '';
    document.getElementById('productImages').value = '';
}

// 關閉商品 Modal
function closeProductModal() {
    document.getElementById('productModal').classList.remove('active');
}

// 開啟編輯商品 Modal
function openEditProductModal(button) {
    const proId = button.getAttribute('data-pro-id');
    const proName = button.getAttribute('data-pro-name');
    const proTypeId = button.getAttribute('data-pro-type-id');
    const proPrice = button.getAttribute('data-pro-price');
    const proDescription = button.getAttribute('data-pro-description');
    const stockQuantity = button.getAttribute('data-stock-quantity');
    const proState = button.getAttribute('data-pro-state');

    // 開啟 Modal
    document.getElementById('productModal').classList.add('active');
    document.getElementById('modalTitle').innerText = '編輯商品';

    // 填入商品資料
    document.getElementById('proId').value = proId;
    document.getElementById('proName').value = proName;
    document.getElementById('proTypeId').value = proTypeId;
    document.getElementById('proPrice').value = proPrice || '';
    document.getElementById('proDescription').value = proDescription || '';
    document.getElementById('stockQuantity').value = stockQuantity;
    document.getElementById('proState').value = proState;

    // 清空新圖片預覽
    document.getElementById('newImagePreview').innerHTML = '';
    document.getElementById('productImages').value = '';

    // 重置刪除清單
    deleteImageIdsList = [];
    document.getElementById('deleteImageInputs').innerHTML = '';

    // 載入現有圖片
    loadExistingImages(proId);
}

// 載入現有商品圖片
function loadExistingImages(proId) {
    fetch(`/seller/product/${proId}/images`)
        .then(response => response.json())
        .then(images => {
            existingImagesList = images;
            const container = document.getElementById('existingImages');
            container.innerHTML = '';

            if (!images || images.length === 0) {
                return;
            }

            images.forEach(img => {
                const imgWrapper = document.createElement('div');
                imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';
                imgWrapper.setAttribute('data-pic-id', img.productPicId);

                const imgElement = document.createElement('img');
                imgElement.src = img.imageBase64;
                imgElement.alt = '商品圖片';
                imgElement.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px;';

                const deleteBtn = document.createElement('button');
                deleteBtn.type = 'button';
                deleteBtn.innerHTML = '&times;';
                deleteBtn.style.cssText = 'position: absolute; top: -8px; right: -8px; background: #ff4d4f; color: white; border: none; border-radius: 50%; width: 24px; height: 24px; cursor: pointer; font-size: 14px; line-height: 1;';
                deleteBtn.onclick = function() {
                    markImageForDeletion(img.productPicId, imgWrapper);
                };

                imgWrapper.appendChild(imgElement);
                imgWrapper.appendChild(deleteBtn);
                container.appendChild(imgWrapper);
            });
        })
        .catch(error => {
            console.error('載入圖片失敗:', error);
        });
}

// 標記圖片待刪除
function markImageForDeletion(picId, wrapper) {
    // 加入刪除清單
    deleteImageIdsList.push(picId);

    // 移除預覽
    wrapper.remove();

    // 更新隱藏欄位
    updateDeleteImageInputs();
}

// 更新刪除圖片的隱藏欄位
function updateDeleteImageInputs() {
    const container = document.getElementById('deleteImageInputs');
    container.innerHTML = '';

    deleteImageIdsList.forEach(id => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deleteImageIds';
        input.value = id;
        container.appendChild(input);
    });
}

// 預覽新上傳的圖片（限制最多1張）
function previewNewImages(input) {
    const container = document.getElementById('newImagePreview');
    const files = input.files;

    if (!files || files.length === 0) {
        return;
    }

    // 計算目前已有的圖片數量（現有圖片 - 待刪除圖片 + 新圖片預覽）
    const existingCount = existingImagesList.length - deleteImageIdsList.length;
    const currentPreviewCount = container.querySelectorAll('div').length;
    const totalAfterUpload = existingCount + currentPreviewCount + files.length;

    // 限制最多1張圖片
    if (totalAfterUpload > 1) {
        alert('最多只能上傳 1 張圖片，請先刪除現有圖片後再上傳新圖片');
        input.value = '';
        return;
    }

    // 如果已經有預覽圖片，先清空
    if (currentPreviewCount > 0) {
        container.innerHTML = '';
    }

    // 只處理第一張圖片
    const file = files[0];

    if (!file.type.startsWith('image/')) {
        alert('請選擇圖片檔案');
        input.value = '';
        return;
    }

    const reader = new FileReader();
    reader.onload = function(e) {
        const imgWrapper = document.createElement('div');
        imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';

        const img = document.createElement('img');
        img.src = e.target.result;
        img.alt = '新圖片預覽';
        img.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 2px solid #51cf66;';

        const deleteBtn = document.createElement('button');
        deleteBtn.type = 'button';
        deleteBtn.innerHTML = '&times;';
        deleteBtn.style.cssText = 'position: absolute; top: -8px; right: -8px; background: #ff4d4f; color: white; border: none; border-radius: 50%; width: 24px; height: 24px; cursor: pointer; font-size: 14px; line-height: 1;';
        deleteBtn.onclick = function() {
            imgWrapper.remove();
            // 清空 file input
            document.getElementById('productImages').value = '';
        };

        imgWrapper.appendChild(img);
        imgWrapper.appendChild(deleteBtn);
        container.appendChild(imgWrapper);
    };
    reader.readAsDataURL(file);
}

// ==================== 營運概況 - 評價列表 ====================

// 展開/收合評價列表
function toggleReviewsList() {
    const reviewsList = document.getElementById('reviewsList');
    if (reviewsList) {
        if (reviewsList.style.display === 'none' || reviewsList.style.display === '') {
            reviewsList.style.display = 'block';
            // 滾動到評價列表
            reviewsList.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else {
            reviewsList.style.display = 'none';
        }
    }
}

// ==================== 訂單管理 - 訂單詳情 Modal ====================

// 開啟訂單詳情 Modal
function openOrderDetailModal(button) {
    const orderId = button.getAttribute('data-order-id');
    const buyerName = button.getAttribute('data-buyer-name');
    const orderTotal = button.getAttribute('data-order-total');
    const orderTime = button.getAttribute('data-order-time');
    const orderStatus = button.getAttribute('data-order-status');
    const receiverName = button.getAttribute('data-receiver-name');
    const receiverPhone = button.getAttribute('data-receiver-phone');
    const receiverAddress = button.getAttribute('data-receiver-address');
    const specialInstructions = button.getAttribute('data-special-instructions');

    // 填入訂單基本資訊
    document.getElementById('detailOrderId').textContent = '#' + orderId;
    document.getElementById('detailBuyerName').textContent = buyerName || '未知買家';
    document.getElementById('detailOrderTotal').textContent = '$' + formatNumber(orderTotal);
    document.getElementById('detailOrderTime').textContent = orderTime || '-';
    document.getElementById('detailReceiverName').textContent = receiverName || '-';
    document.getElementById('detailReceiverPhone').textContent = receiverPhone || '-';
    document.getElementById('detailReceiverAddress').textContent = receiverAddress || '-';

    // 備註
    const specialRow = document.getElementById('detailSpecialRow');
    if (specialInstructions && specialInstructions.trim() && specialInstructions !== 'null') {
        specialRow.style.display = 'flex';
        document.getElementById('detailSpecialInstructions').textContent = specialInstructions;
    } else {
        specialRow.style.display = 'none';
    }

    // 設定訂單狀態
    const statusMap = {
        '0': '<span class="status-badge status-paid">已付款</span>',
        '1': '<span class="status-badge status-shipped">已出貨</span>',
        '2': '<span class="status-badge status-completed">已完成</span>',
        '3': '<span class="status-badge status-cancelled">已取消</span>',
        '4': '<span class="status-badge" style="background: #ffc107; color: #333;">申請退貨中</span>',
        '5': '<span class="status-badge" style="background: #6c757d; color: white;">退貨完成</span>'
    };
    document.getElementById('detailOrderStatus').innerHTML = statusMap[orderStatus] || '<span class="status-badge">未知</span>';

    // 載入訂單商品項目
    loadOrderItems(orderId);

    // 開啟 Modal
    document.getElementById('orderDetailModal').classList.add('active');
}

// 關閉訂單詳情 Modal
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('active');
}

// 載入訂單商品項目
function loadOrderItems(orderId) {
    const container = document.getElementById('orderItemsContainer');
    container.innerHTML = '<div style="text-align: center; padding: 20px; color: #999;"><i class="fas fa-spinner fa-spin"></i> 載入中...</div>';

    fetch(`/seller/order/${orderId}/items`)
        .then(response => response.json())
        .then(items => {
            if (!items || items.length === 0) {
                container.innerHTML = '<div style="text-align: center; padding: 20px; color: #999;">沒有商品資料</div>';
                return;
            }

            let html = '';
            items.forEach(item => {
                html += `
                    <div style="display: flex; align-items: center; padding: 12px 0; border-bottom: 1px solid #f0f0f0;">
                        <img src="${item.productImage || ''}" alt="商品圖片" 
                             style="width: 60px; height: 60px; object-fit: cover; border-radius: 8px; margin-right: 12px;">
                        <div style="flex: 1;">
                            <div style="font-weight: 600; margin-bottom: 4px;">${item.productName || '商品 #' + item.proId}</div>
                            <div style="color: #999; font-size: 0.9rem;">
                                單價: $${formatNumber(item.proPrice)} x ${item.quantity}
                            </div>
                        </div>
                        <div style="text-align: right; font-weight: 600; color: var(--primary-color);">
                            $${formatNumber(item.subtotal)}
                        </div>
                    </div>
                `;
            });

            container.innerHTML = html;
        })
        .catch(error => {
            console.error('載入訂單商品失敗:', error);
            container.innerHTML = '<div style="text-align: center; padding: 20px; color: #dc3545;">載入失敗，請重試</div>';
        });
}

// 數字格式化（加入千分位）
function formatNumber(num) {
    if (num === null || num === undefined || num === '') return '0';
    return parseInt(num).toLocaleString();
}

// ==================== 事件監聽 ====================

// 點擊 Modal 背景關閉
document.addEventListener('DOMContentLoaded', function() {
    // 商品 Modal 點擊背景關閉
    const productModal = document.getElementById('productModal');
    if (productModal) {
        productModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeProductModal();
            }
        });
    }

    // 訂單詳情 Modal 點擊背景關閉
    const orderDetailModal = document.getElementById('orderDetailModal');
    if (orderDetailModal) {
        orderDetailModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeOrderDetailModal();
            }
        });
    }

    // ESC 鍵關閉 Modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (productModal && productModal.classList.contains('active')) {
                closeProductModal();
            }
            if (orderDetailModal && orderDetailModal.classList.contains('active')) {
                closeOrderDetailModal();
            }
        }
    });
});