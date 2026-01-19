/**
 * PetGuardian - Order Complete Page Logic
 * 訂單完成頁面業務邏輯
 *
 * @author PetGuardian Frontend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const ORDER_API = {
    // 查詢訂單詳情 API
    GET_ORDER_DETAILS: '/api/orders/{orderId}',
};

// ========================================
// 訂單狀態對應
// ========================================

const ORDER_STATUS_MAP = {
    0: { text: '已付款', class: 'status-paid' },
    1: { text: '已出貨', class: 'status-shipped' },
    2: { text: '已完成', class: 'status-completed' },
    3: { text: '已取消', class: 'status-cancelled' },
    4: { text: '申請退貨中', class: 'status-cancelled' },
    5: { text: '退貨完成', class: 'status-cancelled' },
};

const PAYMENT_METHOD_MAP = {
    0: '信用卡付款',
    1: '行動支付',
};

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('✅ Order Complete Page Initialized');

    // 1. 從 URL 參數或 sessionStorage 獲取訂單 ID
    const orderId = getOrderId();

    if (!orderId) {
        alert('找不到訂單資訊');
        window.location.href = '/store';
        return;
    }

    // 2. 載入訂單詳情
    loadOrderDetails(orderId);
});

// ========================================
// 獲取訂單 ID
// ========================================

/**
 * 從 URL 參數或 sessionStorage 獲取訂單 ID
 * @returns {string|null} 訂單 ID
 */
function getOrderId() {
    // 優先從 URL 參數讀取
    const urlParams = new URLSearchParams(window.location.search);
    let orderId = urlParams.get('orderId');

    // 如果 URL 沒有，從 sessionStorage 讀取
    if (!orderId) {
        orderId = sessionStorage.getItem('completedOrderId');
    }

    return orderId;
}

// ========================================
// 載入訂單詳情
// ========================================

/**
 * 載入並顯示訂單詳情
 * @param {string} orderId - 訂單 ID
 */
async function loadOrderDetails(orderId) {
    try {
        console.log(`📦 載入訂單詳情: ${orderId}`);

        // 呼叫後端 API 獲取訂單資料
        const orderData = await fetchOrderDetails(orderId);

        if (!orderData) {
            throw new Error('無法獲取訂單資料');
        }

        // 渲染訂單資訊
        renderOrderInfo(orderData);
        renderOrderItems(orderData.orderItems);
        renderReceiverInfo(orderData);

    } catch (error) {
        console.error('❌ 載入訂單失敗:', error);
        alert('載入訂單資料失敗，請稍後再試');
        window.location.href = '/store';
    }
}

/**
 * 呼叫後端 API 獲取訂單詳情
 * @param {string} orderId - 訂單 ID
 * @returns {Promise<Object>} 訂單資料
 */
async function fetchOrderDetails(orderId) {
    const url = `/api/orders/${orderId}`;

    console.log(`📤 呼叫查詢訂單 API: ${url}`);

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

        const result = await response.json();
        console.log('📥 API 回應:', result);

        if (result.success) {
            // 後端回應格式：{ success, data: { order, orderItems } }
            const orderData = result.data.order || result.data;
            const orderItems = result.data.orderItems || [];

            // 組合完整訂單資料
            return {
                ...orderData,
                orderItems: orderItems
            };
        } else {
            throw new Error(result.message || '獲取訂單失敗');
        }

    } catch (error) {
        console.error('❌ API 錯誤:', error);
        throw error;
    }
}

// ========================================
// 渲染訂單資訊
// ========================================

/**
 * 渲染訂單基本資訊
 * @param {Object} orderData - 訂單資料
 */
function renderOrderInfo(orderData) {
    // 訂單編號
    document.getElementById('orderId').textContent = orderData.orderId;

    // 訂單時間
    const orderTime = new Date(orderData.orderTime);
    const formattedTime = formatDateTime(orderTime);
    document.getElementById('orderTime').textContent = formattedTime;

    // 訂單狀態
    const statusInfo = ORDER_STATUS_MAP[orderData.orderStatus] || { text: '未知狀態', class: '' };
    const statusElement = document.getElementById('orderStatus');
    statusElement.textContent = statusInfo.text;
    statusElement.className = `status-badge ${statusInfo.class}`;

    // 付款方式
    const paymentMethod = PAYMENT_METHOD_MAP[orderData.paymentMethod] || '未知';
    document.getElementById('paymentMethod').textContent = paymentMethod;

    // 訂單金額
    document.getElementById('orderTotal').textContent = `$${orderData.orderTotal.toLocaleString()}`;
}

/**
 * 渲染訂單商品列表
 * @param {Array} orderItems - 訂單商品陣列
 */
function renderOrderItems(orderItems) {
    const container = document.getElementById('orderItemsList');

    if (!orderItems || orderItems.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #999;">無商品資料</p>';
        return;
    }

    let html = '';

    orderItems.forEach(item => {
        const subtotal = item.proPrice * item.quantity;

        html += `
            <div class="order-item">
                <img src="${item.productImg}" alt="${item.productTitle}" class="order-item-img">
                <div class="order-item-details">
                    <div class="order-item-title">${item.productTitle}</div>
                    <div class="order-item-meta">
                        <span class="item-quantity">數量：${item.quantity}</span>
                        <span class="item-price">$${subtotal.toLocaleString()}</span>
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

/**
 * 渲染收件人資訊
 * @param {Object} orderData - 訂單資料
 */
function renderReceiverInfo(orderData) {
    document.getElementById('receiverName').textContent = orderData.receiverName;
    document.getElementById('receiverPhone').textContent = orderData.receiverPhone;
    document.getElementById('receiverAddress').textContent = orderData.receiverAddress;

    // 特殊備註（選填）
    if (orderData.specialInstructions && orderData.specialInstructions.trim()) {
        document.getElementById('specialInstructions').textContent = orderData.specialInstructions;
        document.getElementById('specialInstructionsRow').style.display = 'flex';
    }
}

// ========================================
// 查看訂單詳情
// ========================================

/**
 * 跳轉到訂單詳情頁面（會員中心）
 */
function viewOrderDetails() {
    const orderId = getOrderId();

    if (!orderId) {
        alert('找不到訂單資訊');
        return;
    }

    // 跳轉到會員中心的訂單詳情頁面
    console.log(`🔗 查看訂單詳情: ${orderId}`);

    // 儲存訂單 ID 到 sessionStorage，供 dashboard-orders.html 使用
    sessionStorage.setItem('viewOrderId', orderId);

    // 導向會員中心訂單頁面
    window.location.href = '/dashboard/orders';
}

// ========================================
// 工具函數
// ========================================

/**
 * 格式化日期時間
 * @param {Date} date - 日期物件
 * @returns {string} 格式化後的日期時間字串
 */
function formatDateTime(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}/${month}/${day} ${hours}:${minutes}`;
}

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

window.viewOrderDetails = viewOrderDetails;
