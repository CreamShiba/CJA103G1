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
// 1. 開啟視窗 (保持全域函式，讓 HTML 按鈕可以呼叫)
function openAuditModal(btn) {
    // 從按鈕 dataset 取得資料
    let sellerNo = btn.dataset.id;
    let name = btn.dataset.name;
    let email = btn.dataset.email;
    let taxId = btn.dataset.tax;
    let bankAccount = btn.dataset.bank;
    let status = btn.dataset.status;
    let createTime = btn.dataset.time;

    // 填入資料
    document.getElementById('m_sellerNo').innerText = sellerNo || '';
    document.getElementById('m_sellerName').innerText = name || '';
    document.getElementById('m_email').innerText = email || '';
    document.getElementById('m_taxId').innerText = taxId || '';
    document.getElementById('m_bankAccount').innerText = bankAccount || '';

    if(createTime) {
        document.getElementById('m_createTime').innerText = createTime.replace('T', ' ').substring(0, 16);
    } else {
        document.getElementById('m_createTime').innerText = '';
    }

    // 狀態標籤顏色
    let statusSpan = document.getElementById('m_status');
    statusSpan.innerText = status;
    statusSpan.className = 'status-badge';
    if (status === '待審核') statusSpan.classList.add('status-wait');
    else if (status === '已開通') statusSpan.classList.add('status-ok');
    else statusSpan.classList.add('status-block');

    // 設定 hidden input
    document.getElementById('input_sellerNo_approve').value = sellerNo;
    document.getElementById('input_sellerNo_ban').value = sellerNo;

    // 按鈕顯示邏輯
    let btnApprove = document.getElementById('formApprove');
    let btnBan = document.getElementById('formBan');

    if (status === '待審核') {
        btnApprove.style.display = 'inline-block';
        btnBan.style.display = 'inline-block';
        document.getElementById('modalTitle').innerText = '📋 審核賣家申請';
    } else if (status === '已開通') {
        btnApprove.style.display = 'none';
        btnBan.style.display = 'inline-block';
        document.getElementById('modalTitle').innerText = 'ℹ️ 賣家詳細資料';
    } else {
        btnApprove.style.display = 'inline-block';
        btnBan.style.display = 'none';
        document.getElementById('modalTitle').innerText = 'ℹ️ 賣家詳細資料 (已停權)';
    }

    // 顯示彈窗
    document.getElementById('auditModal').style.display = 'flex';
}

// 2. 關閉視窗 (明確定義)
function closeAuditModal() {
    document.getElementById('auditModal').style.display = 'none';
}

// 3. 點擊背景關閉 (改用 addEventListener 防止衝突)
window.addEventListener('click', function(event) {
    let modal = document.getElementById('auditModal');
    if (event.target == modal) {
        modal.style.display = "none";
    }
});