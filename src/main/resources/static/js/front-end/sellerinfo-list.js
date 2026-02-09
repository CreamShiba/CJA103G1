
document.getElementById('file-input').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = function(event) {
            const preview = document.getElementById('image-preview');
            const previewImg = document.getElementById('preview-img');
            const uploadText = document.getElementById('upload-text');

            previewImg.src = event.target.result;
            preview.style.display = 'block';
            uploadText.textContent = '更換圖片';

            // 隱藏現有圖片
            const currentImage = document.querySelector('.current-image');
            if (currentImage) {
                currentImage.style.display = 'none';
            }
        };
        reader.readAsDataURL(file);
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('file-input');

    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                // 檢查檔案大小 (5MB)
                if (file.size > 5 * 1024 * 1024) {
                    alert('圖片大小不能超過 5MB');
                    this.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = function(event) {
                    const previewContainer = document.getElementById('image-preview');
                    const previewImg = document.getElementById('preview-img');
                    const uploadText = document.getElementById('upload-text');
                    const currentImage = document.querySelector('.current-image');

                    // 1. 設定新圖來源並顯示
                    previewImg.src = event.target.result;
                    previewContainer.style.display = 'block';

                    // 2. 更新文字
                    uploadText.textContent = '更換圖片';

                    // 3. 隱藏舊圖 (如果有)
                    if (currentImage) {
                        currentImage.style.display = 'none';
                    }

                    // 4. 平滑滾動到預覽區
                    previewContainer.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                };
                reader.readAsDataURL(file);
            }
        });
    }
});


