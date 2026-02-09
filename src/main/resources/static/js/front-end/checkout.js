
// 初始化數據 (從 Thymeleaf 獲取)
let subtotal = /*[[${subtotal}]]*/ 0;
let ship = 0;
let disc = 0;

// 1. 付款方式選擇邏輯 (解決不能按的問題)
function selectPayment(method) {
    // 1. 更新隱藏欄位的值，讓 Form 送出正確的付款方式
    document.getElementById('ord_payment_method').value = method;

    // 2. 移除所有選項的 'selected' 樣式
    const options = document.querySelectorAll('.payment-option');
    options.forEach(opt => opt.classList.remove('selected'));

    // 3. 根據點擊的選項加入 'selected' 樣式
    // 使用內容文字或 ID 來判斷
    if (method === '信用卡') {
        document.getElementById('pay-credit').classList.add('selected');
    } else if (method === '轉帳') {
        document.getElementById('pay-atm').classList.add('selected');
    } else if (method === '貨到付款') {
        document.getElementById('pay-cod').classList.add('selected');
    }

    console.log("當前選擇付款方式：" + method);
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


//優惠券:更新訂單總額計算 (小計 + 運費 - 折扣)
// 優惠券：更新訂單總額計算 (加入後端 20% 驗證)
async function updateOrderTotal() {
    // 1. 取得商品小計與運費
    const subtotalText = document.getElementById('display-subtotal').innerText.replace(/[^0-9]/g, '');
    const subtotal = parseInt(subtotalText) || 0;

    const deliveryMethod = document.getElementById('delivery-method').value;
    let shippingFee = 0;
    if (deliveryMethod === '宅配') shippingFee = 100;
    else if (deliveryMethod === '超取') shippingFee = 60;

    document.getElementById('display-shipping').innerText = 'NT$ ' + shippingFee;

    // 2. 準備驗證參數
    const currentTotal = subtotal + shippingFee; // 這是校驗 20% 的基準額
    const couponSelect = document.getElementById('couponSelect');
    const couponNo = couponSelect.value;

    let discount = 0;

    // 3. 如果有選擇優惠券，則向後端發起驗證
    if (couponNo) {
        try {
            // 呼叫您在 MemberCouponController 寫的 API
            const response = await fetch(`/member/coupons/validate-usage?couponNo=${couponNo}&orderAmount=${currentTotal}`);

            if (!response.ok) {
                const errorMsg = await response.text();
                // 顯示在網頁元素上，而不是彈窗
                document.getElementById('coupon-error').innerText = "⚠️ " + errorMsg;
                couponSelect.value = "";
                discount = 0;
            } else {
                // 驗證通過：後端會回傳折扣金額
                document.getElementById('coupon-error').innerText = "";
                discount = await response.json();
            }
        } catch (error) {
            console.error("驗證發生錯誤:", error);
            alert("優惠券驗證失敗，請稍後再試");
            couponSelect.value = "";
            discount = 0;
        }
    }

    // 4. 更新隱藏欄位與 UI
    if (document.getElementById('coupon_no')) document.getElementById('coupon_no').value = couponSelect.value;
    if (document.getElementById('discount_price')) document.getElementById('discount_price').value = discount;

    document.getElementById('display-discount').innerText = discount;

    // 5. 計算並顯示最終金額
    const finalTotal = Math.max(0, currentTotal - discount);
    const formattedTotal = new Intl.NumberFormat().format(finalTotal);
    document.getElementById('display-total').innerText = formattedTotal;
    document.getElementById('modal-amount').innerText = formattedTotal;
}

// 綁定物流變更事件 (原本 HTML 已有 onchange="updateShipping()")
function updateShipping() {
    updateOrderTotal();
}



function submitFinalOrder() {
    const form = document.getElementById('checkout-form');
    const paymentMethod = document.getElementById('ord_payment_method').value;

    // 根據付款方式決定路徑
    if (paymentMethod === '信用卡') {
        // 走綠界金流
        form.action = '/cart/checkout-with-ecpay';
    } else {
        // 走一般後端處理 (轉帳、貨到付款)
        form.action = '/cart/process-payment';
    }

    form.submit();
}