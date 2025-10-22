// Transfers Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Инициализация страницы переводов
    initializeTransfersPage();
});

function initializeTransfersPage() {
    // Инициализируем форму перевода
    initializeTransferForm();
    
    // Добавляем валидацию
    addFormValidation();
    
    // Инициализируем селекты карт
    initializeCardSelects();
    
    // Добавляем анимации
    addTransferAnimations();
}

function initializeTransferForm() {
    const form = document.querySelector('.transfer-form');
    if (!form) return;
    
    form.addEventListener('submit', function(e) {
        if (!validateTransferForm()) {
            e.preventDefault();
            return false;
        }
        
        // Показываем индикатор загрузки
        showTransferLoading();
    });
}

function validateTransferForm() {
    const fromCardId = document.getElementById('fromCardId');
    const toCardId = document.getElementById('toCardId');
    const amount = document.getElementById('amount');
    
    let isValid = true;
    
    // Очищаем предыдущие ошибки
    clearFormErrors();
    
    // Проверяем карту отправителя
    if (!fromCardId.value) {
        showFieldError(fromCardId, 'Выберите карту отправителя');
        isValid = false;
    }
    
    // Проверяем карту получателя
    if (!toCardId.value) {
        showFieldError(toCardId, 'Выберите карту получателя');
        isValid = false;
    }
    
    // Проверяем, что карты разные
    if (fromCardId.value && toCardId.value && fromCardId.value === toCardId.value) {
        showFieldError(toCardId, 'Нельзя переводить на ту же карту');
        isValid = false;
    }
    
    // Проверяем сумму
    if (!amount.value || parseFloat(amount.value) <= 0) {
        showFieldError(amount, 'Введите корректную сумму');
        isValid = false;
    }
    
    return isValid;
}

function showFieldError(field, message) {
    field.style.borderColor = '#dc3545';
    
    const errorDiv = document.createElement('div');
    errorDiv.className = 'field-error';
    errorDiv.textContent = message;
    errorDiv.style.color = '#dc3545';
    errorDiv.style.fontSize = '0.8rem';
    errorDiv.style.marginTop = '0.25rem';
    
    field.parentNode.appendChild(errorDiv);
}

function clearFormErrors() {
    const errors = document.querySelectorAll('.field-error');
    errors.forEach(error => error.remove());
    
    const fields = document.querySelectorAll('.form-group input, .form-group select');
    fields.forEach(field => {
        field.style.borderColor = '#ddd';
    });
}

function initializeCardSelects() {
    const fromCardSelect = document.getElementById('fromCardId');
    const toCardSelect = document.getElementById('toCardId');
    
    if (fromCardSelect && toCardSelect) {
        // Обновляем опции получателя при изменении отправителя
        fromCardSelect.addEventListener('change', function() {
            updateToCardOptions(fromCardSelect.value, toCardSelect);
        });
        
        // Обновляем опции отправителя при изменении получателя
        toCardSelect.addEventListener('change', function() {
            updateFromCardOptions(toCardSelect.value, fromCardSelect);
        });
    }
}

function updateToCardOptions(selectedFromId, toSelect) {
    const options = toSelect.querySelectorAll('option');
    options.forEach(option => {
        if (option.value === selectedFromId) {
            option.style.display = 'none';
            option.disabled = true;
        } else {
            option.style.display = 'block';
            option.disabled = false;
        }
    });
}

function updateFromCardOptions(selectedToId, fromSelect) {
    const options = fromSelect.querySelectorAll('option');
    options.forEach(option => {
        if (option.value === selectedToId) {
            option.style.display = 'none';
            option.disabled = true;
        } else {
            option.style.display = 'block';
            option.disabled = false;
        }
    });
}

function addTransferAnimations() {
    // Анимация появления карт
    const cardPreviews = document.querySelectorAll('.card-preview');
    cardPreviews.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
    
    // Анимация действий
    const actionCards = document.querySelectorAll('.action-card');
    actionCards.forEach((card, index) => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, (cardPreviews.length * 100) + (index * 100));
    });
}

function showTransferLoading() {
    const submitButton = document.querySelector('button[type="submit"]');
    if (submitButton) {
        const originalText = submitButton.textContent;
        submitButton.textContent = 'Выполняется перевод...';
        submitButton.disabled = true;
        
        // Добавляем спиннер
        const spinner = document.createElement('span');
        spinner.innerHTML = ' ⏳';
        submitButton.appendChild(spinner);
    }
}

function clearForm() {
    const form = document.querySelector('.transfer-form');
    if (form) {
        form.reset();
        clearFormErrors();
    }
}

// Функция для показа уведомлений
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    // Стили для уведомления
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 1rem 1.5rem;
        border-radius: 8px;
        color: white;
        font-weight: bold;
        z-index: 1000;
        transform: translateX(100%);
        transition: transform 0.3s ease;
    `;
    
    if (type === 'success') {
        notification.style.background = '#28a745';
    } else if (type === 'error') {
        notification.style.background = '#dc3545';
    }
    
    document.body.appendChild(notification);
    
    // Анимация появления
    setTimeout(() => {
        notification.style.transform = 'translateX(0)';
    }, 100);
    
    // Убираем уведомление через 3 секунды
    setTimeout(() => {
        notification.style.transform = 'translateX(100%)';
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// Функция для обновления баланса карт после перевода
function updateCardBalances(fromCardId, toCardId, amount) {
    // Обновляем баланс карты отправителя
    const fromCardElement = document.querySelector(`[data-card-id="${fromCardId}"]`);
    if (fromCardElement) {
        const balanceElement = fromCardElement.querySelector('.card-balance');
        if (balanceElement) {
            const currentBalance = parseFloat(balanceElement.textContent.replace(' ₽', ''));
            const newBalance = currentBalance - parseFloat(amount);
            balanceElement.textContent = newBalance.toFixed(2) + ' ₽';
            
            // Анимация обновления
            balanceElement.style.transform = 'scale(1.1)';
            balanceElement.style.color = '#dc3545';
            
            setTimeout(() => {
                balanceElement.style.transform = 'scale(1)';
                balanceElement.style.color = '';
            }, 500);
        }
    }
    
    // Обновляем баланс карты получателя
    const toCardElement = document.querySelector(`[data-card-id="${toCardId}"]`);
    if (toCardElement) {
        const balanceElement = toCardElement.querySelector('.card-balance');
        if (balanceElement) {
            const currentBalance = parseFloat(balanceElement.textContent.replace(' ₽', ''));
            const newBalance = currentBalance + parseFloat(amount);
            balanceElement.textContent = newBalance.toFixed(2) + ' ₽';
            
            // Анимация обновления
            balanceElement.style.transform = 'scale(1.1)';
            balanceElement.style.color = '#28a745';
            
            setTimeout(() => {
                balanceElement.style.transform = 'scale(1)';
                balanceElement.style.color = '';
            }, 500);
        }
    }
}

// Глобальные функции для использования в HTML
window.clearForm = clearForm;
window.showNotification = showNotification;
