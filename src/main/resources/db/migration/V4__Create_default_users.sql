-- Создание пользователей по умолчанию
-- Создаем только если база данных пустая (нет пользователей)

-- Создание админа (только если нет пользователей в базе)
INSERT INTO users (
    username, 
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    created_at, 
    updated_at
) 
SELECT 
    'admin@git.com',
    'admin@git.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', -- admin1
    'Администратор',
    'Системы',
    'ADMIN',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM users LIMIT 1);

-- Создание обычного пользователя (только если нет пользователей в базе)
INSERT INTO users (
    username, 
    email, 
    password, 
    first_name, 
    last_name, 
    role, 
    created_at, 
    updated_at
) 
SELECT 
    'user@git.com',
    'user@git.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- user12
    'Пользователь',
    'Тестовый',
    'USER',
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM users LIMIT 1);
