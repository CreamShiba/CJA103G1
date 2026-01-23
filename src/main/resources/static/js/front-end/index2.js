// 使用者下拉選單控制
const userIcon = document.getElementById('user-icon');
const userDropdown = document.getElementById('user-dropdown');

userIcon.addEventListener('click', (e) => {
    // 阻止冒泡，避免觸發 window 的點擊事件
    e.stopPropagation();
    userDropdown.classList.toggle('show');
});

// 點擊視窗任何其他地方時，關閉選單
window.addEventListener('click', () => {
    if (userDropdown.classList.contains('show')) {
        userDropdown.classList.remove('show');
    }
});

// 頁面載入完成後
window.onload = function() {
    initUserDropdown();
};

// 初始化下拉選單
function initUserDropdown() {
    // 處理右上角使用者圖示
    const userIcon = document.getElementById('user-icon');
    const userDropdown = document.getElementById('user-dropdown');
    const memberTrigger = document.getElementById('member-center-trigger');
    const memberDropdown = document.getElementById('member-dropdown');

    if (userIcon && userDropdown) {
        userIcon.onclick = function (e) {
            e.stopPropagation();
            userDropdown.classList.toggle('show');
            memberDropdown.classList.remove('show');
        };
    }

    // 處理導航列中的「會員中心」
    if (memberTrigger && memberDropdown) {
        memberTrigger.onclick = function (e) {
            e.preventDefault();
            e.stopPropagation();
            memberDropdown.classList.toggle('show');
            userDropdown.classList.remove('show');
        };
    }
}

