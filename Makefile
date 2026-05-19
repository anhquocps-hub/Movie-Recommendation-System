.PHONY: build run run-dev test test-unit test-integration coverage coverage-verify \
       load-test security-scan clean docker-up docker-down docker-rebuild infra logs help

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
	               --tests "com.movie.recommendation.config.*" \
	               --tests "com.movie.recommendation.modules.auth.*" \
	               --tests "com.movie.recommendation.modules.movie.*" \
	               --tests "com.movie.recommendation.modules.review.*" \
	               --tests "com.movie.recommendation.modules.user.*" \
	               --tests "com.movie.recommendation.modules.watchlist.*" \
	               --tests "com.movie.recommendation.modules.notification.*" \
	               --tests "com.movie.recommendation.modules.recommendation.*"

test-integration:               ## Run integration tests only (requires Docker)
	./gradlew test --tests "com.movie.recommendation.integration.*" \
	               --tests "com.movie.recommendation.RecommendationApplicationTests"

coverage:                       ## Run tests and generate JaCoCo coverage report
	./gradlew test jacocoTestReport
	@echo "Report: build/reports/jacoco/test/html/index.html"

coverage-verify:                ## Verify coverage meets thresholds (80% line, 70% branch)
	./gradlew test jacocoTestCoverageVerification

load-test:                      ## Run Gatling load tests (requires running app)
	./gradlew gatlingRun
	@echo "Report: build/reports/gatling/"

security-scan:                  ## Run OWASP dependency vulnerability check
	./gradlew dependencyCheckAnalyze
	@echo "Report: build/reports/dependency-check-report.html"

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
