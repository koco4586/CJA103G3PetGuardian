/**
 * PetGuardian - Backend Reviews Management
 * 後台評價檢舉管理頁面業務邏輯
 *
 * @author PetGuardian Backend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const REVIEWS_API = {
    // 賣家評價管理
    GET_ALL_REVIEWS: '/api/admin/seller-reviews',
    UPDATE_REVIEW_SHOW_STATUS: '/api/admin/seller-reviews/{reviewId}/show-status',

    // 評價檢舉管理
    GET_ALL_REPORTS: '/api/admin/review-reports',
    GET_PENDING_REPORTS: '/api/admin/review-reports/pending',
    UPDATE_REPORT_STATUS: '/api/admin/review-reports/{reviewRptId}/status',
};

// ========================================
// 檢舉狀態常數
// ========================================

const REPORT_STATUS = {
    PENDING: 0,      // 待審核
    APPROVED: 1,     // 成立
    REJECTED: 2,     // 不成立
};

const REPORT_STATUS_TEXT = {
    0: '待審核',
    1: '成立',
    2: '不成立',
};

// ========================================
// 全域狀態
// ========================================

let allReports = [];
let allReviews = [];
let currentFilter = 'pending';

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('🔍 Backend Reviews Management Page Initialized');

    // 載入待處理檢舉
    loadPendingReports();

    // 載入所有評價
    loadAllReviews();
});

// ========================================
// 載入檢舉資料
// ========================================

/**
 * 載入待處理檢舉
 */
async function loadPendingReports() {
    try {
        console.log('📋 載入待處理檢舉...');

        const reports = await fetchPendingReports();
        allReports = reports;

        renderReports(reports);

    } catch (error) {
        console.error('❌ 載入檢舉失敗:', error);
        showError('載入檢舉資料失敗');
    }
}

/**
 * 呼叫後端 API 獲取待處理檢舉
 * @returns {Promise<Array>} 檢舉陣列
 */
async function fetchPendingReports() {
    const url = REVIEWS_API.GET_PENDING_REPORTS;

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
            throw new Error(result.message || '獲取檢舉失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬資料
        console.log(`🔒 [模擬] 查詢待處理檢舉 API: ${url}`);
        await delay(500);

        const mockReports = [
            {
                reviewRptId: 1,
                reviewId: 101,
                reporterMemId: 3,
                reportReason: '評價內容不實，涉及人身攻擊',
                reportStatus: 0,
                reportTime: '2024-01-14T10:30:00',
                // 擴展資料（從關聯表聯結）
                reviewContent: '這個賣家根本不負責任，態度超級差，大家千萬別上當...',
                rating: 1,
                reviewerName: '王小明',
                reviewSource: '商城評價',
                productTitle: '自動餵食器',
            },
            {
                reviewRptId: 2,
                reviewId: 102,
                reporterMemId: 5,
                reportReason: '垃圾訊息，含外部連結',
                reportStatus: 0,
                reportTime: '2024-01-14T14:15:00',
                reviewContent: '想買更便宜的加我微信: pet-keeper123',
                rating: 5,
                reviewerName: '帳號Unknown',
                reviewSource: '商城評價',
                productTitle: '寵物外出籃',
            },
        ];

        return mockReports;
    }
}

/**
 * 載入所有評價
 */
async function loadAllReviews() {
    try {
        const reviews = await fetchAllReviews();
        allReviews = reviews;
    } catch (error) {
        console.error('❌ 載入評價失敗:', error);
    }
}

/**
 * 呼叫後端 API 獲取所有評價
 * @returns {Promise<Array>} 評價陣列
 */
async function fetchAllReviews() {
    const url = REVIEWS_API.GET_ALL_REVIEWS;

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
            throw new Error(result.message || '獲取評價失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);
        return [];
    }
}

// ========================================
// 渲染檢舉列表
// ========================================

/**
 * 渲染檢舉列表
 * @param {Array} reports - 檢舉陣列
 */
function renderReports(reports) {
    const tbody = document.querySelector('table tbody');

    if (!reports || reports.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align: center; padding: 3rem; color: #999;">
                    <i class="fa-solid fa-inbox" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                    目前沒有待處理的檢舉
                </td>
            </tr>
        `;
        return;
    }

    let html = '';

    reports.forEach(report => {
        const stars = '★'.repeat(report.rating) + '☆'.repeat(5 - report.rating);

        html += `
            <tr data-report-id="${report.reviewRptId}">
                <td>
                    <span class="source-tag">${report.reviewSource}</span><br>
                    <strong>${report.productTitle || '未知商品'}</strong>
                </td>
                <td>
                    <div style="font-style: italic; color: #444;">「${report.reviewContent}」</div>
                    <div style="color:#999; font-size:0.8rem; margin-top:5px;">
                        評價 ID: ${report.reviewId}
                    </div>
                </td>
                <td>
                    ${report.reviewerName}<br>
                    <span style="color:gold">${stars}</span> ${report.rating}.0
                </td>
                <td>
                    <span class="report-reason">${report.reportReason}</span>
                </td>
                <td>
                    <button class="btn" style="background:#d9534f;"
                            onclick="handleReport(${report.reviewRptId}, ${REPORT_STATUS.APPROVED})">
                        刪除
                    </button>
                    <button class="btn ghost"
                            onclick="handleReport(${report.reviewRptId}, ${REPORT_STATUS.REJECTED})">
                        駁回
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

// ========================================
// 處理檢舉
// ========================================

/**
 * 處理檢舉（通過或駁回）
 * @param {number} reviewRptId - 檢舉ID
 * @param {number} newStatus - 新狀態 (1=成立, 2=不成立)
 */
async function handleReport(reviewRptId, newStatus) {
    const actionText = newStatus === REPORT_STATUS.APPROVED ? '刪除此評價並懲處用戶' : '駁回檢舉';

    if (!confirm(`確認${actionText}？`)) {
        return;
    }

    try {
        console.log(`🔨 處理檢舉: reviewRptId=${reviewRptId}, status=${newStatus}`);

        await updateReportStatus(reviewRptId, newStatus);

        const resultText = newStatus === REPORT_STATUS.APPROVED ? '已刪除評價並懲處用戶' : '已駁回檢舉';
        alert(resultText);

        // 移除該行（動畫效果）
        const row = document.querySelector(`tr[data-report-id="${reviewRptId}"]`);
        if (row) {
            row.style.opacity = '0';
            row.style.transition = 'opacity 0.3s ease';

            setTimeout(() => {
                row.remove();

                // 更新本地資料
                allReports = allReports.filter(r => r.reviewRptId !== reviewRptId);

                // 如果沒有檢舉了，重新渲染
                if (allReports.length === 0) {
                    renderReports([]);
                }
            }, 300);
        }

    } catch (error) {
        console.error('❌ 處理檢舉失敗:', error);
        alert('操作失敗，請稍後再試');
    }
}

/**
 * 呼叫後端更新檢舉狀態 API
 * @param {number} reviewRptId - 檢舉ID
 * @param {number} newStatus - 新狀態
 */
async function updateReportStatus(reviewRptId, newStatus) {
    const url = REVIEWS_API.UPDATE_REPORT_STATUS.replace('{reviewRptId}', reviewRptId);

    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ reportStatus: newStatus })
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const result = await response.json();

        if (result.success) {
            return result;
        } else {
            throw new Error(result.message || '更新檢舉狀態失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);

        // ⚠️ 開發階段：模擬 API
        console.log(`🔒 [模擬] 更新檢舉狀態 API: ${url}`);
        await delay(500);

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
    const tbody = document.querySelector('table tbody');
    tbody.innerHTML = `
        <tr>
            <td colspan="5" style="text-align: center; padding: 3rem; color: #d9534f;">
                <i class="fa-solid fa-exclamation-triangle" style="font-size: 2rem; margin-bottom: 0.5rem; display: block;"></i>
                ${message}
            </td>
        </tr>
    `;
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

window.handleReport = handleReport;
