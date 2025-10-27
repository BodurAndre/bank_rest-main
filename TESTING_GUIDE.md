# Инструкция по запуску тестов

## Быстрый старт

### Запуск всех тестов
```bash
mvn test
```

### Запуск тестов по категориям

#### Unit тесты
```bash
# Все unit тесты сервисов
mvn test -Dtest="*ServiceTest"

# Конкретный сервис
mvn test -Dtest="BankCardServiceTest"
mvn test -Dtest="TransferServiceTest"
```

#### Интеграционные тесты
```bash
mvn test -Dtest="*IntegrationTest"
```

#### Тесты безопасности
```bash
mvn test -Dtest="*SecurityTest"
```

#### Тесты производительности
```bash
mvn test -Dtest="PerformanceTest"
```

#### Тесты утилит
```bash
mvn test -Dtest="*UtilsTest"
```

### Запуск с профилем
```bash
mvn test -Dspring.profiles.active=test
```

## Структура тестов

```
src/test/java/com/example/bankcards/
├── config/
│   └── SwaggerConfigTest.java          # Тесты конфигурации Swagger
├── integration/
│   ├── UserFlowIntegrationTest.java    # Базовые интеграционные тесты
│   └── CompleteFlowIntegrationTest.java # Комплексные интеграционные тесты
├── security/
│   ├── SecurityTest.java              # Базовые тесты безопасности
│   └── AdvancedSecurityTest.java      # Расширенные тесты безопасности
├── service/
│   ├── BankCardServiceTest.java       # Unit тесты BankCardService
│   ├── TransferServiceTest.java       # Unit тесты TransferService
│   ├── EdgeCaseAndErrorHandlingTest.java # Тесты граничных случаев
│   └── PerformanceTest.java           # Тесты производительности
└── util/
    ├── ValidationUtilsTest.java       # Тесты валидации
    └── CardEncryptionUtilTest.java    # Тесты шифрования
```

## Требования

- Java 17+
- Maven 3.6+
- Spring Boot 3.2+
- H2 Database (для тестов)

## Результаты тестирования

После запуска тестов вы увидите:
- Количество выполненных тестов
- Количество успешных тестов
- Количество неудачных тестов
- Время выполнения
- Покрытие кода (если настроено)

## Отладка тестов

### Запуск с подробным выводом
```bash
mvn test -X
```

### Запуск конкретного теста
```bash
mvn test -Dtest="BankCardServiceTest#createCard_Success"
```

### Запуск с профилем отладки
```bash
mvn test -Dspring.profiles.active=test -Dlogging.level.com.example.bankcards=DEBUG
```

## Покрытие кода

### Генерация отчета о покрытии
```bash
mvn test jacoco:report
```

### Просмотр отчета
Откройте файл: `target/site/jacoco/index.html`

## Устранение неполадок

### Ошибки компиляции
1. Проверьте версию Java (должна быть 17+)
2. Проверьте зависимости Maven
3. Очистите проект: `mvn clean`

### Ошибки базы данных
1. Убедитесь, что H2 работает в памяти
2. Проверьте конфигурацию в `application-test.yml`

### Ошибки безопасности
1. Проверьте конфигурацию Spring Security
2. Убедитесь, что тестовые профили настроены правильно

## Рекомендации

1. **Регулярно запускайте тесты** при разработке
2. **Добавляйте новые тесты** для новой функциональности
3. **Обновляйте тесты** при изменении API
4. **Используйте тесты** для документирования поведения системы
5. **Мониторьте покрытие кода** и стремитесь к высокому проценту
