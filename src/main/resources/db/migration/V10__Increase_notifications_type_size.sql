-- Увеличение размера поля type в таблице notifications
ALTER TABLE notifications ALTER COLUMN type TYPE VARCHAR(100);
