# 🚀 JWT Authentication Setup - Bank Cards Management System

## ✅ Что было реализовано

### 1. **JWT Аутентификация**
- ✅ JwtUtils - утилиты для работы с JWT токенами
- ✅ JwtAuthenticationFilter - фильтр для обработки JWT в запросах
- ✅ AuthController - API контроллер для аутентификации
- ✅ SecurityConfig - конфигурация безопасности Spring Security

### 2. **Модели и сервисы**
- ✅ User entity с ролями (USER, ADMIN)
- ✅ UserRepository для работы с базой данных
- ✅ UserService для бизнес-логики
- ✅ CustomUserDetailsService для Spring Security

### 3. **DTO классы**
- ✅ AuthResponse - ответ с JWT токеном
- ✅ LoginRequest - запрос на вход
- ✅ RegisterRequest - запрос на регистрацию

### 4. **Конфигурация**
- ✅ Swagger/OpenAPI документация
- ✅ Настройки базы данных (PostgreSQL/MySQL)
- ✅ JWT конфигурация
- ✅ Тестовая конфигурация с H2

### 5. **Тесты**
- ✅ Юнит-тесты для JwtUtils
- ✅ Интеграционные тесты для AuthController
- ✅ Тестовая конфигурация

## 🚀 Запуск проекта

### 1. **Настройка базы данных**

#### MySQL (как в вашем проекте):
```sql
CREATE DATABASE bankcards;
-- Используем существующего пользователя root с паролем 12345678
-- Или создайте нового пользователя:
-- CREATE USER 'bankcards'@'localhost' IDENTIFIED BY '12345678';
-- GRANT ALL PRIVILEGES ON bankcards.* TO 'bankcards'@'localhost';
```

### 2. **Запуск приложения**
```bash
cd C:\Users\Администратор\IdeaProjects\bank_rest-main
mvn spring-boot:run
```

### 3. **Проверка работы**
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs

## 🧪 Тестирование JWT API

### **1. Регистрация пользователя**
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User",
    "dateOfBirth": "1990-01-01",
    "country": "Russia",
    "gender": "Male"
  }'
```

### **2. Вход в систему**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

### **3. Проверка токена**
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
| GET | `/v3/api-docs` | OpenAPI документация | ❌ |

**Приложение работает на порту 8081** (как в вашем оригинальном проекте)

## 🔧 Конфигурация

### **JWT настройки** (application.yml):
```yaml
jwt:
  secret: mySecretKey123456789012345678901234567890
  expiration: 86400000 # 24 hours
```

### **База данных** (application.yml):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bankcards
    username: root
    password: 12345678
```

## 🧪 Запуск тестов

### **Все тесты:**
```bash
mvn test
```

### **Только JWT тесты:**
```bash
mvn test -Dtest=*Jwt*Test
```

### **Интеграционные тесты:**
```bash
mvn test -Dtest=*ControllerTest
```

## 📁 Структура проекта

```
src/main/java/com/example/bankcards/
├── config/
│   ├── SecurityConfig.java          # Конфигурация безопасности
│   └── SwaggerConfig.java           # Конфигурация Swagger
├── controller/
│   └── AuthController.java          # API контроллер аутентификации
├── dto/
│   ├── AuthResponse.java            # Ответ аутентификации
│   ├── LoginRequest.java            # Запрос входа
│   └── RegisterRequest.java         # Запрос регистрации
├── entity/
│   └── User.java                    # Сущность пользователя
├── repository/
│   └── UserRepository.java          # Репозиторий пользователей
├── security/
│   ├── CustomUserDetailsService.java # Сервис для Spring Security
│   └── JwtAuthenticationFilter.java  # JWT фильтр
├── service/
│   └── UserService.java             # Сервис пользователей
├── util/
│   └── JwtUtils.java                # JWT утилиты
└── BankCardsApplication.java        # Главный класс приложения
```

## 🎯 Следующие шаги

Теперь у вас есть полностью рабочая JWT аутентификация! Следующие шаги для завершения системы управления банковскими картами:

1. **Создать модель BankCard** - сущность банковской карты
2. **Создать модель Transaction** - сущность транзакции
3. **Реализовать CRUD операции** для карт
4. **Добавить переводы между картами**
5. **Создать Liquibase миграции**
6. **Добавить Docker Compose**
7. **Реализовать маскирование номеров карт**
8. **Добавить шифрование данных карт**

## 🔒 Безопасность

- ✅ JWT токены с настраиваемым временем жизни
- ✅ BCrypt хеширование паролей
- ✅ Ролевая авторизация (USER, ADMIN)
- ✅ Валидация входных данных
- ✅ CORS настройки
- ✅ Stateless аутентификация

## 📝 Логи

Приложение логирует:
- JWT операции (генерация, валидация)
- Аутентификацию пользователей
- Ошибки безопасности
- SQL запросы (в debug режиме)

**Готово к использованию! 🚀**
