.PHONY: build run run-dev test test-unit test-integration clean \
       docker-up docker-down docker-rebuild infra logs help

# ---------- Build & Run ----------

build:                          ## Compile and package (skip tests)
	./gradlew build -x test

run:                            ## Start app + Postgres + Redis via Docker Compose
	docker compose up --build

run-dev:                        ## Start Postgres + Redis, then run app locally (dev profile)
	docker compose up postgres redis -d
	./gradlew bootRun --args='--spring.profiles.active=dev'

# ---------- Testing ----------

test:                           ## Run all tests (requires Docker)
	./gradlew test

test-unit:                      ## Run unit tests only (no Docker needed)
	./gradlew test --tests "com.movie.recommendation.security.*" \
	               --tests "com.movie.recommendation.modules.auth.AuthServiceTest"

test-integration:               ## Run integration tests only (requires Docker)
	./gradlew test --tests "com.movie.recommendation.integration.*" \
	               --tests "com.movie.recommendation.RecommendationApplicationTests"

# ---------- Docker ----------

docker-up:                      ## Start all containers in background
	docker compose up -d --build

docker-down:                    ## Stop and remove containers
	docker compose down

docker-rebuild:                 ## Rebuild from scratch (remove volumes)
	docker compose down -v
	docker compose up -d --build

infra:                          ## Start only Postgres + Redis (for local dev)
	docker compose up postgres redis -d

logs:                           ## Tail backend container logs
	docker compose logs -f backend

# ---------- Misc ----------

clean:                          ## Remove build artifacts
	./gradlew clean

help:                           ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2}'
