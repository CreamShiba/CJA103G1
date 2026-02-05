/* seller_index.js */

// 1. 切換分頁核心邏輯
function switchTab(tabId) {
    // 移除所有 active class
    document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
    document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));

    // 抓取目標元素
    const targetView = document.getElementById('view-' + tabId);
    // 這裡要注意：你之前 HTML 的側邊欄 id 好像沒設定 id="nav-dashboard" 之類的
    // 如果你的 nav-item 沒有 id，這行可能會報錯，建議加上 null 檢查
    const targetNav = document.getElementById('nav-' + tabId);

    if (targetView) targetView.classList.add('active');
    if (targetNav) targetNav.classList.add('active');
}

// 2. 頁面載入自動判斷停留分頁 (修正版)
document.addEventListener("DOMContentLoaded", function() {
    // 從 HTML 的隱藏欄位抓值，而不是在 JS 裡寫 Thymeleaf
    const tabInput = document.getElementById('currentActiveTab');

    // 如果有抓到值就用，沒抓到就預設 dashboard
    const currentTab = tabInput ? tabInput.value : 'dashboard';

    switchTab(currentTab);
});

// 3. 使用者選單與 Modal 控制
const userTrigger = document.getElementById('userTrigger');
const userDropdown = document.getElementById('userDropdown');

// 點擊頭像切換選單
if(userTrigger) {
    userTrigger.addEventListener('click', (e) => {
        e.stopPropagation(); // 防止冒泡
        userDropdown.classList.toggle('show');
    });
}

// 4. 全局點擊事件 (解決 window.onclick 衝突)
// 使用 addEventListener 比較安全，不會互相覆蓋
window.addEventListener('click', function(event) {

    // (A) 處理下拉選單關閉：如果點擊的不是頭像，就關閉選單
    if (userDropdown && userDropdown.classList.contains('show')) {
        // 注意：因為上面有 stopPropagation，所以點頭像不會觸發這裡
        userDropdown.classList.remove('show');
    }

    // (B) 處理 Modal 關閉：如果點擊的是 Modal 背景(遮罩)，就關閉
    const modal = document.getElementById('cancelModal');
    if (modal && event.target == modal) {
        modal.style.display = 'none';
    }
});

// 5. 開啟 Modal
function openCancelModal(ordNo) {
    const modalInput = document.getElementById('modalOrdNo');
    const modal = document.getElementById('cancelModal');

    if(modalInput) modalInput.value = ordNo; // 填入訂單號
    if(modal) modal.style.display = 'flex';  // 顯示視窗
}

// 6. 關閉 Modal
function closeCancelModal() {
    const modal = document.getElementById('cancelModal');
    if(modal) modal.style.display = 'none';
}

// 7. 選單連動 Textarea (選用)
function updateReason(select) {
    var text = document.getElementById('reasonText');
    if(text && select.value !== "") {
        text.value = select.value;
    }
}

    function validateOrderDate() {
    var startInput = document.getElementById('startDateInput').value;
    var endInput = document.getElementById('endDateInput').value;

    if (startInput && endInput) {
    if (startInput > endInput) {
    alert("⚠️ 開始日期不能晚於結束日期！");
    return false; // 阻止表單送出
}
}
    return true; // 允許送出
}

// ==========================================
// 1. 開啟評價視窗 (新增模式：要評價買家)
// ==========================================
function openRatingModal(ordNo, memberNo) {
    let modal = document.getElementById('ratingModal');
    let form = modal.querySelector('form');
    let title = document.getElementById('modal-title');

    // 🔥 重置：確保表單是顯示的 (避免被 viewBuyerRating 隱藏)
    form.style.display = 'block';
    let tempView = document.getElementById('temp-rating-view');
    if (tempView) tempView.style.display = 'none';

    form.reset(); // 清空表單
    document.getElementById('ratingOrdNo').value = ordNo;
    document.getElementById('ratingMemberNo').value = memberNo;

    // 開啟編輯權限
    document.getElementById('ratingCommentArea').readOnly = false;
    document.getElementById('btn-submit-rating').style.display = 'block'; // 顯示按鈕
    title.innerText = '評價買家';
    title.style.color = '#fff';

    // 讓星星可以點選
    document.querySelector('.star-rating').style.pointerEvents = 'auto';

    modal.style.display = 'flex';
}

// ==========================================
// 2. 查看 "我" 給買家的評價 (唯讀模式)
// ==========================================
function viewMyRating(ordNo) {
    let modal = document.getElementById('ratingModal');
    let form = modal.querySelector('form');
    let title = document.getElementById('modal-title');

    // 🔥 重置：確保表單是顯示的
    form.style.display = 'block';
    let tempView = document.getElementById('temp-rating-view');
    if (tempView) tempView.style.display = 'none';

    // 從 HTML 的 hidden div 抓資料
    let dataDiv = document.getElementById('my-rating-' + ordNo);
    let score = dataDiv.getAttribute('data-score');
    let comment = dataDiv.getAttribute('data-comment');

    // 填入資料
    document.getElementById('ratingCommentArea').value = comment;

    // 勾選對應的星星
    let starRadio = document.querySelector(`input[name="score"][value="${score}"]`);
    if(starRadio) starRadio.checked = true;

    // 鎖定介面
    document.getElementById('ratingCommentArea').readOnly = true;
    document.getElementById('btn-submit-rating').style.display = 'none'; // 隱藏按鈕
    title.innerText = '您的評價內容';
    title.style.color = '#fff';

    // 禁止點選星星
    document.querySelector('.star-rating').style.pointerEvents = 'none';

    modal.style.display = 'flex';
}

// ==========================================
// 3. 查看 "買家" 給我的評價 (列表模式 - 全新改寫)
// ==========================================
function viewBuyerRating(ordNo) {
    let modal = document.getElementById('ratingModal');
    let form = modal.querySelector('form');
    let title = document.getElementById('modal-title');

    // 1. 抓取 HTML 裡藏好的評價列表
    let contentDiv = document.getElementById('buyer-rating-content-' + ordNo);
    if (!contentDiv) {
        alert("找不到評價資料！");
        return;
    }

    // --- 介面切換邏輯 ---

    // 2. 隱藏原本的表單 (因為列表塞不進去 textarea)
    form.style.display = 'none';

    // 3. 準備顯示容器 (如果還沒有就建立一個)
    let displayDiv = document.getElementById('temp-rating-view');
    if (!displayDiv) {
        displayDiv = document.createElement('div');
        displayDiv.id = 'temp-rating-view';
        displayDiv.style.textAlign = 'left';
        displayDiv.style.maxHeight = '400px';
        displayDiv.style.overflowY = 'auto'; // 評價太多時可捲動
        // 把這個 div 插在標題下方
        title.insertAdjacentElement('afterend', displayDiv);
    }

    // 4. 把 HTML 內容塞進去並顯示
    displayDiv.innerHTML = contentDiv.innerHTML;

    // 🔥【新增這段】: 手動補上一個關閉按鈕
    displayDiv.innerHTML += `
        <div style="margin-top: 15px; text-align: right; padding-top: 10px;">
            <button type="button" 
                    onclick="document.getElementById('ratingModal').style.display='none'"
                    style="padding: 8px 20px; background: var(--accent-purple, #7c4dff); color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold;">
                關閉
            </button>
        </div>
    `;

    displayDiv.style.display = 'block';

    // 5. 設定標題
    title.innerText = '買家給您的評價';
    title.style.color = '#ffc107'; // 金色標題

    // 6. 顯示視窗
    modal.style.display = 'flex';
}

document.addEventListener("DOMContentLoaded", function() {
    const startDateInput = document.getElementById('startDateInput');
    const endDateInput = document.getElementById('endDateInput');

    function updateConstraints() {
        // 設定結束日期的最小值 (不能早於開始日期)
        if (startDateInput.value) {
            endDateInput.min = startDateInput.value;
        } else {
            endDateInput.min = '';
        }

        // 設定開始日期的最大值 (不能晚於結束日期)
        if (endDateInput.value) {
            startDateInput.max = endDateInput.value;
        } else {
            startDateInput.max = '';
        }
    }

    // 兩個欄位變動時都檢查
    startDateInput.addEventListener('change', updateConstraints);
    endDateInput.addEventListener('change', updateConstraints);

    // 初始化
    updateConstraints();
});
