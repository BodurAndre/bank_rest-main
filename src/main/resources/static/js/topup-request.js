/**
 * JavaScript для запроса пополнения карты
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeTopupRequestButtons();
});

/**
 * Инициализация кнопок запроса пополнения
 */
function initializeTopupRequestButtons() {
    const topupRequestButtons = document.querySelectorAll('.request-topup-btn');
    topupRequestButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            const cardNumber = this.getAttribute('data-card-number');
            const cardBalance = this.getAttribute('data-card-balance');
            console.log('Topup request button clicked for card:', cardId);
            showTopupRequestModal(cardId, cardNumber, cardBalance);
        });
    });
}

/**
 * Показывает модальное окно для запроса пополнения
 */
function showTopupRequestModal(cardId, cardNumber, cardBalance) {
    // Удаляем существующие модальные окна пополнения
    const existingModals = document.querySelectorAll('.modal-overlay');
    existingModals.forEach(modal => {
        if (modal.querySelector('#topupAmount')) {
            document.body.removeChild(modal);
        }
    });
    
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'modal-overlay';
    modal.innerHTML = `
        <div class="modal-content">
            <div class="modal-header">
                <h3>💰 Запрос на пополнение карты</h3>
                <button type="button" class="modal-close" onclick="closeTopupRequestModal()">&times;</button>
            </div>
            <div class="modal-body">
                <p>Карта: <strong>${cardNumber}</strong></p>
                <p>Текущий баланс: <strong>${parseFloat(cardBalance).toFixed(2)} ₽</strong></p>
                <form id="topupRequestForm">
                    <div class="form-group">
                        <label for="topupRequestAmount">Сумма пополнения:</label>
                        <input type="number" id="topupRequestAmount" name="amount" step="0.01" min="0.01" required placeholder="Введите сумму" value="">
                        <small class="form-help">Минимальная сумма: 0.01 ₽</small>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeTopupRequestModal()">Отмена</button>
                <button type="button" class="btn btn-primary" onclick="submitTopupRequest(${cardId})">Отправить запрос</button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Проверяем, что модальное окно добавлено
    console.log('Modal created and added to DOM');
    console.log('Modal element:', modal);
    console.log('Input element in modal:', modal.querySelector('#topupAmount'));
    
    // Показываем модальное окно
    setTimeout(() => {
        modal.classList.add('show');
        console.log('Modal shown');
    }, 10);
}

/**
 * Закрывает модальное окно запроса пополнения
 */
function closeTopupRequestModal() {
    const modals = document.querySelectorAll('.modal-overlay');
    modals.forEach(modal => {
        if (modal.querySelector('#topupRequestAmount') || modal.querySelector('#topupAmount')) {
            modal.classList.remove('show');
            setTimeout(() => {
                if (document.body.contains(modal)) {
                    document.body.removeChild(modal);
                }
            }, 300);
        }
    });
}

/**
 * Отправляет запрос на пополнение карты
 */
function submitTopupRequest(cardId) {
    console.log('Topup request - cardId:', cardId);
    
    // Проверяем все возможные селекторы
    const amountInput = document.querySelector('#topupRequestAmount') || 
                       document.querySelector('#topupAmount') ||
                       document.querySelector('input[type="number"]') ||
                       document.querySelector('input[name="amount"]');
    
    console.log('Topup request - amountInput element:', amountInput);
    console.log('Topup request - all number inputs:', document.querySelectorAll('input[type="number"]'));
    console.log('Topup request - all inputs in modal:', document.querySelectorAll('.modal-overlay input'));
    console.log('Topup request - modal overlay exists:', document.querySelector('.modal-overlay'));
    console.log('Topup request - all inputs on page:', document.querySelectorAll('input'));
    
    if (!amountInput) {
        console.log('Error: Amount input not found');
        showCustomNotification('❌ Ошибка', 'Поле ввода суммы не найдено', 'error');
        return;
    }
    
    const amountValue = amountInput.value.trim();
    console.log('Topup request - amountValue:', amountValue);
    
    // Проверяем, что поле не пустое
    if (!amountValue) {
        console.log('Error: Empty amount value');
        showCustomNotification('❌ Ошибка', 'Пожалуйста, введите сумму', 'error');
        return;
    }
    
    const amount = parseFloat(amountValue);
    console.log('Topup request - parsed amount:', amount);
    
    // Проверяем, что это корректное число
    if (isNaN(amount) || amount <= 0) {
        console.log('Error: Invalid amount:', amount);
        showCustomNotification('❌ Ошибка', 'Пожалуйста, введите корректную сумму (больше 0)', 'error');
        return;
    }
    
    // Проверяем минимальную сумму
    if (amount < 0.01) {
        console.log('Error: Amount too small:', amount);
        showCustomNotification('❌ Ошибка', 'Минимальная сумма пополнения: 0.01 ₽', 'error');
        return;
    }
    
    console.log('Topup request - sending amount:', amount);
    
    // Отправляем запрос
    fetch(`/cards/${cardId}/request-topup`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `amount=${amount}`
    })
    .then(response => {
        console.log('Topup request - response status:', response.status);
        if (response.ok) {
            return response.text();
        } else {
            return response.text().then(text => {
                console.log('Topup request - error response:', text);
                throw new Error('Ошибка при отправке запроса: ' + text);
            });
        }
    })
    .then(message => {
        console.log('Topup request - success message:', message);
        showCustomNotification('✅ Успех', message, 'success');
        closeTopupRequestModal();
        
        // Изменяем кнопку на "Запрос отправлен"
        const button = document.querySelector(`.request-topup-btn[data-card-id="${cardId}"]`);
        if (button) {
            button.textContent = 'Запрос на пополнение отправлен';
            button.disabled = true;
            button.classList.remove('btn-primary');
            button.classList.add('btn-secondary');
        }
    })
    .catch(error => {
        console.log('Topup request - error:', error);
        showCustomNotification('❌ Ошибка', 'Ошибка при отправке запроса: ' + error.message, 'error');
    });
}

// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        const modal = e.target;
        if (modal.querySelector('#topupRequestAmount') || modal.querySelector('#topupAmount')) {
            closeTopupRequestModal();
        }
    }
});
