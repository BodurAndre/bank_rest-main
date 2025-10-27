-- Добавление поля block_request_sent в таблицу bank_cards
ALTER TABLE bank_cards ADD COLUMN block_request_sent BOOLEAN DEFAULT FALSE;

