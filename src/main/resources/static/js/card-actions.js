// Инициализация обработчиков событий для активации, блокировки и удаления карт
document.addEventListener('DOMContentLoaded', function() {
    initializeActivateButtons();
    initializeBlockButtons();
    initializeDeleteButtons();
});

function initializeActivateButtons() {
    const activateButtons = document.querySelectorAll('.activate-btn');
    
    activateButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            console.log('Activate button clicked for card:', cardId);
            activateCard(cardId);
        });
    });
}

function initializeBlockButtons() {
    const blockButtons = document.querySelectorAll('.block-btn');
    
    blockButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            const reason = this.getAttribute('data-reason');
            console.log('Block button clicked for card:', cardId, 'reason:', reason);
            blockCard(cardId, reason);
        });
    });
}

function initializeDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.delete-btn');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            const cardId = this.getAttribute('data-card-id');
            console.log('Delete button clicked for card:', cardId);
            deleteCard(cardId);
        });
    });
}

// Функция активации карты
function activateCard(cardId) {
    showCustomConfirm(
        '🔓 Активация карты',
        'Вы уверены, что хотите активировать эту карту?',
        'Активировать',
        'Отмена',
        () => {
            fetch(`/cards/${cardId}/activate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Ошибка при активации карты');
                }
            })
            .then(message => {
                showCustomNotification('✅ Успех', message, 'success');
                // Перезагружаем страницу для обновления данных
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                showCustomNotification('❌ Ошибка', 'Ошибка при активации карты: ' + error.message, 'error');
            });
        }
    );
}

// Функция блокировки карты
function blockCard(cardId, reason) {
    showCustomConfirm(
        '🔒 Блокировка карты',
        'Вы уверены, что хотите заблокировать эту карту?',
        'Заблокировать',
        'Отмена',
        () => {
            fetch(`/cards/${cardId}/block`, {
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
                    throw new Error('Ошибка при блокировке карты');
                }
            })
            .then(message => {
                showCustomNotification('✅ Успех', message, 'success');
                // Перезагружаем страницу для обновления данных
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                showCustomNotification('❌ Ошибка', 'Ошибка при блокировке карты: ' + error.message, 'error');
            });
        }
    );
}

// Функция удаления карты
function deleteCard(cardId) {
    showCustomConfirm(
        '🗑️ Удаление карты',
        'Вы уверены, что хотите удалить эту карту? Это действие необратимо!',
        'Удалить',
        'Отмена',
        () => {
            fetch(`/cards/${cardId}/delete`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                }
            })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Ошибка при удалении карты');
                }
            })
            .then(message => {
                showCustomNotification('✅ Успех', message, 'success');
                // Перезагружаем страницу для обновления данных
                setTimeout(() => {
                    window.location.reload();
                }, 1500);
            })
            .catch(error => {
                showCustomNotification('❌ Ошибка', 'Ошибка при удалении карты: ' + error.message, 'error');
            });
        }
    );
}

