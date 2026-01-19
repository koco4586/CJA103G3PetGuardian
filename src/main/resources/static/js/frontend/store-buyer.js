/**
 * PetGuardian - Store Buyer Business Logic
 * 二手商城買家端業務邏輯
 *
 * 功能清單：
 * 1. 商品鎖定機制 (Lock-and-Buy)
 * 2. 加購視窗系統 (Upsell Modal)
 * 3. 金額計算與數據封裝
 * 4. Ajax 後端接口預留
 *
 * @author PetGuardian Frontend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置 (後端對接時修改此區域)
// ========================================

const API_ENDPOINTS = {
    // 商品鎖定 API (POST)
    // 請求參數: { productId: string }
    // 回應格式: { success: boolean, message: string, lockId?: string }
    LOCK_PRODUCT: '/api/products/{productId}/lock',

    // 獲取賣家其他商品 API (GET)
    // 請求參數: sellerId (URL參數)
    // 回應格式: { success: boolean, products: Array<Product> }
    FETCH_SELLER_PRODUCTS: '/api/products/seller/{sellerId}',

    // 建立訂單 API (POST)
    // 請求參數: { mainProductId: string, upsellProductIds: string[], totalAmount: number }
    // 回應格式: { success: boolean, orderId: string, redirectUrl?: string }
    // 建立訂單 API (POST)
    // 請求參數: { mainProductId: string, upsellProductIds: string[], totalAmount: number }
    // 回應格式: { success: boolean, orderId: string, redirectUrl?: string }
    CREATE_ORDER: '/api/orders/create',

    // 獲取賣家評價 API (GET)
    FETCH_SELLER_REVIEWS: '/api/seller-reviews/seller/{sellerId}',

    // 獲取賣家評價統計 API (GET)
    FETCH_SELLER_STATS: '/api/seller-reviews/seller/{sellerId}/stats',
};

// ========================================
// 全域狀態管理
// ========================================

const BuyerState = {
    // 當前鎖定的商品
    lockedProduct: null,

    // 加購清單
    selectedUpsells: [],

    // 總金額
    totalAmount: 0,

    // Modal 狀態
    isModalOpen: false,
};

// ========================================
// 核心功能：商品鎖定機制
// ========================================

/**
 * 處理購買按鈕點擊事件
 * @param {HTMLButtonElement} btnElement - 點擊的按鈕元素
 */
async function handleBuyClick(btnElement) {
    // 1. 獲取商品資訊
    const productId = btnElement.getAttribute('data-product-id');
    const sellerId = btnElement.getAttribute('data-seller-id');
    const productTitle = btnElement.getAttribute('data-product-title');
    const productPrice = btnElement.getAttribute('data-product-price');
    const productImg = btnElement.getAttribute('data-product-img');

    // 驗證必要資料
    if (!productId || !sellerId) {
        console.error('❌ 缺少必要資料：productId 或 sellerId');
        window.showToast('商品資料錯誤，請重新整理頁面');
        return;
    }

    // 2. 立即鎖定按鈕 UI
    lockButtonUI(btnElement, 'loading');

    try {
        // 3. 呼叫後端鎖定 API
        const lockResult = await lockProduct(productId);

        if (lockResult.success) {
            // 鎖定成功
            lockButtonUI(btnElement, 'success');

            // 儲存鎖定資訊
            BuyerState.lockedProduct = {
                productId,
                sellerId,
                title: productTitle,
                price: parseInt(productPrice),
                img: productImg,
                lockId: lockResult.lockId || null,
            };

            // 4. 延遲 500ms 後跳轉到 Bundle View 頁面
            setTimeout(() => {
                openBundleViewEnhanced(productId, sellerId, productTitle, productPrice, productImg);
            }, 500);

        } else {
            // 鎖定失敗 (已被其他人搶先)
            lockButtonUI(btnElement, 'error');
            window.showToast(lockResult.message || '商品已被其他買家搶先鎖定', 'error');

            // 3秒後恢復按鈕
            setTimeout(() => {
                resetButtonUI(btnElement);
            }, 3000);
        }

    } catch (error) {
        console.error('❌ 鎖定商品時發生錯誤:', error);
        lockButtonUI(btnElement, 'error');
        window.showToast('網路錯誤，請稍後再試', 'error');

        setTimeout(() => {
            resetButtonUI(btnElement);
        }, 3000);
    }
}

/**
 * 呼叫後端鎖定商品 API
 * @param {string} productId - 商品ID
 * @returns {Promise<{success: boolean, message: string, lockId?: string}>}
 */
async function lockProduct(productId) {
    // 替換 URL 中的 {productId}
    const url = API_ENDPOINTS.LOCK_PRODUCT.replace('{productId}', productId);

    // ⚠️ 開發階段：模擬 API 回應
    // 正式上線時，請取消註解下方的真實 fetch 邏輯

    // ----- 模擬 API (開發用) -----
    console.log(`🔒 [模擬] 鎖定商品 API: ${url}`);
    await delay(800); // 模擬網路延遲

    // 90% 成功率模擬
    const isSuccess = Math.random() > 0.1;

    return {
        success: isSuccess,
        message: isSuccess ? '商品鎖定成功' : '此商品剛被其他買家搶先鎖定',
        lockId: isSuccess ? `LOCK_${Date.now()}` : null,
    };

    // ----- 真實 API 呼叫 (上線時啟用) -----
    /*
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                // 如需 JWT Token，加入 Authorization header
                // 'Authorization': `Bearer ${getAuthToken()}`
            },
            body: JSON.stringify({ productId }),
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();
        return data;

    } catch (error) {
        console.error('API 錯誤:', error);
        throw error;
    }
    */
}

// ========================================
// UI 控制：按鈕狀態管理
// ========================================

/**
 * 鎖定按鈕 UI 狀態
 * @param {HTMLButtonElement} btn - 按鈕元素
 * @param {'loading'|'success'|'error'} state - 狀態
 */
function lockButtonUI(btn, state) {
    // 移除所有狀態 class
    btn.classList.remove('btn-locked', 'btn-locked-success', 'btn-locked-error');

    // 儲存原始文字
    if (!btn.hasAttribute('data-original-text')) {
        btn.setAttribute('data-original-text', btn.textContent);
    }

    switch (state) {
        case 'loading':
            btn.classList.add('btn-locked');
            btn.textContent = '處理中...';
            break;

        case 'success':
            btn.classList.add('btn-locked-success');
            btn.textContent = '已鎖定';
            break;

        case 'error':
            btn.classList.add('btn-locked-error');
            btn.textContent = '已售出';
            break;
    }

    btn.disabled = true;
}

/**
 * 恢復按鈕初始狀態
 * @param {HTMLButtonElement} btn - 按鈕元素
 */
function resetButtonUI(btn) {
    btn.classList.remove('btn-locked', 'btn-locked-success', 'btn-locked-error');
    btn.textContent = btn.getAttribute('data-original-text') || '購買';
    btn.disabled = false;
}

// ========================================
// Bundle View 增強功能
// ========================================

/**
 * 增強版 Bundle View（整合鎖定機制與動態載入）
 * @param {string} productId - 商品ID
 * @param {string} sellerId - 賣家ID
 * @param {string} title - 商品標題
 * @param {string} price - 商品價格
 * @param {string} img - 商品圖片
 */
async function openBundleViewEnhanced(productId, sellerId, title, price, img) {
    // 1. 切換到 Bundle View 頁面
    document.getElementById('store-views').style.display = 'none';
    document.getElementById('bundle-view').style.display = 'block';
    window.scrollTo(0, 0);

    // 2. 填入主商品資訊
    document.getElementById('bundle-main-title').innerText = title;
    document.getElementById('bundle-main-price').innerText = `$${parseInt(price).toLocaleString()}`;
    document.getElementById('bundle-main-img').src = img;
    document.getElementById('bundle-main-img').src = img;
    document.getElementById('bundle-seller-name').innerText = BuyerState.lockedProduct.title; // 暫時用標題 (需修正為賣家名稱，若有的話)

    // 載入賣家評價 (呼叫 store.html 定義的全域函數)
    if (typeof loadSellerReviews === 'function') {
        loadSellerReviews(sellerId);
    } else {
        console.warn('loadSellerReviews function not found');
    }

    // 3. 初始化狀態
    BuyerState.selectedUpsells = [];

    // 4. 顯示載入狀態
    const addonsContainer = document.querySelector('.addons-scroll-container');
    addonsContainer.innerHTML = `
        <div style="width: 100%; text-align: center; padding: 2rem;">
            <div style="width: 40px; height: 40px; border: 3px solid #e9ecef; border-top-color: var(--primary-color); border-radius: 50%; margin: 0 auto 1rem; animation: spinner-rotate 0.8s linear infinite;"></div>
            <p style="color: #666; margin: 0;">正在載入同賣家其他商品...</p>
        </div>
    `;

    // 5. 獲取賣家其他商品
    try {
        const products = await fetchSellerProducts(sellerId, productId);

        if (products.length === 0) {
            // 無其他商品
            addonsContainer.innerHTML = `
                <div style="width: 100%; text-align: center; padding: 2rem; color: #adb5bd;">
                    <i class="fas fa-box-open" style="font-size: 2.5rem; margin-bottom: 1rem; opacity: 0.5;"></i>
                    <p style="margin: 0;">此賣家目前沒有其他商品</p>
                    <p style="margin: 0.5rem 0 0 0; font-size: 0.9rem;">可直接前往結帳</p>
                </div>
            `;
        } else {
            // 渲染加購商品
            renderAddonsEnhanced(products);
        }

    } catch (error) {
        console.error('❌ 載入賣家商品失敗:', error);
        addonsContainer.innerHTML = `
            <div style="width: 100%; text-align: center; padding: 2rem;">
                <i class="fas fa-exclamation-triangle" style="font-size: 2.5rem; margin-bottom: 1rem; color: var(--danger);"></i>
                <p style="color: var(--danger); margin: 0;">載入失敗，請稍後再試</p>
            </div>
        `;
    }

    // 6. 更新結帳清單（初始只有主商品）
    updateBundleSummaryEnhanced();
}

/**
 * 渲染加購商品到現有的 addons-scroll-container
 * @param {Array} products - 商品陣列
 */
function renderAddonsEnhanced(products) {
    const container = document.querySelector('.addons-scroll-container');

    container.innerHTML = products.map(product => `
        <div class="card addon-card"
             data-product-id="${product.id}"
             data-price="${product.price}"
             data-title="${product.title}"
             onclick="toggleAddonEnhanced(this)">
            <img src="${product.img}"
                 style="height: 100px; width: 100%; object-fit: cover; border-radius: 8px;">
            <h4 style="font-size: 0.95rem; margin: 0.5rem 0 0.2rem; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                ${product.title}
            </h4>
            <p class="text-primary" style="font-weight: 700; font-size: 1rem;">$${product.price.toLocaleString()}</p>
            <div class="addon-check">
                <i class="fas fa-check"></i>
            </div>
        </div>
    `).join('');
}

/**
 * 增強版切換加購商品選取
 * @param {HTMLElement} card - 商品卡片元素
 */
function toggleAddonEnhanced(card) {
    const productId = card.getAttribute('data-product-id');
    const price = parseInt(card.getAttribute('data-price'));
    const title = card.getAttribute('data-title');
    const isSelected = card.style.borderColor === 'var(--primary-color)';

    if (isSelected) {
        // 取消選取
        card.style.borderColor = 'transparent';
        card.querySelector('.addon-check').style.display = 'none';
        BuyerState.selectedUpsells = BuyerState.selectedUpsells.filter(p => p.id !== productId);
    } else {
        // 加入選取
        card.style.borderColor = 'var(--primary-color)';
        card.querySelector('.addon-check').style.display = 'flex';
        BuyerState.selectedUpsells.push({
            id: productId,
            title: title,
            price: price,
        });
    }

    // 更新總金額
    updateBundleSummaryEnhanced();
}

/**
 * 增強版更新結帳清單
 */
function updateBundleSummaryEnhanced() {
    const listEl = document.getElementById('bundle-items-list');
    const mainProduct = BuyerState.lockedProduct;

    listEl.innerHTML = '';

    let total = mainProduct.price;

    // 主商品
    listEl.innerHTML += `
        <div class="d-flex justify-between mb-1" style="font-size:0.9rem;">
            <span><b>主商品：</b>${mainProduct.title}</span>
            <span>$${mainProduct.price.toLocaleString()}</span>
        </div>
    `;

    // 加購商品
    BuyerState.selectedUpsells.forEach(addon => {
        total += addon.price;
        listEl.innerHTML += `
            <div class="d-flex justify-between mb-1" style="font-size:0.9rem;">
                <span><b style="color:var(--primary-color);">+ 加購：</b>${addon.title}</span>
                <span>$${addon.price.toLocaleString()}</span>
            </div>
        `;
    });

    // 更新總計 (加上運費 60)
    const shippingFee = 60;
    const finalTotal = total + shippingFee;

    BuyerState.totalAmount = finalTotal;
    document.querySelector('.bundle-subtotal').innerText = '$' + total.toLocaleString();
    document.querySelector('.bundle-total').innerText = '$' + finalTotal.toLocaleString();
}

/**
 * 獲取賣家其他商品
 * @param {string} sellerId - 賣家ID
 * @param {string} excludeProductId - 要排除的商品ID (主商品)
 * @returns {Promise<Array>}
 */
async function fetchSellerProducts(sellerId, excludeProductId = null) {
    const url = API_ENDPOINTS.FETCH_SELLER_PRODUCTS.replace('{sellerId}', sellerId);

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();

        // 確保回傳的是陣列
        const products = data.data || [];

        // 排除主商品 (比對 ID)
        return products.filter(p => p.proId != excludeProductId && p.proState === 1).map(p => ({
            id: p.proId,
            title: p.proName,
            price: p.proPrice,
            img: `/images/products/${p.proId}.jpg` // 統一圖片路徑格式
        }));

    } catch (error) {
        console.error('API 錯誤:', error);
        throw error;
    }
}



// ========================================
// 結帳流程
// ========================================

/**
 * 前往結帳頁面（增強版，從 Bundle View 觸發）
 */
async function proceedToCheckoutEnhanced() {
    // 1. 檢查是否有鎖定的商品
    if (!BuyerState.lockedProduct) {
        window.showToast('請先選擇商品', 'error');
        return;
    }

    // 2. 封裝購物資訊
    const orderData = {
        mainProductId: BuyerState.lockedProduct.productId,
        mainProductTitle: BuyerState.lockedProduct.title,
        mainProductPrice: BuyerState.lockedProduct.price,
        mainProductImg: BuyerState.lockedProduct.img,
        sellerId: BuyerState.lockedProduct.sellerId,
        lockId: BuyerState.lockedProduct.lockId,
        upsellProductIds: BuyerState.selectedUpsells.map(p => p.id),
        upsellProducts: BuyerState.selectedUpsells,
        totalAmount: BuyerState.totalAmount,
        itemCount: 1 + BuyerState.selectedUpsells.length,
        timestamp: Date.now(),
    };

    console.log('📦 結帳資料:', orderData);

    // 3. 儲存至 SessionStorage (傳遞給結帳頁)
    sessionStorage.setItem('pendingOrder', JSON.stringify(orderData));

    // 4. 顯示處理中（找到 Bundle View 中的結帳按鈕）
    const checkoutBtn = document.querySelector('#bundle-view .btn-primary[onclick*="alert"]');
    if (checkoutBtn) {
        const originalText = checkoutBtn.textContent;
        checkoutBtn.textContent = '處理中...';
        checkoutBtn.disabled = true;

        try {
            // 5. (可選) 呼叫後端建立訂單草稿
            // const result = await createOrderDraft(orderData);

            // 6. 導向結帳頁面
            window.location.href = '/checkout';

        } catch (error) {
            console.error('❌ 建立訂單失敗:', error);
            window.showToast('系統錯誤，請稍後再試', 'error');
            checkoutBtn.textContent = originalText;
            checkoutBtn.disabled = false;
        }
    } else {
        // 直接跳轉（如果找不到按鈕）
        window.location.href = '/checkout';
    }
}

/**
 * 建立訂單草稿 (可選)
 * @param {Object} orderData - 訂單資料
 * @returns {Promise<{orderId: string}>}
 */
async function createOrderDraft(orderData) {
    const url = API_ENDPOINTS.CREATE_ORDER;

    // ----- 真實 API 呼叫 -----
    /*
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(orderData),
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();
        return data;

    } catch (error) {
        console.error('API 錯誤:', error);
        throw error;
    }
    */

    // 模擬回應
    await delay(500);
    return { orderId: `ORD_${Date.now()}` };
}

// ========================================
// 工具函數
// ========================================

/**
 * 延遲執行 (用於模擬 API)
 * @param {number} ms - 毫秒
 * @returns {Promise}
 */
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 獲取認證 Token (如需要)
 * @returns {string|null}
 */
function getAuthToken() {
    return localStorage.getItem('authToken') || null;
}

// ========================================
// 狀態管理
// ========================================

/**
 * 清除買家狀態（返回商品列表時呼叫）
 */
function clearBuyerState() {
    BuyerState.lockedProduct = null;
    BuyerState.selectedUpsells = [];
    BuyerState.totalAmount = 0;
    BuyerState.isModalOpen = false;

    console.log('🔓 買家狀態已清除');
}

// ========================================
// 全域函數暴露 (供 HTML onclick 使用)
// ========================================

window.handleBuyClick = handleBuyClick;
window.toggleAddonEnhanced = toggleAddonEnhanced;
window.proceedToCheckoutEnhanced = proceedToCheckoutEnhanced;
window.clearBuyerState = clearBuyerState;

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🛒 Store Buyer Module Initialized');

    // 檢查是否有待處理的訂單 (從結帳頁返回時清除)
    if (window.location.pathname.includes('/store')) {
        // sessionStorage.removeItem('pendingOrder'); // 可選：自動清除
    }
});
