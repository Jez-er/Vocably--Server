# Vocably Backend

Java-монoliт для Vocably: Spring Boot 3 + Java 21 + PostgreSQL.

## Стек

- Java 21, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL + Flyway (міграції)
- Redis (кеш / сесії / лідерборди)
- JWT (jjwt) для авторизації
- springdoc-openapi (Swagger UI на `/docs`)
- Testcontainers для інтеграційних тестів

## Структура

```
com.vocably
├── user/          # реєстрація, профіль, автентифікація
├── vocabulary/     # слова, колекції слів
├── learning/        # міні-ігри, прогрес навчання
├── garden/          # метафора росту саду
├── common/          # спільні утиліти, health-check тощо
└── config/          # Spring-конфігурація (security, CORS, etc.)
```

## Локальний запуск

1. Підняти інфраструктуру (Postgres + Redis):
   ```bash
   docker compose up -d postgres redis
   ```

2. Згенерувати gradle wrapper (один раз, якщо його немає):
   ```bash
   gradle wrapper --gradle-version 8.10
   ```

3. Запустити застосунок:
   ```bash
   ./gradlew bootRun
   ```

4. Перевірити:
   - Health check: http://localhost:8080/api/health
   - Swagger: http://localhost:8080/docs

## Повний запуск через docker-compose (app + postgres + redis)

```bash
docker compose up --build
```

## Міграції

Файли міграцій лежать у `src/main/resources/db/migration`, формат Flyway:
`V{номер}__{опис}.sql`. Перша міграція вже створює таблицю `users`.

## Формат помилок

Усі ендпоінти віддають помилки однією формою (`ApiErrorResponse`):

```json
{
  "timestamp": "2026-10-06T10:43:05.275Z",
  "status": 409,
  "error": "Conflict",
  "code": "EMAIL_ALREADY_USED",
  "message": "Email is already registered",
  "path": "/api/auth/register",
  "fieldErrors": { "email": "must be a well-formed email address" }
}
```

`code` — машинно-читабельний ідентифікатор (`ErrorCode`), по ньому клієнт розрізняє причини, бо
один статус буває в кількох випадків. `fieldErrors` присутній лише для помилок валідації.

Форму віддають усі джерела: `GlobalExceptionHandler`, `ApiErrorController` (форварди на `/error`),
`JwtAuthenticationEntryPoint` (401) і `ApiAccessDeniedHandler` (403).

## Конфігурація через оточення

| Змінна | Типово | Призначення |
| --- | --- | --- |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origins браузерного клієнта, через кому. Тільки точні значення — API віддає credentials, з якими `*` недопустимий. |
| `REFRESH_COOKIE_SAME_SITE` | `Lax` | Для деплою на різних доменах (`app.example.com` → `api.example.com`) треба `None` разом із `Secure`, інакше куку не буде надіслано. |
| `REFRESH_COOKIE_SECURE` | `true` | Атрибут `Secure`. Для HTTP-локалки — `false`. |
| `REFRESH_COOKIE_DOMAIN` | *(пусто)* | Пусто = кука на точний хост API. |
| `REFRESH_TOKEN_REVOCATION_ENABLED` | `true` | Whitelist живих refresh-токенів у Redis — те, що робить logout і відкликання реальними. `false` знімає залежність від Redis і разом із нею можливість відкликати токен до його протермінування. |
| `PASSWORD_RESET_TTL` | `PT30M` | Час життя токена скидання пароля. |
| `FRONTEND_RESET_PASSWORD_URL` | `http://localhost:3000/reset-password` | База посилання у листі скидання. |

## Заглушки

- **Google OAuth2** (`/api/auth/oauth2/{provider}`) — віддає 501. Схема вже готова: `password_hash`
  nullable, є `provider`/`provider_id` (міграція V4). Бракує залежності
  `spring-boot-starter-oauth2-client`, блоку `spring.security.oauth2.client.*` і
  `AuthenticationSuccessHandler`, який видає наші JWT і редіректить на фронт.
- **Відправка листів** — `LoggingPasswordResetNotifier` пише посилання в лог замість листа. Сам
  флоу скидання робочий (таблиця з TTL, одноразовість, відкликання сесій). Логувати робочий
  креденшл у деплої не можна — замінити цей бін на справжній мейлер.

## Наступні кроки

- Підключити справжній OAuth2 і мейлер замість заглушок вище
- Реалізувати фічі learning / garden за тим самим паттерном (entity + repository + service + controller)
- Налаштувати CI (GitHub Actions): build + test на push
