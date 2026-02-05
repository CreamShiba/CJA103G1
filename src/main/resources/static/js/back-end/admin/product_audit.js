/*
   product_audit.js
   注意：側邊欄摺疊邏輯已移至 admin_sidebar.js，此處僅保留審核頁面專用邏輯
*/

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