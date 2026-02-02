/**
 * PetGuardian - 賣家管理中心 JavaScript
 * 商品管理、訂單管理、營運概況
 */

// 全域變數
var existingImagesList = [];      // 現有圖片清單
var deleteImageIdsList = [];      // 待刪除圖片ID清單
var isEditMode = false;           // 是否為編輯模式

// ==================== 商品管理 ====================

// 開啟新增商品 Modal
function openProductModal() {
    document.getElementById('productModal').classList.add('active');
    document.getElementById('modalTitle').innerText = '新增商品';

    // 設定為新增模式
    isEditMode = false;

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

    // 設定為編輯模式
    isEditMode = true;

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
            existingImagesList = images || [];
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
            existingImagesList = [];
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
    var existingContainer = document.getElementById('existingImages');
    var files = input.files;

    console.log('previewNewImages 被呼叫');
    console.log('選擇的檔案數量:', files ? files.length : 0);
    console.log('是否為編輯模式:', isEditMode);

    if (!files || files.length === 0) {
        console.log('沒有選擇檔案');
        return;
    }

    // 只處理第一張圖片
    var file = files[0];

    if (!file.type.startsWith('image/')) {
        alert('請選擇圖片檔案');
        input.value = '';
        return;
    }

    // 檢查檔案大小（限制 10MB）
    if (file.size > 10 * 1024 * 1024) {
        alert('圖片檔案太大，請選擇小於 10MB 的圖片');
        input.value = '';
        return;
    }

    // 編輯模式下，如果有選擇新圖片，不管原本的 existingImagesList 狀態如何
    // 直接清空舊圖片的顯示區塊，並嘗試標記刪除
    if (isEditMode) {
        console.log('編輯模式且已選新圖：強制隱藏現有圖片，由後端執行全覆蓋');

        // 這裡我們還是盡量標記 ID，給後端參考 (雖然我們已經修改後端為強制的 Replace 模式)
        if (existingImagesList && existingImagesList.length > 0) {
            existingImagesList.forEach(function(img) {
                if (deleteImageIdsList.indexOf(img.productPicId) === -1) {
                    deleteImageIdsList.push(img.productPicId);
                }
            });
            updateDeleteImageInputs();
        }

        // 重要：視覺上直接清空舊圖
        existingImagesList = [];
        existingContainer.innerHTML = '';
    }

    // 清空現有預覽並建立新預覽
    container.innerHTML = '';

    var reader = new FileReader();
    reader.onload = function(e) {
        var imgWrapper = document.createElement('div');
        imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';

        var img = document.createElement('img');
        img.src = e.target.result;
        img.alt = '新圖片預覽';
        img.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 2px solid #51cf66;';

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

        console.log('預覽圖片已建立');
    };
    reader.readAsDataURL(file);
}

// ==================== 訂單管理 ====================

// 開啟訂單詳情 Modal
function openOrderDetailModal(button) {
    var orderId = button.getAttribute('data-order-id');
    var buyerName = button.getAttribute('data-buyer-name');
    var orderTotal = button.getAttribute('data-order-total');
    var orderTime = button.getAttribute('data-order-time');
    var orderStatus = button.getAttribute('data-order-status');
    var receiverName = button.getAttribute('data-receiver-name');
    var receiverPhone = button.getAttribute('data-receiver-phone');
    var receiverAddress = button.getAttribute('data-receiver-address');
    var specialInstructions = button.getAttribute('data-special-instructions');

    // 填入資料
    document.getElementById('detailOrderId').textContent = '#' + orderId;
    document.getElementById('detailBuyerName').textContent = buyerName || '-';
    document.getElementById('detailOrderTotal').textContent = '$' + Number(orderTotal).toLocaleString();
    document.getElementById('detailOrderTime').textContent = orderTime || '-';
    document.getElementById('detailReceiverName').textContent = receiverName || '-';
    document.getElementById('detailReceiverPhone').textContent = receiverPhone || '-';
    document.getElementById('detailReceiverAddress').textContent = receiverAddress || '-';
    document.getElementById('detailSpecialInstructions').textContent = specialInstructions || '無';

    // 設定訂單狀態標籤
    var statusBadge = document.getElementById('detailOrderStatus');
    statusBadge.textContent = getStatusText(orderStatus);
    statusBadge.className = 'badge ' + getStatusClass(orderStatus);

    // 開啟 Modal
    document.getElementById('orderDetailModal').classList.add('active');
}

// 關閉訂單詳情 Modal
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('active');
}

// 取得狀態文字
function getStatusText(status) {
    var statusMap = {
        '0': '待付款',
        '1': '已付款',
        '2': '處理中',
        '3': '已出貨',
        '4': '已完成',
        '5': '已取消',
        '6': '退款中',
        '7': '已退款'
    };
    return statusMap[status] || '未知';
}

// 取得狀態 CSS 類別
function getStatusClass(status) {
    var classMap = {
        '0': 'badge-warning',
        '1': 'badge-info',
        '2': 'badge-info',
        '3': 'badge-primary',
        '4': 'badge-success',
        '5': 'badge-secondary',
        '6': 'badge-warning',
        '7': 'badge-secondary'
    };
    return classMap[status] || 'badge-secondary';
}

// 訂單篩選
function filterOrders(status) {
    // 更新 Tab 樣式
    var tabs = document.querySelectorAll('.order-tab');
    tabs.forEach(function(tab) {
        tab.classList.remove('active');
    });
    event.target.classList.add('active');

    // 篩選訂單列表
    var rows = document.querySelectorAll('.order-row');
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