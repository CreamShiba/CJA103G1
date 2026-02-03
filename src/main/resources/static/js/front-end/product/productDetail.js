function switchImage(element) {
    var newSrc = element.querySelector('img').src;
    var mainImg = document.getElementById('mainImg');
    mainImg.style.opacity = 0;
    setTimeout(function() { mainImg.src = newSrc; mainImg.style.opacity = 1; }, 200);
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

// 開啟 Modal
function openReportModal() {
    document.getElementById('reportModal').style.display = 'flex';
}

// 關閉 Modal
function closeReportModal() {
    document.getElementById('reportModal').style.display = 'none';
    // 清空表單
    document.getElementById('reportForm').reset();
}

// 點擊背景關閉
window.onclick = function(event) {
    const modal = document.getElementById('reportModal');
    if (event.target == modal) {
        closeReportModal();
    }
}

// 提交表單邏輯
function submitReportForm() {
    const reasonSelect = document.getElementById('reportsReason');
    const description = document.getElementById('reportsDescription').value;
    const form = document.getElementById('reportForm');

    // 1. 簡單驗證：必須選擇原因
    if (reasonSelect.value === "") {
        alert("請選擇檢舉原因！");
        reasonSelect.classList.add('error'); // 加上紅框
        reasonSelect.focus();
        return;
    } else {
        reasonSelect.classList.remove('error');
    }

    // 2. 準備提交
    // 如果你想用 AJAX (不換頁提交):
    /*
    const formData = new FormData(form);
    fetch(form.action, {
        method: 'POST',
        body: formData
    })
    .then(response => response.json()) // 假設後端回傳 JSON
    .then(data => {
        alert('檢舉已送出，我們會盡快處理！');
        closeReportModal();
    })
    .catch(error => {
        console.error('Error:', error);
        alert('發生錯誤，請稍後再試');
    });
    */

    // 3. 或者直接使用傳統表單提交 (會換頁):
    // 這裡我們模擬 Alert 讓你知道成功了，實際上你可以直接 form.submit()
    // alert(`檢舉已送出！\n原因：${reasonSelect.value}`);
    form.submit(); // 這行會真的把資料送去後端 @{/reports/submit}
}

const userIcon = document.getElementById('user-icon');
const userDropdown = document.getElementById('user-dropdown');
userIcon.addEventListener('click', (e) => {
    e.stopPropagation();
    userDropdown.classList.toggle('show');
});
window.addEventListener('click', () => {
    if (userDropdown.classList.contains('show')) userDropdown.classList.remove('show');
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

// 檢查是否有 flash message，有的話 3 秒後自動淡出
document.addEventListener("DOMContentLoaded", function() {
    var flashMsg = document.getElementById('flashMessage');
    if (flashMsg) {
        setTimeout(function() {
            flashMsg.style.transition = "opacity 0.5s ease";
            flashMsg.style.opacity = "0";
            setTimeout(function() {
                flashMsg.remove();
            }, 500); // 等待淡出動畫結束後移除
        }, 3000); // 3秒後消失
    }
});