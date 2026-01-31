// ========================================
// 評價輸入功能區塊
// ========================================

/**
 * 初始化星星評分功能
 * @param {HTMLElement} container - 包含星星的容器元素
 */
function initStarRating(container) {
    const stars = container.querySelectorAll('.star-btn');
    const ratingText = container.querySelector('.rating-text') || document.getElementById('rating-text');
    let selectedRating = 0;

    stars.forEach(star => {
        // 點擊邏輯
        star.addEventListener('click', function () {
            const clickedValue = parseInt(this.getAttribute('data-value'));
            if (selectedRating === clickedValue) {
                selectedRating = 0;
            } else {
                selectedRating = clickedValue;
            }
            updateStars(selectedRating);
            container.setAttribute('data-rating', selectedRating);
        });

        // 移入預覽
        star.addEventListener('mouseover', function () {
            updateStars(this.getAttribute('data-value'));
        });
    });

    // 移出恢復
    container.addEventListener('mouseleave', function () {
        updateStars(selectedRating);
    });

    function updateStars(val) {
        stars.forEach(s => {
            const v = parseInt(s.getAttribute('data-value'));
            s.style.color = v <= val ? '#f39c12' : '#ddd';
        });
        if (val > 0) {
            ratingText.innerText = val + ' 顆星';
        } else {
            ratingText.innerText = '請點擊星等';
        }
    }
}

/**
 * 動態插入評價輸入框
 * @param {HTMLElement} button - 觸發按鈕
 * @param {number} orderId - 訂單編號
 * @param {number} sitterId - 保姆編號
 */
window.injectEvalBox = function (button, orderId, sitterId) {
    const parentCard = button.closest('.booking-card');

    // 檢查是否已經有輸入框（在訂單卡片之後）
    let evalBox = parentCard.nextElementSibling;
    if (evalBox && !evalBox.classList.contains('dynamic-eval-wrapper')) {
        evalBox = null;
    }

    // 如果已經展開，則收合
    if (evalBox && evalBox.classList.contains('active')) {
        const textarea = evalBox.querySelector('.eval-content');
        if (textarea.value.trim().length > 0) {
            if (!confirm('您的評價尚未送出，確定要先暫時收起來嗎？(內容將會保留)')) {
                return;
            }
        }
        evalBox.classList.remove('active');
        // 延遲移除，讓動畫完成
        setTimeout(() => evalBox.remove(), 500);
        return;
    }

    // 如果不存在，則建立新的輸入框
    if (!evalBox) {
        evalBox = document.createElement('div');
        evalBox.className = 'dynamic-eval-wrapper';

        evalBox.innerHTML = `
            <h4 style="color: #7d5a00; margin-bottom: 15px;">您對保姆的滿意度為？</h4>
            
            <div class="rating-input dynamic-stars" style="margin-bottom: 15px;">
                <i class="fas fa-star star-btn" data-value="1"></i>
                <i class="fas fa-star star-btn" data-value="2"></i>
                <i class="fas fa-star star-btn" data-value="3"></i>
                <i class="fas fa-star star-btn" data-value="4"></i>
                <i class="fas fa-star star-btn" data-value="5"></i>
                <span class="rating-text" style="font-size: 0.9rem; color: #999; margin-left:10px;">請點擊星等</span>
            </div>

            <div class="tag-container" style="margin-bottom: 15px;">
                <span class="eval-tag">態度優良</span>
                <span class="eval-tag">值得信賴</span>
                <span class="eval-tag">準時盡責</span>
                <span class="eval-tag">細心體貼</span>
                <span class="eval-tag">互動良好</span>
                <span class="eval-tag">強烈推薦</span>
            </div>

            <textarea class="eval-content" style="width: 100%; height: 80px; border: 1px solid #ffeaa7; border-radius: 8px; padding: 10px; background:#fff; font-size: 1rem;" placeholder="輸入評論..."></textarea>
            
            <div style="text-align: right; margin-top: 15px;">
                <button class="cancel-eval-btn" style="background: #95a5a6; color: white; border: none; padding: 10px 25px; border-radius: 50px; cursor: pointer; font-weight: bold; margin-right: 10px; transition: all 0.2s;">
                    <i class="fas fa-times"></i> 取消
                </button>
                <button class="submit-paw-btn">
                    提交評論 <i class="fas fa-paw paw-icon"></i>
                </button>
            </div>
        `;

        // 插入到訂單卡片之後（不是內部）
        parentCard.insertAdjacentElement('afterend', evalBox);

        // 初始化星星邏輯
        initStarRating(evalBox.querySelector('.dynamic-stars'));

        // 標籤點擊邏輯：只記錄，不顯示在輸入框
        evalBox.querySelectorAll('.eval-tag').forEach(tag => {
            tag.onclick = function () {
                this.classList.toggle('selected');
            };
        });

        // 狗腳印移入移出效果
        const submitBtn = evalBox.querySelector('.submit-paw-btn');

        submitBtn.addEventListener('mouseenter', function () {
            const pawIcon = this.querySelector('.paw-icon');
            if (pawIcon) {
                pawIcon.outerHTML = '<i class="fas fa-paw"></i><i class="fas fa-paw"></i>';
            }
        });

        submitBtn.addEventListener('mouseleave', function () {
            const icons = this.querySelectorAll('.fas.fa-paw');
            if (icons.length > 1) {
                icons[1].remove();
                icons[0].classList.add('paw-icon');
            }
        });

        // 取消按鈕
        evalBox.querySelector('.cancel-eval-btn').onclick = function () {
            evalBox.classList.remove('active');
            // 延遲移除，讓動畫完成
            setTimeout(() => evalBox.remove(), 500);
        };

        // 提交按鈕
        submitBtn.onclick = function () {
            const rating = evalBox.querySelector('.dynamic-stars').getAttribute('data-rating') || 0;
            const content = evalBox.querySelector('.eval-content').value;
            const selectedTags = Array.from(evalBox.querySelectorAll('.eval-tag.selected'))
                .map(t => t.innerText).join(',');

            if (rating == 0) return alert('❌ 請先評分！');
            if (!content.trim()) return alert('❌ 請輸入內容！');

            // 確認提示
            if (!confirm('確定要送出評價嗎？')) return;

            sendReviewToBackend(orderId, sitterId, content, 1, rating, evalBox);
        };
    }

    // 展開輸入框
    evalBox.classList.add('active');
}

/**
 * 送出評價到後端
 * @param {number} orderId - 訂單編號
 * @param {number} receiverId - 接收者編號（保姆或會員）
 * @param {string} content - 評價內容
 * @param {number} roleType - 角色類型（1=會員評保姆, 0=保姆評會員）
 * @param {number} rating - 星等評分
 * @param {HTMLElement} evalBox - 評價輸入框元素（用於送出後移除）
 */
function sendReviewToBackend(orderId, receiverId, content, roleType, rating, evalBox) {
    const data = {
        bookingOrderId: orderId,
        receiverId: receiverId,
        content: content,
        roleType: roleType,
        starRating: rating
    };

    fetch('/pet/evaluate/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(res => res.text())
        .then(msg => {
            if (msg === 'success') {
                alert('✅ 評價已送出！');
                // 移除輸入框
                if (evalBox) {
                    evalBox.classList.remove('active');
                    setTimeout(() => evalBox.remove(), 500);
                }
                // 留在原頁面並重新載入
                window.location.reload();
            } else {
                alert('❌ 送出失敗');
            }
        })
        .catch(err => {
            console.error('錯誤:', err);
            alert('❌ 發生錯誤');
        });
}


// ========================================
// 歷史評價功能區塊
// ========================================
// 功能說明：
// 1. toggleHistoryReviews() - 收放歷史評價列表（展開/收起）
// 2. calculateAvgRating() - 計算平均星數
// 3. initPagination() - 初始化分頁功能（每頁10筆）
// 4. renderStars() - 顯示星星評分（唯讀）
// 5. displayReviewCount() - 顯示總評價數量（例如：歷史評價 (10)）
// ========================================

/**
 * 【功能1】收放歷史評價列表
 * 程式碼範圍：第 230-260 行
 * @param {string} containerId - 容器元素的 ID
 * @param {string} iconId - 箭頭圖示元素的 ID
 */
window.toggleHistoryReviews = function (containerId, iconId) {
    const container = document.getElementById(containerId);
    const icon = document.getElementById(iconId);

    if (!container || !icon) {
        console.error('找不到指定的元素');
        return;
    }

    // 切換展開/收起
    if (container.style.maxHeight === '0px' || container.style.maxHeight === '') {
        container.style.maxHeight = container.scrollHeight + 'px';
        icon.style.transform = 'rotate(180deg)';
    } else {
        container.style.maxHeight = '0px';
        icon.style.transform = 'rotate(0deg)';
    }
}

/**
 * 【功能2】載入並顯示保姆的歷史評價
 * 程式碼範圍：第 260-310 行
 * @param {number} sitterId - 保姆 ID
 * @param {string} containerSelector - 評價列表容器的選擇器（例如：'#reviewsList'）
 * @param {string} countSelector - 總筆數顯示元素的選擇器（例如：'span' 或 null）
 */
window.loadAndDisplayReviews = function (sitterId, containerSelector, countSelector) {
    fetch(`/pet/evaluate/list/${sitterId}`)
        .then(res => res.json())
        .then(reviews => {
            // 更新總筆數 - 找到包含 "共" 和 "筆" 的 span 元素
            const allSpans = document.querySelectorAll('span');
            allSpans.forEach(span => {
                const text = span.textContent;
                if (text.includes('共') && text.includes('筆')) {
                    // 找到父元素中的數字 span 並更新
                    const parentH3 = span.closest('h3');
                    if (parentH3) {
                        const numberSpan = parentH3.querySelector('span span');
                        if (numberSpan) {
                            numberSpan.textContent = reviews.length;
                        }
                    }
                }
            });

            // 動態生成評價卡片
            const container = document.querySelector(containerSelector);
            if (!container) {
                console.error('找不到評價容器:', containerSelector);
                return;
            }

            container.innerHTML = '';

            if (reviews.length === 0) {
                container.innerHTML = '<p class="text-center text-muted">目前尚無評價紀錄</p>';
                return;
            }

            reviews.forEach(review => {
                const stars = renderStars(review.starRating || 0);
                const card = document.createElement('div');
                card.className = 'review-card';
                card.innerHTML = `
                    <div class="review-header">
                        <div>
                            <strong>會員 ${review.senderId}</strong>
                            <button class="btn btn-sm btn-outline-danger ms-2"
                                style="padding: 0px 6px; font-size: 0.8rem;"
                                onclick="reportReview(${review.bookingOrderId})">
                                <i class="fas fa-flag"></i> 檢舉
                            </button>
                        </div>
                        <div style="color: #ffc107;">
                            ${stars}
                        </div>
                    </div>
                    <p class="mb-0 text-muted">${review.content || ''}</p>
                    <small class="text-muted">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                `;
                container.appendChild(card);
            });
        })
        .catch(err => {
            console.error('載入評價失敗:', err);
        });
}

/**
 * 【功能3】載入並顯示保母主頁的歷史評價（特殊佈局）
 * 程式碼範圍：第 328-410 行
 * @param {number} sitterId - 保姆 ID
 * @param {string} containerSelector - 不使用，保留參數相容性
 * 
 * 佈局說明：
 * - 星星在檢舉按鈕旁邊
 * - 日期在最右邊
 * - 自動找到並替換現有的 Thymeleaf 評價區塊
 */
window.loadAndDisplayReviewsForDashboard = function (sitterId, containerSelector) {
    console.log('🔍 開始載入保母主頁評價，保姆 ID:', sitterId);

    fetch(`/pet/evaluate/list/${sitterId}`)
        .then(res => {
            console.log('📡 API 回應狀態:', res.status);
            return res.json();
        })
        .then(reviews => {
            console.log('📦 收到評價資料:', reviews);
            console.log('📊 評價數量:', reviews.length);

            // 找到歷史評價卡片 (id="reviews-card")
            const reviewsCard = document.getElementById('reviews-card');
            console.log('🎴 找到評價卡片:', reviewsCard);

            if (!reviewsCard) {
                console.error('❌ 找不到歷史評價卡片 (#reviews-card)');
                return;
            }

            // 修改標題，加入總筆數和收放圖示
            const h3 = reviewsCard.querySelector('h3');
            if (h3) {
                h3.style.cursor = 'pointer';
                h3.style.userSelect = 'none';
                h3.innerHTML = `
                    <i class="fas fa-comments"></i> 歷史評價
                    <span style="color: #999; font-size: 0.9rem; margin-left: 10px;">
                        (共 <span id="dashboardReviewCount">${reviews.length}</span> 筆)
                    </span>
                    <i id="dashboardToggleIcon" class="fas fa-chevron-down" style="float: right; transition: transform 0.3s;"></i>
                `;

                // 綁定點擊事件
                h3.onclick = function () {
                    toggleHistoryReviews('dashboardReviewsList', 'dashboardToggleIcon');
                };
            }

            // 找到或建立評價容器
            let container = reviewsCard.querySelector('[data-reviews-container]');
            if (!container) {
                // 找到 Thymeleaf 渲染的評價區塊並替換
                const thymeleafContainer = reviewsCard.querySelector('div[th\\:if]');
                if (thymeleafContainer) {
                    container = document.createElement('div');
                    container.setAttribute('data-reviews-container', 'true');
                    container.id = 'dashboardReviewsList';
                    container.style.cssText = 'max-height: 0; overflow: hidden; transition: max-height 0.5s ease;';
                    thymeleafContainer.replaceWith(container);
                } else {
                    // 如果找不到，就在 h3 後面插入
                    container = document.createElement('div');
                    container.setAttribute('data-reviews-container', 'true');
                    container.id = 'dashboardReviewsList';
                    container.style.cssText = 'max-height: 0; overflow: hidden; transition: max-height 0.5s ease;';
                    h3.insertAdjacentElement('afterend', container);
                }
            }

            container.innerHTML = '';

            if (reviews.length === 0) {
                container.innerHTML = `
                    <div style="text-align: center; color: #999; padding: 2rem;">
                        <i class="far fa-comment-dots fa-2x" style="margin-bottom: 0.5rem; display: block;"></i>
                        <p>目前尚無評價紀錄</p>
                    </div>
                `;
                return;
            }

            reviews.forEach(review => {
                const stars = renderStars(review.starRating || 0);
                const card = document.createElement('div');
                card.style.cssText = 'border-bottom: 1px solid #eee; padding: 1rem 0; display: flex; flex-direction: column; gap: 0.5rem;';
                card.innerHTML = `
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <strong>會員 ${review.senderId}</strong>
                            <button class="btn btn-sm btn-outline-danger"
                                style="padding: 0px 6px; font-size: 0.8rem;"
                                onclick="reportReview(${review.bookingOrderId})">
                                <i class="fas fa-flag"></i> 檢舉
                            </button>
                            <span style="color: #ffc107;">
                                ${stars}
                            </span>
                        </div>
                        <small style="color: #999;">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                    </div>
                    <p style="margin: 0; color: #555; line-height: 1.6;">${review.content || ''}</p>
                `;
                container.appendChild(card);
            });
        })
        .catch(err => {
            console.error('載入評價失敗:', err);
        });
}

/**
 * 計算平均星數
 * @param {Array} reviews - 評價陣列，每個元素需包含 starRating 屬性
 * @returns {number} 平均星數（保留一位小數）
 */
window.calculateAvgRating = function (reviews) {
    if (!reviews || reviews.length === 0) return 0;

    const total = reviews.reduce((sum, review) => sum + (review.starRating || 0), 0);
    return (total / reviews.length).toFixed(1);
}

/**
 * 初始化分頁功能
 * @param {Array} items - 要分頁的項目陣列
 * @param {number} pageSize - 每頁顯示數量
 * @param {string} containerId - 顯示項目的容器 ID
 * @param {string} paginationId - 分頁按鈕容器 ID
 * @param {Function} renderItem - 渲染單個項目的函數
 */
window.initPagination = function (items, pageSize, containerId, paginationId, renderItem) {
    let currentPage = 1;
    const totalPages = Math.ceil(items.length / pageSize);

    function renderPage(page) {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.innerHTML = '';
        const start = (page - 1) * pageSize;
        const end = start + pageSize;
        const pageItems = items.slice(start, end);

        pageItems.forEach(item => {
            container.innerHTML += renderItem(item);
        });

        renderPaginationButtons(page);
    }

    function renderPaginationButtons(page) {
        const pagination = document.getElementById(paginationId);
        if (!pagination) return;

        pagination.innerHTML = '';

        // 上一頁按鈕
        const prevBtn = document.createElement('button');
        prevBtn.textContent = '上一頁';
        prevBtn.disabled = page === 1;
        prevBtn.onclick = () => {
            currentPage--;
            renderPage(currentPage);
        };
        pagination.appendChild(prevBtn);

        // 頁碼按鈕
        for (let i = 1; i <= totalPages; i++) {
            const pageBtn = document.createElement('button');
            pageBtn.textContent = i;
            pageBtn.className = i === page ? 'active' : '';
            pageBtn.onclick = () => {
                currentPage = i;
                renderPage(currentPage);
            };
            pagination.appendChild(pageBtn);
        }

        // 下一頁按鈕
        const nextBtn = document.createElement('button');
        nextBtn.textContent = '下一頁';
        nextBtn.disabled = page === totalPages;
        nextBtn.onclick = () => {
            currentPage++;
            renderPage(currentPage);
        };
        pagination.appendChild(nextBtn);
    }

    // 初始化第一頁
    renderPage(1);
}

/**
 * 顯示星星評分（唯讀）
 * @param {number} rating - 星等評分
 * @returns {string} 星星的 HTML 字串
 */
window.renderStars = function (rating) {
    let stars = '';
    for (let i = 1; i <= 5; i++) {
        if (i <= rating) {
            stars += '<i class="fas fa-star" style="color: #f39c12;"></i>';
        } else {
            stars += '<i class="far fa-star" style="color: #ddd;"></i>';
        }
    }
    return stars;
}
