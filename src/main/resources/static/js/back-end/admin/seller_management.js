// 1. 開啟視窗 (保持全域函式，讓 HTML 按鈕可以呼叫)
function openAuditModal(btn) {
    // 1. 從按鈕 dataset 取得資料 (新增了許多欄位)
    let sellerNo = btn.dataset.id;
    let shopName = btn.dataset.shop;       // 店名
    let contactName = btn.dataset.contact; // 聯絡人
    let email = btn.dataset.email;
    let phone = btn.dataset.phone;         // 電話
    let address = btn.dataset.address;     // 地址
    let desc = btn.dataset.desc;           // 描述
    let imagePath = btn.dataset.image;     // 圖片路徑
    let taxId = btn.dataset.tax;
    let bankAccount = btn.dataset.bank;
    let status = btn.dataset.status;
    let createTime = btn.dataset.time;
    let isVerified = btn.dataset.verified === 'true'; // 轉成布林值

    // 2. 填入資料到 HTML 元素
    document.getElementById('m_sellerNo').innerText = sellerNo || '';

    // 填入新增的欄位
    document.getElementById('m_shopName').innerText = shopName || '未設定店名';
    document.getElementById('m_contactName').innerText = contactName || '';
    document.getElementById('m_phone').innerText = phone || '未填寫';
    document.getElementById('m_address').innerText = address || '未填寫';
    document.getElementById('m_description').innerText = desc || '暫無描述';
    document.getElementById('m_email').innerText = email || '';
    document.getElementById('m_taxId').innerText = taxId || '未提供';
    document.getElementById('m_bankAccount').innerText = bankAccount || '未提供';

    // 處理圖片 (如果沒有圖片，顯示預設圖)
    let imgElem = document.getElementById('m_image');
    if (imagePath && imagePath.trim() !== "") {
        // 假設你的圖片路徑是存相對路徑，如果是 Base64 或是完整 URL 直接用
        // 如果是後端路徑，可能需要加上 context path，例如: '/product/displayImage?id=' + ...
        // 這裡假設 imagePath 是可以直接用的 URL
        imgElem.src = imagePath;
    } else {
        // 預設圖片 (可以用 FontAwesome 的 icon 或者一張預設圖)
        imgElem.src = "https://ui-avatars.com/api/?name=" + (shopName || "S") + "&background=random";
    }

    // 處理認證標章
    let verifiedBadge = document.getElementById('m_verified_badge');
    if(isVerified) {
        verifiedBadge.style.display = 'block';
    } else {
        verifiedBadge.style.display = 'none';
    }

    // 處理時間
    if(createTime) {
        document.getElementById('m_createTime').innerText = createTime.replace('T', ' ').substring(0, 16);
    } else {
        document.getElementById('m_createTime').innerText = '';
    }

    // 3. 狀態標籤顏色 (保持原本邏輯)
    let statusSpan = document.getElementById('m_status');
    statusSpan.innerText = status;
    statusSpan.className = 'status-badge';
    if (status === '待審核') statusSpan.classList.add('status-wait');
    else if (status === '已開通') statusSpan.classList.add('status-ok');
    else statusSpan.classList.add('status-block');

    // 4. 設定 hidden input (給 Form 用)
    document.getElementById('input_sellerNo_approve').value = sellerNo;
    document.getElementById('input_sellerNo_ban').value = sellerNo;
    if(document.getElementById('input_sellerNo_reject')) {
        document.getElementById('input_sellerNo_reject').value = sellerNo;
    }

    // 5. 按鈕顯示邏輯 (保持原本邏輯)
    let btnApprove = document.getElementById('formApprove');
    let btnBan = document.getElementById('formBan');
    let btnReject = document.getElementById('formReject');

    // 重置所有按鈕為不顯示 (避免殘留)
    if(btnApprove) btnApprove.style.display = 'none';
    if(btnBan) btnBan.style.display = 'none';
    if(btnReject) btnReject.style.display = 'none';

    let modalTitle = document.getElementById('modalTitle');

    if (status === '待審核') {
        // 待審核：可以「通過」或「駁回」
        if(btnApprove) btnApprove.style.display = 'inline-block';
        if(btnReject) btnReject.style.display = 'inline-block'; // 🔥 顯示駁回
        modalTitle.innerText = '📋 審核賣家申請';

    } else if (status === '已開通') {
        // 已開通：只能「停權」
        if(btnBan) btnBan.style.display = 'inline-block';
        modalTitle.innerText = 'ℹ️ 賣家詳細資料';

    } else if (status === '未通過' || status === '審核未通過') {
        // 未通過：通常只能查看，或者給予「通過」補救 (看你需求)
        // 這裡設定為：僅查看，或允許管理員手動改成開通
        if(btnApprove) btnApprove.style.display = 'inline-block';
        modalTitle.innerText = 'ℹ️ 申請未通過資料';

        // 針對 Badge 顏色做個特殊處理 (讓它變成灰色)
        statusSpan.style.background = '#f0f0f0';
        statusSpan.style.color = '#666';
        statusSpan.style.border = '1px solid #ccc';

    } else {
        // 停權：可以「解除停權 (通過)」
        if(btnApprove) btnApprove.style.display = 'inline-block';
        modalTitle.innerText = 'ℹ️ 賣家詳細資料 (已停權)';
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