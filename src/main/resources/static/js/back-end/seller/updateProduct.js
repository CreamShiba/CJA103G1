const fileInput = document.getElementById('fileInput');
const previewContainer = document.getElementById('preview-container');

const dataTransfer = new DataTransfer();

fileInput.addEventListener('change', function(e) {
    const files = e.target.files;

    if (files) {
        Array.from(files).forEach(file => {
            if (file.type.match('image.*')) {

                dataTransfer.items.add(file);

                const imgBox = document.createElement('div');
                imgBox.className = 'preview-img-box';

                const img = document.createElement('img');
                const reader = new FileReader();
                reader.onload = function(event) {
                    img.src = event.target.result;
                }
                reader.readAsDataURL(file);

                const removeBtn = document.createElement('button');
                removeBtn.className = 'btn-remove-img';
                removeBtn.innerHTML = '×';

                removeBtn.onclick = function(evt) {
                    evt.preventDefault();
                    imgBox.remove();
                    updateFileInput(file.name);
                };

                imgBox.appendChild(img);
                imgBox.appendChild(removeBtn);
                previewContainer.appendChild(imgBox);
            }
        });
    }
    fileInput.files = dataTransfer.files;
});

function updateFileInput(fileNameToRemove) {
    const newDataTransfer = new DataTransfer();
    Array.from(dataTransfer.files).forEach(file => {
        if (file.name !== fileNameToRemove) {
            newDataTransfer.items.add(file);
        }
    });
    dataTransfer.items.clear();
    Array.from(newDataTransfer.files).forEach(file => dataTransfer.items.add(file));
    fileInput.files = dataTransfer.files;
}

let deleteIds = [];

function markForDelete(imgNo) {
    const imgElement = document.getElementById('old-img-' + imgNo);

    if (!deleteIds.includes(imgNo)) {
        // 1. 加入刪除清單
        deleteIds.push(imgNo);
        // 2. 視覺回饋：變透明並加紅色邊框
        imgElement.style.opacity = "0.3";
        imgElement.style.border = "2px solid red";
    } else {
        // 3. 再次點擊則取消刪除
        deleteIds = deleteIds.filter(id => id !== imgNo);
        imgElement.style.opacity = "1";
        imgElement.style.border = "none";
    }

    // 4. 將陣列轉為逗號分隔的字串，填入隱藏欄位 (例如: "10,12,15")
    document.getElementById('deleteImageNos').value = deleteIds.join(',');
}