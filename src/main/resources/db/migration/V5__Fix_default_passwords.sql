-- Исправление паролей пользователей по умолчанию
-- Обновляем пароли на правильные BCrypt хеши

-- Обновляем пароль админа на 'admin1'
UPDATE users 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi'
WHERE email = 'admin@git.com';

-- Обновляем пароль пользователя на 'user12'  
UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE email = 'user@git.com';
