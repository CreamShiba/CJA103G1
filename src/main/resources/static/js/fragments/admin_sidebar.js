document.addEventListener("DOMContentLoaded", function() {

    /* =========================================
       1. 第一層選單處理 (.submenu-toggle)
       ========================================= */
    const menuToggles = document.querySelectorAll('.submenu-toggle');

    menuToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();

            const parentLi = this.closest('.menu-item');

            // 手風琴效果：關閉其他已展開的主選單
            // (排除自己，也排除已經打開的子選單結構)
            document.querySelectorAll('.menu-item.has-submenu.open').forEach(item => {
                if (item !== parentLi) {
                    item.classList.remove('open');
                }
            });

            parentLi.classList.toggle('open');
        });
    });

    /* =========================================
       2. 🔥 新增：第三層巢狀選單處理 (.sub-toggle)
       ========================================= */
    const subToggles = document.querySelectorAll('.sub-toggle');

    subToggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault();
            // 🛑 關鍵：阻止事件冒泡！
            // 如果不加這行，點擊第三層時，事件會傳到第一層，導致外層選單以為被點擊而關閉。
            e.stopPropagation();

            // 找到包含這個 toggle 的父層 (has-sub-item)
            const parentSubLi = this.closest('.has-sub-item');

            // 切換 open 狀態
            if (parentSubLi) {
                parentSubLi.classList.toggle('open');
            }
        });
    });

    /* =========================================
       3. 登出確認
       ========================================= */
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