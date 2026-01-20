// 切換分頁
function switchTab(tabName) {
    // 移除所有按鈕的 active 樣式
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 隱藏所有區塊
    document.querySelectorAll('.panel').forEach(panel => {
        panel.classList.remove('active');
    });

    // 加上 active 到被點擊的按鈕
    event.currentTarget.classList.add('active');

    // 顯示對應的區塊
    document.getElementById('panel-' + tabName).classList.add('active');
}

// 自動隱藏成功/錯誤訊息
setTimeout(() => {
    const alerts = document.querySelectorAll('.alert-success, .alert-error');
    alerts.forEach(alert => {
        alert.style.transition = 'opacity 0.5s ease';
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 500);
    });
}, 3000);