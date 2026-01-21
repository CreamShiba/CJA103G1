function switchImage(element) {
    var newSrc = element.querySelector('img').src;
    var mainImg = document.getElementById('mainImg');
    mainImg.style.opacity = 0;
    setTimeout(function() { mainImg.src = newSrc; mainImg.style.opacity = 1; }, 200);
    document.querySelectorAll('.thumb-box').forEach(box => box.classList.remove('active'));
    element.classList.add('active');
}

function updateQty(change) {
    var input = document.getElementById('qtyInput');
    var currentVal = parseInt(input.value);
    var maxVal = parseInt(input.getAttribute('max'));
    var newVal = currentVal + change;
    if (newVal >= 1 && newVal <= maxVal) input.value = newVal;
}

function openReportModal() { document.getElementById('reportModal').style.display = 'flex'; }
function closeReportModal() { document.getElementById('reportModal').style.display = 'none'; }
function submitReport() { alert('已收到您的檢舉！'); closeReportModal(); }

const userIcon = document.getElementById('user-icon');
const userDropdown = document.getElementById('user-dropdown');
userIcon.addEventListener('click', (e) => {
    e.stopPropagation();
    userDropdown.classList.toggle('show');
});
window.addEventListener('click', () => {
    if (userDropdown.classList.contains('show')) userDropdown.classList.remove('show');
});