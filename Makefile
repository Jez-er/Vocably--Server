# ─── Vocably Server ──────────────────────────────────────

.PHONY: help build run test clean db db-stop logs restart

help: ## Показати цю довідку
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

# ─── Gradle ──────────────────────────────────────────────

build: ## Зібрати проект
	./gradlew build -x test

run: ## Запустити локально (потрібні БД і Redis)
	./gradlew bootRun

test: ## Запустити тести
	./gradlew test

clean: ## Очистити артефакти збірки
	./gradlew clean

# ─── Docker ──────────────────────────────────────────────

db: ## Запустити PostgreSQL і Redis
	docker compose up -d postgres redis

db-stop: ## Зупинити PostgreSQL і Redis
	docker compose down

up: ## Запустити все (БД + Redis + App) у Docker
	docker compose up -d --build

down: ## Зупинити все і видалити контейнери
	docker compose down

logs: ## Показати логи застосунку
	docker compose logs -f app

restart: ## Перезібрати і перезапустити app-контейнер
	docker compose up -d --build app
