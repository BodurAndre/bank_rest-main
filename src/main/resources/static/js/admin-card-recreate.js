document.addEventListener('DOMContentLoaded', function() {
    initializeAdminRecreateCardButtons();
});

function initializeAdminRecreateCardButtons() {
    const recreateButtons = document.querySelectorAll('.admin-recreate-card-btn');
    recreateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            const cardNumber = this.getAttribute('data-card-number');
            const ownerEmail = this.getAttribute('data-owner-email');
            showAdminCardRecreateModal(cardId, cardNumber, ownerEmail);
        });
    });
}

function showAdminCardRecreateModal(cardId, cardNumber, ownerEmail) {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'modal-overlay show';
    modal.id = 'adminCardRecreateModal';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>🔄 Пересоздание истекшей карты (Админ)</h3>
                <button type="button" class="modal-close" onclick="closeAdminCardRecreateModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p><strong>Карта:</strong> ${cardNumber}</p>
                <p><strong>Владелец:</strong> ${ownerEmail}</p>
                <p>Выберите новый срок действия для пересоздания карты:</p>
                
                <div class="form-group">
                    <label for="adminNewExpiryDate">Новый срок действия:</label>
                    <select id="adminNewExpiryDate" class="form-control" required>
                        <option value="">Выберите срок действия</option>
                        ${generateExpiryDateOptions()}
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="submitAdminCardRecreateRequest('${cardId}', '${ownerEmail}')">
                    🔄 Пересоздать карту
                </button>
                <button type="button" class="btn btn-secondary" onclick="closeAdminCardRecreateModal()">
                    Отмена
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    setTimeout(() => { modal.classList.add('show'); }, 10);
}

function closeAdminCardRecreateModal() {
    const modal = document.getElementById('adminCardRecreateModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}

function submitAdminCardRecreateRequest(cardId, ownerEmail) {
    const newExpiryDateSelect = document.getElementById('adminNewExpiryDate');
    const newExpiryDate = newExpiryDateSelect.value;

    if (!newExpiryDate) {
        showCustomNotification('❌ Ошибка', 'Пожалуйста, выберите новый срок действия карты', 'error');
        return;
    }

    showCustomConfirm(
        '🔄 Пересоздание карты',
        `Вы точно хотите пересоздать карту с новым сроком действия ${newExpiryDate}?`,
        'Да, пересоздать',
        'Отмена',
        () => {
            console.log('Admin recreating card:', { cardId, newExpiryDate, ownerEmail });

            // Сначала создаем новую карту
            const createCardData = {
                ownerEmail: ownerEmail,
                expiryDate: newExpiryDate
            };

            fetch('/api/cards', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(createCardData)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Ошибка при создании новой карты');
                }
                return response.json();
            })
            .then(newCard => {
                console.log('New card created by admin:', newCard);
                
                // Теперь удаляем старую карту
                return fetch(`/api/cards/${cardId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
            })
            .then(response => {
                if (!response.ok) {
                    console.warn('Warning: Could not delete old card, but new card was created');
                    // Не бросаем ошибку, так как новая карта уже создана
                }
                
                closeAdminCardRecreateModal();
                showCustomNotification('✅ Успех', 'Карта пересоздана успешно', 'success');
                
                // Перезагружаем страницу для обновления списка карт
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                console.error('Error recreating card:', error);
                showCustomNotification('❌ Ошибка', 'Ошибка при пересоздании карты: ' + error.message, 'error');
            });
        }
    );
}

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
    modal.id = 'adminConfirmModal';
    
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>${title}</h3>
            </div>
            <div class="modal-body">
                <p>${message}</p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-primary" onclick="confirmAdminAction()">
                    ${confirmText}
                </button>
                <button type="button" class="btn btn-secondary" onclick="closeAdminConfirmModal()">
                    ${cancelText}
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Сохраняем callback
    window.adminConfirmCallback = onConfirm;
    
    setTimeout(() => { modal.classList.add('show'); }, 10);
}

function confirmAdminAction() {
    if (window.adminConfirmCallback) {
        window.adminConfirmCallback();
        window.adminConfirmCallback = null;
    }
    closeAdminConfirmModal();
}

function closeAdminConfirmModal() {
    const modal = document.getElementById('adminConfirmModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.remove();
        }, 300);
    }
}

