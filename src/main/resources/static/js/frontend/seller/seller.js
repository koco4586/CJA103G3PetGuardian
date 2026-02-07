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
    document.getElementById('imagePreview').innerHTML = '';
    document.getElementById('deleteImageInputs').innerHTML = '';

    // 清空 file input
    var fileInput = document.getElementById('productImage');
    if (fileInput) {
        fileInput.value = '';
    }
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
    document.getElementById('imagePreview').innerHTML = '';

    // 清空 file input
    var fileInput = document.getElementById('productImage');
    if (fileInput) {
        fileInput.value = '';
    }

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
                // 使用 imageUrl 載入圖片，加時間戳防止快取
                imgElement.src = img.imageUrl + '?t=' + Date.now();
                imgElement.alt = '商品圖片';
                imgElement.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px;';
                // 圖片載入失敗時顯示預設圖
                imgElement.onerror = function() {
                    this.src = '/images/default-product.png';
                };

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
    deleteImageIdsList.push(picId);
    console.log('標記刪除圖片 ID:', picId);

    existingImagesList = existingImagesList.filter(function(img) {
        return img.productPicId !== picId;
    });

    wrapper.remove();
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

// 使用者選擇圖片檔案後的預覽處理
function previewNewImages(input) {
    var file = input.files[0];
    var container = document.getElementById('imagePreview');
    container.innerHTML = '';

    if (!file) {
        return;
    }

    // 檢查檔案類型
    var validTypes = ['image/jpeg', 'image/png'];
    if (validTypes.indexOf(file.type) === -1) {
        alert('只支援 JPG、PNG 格式的圖片');
        input.value = '';
        return;
    }

    // 檢查檔案大小（限制 5MB）
    var maxSize = 5 * 1024 * 1024;
    if (file.size > maxSize) {
        alert('圖片大小不能超過 5MB');
        input.value = '';
        return;
    }

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
            input.value = '';
            container.innerHTML = '';
        };

        imgWrapper.appendChild(img);
        imgWrapper.appendChild(deleteBtn);
        container.appendChild(imgWrapper);
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

    document.getElementById('detailOrderId').innerText = '#' + orderId;
    document.getElementById('detailBuyerName').innerText = buyerName || '-';
    document.getElementById('detailOrderTotal').innerText = '$' + (orderTotal || '0');
    document.getElementById('detailOrderTime').innerText = orderTime || '-';

    var statusElement = document.getElementById('detailOrderStatus');
    var statusMap = {
        '0': { text: '已付款', className: 'status-paid' },
        '1': { text: '已出貨', className: 'status-shipped' },
        '2': { text: '已完成', className: 'status-completed' },
        '3': { text: '已取消', className: 'status-cancelled' },
        '4': { text: '退貨中', className: 'status-paid' },
        '5': { text: '已退貨', className: 'status-cancelled' },
        '6': { text: '已撥款', className: 'status-completed' }
    };

    var statusInfo = statusMap[orderStatus] || { text: '未知', className: '' };
    statusElement.innerText = statusInfo.text;
    statusElement.className = 'status-badge ' + statusInfo.className;

    document.getElementById('detailReceiverName').innerText = receiverName || '-';
    document.getElementById('detailReceiverPhone').innerText = receiverPhone || '-';
    document.getElementById('detailReceiverAddress').innerText = receiverAddress || '-';
    document.getElementById('detailSpecialInstructions').innerText = specialInstructions || '無';

    loadOrderItems(orderId);

    // 根據狀態決定是否載入退貨資訊
    var returnSection = document.getElementById('returnInfoSection');
    if (orderStatus === '4' || orderStatus === '5') {
        loadReturnInfo(orderId);
    } else {
        if (returnSection) {
            returnSection.style.display = 'none';
        }
    }

    document.getElementById('orderDetailModal').classList.add('active');
}

// 載入訂單商品明細
function loadOrderItems(orderId) {
    var container = document.getElementById('orderItemsContainer');
    if (!container) return;

    container.innerHTML = '<p style="color: #999; text-align: center;">載入中...</p>';

    fetch('/seller/order/' + orderId + '/items')
        .then(function(response) {
            return response.json();
        })
        .then(function(items) {
            container.innerHTML = '';

            if (!items || items.length === 0) {
                container.innerHTML = '<p style="color: #999; text-align: center;">無商品明細</p>';
                return;
            }

            items.forEach(function(item) {
                var itemDiv = document.createElement('div');
                itemDiv.style.cssText = 'display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f0f0f0;';

                var img = document.createElement('img');
                img.src = item.productImage || '/images/default-product.png';
                img.alt = item.productName;
                img.style.cssText = 'width: 50px; height: 50px; object-fit: cover; border-radius: 8px;';
                img.onerror = function() {
                    this.src = '/images/default-product.png';
                };

                var info = document.createElement('div');
                info.style.cssText = 'flex: 1;';
                info.innerHTML = '<div style="font-weight: 600;">' + item.productName + '</div>'
                    + '<div style="color: #999; font-size: 0.85rem;">$' + item.proPrice + ' x ' + item.quantity + '</div>';

                var subtotal = document.createElement('div');
                subtotal.style.cssText = 'font-weight: 700; color: var(--primary-color);';
                subtotal.innerText = '$' + item.subtotal;

                itemDiv.appendChild(img);
                itemDiv.appendChild(info);
                itemDiv.appendChild(subtotal);
                container.appendChild(itemDiv);
            });
        })
        .catch(function(error) {
            console.error('載入商品明細失敗:', error);
            container.innerHTML = '<p style="color: #ff4d4f; text-align: center;">載入失敗</p>';
        });
}

// 載入退貨資訊
function loadReturnInfo(orderId) {
    var section = document.getElementById('returnInfoSection');
    if (!section) return;

    fetch('/seller/order/' + orderId + '/return-info')
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (!data || !data.success || !data.hasReturnOrder) {
                section.style.display = 'none';
                return;
            }

            section.style.display = 'block';

            // 填入申請時間
            var applyTimeEl = document.getElementById('detailReturnApplyTime');
            if (applyTimeEl) {
                var timeStr = data.applyTime || '-';
                if (timeStr && timeStr.indexOf('T') > -1) {
                    timeStr = timeStr.replace('T', ' ').substring(0, 19);
                }
                applyTimeEl.innerText = timeStr;
            }

            // 填入退款金額
            var refundAmountEl = document.getElementById('detailRefundAmount');
            if (refundAmountEl) {
                refundAmountEl.innerText = '$' + (data.refundAmount || 0);
            }

            // 填入退款原因
            var returnReasonEl = document.getElementById('detailReturnReason');
            if (returnReasonEl) {
                returnReasonEl.innerText = data.returnReason || '-';
            }

            // 顯示退貨圖片
            var imagesContainer = document.getElementById('returnImagesContainer');
            var imagesDiv = document.getElementById('returnImagesDiv');
            if (imagesContainer && imagesDiv) {
                imagesDiv.innerHTML = '';
                if (data.returnImages && data.returnImages.length > 0) {
                    imagesContainer.style.display = 'block';
                    data.returnImages.forEach(function(imageUrl, index) {
                        var img = document.createElement('img');
                        img.src = imageUrl;
                        img.alt = '退貨圖片 ' + (index + 1);
                        img.style.cssText = 'width: 150px; height: 150px; object-fit: cover; border-radius: 8px; border: 1px solid #ddd; cursor: pointer;';
                        img.onclick = function() {
                            window.open(imageUrl, '_blank');
                        };
                        imagesDiv.appendChild(img);
                    });
                } else {
                    imagesContainer.style.display = 'none';
                }
            }
        })
        .catch(function(error) {
            console.error('載入退貨資訊失敗:', error);
            section.style.display = 'none';
        });
}

// 關閉訂單詳情 Modal
function closeOrderDetailModal() {
    document.getElementById('orderDetailModal').classList.remove('active');
}

// 訂單狀態篩選
function filterOrders(status, btn) {
    document.querySelectorAll('.sub-tabs .sub-tab').forEach(function(t) {
        t.classList.remove('active');
    });
    btn.classList.add('active');

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

    document.querySelectorAll('.modal').forEach(function(modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal.active').forEach(function(modal) {
                modal.classList.remove('active');
            });
        }
    });
});