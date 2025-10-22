// Authentication JavaScript functions

// Form validation
function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return false;
    
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;
    
    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.style.borderColor = '#dc3545';
            isValid = false;
        } else {
            input.style.borderColor = '#ddd';
        }
    });
    
    return isValid;
}

// Email validation
function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Password strength validation
function validatePassword(password) {
    return password.length >= 6;
}

// Show error message
function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'error';
    errorDiv.textContent = message;
    
    const form = document.querySelector('form');
    if (form) {
        form.insertBefore(errorDiv, form.firstChild);
        
        // Remove error after 5 seconds
        setTimeout(() => {
            errorDiv.remove();
        }, 5000);
    }
}

// Show success message
function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'success';
    successDiv.textContent = message;
    
    const form = document.querySelector('form');
    if (form) {
        form.insertBefore(successDiv, form.firstChild);
        
        // Remove success after 3 seconds
        setTimeout(() => {
            successDiv.remove();
        }, 3000);
    }
}

// Form submission handler
function handleFormSubmit(event, formType) {
    event.preventDefault();
    
    if (!validateForm(event.target.id)) {
        showError('Пожалуйста, заполните все обязательные поля');
        return;
    }
    
    // Additional validation based on form type
    if (formType === 'register') {
        const email = event.target.querySelector('input[name="email"]').value;
        const password = event.target.querySelector('input[name="password"]').value;
        
        if (!validateEmail(email)) {
            showError('Пожалуйста, введите корректный email');
            return;
        }
        
        if (!validatePassword(password)) {
            showError('Пароль должен содержать минимум 6 символов');
            return;
        }
    }
    // Для логина не проверяем длину пароля
    
    // Submit form
    event.target.submit();
}

// Initialize form handlers
document.addEventListener('DOMContentLoaded', function() {
    // Login form
    const loginForm = document.querySelector('form[action="/login"]');
    if (loginForm) {
        loginForm.id = 'loginForm';
        loginForm.addEventListener('submit', (e) => handleFormSubmit(e, 'login'));
    }
    
    // Register form
    const registerForm = document.querySelector('form[action="/register"]');
    if (registerForm) {
        registerForm.id = 'registerForm';
        registerForm.addEventListener('submit', (e) => handleFormSubmit(e, 'register'));
    }
    
    // Real-time validation
    const emailInputs = document.querySelectorAll('input[type="email"]');
    emailInputs.forEach(input => {
        input.addEventListener('blur', function() {
            if (this.value && !validateEmail(this.value)) {
                this.style.borderColor = '#dc3545';
                showError('Пожалуйста, введите корректный email');
            } else {
                this.style.borderColor = '#ddd';
            }
        });
    });
    
    // Проверка длины пароля только для формы регистрации
    const registerPasswordInput = document.querySelector('#registerForm input[type="password"]');
    if (registerPasswordInput) {
        registerPasswordInput.addEventListener('blur', function() {
            if (this.value && !validatePassword(this.value)) {
                this.style.borderColor = '#dc3545';
                showError('Пароль должен содержать минимум 6 символов');
            } else {
                this.style.borderColor = '#ddd';
            }
        });
    }
});
