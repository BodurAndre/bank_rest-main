-- Сброс пользователей по умолчанию для правильного создания через DataInitializer
-- Удаляем существующих пользователей по умолчанию, чтобы DataInitializer создал их с правильными паролями

-- Удаляем пользователей по умолчанию
DELETE FROM users WHERE email IN ('admin@git.com', 'user@git.com');

-- Комментарий: После выполнения этой миграции DataInitializer автоматически создаст пользователей:
-- admin@git.com / admin1 (роль ADMIN)
-- user@git.com / user12 (роль USER)
