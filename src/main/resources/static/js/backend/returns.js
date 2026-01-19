/**
 * PetGuardian - Backend Returns Management
 * 後台退貨管理頁面業務邏輯
 *
 * @author PetGuardian Backend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const RETURNS_API = {
    GET_ALL_RETURNS: '/api/management/returns',
    GET_PENDING_RETURNS: '/api/management/returns/pending',
    UPDATE_RETURN_STATUS: '/api/management/returns/{returnId}/status',
    GET_RETURN_DETAILS: '/api/returns/{returnId}',
};

// ========================================
// 退貨狀態常數
// ========================================

const RETURN_STATUS = {
    PENDING: 0,   // 待審核
    APPROVED: 1,  // 已通過
    REJECTED: 2,  // 已拒絕
};

const RETURN_STATUS_TEXT = {
    0: '待審核',
    1: '已通過',
    2: '已拒絕',
};

const RETURN_STATUS_COLOR = {
    0: '#f39c12',
    1: '#2ecc71',
    2: '#e74c3c',
};

// ========================================
// 全域狀態
// ========================================

let allReturns = [];
let filteredReturns = [];
let currentFilter = 'all';
let selectedReturnId = null;

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🔄 Backend Returns Management Page Initialized');

    // 載入所有退貨單
    loadAllReturns();
});

// ========================================
// 載入退貨資料
// ========================================

/**
 * 載入所有退貨單
 */
async function loadAllReturns() {
    try {
        console.log('🔄 載入所有退貨單...');

        const returns = await fetchAllReturns();
        allReturns = returns;
        filteredReturns = returns;

        updateStatistics(returns);
        renderReturns(returns);

    } catch (error) {
        console.error('❌ 載入退貨單失敗:', error);
        showError('載入退貨資料失敗');
    }
}

/**
 * 呼叫後端 API 獲取所有退貨單
 * @returns {Promise<Array>} 退貨單陣列
 */
async function fetchAllReturns() {
    const url = RETURNS_API.GET_ALL_RETURNS;

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
            throw new Error(result.message || '獲取退貨單失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬資料
        console.log(`🔒 [模擬] 查詢所有退貨單 API: ${url}`);
        await delay(500);

        const mockReturns = [
            {
                returnId: 1,
                orderId: 4,
                applyTime: '2024-01-14T10:30:00',
                returnReason: '商品有瑕疵 - 背包拉鍊損壞，無法正常使用',
                refundAmount: 910,
                returnStatus: 0,
                // 擴展資料（從訂單聯結）
                buyerMemId: 7,
                buyerName: '劉小美',
                buyerPhone: '0945678901',
            },
            {
                returnId: 2,
                orderId: 5,
                applyTime: '2024-01-13T14:15:00',
                returnReason: '商品與描述不符 - 顏色不一樣',
                refundAmount: 1200,
                returnStatus: 1,
                approveTime: '2024-01-13T16:00:00',
                buyerMemId: 8,
                buyerName: '陳大華',
                buyerPhone: '0956789012',
            },
            {
                returnId: 3,
                orderId: 6,
                applyTime: '2024-01-12T11:00:00',
                returnReason: '不想要了',
                refundAmount: 500,
                returnStatus: 2,
                approveTime: '2024-01-12T13:00:00',
                buyerMemId: 9,
                buyerName: '林小花',
                buyerPhone: '0967890123',
            },
        ];

        return mockReturns;
    }
}

// ========================================
// 統計資料更新
// ========================================

/**
 * 更新統計資料
 * @param {Array} returns - 退貨單陣列
 */
function updateStatistics(returns) {
    const stats = {
        pending: returns.filter(r => r.returnStatus === RETURN_STATUS.PENDING).length,
        approved: returns.filter(r => r.returnStatus === RETURN_STATUS.APPROVED).length,
        rejected: returns.filter(r => r.returnStatus === RETURN_STATUS.REJECTED).length,
    };

    document.getElementById('stat-pending').textContent = stats.pending;
    document.getElementById('stat-approved').textContent = stats.approved;
    document.getElementById('stat-rejected').textContent = stats.rejected;
}

// ========================================
// 渲染退貨列表
// ========================================

/**
 * 渲染退貨列表
 * @param {Array} returns - 退貨單陣列
 */
function renderReturns(returns) {
    const tbody = document.querySelector('#returnsTable tbody');

    if (!returns || returns.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" style="text-align: center; padding: 3rem; color: #999;">
                    <i class="fa-solid fa-inbox" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                    目前沒有退貨申請
                </td>
            </tr>
        `;
        return;
    }

    let html = '';

    returns.forEach(returnOrder => {
        const applyTime = formatDateTime(returnOrder.applyTime);
        const statusColor = RETURN_STATUS_COLOR[returnOrder.returnStatus];
        const statusText = RETURN_STATUS_TEXT[returnOrder.returnStatus];

        // 截斷過長的退貨原因
        const shortReason = returnOrder.returnReason.length > 50
            ? returnOrder.returnReason.substring(0, 50) + '...'
            : returnOrder.returnReason;

        html += `
            <tr>
                <td><strong>#${returnOrder.returnId}</strong></td>
                <td><strong>#${returnOrder.orderId}</strong></td>
                <td>${applyTime}</td>
                <td style="max-width: 300px;">
                    <div style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${returnOrder.returnReason}">
                        ${shortReason}
                    </div>
                </td>
                <td><strong>$${returnOrder.refundAmount.toLocaleString()}</strong></td>
                <td>
                    <span class="badge" style="background: ${statusColor}; color: white; padding: 0.4rem 0.8rem; border-radius: 4px; font-size: 0.85rem;">
                        ${statusText}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm" onclick="viewReturnDetail(${returnOrder.returnId})"
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
// 退貨篩選
// ========================================

/**
 * 篩選退貨單
 * @param {string|number} status - 狀態 ('all' 或狀態碼)
 * @param {HTMLElement} btnElement - 按鈕元素
 */
function filterReturns(status, btnElement) {
    currentFilter = status;

    // 更新按鈕樣式
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    btnElement.classList.add('active');

    // 篩選退貨單
    if (status === 'all') {
        filteredReturns = allReturns;
    } else {
        filteredReturns = allReturns.filter(returnOrder => returnOrder.returnStatus === status);
    }

    renderReturns(filteredReturns);
}

// ========================================
// 退貨詳情
// ========================================

/**
 * 查看退貨詳情
 * @param {number} returnId - 退貨單ID
 */
async function viewReturnDetail(returnId) {
    try {
        const returnOrder = allReturns.find(r => r.returnId === returnId);
        if (!returnOrder) {
            alert('退貨單不存在');
            return;
        }

        selectedReturnId = returnId;

        const applyTime = formatDateTimeFull(returnOrder.applyTime);
        const statusText = RETURN_STATUS_TEXT[returnOrder.returnStatus];
        const statusColor = RETURN_STATUS_COLOR[returnOrder.returnStatus];

        let html = `
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;">
                <div>
                    <div style="color: #666; font-size: 0.9rem;">退貨單號</div>
                    <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary-color);">#${returnOrder.returnId}</div>
                </div>
                <div>
                    <div style="color: #666; font-size: 0.9rem;">訂單編號</div>
                    <div style="font-weight: 700; font-size: 1.1rem; color: var(--primary-color);">#${returnOrder.orderId}</div>
                </div>
                <div>
                    <div style="color: #666; font-size: 0.9rem;">申請時間</div>
                    <div style="font-weight: 600;">${applyTime}</div>
                </div>
                <div>
                    <div style="color: #666; font-size: 0.9rem;">退貨狀態</div>
                    <span class="badge" style="background: ${statusColor}; color: white; padding: 0.4rem 0.8rem; border-radius: 4px; font-size: 0.85rem;">
                        ${statusText}
                    </span>
                </div>
                <div style="grid-column: 1 / -1;">
                    <div style="color: #666; font-size: 0.9rem;">退款金額</div>
                    <div style="font-weight: 700; font-size: 1.3rem; color: var(--primary-color);">$${returnOrder.refundAmount.toLocaleString()}</div>
                </div>
            </div>

            <div style="border-top: 1px solid #eee; padding-top: 1rem; margin-bottom: 1rem;">
                <div style="color: #666; font-size: 0.9rem; margin-bottom: 0.5rem;">買家資訊</div>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 0.5rem;">
                    <div><strong>會員ID：</strong>${returnOrder.buyerMemId}</div>
                    <div><strong>姓名：</strong>${returnOrder.buyerName || '未知'}</div>
                    <div><strong>電話：</strong>${returnOrder.buyerPhone || '未知'}</div>
                </div>
            </div>

            <div style="border-top: 1px solid #eee; padding-top: 1rem;">
                <div style="color: #666; font-size: 0.9rem; margin-bottom: 0.5rem;">退貨原因</div>
                <div style="background: #f8f9fa; padding: 1rem; border-radius: 8px; line-height: 1.6;">
                    ${returnOrder.returnReason}
                </div>
            </div>
        `;

        document.getElementById('returnDetailContent').innerHTML = html;

        // 渲染操作按鈕
        let actionsHTML = '';

        if (returnOrder.returnStatus === RETURN_STATUS.PENDING) {
            actionsHTML = `
                <button class="btn ghost" onclick="closeReturnDetail()" style="flex:1;">取消</button>
                <button class="btn" onclick="handleReturn(${returnId}, ${RETURN_STATUS.REJECTED})"
                        style="flex:1; background:#e74c3c; color: white;">
                    <i class="fa-solid fa-times"></i> 拒絕退貨
                </button>
                <button class="btn" onclick="handleReturn(${returnId}, ${RETURN_STATUS.APPROVED})"
                        style="flex:1; background:#2ecc71; color: white;">
                    <i class="fa-solid fa-check"></i> 通過退貨
                </button>
            `;
        } else {
            actionsHTML = `
                <button class="btn" onclick="closeReturnDetail()" style="flex:1; background: var(--primary-color); color: white;">關閉</button>
            `;
        }

        document.getElementById('returnActions').innerHTML = actionsHTML;

        document.getElementById('returnDetailModal').style.display = 'flex';

    } catch (error) {
        console.error('❌ 查看退貨詳情失敗:', error);
        alert('查看退貨詳情失敗');
    }
}

/**
 * 關閉退貨詳情視窗
 */
function closeReturnDetail() {
    document.getElementById('returnDetailModal').style.display = 'none';
    selectedReturnId = null;
}

/**
 * 處理退貨（通過或拒絕）
 * @param {number} returnId - 退貨單ID
 * @param {number} newStatus - 新狀態
 */
async function handleReturn(returnId, newStatus) {
    const actionText = newStatus === RETURN_STATUS.APPROVED ? '通過退貨' : '拒絕退貨';

    if (!confirm(`確定要${actionText}嗎？`)) {
        return;
    }

    try {
        console.log(`🔨 處理退貨: returnId=${returnId}, status=${newStatus}`);

        await updateReturnStatus(returnId, newStatus);

        const resultText = newStatus === RETURN_STATUS.APPROVED
            ? '已通過退貨申請，將為買家辦理退款'
            : '已拒絕退貨申請';

        alert(resultText);
        closeReturnDetail();

        // 重新載入退貨單
        await loadAllReturns();

    } catch (error) {
        console.error('❌ 處理退貨失敗:', error);
        alert('操作失敗，請稍後再試');
    }
}

/**
 * 呼叫後端更新退貨狀態 API
 * @param {number} returnId - 退貨單ID
 * @param {number} newStatus - 新狀態
 */
async function updateReturnStatus(returnId, newStatus) {
    const url = RETURNS_API.UPDATE_RETURN_STATUS.replace('{returnId}', returnId);

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ returnStatus: newStatus })
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            return result;
        } else {
            throw new Error(result.message || '更新退貨狀態失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬 API
        console.log(`🔒 [模擬] 更新退貨狀態 API: ${url}`);
        await delay(500);

        // 更新本地資料
        const returnOrder = allReturns.find(r => r.returnId === returnId);
        if (returnOrder) {
            returnOrder.returnStatus = newStatus;
            returnOrder.approveTime = new Date().toISOString();
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
    const tbody = document.querySelector('#returnsTable tbody');
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

window.filterReturns = filterReturns;
window.viewReturnDetail = viewReturnDetail;
window.closeReturnDetail = closeReturnDetail;
window.handleReturn = handleReturn;
