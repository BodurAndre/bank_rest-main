/**
 * JavaScript для запроса разблокировки карты
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeUnblockRequestButtons();
});

/**
 * Инициализация кнопок запроса разблокировки
 */
function initializeUnblockRequestButtons() {
    const unblockRequestButtons = document.querySelectorAll('.request-unblock-btn');
    unblockRequestButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            console.log('Unblock request button clicked for card:', cardId);
            showUnblockRequestModal(cardId);
        });
    });
}

/**
 * Показывает модальное окно для запроса разблокировки
 */
function showUnblockRequestModal(cardId) {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>🔓 Запрос на разблокировку карты</h3>
                <button type="button" class="modal-close" onclick="closeUnblockRequestModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p>Укажите причину, по которой карта должна быть разблокирована:</p>
                <form id="unblockRequestForm">
                    <div class="form-group">
                        <label for="unblockReason">Причина разблокировки:</label>
                        <select id="unblockReason" required>
                            <option value="">Выберите причину</option>
                            <option value="Ошибка при блокировке">Ошибка при блокировке</option>
                            <option value="Карта была заблокирована по ошибке">Карта была заблокирована по ошибке</option>
                            <option value="Проблема решена">Проблема решена</option>
                            <option value="Другое">Другое</option>
                        </select>
                    </div>
                    <div class="form-group" id="customReasonGroup" style="display: none;">
                        <label for="customUnblockReason">Укажите причину:</label>
                        <textarea id="customUnblockReason" rows="3" placeholder="Опишите причину разблокировки"></textarea>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeUnblockRequestModal()">Отмена</button>
                <button type="button" class="btn btn-primary" onclick="submitUnblockRequest(${cardId})">Отправить запрос</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Показываем модальное окно
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
    
    // Обработка изменения селекта
    const reasonSelect = modal.querySelector('#unblockReason');
    const customReasonGroup = modal.querySelector('#customReasonGroup');
    
    reasonSelect.addEventListener('change', function() {
        if (this.value === 'Другое') {
            customReasonGroup.style.display = 'block';
        } else {
            customReasonGroup.style.display = 'none';
        }
    });
}

/**
 * Закрывает модальное окно запроса разблокировки
 */
function closeUnblockRequestModal() {
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
 * Отправляет запрос на разблокировку карты
 */
function submitUnblockRequest(cardId) {
    const reasonSelect = document.querySelector('#unblockReason');
    const customReason = document.querySelector('#customUnblockReason');
    
    let reason = reasonSelect.value;
    
    if (!reason) {
        showCustomNotification('❌ Ошибка', 'Пожалуйста, выберите причину разблокировки', 'error');
        return;
    }
    
    if (reason === 'Другое') {
        if (!customReason.value.trim()) {
            showCustomNotification('❌ Ошибка', 'Пожалуйста, укажите причину разблокировки', 'error');
            return;
        }
        reason = customReason.value.trim();
    }
    
    console.log('Submitting unblock request for card:', cardId, 'reason:', reason);
    
    // Отправляем запрос
    fetch(`/cards/${cardId}/request-unblock`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `reason=${encodeURIComponent(reason)}`
    })
    .then(response => {
        console.log('Unblock request - response status:', response.status);
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => {
                console.log('Unblock request - error response:', text);
                throw new Error('Ошибка при отправке запроса: ' + text);
            });
        }
    })
    .then(message => {
        console.log('Unblock request - success message:', message);
        showCustomNotification('✅ Успех', message, 'success');
        closeUnblockRequestModal();
        
        // Изменяем кнопку на "Запрос отправлен"
        const button = document.querySelector(`.request-unblock-btn[data-card-id="${cardId}"]`);
        if (button) {
            button.textContent = 'Запрос на разблокировку отправлен';
            button.disabled = true;
            button.classList.remove('btn-primary');
            button.classList.add('btn-secondary');
        }
    })
    .catch(error => {
        console.log('Unblock request - error:', error);
        showCustomNotification('❌ Ошибка', 'Ошибка при отправке запроса: ' + error.message, 'error');
    });
}

// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        const modal = e.target;
        if (modal.querySelector('#unblockReason')) {
            closeUnblockRequestModal();
        }
    }
});
