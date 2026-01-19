/**
 * PetGuardian - Checkout Page Logic
 * 結帳頁面業務邏輯
 *
 * @author PetGuardian Frontend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const CHECKOUT_API = {
    // 建立訂單 API
    CREATE_ORDER: '/api/orders/create',

    // 付款處理 API
    PROCESS_PAYMENT: '/api/payments/process',
};

// ========================================
// 全域狀態
// ========================================

let orderData = null;
let formData = {
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    specialInstructions: '',
    paymentMethod: 0,
};

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🛒 Checkout Page Initialized');

    // 1. 從 sessionStorage 讀取訂單資料
    loadOrderData();

    // 2. 渲染訂單摘要
    renderOrderSummary();

    // 3. 綁定表單事件
    bindFormEvents();

    // 4. 載入使用者預設資料（如果已登入）
    loadUserDefaultData();
});

// ========================================
// 載入訂單資料
// ========================================

/**
 * 從 sessionStorage 載入訂單資料
 */
function loadOrderData() {
    const pendingOrder = sessionStorage.getItem('pendingOrder');

    if (!pendingOrder) {
        // 無訂單資料，導回商品頁面
        alert('找不到訂單資料，請重新選購商品');
        window.location.href = '/store';
        return;
    }

    try {
        orderData = JSON.parse(pendingOrder);
        console.log('📦 載入訂單資料:', orderData);

        // 驗證必要欄位
        if (!orderData.mainProductId || !orderData.sellerId) {
            throw new Error('訂單資料不完整');
        }

    } catch (error) {
        console.error('❌ 訂單資料解析失敗:', error);
        alert('訂單資料錯誤，請重新選購商品');
        window.location.href = '/store';
    }
}

// ========================================
// 渲染訂單摘要
// ========================================

/**
 * 渲染訂單摘要（桌面版與手機版）
 */
function renderOrderSummary() {
    if (!orderData) return;

    // 計算運費（固定 $60）
    const shipping = 60;
    const subtotal = orderData.totalAmount;
    const total = subtotal + shipping;

    // 渲染桌面版
    renderOrderItems('order-items-list');
    document.getElementById('desktop-subtotal').textContent = `$${subtotal.toLocaleString()}`;
    document.getElementById('desktop-shipping').textContent = `$${shipping.toLocaleString()}`;
    document.getElementById('desktop-total').textContent = `$${total.toLocaleString()}`;

    // 渲染手機版
    renderOrderItems('mobile-order-items');
    document.getElementById('mobile-subtotal').textContent = `$${subtotal.toLocaleString()}`;
    document.getElementById('mobile-shipping').textContent = `$${shipping.toLocaleString()}`;
    document.getElementById('mobile-total').textContent = `$${total.toLocaleString()}`;
}

/**
 * 渲染訂單商品列表
 * @param {string} containerId - 容器 ID
 */
function renderOrderItems(containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;

    let html = '';

    // 主商品
    html += `
        <div class="order-item-row">
            <img src="${orderData.mainProductImg}" alt="${orderData.mainProductTitle}" class="order-item-img">
            <div class="order-item-info">
                <div>
                    <span class="order-item-badge">主商品</span>
                    <div class="order-item-title">${orderData.mainProductTitle}</div>
                </div>
                <div class="order-item-price">$${orderData.mainProductPrice.toLocaleString()}</div>
            </div>
        </div>
    `;

    // 加購商品
    if (orderData.upsellProducts && orderData.upsellProducts.length > 0) {
        orderData.upsellProducts.forEach(item => {
            html += `
                <div class="order-item-row">
                    <div class="order-item-img" style="background: var(--secondary-color); display: flex; align-items: center; justify-content: center;">
                        <i class="fas fa-box" style="color: var(--primary-color); font-size: 1.5rem;"></i>
                    </div>
                    <div class="order-item-info">
                        <div>
                            <span class="order-item-badge addon">加購</span>
                            <div class="order-item-title">${item.title}</div>
                        </div>
                        <div class="order-item-price">$${item.price.toLocaleString()}</div>
                    </div>
                </div>
            `;
        });
    }

    container.innerHTML = html;
}

// ========================================
// 表單事件綁定
// ========================================

/**
 * 綁定表單事件
 */
function bindFormEvents() {
    // 表單輸入即時驗證
    const form = document.getElementById('checkout-form');
    const inputs = form.querySelectorAll('input, textarea');

    inputs.forEach(input => {
        input.addEventListener('blur', () => validateField(input));
        input.addEventListener('input', () => {
            if (input.classList.contains('error')) {
                validateField(input);
            }
        });
    });

    // 付款方式選擇
    const paymentRadios = document.querySelectorAll('input[name="paymentMethod"]');
    paymentRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            formData.paymentMethod = parseInt(e.target.value);
            console.log('付款方式:', formData.paymentMethod === 0 ? '信用卡' : '行動支付');
        });
    });
}

/**
 * 驗證單一欄位
 * @param {HTMLElement} input - 輸入元素
 * @returns {boolean} 是否有效
 */
function validateField(input) {
    const name = input.name;
    const value = input.value.trim();
    let isValid = true;
    let errorMsg = '';

    // 必填欄位檢查
    if (input.hasAttribute('required') && !value) {
        isValid = false;
        errorMsg = '此欄位為必填';
    }

    // 特定欄位驗證
    switch (name) {
        case 'receiverName':
            if (value && value.length < 2) {
                isValid = false;
                errorMsg = '姓名至少需要 2 個字';
            }
            break;

        case 'receiverPhone':
            const phonePattern = /^[0-9]{10}$/;
            if (value && !phonePattern.test(value)) {
                isValid = false;
                errorMsg = '請輸入有效的 10 碼電話號碼';
            }
            break;

        case 'receiverAddress':
            if (value && value.length < 10) {
                isValid = false;
                errorMsg = '請輸入完整地址（至少 10 個字）';
            }
            break;
    }

    // 更新 UI
    const errorElement = document.getElementById(`error-${name}`);
    if (errorElement) {
        if (isValid) {
            input.classList.remove('error');
            errorElement.textContent = '';
        } else {
            input.classList.add('error');
            errorElement.textContent = errorMsg;
        }
    }

    return isValid;
}

/**
 * 驗證整個表單
 * @returns {boolean} 表單是否有效
 */
function validateForm() {
    const form = document.getElementById('checkout-form');
    const inputs = form.querySelectorAll('input[required], textarea[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!validateField(input)) {
            isValid = false;
        }
    });


    return isValid;
}

// ========================================
// 載入使用者預設資料
// ========================================

/**
 * 載入已登入使用者的預設資料
 */
function loadUserDefaultData() {
    // TODO: 從後端 API 獲取使用者資料
    // 目前為示範，從 localStorage 讀取（如果有的話）

    const savedData = localStorage.getItem('userDefaultAddress');
    if (savedData) {
        try {
            const data = JSON.parse(savedData);
            document.getElementById('receiverName').value = data.name || '';
            document.getElementById('receiverPhone').value = data.phone || '';
            document.getElementById('receiverAddress').value = data.address || '';
        } catch (error) {
            console.error('載入預設資料失敗:', error);
        }
    }
}

// ========================================
// 提交訂單
// ========================================

/**
 * 提交訂單
 */
async function submitOrder() {
    console.log('📝 準備提交訂單...');

    // 1. 驗證表單
    if (!validateForm()) {
        return;
    }

    // 2. 收集表單資料
    formData.receiverName = document.getElementById('receiverName').value.trim();
    formData.receiverPhone = document.getElementById('receiverPhone').value.trim();
    formData.receiverAddress = document.getElementById('receiverAddress').value.trim();
    formData.specialInstructions = document.getElementById('specialInstructions').value.trim();

    // 3. 組合完整訂單資料
    const shipping = 60;
    const finalOrderData = {
        // 從 sessionStorage 來的資料
        mainProductId: orderData.mainProductId,
        sellerId: orderData.sellerId,
        lockId: orderData.lockId,
        upsellProductIds: orderData.upsellProductIds || [],

        // 計算金額
        orderTotal: orderData.totalAmount + shipping,

        // 表單資料
        receiverName: formData.receiverName,
        receiverPhone: formData.receiverPhone,
        receiverAddress: formData.receiverAddress,
        specialInstructions: formData.specialInstructions,
        paymentMethod: formData.paymentMethod,

        // 其他資訊
        timestamp: Date.now(),
    };

    console.log('📦 完整訂單資料:', finalOrderData);

    // 4. 顯示載入狀態
    const submitBtn = document.getElementById('submit-order-btn');
    const originalText = submitBtn.innerHTML;
    submitBtn.classList.add('btn-loading');
    submitBtn.textContent = '處理中...';
    submitBtn.disabled = true;

    try {
        // 5. 呼叫後端 API
        const result = await createOrder(finalOrderData);

        if (result.success) {
            // 6. 成功：清除 sessionStorage
            sessionStorage.removeItem('pendingOrder');

            // 7. 儲存訂單 ID 用於確認頁
            sessionStorage.setItem('completedOrderId', result.orderId);

            // 8. 跳轉到訂單完成頁面
            console.log('✅ 訂單建立成功！');
            window.location.href = `/order-complete?orderId=${result.orderId}`;

        } else {
            throw new Error(result.message || '訂單建立失敗');
        }

    } catch (error) {
        console.error('❌ 提交訂單失敗:', error);
        alert(error.message || '系統錯誤，請稍後再試');

        // 恢復按鈕
        submitBtn.classList.remove('btn-loading');
        submitBtn.innerHTML = originalText;
        submitBtn.disabled = false;
    }
}

/**
 * 呼叫後端建立訂單 API
 * @param {Object} orderData - 訂單資料
 * @returns {Promise<{success: boolean, orderId: string, message?: string}>}
 */
async function createOrder(orderData) {
    const url = '/api/orders';

    // 獲取當前登入會員 ID
    const currentMemId = parseInt(localStorage.getItem('currentMemId') || '1002');

    // 準備符合後端格式的請求資料
    const requestData = {
        order: {
            buyerMemId: currentMemId,
            sellerMemId: parseInt(orderData.sellerId),
            paymentMethod: orderData.paymentMethod,
            receiverName: orderData.receiverName,
            receiverPhone: orderData.receiverPhone,
            receiverAddress: orderData.receiverAddress,
            specialInstructions: orderData.specialInstructions || '',
        },
        orderItems: []
    };

    // 組合訂單項目 (主商品 + 加購商品)
    // 主商品
    requestData.orderItems.push({
        proId: parseInt(orderData.mainProductId),
        proPrice: orderData.mainProductPrice || 0,
        quantity: 1
    });

    // 加購商品
    if (orderData.upsellProductIds && orderData.upsellProductIds.length > 0) {
        orderData.upsellProductIds.forEach((proId, index) => {
            const upsellProduct = orderData.upsellProducts?.[index];
            requestData.orderItems.push({
                proId: parseInt(proId),
                proPrice: upsellProduct?.price || 0,
                quantity: 1
            });
        });
    }

    console.log('📤 呼叫建立訂單 API:', url);
    console.log('📦 請求資料:', requestData);

    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(requestData),
        });

        if (!response.ok) {
            // 嘗試解析錯誤訊息
            let errorMessage = `HTTP Error: ${response.status}`;
            try {
                const errorData = await response.json();
                if (errorData.message) {
                    errorMessage = errorData.message;
                }
            } catch (e) {
                // 無法解析 JSON，使用預設訊息
            }
            throw new Error(errorMessage);
        }

        const result = await response.json();
        console.log('📥 API 回應:', result);

        if (result.success) {
            return {
                success: true,
                orderId: result.data.order?.orderId || result.data.orderId,
                message: result.message || '訂單建立成功',
            };
        } else {
            throw new Error(result.message || '訂單建立失敗');
        }

    } catch (error) {
        console.error('❌ API 錯誤:', error);
        throw error;
    }
}

// ========================================
// 返回商品頁面
// ========================================

/**
 * 返回商品頁面
 */
function backToStore() {
    if (confirm('確定要返回商品頁面嗎？訂單資料將會清除。')) {
        // 清除訂單資料
        sessionStorage.removeItem('pendingOrder');
        window.location.href = '/store';
    }
}

// ========================================
// 工具函數
// ========================================

/**
 * 延遲執行
 * @param {number} ms - 毫秒
 * @returns {Promise}
 */
function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * 獲取認證 Token
 * @returns {string|null}
 */
function getAuthToken() {
    return localStorage.getItem('authToken') || null;
}

// ========================================
// 全域函數暴露
// ========================================

window.submitOrder = submitOrder;
window.backToStore = backToStore;
