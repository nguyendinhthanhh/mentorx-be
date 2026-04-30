# MentorX Backend API

A comprehensive mentoring platform backend built with Spring Boot 3.x and Java 25.

## ?? Features

- **User Management** - Registration, authentication, profile management
- **Mentor System** - Mentor applications, approvals, skill management
- **Job Management** - Job posting, proposals, contracts, milestones
- **Wallet System** - Double-entry bookkeeping, escrow payments, transactions
- **Security** - JWT authentication, OAuth2 integration, role-based access
- **API Documentation** - OpenAPI/Swagger integration

## ??? Tech Stack

- **Java 25** - Latest LTS version
- **Spring Boot 3.x** - Framework
- **Spring Security 6** - Authentication & Authorization
- **Spring Data JPA** - Database access
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **MapStruct** - Entity-DTO mapping
- **Docker** - Containerization
- **GitHub Actions** - CI/CD pipeline

## ?? Quick Start

### Prerequisites

- Java 25+
- Docker & Docker Compose
- Maven 3.8+

### 1. Clone the repository

```bash
git clone https://github.com/nguyendinhthanhh/mentorx-be.git
cd mentorx-be
```

### 2. Setup environment variables

```bash
# Copy environment template
cp .env.example .env

# Edit .env file with your configuration
# IMPORTANT: Set strong passwords and secrets!
```

### 3. Run with Docker (Recommended)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f mentorx-app
```

### 4. Run locally (Alternative)

```bash
# Start PostgreSQL and Redis
docker-compose up -d postgres redis

# Run the application
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## ?? Security Configuration

### ?? CRITICAL: Before deploying to production

1. **Set strong JWT secret** (minimum 32 characters):
   ```bash
   JWT_SECRET=your-super-strong-jwt-secret-here-minimum-32-chars
   ```

2. **Set strong database password**:
   ```bash
   POSTGRES_PASSWORD=your-strong-database-password
   ```

3. **Set wallet secret key**:
   ```bash
   WALLET_SECRET_KEY=your-wallet-secret-for-transaction-hashing
   ```

4. **Configure OAuth2 credentials**:
   ```bash
   GOOGLE_CLIENT_ID=your-google-client-id
   GOOGLE_CLIENT_SECRET=your-google-client-secret
   ```

### ?? Never commit these files:
- `.env` - Contains real secrets
- `application-local.yml` - Local configuration
- `*.log` - Log files
- Database dumps with real data

## ?? API Documentation

Once the application is running, visit:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## ?? Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=UserServiceTest
```

## ?? Docker Commands

```bash
# Build image
docker build -t mentorx-backend .

# Run with compose
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean up
docker-compose down -v
```

## ?? Database Management

### pgAdmin (Development only)
- URL: http://localhost:5050
- Email: admin@mentorx.local
- Password: (set in .env file)

### Database Backup
```bash
# Create backup
make backup-db

# Restore backup
make restore-db
```

## ?? Deployment

### Environment Setup
1. **Development**: Uses `application-dev.yml`
2. **Docker**: Uses `application-docker.yml`
3. **Production**: Uses `application-prod.yml`

### CI/CD Pipeline
The project includes GitHub Actions for:
- Automated testing
- Code quality analysis
- Security scanning
- Docker image building
- Deployment to staging/production

## ?? Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## ?? License

This project is licensed under the MIT License.

## ?? Support

For support and questions:
- Create an issue on GitHub
- Contact: [your-email@domain.com]

---

**?? Security Notice**: This application handles sensitive financial data. Always follow security best practices and never commit secrets to version control.
