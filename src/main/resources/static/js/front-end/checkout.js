
// 初始化數據 (從 Thymeleaf 獲取)
let subtotal = /*[[${subtotal}]]*/ 0;
let ship = 0;
let disc = 0;

// 1. 付款方式選擇邏輯 (解決不能按的問題)
function selectPayment(method) {
    // 更新隱藏的 input 值
    document.getElementById('ord_payment_method').value = method;

    // 切換 CSS 樣式
    // 先移除所有選項的 selected 類別
    document.querySelectorAll('.payment-option').forEach(opt => {
        opt.classList.remove('selected');
    });

    // 根據點擊的內容增加 selected 類別 (利用 ID 判斷或根據傳入文字)
    if (method === '信用卡') document.getElementById('pay-credit').classList.add('selected');
    if (method === '轉帳') document.getElementById('pay-atm').classList.add('selected');
    if (method === '超商代收') document.getElementById('pay-convenience').classList.add('selected');

    console.log("當前付款方式:", method);
}

// 2. 物流方式變動：更新運費
function updateShipping() {
    const method = document.getElementById('delivery-method').value;
    if (method === '宅配') ship = 100;
    else if (method === '超取') ship = 60;
    else ship = 0;

    document.getElementById('display-shipping').innerText = "NT$ " + ship.toLocaleString();
    render();
}

// 3. 折價券套用 (模擬顯示，實際折扣由後端 process-payment 決定)
function applyCoupon() {
    let inputCode = document.getElementById('coupon-input').value.trim();
    if (!inputCode) {
        alert("請輸入代碼");
        return;
    }

    // 為了讓使用者有感，這裡先做一個模擬 UI 更新
    // 真正的安全性驗證在你寫好的 cartservice.java 裡面
    document.getElementById('coupon-msg').innerText = "已套用代碼：" + inputCode;
    document.getElementById('coupon-msg').style.color = "#4CAF50";

    // 這裡可以選擇是否要在前端先減去預估金額，或維持 0 等後端算
    // disc = 100; // 如果你想讓前端看起來有變化可以設值
    // document.getElementById('row-discount').style.display = 'flex';
    // document.getElementById('display-discount').innerText = "- NT$ " + disc;

    render();
}

// 4. 渲染總金額
function render() {
    let total = subtotal + ship - disc;
    if (total < 0) total = 0;

    document.getElementById('display-total').innerText = total.toLocaleString();
    document.getElementById('modal-amount').innerText = total.toLocaleString();
}

// 5. 彈窗控制
function showModal() {
    const shipMethod = document.getElementById('delivery-method').value;
    if (!shipMethod) {
        alert("請選擇物流方式");
        return;
    }
    document.getElementById('confirmModal').classList.add('active');
}

function hideModal() {
    document.getElementById('confirmModal').classList.remove('active');
}

// 初始化頁面
window.onload = function() {
    render();
};
