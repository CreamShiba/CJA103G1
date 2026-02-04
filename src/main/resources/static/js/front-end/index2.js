// index2.js - 修正版

// 1. 確保頁面載入後才執行 (避免抓不到元素)
window.onload = function() {
    initUserDropdown();
};

function initUserDropdown() {
    // 定義 ID (請確認 HTML 對應的 ID)
    const userIcon = document.getElementById('user-icon');
    const userDropdown = document.getElementById('user-dropdown');
    const memberTrigger = document.getElementById('member-center-trigger');
    const memberDropdown = document.getElementById('member-dropdown');

    // --- 功能 A: 點擊頭像切換選單 ---
    if (userIcon && userDropdown) {
        userIcon.onclick = function (e) {
            // 阻止冒泡，避免觸發 window 的點擊事件馬上又把它關掉
            e.stopPropagation();
            userDropdown.classList.toggle('show');

            // 如果會員選單開著，順便關掉
            if (memberDropdown) memberDropdown.classList.remove('show');
        };
    }

    // --- 功能 B: 點擊畫面其他地方，關閉所有選單 ---
    // (這段原本只寫在最上面，現在搬進來這裡才安全)
    window.addEventListener('click', function() {
        if (userDropdown && userDropdown.classList.contains('show')) {
            userDropdown.classList.remove('show');
        }
        if (memberDropdown && memberDropdown.classList.contains('show')) {
            memberDropdown.classList.remove('show');
        }
    });

    // --- 功能 C: 處理導航列中的「會員中心」(如果有) ---
    if (memberTrigger && memberDropdown) {
        memberTrigger.onclick = function (e) {
            e.preventDefault();
            e.stopPropagation();
            memberDropdown.classList.toggle('show');

            // 如果使用者選單開著，順便關掉
            if (userDropdown) userDropdown.classList.remove('show');
        };
    }
}