# Vocably Backend

Java-моноліт для Vocably: Spring Boot 3.5 + Java 25 + PostgreSQL.

У коді коментарів немає — усі пояснення й причини живуть у [`docs/`](docs/):

| Документ | Про що |
| --- | --- |
| [docs/architecture.md](docs/architecture.md) | owner-scoping, автентифікація, скидання пароля, формат помилок, health |
| [docs/database.md](docs/database.md) | міграції, констрейнти, індекси, вибір типів |
| [docs/configuration.md](docs/configuration.md) | профілі, змінні оточення, docker-compose, CI |
| [docs/testing.md](docs/testing.md) | що покрито і чому саме так |
| [docs/decisions.md](docs/decisions.md) | неочевидні рішення щодо залежностей і збірки |

## Стек

- Java 25, Spring Boot 3.5.6
- Spring Web, Spring Data JPA, Spring Security, Spring Boot Actuator
- PostgreSQL + Flyway (міграції)
- Redis (whitelist живих refresh-токенів)
- JWT (jjwt) для авторизації
- springdoc-openapi (Swagger UI на `/docs`)
- Testcontainers для інтеграційних тестів
- Spotless тримає форматування за `.editorconfig`

## Структура

```
com.vocably
├── auth/            # реєстрація, логін, JWT, refresh, скидання пароля
├── user/            # сутність користувача, роль, профіль
├── dictionary/      # словник на мову, по одному на користувача
├── word/            # слова всередині словника
├── language/        # довідник мов (спільні дані, запис — лише ADMIN)
├── common/          # health-check, формат помилок
└── config/          # Spring-конфігурація (security, CORS, OpenAPI, Redis)
```

## Локальний запуск

```bash
make db     # Postgres + Redis
make run    # застосунок із профілем dev
```

`make run` активує профіль `dev`: звідти беруться DEBUG-логування, SQL-трейс і локальний
JWT-секрет, тож нічого експортувати не треба. Базовий профіль цього навмисно не має —
див. [configuration.md](docs/configuration.md).

Перевірити:

- Health check: http://localhost:8080/api/health
- Swagger: http://localhost:8080/docs

## Повний запуск у Docker

```bash
echo "JWT_SECRET=$(openssl rand -base64 48)" >> .env
docker compose up --build
```

> **Зараз не збереться.** Toolchain вимагає Java 25, а `Dockerfile` бере temurin 21 на обох
> стадіях. Діагноз і варіанти лагодження — в [decisions.md](docs/decisions.md).

## Команди

```bash
make help     # усі таргети
make test     # unit + інтеграційні (треба Docker)
make check    # тести + перевірка форматування
make format   # привести форматування до .editorconfig
```

## API

| Метод | Шлях | Доступ |
| --- | --- | --- |
| `POST` | `/api/auth/register` | відкритий |
| `POST` | `/api/auth/login` | відкритий |
| `POST` | `/api/auth/refresh` | refresh-кука |
| `POST` | `/api/auth/logout` | відкритий |
| `POST` | `/api/auth/forgot-password` | відкритий |
| `POST` | `/api/auth/reset-password` | reset-токен |
| `GET` | `/api/auth/me` | access-токен |
| `GET` | `/api/auth/oauth2/{provider}`, `/callback/{provider}` | заглушка, 501 |
| `GET`/`POST` | `/api/dictionaries` | access-токен |
| `GET` | `/api/dictionaries/{id}`, `/language/{code}` | access-токен |
| `GET`/`POST` | `/api/words` | access-токен (пагіновано) |
| `GET` | `/api/words/{id}`, `/search/{word}`, `/dictionary/{id}` | access-токен |
| `GET` | `/api/languages` (і `/all`), `/code/{code}`, `/id/{id}` | access-токен |
| `POST` | `/api/languages` | **ADMIN** |
| `GET` | `/api/health`, `/actuator/health` | відкритий |

Повна схема — на `/docs`.

### Пагінація

Колекційні ендпоінти слів приймають стандартні параметри Spring Data (`page`, `size`,
`sort`) і віддають `PagedModel`:

```json
{
  "content": [ /* WordResponse */ ],
  "page": { "size": 20, "number": 0, "totalElements": 42, "totalPages": 3 }
}
```

### Формат помилок

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

`code` — машинно-читабельний ідентифікатор (`ErrorCode`): один статус буває в кількох
випадків, і клієнт розрізняє причини саме по ньому. `fieldErrors` присутній лише для
помилок валідації.

## Роль користувача

Реєстрація завжди створює `role = 'USER'`. `ADMIN` потрібен лише для `POST /api/languages`.
Навмисно не засіяно жодного адміна; видається вручну:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

## Заглушки

- **Google OAuth2** (`/api/auth/oauth2/{provider}`) — віддає 501. Схема вже готова:
  `password_hash` nullable, є `provider`/`provider_id` (міграція V4). Бракує залежності
  `spring-boot-starter-oauth2-client`, блоку `spring.security.oauth2.client.*` і
  `AuthenticationSuccessHandler`, який видає наші JWT і редіректить на фронт.
- **Відправка листів** — `LoggingPasswordResetNotifier` пише посилання в лог замість листа.
  Сам флоу скидання робочий (таблиця з TTL, одноразовість, відкликання сесій). Логувати
  робочий креденшл у деплої не можна — замінити цей бін на справжній мейлер.

## Наступні кроки

- Полагодити Java-версію в `Dockerfile`
- Підключити справжній OAuth2 і мейлер замість заглушок вище
- Реалізувати фічі learning / garden за тим самим паттерном
  (entity + repository + service + controller)
