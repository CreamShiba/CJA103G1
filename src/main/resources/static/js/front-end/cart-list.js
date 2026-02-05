

    // +/- 按鈕點擊
    function changeQty(prodNo, newQty) {
    if (newQty < 1) return;
    sendUpdate(prodNo, newQty);
}

    // 直接輸入數量後按 Enter / 失焦
    function inputQty(el) {
    let val = parseInt(el.value);
    if (isNaN(val) || val < 1) {
    el.value = 1;
    val = 1;
}
    const prodNo = el.getAttribute('data-prod-no');
    sendUpdate(prodNo, val);
}

    // 呼叫後端更新數量 API
    function sendUpdate(prodNo, qty) {
    fetch('/members/carts/update-quantity', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'prodNo=' + prodNo + '&quantity=' + qty
    })
        .then(r => r.json())
        .then(data => {
            if (data.success) {
                location.reload();
            } else {
                alert('更新失敗：' + data.message);
            }
        })
        .catch(() => alert('網路錯誤，請再試一次'));
}

    // ===================================================================
    // LocalStorage 同步（未登入時暫存的購物車）
    // ===================================================================
    document.addEventListener("DOMContentLoaded", function() {
    const localCart = localStorage.getItem('my_cart');
    if (localCart) {
    try {
    const cartData = JSON.parse(localCart);
    const prodNos = cartData.map(item => item.prodId);

    fetch('/members/carts/sync', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(prodNos)
})
    .then(r => r.json())
    .then(data => {
    if (data.success) {
    console.log("✅ 同步成功，已將 LocalStorage 購物車合併到資料庫");
    localStorage.removeItem('my_cart');
    window.location.reload();
}
})
    .catch(err => console.error('同步錯誤:', err));
} catch (e) {
    console.error('解析 LocalStorage 失敗:', e);
}
}
});
