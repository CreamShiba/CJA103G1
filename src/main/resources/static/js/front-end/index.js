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

