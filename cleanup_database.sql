-- Скрипт для очистки базы данных от неудачных миграций Liquibase
-- Выполните этот скрипт в MySQL перед перезапуском приложения

-- Удаляем таблицу уведомлений, если она была создана частично
DROP TABLE IF EXISTS notifications;

-- Очищаем таблицы Liquibase для повторного выполнения миграций
DELETE FROM DATABASECHANGELOG WHERE ID IN ('6', '8', '9', '10', '11', '12', '13');
DELETE FROM DATABASECHANGELOGLOCK;

-- Удаляем пользователей по умолчанию для правильного пересоздания
DELETE FROM users WHERE email IN ('admin@git.com', 'user@git.com');

-- Показываем текущее состояние миграций
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED;

-- Показываем пользователей в базе
SELECT email, role, first_name, last_name FROM users;
