document.addEventListener("DOMContentLoaded", function() {
    initHeaderDropdowns();
});

function initHeaderDropdowns() {
    // 1. 定義所有下拉選單的 Trigger (按鈕) 和 Content (內容)
    const dropdowns = [
        {
            trigger: document.getElementById('user-icon'),      // 使用者頭像
            menu: document.getElementById('user-dropdown')
        },
        {
            trigger: document.getElementById('member-center-trigger'), // 會員中心
            menu: document.getElementById('member-dropdown')
        },
        {
            trigger: document.getElementById('forum-center-trigger'),  // 論壇中心
            menu: document.getElementById('forum-dropdown')
        }
    ];

    // 2. 為每個選單綁定點擊事件
    dropdowns.forEach(item => {
        // 只有當按鈕和選單都存在時才執行，避免報錯
        if (item.trigger && item.menu) {
            item.trigger.addEventListener('click', function(e) {
                e.preventDefault(); // 防止連結跳轉 (#)
                e.stopPropagation(); // 阻止事件冒泡到 window

                // (A) 關閉「其他」所有已開啟的選單 (互斥效果：一次只開一個)
                dropdowns.forEach(other => {
                    if (other.menu && other.menu !== item.menu) {
                        other.menu.classList.remove('show');
                    }
                });

                // (B) 切換「自己」的開關狀態
                item.menu.classList.toggle('show');
            });
        }
    });

    // 3. 全域點擊監聽：點擊畫面空白處，關閉所有選單
    window.addEventListener('click', function() {
        dropdowns.forEach(item => {
            if (item.menu) {
                item.menu.classList.remove('show');
            }
        });
    });
}

// 🔥 4. 補上頁面跳轉函式 (這是您 HTML onclick 呼叫的)
function navigateTo(page) {
    // 這裡定義基礎路徑，您可以依據需求修改
    let baseUrl = "/members/";

    switch(page) {
        case 'coupons':
            window.location.href = baseUrl + "coupons";
            break;
        case 'favorite':
            window.location.href = baseUrl + "favorite";
            break;
        case 'orderInfo':
            window.location.href = baseUrl + "orders"; // 或是 order/list
            break;
        default:
            console.warn("未知的頁面跳轉: " + page);
    }
}

// 收藏功能
document.addEventListener('DOMContentLoaded', function() {
    const favStoreBtn = document.getElementById('favStoreBtn');

    if (favStoreBtn) {
        favStoreBtn.addEventListener('click', function() {
            const btn = this;
            if (btn.disabled) return;
            btn.disabled = true;

            const sellerNo = btn.getAttribute('data-sellerno');
            const isActive = btn.classList.contains('active');
            const url = isActive ? '/favoriteStore/deleteAjax' : '/favoriteStore/addAjax';

            fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `sellerNo=${sellerNo}`
            })
                .then(response => {
                    if (response.status === 401) {
                        alert("請先登入後再進行收藏操作");
                        window.location.href = "/members/login";
                        return;
                    }
                    return response.json();
                })
                .then(res => {
                    if (res && res.status === "success") {
                        // 1. 切換背景顏色與邊框 (active class)
                        btn.classList.toggle('active');

                        // 2. 僅切換文字內容
                        const textSpan = btn.querySelector('span');
                        if (textSpan) {
                            textSpan.textContent = btn.classList.contains('active') ? '已收藏' : '收藏賣場';
                        }
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert("系統忙碌中，請稍後再試");
                })
                .finally(() => {
                    btn.disabled = false;
                });
        });
    }
});