document.addEventListener('DOMContentLoaded', function() {
    initializeRecreateCardButtons();
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
        options += `<option value="${monthStr}/${shortYear}">${monthStr}/${shortYear}</option>`;
    }
    
    return options;
}

function initializeRecreateCardButtons() {
    const recreateButtons = document.querySelectorAll('.recreate-card-btn');
    recreateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            const cardNumber = this.getAttribute('data-card-number');
            showCardRecreateModal(cardId, cardNumber);
        });
    });
}

function showCardRecreateModal(cardId, cardNumber) {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'modal-overlay show';
    modal.id = 'cardRecreateModal';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>🔄 Пересоздание карты</h3>
                <button type="button" class="modal-close" onclick="closeCardRecreateModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p><strong>Карта:</strong> ${cardNumber}</p>
                <p>Выберите новый срок действия для пересоздания карты:</p>
                
                <div class="form-group">
                    <label for="newExpiryDate">Новый срок действия:</label>
                    <select id="newExpiryDate" class="form-control" required>
                        <option value="">Выберите срок действия</option>
                        ${generateExpiryDateOptions()}
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="submitCardRecreateRequest('${cardId}')">
                    🔄 Отправить запрос
                </button>
                <button type="button" class="btn btn-secondary" onclick="closeCardRecreateModal()">
                    Отмена
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => { modal.classList.add('show'); }, 10);
}

function closeCardRecreateModal() {
    const modal = document.getElementById('cardRecreateModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}

function submitCardRecreateRequest(cardId) {
    const newExpiryDateSelect = document.getElementById('newExpiryDate');
    const newExpiryDate = newExpiryDateSelect.value;

    if (!newExpiryDate) {
        showCustomNotification('❌ Ошибка', 'Пожалуйста, выберите новый срок действия карты', 'error');
        return;
    }

    showCustomConfirm(
        '🔄 Пересоздание карты',
        `Вы точно хотите отправить запрос на пересоздание карты с новым сроком действия ${newExpiryDate}?`,
        'Да, отправить запрос',
        'Отмена',
        () => {
            fetch(`/cards/${cardId}/request-recreate`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: `newExpiryDate=${encodeURIComponent(newExpiryDate)}`
            })
            .then(response => response.text())
            .then(message => {
                closeCardRecreateModal(); // Close modal first
                setTimeout(() => {
                    showCustomNotification('✅ Успех', message, 'success');
                }, 100);
                
                // Обновляем состояние кнопки
                const button = document.querySelector(`.recreate-card-btn[data-card-id="${cardId}"]`);
                if (button) {
                    button.textContent = '📤 Запрос отправлен';
                    button.disabled = true;
                    button.classList.remove('btn-warning');
                    button.classList.add('btn-secondary');
                }
            })
            .catch(error => {
                console.error('Card recreate request error:', error);
                showCustomNotification('❌ Ошибка', 'Произошла ошибка при отправке запроса', 'error');
            });
        }
    );
}

// Функция для показа уведомлений (если не определена в других файлах)
function showCustomNotification(title, message, type) {
    // Создаем уведомление
    const notification = document.createElement('div');
    notification.className = `notification notification-${type} show`;
    notification.innerHTML = `
        <div class="notification-content">
            <div class="notification-icon">${type === 'success' ? '✅' : '❌'}</div>
            <div class="notification-text">
                <strong>${title}</strong><br>${message}
            </div>
        </div>
    `;
    
    document.body.appendChild(notification);
    
    // Автоматически скрываем через 5 секунд
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            notification.remove();
        }, 300);
    }, 5000);
}

// Функция для показа подтверждения (если не определена в других файлах)
function showCustomConfirm(title, message, confirmText, cancelText, onConfirm) {
    const modal = document.createElement('div');
    modal.className = 'modal-overlay show';
    modal.id = 'confirmModal';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>${title}</h3>
            </div>
            <div class="modal-body">
                <p>${message}</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="confirmAction()">
                    ${confirmText}
                </button>
                <button type="button" class="btn btn-secondary" onclick="closeConfirmModal()">
                    ${cancelText}
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Сохраняем callback
    window.confirmCallback = onConfirm;
    
    setTimeout(() => { modal.classList.add('show'); }, 10);
}

function confirmAction() {
    if (window.confirmCallback) {
        window.confirmCallback();
        window.confirmCallback = null;
    }
    closeConfirmModal();
}

function closeConfirmModal() {
    const modal = document.getElementById('confirmModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}
