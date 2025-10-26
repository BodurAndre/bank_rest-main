# 🔧 Исправление ошибки Liquibase с MySQL

## ❌ Проблема
Ошибка: `You have an error in your SQL syntax... near 'BIGSERIAL PRIMARY KEY'`

**Причина:** В миграции V6 использовался PostgreSQL синтаксис (`BIGSERIAL`), а у вас MySQL база данных.

## ✅ Что было исправлено

### 1. **Обновлена миграция V6:**
```sql
-- Было (PostgreSQL):
id BIGSERIAL PRIMARY KEY,

-- Стало (MySQL):
id BIGINT AUTO_INCREMENT PRIMARY KEY,
```

### 2. **Объединены миграции:**
- ✅ **V6** - создание таблицы notifications с полем amount и увеличенным размером type
- ❌ **V8** - удалена (добавление amount уже в V6)
- ❌ **V9** - удалена (обновление amount уже не нужно)
- ❌ **V10** - удалена (увеличение type уже в V6)
- ✅ **V11** - оставлена для шифрования существующих данных

### 3. **Обновлен changelog.xml:**
```xml
<changeSet id="6" author="system">
    <sqlFile path="V6__Create_notifications_table.sql" relativeToChangelogFile="true"/>
</changeSet>
<changeSet id="7" author="system">
    <sqlFile path="V7__Add_block_request_sent_to_bank_cards.sql" relativeToChangelogFile="true"/>
</changeSet>
<changeSet id="11" author="system">
    <sqlFile path="V11__Encrypt_existing_card_numbers.sql" relativeToChangelogFile="true"/>
</changeSet>
```

## 🚀 Как исправить

### **Шаг 1: Очистка базы данных**
Выполните скрипт `cleanup_database.sql` в MySQL:

```sql
-- Удаляем таблицу уведомлений, если она была создана частично
DROP TABLE IF EXISTS notifications;

-- Очищаем таблицы Liquibase для повторного выполнения миграций
DELETE FROM DATABASECHANGELOG WHERE ID IN ('6', '8', '9', '10', '11');
DELETE FROM DATABASECHANGELOGLOCK;
```

### **Шаг 2: Перезапуск приложения**
```bash
mvn spring-boot:run
```

### **Шаг 3: Проверка миграций**
Проверьте, что миграции выполнились успешно:
```sql
SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED;
```

Должны быть записи:
- ✅ V1__Create_users_table
- ✅ V1__Create_bank_cards_table  
- ✅ V2__Create_bank_cards_table
- ✅ V2__Create_default_users
- ✅ V3__Create_transfers_table
- ✅ V4__Create_default_users
- ✅ V5__Fix_default_passwords
- ✅ V6__Create_notifications_table ← **Новая исправленная**
- ✅ V7__Add_block_request_sent_to_bank_cards
- ✅ V11__Encrypt_existing_card_numbers

## 🔍 Проверка результата

### **Таблица notifications должна содержать:**
```sql
DESCRIBE notifications;
```

Ожидаемые поля:
- `id` - BIGINT AUTO_INCREMENT PRIMARY KEY
- `user_id` - BIGINT NOT NULL
- `card_id` - BIGINT NOT NULL  
- `type` - VARCHAR(100) NOT NULL ← **Увеличено до 100**
- `title` - VARCHAR(255) NOT NULL
- `message` - TEXT NOT NULL
- `is_read` - BOOLEAN NOT NULL DEFAULT FALSE
- `is_processed` - BOOLEAN NOT NULL DEFAULT FALSE
- `created_at` - TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
- `read_at` - TIMESTAMP NULL
- `processed_at` - TIMESTAMP NULL
- `amount` - DECIMAL(10,2) NULL ← **Уже включено**

### **Проверка работы системы:**
1. **Запустите приложение:** `mvn spring-boot:run`
2. **Откройте:** http://localhost:8081
3. **Проверьте уведомления:** http://localhost:8081/notifications (для админа)
4. **Создайте карту** и проверьте, что номер зашифрован

## ✅ Готово!

Теперь все миграции должны выполниться успешно, и система будет работать с:
- 🔐 **Шифрованием номеров карт**
- 🔔 **Системой уведомлений**  
- 🏦 **Полной функциональностью банковских карт**

Если возникнут проблемы, проверьте логи приложения и выполните cleanup_database.sql еще раз.
