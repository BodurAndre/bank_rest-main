/**
 * JavaScript для запроса создания новой карты
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeRequestCardButtons();
});

function generateExpiryDateOptions() {
    const now = new Date();
    const currentMonth = now.getMonth() + 1; // getMonth() returns 0-11, so add 1
    const currentYear = now.getFullYear();
    
    // Минимальное обслуживание 2 года
    const startYear = currentYear + 2;
    const endYear = startYear + 3; // Показываем 4 года вперед
    
    let options = '';
    
    for (let year = startYear; year <= endYear; year++) {
        const shortYear = year.toString().slice(-2); // Последние 2 цифры года
        const monthStr = currentMonth.toString().padStart(2, '0'); // Добавляем 0 если нужно
        const yearsFromNow = year - currentYear;
        options += `<option value="${monthStr}/${shortYear}">${monthStr}/${shortYear} (${yearsFromNow} ${yearsFromNow === 1 ? 'год' : yearsFromNow < 5 ? 'года' : 'лет'})</option>`;
    }
    
    return options;
}

/**
 * Инициализация кнопок запроса создания карты
 */
function initializeRequestCardButtons() {
    const requestCardButtons = document.querySelectorAll('.request-card-btn');
    requestCardButtons.forEach(button => {
        button.addEventListener('click', function() {
            showCardCreateRequestModal();
        });
    });
}

/**
 * Показывает модальное окно запроса создания карты
 */
function showCardCreateRequestModal() {
    // Удаляем существующие модальные окна
    const existingModals = document.querySelectorAll('.modal-overlay');
    existingModals.forEach(modal => {
        if (modal.querySelector('#cardExpiryDate')) {
            document.body.removeChild(modal);
        }
    });

    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>➕ Запрос на создание карты</h3>
                <button type="button" class="modal-close" onclick="closeCardCreateRequestModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p>Вы хотите запросить создание новой банковской карты?</p>
                <div class="form-group">
                    <label for="cardExpiryDate">Срок действия карты:</label>
                    <select id="cardExpiryDate" name="expiryDate" required>
                        <option value="">Выберите срок действия</option>
                        ${generateExpiryDateOptions()}
                    </select>
                </div>
                <p class="modal-info">
                    ℹ️ После отправки запроса администратор рассмотрит его и создаст карту.
                    Вы получите уведомление о результате.
                </p>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-success" onclick="submitCardCreateRequest()">
                    ✅ Да, создать запрос
                </button>
                <button type="button" class="btn btn-secondary" onclick="closeCardCreateRequestModal()">
                    ❌ Отмена
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
}

/**
 * Закрывает модальное окно запроса создания карты
 */
function closeCardCreateRequestModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            if (document.body.contains(modal)) {
                document.body.removeChild(modal);
            }
        }, 300);
    }
}

/**
 * Отправляет запрос на создание карты
 */
function submitCardCreateRequest() {
    const expiryDateSelect = document.querySelector('#cardExpiryDate');
    
    if (!expiryDateSelect || !expiryDateSelect.value) {
        showCustomNotification('❌ Ошибка', 'Пожалуйста, выберите срок действия карты', 'error');
        return;
    }
    
    const expiryDate = expiryDateSelect.value;
    
    console.log('Submitting card create request with expiry date:', expiryDate);
    
    fetch('/cards/request-create', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `expiryDate=${encodeURIComponent(expiryDate)}`
    })
    .then(response => {
        console.log('Card create request response status:', response.status);
        return response.text();
    })
    .then(message => {
        console.log('Card create request response:', message);
        
        // Сначала закрываем модальное окно
        closeCardCreateRequestModal();
        
        // Затем показываем уведомление
        setTimeout(() => {
            showCustomNotification('✅ Успех', message, 'success');
        }, 100);
        
        // Обновляем кнопку, чтобы показать, что запрос отправлен
        const button = document.querySelector('.request-card-btn');
        if (button) {
            button.textContent = '📤 Запрос отправлен';
            button.disabled = true;
            button.classList.remove('btn-success');
            button.classList.add('btn-secondary');
        }
    })
    .catch(error => {
        console.error('Card create request error:', error);
        showCustomNotification('❌ Ошибка', 'Произошла ошибка при отправке запроса: ' + error.message, 'error');
    });
}

// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        closeCardCreateRequestModal();
    }
});

/**
 * Показывает кастомное уведомление
 */
function showCustomNotification(title, message, type) {
    // Используем существующую функцию из cards.js, если она есть
    if (typeof showStatusNotification === 'function') {
        showStatusNotification(message, type);
    } else {
        // Простая реализация, если функции нет
        alert(title + ': ' + message);
    }
}
