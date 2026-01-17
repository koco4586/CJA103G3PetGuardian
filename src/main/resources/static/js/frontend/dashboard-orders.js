/**
 * PetGuardian - Dashboard Orders Page Logic
 * 會員訂單管理頁面業務邏輯
 *
 * @author PetGuardian Frontend Team
 * @version 2.0.0
 */

// ========================================
// API 端點配置
// ========================================

const ORDERS_API = {
    // 查詢買家所有訂單
    GET_BUYER_ORDERS: '/api/orders/buyer/{buyerMemId}',

    // 查詢單筆訂單詳情
    GET_ORDER_DETAILS: '/api/orders/{orderId}',

    // 更新訂單狀態
    UPDATE_ORDER_STATUS: '/api/orders/{orderId}/status',

    // 申請退貨
    APPLY_RETURN: '/api/returns',

    // 查詢退貨單
    GET_RETURN_DETAILS: '/api/returns/order/{orderId}',

    // 查詢買家所有退貨單
    GET_BUYER_RETURNS: '/api/returns/buyer/{buyerMemId}',

    // 新增評價
    CREATE_REVIEW: '/api/seller-reviews',

    // 檢查訂單是否已評價
    CHECK_REVIEWED: '/api/seller-reviews/order/{orderId}/check',

    // 查詢買家所有評價
    GET_BUYER_REVIEWS: '/api/seller-reviews/buyer/{buyerMemId}',
};

// ========================================
// 訂單狀態與退貨狀態對應
// ========================================

const ORDER_STATUS_MAP = {
    0: { text: '已付款', badge: 'badge-warning', allowCancel: true, allowReturn: false },
    1: { text: '已出貨', badge: 'badge-primary', allowCancel: false, allowReturn: false },
    2: { text: '已完成', badge: 'badge-success', allowCancel: false, allowReturn: true },
    3: { text: '已取消', badge: 'badge-secondary', allowCancel: false, allowReturn: false },
    4: { text: '申請退貨中', badge: 'badge-danger', allowCancel: false, allowReturn: false },
    5: { text: '退貨完成', badge: 'badge-secondary', allowCancel: false, allowReturn: false },
};

const RETURN_STATUS_MAP = {
    0: { text: '審核中', badge: 'badge-warning', icon: 'fa-clock' },
    1: { text: '退貨通過', badge: 'badge-success', icon: 'fa-check-circle' },
    2: { text: '退貨失敗', badge: 'badge-danger', icon: 'fa-times-circle' },
};

const PAYMENT_METHOD_MAP = {
    0: '信用卡付款',
    1: '行動支付',
};

const SHIPPING_FEE = 60;

// ========================================
// 全域狀態
// ========================================

let currentBuyerMemId = null;
let allOrders = [];
let allReturns = [];
let allReviews = [];
let currentFilter = 'all';
let currentOrderForReturn = null;
let currentRating = 0;

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('📦 Dashboard Orders Page Initialized');

    // 從 session 或 API 獲取當前登入的會員 ID
    // 預設為 1002 (測試用保險起見，與 checkout.js 一致)
    currentBuyerMemId = parseInt(localStorage.getItem('currentMemId') || '1002');

    // 載入訂單資料
    loadOrders();

    // 載入退貨資料
    loadReturns();

    // 載入評價資料
    loadReviews();
});

// ========================================
// 載入評價資料
// ========================================

/**
 * 載入買家所有評價
 */
async function loadReviews() {
    try {
        console.log(`📦 載入買家評價: buyerMemId=${currentBuyerMemId}`);
        const reviews = await fetchBuyerReviews(currentBuyerMemId);
        allReviews = reviews;
        // 如果評價載入比訂單慢，可能需要重新渲染訂單以更新按鈕狀態
        if (allOrders.length > 0) {
            renderOrders(allOrders);
        }
    } catch (error) {
        console.error('❌ 載入評價失敗:', error);
    }
}

/**
 * 呼叫後端 API 獲取買家評價
 * @param {number} buyerMemId
 */
async function fetchBuyerReviews(buyerMemId) {
    const url = ORDERS_API.GET_BUYER_REVIEWS.replace('{buyerMemId}', buyerMemId);
    const response = await fetch(url);
    if (!response.ok) throw new Error(`HTTP Error: ${response.status}`);
    const result = await response.json();
    return result.success ? (result.data || []) : [];
}

// ========================================
// 載入訂單資料
// ========================================

/**
 * 載入買家所有訂單
 */
async function loadOrders() {
    try {
        console.log(`📦 載入買家訂單: buyerMemId=${currentBuyerMemId}`);

        const orders = await fetchBuyerOrders(currentBuyerMemId);
        allOrders = orders;

        renderOrders(orders);

    } catch (error) {
        console.error('❌ 載入訂單失敗:', error);
        alert('載入訂單資料失敗，請稍後再試');
    }
}

/**
 * 呼叫後端 API 獲取買家訂單
 * @param {number} buyerMemId - 買家會員ID
 * @returns {Promise<Array>} 訂單陣列
 */
async function fetchBuyerOrders(buyerMemId) {
    const url = ORDERS_API.GET_BUYER_ORDERS.replace('{buyerMemId}', buyerMemId);

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        // 後端返回格式: [{order: {...}, orderItems: [...], itemCount: n}, ...]
        // 前端需要的格式: [{orderId, orderStatus, orderItems, ...}, ...]
        const orders = (result.data || []).map(item => {
            const order = item.order;
            return {
                orderId: order.orderId,
                buyerMemId: order.buyerMemId,
                sellerMemId: order.sellerMemId,
                orderTime: order.orderTime,
                orderTotal: order.orderTotal,
                paymentMethod: order.paymentMethod,
                orderStatus: order.orderStatus,
                receiverName: order.receiverName,
                receiverPhone: order.receiverPhone,
                receiverAddress: order.receiverAddress,
                specialInstructions: order.specialInstructions,
                orderItems: item.orderItems || []
            };
        });
        return orders;
    } else {
        throw new Error(result.message || '獲取訂單失敗');
    }
}

// ========================================
// 渲染訂單列表
// ========================================

/**
 * 渲染訂單列表 - 緊湊版
 * @param {Array} orders - 訂單陣列
 */
/**
 * 渲染訂單列表 - 緊湊版 (含期限與詳情邏輯)
 * @param {Array} orders - 訂單陣列
 */
function renderOrders(orders) {
    const container = document.getElementById('orders-list');
    const countElement = document.getElementById('order-count');

    if (!orders || orders.length === 0) {
        container.innerHTML = `
            <div style="text-align: center; padding: 2rem; color: #999;">
                <i class="fas fa-inbox" style="font-size: 2.5rem; margin-bottom: 0.75rem; display: block;"></i>
                <p>尚無訂單記錄</p>
            </div>
        `;
        countElement.textContent = '共 0 筆訂單';
        return;
    }

    countElement.textContent = `共 ${orders.length} 筆訂單`;
    let html = '';

    const now = new Date();

    orders.forEach(order => {
        const statusInfo = ORDER_STATUS_MAP[order.orderStatus] || { text: '未知狀態', badge: 'badge-secondary' };
        const orderTime = new Date(order.orderTime);
        const orderTimeStr = formatDateTime(order.orderTime);

        // 計算時間差 (小時)
        const diffHours = (now - orderTime) / (1000 * 60 * 60);

        // 期限邏輯 (1天 = 24小時)
        const isWithinCancelPeriod = diffHours <= 24;
        const isWithinReturnPeriod = diffHours <= 24; // 暫時使用訂單時間+24h作為退貨期限 (依需求)

        // 計算總商品數量（主商品 + 加購商品）
        const totalItems = order.orderItems.length;
        const mainProduct = order.orderItems[0]; // 顯示第一個商品

        // 查詢關聯資訊
        const returnOrder = allReturns.find(r => r.orderId === order.orderId);
        const review = allReviews.find(r => r.orderId === order.orderId);

        // 按鈕顯示邏輯
        const showCancelBtn = order.orderStatus === 0 && isWithinCancelPeriod;
        // 退貨按鈕：狀態已完成 + 無退貨紀錄 + 在期限內 (如果已退貨則不顯示退貨紐)
        const showReturnBtn = order.orderStatus === 2 && !returnOrder && isWithinReturnPeriod;
        // 評價按鈕：狀態已完成 + 無評價紀錄 + 無退貨紀錄
        const showReviewBtn = order.orderStatus === 2 && !review && !returnOrder;

        html += `
            <div class="order-item" data-order-id="${order.orderId}" data-status="${order.orderStatus}">
                <!-- 訂單元資訊列（左上角） -->
                <div class="order-header">
                    <div class="order-meta-info">
                        <span class="order-id">#${order.orderId}</span>
                        <span class="order-time">
                            <i class="far fa-clock"></i> ${orderTimeStr}
                        </span>
                        ${totalItems > 1 ? `<span class="order-item-count-badge">共${totalItems}件</span>` : ''}
                    </div>
                    <div class="order-status-badges">
                        <span class="badge ${statusInfo.badge}">${statusInfo.text}</span>
                        ${returnOrder ? `<span class="badge ${RETURN_STATUS_MAP[returnOrder.returnStatus].badge}">
                            ${RETURN_STATUS_MAP[returnOrder.returnStatus].text}
                        </span>` : ''}
                        ${review ? `<span class="badge badge-success"><i class="fas fa-check"></i> 已評價</span>` : ''}
                    </div>
                </div>

                <!-- 訂單主體內容列 -->
                <div class="order-main-content">
                    <!-- 商品區 -->
                    <div class="order-product-section">
                        <img src="${mainProduct.productImg}" alt="${mainProduct.productTitle}" class="order-product-img">
                        <div class="order-product-details">
                            <div class="order-product-title">${mainProduct.productTitle}</div>
                            <div class="order-product-meta">
                                <span>x${mainProduct.quantity}</span>
                                <span class="divider">|</span>
                                <span>$${mainProduct.proPrice.toLocaleString()}</span>
                            </div>
                        </div>
                    </div>

                    <!-- 訂單金額 -->
                    <div class="order-total">
                        <span class="order-total-label">訂單金額</span>
                        <span class="order-total-value">$${order.orderTotal.toLocaleString()}</span>
                    </div>

                    <!-- 操作按鈕區 -->
                    <div class="order-actions-section">
                        ${order.orderStatus === 1 ? `
                            <button class="btn btn-primary btn-sm" onclick="confirmReceipt(${order.orderId})">
                                <i class="fas fa-check"></i> 確認收貨
                            </button>
                        ` : ''}

                        ${showReviewBtn ? `
                            <button class="btn btn-outline btn-sm" onclick="openReviewModal(${order.orderId})">
                                <i class="fas fa-star"></i> 評價
                            </button>
                        ` : ''}
                        
                        ${showReturnBtn ? `
                            <button class="btn btn-outline btn-sm btn-danger-outline" onclick="openReturnModal(${order.orderId})">
                                <i class="fas fa-undo"></i> 退貨
                            </button>
                        ` : ''}

                        ${showCancelBtn ? `
                            <button class="btn btn-outline btn-sm btn-danger-outline" onclick="openCancelOrderModal(${order.orderId})">
                                <i class="fas fa-times"></i> 取消訂單
                            </button>
                        ` : ''}

                        <button class="btn btn-outline btn-sm" onclick="toggleOrderDetails(${order.orderId})">
                            <i class="fas fa-chevron-down"></i> 詳情
                        </button>
                    </div>
                </div>

                <!-- 訂單詳情（摺疊區域）-->
                <div class="order-details-collapse" id="order-details-${order.orderId}" style="display: none;">
                    <div class="order-details-content">
                        
                        <!-- 擴充資訊區塊 (退貨/評價/取消) -->
                        ${returnOrder ? `
                        <div class="detail-section warning-bg">
                            <h5 style="color: var(--danger);">
                                <i class="fas fa-undo"></i> 退貨詳情
                            </h5>
                            <div class="receiver-info-detail">
                                <div><strong>狀態：</strong>${RETURN_STATUS_MAP[returnOrder.returnStatus].text}</div>
                                <div><strong>原因：</strong>${returnOrder.returnReason}</div>
                                <div><strong>退款金額：</strong>$${returnOrder.refundAmount.toLocaleString()}</div>
                                <div style="font-size: 0.85rem; color: #666; margin-top: 0.5rem;">申請時間: ${formatDateTimeFull(returnOrder.applyTime)}</div>
                            </div>
                        </div>
                        ` : ''}

                        ${review ? `
                        <div class="detail-section success-bg">
                            <h5 style="color: var(--success);">
                                <i class="fas fa-star"></i> 您的評價
                            </h5>
                            <div class="receiver-info-detail">
                                <div style="color: #ffd43b; font-size: 1.1rem; margin-bottom: 0.25rem;">
                                    ${'<i class="fas fa-star"></i>'.repeat(review.rating)}${'<i class="far fa-star"></i>'.repeat(5 - review.rating)}
                                </div>
                                <div style="font-style: italic;">"${review.reviewContent}"</div>
                                <div style="font-size: 0.85rem; color: #666; margin-top: 0.5rem;">評價時間: ${formatDateTimeFull(review.createdTime)}</div>
                            </div>
                        </div>
                        ` : ''}

                        ${order.orderStatus === 3 ? `
                        <div class="detail-section gray-bg">
                            <h5 style="color: #666;">
                                <i class="fas fa-info-circle"></i> 取消詳情
                            </h5>
                             <div class="receiver-info-detail">
                                <div>訂單已於 ${formatDateTimeFull(order.orderTime)} 之後取消。</div>
                                <div>若已付款，款項 ($${Math.max(0, order.orderTotal - SHIPPING_FEE).toLocaleString()}) 將於 3-5 個工作天內退回 (不含運費)。</div>
                            </div>
                        </div>
                        ` : ''}

                        ${totalItems > 1 ? `
                        <!-- 所有商品明細 -->
                        <div class="detail-section">
                            <h5>
                                <i class="fas fa-shopping-bag"></i>
                                商品明細（${totalItems}件）
                            </h5>
                            <div class="order-items-list">
                                ${order.orderItems.map(item => `
                                    <div class="order-detail-item">
                                        <img src="${item.productImg}" alt="${item.productTitle}">
                                        <div class="item-info">
                                            <div class="item-title">${item.productTitle}</div>
                                            <div class="item-meta">x${item.quantity} · $${item.proPrice.toLocaleString()}</div>
                                        </div>
                                        <div class="item-subtotal">$${(item.proPrice * item.quantity).toLocaleString()}</div>
                                    </div>
                                `).join('')}
                            </div>
                        </div>
                        ` : ''}

                        <!-- 收件人資訊 -->
                        <div class="detail-section" style="margin-bottom: 0;">
                            <h5>
                                <i class="fas fa-truck"></i>
                                收件資訊
                            </h5>
                            <div class="receiver-info-detail">
                                <div><strong>收件人：</strong>${order.receiverName}</div>
                                <div><strong>電話：</strong>${order.receiverPhone}</div>
                                <div><strong>地址：</strong>${order.receiverAddress}</div>
                                ${order.specialInstructions ? `<div><strong>備註：</strong>${order.specialInstructions}</div>` : ''}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

// ========================================
// 訂單篩選
// ========================================

/**
 * 篩選訂單
 * @param {string} status - 篩選狀態 (all/processing/delivered/cancelled)
 * @param {HTMLElement} btnElement - 按鈕元素
 */
function filterOrders(status, btnElement) {
    currentFilter = status;

    // 更新按鈕樣式
    document.querySelectorAll('#filter-buttons .btn').forEach(btn => {
        btn.classList.remove('active');
    });
    btnElement.classList.add('active');

    // 篩選訂單
    let filtered = allOrders;

    if (status === 'processing') {
        // 進行中：已付款(0) + 已出貨(1)
        filtered = allOrders.filter(order => order.orderStatus === 0 || order.orderStatus === 1);
    } else if (status === 'delivered') {
        // 已送達：已完成(2)
        filtered = allOrders.filter(order => order.orderStatus === 2);
    } else if (status === 'cancelled') {
        // 已取消：已取消(3) + 申請退貨中(4) + 退貨完成(5)
        filtered = allOrders.filter(order => order.orderStatus === 3 || order.orderStatus === 4 || order.orderStatus === 5);
    }

    renderOrders(filtered);
}

// ========================================
// 訂單詳情展開/收合
// ========================================

/**
 * 切換訂單詳情顯示
 * @param {number} orderId - 訂單ID
 */
function toggleOrderDetails(orderId) {
    const detailsElement = document.getElementById(`order-details-${orderId}`);
    const button = event.target.closest('button');
    const icon = button.querySelector('i');

    if (detailsElement.style.display === 'none') {
        detailsElement.style.display = 'block';
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-up');
        button.innerHTML = '<i class="fas fa-chevron-up"></i> 收合詳情';
    } else {
        detailsElement.style.display = 'none';
        icon.classList.remove('fa-chevron-up');
        icon.classList.add('fa-chevron-down');
        button.innerHTML = '<i class="fas fa-chevron-down"></i> 查看詳情';
    }
}

// ========================================
// 載入退貨資料
// ========================================

/**
 * 載入買家所有退貨單
 */
async function loadReturns() {
    try {
        console.log(`📦 載入買家退貨單: buyerMemId=${currentBuyerMemId}`);

        const returns = await fetchBuyerReturns(currentBuyerMemId);
        allReturns = returns;

    } catch (error) {
        console.error('❌ 載入退貨單失敗:', error);
    }
}

/**
 * 呼叫後端 API 獲取買家退貨單
 * @param {number} buyerMemId - 買家會員ID
 * @returns {Promise<Array>} 退貨單陣列
 */
async function fetchBuyerReturns(buyerMemId) {
    const url = ORDERS_API.GET_BUYER_RETURNS.replace('{buyerMemId}', buyerMemId);

    const response = await fetch(url, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result.data || [];
    } else {
        throw new Error(result.message || '獲取退貨單失敗');
    }
}

// ========================================
// 確認收貨
// ========================================

/**
 * 確認收貨
 * @param {number} orderId - 訂單ID
 */
async function confirmReceipt(orderId) {
    if (!confirm('確認已收到商品嗎？確認後訂單將完成。')) {
        return;
    }

    try {
        console.log(`✅ 確認收貨: orderId=${orderId}`);

        // 呼叫後端 API
        await confirmReceiptAPI(orderId);

        alert('已確認收貨！');

        // 重新載入訂單
        await loadOrders();

    } catch (error) {
        console.error('❌ 確認收貨失敗:', error);
        alert('確認收貨失敗，請稍後再試');
    }
}

/**
 * 呼叫後端確認收貨 API
 * @param {number} orderId - 訂單ID
 */
async function confirmReceiptAPI(orderId) {
    const url = ORDERS_API.UPDATE_ORDER_STATUS.replace('{orderId}', orderId);

    const response = await fetch(url, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: 2 }) // 2 = 已完成
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result;
    } else {
        throw new Error(result.message || '確認收貨失敗');
    }
}

// ========================================
// 取消訂單
// ========================================

let orderToCancel = null;

/**
 * 開啟取消訂單確認視窗
 * @param {number} orderId - 訂單ID
 */
function openCancelOrderModal(orderId) {
    orderToCancel = orderId;
    const order = allOrders.find(o => o.orderId === orderId);

    document.getElementById('cancelOrderId').textContent = `#${orderId}`;

    // 計算退款金額 (不含運費)
    const refundAmount = Math.max(0, order.orderTotal - SHIPPING_FEE);

    // 更新提示訊息
    const noticeBox = document.querySelector('#cancelOrderModal .notice-box');
    if (noticeBox) {
        noticeBox.innerHTML = `
            <i class="fas fa-exclamation-triangle"></i> 
            訂單取消後將退款 <strong>$${refundAmount.toLocaleString()}</strong> (不含運費 $${SHIPPING_FEE})。<br>
            款項預計 3-5 個工作天退回原付款帳戶。
        `;
    }

    document.getElementById('cancelOrderModal').style.display = 'flex';
}

/**
 * 關閉取消訂單視窗
 */
function closeCancelOrderModal() {
    document.getElementById('cancelOrderModal').style.display = 'none';
    orderToCancel = null;
}

/**
 * 確認取消訂單
 */
async function confirmCancelOrder() {
    if (!orderToCancel) return;

    try {
        console.log(`❌ 取消訂單: orderId=${orderToCancel}`);

        // 呼叫後端 API
        await cancelOrderAPI(orderToCancel);

        alert(`訂單 #${orderToCancel} 已取消成功`);
        closeCancelOrderModal();

        // 重新載入訂單
        await loadOrders();

    } catch (error) {
        console.error('❌ 取消訂單失敗:', error);
        alert('取消訂單失敗，請稍後再試');
    }
}

/**
 * 呼叫後端取消訂單 API
 * @param {number} orderId - 訂單ID
 */
async function cancelOrderAPI(orderId) {
    const url = ORDERS_API.UPDATE_ORDER_STATUS.replace('{orderId}', orderId);

    const response = await fetch(url, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ status: 3 }) // 3 = 已取消
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result;
    } else {
        throw new Error(result.message || '取消訂單失敗');
    }
}

// ========================================
// 申請退貨
// ========================================

/**
 * 開啟申請退貨視窗
 * @param {number} orderId - 訂單ID
 */
function openReturnModal(orderId) {
    const order = allOrders.find(o => o.orderId === orderId);
    if (!order) return;

    currentOrderForReturn = order;

    // 填充訂單資訊
    document.getElementById('returnOrderId').textContent = `#${order.orderId}`;

    // 退款金額不包含運費
    const refundAmount = Math.max(0, order.orderTotal - SHIPPING_FEE);
    document.getElementById('returnRefundAmount').textContent = `$${refundAmount.toLocaleString()} (不含運費)`;

    // 清空表單
    document.getElementById('returnReasonSelect').value = '';
    document.getElementById('returnReasonText').value = '';
    document.getElementById('returnImages').value = '';
    updateImagePreview();

    // 顯示視窗
    document.getElementById('returnModal').style.display = 'flex';
}

/**
 * 關閉申請退貨視窗
 */
function closeReturnModal() {
    document.getElementById('returnModal').style.display = 'none';
    currentOrderForReturn = null;
}

/**
 * 提交退貨申請
 * @param {Event} e - 表單提交事件
 */
async function submitReturn(e) {
    e.preventDefault();

    if (!currentOrderForReturn) return;

    const reasonSelect = document.getElementById('returnReasonSelect').value;
    const reasonText = document.getElementById('returnReasonText').value.trim();

    if (!reasonSelect) {
        alert('請選擇退貨原因');
        return;
    }

    if (!reasonText) {
        alert('請填寫詳細說明');
        return;
    }

    // 組合完整退貨原因
    const fullReason = `${reasonSelect} - ${reasonText}`;

    // 退款金額不包含運費
    const refundAmount = Math.max(0, currentOrderForReturn.orderTotal - SHIPPING_FEE);

    const returnData = {
        orderId: currentOrderForReturn.orderId,
        returnReason: fullReason,
        refundAmount: refundAmount,
        // TODO: 圖片上傳處理
    };

    try {
        console.log('📝 提交退貨申請:', returnData);

        // 呼叫後端 API
        const result = await applyReturnAPI(returnData);

        alert('退貨申請已提交，我們將儘快審核');
        closeReturnModal();

        // 重新載入訂單和退貨資料
        await loadOrders();
        await loadReturns();

    } catch (error) {
        console.error('❌ 提交退貨失敗:', error);
        alert('提交退貨失敗，請稍後再試');
    }
}

/**
 * 呼叫後端申請退貨 API
 * @param {Object} returnData - 退貨資料
 */
async function applyReturnAPI(returnData) {
    const url = ORDERS_API.APPLY_RETURN;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(returnData)
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result;
    } else {
        throw new Error(result.message || '申請退貨失敗');
    }
}

/**
 * 更新圖片預覽
 */
function updateImagePreview() {
    const input = document.getElementById('returnImages');
    const preview = document.getElementById('imagePreview');

    if (!input.files || input.files.length === 0) {
        preview.innerHTML = '';
        return;
    }

    let html = '';

    for (let i = 0; i < Math.min(input.files.length, 5); i++) {
        const file = input.files[i];
        const reader = new FileReader();

        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.width = '80px';
            img.style.height = '80px';
            img.style.objectFit = 'cover';
            img.style.borderRadius = '8px';
            img.style.marginRight = '0.5rem';
            preview.appendChild(img);
        };

        reader.readAsDataURL(file);
    }
}

/**
 * 查看退貨詳情
 * @param {number} orderId - 訂單ID
 */
function viewReturnDetails(orderId) {
    const returnOrder = allReturns.find(r => r.orderId === orderId);
    if (!returnOrder) return;

    const statusInfo = RETURN_STATUS_MAP[returnOrder.returnStatus];
    const applyTime = formatDateTimeFull(returnOrder.applyTime);

    alert(
        `退貨單資訊\n\n` +
        `訂單編號：#${returnOrder.orderId}\n` +
        `退貨單號：#${returnOrder.returnId}\n` +
        `申請時間：${applyTime}\n` +
        `退貨狀態：${statusInfo.text}\n` +
        `退款金額：$${returnOrder.refundAmount.toLocaleString()}\n` +
        `退貨原因：${returnOrder.returnReason}`
    );

    // TODO: 改為顯示退貨詳情頁面或 Modal
}

// ========================================
// 評價賣家
// ========================================

/**
 * 開啟評價視窗
 * @param {number} orderId - 訂單ID
 */
function openReviewModal(orderId) {
    document.getElementById('reviewOrderId').textContent = `#${orderId}`;
    document.getElementById('reviewModal').style.display = 'flex';
    setRating(0);
}

/**
 * 關閉評價視窗
 */
function closeReviewModal() {
    document.getElementById('reviewModal').style.display = 'none';
    currentRating = 0;
}

/**
 * 設定評分
 * @param {number} rating - 評分 (1-5)
 */
function setRating(rating) {
    currentRating = rating;
    const stars = document.querySelectorAll('.rating-star');

    stars.forEach((star, index) => {
        if (index < rating) {
            star.classList.remove('far');
            star.classList.add('fas');
        } else {
            star.classList.remove('fas');
            star.classList.add('far');
        }
    });
}

/**
 * 提交評價
 * @param {Event} e - 表單提交事件
 */
async function submitReview(e) {
    e.preventDefault();

    if (currentRating === 0) {
        alert('請選擇評分');
        return;
    }

    const reviewContent = e.target.querySelector('textarea').value.trim();

    if (!reviewContent) {
        alert('請填寫評價內容');
        return;
    }

    const orderIdText = document.getElementById('reviewOrderId').textContent;
    const orderId = parseInt(orderIdText.replace('#', ''));

    const reviewData = {
        orderId: orderId,
        rating: currentRating,
        reviewContent: reviewContent
    };

    try {
        console.log('提交評價:', reviewData);

        // 呼叫後端評價 API
        await createReviewAPI(reviewData);

        alert('評價成功！感謝您的分享');
        closeReviewModal();

        // 重新載入訂單
        await loadOrders();
        await loadReviews();

    } catch (error) {
        console.error('❌ 提交評價失敗:', error);
        alert('提交評價失敗，請稍後再試');
    }
}

/**
 * 呼叫後端新增評價 API
 * @param {Object} reviewData - 評價資料
 */
async function createReviewAPI(reviewData) {
    const url = ORDERS_API.CREATE_REVIEW;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(reviewData)
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result;
    } else {
        throw new Error(result.message || '新增評價失敗');
    }
}

// ========================================
// 工具函數
// ========================================

/**
 * 格式化日期時間（簡短版）
 * @param {string} dateTimeString - ISO 日期時間字串
 * @returns {string} 格式化後的日期時間
 */
function formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString);
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${month}/${day} ${hours}:${minutes}`;
}

/**
 * 格式化日期時間（完整版）
 * @param {string} dateTimeString - ISO 日期時間字串
 * @returns {string} 格式化後的日期時間
 */
function formatDateTimeFull(dateTimeString) {
    const date = new Date(dateTimeString);
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

window.filterOrders = filterOrders;
window.toggleOrderDetails = toggleOrderDetails;
window.confirmReceipt = confirmReceipt;
window.openCancelOrderModal = openCancelOrderModal;
window.closeCancelOrderModal = closeCancelOrderModal;
window.confirmCancelOrder = confirmCancelOrder;
window.openReturnModal = openReturnModal;
window.closeReturnModal = closeReturnModal;
window.submitReturn = submitReturn;
window.updateImagePreview = updateImagePreview;
window.viewReturnDetails = viewReturnDetails;
window.openReviewModal = openReviewModal;
window.closeReviewModal = closeReviewModal;
window.setRating = setRating;
window.submitReview = submitReview;
