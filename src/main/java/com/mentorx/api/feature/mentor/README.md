# Mentor Services Module

## Domain: Mentor Services
This module handles all mentor-related services including packages, offerings, and availability management.

## Sub-domains

### 1. Mentorship Packages (`package/`)
**Responsibility**: Manage mentoring service packages (1-1 sessions, package deals, subscriptions)

**Entities**:
- `MentorPackage` - Mentoring service packages

**Key Features**:
- Create/Update/Delete packages
- Toggle active status
- Display order management

### 2. Mentor Offerings
**Responsibility**: Manage mentor service offerings (simple course-like packages)

**Entities**:
- `MentorOffering` - Mentor service offerings

**Key Features**:
- Create/Update/Delete offerings
- Publish/Archive offerings
- Offering status management (DRAFT, PUBLISHED, ARCHIVED)

### 3. Availability & Scheduling (`availability/`)
**Responsibility**: Manage mentor availability and blocked dates

**Entities**:
- `MentorAvailability` - Weekly recurring availability
- `MentorBlockedDate` - Specific blocked dates

**Key Features**:
- Set weekly availability
- Block specific dates
- Get weekly schedule

## Module Boundaries

### Internal Dependencies
- `user.MentorProfile` - All entities reference MentorProfile

### External API
- Controllers expose REST APIs
- Services provide business logic
- Repositories handle data access

## Architecture

```
mentor/
├── controller/          # REST API endpoints
├── service/            # Business logic
│   └── impl/          # Service implementations
├── repository/         # Data access
├── entity/            # Domain entities
└── dto/               # Data transfer objects
    ├── request/       # API request DTOs
    └── response/      # API response DTOs
```

## Database Tables
- `mentor_packages` - Mentorship packages
- `mentor_courses` - Educational courses
- `mentor_availability` - Weekly availability
- `mentor_blocked_dates` - Blocked dates

## API Endpoints

### Packages
- `GET /api/mentors/{userId}/packages` - Get all packages
- `POST /api/mentors/{userId}/packages` - Create package
- `PUT /api/mentors/packages/{id}` - Update package
- `DELETE /api/mentors/packages/{id}` - Delete package

### Offerings
- `GET /api/mentors/{userId}/courses` - Get all offerings
- `POST /api/mentors/{userId}/courses` - Create offering
- `PATCH /api/mentors/courses/{id}/publish` - Publish offering

### Availability
- `GET /api/mentors/{userId}/availability/week` - Get weekly schedule
- `POST /api/mentors/{userId}/availability` - Set availability
- `POST /api/mentors/{userId}/blocked-dates` - Block date
