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