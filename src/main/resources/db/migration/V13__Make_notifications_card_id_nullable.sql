-- Изменение поля card_id в таблице notifications на nullable
-- Это необходимо для запросов на создание карт, где карта еще не существует

ALTER TABLE notifications MODIFY COLUMN card_id BIGINT NULL;
