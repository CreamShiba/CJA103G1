/* register.js - 包含防呆與記憶功能的版本 */
document.addEventListener('DOMContentLoaded', function() {

    const avatarInput = document.getElementById('memAvatar');
    const avatarPreview = document.getElementById('avatarPreview');
    const defaultPlaceholderSrc = avatarPreview.src;

    // 用來暫存「上一次有效」的檔案列表
    // DataTransfer 是瀏覽器用來處理拖拉或剪貼簿檔案的 API，這裡我們借用它來模擬 FileList
    let lastValidFiles = new DataTransfer();

    avatarInput.addEventListener('change', function(event) {

        const file = event.target.files[0];

        // 情況 A: 使用者選了一個有效的圖片檔案
        if (file && file.type.startsWith('image/')) {

            // 1. 讀取並顯示預覽圖
            const reader = new FileReader();
            reader.onload = function(e) {
                avatarPreview.src = e.target.result;
                avatarPreview.classList.add('preview-loaded');
            }
            reader.readAsDataURL(file);

            // 2. 更新我們的「備份」
            lastValidFiles.items.clear(); // 清除舊的
            lastValidFiles.items.add(file); // 加入新的

        }
        // 情況 B: 使用者按了取消 (files 為空) 或選了非圖片檔案
        else {

            // 檢查我們有沒有「備份」 (之前是否已經選過有效圖片)
            if (lastValidFiles.files.length > 0) {
                // === 關鍵步驟 ===
                // 如果有備份，就把備份的檔案「塞回去」給 input
                avatarInput.files = lastValidFiles.files;

                // 這裡 "return" 結束函式，不做任何清除動作
                // 這樣預覽圖就會維持原本的樣子
                return;
            }

            // 情況 C: 使用者第一次就按取消，或是想把原本選的刪除 (如果這是唯一意圖)
            // 既然沒有備份，就代表真的要清空
            avatarInput.value = '';
            avatarPreview.src = defaultPlaceholderSrc;
            avatarPreview.classList.remove('preview-loaded');
        }
    });
});