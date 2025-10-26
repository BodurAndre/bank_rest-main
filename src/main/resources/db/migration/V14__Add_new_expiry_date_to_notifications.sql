-- Добавление поля new_expiry_date в таблицу notifications для запросов на пересоздание карт
ALTER TABLE notifications 
ADD COLUMN new_expiry_date VARCHAR(5) NULL COMMENT 'Новая дата истечения для пересоздания карты (MM/YY)';
