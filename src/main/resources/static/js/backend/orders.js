/**
 * PetGuardian - Backend Orders Management
 * 後台訂單管理頁面業務邏輯
 *
 * @author PetGuardian Backend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const ORDERS_API = {
    GET_ALL_ORDERS: '/api/management/orders',
    GET_ORDERS_BY_STATUS: '/api/management/orders/status/{status}',
    UPDATE_ORDER_STATUS: '/api/management/orders/{orderId}/status',
    GET_ORDER_DETAILS: '/api/orders/{orderId}',
};

// ========================================
// 訂單狀態常數
// ========================================

const ORDER_STATUS = {
    PAID: 0,           // 已付款
    SHIPPED: 1,        // 已出貨
    COMPLETED: 2,      // 已完成
    CANCELLED: 3,      // 已取消
    RETURN_PENDING: 4, // 申請退貨中
    RETURN_DONE: 5,    // 退貨完成
};

const ORDER_STATUS_TEXT = {
    0: '已付款',
    1: '已出貨',
    2: '已完成',
    3: '已取消',
    4: '申請退貨中',
    5: '退貨完成',
};

const ORDER_STATUS_COLOR = {
    0: '#f39c12',
    1: '#3498db',
    2: '#2ecc71',
    3: '#95a5a6',
    4: '#e74c3c',
    5: '#95a5a6',
};

// ========================================
// 全域狀態
// ========================================

let allOrders = [];
let filteredOrders = [];
let currentFilter = 'all';
let currentPage = 1;
let pageSize = 10;
let selectedOrderId = null;

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('📦 Backend Orders Management Page Initialized');

    // 載入所有訂單
    loadAllOrders();
});

// ========================================
// 載入訂單資料
// ========================================

/**
 * 載入所有訂單
 */
async function loadAllOrders() {
    try {
        console.log('📦 載入所有訂單...');

        const orders = await fetchAllOrders();
        allOrders = orders;
        filteredOrders = orders;

        updateStatistics(orders);
        renderOrders(orders);
        renderPagination();

    } catch (error) {
        console.error('❌ 載入訂單失敗:', error);
        showError('載入訂單資料失敗');
    }
}

/**
 * 呼叫後端 API 獲取所有訂單
 * @returns {Promise<Array>} 訂單陣列
 */
async function fetchAllOrders() {
    const url = ORDERS_API.GET_ALL_ORDERS;

    try {
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
            throw new Error(result.message || '獲取訂單失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬資料
        console.log(`🔒 [模擬] 查詢所有訂單 API: ${url}`);
        await delay(500);

        const mockOrders = [
            {
                orderId: 1,
                buyerMemId: 1,
                sellerMemId: 2,
                orderTime: '2024-01-14T10:30:00',
                orderTotal: 1260,
                paymentMethod: 0,
                orderStatus: 0,
                receiverName: '王小明',
                receiverPhone: '0912345678',
                receiverAddress: '台北市中正區重慶南路一段122號',
                orderItems: [
                    {
                        proId: 1,
                        productTitle: '全新貓咪自動餵食器',
                        quantity: 1,
                        proPrice: 1200,
                    }
                ]
            },
            {
                orderId: 2,
                buyerMemId: 3,
                sellerMemId: 2,
                orderTime: '2024-01-14T14:15:00',
                orderTotal: 560,
                paymentMethod: 1,
                orderStatus: 1,
                receiverName: '李小華',
                receiverPhone: '0923456789',
                receiverAddress: '台北市信義區信義路五段7號',
                orderItems: [
                    {
                        proId: 2,
                        productTitle: '狗狗潔牙骨 (大包裝)',
                        quantity: 1,
                        proPrice: 500,
                    }
                ]
            },
            {
                orderId: 3,
                buyerMemId: 5,
                sellerMemId: 4,
                orderTime: '2024-01-13T16:20:00',
                orderTotal: 910,
                paymentMethod: 0,
                orderStatus: 2,
                receiverName: '張大明',
                receiverPhone: '0934567890',
                receiverAddress: '台北市大安區敦化南路二段105號',
                orderItems: [
                    {
                        proId: 3,
                        productTitle: '寵物外出提籃',
                        quantity: 1,
                        proPrice: 890,
                    }
                ]
            },
            {
                orderId: 4,
                buyerMemId: 7,
                sellerMemId: 2,
                orderTime: '2024-01-12T11:00:00',
                orderTotal: 910,
                paymentMethod: 0,
                orderStatus: 4,
                receiverName: '劉小美',
                receiverPhone: '0945678901',
                receiverAddress: '台北市松山區南京東路四段2號',
                orderItems: [
                    {
                        proId: 4,
                        productTitle: '太空艙寵物背包',
                        quantity: 1,
                        proPrice: 850,
                    }
                ]
            },
        ];

        return mockOrders;
    }
}

// ========================================
// 統計資料更新
// ========================================

/**
 * 更新統計資料
 * @param {Array} orders - 訂單陣列
 */
function updateStatistics(orders) {
    const stats = {
        pending: orders.filter(o => o.orderStatus === ORDER_STATUS.PAID).length,
        shipped: orders.filter(o => o.orderStatus === ORDER_STATUS.SHIPPED).length,
        completed: orders.filter(o => o.orderStatus === ORDER_STATUS.COMPLETED).length,
        returns: orders.filter(o => o.orderStatus === ORDER_STATUS.RETURN_PENDING).length,
    };

    document.getElementById('stat-pending').textContent = stats.pending;
    document.getElementById('stat-shipped').textContent = stats.shipped;
    document.getElementById('stat-completed').textContent = stats.completed;
    document.getElementById('stat-returns').textContent = stats.returns;
}

// ========================================
// 渲染訂單列表
// ========================================

/**
 * 渲染訂單列表
 * @param {Array} orders - 訂單陣列
 */
function renderOrders(orders) {
    const tbody = document.querySelector('#ordersTable tbody');

    if (!orders || orders.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 3rem; color: #999;">
                    <i class="fa-solid fa-inbox" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                    目前沒有訂單
                </td>
            </tr>
        `;
        return;
    }

    // 分頁處理
    const start = (currentPage - 1) * pageSize;
    const end = start + pageSize;
    const paginatedOrders = orders.slice(start, end);

    let html = '';

    paginatedOrders.forEach(order => {
        const orderTime = formatDateTime(order.orderTime);
        const statusColor = ORDER_STATUS_COLOR[order.orderStatus];
        const statusText = ORDER_STATUS_TEXT[order.orderStatus];

        const mainProduct = order.orderItems[0];
        const itemCount = order.orderItems.length;

        html += `
            <tr>
                <td><strong>#${order.orderId}</strong></td>
                <td>${orderTime}</td>
                <td>
                    會員 ID: ${order.buyerMemId}<br>
                    <small style="color:#666;">${order.receiverName}</small>
                </td>
                <td>
                    ${mainProduct.productTitle}
                    ${itemCount > 1 ? `<br><small style="color:#666;">+${itemCount - 1} 件商品</small>` : ''}
                </td>
                <td><strong>$${order.orderTotal.toLocaleString()}</strong></td>
                <td>
                    <span class="badge" style="background: ${statusColor}; color: white; padding: 0.4rem 0.8rem; border-radius: 4px; font-size: 0.85rem;">
                        ${statusText}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm" onclick="viewOrderDetail(${order.orderId})"
                            style="background: #3498db; color: white; padding: 0.4rem 0.8rem; border: none; border-radius: 4px; cursor: pointer;">
                        <i class="fa-solid fa-eye"></i> 詳情
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// ========================================
// 分頁功能
// ========================================

/**
 * 渲染分頁
 */
function renderPagination() {
    const container = document.getElementById('pagination');
    const totalPages = Math.ceil(filteredOrders.length / pageSize);

    if (totalPages <= 1) {
        container.innerHTML = '';
        return;
    }

    let html = '';

    // 上一頁
    if (currentPage > 1) {
        html += `<button class="btn btn-sm" onclick="changePage(${currentPage - 1})" style="padding: 0.4rem 0.8rem;">上一頁</button>`;
    }

    // 頁碼
    for (let i = 1; i <= totalPages; i++) {
        if (i === currentPage) {
            html += `<button class="btn btn-sm" style="background: var(--primary-color); color: white; padding: 0.4rem 0.8rem;">${i}</button>`;
        } else if (i === 1 || i === totalPages || (i >= currentPage - 1 && i <= currentPage + 1)) {
            html += `<button class="btn btn-sm ghost" onclick="changePage(${i})" style="padding: 0.4rem 0.8rem;">${i}</button>`;
        } else if (i === currentPage - 2 || i === currentPage + 2) {
            html += `<span>...</span>`;
        }
    }

    // 下一頁
    if (currentPage < totalPages) {
        html += `<button class="btn btn-sm" onclick="changePage(${currentPage + 1})" style="padding: 0.4rem 0.8rem;">下一頁</button>`;
    }

    container.innerHTML = html;
}

/**
 * 切換頁面
 * @param {number} page - 頁碼
 */
function changePage(page) {
    currentPage = page;
    renderOrders(filteredOrders);
    renderPagination();
}

// ========================================
// 訂單篩選
// ========================================

/**
 * 篩選訂單
 * @param {string|number} status - 狀態 ('all' 或狀態碼)
 * @param {HTMLElement} btnElement - 按鈕元素
 */
function filterOrders(status, btnElement) {
    currentFilter = status;
    currentPage = 1;

    // 更新按鈕樣式
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    btnElement.classList.add('active');

    // 篩選訂單
    if (status === 'all') {
        filteredOrders = allOrders;
    } else {
        filteredOrders = allOrders.filter(order => order.orderStatus === status);
    }

    renderOrders(filteredOrders);
    renderPagination();
}

/**
 * 搜尋訂單
 */
function searchOrders() {
    const searchText = document.getElementById('searchInput').value.trim().toLowerCase();

    if (!searchText) {
        filteredOrders = currentFilter === 'all' ? allOrders : allOrders.filter(o => o.orderStatus === currentFilter);
    } else {
        const baseOrders = currentFilter === 'all' ? allOrders : allOrders.filter(o => o.orderStatus === currentFilter);

        filteredOrders = baseOrders.filter(order => {
            return (
                order.orderId.toString().includes(searchText) ||
                order.receiverName.toLowerCase().includes(searchText) ||
                order.receiverPhone.includes(searchText)
            );
        });
    }

    currentPage = 1;
    renderOrders(filteredOrders);
    renderPagination();
}

// ========================================
// 訂單詳情
// ========================================

/**
 * 查看訂單詳情
 * @param {number} orderId - 訂單ID
 */
async function viewOrderDetail(orderId) {
    try {
        const order = allOrders.find(o => o.orderId === orderId);
        if (!order) {
            alert('訂單不存在');
            return;
        }

        selectedOrderId = orderId;

        const orderTime = formatDateTimeFull(order.orderTime);
        const statusText = ORDER_STATUS_TEXT[order.orderStatus];

        let html = `
            <div style="border-bottom: 1px solid #eee; padding-bottom: 1rem; margin-bottom: 1rem;">
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div>
                        <div style="color: #666; font-size: 0.9rem;">訂單編號</div>
                        <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary-color);">#${order.orderId}</div>
                    </div>
                    <div>
                        <div style="color: #666; font-size: 0.9rem;">訂單時間</div>
                        <div style="font-weight: 600;">${orderTime}</div>
                    </div>
                    <div>
                        <div style="color: #666; font-size: 0.9rem;">訂單狀態</div>
                        <select id="orderStatusSelect" style="padding: 0.5rem; border: 1px solid #ddd; border-radius: 4px; width: 100%;">
                            ${Object.keys(ORDER_STATUS_TEXT).map(status => `
                                <option value="${status}" ${order.orderStatus == status ? 'selected' : ''}>
                                    ${ORDER_STATUS_TEXT[status]}
                                </option>
                            `).join('')}
                        </select>
                    </div>
                    <div>
                        <div style="color: #666; font-size: 0.9rem;">訂單金額</div>
                        <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary-color);">$${order.orderTotal.toLocaleString()}</div>
                    </div>
                </div>
            </div>

            <div style="border-bottom: 1px solid #eee; padding-bottom: 1rem; margin-bottom: 1rem;">
                <h4 style="margin-bottom: 0.75rem;">收件資訊</h4>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                    <div><strong>收件人：</strong>${order.receiverName}</div>
                    <div><strong>電話：</strong>${order.receiverPhone}</div>
                    <div style="grid-column: 1 / -1;"><strong>地址：</strong>${order.receiverAddress}</div>
                </div>
            </div>

            <div>
                <h4 style="margin-bottom: 0.75rem;">商品明細</h4>
                ${order.orderItems.map(item => `
                    <div style="display: flex; justify-content: space-between; padding: 0.5rem 0; border-bottom: 1px solid #f0f0f0;">
                        <div>${item.productTitle} x${item.quantity}</div>
                        <div style="font-weight: 600;">$${item.proPrice.toLocaleString()}</div>
                    </div>
                `).join('')}
                <div style="display: flex; justify-content: space-between; padding: 0.75rem 0; font-weight: 700; font-size: 1.1rem;">
                    <div>總計</div>
                    <div style="color: var(--primary-color);">$${order.orderTotal.toLocaleString()}</div>
                </div>
            </div>
        `;

        document.getElementById('orderDetailContent').innerHTML = html;
        document.getElementById('orderDetailModal').style.display = 'flex';

    } catch (error) {
        console.error('❌ 查看訂單詳情失敗:', error);
        alert('查看訂單詳情失敗');
    }
}

/**
 * 關閉訂單詳情視窗
 */
function closeOrderDetail() {
    document.getElementById('orderDetailModal').style.display = 'none';
    selectedOrderId = null;
}

/**
 * 從視窗更新訂單狀態
 */
async function updateOrderStatusFromModal() {
    if (!selectedOrderId) return;

    const newStatus = parseInt(document.getElementById('orderStatusSelect').value);
    const order = allOrders.find(o => o.orderId === selectedOrderId);

    if (!order) return;

    if (order.orderStatus === newStatus) {
        alert('狀態未變更');
        return;
    }

    if (!confirm(`確定要將訂單狀態更新為「${ORDER_STATUS_TEXT[newStatus]}」？`)) {
        return;
    }

    try {
        await updateOrderStatus(selectedOrderId, newStatus);

        alert('訂單狀態已更新');
        closeOrderDetail();

        // 重新載入訂單
        await loadAllOrders();

    } catch (error) {
        console.error('❌ 更新訂單狀態失敗:', error);
        alert('更新訂單狀態失敗');
    }
}

/**
 * 呼叫後端更新訂單狀態 API
 * @param {number} orderId - 訂單ID
 * @param {number} newStatus - 新狀態
 */
async function updateOrderStatus(orderId, newStatus) {
    const url = ORDERS_API.UPDATE_ORDER_STATUS.replace('{orderId}', orderId);

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ orderStatus: newStatus })
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            return result;
        } else {
            throw new Error(result.message || '更新訂單狀態失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬 API
        console.log(`🔒 [模擬] 更新訂單狀態 API: ${url}`);
        await delay(500);

        // 更新本地資料
        const order = allOrders.find(o => o.orderId === orderId);
        if (order) {
            order.orderStatus = newStatus;
        }

        return { success: true };
    }
}

// ========================================
// 工具函數
// ========================================

/**
 * 顯示錯誤訊息
 * @param {string} message - 錯誤訊息
 */
function showError(message) {
    const tbody = document.querySelector('#ordersTable tbody');
    tbody.innerHTML = `
        <tr>
            <td colspan="7" style="text-align: center; padding: 3rem; color: #d9534f;">
                <i class="fa-solid fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                ${message}
            </td>
        </tr>
    `;
}

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

// ========================================
// 全域函數暴露
// ========================================

window.filterOrders = filterOrders;
window.searchOrders = searchOrders;
window.viewOrderDetail = viewOrderDetail;
window.closeOrderDetail = closeOrderDetail;
window.updateOrderStatusFromModal = updateOrderStatusFromModal;
window.changePage = changePage;
