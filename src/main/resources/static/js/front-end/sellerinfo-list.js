
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
