# MentorX Backend Makefile
# Convenient commands for development and deployment

.PHONY: help build test run clean docker-build docker-run docker-stop setup-dev

# Default target
help:
	@echo "Available commands:"
	@echo "  build        - Build the application"
	@echo "  test         - Run tests"
	@echo "  run          - Run the application locally"
	@echo "  clean        - Clean build artifacts"
	@echo "  docker-build - Build Docker image"
	@echo "  docker-run   - Run with Docker Compose"
	@echo "  docker-stop  - Stop Docker containers"
	@echo "  setup-dev    - Setup development environment"
	@echo "  lint         - Run code quality checks"
	@echo "  security     - Run security scan"

# Build the application
build:
	@echo "Building MentorX Backend..."
	./mvnw clean compile

# Run tests
test:
	@echo "Running tests..."
	./mvnw clean test

# Run tests with coverage
test-coverage:
	@echo "Running tests with coverage..."
	./mvnw clean test jacoco:report

# Package the application
package:
	@echo "Packaging application..."
	./mvnw clean package -DskipTests

# Run the application locally
run:
	@echo "Starting MentorX Backend..."
	./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean
	docker system prune -f

# Build Docker image
docker-build:
	@echo "Building Docker image..."
	docker build -t mentorx-backend:latest .

# Run with Docker Compose
docker-run:
	@echo "Starting services with Docker Compose..."
	docker-compose up -d

# Run with Docker Compose including tools (pgAdmin)
docker-run-tools:
	@echo "Starting services with tools..."
	docker-compose --profile tools up -d

# Stop Docker containers
docker-stop:
	@echo "Stopping Docker containers..."
	docker-compose down

# Stop and remove volumes
docker-clean:
	@echo "Stopping containers and removing volumes..."
	docker-compose down -v
	docker system prune -f

# Setup development environment
setup-dev:
	@echo "Setting up development environment..."
	@if [ ! -f .env ]; then cp .env.example .env; echo "Created .env file from template"; fi
	@echo "Please update .env file with your configuration"
	./mvnw dependency:resolve
	@echo "Development environment setup complete!"

# Run code quality checks
lint:
	@echo "Running code quality checks..."
	./mvnw checkstyle:check
	./mvnw pmd:check
	./mvnw spotbugs:check

# Run security scan
security:
	@echo "Running security scan..."
	./mvnw org.owasp:dependency-check-maven:check

# Generate API documentation
docs:
	@echo "Generating API documentation..."
	./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=docs" &
	@sleep 10
	@echo "API docs available at: http://localhost:8080/swagger-ui.html"

# Database migration (if using Flyway)
db-migrate:
	@echo "Running database migrations..."
	./mvnw flyway:migrate

# Database rollback (if using Flyway)
db-rollback:
	@echo "Rolling back database..."
	./mvnw flyway:undo

# View logs
logs:
	@echo "Viewing application logs..."
	docker-compose logs -f mentorx-app

# View database logs
logs-db:
	@echo "Viewing database logs..."
	docker-compose logs -f postgres

# Backup database
backup-db:
	@echo "Creating database backup..."
	docker-compose exec postgres pg_dump -U mentorx_user mentorx_db > backup_$(shell date +%Y%m%d_%H%M%S).sql

# Restore database
restore-db:
	@echo "Restoring database from backup..."
	@read -p "Enter backup file path: " backup_file; \
	docker-compose exec -T postgres psql -U mentorx_user -d mentorx_db < $$backup_file

# Install pre-commit hooks
install-hooks:
	@echo "Installing pre-commit hooks..."
	@echo "#!/bin/sh" > .git/hooks/pre-commit
	@echo "make test" >> .git/hooks/pre-commit
	@chmod +x .git/hooks/pre-commit
	@echo "Pre-commit hooks installed!"

# Performance test
perf-test:
	@echo "Running performance tests..."
	# Add your performance testing commands here
	# Example: ab -n 1000 -c 10 http://localhost:8080/api/v1/health

# Deploy to staging
deploy-staging:
	@echo "Deploying to staging..."
	# Add your staging deployment commands here

# Deploy to production
deploy-prod:
	@echo "Deploying to production..."
	# Add your production deployment commands here
	@read -p "Are you sure you want to deploy to production? (y/N): " confirm; \
	if [ "$$confirm" = "y" ] || [ "$$confirm" = "Y" ]; then \
		echo "Deploying to production..."; \
	else \
		echo "Deployment cancelled."; \
	fi