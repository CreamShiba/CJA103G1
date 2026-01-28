document.addEventListener("DOMContentLoaded", function() {
    // 找到所有有子選單的開關
    const toggles = document.querySelectorAll('.submenu-toggle');

    toggles.forEach(toggle => {
        toggle.addEventListener('click', function(e) {
            e.preventDefault(); // 防止連結亂跳

            // 找到父層 li
            const parent = this.parentElement;

            // 切換 open class
            parent.classList.toggle('open');
        });
    });
});