/* =========================================
   1. 商品詳情頁專屬功能 (圖片、數量、檢舉)
   ========================================= */

function switchImage(element) {
    var newSrc = element.querySelector('img').src;
    var mainImg = document.getElementById('mainImg');

    // 簡單的淡入淡出效果
    mainImg.style.opacity = 0;
    setTimeout(function() {
        mainImg.src = newSrc;
        mainImg.style.opacity = 1;
    }, 200);

    document.querySelectorAll('.thumb-box').forEach(box => box.classList.remove('active'));
    element.classList.add('active');
}

function updateQty(change) {
    var input = document.getElementById('qtyInput');
    var currentVal = parseInt(input.value);
    var maxVal = parseInt(input.getAttribute('max'));
    var newVal = currentVal + change;
    if (newVal >= 1 && newVal <= maxVal) input.value = newVal;
}

/* --- 檢舉 Modal 相關 JS --- */

function openReportModal() {
    document.getElementById('reportModal').style.display = 'flex';
}

function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
    document.getElementById('reportForm').reset();
}

// 點擊背景關閉 Modal
window.onclick = function(event) {
    const modal = document.getElementById('reportModal');
    if (event.target == modal) {
        closeReportModal();
    }
}

function submitReportForm() {
    const reasonSelect = document.getElementById('reportsReason');
    const form = document.getElementById('reportForm');

    if (reasonSelect.value === "") {
        alert("請選擇檢舉原因！");
        reasonSelect.classList.add('error');
        reasonSelect.focus();
        return;
    } else {
        reasonSelect.classList.remove('error');
    }
    form.submit();
}

/* =========================================
   2. 全域 Header 功能 (下拉選單、導頁) 
   🔥 這裡是新加入的邏輯，與 index2.js 同步
   ========================================= */

document.addEventListener("DOMContentLoaded", function() {
    // 初始化 Header 下拉選單
    initHeaderDropdowns();

    // 處理 Flash Message (提示訊息淡出)
    handleFlashMessage();

    // 初始化 Ajax 收藏按鈕
    initAjaxFavorite();
});

function initHeaderDropdowns() {
    // 定義所有下拉選單
    const dropdowns = [
        {
            trigger: document.getElementById('user-icon'),
            menu: document.getElementById('user-dropdown')
        },
        {
            trigger: document.getElementById('member-center-trigger'),
            menu: document.getElementById('member-dropdown')
        },
        {
            // 🔥 補上論壇選單
            trigger: document.getElementById('forum-center-trigger'),
            menu: document.getElementById('forum-dropdown')
        }
    ];

    // 綁定點擊事件 (互斥開啟)
    dropdowns.forEach(item => {
        if (item.trigger && item.menu) {
            item.trigger.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();

                // 關閉其他選單
                dropdowns.forEach(other => {
                    if (other.menu && other.menu !== item.menu) {
                        other.menu.classList.remove('show');
                    }
                });

                // 切換自己
                item.menu.classList.toggle('show');
            });
        }
    });

    // 點擊空白處關閉所有選單
    window.addEventListener('click', function() {
        dropdowns.forEach(item => {
            if (item.menu) {
                item.menu.classList.remove('show');
            }
        });
    });
}

// 🔥 補上導頁函式 (解決 onclick="navigateTo..." 報錯)
function navigateTo(page) {
    let baseUrl = "/members/";
    switch(page) {
        case 'coupons':
            window.location.href = baseUrl + "coupons";
            break;
        case 'favorite':
            window.location.href = baseUrl + "favorite";
            break;
        case 'orderInfo':
            window.location.href = baseUrl + "orders";
            break;
        default:
            console.warn("未知的頁面跳轉: " + page);
    }
}

/* =========================================
   3. 其他輔助功能 (Flash Msg, Ajax Favorite)
   ========================================= */

function handleFlashMessage() {
    var flashMsg = document.getElementById('flashMessage');
    if (flashMsg) {
        setTimeout(function() {
            flashMsg.style.transition = "opacity 0.5s ease";
            flashMsg.style.opacity = "0";
            setTimeout(function() {
                flashMsg.remove();
            }, 500);
        }, 3000);
    }
}

function initAjaxFavorite() {
    const favBtn = document.getElementById('favBtn');
    const favIcon = document.getElementById('favIcon');

    if (favBtn) {
        favBtn.addEventListener('click', function() {
            const prodNo = this.getAttribute('data-prodno');

            fetch('/favorite/toggleAjax', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({ 'prodNo': prodNo })
            })
                .then(response => {
                    if (response.status === 401) {
                        alert("請先登入後再執行收藏！");
                        window.location.href = "/members/login";
                        return;
                    }
                    return response.json();
                })
                .then(data => {
                    if (data && data.status === 'success') {
                        if (data.isFavorite) {
                            favIcon.className = 'fas fa-heart';
                            favBtn.classList.add('active');
                            favBtn.title = "取消收藏";
                        } else {
                            favIcon.className = 'far fa-heart';
                            favBtn.classList.remove('active');
                            favBtn.title = "加入收藏";
                        }
                    }
                })
                .catch(error => console.error('Error:', error));
        });
    }
}