# Конфігурація

## Профілі

| Профіль | Коли | Що дає |
| --- | --- | --- |
| *(базовий)* | деплой | `INFO`-логування, `Secure` на куці, `JWT_SECRET` обов'язковий |
| `dev` | локальна розробка (`make run`) | DEBUG-логи, SQL-трейс, локальний JWT-секрет, `Secure=false` |
| `test` | інтеграційні тести | Redis вимкнений, тестовий секрет, datasource від Testcontainers |

`application-dev.yml` не перевизначає `spring.config.activate.on-profile` — профіль
визначається іменем файлу, і дублювання ключа Spring Boot 3 відкидає як невалідне в
profile-specific ресурсі.

Усе, що в `dev`, свідомо не є дефолтом деплою: логування стейтментів друкує кожен запит, що
в продакшн-лозі і шум, і спосіб для даних користувача потрапити в агрегатор логів.

## Змінні оточення

| Змінна | Типово | Призначення |
| --- | --- | --- |
| `JWT_SECRET` | **без дефолту** | Base64 ≥ 32 байт (`openssl rand -base64 48`) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Origins браузерного клієнта, через кому |
| `REFRESH_COOKIE_SAME_SITE` | `Lax` | `None` для деплою на різних доменах |
| `REFRESH_COOKIE_SECURE` | `true` | Атрибут `Secure` |
| `REFRESH_COOKIE_DOMAIN` | *(пусто)* | Пусто = кука на точний хост API |
| `REFRESH_TOKEN_REVOCATION_ENABLED` | `true` | Whitelist живих refresh-токенів у Redis |
| `PASSWORD_RESET_TTL` | `PT30M` | Час життя токена скидання пароля |
| `PASSWORD_RESET_SWEEP_CRON` | `0 15 3 * * *` | Коли прибирати мертві reset-токени |
| `FRONTEND_RESET_PASSWORD_URL` | `http://localhost:3000/reset-password` | База посилання у листі |
| `SPRING_PROFILES_ACTIVE` | *(пусто)* | `dev` для локальної розробки |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | `localhost` / `5432` / `vocably` / `vocably` / `vocably` | Postgres |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` | Redis |
| `SERVER_PORT` | `8080` | Порт застосунку |

## Чому `JWT_SECRET` без дефолту

Fallback тут — це секрет, закомічений у репозиторій, і деплой, який забув виставити змінну,
тихо підписував би токени ключем, що його будь-хто може підробити. Тому змінна обов'язкова:
застосунок відмовляється стартувати без неї.

`JwtService` додатково декодує й перевіряє ключ на старті, а не на першому запиті. Відсутній,
не-base64 або закороткий секрет — це помилка деплою, і кожну з них дешевше знайти як
відмову старту, ніж як токени, які неможливо верифікувати — або, гірше, токени, підписані
ключем, достатньо слабким для перебору. HS256 потребує ключа не коротшого за свій вихід:
коротший послаблює кожен токен.

Профіль `dev` має власний локальний секрет — публічний за визначенням, бо лежить у
репозиторії, і саме тому базовий профіль дефолту не має взагалі.

## CORS

`allowed-origins` — список точних значень, не wildcard: API віддає credentials
(refresh-куку), а специфікація забороняє поєднувати їх з origin `*`.

## Пагінація

`default-page-size: 20`, `max-page-size: 100`. Сенс пагінації цих ендпоінтів у тому, що
жоден окремий запит не може витягнути необмежений набір — тому є і верхня межа, не лише
дефолт.

## docker-compose

`app` працює на базовому профілі, тому `JWT_SECRET` обов'язковий:

```bash
echo "JWT_SECRET=$(openssl rand -base64 48)" >> .env
docker compose up --build
```

`app` чекає на healthy і Postgres, і Redis. Redis не опційний:
`revocation-enabled` типово `true`, тож whitelist refresh-токенів живе в Redis, і без нього
застосунок не обслужить `/auth/refresh`.

Ключ `version` прибраний — у Compose V2 він застарілий і дає лише warning.

## CI

`.github/workflows/ci.yml` — build + test на push у `main` і на кожен PR.

- JDK мусить відповідати toolchain у `build.gradle.kts`; toolchain-резолвера немає, тож
  Gradle бере JDK, який дає раннер, а не завантажує свій.
- `build` запускає `spotlessCheck` і тести, тож і дрейф форматування, і падіння тесту
  ловляться одним кроком.
- Інтеграційні тести стартують Postgres через Testcontainers — на `ubuntu-latest` це
  працює, бо Docker там уже є.
- Новий push у ту саму гілку скасовує попередній незавершений прогін (`concurrency`).
