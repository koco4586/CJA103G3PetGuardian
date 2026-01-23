function openProductModal() {
    document.getElementById('modalTitle').innerText = '新增商品';
    document.getElementById('productForm').reset();
    document.getElementById('proId').value = '';
    document.getElementById('existingImages').innerHTML = '';
    document.getElementById('newImagePreview').innerHTML = '';
    document.getElementById('deleteImageInputs').innerHTML = '';
    document.getElementById('productModal').classList.add('active');
}

// 開啟編輯商品 Modal
function openEditProductModal(button) {
    document.getElementById('modalTitle').innerText = '編輯商品';

    // 清空表單和預覽區
    document.getElementById('productForm').reset();
    document.getElementById('existingImages').innerHTML = '';
    document.getElementById('newImagePreview').innerHTML = '';
    document.getElementById('deleteImageInputs').innerHTML = '';

    // 從按鈕的 data 屬性取得商品資料
    const proId = button.dataset.proId;
    const proName = button.dataset.proName || '';
    const proTypeId = button.dataset.proTypeId || '';
    const proPrice = button.dataset.proPrice || '';
    const proDescription = button.dataset.proDescription || '';
    const stockQuantity = button.dataset.stockQuantity || 0;
    const proState = button.dataset.proState || 1;

    // 填入表單
    document.getElementById('proId').value = proId;
    document.getElementById('proName').value = proName;
    document.getElementById('proTypeId').value = proTypeId;
    document.getElementById('proPrice').value = proPrice;
    document.getElementById('proDescription').value = proDescription;
    document.getElementById('stockQuantity').value = stockQuantity;
    document.getElementById('proState').value = proState;

    // 載入現有圖片
    loadExistingImages(proId);

    document.getElementById('productModal').classList.add('active');
}

// 載入商品現有圖片
function loadExistingImages(proId) {
    const container = document.getElementById('existingImages');
    container.innerHTML = '<div style="color: #999; font-size: 0.85rem;"><i class="fas fa-spinner fa-spin"></i> 載入圖片中...</div>';

    fetch('/seller/product/' + proId + '/images')
        .then(response => response.json())
        .then(images => {
            container.innerHTML = '';
            if (images && images.length > 0) {
                images.forEach(img => {
                    const div = document.createElement('div');
                    div.style.cssText = 'position: relative; width: 80px; height: 80px;';
                    div.id = 'existing-pic-' + img.productPicId;
                    div.innerHTML = `
                            <img src="${img.imageBase64}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 1px solid #eee;">
                            <button type="button" onclick="markImageForDelete(${img.productPicId})"
                                    style="position: absolute; top: -8px; right: -8px; width: 22px; height: 22px;
                                           border-radius: 50%; background: #dc3545; color: white; border: 2px solid white;
                                           cursor: pointer; font-size: 12px; display: flex;
                                           align-items: center; justify-content: center; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                                <i class="fas fa-times"></i>
                            </button>
                        `;
                    container.appendChild(div);
                });
            }
        })
        .catch(err => {
            container.innerHTML = '<div style="color: #999; font-size: 0.85rem;">無圖片</div>';
        });
}

// 標記圖片為待刪除
function markImageForDelete(picId) {
    // 移除預覽
    const picDiv = document.getElementById('existing-pic-' + picId);
    if (picDiv) {
        picDiv.remove();
    }
    // 新增隱藏欄位
    const input = document.createElement('input');
    input.type = 'hidden';
    input.name = 'deleteImageIds';
    input.value = picId;
    document.getElementById('deleteImageInputs').appendChild(input);
}

// 預覽新上傳的圖片
function previewNewImages(input) {
    const container = document.getElementById('newImagePreview');

    if (input.files) {
        Array.from(input.files).forEach((file, index) => {
            // 檢查檔案大小
            if (file.size > 5 * 1024 * 1024) {
                alert('檔案 "' + file.name + '" 超過 5MB 限制');
                return;
            }

            const reader = new FileReader();
            reader.onload = function(e) {
                const div = document.createElement('div');
                div.style.cssText = 'position: relative; width: 80px; height: 80px;';
                div.innerHTML = `
                        <img src="${e.target.result}" style="width: 100%; height: 100%; object-fit: cover; border-radius: 8px; border: 1px solid #eee;">
                        <button type="button" onclick="this.parentElement.remove()"
                                style="position: absolute; top: -8px; right: -8px; width: 22px; height: 22px;
                                       border-radius: 50%; background: #dc3545; color: white; border: 2px solid white;
                                       cursor: pointer; font-size: 12px; display: flex;
                                       align-items: center; justify-content: center; box-shadow: 0 2px 4px rgba(0,0,0,0.2);">
                            <i class="fas fa-times"></i>
                        </button>
                    `;
                container.appendChild(div);
            };
            reader.readAsDataURL(file);
        });
    }
}

// 關閉商品 Modal
function closeProductModal() {
    document.getElementById('productModal').classList.remove('active');
}

// 點擊 Modal 背景關閉
document.getElementById('productModal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeProductModal();
    }
});

// 展開/收合評價列表
function toggleReviewsList() {
    const reviewsList = document.getElementById('reviewsList');
    if (reviewsList.style.display === 'none') {
        reviewsList.style.display = 'block';
    } else {
        reviewsList.style.display = 'none';
    }
}