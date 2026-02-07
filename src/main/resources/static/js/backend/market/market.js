// ==========================================
// 1. 分頁切換邏輯 (Tabs)
// ==========================================

// 對應 HTML 中的 onclick="switchTab('...')"
function switchTab(tabName) {
    const event = window.event;
    // 移除所有主分頁按鈕與面板的 active 狀態
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.panel').forEach(panel => panel.classList.remove('active'));

    // 設置當前點擊的按鈕與面板為 active
    if (event) {
        // 使用 closest 確保點擊到按鈕內的圖標(i標籤)也能正確抓到按鈕
        event.target.closest('.tab-btn').classList.add('active');
    }

    const targetPanel = document.getElementById('panel-' + tabName);
    if (targetPanel) {
        targetPanel.classList.add('active');
    }
}

// 對應 HTML 中的 onclick="switchOrderTab('...')"
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
// 3. 退貨詳情 Modal 相關 (Return)
// ==========================================

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '-';
    return dateTimeStr.replace('T', ' ').substring(0, 19);
}

// 顯示退貨詳情
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
                document.getElementById('modal-applyTime').textContent = formatDateTime(data.applyTime);
                document.getElementById('modal-returnReason').textContent = data.returnReason || '-';

                const imagesContainer = document.getElementById('modal-images-container');
                const imagesDiv = document.getElementById('modal-images');

                // 退貨圖片以 URL 路徑方式讀取（存放於 /images/return/ 內）
                if (data.hasImages && data.returnImages && data.returnImages.length > 0) {
                    imagesDiv.innerHTML = '';
                    data.returnImages.forEach(function(imageUrl, index) {
                        const img = document.createElement('img');
                        img.src = imageUrl;
                        img.alt = '退貨圖片 ' + (index + 1);
                        img.style.cssText = 'width: 150px; height: 150px; object-fit: cover; border-radius: 8px; border: 1px solid #ddd; cursor: pointer;';
                        img.onclick = function() {
                            window.open(imageUrl, '_blank');
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

// 對應 HTML 中的 onclick="closeModal()"
function closeModal() {
    const returnModal = document.getElementById('returnDetailModal');
    if (returnModal) {
        returnModal.classList.remove('active');
    }
}

// ==========================================
// 4. 初始化與點擊外部關閉邏輯
// ==========================================

document.addEventListener('DOMContentLoaded', function() {
    // 類別 Modal 外部點擊關閉
    const categoryModal = document.getElementById('categoryModal');
    if (categoryModal) {
        categoryModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeCategoryModal();
            }
        });
    }

    // 退貨 Modal 外部點擊關閉
    const returnModal = document.getElementById('returnDetailModal');
    if (returnModal) {
        returnModal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeModal();
            }
        });
    }
});