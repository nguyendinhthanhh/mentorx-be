# Matching Module API Documentation

## Overview
The Matching module provides intelligent mentor-user matching capabilities, user interest profiling, and saved search functionality for the MentorX platform.

## Features
- **Mentor Match Scores**: Compute and manage compatibility scores between users and mentors
- **User Interest Profiles**: Track and analyze user interests and behavior patterns
- **Saved Searches**: Allow users to save and reuse search criteria

---

## API Endpoints

### 1. Mentor Match Scores

#### Base URL: `/api/matching/mentor-match-scores`

#### Create Match Score
```http
POST /api/matching/mentor-match-scores
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": "uuid",
  "mentorProfileId": "uuid",
  "matchScore": 0.85,
  "interestCompatibility": 0.90,
  "skillCompatibility": 0.80,
  "budgetCompatibility": 0.75,
  "availabilityCompatibility": 0.85,
  "communicationCompatibility": 0.88,
  "geographicCompatibility": 0.92,
  "algorithmVersion": "1.0.0"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Mentor match score created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "userFullName": "John Doe",
    "mentorProfileId": "uuid",
    "mentorFullName": "Jane Smith",
    "matchScore": 0.85,
    "interestCompatibility": 0.90,
    "skillCompatibility": 0.80,
    "budgetCompatibility": 0.75,
    "availabilityCompatibility": 0.85,
    "communicationCompatibility": 0.88,
    "geographicCompatibility": 0.92,
    "computedAt": "2026-05-04 10:30:00",
    "expiresAt": "2026-05-11 10:30:00",
    "algorithmVersion": "1.0.0",
    "isShown": false,
    "shownAt": null,
    "showCount": 0,
    "createdAt": "2026-05-04 10:30:00",
    "updatedAt": "2026-05-04 10:30:00"
  },
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Get Match Score by ID
```http
GET /api/matching/mentor-match-scores/{id}
Authorization: Bearer {token}
```

#### Update Match Score
```http
PUT /api/matching/mentor-match-scores/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "matchScore": 0.90,
  "interestCompatibility": 0.95
}
```

#### Delete Match Score
```http
DELETE /api/matching/mentor-match-scores/{id}
Authorization: Bearer {token}
```

#### Get All Match Scores (Admin/Moderator)
```http
GET /api/matching/mentor-match-scores?page=0&size=20&sortBy=matchScore&sortDir=desc
Authorization: Bearer {token}
```

#### Get Match Scores by User
```http
GET /api/matching/mentor-match-scores/user/{userId}?page=0&size=20
Authorization: Bearer {token}
```

#### Get Match Scores by Mentor
```http
GET /api/matching/mentor-match-scores/mentor/{mentorProfileId}?page=0&size=20
Authorization: Bearer {token}
```

#### Get Top Matches for User
```http
GET /api/matching/mentor-match-scores/user/{userId}/top?limit=10
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "userFullName": "John Doe",
      "mentorProfileId": "uuid",
      "mentorFullName": "Jane Smith",
      "matchScore": 0.95,
      "interestCompatibility": 0.98,
      "skillCompatibility": 0.92,
      "budgetCompatibility": 0.90,
      "availabilityCompatibility": 0.95,
      "communicationCompatibility": 0.96,
      "geographicCompatibility": 0.98,
      "computedAt": "2026-05-04 10:30:00",
      "expiresAt": "2026-05-11 10:30:00",
      "algorithmVersion": "1.0.0",
      "isShown": false,
      "shownAt": null,
      "showCount": 0,
      "createdAt": "2026-05-04 10:30:00",
      "updatedAt": "2026-05-04 10:30:00"
    }
  ],
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Mark Match as Shown
```http
POST /api/matching/mentor-match-scores/{id}/mark-shown
Authorization: Bearer {token}
```

#### Recompute Expired Scores (Admin)
```http
POST /api/matching/mentor-match-scores/recompute-expired
Authorization: Bearer {token}
```

#### Compute Match Score (Admin/Moderator)
```http
POST /api/matching/mentor-match-scores/compute?userId={uuid}&mentorProfileId={uuid}
Authorization: Bearer {token}
```

---

### 2. User Interest Profiles

#### Base URL: `/api/matching/user-interest-profiles`

#### Create Interest Profile
```http
POST /api/matching/user-interest-profiles
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": "uuid",
  "categoryId": 1,
  "interestScore": 0.75,
  "interactionCount": 10,
  "timeSpentMinutes": 120,
  "decayFactor": 0.95,
  "isExplicit": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "User interest profile created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "userFullName": "John Doe",
    "categoryId": 1,
    "categoryName": "Software Development",
    "interestScore": 0.75,
    "interactionCount": 10,
    "timeSpentMinutes": 120,
    "lastInteractionAt": null,
    "lastUpdated": "2026-05-04 10:30:00",
    "decayFactor": 0.95,
    "isExplicit": true,
    "createdAt": "2026-05-04 10:30:00",
    "updatedAt": "2026-05-04 10:30:00"
  },
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Get Interest Profile by ID
```http
GET /api/matching/user-interest-profiles/{id}
Authorization: Bearer {token}
```

#### Update Interest Profile
```http
PUT /api/matching/user-interest-profiles/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "interestScore": 0.85,
  "interactionCount": 15
}
```

#### Delete Interest Profile
```http
DELETE /api/matching/user-interest-profiles/{id}
Authorization: Bearer {token}
```

#### Get All Interest Profiles (Admin/Moderator)
```http
GET /api/matching/user-interest-profiles?page=0&size=20&sortBy=interestScore&sortDir=desc
Authorization: Bearer {token}
```

#### Get Interest Profiles by User
```http
GET /api/matching/user-interest-profiles/user/{userId}
Authorization: Bearer {token}
```

#### Get Top Interests for User
```http
GET /api/matching/user-interest-profiles/user/{userId}/top?limit=5
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "userFullName": "John Doe",
      "categoryId": 1,
      "categoryName": "Software Development",
      "interestScore": 0.95,
      "interactionCount": 50,
      "timeSpentMinutes": 600,
      "lastInteractionAt": "2026-05-04 09:00:00",
      "lastUpdated": "2026-05-04 10:30:00",
      "decayFactor": 0.95,
      "isExplicit": true,
      "createdAt": "2026-05-03 10:30:00",
      "updatedAt": "2026-05-04 10:30:00"
    }
  ],
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Record User Interaction
```http
POST /api/matching/user-interest-profiles/record-interaction?userId={uuid}&categoryId=1&timeSpentMinutes=5
Authorization: Bearer {token}
```

**Description:** Records a user interaction with a category. If the interest profile doesn't exist, it will be created automatically.

#### Apply Decay to Interests (Admin/Moderator)
```http
POST /api/matching/user-interest-profiles/user/{userId}/apply-decay
Authorization: Bearer {token}
```

**Description:** Applies decay factor to all interest scores for a user to reduce scores over time.

---

### 3. Saved Searches

#### Base URL: `/api/matching/saved-searches`

#### Create Saved Search
```http
POST /api/matching/saved-searches
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": "uuid",
  "name": "Senior Java Developers",
  "filters": "{\"skills\":[\"java\",\"spring-boot\"],\"minExperience\":5,\"maxRate\":100}"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Saved search created successfully",
  "data": {
    "id": "uuid",
    "userId": "uuid",
    "userFullName": "John Doe",
    "name": "Senior Java Developers",
    "filters": "{\"skills\":[\"java\",\"spring-boot\"],\"minExperience\":5,\"maxRate\":100}",
    "createdAt": "2026-05-04 10:30:00"
  },
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Get Saved Search by ID
```http
GET /api/matching/saved-searches/{id}
Authorization: Bearer {token}
```

#### Update Saved Search
```http
PUT /api/matching/saved-searches/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Senior Java/Spring Developers",
  "filters": "{\"skills\":[\"java\",\"spring-boot\",\"microservices\"],\"minExperience\":5,\"maxRate\":120}"
}
```

#### Delete Saved Search
```http
DELETE /api/matching/saved-searches/{id}
Authorization: Bearer {token}
```

#### Get All Saved Searches (Admin/Moderator)
```http
GET /api/matching/saved-searches?page=0&size=20&sortBy=createdAt&sortDir=desc
Authorization: Bearer {token}
```

#### Get Saved Searches by User
```http
GET /api/matching/saved-searches/user/{userId}
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": "uuid",
      "userId": "uuid",
      "userFullName": "John Doe",
      "name": "Senior Java Developers",
      "filters": "{\"skills\":[\"java\",\"spring-boot\"],\"minExperience\":5,\"maxRate\":100}",
      "createdAt": "2026-05-04 10:30:00"
    },
    {
      "id": "uuid",
      "userId": "uuid",
      "userFullName": "John Doe",
      "name": "React Experts",
      "filters": "{\"skills\":[\"react\",\"typescript\"],\"minExperience\":3}",
      "createdAt": "2026-05-03 15:20:00"
    }
  ],
  "timestamp": "2026-05-04 10:30:00"
}
```

#### Get Saved Searches by User (Paginated)
```http
GET /api/matching/saved-searches/user/{userId}/paginated?page=0&size=10
Authorization: Bearer {token}
```

#### Count Saved Searches by User
```http
GET /api/matching/saved-searches/user/{userId}/count
Authorization: Bearer {token}
```

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": 5,
  "timestamp": "2026-05-04 10:30:00"
}
```

---

## Data Models

### MentorMatchScore
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userFullName": "string",
  "mentorProfileId": "uuid",
  "mentorFullName": "string",
  "matchScore": "decimal (0.0-1.0)",
  "interestCompatibility": "decimal (0.0-1.0)",
  "skillCompatibility": "decimal (0.0-1.0)",
  "budgetCompatibility": "decimal (0.0-1.0)",
  "availabilityCompatibility": "decimal (0.0-1.0)",
  "communicationCompatibility": "decimal (0.0-1.0)",
  "geographicCompatibility": "decimal (0.0-1.0)",
  "computedAt": "datetime",
  "expiresAt": "datetime",
  "algorithmVersion": "string",
  "isShown": "boolean",
  "shownAt": "datetime",
  "showCount": "integer",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### UserInterestProfile
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userFullName": "string",
  "categoryId": "integer",
  "categoryName": "string",
  "interestScore": "decimal (0.0-1.0)",
  "interactionCount": "integer",
  "timeSpentMinutes": "integer",
  "lastInteractionAt": "datetime",
  "lastUpdated": "datetime",
  "decayFactor": "decimal (0.0-1.0)",
  "isExplicit": "boolean",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

### SavedSearch
```json
{
  "id": "uuid",
  "userId": "uuid",
  "userFullName": "string",
  "name": "string (max 100 chars)",
  "filters": "string (JSON)",
  "createdAt": "datetime"
}
```

---

## Authorization

### Required Roles

| Endpoint | Required Role |
|----------|--------------|
| Create Match Score | ADMIN, MODERATOR |
| Get Match Score | Authenticated User |
| Update Match Score | ADMIN, MODERATOR |
| Delete Match Score | ADMIN |
| Get All Match Scores | ADMIN, MODERATOR |
| Get User Matches | Authenticated User |
| Mark as Shown | Authenticated User |
| Recompute Scores | ADMIN |
| Compute Match Score | ADMIN, MODERATOR |
| Create Interest Profile | Authenticated User |
| Get Interest Profile | Authenticated User |
| Update Interest Profile | Authenticated User |
| Delete Interest Profile | Authenticated User |
| Record Interaction | Authenticated User |
| Apply Decay | ADMIN, MODERATOR |
| Create Saved Search | Authenticated User |
| Get Saved Search | Authenticated User |
| Update Saved Search | Authenticated User |
| Delete Saved Search | Authenticated User |

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation error",
  "data": null,
  "timestamp": "2026-05-04 10:30:00"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Token has expired",
  "data": null,
  "timestamp": "2026-05-04 10:30:00"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "Access denied",
  "data": null,
  "timestamp": "2026-05-04 10:30:00"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "data": null,
  "timestamp": "2026-05-04 10:30:00"
}
```

---

## Usage Examples

### Example 1: Get Top Mentor Matches for a User
```bash
curl -X GET "http://localhost:8080/api/matching/mentor-match-scores/user/123e4567-e89b-12d3-a456-426614174000/top?limit=5" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 2: Record User Interaction
```bash
curl -X POST "http://localhost:8080/api/matching/user-interest-profiles/record-interaction?userId=123e4567-e89b-12d3-a456-426614174000&categoryId=1&timeSpentMinutes=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Example 3: Create Saved Search
```bash
curl -X POST "http://localhost:8080/api/matching/saved-searches" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Python ML Engineers",
    "filters": "{\"skills\":[\"python\",\"machine-learning\",\"tensorflow\"],\"minExperience\":3,\"maxRate\":150}"
  }'
```

---

## Notes

1. **Match Score Expiration**: Match scores expire after 7 days by default and need to be recomputed.
2. **Interest Decay**: Interest scores decay over time based on the decay factor (default 0.95).
3. **Filters Format**: Saved search filters are stored as JSON strings for flexibility.
4. **Pagination**: Most list endpoints support pagination with `page`, `size`, `sortBy`, and `sortDir` parameters.
5. **Algorithm Version**: Match scores include an algorithm version for tracking and recomputation purposes.

---

## Future Enhancements

- [ ] Implement actual matching algorithm with ML models
- [ ] Add real-time match score updates via WebSocket
- [ ] Implement collaborative filtering for recommendations
- [ ] Add A/B testing for different matching algorithms
- [ ] Implement match score explanation API
- [ ] Add batch computation endpoints for performance
- [ ] Implement caching for frequently accessed matches
