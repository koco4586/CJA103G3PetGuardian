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
        const rating = evalBox.querySelector('.dynamic-stars').getAttribute('data-rating') || 0;

        // 檢查是否有填寫內容
        if (textarea.value.trim().length > 0 || rating > 0) {
            const keepContent = confirm('您的評價尚未送出，是否要保留內容？\n\n點擊「確定」保留內容（下次展開時可繼續編輯）\n點擊「取消」清空內容');

            if (keepContent) {
                // 保留內容，只收起
                evalBox.classList.remove('active');
            } else {
                // 不保留，移除元素
                evalBox.classList.remove('active');
                setTimeout(() => evalBox.remove(), 500);
            }
        } else {
            // 沒有內容，直接移除
            evalBox.classList.remove('active');
            setTimeout(() => evalBox.remove(), 500);
        }
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
            const content = evalBox.querySelector('.eval-content').value.trim();
            const selectedTags = Array.from(evalBox.querySelectorAll('.eval-tag.selected'))
                .map(t => t.innerText);

            if (rating == 0) return alert('❌ 請先評分！');
            if (!content) return alert('❌ 請輸入內容！');

            // 確認提示
            if (!confirm('確定要送出評價嗎？')) return;

            // 合併標籤與內容
            const fullContent = (selectedTags.length > 0 ? `[${selectedTags.join(',')}] ` : '') + content;
            sendReviewToBackend(orderId, sitterId, fullContent, 1, rating, evalBox);
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

    // 獲取 Context Path (若 HTML 沒定義則設為空字串)
    let base = typeof contextPath !== 'undefined' ? contextPath : '';
    if (base === '/') base = '';

    fetch(base + '/pet/evaluate/save', {
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

/**
 * 動態插入保母評價會員的輸入框
 * @param {HTMLElement} button - 觸發按鈕
 * @param {number} orderId - 訂單編號
 * @param {number} memberId - 會員編號
 */
window.injectSitterEvalBox = function (button, orderId, memberId) {
    const parentCard = button.closest('.booking-card');

    // 檢查是否已經有輸入框（在訂單卡片之後）
    let evalBox = parentCard.nextElementSibling;
    if (evalBox && !evalBox.classList.contains('dynamic-eval-wrapper')) {
        evalBox = null;
    }

    // 如果已經展開，則收合
    if (evalBox && evalBox.classList.contains('active')) {
        const textarea = evalBox.querySelector('.eval-content');
        const rating = evalBox.querySelector('.dynamic-stars').getAttribute('data-rating') || 0;

        // 檢查是否有填寫內容
        if (textarea.value.trim().length > 0 || rating > 0) {
            const keepContent = confirm('您的評價尚未送出，是否要保留內容？\n\n點擊「確定」保留內容（下次展開時可繼續編輯）\n點擊「取消」清空內容');

            if (keepContent) {
                // 保留內容，只收起
                evalBox.classList.remove('active');
            } else {
                // 不保留，移除元素
                evalBox.classList.remove('active');
                setTimeout(() => evalBox.remove(), 500);
            }
        } else {
            // 沒有內容，直接移除
            evalBox.classList.remove('active');
            setTimeout(() => evalBox.remove(), 500);
        }
        return;
    }

    // 如果不存在，則建立新的輸入框
    if (!evalBox) {
        evalBox = document.createElement('div');
        evalBox.className = 'dynamic-eval-wrapper';

        evalBox.innerHTML = `
            <h4 style="color: #7d5a00; margin-bottom: 15px;">您對會員的滿意度為？</h4>
            
            <div class="rating-input dynamic-stars" style="margin-bottom: 15px;">
                <i class="fas fa-star star-btn" data-value="1"></i>
                <i class="fas fa-star star-btn" data-value="2"></i>
                <i class="fas fa-star star-btn" data-value="3"></i>
                <i class="fas fa-star star-btn" data-value="4"></i>
                <i class="fas fa-star star-btn" data-value="5"></i>
                <span class="rating-text" style="font-size: 0.9rem; color: #999; margin-left:10px;">請點擊星等</span>
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

            if (rating == 0) return alert('❌ 請先評分！');
            if (!content.trim()) return alert('❌ 請輸入內容！');

            // 確認提示
            if (!confirm('確定要送出評價嗎？')) return;

            // roleType=0 代表保母評價會員
            sendReviewToBackend(orderId, memberId, content, 0, rating, evalBox);
        };
    }

    // 展開輸入框
    evalBox.classList.add('active');
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
        console.error('找不到指定的元素:', { containerId, iconId });
        return;
    }

    // 取得當前的顯示狀態
    // 檢查元素是否隱藏（display 為 none、或 maxHeight 為 0、或初次載入沒有 inline style）
    const isHidden = container.style.display === 'none' ||
        container.style.maxHeight === '0px' ||
        container.style.maxHeight === '';

    if (isHidden) {
        // 展開：先設為 block 讓瀏覽器計算高度，再設為 maxHeight
        container.style.display = 'block';
        // 強制瀏覽器重繪，確保過渡動畫生效
        void container.offsetHeight;
        container.style.maxHeight = container.scrollHeight + 'px';
        icon.style.transform = 'rotate(180deg)';
    } else {
        // 收起
        container.style.maxHeight = '0px';
        icon.style.transform = 'rotate(0deg)';
        // 動畫結束後（0.5s）設為 none 以避免佈局佔位
        setTimeout(() => {
            if (container.style.maxHeight === '0px') {
                container.style.display = 'none';
            }
        }, 500);
    }
}

/**
 * 【功能2】載入並顯示保姆的歷史評價
 * 程式碼範圍：第 260-310 行
 * @param {string} countSelector - 不使用，保留參數相容性
 * @param {string} sitterName - 保姆名稱 (用於標題顯示)
 */
window.loadAndDisplayReviews = function (sitterId, containerSelector, countSelector, sitterName) {
    let base = typeof contextPath !== 'undefined' ? contextPath : '';
    if (base === '/') base = '';
    fetch(base + `/pet/evaluate/list/${sitterId}`)
        .then(res => res.json())
        .then(reviews => {
            // 更新標題顯示 "XXX 的歷史評價"
            if (sitterName) {
                const reviewsSection = document.getElementById('reviews');
                if (reviewsSection) {
                    const h3 = reviewsSection.querySelector('h3');
                    if (h3) {
                        // 計算平均星數
                        const avg = calculateAvgRating(reviews);
                        // 保留 icon 和筆數 span，只改文字
                        const safeName = (sitterName && sitterName !== '""' && sitterName !== "''") ? sitterName : '保母';
                        h3.innerHTML = `
                            <i class="fas fa-comments"></i> ${safeName} 的歷史評價
                            <span style="color: #999; font-size: 0.9rem; margin-left: 10px;">
                                (共 <span id="sitterReviewCount">${reviews.length}</span> 筆)
                            </span>
                            <i id="toggleIcon" class="fas fa-chevron-down" style="float: right; transition: transform 0.3s;"></i>
                            <span class="avg-rating" style="float: right; margin-right: 15px; color: #f39c12; font-weight: bold;">
                                <i class="fas fa-star"></i> ${avg}
                            </span>
                        `;
                    }
                }
            } else {
                // 原有的總筆數更新邏輯 (作為 fallback)
                const allSpans = document.querySelectorAll('span');
                allSpans.forEach(span => {
                    const text = span.textContent;
                    if (text.includes('共') && text.includes('筆')) {
                        const parentH3 = span.closest('h3');
                        if (parentH3) {
                            const numberSpan = parentH3.querySelector('span span');
                            if (numberSpan) {
                                numberSpan.textContent = reviews.length;
                            }
                        }
                    }
                });
            }

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

            // 如果評價數量 >= 10，使用分頁功能
            if (reviews.length >= 10) {
                // 建立分頁結構
                container.innerHTML = `
                    <div id="reviewsItemsContainer"></div>
                    <div id="reviewsPaginationContainer" style="text-align: center; margin-top: 1.5rem; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;"></div>
                `;

                initPagination(
                    reviews,
                    10,
                    'reviewsItemsContainer',
                    'reviewsPaginationContainer',
                    function (review) {
                        const stars = renderStars(review.starRating || 0);
                        const reviewerName = review.senderName || `會員 ${review.senderId}`;
                        const { tags, plainContent } = parseEvaluationContent(review.content);
                        const tagsHtml = renderTagsVertical(tags);

                        return `
                            <div class="review-card" style="border-bottom: 1px solid #eee; padding: 1.5rem 1.2rem;">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                    <div style="flex: 1; min-width: 0; padding-right: 20px; display: flex; flex-direction: column; gap: 8px;">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <strong style="font-size: 1.1rem; color: #2c3e50;">${reviewerName}</strong>
                                            <button class="btn btn-sm btn-outline-danger" 
                                                style="padding: 2px 8px; font-size: 0.8rem; border-radius: 4px;"
                                                onclick="reportReview(this, ${review.bookingOrderId})">
                                                <i class="fas fa-flag"></i> 檢舉
                                            </button>
                                        </div>
                                        <div>
                                            <p style="margin: 0; color: #555; line-height: 1.6; word-break: break-all;">
                                                ${plainContent || '無評論內容'}
                                            </p>
                                        </div>
                                        <div>
                                            <small style="color: #999;">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                                        </div>
                                    </div>
                                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-top: 5px;">
                                        <div style="color: #ffc107; font-size: 1.1rem; margin-bottom: 5px;">
                                            ${stars}
                                        </div>
                                        ${tagsHtml}
                                    </div>
                                </div>
                            </div>
                        `;
                    }
                );
            } else {
                // 不足10筆，直接全部顯示，不使用分頁
                reviews.forEach(review => {
                    const stars = renderStars(review.starRating || 0);
                    const reviewerName = review.senderName || `會員 ${review.senderId}`;
                    const { tags, plainContent } = parseEvaluationContent(review.content);
                    const tagsHtml = renderTagsVertical(tags);

                    const card = document.createElement('div');
                    card.innerHTML = `
                        <div class="review-card" style="border-bottom: 1px solid #eee; padding: 1.5rem 0;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                <div style="flex: 1; min-width: 0; padding-right: 20px; display: flex; flex-direction: column; gap: 8px;">
                                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 12px;">
                                        <strong style="font-size: 1.1rem; color: #2c3e50;">${reviewerName}</strong>
                                        <button class="btn btn-sm btn-outline-danger" 
                                            style="padding: 2px 8px; font-size: 0.8rem; border-radius: 4px;"
                                            onclick="reportReview(this, ${review.bookingOrderId})">
                                            <i class="fas fa-flag"></i> 檢舉
                                        </button>
                                    </div>
                                    <p style="margin: 0; color: #555; line-height: 1.6; word-break: break-all;">
                                        ${plainContent || '無評論內容'}
                                    </p>
                                    <small style="color: #999;">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                                </div>
                                <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-top: 5px;">
                                    <div style="color: #ffc107; font-size: 1.1rem; margin-bottom: 5px;">
                                        ${stars}
                                    </div>
                                    ${tagsHtml}
                                </div>
                            </div>
                        </div>
                    `;
                    container.appendChild(card);
                });
            }
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
 * @param {string} sitterName - 保姆名稱 (用於標題顯示)
 * 
 * 佈局說明：
 * - 星星在檢舉按鈕旁邊
 * - 日期在最右邊
 * - 自動找到並替換現有的 Thymeleaf 評價區塊
 */
window.loadAndDisplayReviewsForDashboard = function (sitterId, containerSelector, sitterName) {
    console.log('🔍 開始載入保母主頁評價，保姆 ID:', sitterId);

    let base = typeof contextPath !== 'undefined' ? contextPath : '';
    if (base === '/') base = '';
    fetch(base + `/pet/evaluate/list/${sitterId}`)
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

            // 修改標題，加入人名、總筆數和收放圖示
            const h3 = reviewsCard.querySelector('h3');
            if (h3) {
                const avg = calculateAvgRating(reviews);
                const safeName = (sitterName && sitterName !== '""' && sitterName !== "''") ? sitterName : '保母';
                h3.style.cursor = 'pointer';
                h3.style.userSelect = 'none';
                h3.innerHTML = `
                    <i class="fas fa-comments"></i> ${safeName} 的歷史評價
                    <span style="color: #999; font-size: 0.9rem; margin-left: 10px;">
                        (共 <span id="dashboardReviewCount">${reviews.length}</span> 筆)
                    </span>
                    <i id="dashboardToggleIcon" class="fas fa-chevron-down" style="float: right; transition: transform 0.3s;"></i>
                    <span class="avg-rating" style="float: right; margin-right: 15px; color: #f39c12; font-weight: bold;">
                        <i class="fas fa-star"></i> ${avg}
                    </span>
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
                    container.style.cssText = 'max-height: 0; overflow: hidden; transition: max-height 0.5s ease;';
                    thymeleafContainer.replaceWith(container);
                } else {
                    // 如果找不到，就在 h3 後面插入
                    container = document.createElement('div');
                    container.setAttribute('data-reviews-container', 'true');
                    container.style.cssText = 'max-height: 0; overflow: hidden; transition: max-height 0.5s ease;';
                    h3.insertAdjacentElement('afterend', container);
                }
            }

            // 強制設定 ID 以確保與 onclick 邏輯匹配
            container.id = 'dashboardReviewsList';
            container.style.display = 'none'; // 預設隱藏
            container.style.maxHeight = '0px';

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

            // 如果評價數量 >= 10，使用分頁功能
            if (reviews.length >= 10) {
                // 建立分頁結構
                container.innerHTML = `
                    <div id="dashboardReviewsItems"></div>
                    <div id="dashboardReviewsPagination" style="text-align: center; margin-top: 1.5rem; display: flex; gap: 8px; justify-content: center; flex-wrap: wrap;"></div>
                `;

                initPagination(
                    reviews,
                    10,
                    'dashboardReviewsItems',
                    'dashboardReviewsPagination',
                    function (review) {
                        const stars = renderStars(review.starRating || 0);
                        const reviewerName = review.senderName || `會員 ${review.senderId}`;
                        const { tags, plainContent } = parseEvaluationContent(review.content);
                        const tagsHtml = renderTagsVertical(tags);

                        return `
                            <div style="border-bottom: 1px solid #eee; padding: 1.5rem 1.2rem;">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                    <div style="flex: 1; min-width: 0; padding-right: 20px; display: flex; flex-direction: column; gap: 8px;">
                                        <div style="display: flex; align-items: center; gap: 10px;">
                                            <strong style="font-size: 1.1rem; color: #2c3e50;">${reviewerName}</strong>
                                            <button class="btn btn-sm btn-outline-danger" 
                                                style="padding: 2px 8px; font-size: 0.8rem; border-radius: 4px;"
                                                onclick="reportReview(this, ${review.bookingOrderId})">
                                                <i class="fas fa-flag"></i> 檢舉
                                            </button>
                                        </div>
                                        <div>
                                            <p style="margin: 0; color: #555; line-height: 1.6; word-break: break-all;">
                                                ${plainContent || '無評論內容'}
                                            </p>
                                        </div>
                                        <div>
                                            <small style="color: #999;">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                                        </div>
                                    </div>
                                    <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-top: 5px;">
                                        <div style="color: #ffc107; font-size: 1.1rem; margin-bottom: 5px;">
                                            ${stars}
                                        </div>
                                        ${tagsHtml}
                                    </div>
                                </div>
                            </div>
                        `;
                    }
                );
            } else {
                // 不足10筆，直接全部顯示，不使用分頁
                reviews.forEach(review => {
                    const stars = renderStars(review.starRating || 0);
                    const reviewerName = review.senderName || `會員 ${review.senderId}`;
                    const { tags, plainContent } = parseEvaluationContent(review.content);
                    const tagsHtml = renderTagsVertical(tags);

                    const card = document.createElement('div');
                    card.innerHTML = `
                        <div style="border-bottom: 1px solid #eee; padding: 1.5rem 1.2rem;">
                            <div style="display: flex; justify-content: space-between; align-items: flex-start;">
                                <div style="flex: 1; min-width: 0; padding-right: 20px; display: flex; flex-direction: column; gap: 8px;">
                                    <div style="display: flex; align-items: center; gap: 10px;">
                                        <strong style="font-size: 1.1rem; color: #2c3e50;">${reviewerName}</strong>
                                        <button class="btn btn-sm btn-outline-danger" 
                                            style="padding: 2px 8px; font-size: 0.8rem; border-radius: 4px;"
                                            onclick="reportReview(this, ${review.bookingOrderId})">
                                            <i class="fas fa-flag"></i> 檢舉
                                        </button>
                                    </div>
                                    <div>
                                        <p style="margin: 0; color: #555; line-height: 1.6; word-break: break-all;">
                                            ${plainContent || '無評論內容'}
                                        </p>
                                    </div>
                                    <div>
                                        <small style="color: #999;">${new Date(review.createTime).toLocaleDateString('zh-TW')}</small>
                                    </div>
                                </div>
                                <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 8px; margin-top: 5px;">
                                    <div style="color: #ffc107; font-size: 1.1rem; margin-bottom: 5px;">
                                        ${stars}
                                    </div>
                                    ${tagsHtml}
                                </div>
                            </div>
                        </div>
                    `;
                    container.appendChild(card);
                });
            }
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

/**
 * 顯示會員被保母評價的歷史紀錄
 * @param {number} memberId - 會員 ID
 * @param {string} memberName - 會員名稱
 * @param {HTMLElement} buttonElement - 觸發按鈕元素
 */
window.loadMemberReviews = function (memberId, memberName, buttonElement) {
    const parentCard = buttonElement.closest('.booking-card');

    // 檢查是否已經展開評論區塊
    let reviewBox = parentCard.nextElementSibling;
    if (reviewBox && reviewBox.classList.contains('member-review-box')) {
        // 如果已展開，則收合並移除
        reviewBox.remove();
        return;
    }

    let base = typeof contextPath !== 'undefined' ? contextPath : '';
    if (base === '/') base = '';
    fetch(base + `/pet/evaluate/member/${memberId}`)
        .then(res => res.json())
        .then(reviews => {
            console.log('📦 收到會員評價資料:', reviews);

            // 建立評論顯示區塊
            reviewBox = document.createElement('div');
            reviewBox.className = 'member-review-box';

            // 建立標題
            let headerHTML = `
                <h4>
                    <i class="fas fa-user-circle"></i> ${memberName} 的歷史評價
                    <span style="color: #999; font-size: 0.9rem; margin-left: 10px;">(共 ${reviews.length} 筆)</span>
                </h4>
            `;

            // 建立評價列表
            let reviewsHTML = '<div class="reviews-container">';

            if (reviews.length === 0) {
                reviewsHTML += `
                    <div style="text-align: center; padding: 2rem; color: #999;">
                        <i class="far fa-comment-dots fa-2x" style="margin-bottom: 0.5rem; display: block;"></i>
                        <p>目前尚無評價紀錄</p>
                    </div>
                `;
            } else {
                reviews.forEach(review => {
                    const stars = renderStars(review.starRating || 0);
                    const reviewDate = new Date(review.createTime).toLocaleDateString('zh-TW');

                    reviewsHTML += `
                        <div class="review-card">
                            <div class="review-header">
                                <div>
                                    <strong>保母 ${review.senderId}</strong>
                                </div>
                                <div style="color: #ffc107;">
                                    ${stars}
                                </div>
                            </div>
                            <p class="mb-0 text-muted">${review.content || ''}</p>
                            <small class="text-muted">${reviewDate}</small>
                        </div>
                    `;
                });
            }

            reviewsHTML += '</div>';

            // 組合完整 HTML
            reviewBox.innerHTML = headerHTML + reviewsHTML;

            // 插入到訂單卡片之後
            parentCard.insertAdjacentElement('afterend', reviewBox);
        })
        .catch(err => {
            console.error('載入會員評價失敗:', err);
            alert('❌ 載入評價失敗，請稍後再試');
        });
}

// ========================================
// 輔助功能：解析與渲染評價標籤
// ========================================

function parseEvaluationContent(content) {
    if (!content) return { tags: [], plainContent: '' };

    // 匹配格式: [標籤1,標籤2] 實際評價內容
    // 改良 regex 支援沒有內容的情況
    const match = content.match(/^\[(.*?)\]\s?(.*)$/);
    if (match) {
        return {
            tags: match[1].split(',').map(t => t.trim()).filter(t => t),
            plainContent: match[2].trim()
        };
    }
    return { tags: [], plainContent: content };
}

function renderTagsVertical(tags) {
    if (!tags || tags.length === 0) return '';

    // 使用 Grid 佈局，實現每三個標籤為一個垂直列 (Column) 的立體排列效果
    // grid-template-rows: repeat(3, auto) 限制每列最多 3 個
    // grid-auto-flow: column 讓超過 3 個的標籤自動排到左邊的新列
    return `
        <div style="
            display: grid;
            grid-template-rows: repeat(3, auto);
            grid-auto-flow: column;
            gap: 6px 12px;
            justify-content: end;
            margin-top: 5px;
        ">
            ${tags.map(tag => `
                <span style="
                    background: #fff9db; 
                    color: #f08c00; 
                    font-size: 0.75rem; 
                    padding: 2px 10px; 
                    border-radius: 4px; 
                    border: 1px solid #ffe066;
                    white-space: nowrap;
                    display: inline-block;
                    text-align: center;
                ">${tag}</span>
            `).join('')}
        </div>
    `;
}

