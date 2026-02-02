/**
 * PetGuardian - 賣家管理中心 JavaScript
 * 商品管理、訂單管理、營運概況
 */

// 全域變數
var existingImagesList = [];
var deleteImageIdsList = [];

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
    var proId = button.getAttribute('data-pro-id');
    var proName = button.getAttribute('data-pro-name');
    var proTypeId = button.getAttribute('data-pro-type-id');
    var proPrice = button.getAttribute('data-pro-price');
    var proDescription = button.getAttribute('data-pro-description');
    var stockQuantity = button.getAttribute('data-stock-quantity');
    var proState = button.getAttribute('data-pro-state');

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
    fetch('/seller/product/' + proId + '/images')
        .then(function(response) {
            return response.json();
        })
        .then(function(images) {
            existingImagesList = images;
            var container = document.getElementById('existingImages');
            container.innerHTML = '';

            if (!images || images.length === 0) {
                console.log('此商品沒有現有圖片');
                return;
            }

            console.log('載入現有圖片數量:', images.length);

            images.forEach(function(img) {
                var imgWrapper = document.createElement('div');
                imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';
                imgWrapper.setAttribute('data-pic-id', img.productPicId);

                var imgElement = document.createElement('img');
                imgElement.src = img.imageBase64;
                imgElement.alt = '商品圖片';
                imgElement.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px;';

                var deleteBtn = document.createElement('button');
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
        .catch(function(error) {
            console.error('載入圖片失敗:', error);
        });
}

// 標記圖片待刪除
function markImageForDeletion(picId, wrapper) {
    // 加入刪除清單
    deleteImageIdsList.push(picId);
    console.log('標記刪除圖片 ID:', picId);

    // 從現有圖片清單中移除
    existingImagesList = existingImagesList.filter(function(img) {
        return img.productPicId !== picId;
    });

    // 移除預覽
    wrapper.remove();

    // 更新隱藏欄位
    updateDeleteImageInputs();
}

// 更新刪除圖片的隱藏欄位
function updateDeleteImageInputs() {
    var container = document.getElementById('deleteImageInputs');
    container.innerHTML = '';

    deleteImageIdsList.forEach(function(id) {
        var input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deleteImageIds';
        input.value = id;
        container.appendChild(input);
    });

    console.log('更新刪除清單，共 ' + deleteImageIdsList.length + ' 張待刪除');
}

// 預覽新上傳的圖片
function previewNewImages(input) {
    var container = document.getElementById('newImagePreview');
    var files = input.files;

    console.log('previewNewImages 被呼叫');
    console.log('選擇的檔案數量:', files ? files.length : 0);

    if (!files || files.length === 0) {
        console.log('沒有選擇檔案');
        return;
    }

    // 只取第一個檔案
    var file = files[0];
    console.log('處理檔案:', file.name);

    // 清空舊預覽
    container.innerHTML = '';

    // 讀取並顯示預覽
    var reader = new FileReader();
    reader.onload = function(e) {
        var imgWrapper = document.createElement('div');
        imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';

        var img = document.createElement('img');
        img.src = e.target.result;
        img.alt = '新圖片預覽';
        img.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 2px solid var(--primary-color);';

        var deleteBtn = document.createElement('button');
        deleteBtn.type = 'button';
        deleteBtn.innerHTML = '&times;';
        deleteBtn.style.cssText = 'position: absolute; top: -8px; right: -8px; background: #ff4d4f; color: white; border: none; border-radius: 50%; width: 24px; height: 24px; cursor: pointer; font-size: 14px; line-height: 1;';
        deleteBtn.onclick = function() {
            // 清空 file input 和預覽
            document.getElementById('productImages').value = '';
            container.innerHTML = '';
            console.log('已移除新圖片預覽');
        };

        imgWrapper.appendChild(img);
        imgWrapper.appendChild(deleteBtn);
        container.appendChild(imgWrapper);

        console.log('預覽圖片已建立，file input 仍保留檔案');
    };
    reader.readAsDataURL(file);
}

// ==================== 訂單管理 ====================

/**
 * 開啟訂單詳情 Modal
 * 從按鈕的 data 屬性讀取訂單資料並顯示在 Modal 中
 * 所有資料皆來自資料庫
 */
function openOrderDetailModal(button) {
    // 從按鈕的 data 屬性取得訂單資料（資料來源為資料庫）
    var orderId = button.getAttribute('data-order-id');
    var buyerName = button.getAttribute('data-buyer-name');
    var orderTotal = button.getAttribute('data-order-total');
    var orderTime = button.getAttribute('data-order-time');
    var orderStatus = button.getAttribute('data-order-status');
    var receiverName = button.getAttribute('data-receiver-name');
    var receiverPhone = button.getAttribute('data-receiver-phone');
    var receiverAddress = button.getAttribute('data-receiver-address');
    var specialInstructions = button.getAttribute('data-special-instructions');

    // 填入訂單編號
    document.getElementById('detailOrderId').textContent = '#' + orderId;

    // 填入買家姓名（來自 member 表的 mem_name）
    document.getElementById('detailBuyerName').textContent = buyerName || '-';

    // 填入訂單金額（來自 orders 表的 order_total）
    document.getElementById('detailOrderTotal').textContent = '$' + Number(orderTotal).toLocaleString();

    // 填入下單時間（來自 orders 表的 order_time）
    document.getElementById('detailOrderTime').textContent = orderTime || '-';

    // 填入收件人姓名（來自 orders 表的 receiver_name）
    document.getElementById('detailReceiverName').textContent = receiverName || '-';

    // 填入收件人電話（來自 orders 表的 receiver_phone）
    document.getElementById('detailReceiverPhone').textContent = receiverPhone || '-';

    // 填入收件人地址（來自 orders 表的 receiver_address）
    document.getElementById('detailReceiverAddress').textContent = receiverAddress || '-';

    // 訂單狀態對應表（對應資料庫 order_status 欄位）
    // 0:已付款 1:已出貨 2:已完成 3:已取消 4:申請退貨中 5:退貨完成
    var statusMap = {
        '0': '<span class="badge badge-warning">已付款</span>',
        '1': '<span class="badge badge-info">已出貨</span>',
        '2': '<span class="badge badge-success">已完成</span>',
        '3': '<span class="badge badge-secondary">已取消</span>',
        '4': '<span class="badge" style="background: #ffc107; color: #333;">申請退貨中</span>',
        '5': '<span class="badge" style="background: #6c757d; color: white;">退貨完成</span>'
    };

    // 填入訂單狀態（來自 orders 表的 order_status）
    var statusHtml = statusMap[String(orderStatus)] || '<span class="badge">未知狀態</span>';
    document.getElementById('detailOrderStatus').innerHTML = statusHtml;

    // 處理訂單備註（來自 orders 表的 special_instructions）
    var specialRow = document.getElementById('detailSpecialRow');
    if (specialInstructions && specialInstructions.trim() && specialInstructions !== 'null') {
        document.getElementById('detailSpecialInstructions').textContent = specialInstructions;
        specialRow.style.display = 'flex';
    } else {
        specialRow.style.display = 'none';
    }

    // 載入訂單商品項目（從資料庫動態讀取）
    loadOrderItems(orderId);

    // 顯示 Modal
    document.getElementById('orderDetailModal').classList.add('active');
}

/**
 * 載入訂單商品項目
 * 透過 AJAX 從後端取得訂單的商品明細
 */
function loadOrderItems(orderId) {
    var container = document.getElementById('orderItemsContainer');
    container.innerHTML = '<div style="text-align: center; padding: 20px; color: #999;"><i class="fas fa-spinner fa-spin"></i> 載入中...</div>';

    fetch('/seller/order/' + orderId + '/items')
        .then(function(response) {
            return response.json();
        })
        .then(function(items) {
            if (!items || items.length === 0) {
                container.innerHTML = '<div style="text-align: center; padding: 20px; color: #999;">沒有商品項目</div>';
                return;
            }

            var html = '';
            items.forEach(function(item) {
                html += '<div style="display: flex; gap: 12px; padding: 12px; background: white; border-radius: 8px; margin-bottom: 8px; align-items: center;">';

                // 商品圖片
                if (item.productImage) {
                    html += '<img src="' + item.productImage + '" alt="商品圖片" style="width: 60px; height: 60px; object-fit: cover; border-radius: 6px;">';
                } else {
                    html += '<div style="width: 60px; height: 60px; background: #f0f0f0; border-radius: 6px; display: flex; align-items: center; justify-content: center; color: #ccc;"><i class="fas fa-image"></i></div>';
                }

                // 商品資訊
                html += '<div style="flex: 1;">';
                html += '<div style="font-weight: 600; margin-bottom: 4px;">' + (item.productName || '商品 #' + item.proId) + '</div>';
                html += '<div style="font-size: 0.85rem; color: #666;">單價: $' + Number(item.proPrice).toLocaleString() + ' x ' + item.quantity + '</div>';
                html += '</div>';

                // 小計
                html += '<div style="font-weight: 600; color: var(--primary-color);">$' + Number(item.subtotal).toLocaleString() + '</div>';
                html += '</div>';
            });

            container.innerHTML = html;
        })
        .catch(function(error) {
            console.error('載入訂單商品失敗:', error);
            container.innerHTML = '<div style="text-align: center; padding: 20px; color: #dc3545;"><i class="fas fa-exclamation-circle"></i> 載入失敗</div>';
        });
}

// 關閉訂單詳情 Modal
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('active');
}

/**
 * 切換訂單狀態篩選
 * 根據選擇的狀態篩選訂單列表
 */
function filterOrders(status, btn) {
    // 更新按鈕狀態
    document.querySelectorAll('.sub-tabs .sub-tab').forEach(function(t) {
        t.classList.remove('active');
    });
    btn.classList.add('active');

    // 篩選訂單
    var rows = document.querySelectorAll('#ordersTable tbody tr');
    rows.forEach(function(row) {
        var orderStatus = row.getAttribute('data-status');
        if (status === 'all' || orderStatus === status) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

// 確認出貨
function confirmShipment(orderId) {
    if (confirm('確定要標記此訂單為已出貨嗎？')) {
        document.getElementById('shipForm_' + orderId).submit();
    }
}

// 取消訂單
function confirmCancel(orderId) {
    if (confirm('確定要取消此訂單嗎？買家將會收到退款。')) {
        document.getElementById('cancelForm_' + orderId).submit();
    }
}

// ==================== 評價管理 ====================

function toggleReviewsList() {
    var reviewsList = document.getElementById('reviewsList');
    if (reviewsList) {
        if (reviewsList.style.display === 'none' || reviewsList.style.display === '') {
            reviewsList.style.display = 'block';
            reviewsList.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } else {
            reviewsList.style.display = 'none';
        }
    }
}

// 檢舉評價 Modal
function openReportModal(reviewId) {
    document.getElementById('reportReviewId').value = reviewId;
    document.getElementById('reportReason').value = '';
    document.getElementById('reportModal').classList.add('active');
}

function closeReportModal() {
    document.getElementById('reportModal').classList.remove('active');
}

// ==================== 頁面初始化 ====================

document.addEventListener('DOMContentLoaded', function() {
    console.log('賣家管理中心 JS 已載入');

    // 點擊 Modal 外部關閉
    document.querySelectorAll('.modal').forEach(function(modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });

    // ESC 鍵關閉 Modal
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal.active').forEach(function(modal) {
                modal.classList.remove('active');
            });
        }
    });
});