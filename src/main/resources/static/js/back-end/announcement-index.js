
// 開啟新增 Modal
function openAddModal() {
    document.getElementById('modal-title').textContent = '新增公告';
    document.getElementById('add-form').style.display = 'block';
    document.getElementById('edit-form').style.display = 'none';
    document.getElementById('add-form').reset();
    document.getElementById('modal-overlay').classList.add('active');
}

// 開啟編輯 Modal
function openEditModal(btn) {
    document.getElementById('modal-title').textContent = '編輯公告';
    document.getElementById('add-form').style.display = 'none';
    document.getElementById('edit-form').style.display = 'block';

    document.getElementById('edit-id').value = btn.getAttribute('data-id');
    document.getElementById('edit-title').value = btn.getAttribute('data-title');
    document.getElementById('edit-content').value = btn.getAttribute('data-content');
    document.getElementById('edit-status').value = btn.getAttribute('data-status');

    document.getElementById('modal-overlay').classList.add('active');
}

// 關閉 Modal
function closeModal() {
    document.getElementById('modal-overlay').classList.remove('active');
}

// 點擊背景關閉
document.getElementById('modal-overlay').addEventListener('click', function(e) {
    if (e.target.id === 'modal-overlay') {
        closeModal();
    }
});

// ESC 鍵關閉
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeModal();
    }
});
