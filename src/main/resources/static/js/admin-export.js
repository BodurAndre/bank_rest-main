/**
 * JavaScript для экспорта данных администратором
 */

document.addEventListener('DOMContentLoaded', function() {
    // Добавляем стили для модального окна экспорта
    addExportModalStyles();
});

/**
 * Показывает модальное окно выбора типа экспорта
 */
function showExportModal(dataType) {
    const modal = document.createElement('div');
    modal.className = 'export-modal-overlay';
    modal.onclick = (e) => {
        if (e.target === modal) {
            closeExportModal();
        }
    };

    let title = '';
    let description = '';
    
    switch (dataType) {
        case 'cards':
            title = '📊 Экспорт карт';
            description = 'Выберите формат для экспорта данных о банковских картах';
            break;
        case 'users':
            title = '👥 Экспорт пользователей';
            description = 'Выберите формат для экспорта данных о пользователях';
            break;
        case 'audit':
            title = '📋 Экспорт аудит-логов';
            description = 'Выберите формат для экспорта аудит-логов';
            break;
        case 'transfers':
            title = '💸 Экспорт переводов';
            description = 'Выберите формат для экспорта данных о переводах';
            break;
        default:
            title = '📊 Экспорт данных';
            description = 'Выберите формат для экспорта';
    }

    modal.innerHTML = `
        <div class="export-modal-content">
            <div class="export-modal-header">
                <h3>${title}</h3>
                <button type="button" class="export-modal-close" onclick="closeExportModal()">&times;</button>
            </div>
            <div class="export-modal-body">
                <p>${description}</p>
                <div class="export-format-options">
                    <button type="button" class="btn btn-export-format" onclick="exportData('${dataType}', 'csv')">
                        <div class="format-icon">📊</div>
                        <div class="format-info">
                            <div class="format-name">CSV</div>
                            <div class="format-desc">Таблица для Excel/Google Sheets</div>
                        </div>
                    </button>
                    <button type="button" class="btn btn-export-format" onclick="exportData('${dataType}', 'pdf')">
                        <div class="format-icon">📄</div>
                        <div class="format-info">
                            <div class="format-name">PDF</div>
                            <div class="format-desc">Документ для печати и просмотра</div>
                        </div>
                    </button>
                </div>
                <div class="export-info">
                    <p><strong>ℹ️ Информация:</strong></p>
                    <ul>
                        <li>Экспорт учитывает текущие фильтры</li>
                        <li>В отчет включается информация о владельцах карт</li>
                        <li>Файл будет автоматически загружен</li>
                    </ul>
                </div>
            </div>
            <div class="export-modal-actions">
                <button type="button" class="btn btn-secondary" onclick="closeExportModal()">Отмена</button>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
    setTimeout(() => { modal.classList.add('show'); }, 10);
}

/**
 * Закрывает модальное окно экспорта
 */
function closeExportModal() {
    const modal = document.querySelector('.export-modal-overlay');
    if (modal) {
        modal.classList.remove('show');
        setTimeout(() => {
            document.body.removeChild(modal);
        }, 300);
    }
}

/**
 * Выполняет экспорт данных
 */
function exportData(dataType, format) {
    // Получаем текущие параметры фильтров из URL
    const urlParams = new URLSearchParams(window.location.search);
    const filters = {};
    
    // Собираем параметры в зависимости от типа данных
    switch (dataType) {
        case 'cards':
            if (urlParams.get('status')) filters.status = urlParams.get('status');
            if (urlParams.get('search')) filters.search = urlParams.get('search');
            break;
        case 'users':
            if (urlParams.get('search')) filters.search = urlParams.get('search');
            if (urlParams.get('role')) filters.role = urlParams.get('role');
            break;
        case 'audit':
            if (urlParams.get('action')) filters.action = urlParams.get('action');
            if (urlParams.get('status')) filters.status = urlParams.get('status');
            if (urlParams.get('userEmail')) filters.userEmail = urlParams.get('userEmail');
            break;
        case 'transfers':
            if (urlParams.get('userEmail')) filters.userEmail = urlParams.get('userEmail');
            if (urlParams.get('fromDate')) filters.fromDate = urlParams.get('fromDate');
            if (urlParams.get('toDate')) filters.toDate = urlParams.get('toDate');
            break;
    }
    
    // Строим URL для экспорта
    const baseUrl = `/admin/export/${dataType}/${format}`;
    const queryString = new URLSearchParams(filters).toString();
    const exportUrl = queryString ? `${baseUrl}?${queryString}` : baseUrl;
    
    // Показываем индикатор загрузки
    showExportProgress();
    
    // Создаем скрытую ссылку для загрузки файла
    const link = document.createElement('a');
    link.href = exportUrl;
    link.style.display = 'none';
    document.body.appendChild(link);
    
    // Запускаем загрузку
    link.click();
    
    // Убираем ссылку
    document.body.removeChild(link);
    
    // Закрываем модальное окно
    closeExportModal();
    
    // Показываем уведомление об успешном экспорте
    setTimeout(() => {
        hideExportProgress();
        showCustomNotification('✅ Успех', `Файл ${format.toUpperCase()} успешно экспортирован`, 'success');
    }, 1000);
}

/**
 * Показывает индикатор прогресса экспорта
 */
function showExportProgress() {
    const progress = document.createElement('div');
    progress.className = 'export-progress-overlay';
    progress.innerHTML = `
        <div class="export-progress-content">
            <div class="export-progress-spinner"></div>
            <p>Подготовка файла для экспорта...</p>
        </div>
    `;
    document.body.appendChild(progress);
}

/**
 * Скрывает индикатор прогресса экспорта
 */
function hideExportProgress() {
    const progress = document.querySelector('.export-progress-overlay');
    if (progress) {
        document.body.removeChild(progress);
    }
}

/**
 * Добавляет CSS стили для модального окна экспорта
 */
function addExportModalStyles() {
    const style = document.createElement('style');
    style.textContent = `
        /* Стили для кнопки экспорта */
        .export-section {
            margin-top: 1rem;
            text-align: center;
        }
        
        .btn-export {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 8px;
            font-weight: 500;
            transition: all 0.3s ease;
        }
        
        .btn-export:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(40, 167, 69, 0.3);
        }

        /* Модальное окно экспорта */
        .export-modal-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 1000;
            opacity: 0;
            transition: opacity 0.3s ease;
        }
        
        .export-modal-overlay.show {
            opacity: 1;
        }
        
        .export-modal-content {
            background: white;
            border-radius: 15px;
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            transform: scale(0.9);
            transition: transform 0.3s ease;
        }
        
        .export-modal-overlay.show .export-modal-content {
            transform: scale(1);
        }
        
        .export-modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1.5rem;
            border-bottom: 1px solid #dee2e6;
        }
        
        .export-modal-header h3 {
            margin: 0;
            color: #2c3e50;
        }
        
        .export-modal-close {
            background: none;
            border: none;
            font-size: 1.5rem;
            color: #6c757d;
            cursor: pointer;
            padding: 0;
            width: 30px;
            height: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: all 0.3s ease;
        }
        
        .export-modal-close:hover {
            background: #f8f9fa;
            color: #495057;
        }
        
        .export-modal-body {
            padding: 1.5rem;
        }
        
        .export-modal-body p {
            margin-bottom: 1.5rem;
            color: #6c757d;
        }
        
        .export-format-options {
            display: grid;
            gap: 1rem;
            margin-bottom: 1.5rem;
        }
        
        .btn-export-format {
            display: flex;
            align-items: center;
            padding: 1rem;
            border: 2px solid #dee2e6;
            border-radius: 10px;
            background: white;
            cursor: pointer;
            transition: all 0.3s ease;
            text-align: left;
        }
        
        .btn-export-format:hover {
            border-color: #007bff;
            background: #f8f9ff;
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0, 123, 255, 0.1);
        }
        
        .format-icon {
            font-size: 2rem;
            margin-right: 1rem;
        }
        
        .format-info {
            flex: 1;
        }
        
        .format-name {
            font-weight: 600;
            font-size: 1.1rem;
            color: #2c3e50;
            margin-bottom: 0.25rem;
        }
        
        .format-desc {
            color: #6c757d;
            font-size: 0.9rem;
        }
        
        .export-info {
            background: #f8f9fa;
            padding: 1rem;
            border-radius: 8px;
            border-left: 4px solid #17a2b8;
        }
        
        .export-info p {
            margin-bottom: 0.5rem;
            color: #495057;
        }
        
        .export-info ul {
            margin: 0;
            padding-left: 1.5rem;
        }
        
        .export-info li {
            color: #6c757d;
            font-size: 0.9rem;
            margin-bottom: 0.25rem;
        }
        
        .export-modal-actions {
            padding: 1rem 1.5rem;
            border-top: 1px solid #dee2e6;
            text-align: right;
        }
        
        /* Индикатор прогресса */
        .export-progress-overlay {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.7);
            display: flex;
            justify-content: center;
            align-items: center;
            z-index: 1001;
        }
        
        .export-progress-content {
            background: white;
            padding: 2rem;
            border-radius: 15px;
            text-align: center;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
        }
        
        .export-progress-spinner {
            width: 40px;
            height: 40px;
            border: 4px solid #f3f3f3;
            border-top: 4px solid #007bff;
            border-radius: 50%;
            animation: spin 1s linear infinite;
            margin: 0 auto 1rem;
        }
        
        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }
        
        .export-progress-content p {
            margin: 0;
            color: #495057;
            font-weight: 500;
        }
    `;
    document.head.appendChild(style);
}

/**
 * Показывает кастомное уведомление (если функция не определена в других файлах)
 */
function showCustomNotification(title, message, type) {
    // Проверяем, есть ли уже функция showCustomNotification
    if (typeof window.showCustomNotification === 'function') {
        window.showCustomNotification(title, message, type);
        return;
    }
    
    // Если нет, создаем простое уведомление
    const notification = document.createElement('div');
    notification.className = `custom-notification ${type}`;
    notification.innerHTML = `
        <div class="notification-content">
            <strong>${title}</strong>
            <p>${message}</p>
        </div>
    `;
    
    // Добавляем стили, если их нет
    if (!document.querySelector('#notification-styles')) {
        const style = document.createElement('style');
        style.id = 'notification-styles';
        style.textContent = `
            .custom-notification {
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 1rem;
                border-radius: 8px;
                color: white;
                z-index: 1002;
                max-width: 300px;
                animation: slideIn 0.3s ease;
            }
            
            .custom-notification.success {
                background: #28a745;
            }
            
            .custom-notification.error {
                background: #dc3545;
            }
            
            .custom-notification.info {
                background: #17a2b8;
            }
            
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            
            .notification-content strong {
                display: block;
                margin-bottom: 0.5rem;
            }
            
            .notification-content p {
                margin: 0;
                font-size: 0.9rem;
            }
        `;
        document.head.appendChild(style);
    }
    
    document.body.appendChild(notification);
    
    // Автоматически убираем уведомление через 5 секунд
    setTimeout(() => {
        if (notification.parentNode) {
            notification.parentNode.removeChild(notification);
        }
    }, 5000);
}

