// 全域變數
let existingImagesList = [];
let deleteImageIdsList = [];

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
                deleteBtn.style.cssText = `
                    position: absolute;
                    top: -8px;
                    right: -8px;
                    width: 24px;
                    height: 24px;
                    border-radius: 50%;
                    background: #dc3545;
                    color: white;
                    border: 2px solid white;
                    cursor: pointer;
                    font-size: 16px;
                    line-height: 1;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                `;
                deleteBtn.onclick = function() {
                    markImageForDeletion(img.productPicId);
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

// 標記圖片為待刪除
function markImageForDeletion(picId) {
    if (!confirm('確定要刪除此圖片嗎?')) {
        return;
    }

    deleteImageIdsList.push(picId);

    // 隱藏圖片
    const imgWrapper = document.querySelector(`[data-pic-id="${picId}"]`);
    if (imgWrapper) {
        imgWrapper.style.display = 'none';
    }

    // 更新隱藏輸入欄位
    updateDeleteImageInputs();
}

// 更新待刪除圖片的隱藏輸入欄位
function updateDeleteImageInputs() {
    const container = document.getElementById('deleteImageInputs');
    container.innerHTML = '';

    deleteImageIdsList.forEach(picId => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'deleteImageIds';
        input.value = picId;
        container.appendChild(input);
    });
}

// 預覽新上傳的圖片
function previewNewImages(input) {
    const container = document.getElementById('newImagePreview');
    const maxFiles = 5;

    // 計算現有圖片數量(扣除待刪除的)
    const currentImageCount = existingImagesList.length - deleteImageIdsList.length;
    const newFileCount = input.files.length;

    if (currentImageCount + newFileCount > maxFiles) {
        alert(`最多只能上傳 ${maxFiles} 張圖片`);
        input.value = '';
        return;
    }

    container.innerHTML = '';

    Array.from(input.files).forEach(file => {
        if (!file.type.match('image.*')) {
            return;
        }

        const reader = new FileReader();
        reader.onload = function(e) {
            const imgWrapper = document.createElement('div');
            imgWrapper.style.cssText = 'position: relative; width: 100px; height: 100px;';

            const img = document.createElement('img');
            img.src = e.target.result;
            img.alt = '新圖片預覽';
            img.style.cssText = 'width: 100%; height: 100%; object-fit: cover; border-radius: 8px;';

            imgWrapper.appendChild(img);
            container.appendChild(imgWrapper);
        };
        reader.readAsDataURL(file);
    });
}