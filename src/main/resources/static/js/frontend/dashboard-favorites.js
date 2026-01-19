/**
 * PetGuardian - Dashboard Favorites Page Logic
 * 會員收藏列表頁面業務邏輯
 *
 * @author PetGuardian Frontend Team
 * @version 1.0.0
 */

// ========================================
// API 端點配置
// ========================================

const FAVORITES_API = {
    // 查詢會員收藏列表
    GET_MEMBER_FAVORITES: '/api/favorites/member/{memId}',

    // 新增收藏
    ADD_FAVORITE: '/api/favorites',

    // 取消收藏
    REMOVE_FAVORITE: '/api/favorites',

    // 切換收藏狀態
    TOGGLE_FAVORITE: '/api/favorites/toggle',

    // 檢查是否已收藏
    CHECK_FAVORITED: '/api/favorites/check',

    // 統計會員收藏數
    COUNT_MEMBER_FAVORITES: '/api/favorites/member/{memId}/count',
};

// ========================================
// 全域狀態
// ========================================

let currentMemId = null;
let allFavorites = [];
let currentCategory = 'store';

// ========================================
// 初始化
// ========================================

document.addEventListener('DOMContentLoaded', () => {
    console.log('❤️ Dashboard Favorites Page Initialized');

    // TODO: 從 session 或 API 獲取當前登入的會員 ID
    // 測試用：使用資料庫中存在的會員 ID（1002 有收藏資料）
    currentMemId = 1002;

    // 載入收藏資料
    loadFavorites();
});

// ========================================
// 載入收藏資料
// ========================================

/**
 * 載入會員收藏列表
 */
async function loadFavorites() {
    try {
        console.log(`❤️ 載入會員收藏: memId=${currentMemId}`);

        const favorites = await fetchMemberFavorites(currentMemId);
        allFavorites = favorites;

        renderFavorites(favorites);

    } catch (error) {
        console.error('❌ 載入收藏失敗:', error);
        showEmptyState('載入收藏資料失敗，請稍後再試');
    }
}

/**
 * 呼叫後端 API 獲取會員收藏
 * @param {number} memId - 會員ID
 * @returns {Promise<Array>} 收藏陣列
 */
async function fetchMemberFavorites(memId) {
    const url = FAVORITES_API.GET_MEMBER_FAVORITES.replace('{memId}', memId);

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
        throw new Error(result.message || '獲取收藏失敗');
    }
}

// ========================================
// 渲染收藏列表
// ========================================

/**
 * 渲染收藏列表
 * @param {Array} favorites - 收藏陣列
 */
function renderFavorites(favorites) {
    const container = document.getElementById('fav-store');

    if (!favorites || favorites.length === 0) {
        showEmptyState('尚無收藏商品');
        return;
    }

    let html = '';

    favorites.forEach(fav => {
        html += `
            <div class="card favorite-card" data-pro-id="${fav.proId}">
                <div style="position: relative;">
                    <img src="${fav.productImg}"
                         alt="${fav.productTitle}"
                         class="card-img-top"
                         onclick="viewProductDetail(${fav.proId})"
                         style="cursor: pointer;">
                    <button class="btn-like active" onclick="removeFavoriteProduct(${fav.proId})">
                        <i class="fas fa-heart"></i>
                    </button>
                    ${fav.productStatus === '已售出' ? `
                        <div class="sold-badge">已售出</div>
                    ` : ''}
                </div>
                <div class="card-body">
                    <h4 class="mt-1">${fav.productTitle}</h4>
                    <p class="text-primary" style="font-weight: 700;">$${fav.productPrice.toLocaleString()}</p>
                    ${fav.productStatus === '販售中' ? `
                        <button class="btn btn-primary"
                                style="width: 100%; margin-top: 0.5rem;"
                                onclick="viewProductDetail(${fav.proId})">
                            查看詳情
                        </button>
                    ` : `
                        <button class="btn btn-outline"
                                style="width: 100%; margin-top: 0.5rem;"
                                disabled>
                            已售出
                        </button>
                    `}
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

/**
 * 顯示空狀態
 * @param {string} message - 訊息文字
 */
function showEmptyState(message) {
    const container = document.getElementById('fav-store');
    container.innerHTML = `
        <div style="text-align: center; padding: 3rem; color: #999; grid-column: 1 / -1;">
            <i class="fas fa-heart" style="font-size: 3rem; margin-bottom: 1rem; display: block; opacity: 0.3;"></i>
            <p>${message}</p>
            <a href="/store" class="btn btn-primary" style="margin-top: 1rem;">
                <i class="fas fa-shopping-bag"></i> 前往商城
            </a>
        </div>
    `;
}

// ========================================
// 收藏操作
// ========================================

/**
 * 移除收藏商品
 * @param {number} proId - 商品ID
 */
async function removeFavoriteProduct(proId) {
    if (!confirm('確定要移除此收藏嗎？')) {
        return;
    }

    try {
        console.log(`💔 移除收藏: memId=${currentMemId}, proId=${proId}`);

        await removeFavoriteAPI(currentMemId, proId);

        // 移除 DOM 元素（動畫效果）
        const card = document.querySelector(`[data-pro-id="${proId}"]`);
        if (card) {
            card.style.opacity = '0';
            card.style.transform = 'scale(0.9)';
            card.style.transition = 'all 0.3s ease';

            setTimeout(() => {
                card.remove();

                // 更新本地資料
                allFavorites = allFavorites.filter(f => f.proId !== proId);

                // 如果沒有收藏了，顯示空狀態
                if (allFavorites.length === 0) {
                    showEmptyState('尚無收藏商品');
                }

                if (window.showToast) {
                    window.showToast('已從收藏移除');
                }
            }, 300);
        }

    } catch (error) {
        console.error('❌ 移除收藏失敗:', error);
        alert('移除收藏失敗，請稍後再試');
    }
}

/**
 * 呼叫後端移除收藏 API
 * @param {number} memId - 會員ID
 * @param {number} proId - 商品ID
 */
async function removeFavoriteAPI(memId, proId) {
    const url = `${FAVORITES_API.REMOVE_FAVORITE}?memId=${memId}&proId=${proId}`;

    const response = await fetch(url, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return result;
    } else {
        throw new Error(result.message || '移除收藏失敗');
    }
}

/**
 * 切換收藏狀態（用於商品詳情頁）
 * @param {number} proId - 商品ID
 */
async function toggleFavorite(proId) {
    try {
        console.log(`❤️ 切換收藏: memId=${currentMemId}, proId=${proId}`);

        const result = await toggleFavoriteAPI(currentMemId, proId);

        if (result.action === 'added') {
            if (window.showToast) {
                window.showToast('已加入收藏');
            }
        } else {
            if (window.showToast) {
                window.showToast('已移除收藏');
            }
        }

        return result;

    } catch (error) {
        console.error('❌ 切換收藏失敗:', error);
        alert('操作失敗，請稍後再試');
    }
}

/**
 * 呼叫後端切換收藏 API
 * @param {number} memId - 會員ID
 * @param {number} proId - 商品ID
 */
async function toggleFavoriteAPI(memId, proId) {
    const url = FAVORITES_API.TOGGLE_FAVORITE;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ memId, proId })
    });

    if (!response.ok) {
        throw new Error(`HTTP Error: ${response.status}`);
    }

    const result = await response.json();

    if (result.success) {
        return {
            action: result.action,
            isFavorited: result.isFavorited
        };
    } else {
        throw new Error(result.message || '切換收藏失敗');
    }
}

/**
 * 檢查是否已收藏
 * @param {number} proId - 商品ID
 * @returns {Promise<boolean>}
 */
async function checkFavorited(proId) {
    const url = `${FAVORITES_API.CHECK_FAVORITED}?memId=${currentMemId}&proId=${proId}`;

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
            return result.isFavorited;
        } else {
            throw new Error(result.message || '檢查收藏失敗');
        }

    } catch (error) {
        console.error('API 錯誤:', error);
        return false;
    }
}

// ========================================
// 頁面導航
// ========================================

/**
 * 查看商品詳情（跳轉到商品頁面）
 * @param {number} proId - 商品ID
 */
function viewProductDetail(proId) {
    // 檢查商品是否已售出
    const favorite = allFavorites.find(f => f.proId === proId);

    if (favorite && favorite.productStatus === '已售出') {
        alert('此商品已售出');
        return;
    }

    // 儲存商品 ID 到 sessionStorage，供 store.html 使用
    sessionStorage.setItem('viewProductId', proId);

    // 跳轉到商品頁面
    window.location.href = '/store';
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

// ========================================
// 全域函數暴露
// ========================================

window.removeFavoriteProduct = removeFavoriteProduct;
window.toggleFavorite = toggleFavorite;
window.checkFavorited = checkFavorited;
window.viewProductDetail = viewProductDetail;
