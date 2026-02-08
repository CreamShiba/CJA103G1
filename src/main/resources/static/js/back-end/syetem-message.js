function openAddModal() {
    document.getElementById('modal-title').innerText = '➕ 新增通知';
    document.getElementById('system-message-form').action = '/admins/system-message/create';
    document.getElementById('edit-id').value = '';
    document.getElementById('field-member-no').value = '';
    document.getElementById('field-content').value = '';
    document.getElementById('modal-overlay').style.display = 'flex';
}

function openEditModal(btn) {
    document.getElementById('modal-title').innerText = '✏️ 編輯通知';
    document.getElementById('system-message-form').action = '/admins/system-message/update';

    // 從按鈕的 data 屬性取得資料
    const id = btn.getAttribute('data-id');
    const member = btn.getAttribute('data-member');
    const content = btn.getAttribute('data-content');

    document.getElementById('edit-id').value = id;
    document.getElementById('field-member-no').value = member;
    document.getElementById('field-content').value = content;

    document.getElementById('modal-overlay').style.display = 'flex';
}

function closeModal() {
    document.getElementById('modal-overlay').style.display = 'none';
}