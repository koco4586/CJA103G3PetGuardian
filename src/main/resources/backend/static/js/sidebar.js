/**
 * Sidebar 模組：自動生成側邊欄並控制行為
 */

// 將 toggleSidebar 放在全域，讓 HTML 中的 onclick="toggleSidebar()" 找得到
window.toggleSidebar = function () {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('overlay');
    if (sidebar && overlay) {
        sidebar.classList.toggle('active');
        overlay.classList.toggle('active');
    }
};

document.addEventListener("DOMContentLoaded", function () {
    // 1. 定義側邊欄的 HTML 結構
    const sidebarHTML = `
        <div class="sidebar-overlay" id="overlay" onclick="toggleSidebar()"></div>
        <nav id="sidebar">
            <div class="logo">
                <a href="index.html" style="display:flex; align-items:center; gap:10px; text-decoration:none; color:inherit;">
                    <img src="../images/logo.png" alt="Pet Guardian Logo" style="width:32px; height:32px; object-fit:contain;">
                    <strong>Admin</strong>
                </a>
            </div>
            <ul>
                <li data-page="index"><a href="index.html"><i class="fa-solid fa-gauge-high"></i> 儀表板</a></li>
                <li data-page="members"><a href="members.html"><i class="fa-solid fa-users"></i> 會員管理</a></li>
                <li data-page="sitters"><a href="sitters.html"><i class="fa-solid fa-paw"></i> 保母系統</a></li>
                <li data-page="bookings"><a href="bookings.html"><i class="fa-regular fa-calendar-check"></i> 預約管理</a></li>
                <li data-page="chat"><a href="chat.html"><i class="fa-solid fa-comments"></i> 聊天監控</a></li>
                <li data-page="reviews"><a href="reviews.html"><i class="fa-solid fa-star"></i> 評價管理</a></li>
                <li data-page="news"><a href="news.html"><i class="fa-solid fa-bullhorn"></i> 活動/文章</a></li>
                <li data-page="market"><a href="market.html"><i class="fa-solid fa-store"></i> 二手商城</a></li>
                <li data-page="forum"><a href="forum.html"><i class="fa-solid fa-message"></i> 討論區管理</a></li>
                <li data-page="accounts"><a href="accounts.html"><i class="fa-solid fa-user-lock"></i> 管理員帳號</a></li>
            </ul>
        </nav>
    `;

    // 2. 插入到 body 的最前面
    document.body.insertAdjacentHTML('afterbegin', sidebarHTML);

    // 3. 自動偵測目前的頁面，並設定 active 狀態
    const currentPath = window.location.pathname.split("/").pop() || "index.html";
    const pageName = currentPath.split(".")[0];
    const activeItem = document.querySelector(`[data-page="${pageName}"]`);
    if (activeItem) {
        activeItem.classList.add("active");
    }
});

// 4. 監聽視窗縮放：當視窗變大時自動關閉手機版開啟狀態
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