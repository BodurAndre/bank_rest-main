# 🏦 Bank Cards Management System

Система управления банковскими картами с JWT аутентификацией.

## 🚀 Быстрый старт

### 1. Настройка базы данных
```sql
-- MySQL (как в вашем проекте)
CREATE DATABASE bankcards;
-- Используем существующего пользователя root с паролем 12345678
```

### 2. Запуск приложения
```bash
mvn spring-boot:run
```

### 3. Проверка работы
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs

## 🧪 Тестирование API

### Регистрация пользователя
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Вход в систему
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### Проверка токена
```bash
curl -X POST http://localhost:8081/api/auth/validate \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 📊 API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Регистрация пользователя | ❌ |
| POST | `/api/auth/login` | Вход в систему | ❌ |
| POST | `/api/auth/validate` | Проверка токена | ❌ |
| GET | `/swagger-ui.html` | Swagger UI | ❌ |

## 🧪 Запуск тестов
```bash
mvn test
```

## 🔧 Технологии

- **Java 17+**
- **Spring Boot 3.2.0**
- **Spring Security + JWT**
- **Spring Data JPA**
- **PostgreSQL/MySQL**
- **Swagger/OpenAPI**
- **Liquibase**
- **Docker**

## 📁 Структура проекта

```
src/main/java/com/example/bankcards/
├── config/          # Конфигурации
├── controller/      # REST контроллеры
├── dto/            # Data Transfer Objects
├── entity/         # JPA сущности
├── repository/     # Репозитории
├── security/       # Безопасность
├── service/        # Бизнес-логика
└── util/           # Утилиты
```

## 🔒 Безопасность

- ✅ JWT токены
- ✅ BCrypt хеширование паролей
- ✅ Ролевая авторизация (USER, ADMIN)
- ✅ Валидация данных
- ✅ CORS настройки

## 📝 Лицензия

MIT License

---

**Подробная документация**: [JWT_SETUP_README.md](JWT_SETUP_README.md)