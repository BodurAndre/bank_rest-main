/**
 * JavaScript для работы с уведомлениями и запросами
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeRequestBlockButtons();
});

/**
 * Инициализация кнопок запроса блокировки
 */
function initializeRequestBlockButtons() {
    const requestBlockButtons = document.querySelectorAll('.request-block-btn');
    requestBlockButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            console.log('Request block button clicked for card:', cardId);
            requestCardBlock(cardId);
        });
    });
}

/**
 * Запрос на блокировку карты
 */
function requestCardBlock(cardId) {
    console.log('requestCardBlock called with cardId:', cardId);
    // Показываем модальное окно для ввода причины
    showBlockRequestModal(cardId);
}

/**
 * Показывает модальное окно для запроса блокировки
 */
function showBlockRequestModal(cardId) {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>🔒 Запрос на блокировку карты</h3>
                <button type="button" class="modal-close" onclick="closeBlockRequestModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p>Укажите причину блокировки карты:</p>
                <form id="blockRequestForm">
                    <div class="form-group">
                        <label for="blockReason">Причина блокировки:</label>
                        <select id="blockReason" required>
                            <option value="">Выберите причину</option>
                            <option value="Утеряна карта">Утеряна карта</option>
                            <option value="Кража карты">Кража карты</option>
                            <option value="Подозрительные операции">Подозрительные операции</option>
                            <option value="Смена номера телефона">Смена номера телефона</option>
                            <option value="Другое">Другое</option>
                        </select>
                    </div>
                    <div class="form-group" id="customReasonGroup" style="display: none;">
                        <label for="customReason">Укажите причину:</label>
                        <input type="text" id="customReason" placeholder="Введите причину блокировки">
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeBlockRequestModal()">Отмена</button>
                <button type="button" class="btn btn-warning" onclick="submitBlockRequest(${cardId})">Отправить запрос</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Показываем модальное окно
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
    
    // Обработка выбора причины
    const reasonSelect = modal.querySelector('#blockReason');
    const customReasonGroup = modal.querySelector('#customReasonGroup');
    const customReasonInput = modal.querySelector('#customReason');
    
    reasonSelect.addEventListener('change', function() {
        if (this.value === 'Другое') {
            customReasonGroup.style.display = 'block';
            customReasonInput.required = true;
        } else {
            customReasonGroup.style.display = 'none';
            customReasonInput.required = false;
        }
    });
}

/**
 * Закрывает модальное окно запроса блокировки
 */
function closeBlockRequestModal() {
    const modal = document.querySelector('.modal-overlay');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(modal);
        }, 300);
    }
}

/**
 * Отправляет запрос на блокировку карты
 */
function submitBlockRequest(cardId) {
    console.log('submitBlockRequest called with cardId:', cardId);
    const reasonSelect = document.querySelector('#blockReason');
    const customReason = document.querySelector('#customReason');
    
    let reason = reasonSelect.value;
    
    if (reason === 'Другое') {
        if (!customReason.value.trim()) {
            showCustomNotification('❌ Ошибка', 'Пожалуйста, укажите причину блокировки', 'error');
            return;
        }
        reason = customReason.value.trim();
    }
    
    if (!reason) {
        showCustomNotification('❌ Ошибка', 'Пожалуйста, выберите причину блокировки', 'error');
        return;
    }
    
    // Отправляем запрос
    fetch(`/cards/${cardId}/request-block`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `reason=${encodeURIComponent(reason)}`
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('Ошибка при отправке запроса');
        }
    })
    .then(message => {
        showCustomNotification('✅ Успех', message, 'success');
        closeBlockRequestModal();
        
        // Изменяем кнопку на "Запрос отправлен"
        const button = document.querySelector(`.request-block-btn[data-card-id="${cardId}"]`);
        console.log('Looking for button with cardId:', cardId);
        console.log('Found button:', button);
        
        if (button) {
            button.textContent = 'Запрос на блокировку отправлен';
            button.disabled = true;
            button.classList.remove('btn-warning');
            button.classList.add('btn-secondary');
            console.log('Button updated successfully');
        } else {
            console.log('Button not found!');
        }
        
        // Убираем редирект, так как статус теперь сохраняется в БД
        // setTimeout(() => {
        //     window.location.reload();
        // }, 1500);
    })
    .catch(error => {
        showCustomNotification('❌ Ошибка', 'Ошибка при отправке запроса: ' + error.message, 'error');
    });
}

// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        closeBlockRequestModal();
    }
});
