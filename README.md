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

## Наступні кроки

- Додати Spring Security конфіг (JWT filter, endpoints)
- Реалізувати фічі vocabulary / learning / garden за тим самим паттерном (entity + repository + service + controller)
- Налаштувати CI (GitHub Actions): build + test на push
