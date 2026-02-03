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

document.addEventListener("DOMContentLoaded", function() {
    // 選取所有的 toast (包含成功或失敗)
    const toasts = document.querySelectorAll('.admin-toast');

    toasts.forEach(toast => {
        if (toast) {
            // 設定 3 秒後開始淡出
            setTimeout(() => {
                toast.style.transition = "opacity 0.5s ease, top 0.5s ease";
                toast.style.opacity = "0";
                toast.style.top = "-50px"; // 往上滑回去

                // 動畫結束後從 DOM 移除
                setTimeout(() => {
                    toast.remove();
                }, 500);
            }, 3000);
        }
    });
});