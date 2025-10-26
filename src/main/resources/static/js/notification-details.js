/**
 * JavaScript для работы с деталями уведомлений
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeDetailsButtons();
});

/**
 * Инициализация кнопок "Подробнее"
 */
function initializeDetailsButtons() {
    const detailsButtons = document.querySelectorAll('.details-btn');
    detailsButtons.forEach(button => {
        button.addEventListener('click', function() {
            const notificationId = this.getAttribute('data-notification-id');
            const cardId = this.getAttribute('data-card-id');
            const userName = this.getAttribute('data-user-name');
            const cardNumber = this.getAttribute('data-card-number');
            const reason = this.getAttribute('data-reason');
            const notificationType = this.getAttribute('data-notification-type');
            
            showNotificationDetails(notificationId, cardId, userName, cardNumber, reason, notificationType);
        });
    });
}

/**
 * Показывает модальное окно с деталями уведомления
 */
function showNotificationDetails(notificationId, cardId, userName, cardNumber, reason, notificationType) {
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'notification-details-modal';
    modal.innerHTML = `
        <div class="notification-details-content">
            <div class="notification-details-header">
                <h3 id="modalTitle">🔍 Детали уведомления</h3>
                <button type="button" class="notification-details-close" onclick="closeNotificationDetails()">&times;</button>
            </div>
            <div class="notification-details-body">
                <div class="detail-row">
                    <span class="detail-label">Пользователь:</span>
                    <span class="detail-value">${userName}</span>
                </div>
                ${cardNumber ? `
                <div class="detail-row">
                    <span class="detail-label">Номер карты:</span>
                    <span class="detail-value">${cardNumber}</span>
                </div>
                ` : ''}
                <div class="detail-row">
                    <span class="detail-label">Описание:</span>
                    <span class="detail-value">${reason}</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">ID уведомления:</span>
                    <span class="detail-value">${notificationId}</span>
                </div>
            </div>
            <div class="notification-details-actions" id="notificationActions">
                <!-- Кнопки будут добавлены динамически в зависимости от типа уведомления -->
                <button type="button" class="btn btn-secondary" onclick="closeNotificationDetails()">
                    Закрыть
                </button>
            </div>
        </div>
    `;
    
    document.body.appendChild(modal);
    
    // Устанавливаем заголовок и добавляем соответствующую кнопку
    setModalTitleAndActions(notificationType, cardId, notificationId);
    
    // Показываем модальное окно
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
}

/**
 * Закрывает модальное окно деталей уведомления
 */
function closeNotificationDetails() {
    const modal = document.querySelector('.notification-details-modal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(modal);
        }, 300);
    }
}

/**
 * Блокирует карту из уведомления
 */
function blockCardFromNotification(cardId, notificationId) {
    showCustomConfirm(
        '🚫 Блокировка карты',
        'Вы уверены, что хотите заблокировать эту карту?',
        'Заблокировать',
        'Отмена',
        () => {
            // Отправляем запрос на блокировку карты
            fetch(`/cards/${cardId}/block`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `reason=Блокировка по запросу пользователя`
            })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Ошибка при блокировке карты');
                }
            })
            .then(message => {
                // Отмечаем уведомление как обработанное
                return fetch(`/notifications/${notificationId}/process`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    }
                });
            })
            .then(response => {
                if (response.ok) {
                    showCustomNotification('✅ Успех', 'Карта заблокирована и уведомление обработано', 'success');
                    closeNotificationDetails();
                    
                    // Обновляем статус уведомления в DOM вместо перезагрузки страницы
                    updateNotificationStatusInDOM(notificationId);
                } else {
                    throw new Error('Ошибка при обработке уведомления');
                }
            })
            .catch(error => {
                showCustomNotification('❌ Ошибка', 'Ошибка при блокировке карты: ' + error.message, 'error');
            });
        }
    );
}

/**
 * Пополняет карту из уведомления (на сумму из уведомления)
 */
function topupCardFromNotification(cardId, notificationId) {
    // Получаем сумму из data-атрибута кнопки
    const button = document.querySelector(`.details-btn[data-notification-id="${notificationId}"]`);
    let amount = button ? parseFloat(button.getAttribute('data-amount')) : 1000;
    
    console.log('Topup from notification - amount from data:', amount);
    console.log('Topup from notification - raw data-amount:', button ? button.getAttribute('data-amount') : 'button not found');
    
    // Если сумма не найдена или NaN, извлекаем из сообщения
    if (isNaN(amount) || amount <= 0) {
        const reason = button ? button.getAttribute('data-reason') : '';
        console.log('Topup from notification - extracting from reason:', reason);
        
        // Извлекаем сумму из сообщения (формат: "на сумму 500,00 руб.")
        const amountMatch = reason.match(/на сумму ([\d,]+) руб\./);
        if (amountMatch) {
            // Заменяем запятую на точку для parseFloat
            const amountStr = amountMatch[1].replace(',', '.');
            amount = parseFloat(amountStr);
            console.log('Topup from notification - extracted amount:', amount);
        } else {
            amount = 1000; // Fallback
            console.log('Topup from notification - using fallback amount:', amount);
        }
    }
    
    // Показываем подтверждение с суммой из уведомления
    showCustomConfirm(
        '💰 Пополнение карты',
        `Пополнить карту на ${amount} ₽?`,
        'Пополнить',
        'Отмена',
        () => {
            submitTopupFromNotification(cardId, notificationId, amount);
        }
    );
}


/**
 * Отправляет запрос на пополнение карты из уведомления
 */
function submitTopupFromNotification(cardId, notificationId, amount) {
    console.log('Topup from notification - cardId:', cardId, 'notificationId:', notificationId, 'amount:', amount);
    
    // Отправляем запрос на пополнение
    fetch(`/cards/${cardId}/topup`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `amount=${amount}`
    })
    .then(response => {
        if (response.ok) {
            return response.text();
        } else {
            throw new Error('Ошибка при пополнении карты');
        }
    })
    .then(message => {
        console.log('Topup successful, marking notification as processed');
        // Отмечаем уведомление как обработанное
        return fetch(`/notifications/${notificationId}/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        });
    })
    .then(response => {
        if (response.ok) {
            console.log('Notification marked as processed');
            showCustomNotification('✅ Успех', 'Карта пополнена на ' + amount + ' ₽ и уведомление обработано', 'success');
            closeNotificationDetails();
            
            // Обновляем статус уведомления в DOM вместо перезагрузки страницы
            updateNotificationStatusInDOM(notificationId);
        } else {
            throw new Error('Ошибка при обработке уведомления');
        }
    })
    .catch(error => {
        showCustomNotification('❌ Ошибка', 'Ошибка при пополнении карты: ' + error.message, 'error');
    });
}

/**
 * Разблокирует карту из уведомления
 */
function unblockCardFromNotification(cardId, notificationId) {
    // Показываем подтверждение
    showCustomConfirm(
        '🔓 Разблокировка карты',
        'Разблокировать карту?',
        'Разблокировать',
        'Отмена',
        () => {
            // Отправляем запрос на разблокировку
            fetch(`/cards/${cardId}/activate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `reason=${encodeURIComponent('Разблокировка по запросу пользователя через уведомление')}`
            })
            .then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Ошибка при разблокировке карты');
                }
            })
            .then(message => {
                console.log('Unblock successful, marking notification as processed');
                // Отмечаем уведомление как обработанное
                return fetch(`/notifications/${notificationId}/process`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    }
                });
            })
            .then(response => {
                if (response.ok) {
                    console.log('Notification marked as processed');
                    showCustomNotification('✅ Успех', 'Карта разблокирована и уведомление обработано', 'success');
                    closeNotificationDetails();
                    
                    // Обновляем статус уведомления в DOM вместо перезагрузки страницы
                    updateNotificationStatusInDOM(notificationId);
                } else {
                    throw new Error('Ошибка при обработке уведомления');
                }
            })
            .catch(error => {
                showCustomNotification('❌ Ошибка', 'Ошибка при разблокировке карты: ' + error.message, 'error');
            });
        }
    );
}

/**
 * Устанавливает заголовок и добавляет соответствующую кнопку в зависимости от типа уведомления
 */
function setModalTitleAndActions(notificationType, cardId, notificationId) {
    const titleElement = document.getElementById('modalTitle');
    const actionsContainer = document.getElementById('notificationActions');
    
    // Очищаем контейнер кнопок (оставляем только кнопку "Закрыть")
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.innerHTML = '';
    actionsContainer.appendChild(closeButton);
    
    switch (notificationType) {
        case 'CARD_BLOCK_REQUEST':
            titleElement.textContent = '🔍 Детали запроса на блокировку';
            addBlockButton(cardId, notificationId);
            break;
            
        case 'CARD_TOPUP_REQUEST':
            titleElement.textContent = '🔍 Детали запроса на пополнение';
            addTopupButton(cardId, notificationId);
            break;
            
        case 'CARD_UNBLOCK_REQUEST':
            titleElement.textContent = '🔍 Детали запроса на разблокировку';
            addUnblockButton(cardId, notificationId);
            break;
            
        case 'CARD_CREATE_REQUEST':
            titleElement.textContent = '🔍 Детали запроса на создание карты';
            addCreateCardButton(notificationId);
            break;
            
        case 'CARD_RECREATE_REQUEST':
            titleElement.textContent = '🔍 Детали запроса на пересоздание карты';
            addRecreateCardButton(cardId, notificationId);
            break;
            
        default:
            titleElement.textContent = '🔍 Детали уведомления';
            break;
    }
}

/**
 * Добавляет кнопку блокировки карты
 */
function addBlockButton(cardId, notificationId) {
    const actionsContainer = document.getElementById('notificationActions');
    const blockButton = document.createElement('button');
    blockButton.type = 'button';
    blockButton.className = 'btn btn-warning';
    blockButton.innerHTML = '🚫 Заблокировать карту';
    blockButton.onclick = () => blockCardFromNotification(cardId, notificationId);
    
    // Вставляем перед кнопкой "Закрыть"
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.insertBefore(blockButton, closeButton);
}

/**
 * Добавляет кнопку пополнения карты
 */
function addTopupButton(cardId, notificationId) {
    const actionsContainer = document.getElementById('notificationActions');
    const topupButton = document.createElement('button');
    topupButton.type = 'button';
    topupButton.className = 'btn btn-success';
    topupButton.innerHTML = '💰 Пополнить карту';
    topupButton.onclick = () => topupCardFromNotification(cardId, notificationId);
    
    // Вставляем перед кнопкой "Закрыть"
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.insertBefore(topupButton, closeButton);
}

/**
 * Добавляет кнопку разблокировки карты
 */
function addUnblockButton(cardId, notificationId) {
    const actionsContainer = document.getElementById('notificationActions');
    const unblockButton = document.createElement('button');
    unblockButton.type = 'button';
    unblockButton.className = 'btn btn-info';
    unblockButton.innerHTML = '🔓 Разблокировать карту';
    unblockButton.onclick = () => unblockCardFromNotification(cardId, notificationId);
    
    // Вставляем перед кнопкой "Закрыть"
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.insertBefore(unblockButton, closeButton);
}

/**
 * Добавляет кнопку создания карты
 */
function addCreateCardButton(notificationId) {
    const actionsContainer = document.getElementById('notificationActions');
    const createButton = document.createElement('button');
    createButton.type = 'button';
    createButton.className = 'btn btn-primary';
    createButton.innerHTML = '➕ Создать карту';
    createButton.onclick = () => createCardFromNotification(notificationId);
    
    // Вставляем перед кнопкой "Закрыть"
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.insertBefore(createButton, closeButton);
}

function addRecreateCardButton(cardId, notificationId) {
    const actionsContainer = document.getElementById('notificationActions');
    const recreateButton = document.createElement('button');
    recreateButton.type = 'button';
    recreateButton.className = 'btn btn-warning';
    recreateButton.innerHTML = '🔄 Пересоздать карту';
    recreateButton.onclick = () => recreateCardFromNotification(cardId, notificationId);
    
    // Вставляем перед кнопкой "Закрыть"
    const closeButton = actionsContainer.querySelector('button[onclick="closeNotificationDetails()"]');
    actionsContainer.insertBefore(recreateButton, closeButton);
}

/**
 * Создает карту из уведомления (для админа)
 */
function createCardFromNotification(notificationId) {
    const button = document.querySelector(`.details-btn[data-notification-id="${notificationId}"]`);
    const userName = button ? button.getAttribute('data-user-name') : 'пользователя';
    
    showCustomConfirm(
        '➕ Создание карты',
        `Создать новую карту для ${userName}?`,
        'Создать',
        'Отмена',
        () => {
            // Получаем email пользователя из сообщения уведомления
            const reason = button ? button.getAttribute('data-reason') : '';
            const userEmailMatch = reason.match(/Пользователь\s+(.+?)\s+запросил/);
            
            if (!userEmailMatch) {
                showCustomNotification('❌ Ошибка', 'Не удалось определить пользователя', 'error');
                return;
            }
            
            // Извлекаем срок действия из сообщения
            const expiryMatch = reason.match(/Срок действия:\s+(\d{2}\/\d{2})/);
            const expiryDate = expiryMatch ? expiryMatch[1] : '12/26';
            
            // Создаем карту через API создания карт
            const createCardData = {
                ownerEmail: getUserEmailFromNotification(notificationId),
                expiryDate: expiryDate
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
                    throw new Error('Ошибка создания карты');
                }
                return response.json();
            })
            .then(card => {
                // Отмечаем уведомление как обработанное
                return fetch(`/notifications/${notificationId}/process`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
            })
            .then(processResponse => {
                if (!processResponse.ok) {
                    console.warn('Не удалось отметить уведомление как обработанное');
                }
                showCustomNotification('✅ Успех', 'Карта создана и уведомление обработано', 'success');
                closeNotificationDetails();
                
                // Обновляем статус уведомления в DOM вместо перезагрузки страницы
                updateNotificationStatusInDOM(notificationId);
            })
            .catch(error => {
                console.error('Error creating card:', error);
                showCustomNotification('❌ Ошибка', 'Ошибка при создании карты: ' + error.message, 'error');
            });
        }
    );
}

/**
 * Пересоздает карту из уведомления (для админа)
 */
function recreateCardFromNotification(cardId, notificationId) {
    const button = document.querySelector(`.details-btn[data-notification-id="${notificationId}"]`);
    const userName = button ? button.getAttribute('data-user-name') : 'пользователя';

    showCustomConfirm(
        '🔄 Пересоздание карты',
        `Пересоздать карту для ${userName}?`,
        'Пересоздать',
        'Отмена',
        () => {
            // Получаем новую дату истечения из data-атрибута
            const newExpiryDate = button ? button.getAttribute('data-new-expiry-date') : '12/26';
            const userEmail = getUserEmailFromNotification(notificationId);
            
            console.log('Recreating card:', { cardId, newExpiryDate, userEmail, notificationId });

            // Сначала создаем новую карту
            const createCardData = {
                ownerEmail: userEmail,
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
                console.log('New card created:', newCard);
                
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
                
                console.log('Old card deleted, marking notification as processed');
                
                // Отмечаем уведомление как обработанное
                return fetch(`/api/notifications/${notificationId}/mark-processed`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });
            })
            .then(response => {
                if (!response.ok) {
                    console.warn('Warning: Could not mark notification as processed, but card was recreated');
                    // Не бросаем ошибку, так как карта уже пересоздана
                }
                
                showCustomNotification('✅ Успех', 'Карта пересоздана успешно', 'success');
                closeNotificationDetails();
                
                // Обновляем статус уведомления в DOM вместо перезагрузки страницы
                updateNotificationStatusInDOM(notificationId);
            })
            .catch(error => {
                console.error('Error recreating card:', error);
                showCustomNotification('❌ Ошибка', 'Ошибка при пересоздании карты: ' + error.message, 'error');
            });
        }
    );
}

/**
 * Обновляет статус уведомления в DOM без перезагрузки страницы
 */
function updateNotificationStatusInDOM(notificationId) {
    const notificationElement = document.querySelector(`[data-notification-id="${notificationId}"]`).closest('.notification-item');
    if (notificationElement) {
        // Находим кнопку "Подробнее" и заменяем на "Обработано"
        const actionsContainer = notificationElement.querySelector('.notification-actions');
        if (actionsContainer) {
            actionsContainer.innerHTML = '<span class="badge badge-secondary">Обработано</span>';
        }
        
        // Обновляем статус в заголовке
        const statusBadge = notificationElement.querySelector('.status-badge');
        if (statusBadge) {
            statusBadge.textContent = 'Обработано';
            statusBadge.className = 'status-badge status-processed';
        }
    }
}

/**
 * Получает email пользователя из уведомления
 */
function getUserEmailFromNotification(notificationId) {
    // Получаем email из data-атрибута кнопки
    const button = document.querySelector(`.details-btn[data-notification-id="${notificationId}"]`);
    if (!button) {
        return 'user@git.com'; // fallback
    }
    
    const userEmail = button.getAttribute('data-user-email');
    return userEmail || 'user@git.com'; // fallback если email не найден
}


// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    if (e.target.classList.contains('modal-overlay')) {
        closeNotificationDetails();
    }
});
