// ===================================================================
// 1. 金額計算與 UI 更新邏輯
// ===================================================================

// 全選/全不選功能
function toggleAll(source) {
    const checkboxes = document.querySelectorAll('.item-check');
    checkboxes.forEach(cb => {
        cb.checked = source.checked;
    });
    updateSummary(); // 選完後更新金額
}

// 更新已選商品數量與總計金額
function updateSummary() {
    const checkedBoxes = document.querySelectorAll('.item-check:checked');
    let total = 0;

    checkedBoxes.forEach(cb => {
        // 向上尋找最近的 .cart-item 容器來獲取價格
        const parentItem = cb.closest('.cart-item');
        // 從 HTML 的 th:attr="data-price=..." 讀取數值
        const price = parseInt(parentItem.getAttribute('data-price')) || 0;
        // 從 Checkbox 的 th:attr="data-qty=..." 讀取數值
        const qty = parseInt(cb.getAttribute('data-qty')) || 0;

        total += (price * qty);
    });

    // 更新畫面上的數字
    const countEl = document.getElementById('checkedCount');
    const totalEl = document.getElementById('checkedTotal');

    if (countEl) countEl.innerText = checkedBoxes.length;
    if (totalEl) totalEl.innerText = total.toLocaleString(); // 加上千分位

    // 反向控制「全選」按鈕的狀態
    const selectAll = document.getElementById('selectAll');
    const allItems = document.querySelectorAll('.item-check');
    if (selectAll && allItems.length > 0) {
        selectAll.checked = (checkedBoxes.length === allItems.length);
    }
}

// ===================================================================
// 2. 結帳邏輯
// ===================================================================

function goToCheckout() {
    const checkedBoxes = document.querySelectorAll('.item-check:checked');

    if (checkedBoxes.length === 0) {
        alert("請至少選擇一項商品進行結帳！");
        return;
    }

    // 收集所有被勾選的商品編號 (prodNo)
    const selectedIds = Array.from(checkedBoxes).map(cb => cb.value).join(',');

    // 將編號填入隱藏欄位並送出表單
    const hiddenInput = document.getElementById('selectedProdNos');
    const form = document.getElementById('checkoutForm');

    if (hiddenInput && form) {
        hiddenInput.value = selectedIds;
        form.submit(); // 這會觸發 Controller 的 /cart/checkout?prodNos=...
    }
}

// ===================================================================
// 3. 數量變更邏輯 (修正 API 路徑)
// ===================================================================

function changeQty(prodNo, newQty) {
    if (newQty < 1) return;
    sendUpdate(prodNo, newQty);
}

function sendUpdate(prodNo, qty) {
    // ⚠️ 注意：路徑必須對應你的 Controller @RequestMapping("/cart")
    fetch('/cart/update-quantity', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'prodNo=' + prodNo + '&quantity=' + qty
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                location.reload(); // 更新成功後刷頁面顯示最新小計
            } else {
                alert('更新失敗：' + data.message);
            }
        })
        .catch(() => alert('網路錯誤，請再試一次'));
}

// ===================================================================
// 4. 初始化
// ===================================================================
document.addEventListener("DOMContentLoaded", function() {
    // 頁面載入時先算一次金額 (預設是 0)
    updateSummary();

    // 原有的 LocalStorage 同步邏輯 (如有需要)
    const localCart = localStorage.getItem('my_cart');
    if (localCart) {
        const prodNos = JSON.parse(localCart).map(item => item.prodId);
        fetch('/cart/sync', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(prodNos)
        })
            .then(r => r.json())
            .then(data => {
                if (data.success) {
                    localStorage.removeItem('my_cart');
                    location.reload();
                }
            });
    }
});