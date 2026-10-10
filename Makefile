.PHONY: help build run test check format clean db db-stop up down logs restart

help: ## Показати цю довідку
	@grep -E '^[a-zA-Z_-]+:.*##' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*## "}; {printf "  \033[36m%-12s\033[0m %s\n", $$1, $$2}'

build: ## Зібрати проект
	./gradlew build -x test

run: ## Запустити локально з dev-профілем (потрібні БД і Redis)
	SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

test: ## Запустити тести (інтеграційні піднімають Postgres через Testcontainers — треба Docker)
	./gradlew test

check: ## Тести + перевірка форматування
	./gradlew build

format: ## Привести форматування до .editorconfig
	./gradlew spotlessApply

clean: ## Очистити артефакти збірки
	./gradlew clean

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
