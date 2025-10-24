-- Обновление существующих уведомлений о пополнении карт
-- Извлекаем сумму из сообщения и сохраняем в поле amount

UPDATE notifications 
SET amount = CAST(
    REGEXP_REPLACE(
        REGEXP_REPLACE(message, '.*на сумму ([\d,]+) руб\..*', '\1'),
        ',', '.'
    ) AS DECIMAL(10,2)
)
WHERE type = 'CARD_TOPUP_REQUEST' 
AND message LIKE '%на сумму%руб.%'
AND amount IS NULL;
