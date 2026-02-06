
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

//監聽物流、優惠券下拉
// document.addEventListener('DOMContentLoaded', function() {
//     // 1. 取得 DOM 元素
//     const subtotal = parseInt(document.getElementById('display-subtotal').innerText.replace(/,/g, '')) || 0;
//     const deliveryMethod = document.getElementById('delivery-method');
//     const couponSelect = document.getElementById('couponSelect');
//
//     // 顯示用的元素
//     const displayShipping = document.getElementById('display-shipping');
//     const displayTotal = document.getElementById('display-total');
//     const modalAmount = document.getElementById('modal-amount');
//
//     // 隱藏表單欄位 (用於送回後端)
//     const inputDiscountPrice = document.getElementById('discount_price');
//     const inputCouponNo = document.getElementById('coupon_no');
//
//     /**
//      * 核心計算函式
//      */
//     function calculateTotal() {
//         // A. 取得運費
//         let shipFee = 0;
//         const method = deliveryMethod.value;
//         if (method === '宅配') shipFee = 100;
//         else if (method === '超取') shipFee = 60;
//         else if (method === '自取') shipFee = 0;
//
//         // B. 取得優惠券面額
//         const selectedOption = couponSelect.options[couponSelect.selectedIndex];
//         let discount = 0;
//         let couponNo = "";
//
//         if (selectedOption && selectedOption.value !== "") {
//             discount = parseInt(selectedOption.getAttribute('data-price')) || 0;
//             couponNo = selectedOption.value;
//         }
//
//         // C. 驗證 20% 限制邏輯
//         // 公式：折扣金額不可超過 (小計 + 運費) 的 20%
//         const maxDiscount = Math.floor((subtotal + shipFee) * 0.2);
//
//         if (discount > maxDiscount) {
//             alert(`該優惠券不符合使用門檻！\n目前折扣金額 ($${discount}) 已超過訂單總額 20% (上限為 $${maxDiscount})`);
//
//             // 強制重置下拉選單
//             couponSelect.value = "";
//             discount = 0;
//             couponNo = "";
//         }
//
//         // D. 更新畫面與隱藏欄位
//         const finalPrice = subtotal + shipFee - discount;
//
//         displayShipping.innerText = `NT$ ${shipFee}`;
//         displayTotal.innerText = finalPrice.toLocaleString(); // 加上千分位
//         modalAmount.innerText = finalPrice.toLocaleString();
//
//         // 寫入 Form 隱藏欄位以便送交後端
//         inputDiscountPrice.value = discount;
//         inputCouponNo.value = couponNo;
//     }
//
//     // 2. 綁定監聽事件
//     deliveryMethod.addEventListener('change', calculateTotal);
//     couponSelect.addEventListener('change', calculateTotal);
//
//     // 3. 初始執行一次
//     calculateTotal();
// });