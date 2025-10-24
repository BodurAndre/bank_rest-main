// Cards Page JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Инициализация страницы карт
    initializeCardsPage();
    
    // Инициализация кнопок пополнения
    initializeTopupButtons();
});

function initializeCardsPage() {
    // Добавляем анимации для карт
    addCardAnimations();
    
    // Инициализируем фильтры
    initializeFilters();
    
    // Добавляем подтверждения для действий
    addConfirmationDialogs();
    
    // Инициализируем пагинацию
    initializePagination();
}

function addCardAnimations() {
    const cards = document.querySelectorAll('.card-item');
    
    cards.forEach((card, index) => {
        // Задержка анимации для каждой карты
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        
        setTimeout(() => {
            card.style.transition = 'all 0.5s ease';
            card.style.opacity = '1';
            card.style.transform = 'translateY(0)';
        }, index * 100);
    });
}

function initializeFilters() {
    const filterForm = document.querySelector('.filter-form');
    if (!filterForm) return;
    
    // Автоматическая отправка формы при изменении селекта статуса
    const statusSelect = filterForm.querySelector('select[name="status"]');
    if (statusSelect) {
        statusSelect.addEventListener('change', function() {
            filterForm.submit();
        });
    }
    
    // Поиск с задержкой
    const searchInput = filterForm.querySelector('input[name="search"]');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                filterForm.submit();
            }, 500);
        });
    }
}

function addConfirmationDialogs() {
    // Подтверждение блокировки карты
    const blockButtons = document.querySelectorAll('button[onclick*="confirm"]');
    blockButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            showCustomConfirm(
                '🔒 Блокировка карты',
                'Вы уверены, что хотите заблокировать эту карту?',
                'Заблокировать',
                'Отмена',
                () => {
                    button.closest('form').submit();
                }
            );
        });
    });
    
    // Подтверждение удаления карты
    const deleteButtons = document.querySelectorAll('form[action*="delete"]');
    deleteButtons.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            showCustomConfirm(
                '🗑️ Удаление карты',
                'Вы уверены, что хотите удалить эту карту? Это действие необратимо.',
                'Удалить',
                'Отмена',
                () => {
                    form.submit();
                }
            );
        });
    });
    
    // Подтверждение активации карты
    const activateButtons = document.querySelectorAll('form[action*="activate"]');
    activateButtons.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            showCustomConfirm(
                '✅ Активация карты',
                'Вы уверены, что хотите активировать эту карту?',
                'Активировать',
                'Отмена',
                () => {
                    form.submit();
                }
            );
        });
    });
}

function initializePagination() {
    const paginationLinks = document.querySelectorAll('.btn-pagination');
    
    paginationLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            // Добавляем индикатор загрузки
            showLoadingIndicator();
        });
    });
}

function initializeTopupButtons() {
    const topupButtons = document.querySelectorAll('.topup-btn');
    
    topupButtons.forEach(button => {
        button.addEventListener('click', function() {
            let cardId = this.getAttribute('data-card-id');
            let cardNumber = this.getAttribute('data-card-number');
            let cardBalanceStr = this.getAttribute('data-card-balance');
            
            console.log('Raw attributes:', {
                cardId: cardId,
                cardNumber: cardNumber,
                cardBalanceStr: cardBalanceStr
            });
            
            // Если data-атрибуты не работают, используем скрытые поля
            if (!cardId || cardId.includes('${') || !cardNumber || cardNumber.includes('${')) {
                console.log('Trying fallback method with hidden fields...');
                const hiddenData = this.closest('.card-item').querySelector('.hidden-card-data');
                if (hiddenData) {
                    cardId = hiddenData.querySelector('.card-id')?.textContent;
                    cardNumber = hiddenData.querySelector('.card-number')?.textContent;
                    cardBalanceStr = hiddenData.querySelector('.card-balance')?.textContent;
                    
                    console.log('Fallback data:', {
                        cardId: cardId,
                        cardNumber: cardNumber,
                        cardBalanceStr: cardBalanceStr
                    });
                }
            }
            
            // Проверяем, что данные загружены правильно
            if (!cardId || !cardNumber) {
                console.error('Card data not loaded properly');
                alert('Ошибка загрузки данных карты. Попробуйте обновить страницу.');
                return;
            }
            
            const cardBalance = parseFloat(cardBalanceStr);
            
            if (isNaN(cardBalance)) {
                console.error('Invalid balance:', cardBalanceStr);
                alert('Ошибка: некорректный баланс карты');
                return;
            }
            
            console.log('Topup button clicked:', cardId, cardNumber, cardBalance);
            openTopupModal(cardId, cardNumber, cardBalance);
        });
    });
}

function showLoadingIndicator() {
    const container = document.querySelector('.container');
    if (container) {
        const loader = document.createElement('div');
        loader.className = 'loading-overlay';
        loader.innerHTML = '<div class="spinner"></div><p>Загрузка...</p>';
        container.appendChild(loader);
        
        // Убираем индикатор через 2 секунды
        setTimeout(() => {
            if (loader.parentNode) {
                loader.parentNode.removeChild(loader);
            }
        }, 2000);
    }
}

// Функция для обновления баланса карты (если нужно)
function updateCardBalance(cardId, newBalance) {
    const cardElement = document.querySelector(`[data-card-id="${cardId}"]`);
    if (cardElement) {
        const balanceElement = cardElement.querySelector('.balance-amount');
        if (balanceElement) {
            balanceElement.textContent = newBalance + ' ₽';
            
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

// Функция для показа уведомлений
function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // Анимация появления
    setTimeout(() => {
        notification.classList.add('show');
    }, 100);
    
    // Убираем уведомление через 3 секунды
    setTimeout(() => {
        notification.classList.remove('show');
        setTimeout(() => {
            if (notification.parentNode) {
                notification.parentNode.removeChild(notification);
            }
        }, 300);
    }, 3000);
}

// Обработка ошибок AJAX (если используется)
function handleAjaxError(xhr, status, error) {
    console.error('AJAX Error:', error);
    showNotification('Произошла ошибка при загрузке данных', 'error');
}

// Функция для обновления статуса карты
function updateCardStatus(cardId, newStatus) {
    const cardElement = document.querySelector(`[data-card-id="${cardId}"]`);
    if (cardElement) {
        const statusElement = cardElement.querySelector('.card-status');
        if (statusElement) {
            statusElement.textContent = newStatus;
            statusElement.className = `card-status ${newStatus.toLowerCase()}`;
        }
    }
}

// User Search Functionality
document.addEventListener('DOMContentLoaded', function() {
    initializeUserSearch();
});

function initializeUserSearch() {
    const userSearchInput = document.getElementById('userSearch');
    const userSearchResults = document.getElementById('userSearchResults');
    const ownerEmailInput = document.getElementById('ownerEmail');
    
    if (!userSearchInput || !userSearchResults || !ownerEmailInput) return;
    
    let searchTimeout;
    let selectedIndex = -1;
    
    userSearchInput.addEventListener('input', function() {
        const query = this.value.trim();
        
        if (query.length < 2) {
            hideSearchResults();
            return;
        }
        
        clearTimeout(searchTimeout);
        searchTimeout = setTimeout(() => {
            searchUsers(query);
        }, 300);
    });
    
    userSearchInput.addEventListener('keydown', function(e) {
        const results = userSearchResults.querySelectorAll('.user-search-result');
        
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selectedIndex = Math.min(selectedIndex + 1, results.length - 1);
            updateSelection(results);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selectedIndex = Math.max(selectedIndex - 1, -1);
            updateSelection(results);
        } else if (e.key === 'Enter') {
            e.preventDefault();
            if (selectedIndex >= 0 && results[selectedIndex]) {
                selectUser(results[selectedIndex]);
            }
        } else if (e.key === 'Escape') {
            hideSearchResults();
        }
    });
    
    // Скрываем результаты при клике вне поля
    document.addEventListener('click', function(e) {
        if (!userSearchInput.contains(e.target) && !userSearchResults.contains(e.target)) {
            hideSearchResults();
        }
    });
    
    function searchUsers(query) {
        console.log('Searching users with query:', query);
        fetch(`/api/users/search?q=${encodeURIComponent(query)}`)
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(users => {
                console.log('Found users:', users);
                displaySearchResults(users);
            })
            .catch(error => {
                console.error('Search error:', error);
                userSearchResults.innerHTML = '<div class="user-search-result">Ошибка при поиске пользователей: ' + error.message + '</div>';
                userSearchResults.style.display = 'block';
            });
    }
    
    function displaySearchResults(users) {
        userSearchResults.innerHTML = '';
        
        if (users.length === 0) {
            userSearchResults.innerHTML = '<div class="user-search-result">Пользователи не найдены</div>';
        } else {
            users.forEach((user, index) => {
                const result = document.createElement('div');
                result.className = 'user-search-result';
                result.innerHTML = `
                    <div class="user-info">
                        <div class="user-name">${user.firstName} ${user.lastName}</div>
                        <div class="user-email">${user.email}</div>
                        <div class="user-role">${user.role}</div>
                    </div>
                `;
                
                result.addEventListener('click', () => selectUser(result));
                userSearchResults.appendChild(result);
            });
        }
        
        userSearchResults.style.display = 'block';
        selectedIndex = -1;
    }
    
    function updateSelection(results) {
        results.forEach((result, index) => {
            result.classList.toggle('selected', index === selectedIndex);
        });
    }
    
    function selectUser(resultElement) {
        const userInfo = resultElement.querySelector('.user-info');
        const name = userInfo.querySelector('.user-name').textContent;
        const email = userInfo.querySelector('.user-email').textContent;
        
        userSearchInput.value = name;
        ownerEmailInput.value = email;
        
        hideSearchResults();
    }
    
    function hideSearchResults() {
        userSearchResults.style.display = 'none';
        selectedIndex = -1;
    }
}

// Beautiful Custom Confirm Dialog
function showCustomConfirm(title, message, confirmText, cancelText, onConfirm) {
    // Создаем overlay
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        backdrop-filter: blur(5px);
        z-index: 10000;
        display: flex;
        align-items: center;
        justify-content: center;
        animation: fadeIn 0.3s ease-out;
    `;
    
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.className = 'custom-modal';
    modal.style.cssText = `
        background: white;
        border-radius: 20px;
        padding: 2rem;
        max-width: 400px;
        width: 90%;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
        animation: slideInUp 0.3s ease-out;
        position: relative;
        overflow: hidden;
    `;
    
    // Добавляем градиентную полосу сверху
    const gradientBar = document.createElement('div');
    gradientBar.style.cssText = `
        position: absolute;
        top: 0;
        left: 0;
        right: 0;
        height: 4px;
        background: linear-gradient(90deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
    `;
    modal.appendChild(gradientBar);
    
    // Создаем содержимое
    modal.innerHTML = `
        <div style="margin-top: 1rem;">
            <h3 style="margin: 0 0 1rem 0; color: #333; font-size: 1.5rem; text-align: center;">${title}</h3>
            <p style="margin: 0 0 2rem 0; color: #666; line-height: 1.5; text-align: center;">${message}</p>
            <div style="display: flex; gap: 1rem; justify-content: center;">
                <button class="btn-cancel" style="
                    background: #f8f9fa;
                    color: #666;
                    border: 2px solid #e1e5e9;
                    padding: 0.75rem 1.5rem;
                    border-radius: 12px;
                    cursor: pointer;
                    font-weight: 600;
                    transition: all 0.3s ease;
                ">${cancelText}</button>
                <button class="btn-confirm" style="
                    background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                    color: white;
                    border: none;
                    padding: 0.75rem 1.5rem;
                    border-radius: 12px;
                    cursor: pointer;
                    font-weight: 600;
                    transition: all 0.3s ease;
                ">${confirmText}</button>
            </div>
        </div>
    `;
    
    // Добавляем стили для анимаций
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes slideInUp {
            from { 
                opacity: 0;
                transform: translateY(30px) scale(0.95);
            }
            to { 
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }
        .btn-cancel:hover {
            background: #e9ecef !important;
            color: #333 !important;
            transform: translateY(-1px);
        }
        .btn-confirm:hover {
            transform: translateY(-2px);
            box-shadow: 0 10px 25px rgba(220, 53, 69, 0.3);
        }
    `;
    document.head.appendChild(style);
    
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    
    // Обработчики событий
    const cancelBtn = modal.querySelector('.btn-cancel');
    const confirmBtn = modal.querySelector('.btn-confirm');
    
    function closeModal() {
        overlay.style.animation = 'fadeOut 0.3s ease-out';
        modal.style.animation = 'slideOutDown 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(overlay);
            document.head.removeChild(style);
        }, 300);
    }
    
    cancelBtn.addEventListener('click', closeModal);
    confirmBtn.addEventListener('click', () => {
        closeModal();
        onConfirm();
    });
    
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            closeModal();
        }
    });
    
    // Добавляем стили для закрытия
    const closeStyle = document.createElement('style');
    closeStyle.textContent = `
        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
        @keyframes slideOutDown {
            from { 
                opacity: 1;
                transform: translateY(0) scale(1);
            }
            to { 
                opacity: 0;
                transform: translateY(30px) scale(0.95);
            }
        }
    `;
    document.head.appendChild(closeStyle);
}

// Инициализация уведомлений при загрузке страницы
setTimeout(() => {
    // Проверяем наличие сообщений в шаблонах
    const successElements = document.querySelectorAll('[th\\:if*="successMessage"]');
    const errorElements = document.querySelectorAll('[th\\:if*="errorMessage"]');
    
    successElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'success');
        }
    });
    
    errorElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'error');
        }
    });
}, 500);

// Status Notification Function
function showStatusNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `status-notification status-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        padding: 1.5rem;
        max-width: 400px;
        z-index: 10001;
        animation: slideInRight 0.3s ease-out;
        border-left: 4px solid ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#667eea'};
    `;
    
    notification.innerHTML = `
        <div style="display: flex; align-items: center; gap: 1rem;">
            <div style="
                width: 2.5rem;
                height: 2.5rem;
                border-radius: 50%;
                background: ${type === 'success' ? 'rgba(40, 167, 69, 0.2)' : type === 'error' ? 'rgba(220, 53, 69, 0.2)' : 'rgba(102, 126, 234, 0.2)'};
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1.5rem;
                color: ${type === 'success' ? '#28a745' : type === 'error' ? '#dc3545' : '#667eea'};
            ">
                ${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}
            </div>
            <div style="flex: 1;">
                <p style="margin: 0; color: #333; font-size: 1rem; font-weight: 600;">${message}</p>
            </div>
            <button onclick="this.parentElement.parentElement.remove()" style="
                background: none;
                border: none;
                font-size: 1.5rem;
                color: #999;
                cursor: pointer;
                padding: 0;
                width: 2rem;
                height: 2rem;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                transition: background 0.3s ease;
            " onmouseover="this.style.background='rgba(0,0,0,0.1)'" onmouseout="this.style.background='none'">
                &times;
            </button>
        </div>
    `;
    
    // Добавляем стили для анимации
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from { 
                opacity: 0;
                transform: translateX(100%);
            }
            to { 
                opacity: 1;
                transform: translateX(0);
            }
        }
        @keyframes slideOutRight {
            from { 
                opacity: 1;
                transform: translateX(0);
            }
            to { 
                opacity: 0;
                transform: translateX(100%);
            }
        }
    `;
    document.head.appendChild(style);
    
    document.body.appendChild(notification);
    
    // Автоматически убираем через 3 секунды
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease-out';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
                if (style.parentNode) {
                    style.parentNode.removeChild(style);
                }
            }, 300);
        }
    }, 3000);
}

// Custom Confirm Dialog Function
function showCustomConfirm(title, message, confirmText = 'Да', cancelText = 'Отмена', onConfirm = null) {
    // Создаем overlay
    const overlay = document.createElement('div');
    overlay.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(0, 0, 0, 0.5);
        backdrop-filter: blur(5px);
        z-index: 10000;
        display: flex;
        align-items: center;
        justify-content: center;
        animation: fadeIn 0.3s ease-out;
    `;
    
    // Создаем модальное окно
    const modal = document.createElement('div');
    modal.style.cssText = `
        background: white;
        border-radius: 20px;
        padding: 2rem;
        max-width: 400px;
        width: 90%;
        box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
        animation: slideInUp 0.3s ease-out;
        position: relative;
    `;
    
    modal.innerHTML = `
        <div style="text-align: center; margin-bottom: 2rem;">
            <div style="
                width: 4rem;
                height: 4rem;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin: 0 auto 1rem;
                font-size: 2rem;
            ">⚠️</div>
            <h3 style="margin: 0 0 1rem 0; color: #333; font-size: 1.5rem;">${title}</h3>
            <p style="margin: 0; color: #666; font-size: 1rem; line-height: 1.5;">${message}</p>
        </div>
        <div style="display: flex; gap: 1rem; justify-content: center;">
            <button id="confirmBtn" style="
                background: linear-gradient(135deg, #dc3545 0%, #c82333 100%);
                color: white;
                border: none;
                padding: 0.75rem 1.5rem;
                border-radius: 12px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
            ">${confirmText}</button>
            <button id="cancelBtn" style="
                background: #f8f9fa;
                color: #666;
                border: 2px solid #e1e5e9;
                padding: 0.75rem 1.5rem;
                border-radius: 12px;
                font-weight: 600;
                cursor: pointer;
                transition: all 0.3s ease;
            ">${cancelText}</button>
        </div>
    `;
    
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    
    // Добавляем стили для анимации
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        @keyframes slideInUp {
            from { 
                opacity: 0;
                transform: translateY(30px) scale(0.95);
            }
            to { 
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }
    `;
    document.head.appendChild(style);
    
    // Обработчики событий
    const confirmBtn = modal.querySelector('#confirmBtn');
    const cancelBtn = modal.querySelector('#cancelBtn');
    
    confirmBtn.addEventListener('click', () => {
        overlay.style.animation = 'fadeOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(overlay);
            document.head.removeChild(style);
        }, 300);
        if (onConfirm) onConfirm();
    });
    
    cancelBtn.addEventListener('click', () => {
        overlay.style.animation = 'fadeOut 0.3s ease-out';
        setTimeout(() => {
            document.body.removeChild(overlay);
            document.head.removeChild(style);
        }, 300);
    });
    
    // Закрытие по клику вне модального окна
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) {
            overlay.style.animation = 'fadeOut 0.3s ease-out';
            setTimeout(() => {
                document.body.removeChild(overlay);
                document.head.removeChild(style);
            }, 300);
        }
    });
    
    // Добавляем стили для исчезновения
    const closeStyle = document.createElement('style');
    closeStyle.textContent = `
        @keyframes fadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }
    `;
    document.head.appendChild(closeStyle);
}

// Инициализация уведомлений при загрузке страницы
setTimeout(() => {
    // Проверяем наличие сообщений в шаблонах
    const successElements = document.querySelectorAll('[th\\:if*="successMessage"]');
    const errorElements = document.querySelectorAll('[th\\:if*="errorMessage"]');
    
    successElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'success');
        }
    });
    
    errorElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'error');
        }
    });
}, 500);

// Topup Modal Functions
function openTopupModal(cardId, cardNumber, currentBalance) {
    console.log('Opening topup modal for card:', cardId, cardNumber, currentBalance);
    
    // Преобразуем параметры в правильные типы
    const cardIdNum = parseInt(cardId);
    const balanceNum = parseFloat(currentBalance);
    
    const modal = document.getElementById('topupModal');
    const cardNumberElement = document.getElementById('topupCardNumber');
    const balanceElement = document.getElementById('topupCurrentBalance');
    const form = document.getElementById('topupForm');
    
    if (!modal) {
        console.error('Modal element not found!');
        return;
    }
    
    if (!cardNumberElement) {
        console.error('Card number element not found!');
        return;
    }
    
    if (!balanceElement) {
        console.error('Balance element not found!');
        return;
    }
    
    if (!form) {
        console.error('Form element not found!');
        return;
    }
    
    // Заполняем данные карты
    cardNumberElement.textContent = cardNumber;
    balanceElement.textContent = balanceNum.toFixed(2);
    
    // Очищаем форму
    form.reset();
    
    // Показываем модальное окно
    console.log('Showing modal...');
    modal.style.display = 'flex';
    // Небольшая задержка для правильного отображения
    setTimeout(() => {
        modal.classList.add('show');
    }, 10);
    
    // Обработчик отправки формы
    form.onsubmit = function(e) {
        e.preventDefault();
        const amount = document.getElementById('topupAmount').value;
        
        if (!amount || amount <= 0) {
            showCustomNotification('❌ Ошибка', 'Пожалуйста, введите корректную сумму', 'error');
            return;
        }
        
        // Показываем подтверждение
        showCustomConfirm(
            '💰 Пополнение карты',
            `Вы уверены, что хотите пополнить карту на ${amount} ₽?`,
            'Пополнить',
            'Отмена',
            () => {
                // Отправляем AJAX запрос
                fetch(`/cards/${cardIdNum}/topup`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `amount=${amount}`
                })
                .then(response => {
                    if (response.ok) {
                        // Закрываем модальное окно
                        closeTopupModal();
                        // Перезагружаем страницу для обновления данных
                        window.location.reload();
                    } else {
                        throw new Error('Ошибка при пополнении карты');
                    }
                })
                .catch(error => {
                    showCustomNotification('❌ Ошибка', 'Ошибка при пополнении карты: ' + error.message, 'error');
                });
            }
        );
    };
}

function closeTopupModal() {
    console.log('Closing topup modal...');
    const modal = document.getElementById('topupModal');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
        }, 300);
    }
}

// Закрытие модального окна при клике вне его
document.addEventListener('click', function(e) {
    const modal = document.getElementById('topupModal');
    if (e.target === modal) {
        closeTopupModal();
    }
});

// Custom Notification Function
function showCustomNotification(title, message, type = 'info') {
    const notification = document.createElement('div');
    notification.className = `custom-notification notification-${type}`;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        background: white;
        border-radius: 15px;
        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
        padding: 1.5rem;
        max-width: 400px;
        z-index: 10001;
        animation: slideInRight 0.3s ease-out;
        border-left: 4px solid ${type === 'error' ? '#dc3545' : type === 'success' ? '#28a745' : '#667eea'};
    `;
    
    notification.innerHTML = `
        <div style="display: flex; align-items: center; gap: 1rem;">
            <div style="
                width: 2.5rem;
                height: 2.5rem;
                border-radius: 50%;
                background: ${type === 'error' ? 'rgba(220, 53, 69, 0.2)' : type === 'success' ? 'rgba(40, 167, 69, 0.2)' : 'rgba(102, 126, 234, 0.2)'};
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 1.5rem;
                color: ${type === 'error' ? '#dc3545' : type === 'success' ? '#28a745' : '#667eea'};
            ">
                ${type === 'error' ? '❌' : type === 'success' ? '✅' : 'ℹ️'}
            </div>
            <div style="flex: 1;">
                <h4 style="margin: 0 0 0.5rem 0; color: #333; font-size: 1.1rem;">${title}</h4>
                <p style="margin: 0; color: #666; font-size: 0.95rem;">${message}</p>
            </div>
            <button onclick="this.parentElement.parentElement.remove()" style="
                background: none;
                border: none;
                font-size: 1.5rem;
                color: #999;
                cursor: pointer;
                padding: 0;
                width: 2rem;
                height: 2rem;
                display: flex;
                align-items: center;
                justify-content: center;
                border-radius: 50%;
                transition: background 0.3s ease;
            " onmouseover="this.style.background='rgba(0,0,0,0.1)'" onmouseout="this.style.background='none'">
                &times;
            </button>
        </div>
    `;
    
    // Добавляем стили для анимации
    const style = document.createElement('style');
    style.textContent = `
        @keyframes slideInRight {
            from { 
                opacity: 0;
                transform: translateX(100%);
            }
            to { 
                opacity: 1;
                transform: translateX(0);
            }
        }
    `;
    document.head.appendChild(style);
    
    document.body.appendChild(notification);
    
    // Автоматически убираем через 5 секунд
    setTimeout(() => {
        if (notification.parentNode) {
            notification.style.animation = 'slideOutRight 0.3s ease-out';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
                if (style.parentNode) {
                    style.parentNode.removeChild(style);
                }
            }, 300);
        }
    }, 5000);
    
    // Добавляем стили для исчезновения
    const closeStyle = document.createElement('style');
    closeStyle.textContent = `
        @keyframes slideOutRight {
            from { 
                opacity: 1;
                transform: translateX(0);
            }
            to { 
                opacity: 0;
                transform: translateX(100%);
            }
        }
    `;
    document.head.appendChild(closeStyle);
}

// Инициализация уведомлений при загрузке страницы
setTimeout(() => {
    // Проверяем наличие сообщений в шаблонах
    const successElements = document.querySelectorAll('[th\\:if*="successMessage"]');
    const errorElements = document.querySelectorAll('[th\\:if*="errorMessage"]');
    
    successElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'success');
        }
    });
    
    errorElements.forEach(element => {
        if (element.textContent && element.textContent.trim()) {
            showStatusNotification(element.textContent.trim(), 'error');
        }
    });
}, 500);
