// index.js
// 側邊欄摺疊邏輯 (保留原功能)
const menuItems = document.querySelectorAll('.has-submenu .submenu-toggle');
menuItems.forEach(item => {
    item.addEventListener('click', (e) => {
        e.preventDefault();
        const parent = item.parentElement;
        parent.classList.toggle('open');
    });
});
// 1. 開啟撥款視窗
function openPayoutModal(btn) {
    let ordNo = btn.dataset.ordno;
    let sellerName = btn.dataset.seller;
    let bankAccount = btn.dataset.bank;

    // 抓取三個金額數據
    let total = parseInt(btn.dataset.total);
    let commission = parseInt(btn.dataset.commission);
    let net = parseInt(btn.dataset.net);

    document.getElementById('p_ordNo').innerText = '#' + ordNo;
    document.getElementById('p_seller').innerText = sellerName;
    document.getElementById('p_bank').innerText = bankAccount || '(未設定)';

    // 填入算式 (加上千分位逗號)
    document.getElementById('p_total').innerText = '$' + total.toLocaleString();
    document.getElementById('p_commission').innerText = '- $' + commission.toLocaleString();
    document.getElementById('p_net').innerText = '$' + net.toLocaleString();

    document.getElementById('input_payout_ordNo').value = ordNo;
    document.getElementById('payoutModal').style.display = 'flex';
}

// 2. 關閉視窗
function closePayoutModal() {
    document.getElementById('payoutModal').style.display = 'none';
}

// 3. 點擊背景關閉
window.addEventListener('click', function(event) {
    let modal = document.getElementById('payoutModal');
    if (event.target == modal) {
        modal.style.display = "none";
    }
});