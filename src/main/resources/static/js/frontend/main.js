document.addEventListener('DOMContentLoaded', () => {
    // Dynamic Header Injection (Simulating a component system)
    const headerEl = document.getElementById('main-header');
    if (headerEl) {
        headerEl.innerHTML = `
            <div class="container nav">
                <a href="index.html" class="logo" style="display: flex; align-items: center; text-decoration: none;">
                    <img src="/images/frontend/logo.png" alt="PetGuardian" style="height: 40px; margin-right: 0.5rem;">
                    <span style="font-family: 'Rubik', sans-serif; font-weight: 700; color: var(--text-color); font-size: 1.4rem;">PetGuardian</span>
                </a>
                <ul class="nav-links">
                    <li><a href="index.html" class="nav-link">首頁</a></li>
                    <li><a href="services.html" class="nav-link">預約服務</a></li>
                    <li class="dropdown">
                        <a href="store.html" class="nav-link">二手商城 <i class="fas fa-chevron-down" style="font-size: 0.8rem; margin-left: 4px;"></i></a>
                        <div class="dropdown-menu" style="left:0; right:auto; width:160px; top:120%;">
                            <a href="store.html" class="dropdown-item">瀏覽商品</a>
                            <a href="store-seller.html" class="dropdown-item">賣家管理</a>
                        </div>
                    </li>
                    <li><a href="/forum/listAllActiveForum" class="nav-link">討論區</a></li>
                    <li><a href="news.html" class="nav-link">最新消息</a></li>
                </ul>
                <div class="d-flex align-center">
                    <a href="chat.html" class="btn btn-outline" style="border:none; margin-right: 0.5rem; font-size: 1.2rem; position: relative; padding: 0.5rem;">
                        <i class="fas fa-comment-dots"></i>
                        <span style="position: absolute; top: 0; right: 0; width: 10px; height: 10px; background: var(--danger); border-radius: 50%; border: 2px solid white;"></span>
                    </a>
                    <div class="dropdown">
                    <button class="btn btn-primary d-flex align-center gap-sm">
                        <span>我的帳戶</span>
                        <i class="fas fa-chevron-down"></i>
                    </button>
                    <div class="dropdown-menu">
                        <a href="login.html" class="dropdown-item">登入 / 註冊</a>
                        <a href="dashboard.html" class="dropdown-item">會員中心</a>
                        <a href="sitter-dashboard.html" class="dropdown-item">保母專區</a>
                        <hr style="border:0; border-top:1px solid #eee; margin: 0.5rem 0;">
                        <a href="#" class="dropdown-item">登出</a>
                    </div>
                </div>
                <div class="mobile-menu-btn">☰</div>
            </div>
        `;

        // Mobile Menu Logic
        const mobileBtn = headerEl.querySelector('.mobile-menu-btn');
        const navLinks = headerEl.querySelector('.nav-links');
        if (mobileBtn && navLinks) {
            mobileBtn.addEventListener('click', () => {
                navLinks.classList.toggle('active');
            });
        }
    }

    // Dynamic Footer Injection
    const footerEl = document.getElementById('main-footer');
    if (footerEl) {
        footerEl.innerHTML = `
            <div class="container" style="padding: 2rem 0;">
                <div class="d-flex justify-between align-center" style="flex-wrap: wrap; gap: 2rem;">
                    <div>
                        <h3>🐾 PetGuardian 寵護家</h3>
                        <p style="color: #666;">給毛小孩最溫暖的守護</p>
                    </div>
                    <div>
                        <h4>快速連結</h4>
                        <ul style="color: #666;">
                            <li><a href="about.html">關於我們</a></li>
                            <li><a href="terms.html">服務條款</a></li>
                            <li><a href="privacy.html">隱私政策</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4>聯絡我們</h4>
                        <p style="color: #666;">hello@petguardian.com</p>
                        <p style="color: #666;">02-1234-5678</p>
                    </div>
                </div>
                <div class="text-center mt-2" style="border-top: 1px solid #eee; padding-top: 1rem; color: #999; font-size: 0.9rem;">
                    &copy; 2024 PetGuardian. All rights reserved.
                </div>
            </div>
        `;
    }

    // Highlight active nav link
    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });

    console.log('PetGuardian App Initialized 🚀');
});

// Global Favorite Toggle
window.toggleFavorite = function (btn, type = 'general', id = null) {
    const icon = btn.querySelector('i');
    const isSolid = icon.classList.contains('fas');

    if (!isSolid) { // Regular (Empty) -> Solid (Filled)
        icon.classList.remove('far');
        icon.classList.add('fas');
        icon.style.color = 'var(--danger)';
        window.showToast('已加入我的收藏');

        if (type === 'article' && id) {
            saveToStorage('fav_articles', id);
        }
    } else {
        icon.classList.remove('fas');
        icon.classList.add('far');
        icon.style.color = '#ccc';
        window.showToast('已從收藏移除');

        if (type === 'article' && id) {
            removeFromStorage('fav_articles', id);
        }
    }
};

// Storage Helpers
function saveToStorage(key, id) {
    let items = JSON.parse(localStorage.getItem(key) || '[]');
    if (!items.includes(id)) {
        items.push(id);
        localStorage.setItem(key, JSON.stringify(items));
    }
}

function removeFromStorage(key, id) {
    let items = JSON.parse(localStorage.getItem(key) || '[]');
    items = items.filter(item => item !== id);
    localStorage.setItem(key, JSON.stringify(items));
}

// Global Toast System
window.showToast = function (message) {
    const toast = document.createElement('div');
    toast.className = 'pg-toast';
    toast.textContent = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => toast.remove(), 300);
    }, 2000);
};

// Add Global Toast Styles
if (!document.getElementById('pg-toast-styles')) {
    const style = document.createElement('style');
    style.id = 'pg-toast-styles';
    style.textContent = `
        .pg-toast {
            position: fixed;
            bottom: 2rem;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(0,0,0,0.8);
            color: white;
            padding: 0.8rem 1.5rem;
            border-radius: 50px;
            z-index: 10000;
            font-size: 0.9rem;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            animation: toast-in 0.3s ease;
            pointer-events: none;
        }
        .pg-toast.fade-out {
            opacity: 0;
            bottom: 1rem;
            transition: all 0.3s ease;
        }
        @keyframes toast-in {
            from { bottom: 0; opacity: 0; }
            to { bottom: 2rem; opacity: 1; }
        }
    `;
    document.head.appendChild(style);
}

// Remove Favorite logic (visual only for dashboard)
window.removeFavorite = function (btn, type = 'general', id = null) {
    if (confirm('確定要取消收藏嗎？')) {
        const card = btn.closest('.card');
        card.style.opacity = '0';

        if (type === 'article' && id) {
            removeFromStorage('fav_articles', id);
        }

        setTimeout(() => {
            card.remove();
            // Check if grid is empty
            const grid = document.querySelector('.favorites-grid.active');
            if (grid && grid.querySelectorAll('.card').length === 0) {
                grid.innerHTML = '<div class="empty-state" style="width:100%; text-align:center; padding:3rem; color:#adb5bd;">' +
                    '<i class="fas fa-heart-broken mb-1" style="font-size: 2rem;"></i>' +
                    '<p>暫無收藏項目</p></div>';
            }
        }, 300);
    }
};
