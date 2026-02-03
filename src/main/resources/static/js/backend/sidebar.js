/**
 * Sidebar 模組
 * 自動生成側邊欄並控制行為
 */

// 將 toggleSidebar 放在全域
window.toggleSidebar = function () {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    if (sidebar && overlay) {
        sidebar.classList.toggle('active');
        overlay.classList.toggle('active');
    }
};

// 切換子選單
window.toggleSubmenu = function (e) {
    e.preventDefault();
    const parent = e.currentTarget.parentElement;
    parent.classList.toggle('open');
};

// 管理員登出功能
window.adminLogout = function () {
    window.location.href = "/admin/adminlogoutpage";
};

document.addEventListener("DOMContentLoaded", function () {
    // 定義側邊欄的 HTML 結構
    const sidebarHTML = `
        <div class="sidebar-overlay" id="overlay" onclick="toggleSidebar()"></div>
        <nav id="sidebar">
            <div class="logo">
                <a href="/admin/index" style="display:flex; align-items:center; gap:10px; text-decoration:none; color:inherit;">
                    <img src="../../../images/backend/logo.png" alt="Pet Guardian Logo" style="width:32px; height:32px; object-fit:contain;">
                    <strong>Admin</strong>
                </a>
            </div>
            <ul>
                <li data-page="index"><a href="/admin/index"><i class="fa-solid fa-gauge-high"></i> 首頁</a></li>
                <li data-page="members"><a href="/html/backend/member/admin_member_management.html"><i class="fa-solid fa-users"></i> 會員管理</a></li>
                <li data-page="sitters"><a href="/admin/sitter/manage"><i class="fa-solid fa-paw"></i> 保母系統</a></li>
                <li class="has-submenu" data-page="bookings">
                    <a href="#" onclick="toggleSubmenu(event)">
                        <span><i class="fa-regular fa-calendar-check"></i> 預約管理</span>
                        <i class="fa-solid fa-chevron-down arrow"></i>
                    </a>
                    <ul class="submenu">
                        <li data-page="bookings-all"><a href="/admin/bookings/all"><i class="fa-solid fa-list"></i> 預約列表</a></li>
                        <li data-page="bookings-reviews"><a href="/admin/reviews1"><i class="fa-solid fa-star"></i> 評價管理</a></li>
                    </ul>
                </li>
                <li data-page="chat-reports"><a href="/admin/chat-reports"><i class="fa-solid fa-comment-slash"></i> 聊天檢舉<span class="badge" id="chatReportBadge" style="display:none; margin-left:5px; background: #e74c3c; color: white; padding: 2px 6px; border-radius: 4px; font-size: 10px;">0</span></a></li>
                <li data-page="news"><a href="/admin/news/list"><i class="fa-solid fa-bullhorn"></i> 消息管理</a></li>
                <li class="has-submenu" data-page="market">
                    <a href="#" onclick="toggleSubmenu(event)">
                        <span><i class="fa-solid fa-store"></i> 二手商城</span>
                        <i class="fa-solid fa-chevron-down arrow"></i>
                    </a>
                    <ul class="submenu">
                        <li data-page="market-manage"><a href="/admin/store/manage"><i class="fa-solid fa-box"></i> 商品管理</a></li>
                        <li data-page="market-reviews"><a href="/admin/store-reviews"><i class="fa-solid fa-flag"></i> 評價管理</a></li>
                    </ul>
                </li>
                <li data-page="forum"><a href="/admin/forum/list-all-forum"><i class="fa-solid fa-message"></i> 討論區管理</a></li>
                <li data-page="accounts"><a href="/html/backend/admin/admin_admin_management.html"><i class="fa-solid fa-user-lock"></i> 管理員帳號</a></li>
            </ul>
            
            <div class="sidebar-footer">
                <div class="admin-info">
                    <i class="fa-solid fa-user-shield"></i>
                    <span id="adminName">管理員</span>
                </div>
                <button class="logout-btn" onclick="adminLogout()">
                    <i class="fa-solid fa-right-from-bracket"></i> 登出
                </button>
            </div>
        </nav>
    `;

    // 插入到 body 的最前面
    document.body.insertAdjacentHTML('afterbegin', sidebarHTML);

    // 自動偵測目前的頁面，並設定 active 狀態
    const currentPath = window.location.pathname;

    // 根據路徑判斷當前頁面
    let pageName = "index";

    // 會員管理 - 修正路徑判斷，支援靜態HTML頁面路徑
    if (currentPath.includes("/admin/membermanagementpage") ||
        currentPath.includes("/html/backend/member/admin_member_management")) {
        pageName = "members";
    }
    // 管理員帳號管理 - 修正路徑判斷，支援靜態HTML頁面路徑
    else if (currentPath.includes("/admin/adminmanagementpage") ||
        currentPath.includes("/html/backend/admin/admin_admin_management")) {
        pageName = "accounts";
    }
    // 保母系統
    else if (currentPath.includes("/admin/sitter/manage")) {
        pageName = "sitters";
    }
    // 預約管理 - 評價
    else if (currentPath.includes("/admin/reviews")) {
        pageName = "bookings-reviews";
    }
    // 預約管理 - 列表
    else if (currentPath.includes("/admin/bookings")) {
        pageName = "bookings-all";
    }
    // 聊天檢舉
    else if (currentPath.includes("/admin/chat-reports")) {
        pageName = "chat-reports";
    }
    // 聊天監控
    else if (currentPath.includes("/admin/chat")) {
        pageName = "chat";
    }
    // 消息管理
    else if (currentPath.includes("/admin/news/list")) {
        pageName = "news";
    }
    // 二手商城 - 評價管理
    else if (currentPath.includes("/admin/store-reviews")) {
        pageName = "market-reviews";
    }
    // 二手商城 - 商品管理
    else if (currentPath.includes("/admin/store/manage") || currentPath.includes("/admin/store")) {
        pageName = "market-manage";
    }
    // 討論區管理
    else if (currentPath.includes("/admin/forum/list-all-forum")) {
        pageName = "forum";
    }
    // 首頁 (預設)
    else if (currentPath.includes("/admin/index") || currentPath === "/admin") {
        pageName = "index";
    }

    // 設定 active 狀態
    const activeItem = document.querySelector(`[data-page="${pageName}"]`);
    if (activeItem) {
        activeItem.classList.add("active");
        // 如果是子選單項目，自動展開父選單
        const parentSubmenu = activeItem.closest('.has-submenu');
        if (parentSubmenu) {
            parentSubmenu.classList.add("open");
        }
    }

    // 載入管理員名稱
    loadAdminName();
});

// 載入管理員名稱的函數
function loadAdminName() {
    // 從後端取得當前登入的管理員資訊
    fetch('/admin/current-admin')
        .then(response => response.json())
        .then(data => {
            if (data && data.name) {
                document.getElementById('adminName').textContent = data.name;
            }
        })
        .catch(error => {
            console.log('無法載入管理員資訊');
        });
}

// 監聽視窗縮放，當視窗變大時自動關閉手機版開啟狀態
window.addEventListener('resize', () => {
    if (window.innerWidth > 992) {
        const sidebar = document.getElementById('sidebar');
        const overlay = document.getElementById('overlay');
        if (sidebar && sidebar.classList.contains('active')) {
            sidebar.classList.remove('active');
            overlay.classList.remove('active');
        }
    }
});