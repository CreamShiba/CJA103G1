// admin-index.js
// 側邊欄摺疊邏輯 (保留原功能)
const menuItems = document.querySelectorAll('.has-submenu .submenu-toggle');
menuItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const parent = item.parentElement;
        parent.classList.toggle('open');
    });
});
