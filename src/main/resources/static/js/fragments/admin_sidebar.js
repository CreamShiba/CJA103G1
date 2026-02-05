document.addEventListener("DOMContentLoaded", function() {

    // 1. 處理側邊欄選單的展開/收合 (Accordion 效果)
    const menuToggles = document.querySelectorAll('.submenu-toggle');

    menuToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault(); // 防止連結跳轉

            // 找到目前的父層 <li>
            const parentLi = this.closest('.menu-item');

            // 檢查目前是否已經是展開狀態
            const isOpen = parentLi.classList.contains('open');

            // (選項 A) 手風琴效果：點擊一個時，關閉其他所有已展開的選單
            // 如果您希望可以同時展開多個，請把這段註解掉
            document.querySelectorAll('.menu-item.has-submenu.open').forEach(item => {
                if (item !== parentLi) {
                    item.classList.remove('open');
                }
            });

            // (選項 B) 切換目前的狀態
            // 如果原本是開的 -> 關閉 (移除 open)
            // 如果原本是關的 -> 展開 (加上 open)
            parentLi.classList.toggle('open');
        });
    });

    // 2. 處理登出確認 (優化使用者體驗)
    // 雖然您的 HTML 已經有 inline onclick，但在 JS 處理可以加入確認視窗
    const logoutBtn = document.querySelector('.sidebar-footer a');
    if (logoutBtn) {
        logoutBtn.onclick = function(e) {
            e.preventDefault();
            if (confirm("確定要登出後台管理系統嗎？")) {
                document.getElementById('logoutForm').submit();
            }
        };
    }
});