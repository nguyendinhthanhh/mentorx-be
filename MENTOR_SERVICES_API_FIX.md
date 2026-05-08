# Mentor Services API Fix - userId vs mentorProfileId

## Vấn Đề
Frontend gọi API với `userId` nhưng backend service đang tìm theo `mentorProfileId`, dẫn đến không tìm thấy data.

## Nguyên Nhân
- Controller nhận `userId` từ URL path parameter
- Service implementation đang dùng `userId` như là `mentorProfileId` 
- Cần convert `userId` → `mentorProfileId` thông qua `MentorProfile` entity

## Giải Pháp
Sửa tất cả service implementations để convert `userId` sang `mentorProfileId`:

### 1. MentorPackageServiceImpl
**Methods đã sửa:**
- `createPackage(UUID userId, ...)` - Thêm lookup mentorProfile by userId
- `getAllPackagesByMentor(UUID userId)` - Thêm lookup mentorProfile by userId
- `getActivePackagesByMentor(UUID userId)` - Thêm lookup mentorProfile by userId

**Code pattern:**
```java
// Get mentor profile by user ID
MentorProfile mentorProfile = mentorProfileRepository.findByUserId(userId)
        .orElseThrow(() -> new AppException(ErrorCode.MENTOR_PROFILE_NOT_FOUND));

// Use mentorProfile.getId() for queries
List<MentorPackage> packages = mentorPackageRepository
        .findByMentorProfileIdOrderByDisplayOrderAsc(mentorProfile.getId());
```

### 2. MentorOfferingServiceImpl
**Methods đã sửa:**
- `createCourse(UUID userId, ...)` - Thêm lookup mentorProfile by userId
- `getAllCoursesByMentor(UUID userId)` - Thêm lookup mentorProfile by userId
- `getPublishedCoursesByMentor(UUID userId)` - Thêm lookup mentorProfile by userId

### 3. MentorAvailabilityServiceImpl
**Methods đã sửa:**
- `createAvailability(UUID userId, ...)` - Thêm lookup mentorProfile by userId
- `getAllAvailabilityByMentor(UUID userId)` - Thêm lookup mentorProfile by userId
- `getWeeklyAvailability(UUID userId)` - Thêm lookup mentorProfile by userId
- `blockDate(UUID userId, ...)` - Thêm lookup mentorProfile by userId
- `getBlockedDatesByMentor(UUID userId)` - Thêm lookup mentorProfile by userId

## API Endpoints (Không Đổi)
Endpoints vẫn giữ nguyên, nhận `userId`:

```
GET /api/mentors/{userId}/packages/active
GET /api/mentors/{userId}/courses/published
GET /api/mentors/{userId}/availability/week
```

## Frontend Changes
Thêm console.log để debug API calls:

```typescript
const { data: packages = [], isLoading: packagesLoading } = useQuery(
  ['mentor-packages', userId],
  async () => {
    console.log('🔵 Fetching packages for userId:', userId)
    const result = await mentorApi.getActiveMentorPackages(userId!)
    console.log('📦 Packages result:', result)
    return result
  },
  { enabled: !!userId }
)
```

## Testing
1. **Restart backend server** để áp dụng thay đổi
2. **Mở browser DevTools** (F12) → Console tab
3. **Vào trang** `http://localhost:3000/mentors/{userId}`
4. **Kiểm tra console logs:**
   - `🔵 Fetching packages for userId: ...`
   - `📦 Packages result: [...]`
   - `🔵 Fetching courses for userId: ...`
   - `📚 Courses result: [...]`
   - `🔵 Fetching availability for userId: ...`
   - `📅 Availability result: {...}`

5. **Kiểm tra Network tab:**
   - Xem các API calls có status 200 OK
   - Xem response data có đúng không

## Expected Behavior

### Nếu mentor có data:
- API trả về packages/courses/availability từ database
- UI hiển thị data thật
- Console log hiển thị data

### Nếu mentor chưa có data:
- API trả về mảng rỗng `[]` hoặc `{weeklySchedule: {}, blockedDates: []}`
- UI hiển thị empty state: "Mentor chưa tạo gói mentoring nào"
- Hoặc fallback về mock data để demo

### Nếu user không phải mentor:
- API trả về 404 với message "Mentor profile not found"
- Frontend hiển thị error state

## Files Changed
- `mentorx-be/src/main/java/com/mentorx/api/feature/mentor/service/impl/MentorPackageServiceImpl.java`
- `mentorx-be/src/main/java/com/mentorx/api/feature/mentor/service/impl/MentorOfferingServiceImpl.java`
- `mentorx-be/src/main/java/com/mentorx/api/feature/mentor/service/impl/MentorAvailabilityServiceImpl.java`
- `mentorx-fe/src/pages/mentor/MentorPublicProfilePage.tsx`

## Build Status
✅ Backend compiles successfully
✅ All service methods updated
✅ Frontend console logging added
⏳ Waiting for backend restart to test

## Next Steps
1. ✅ Restart backend server
2. ✅ Test API calls in browser
3. ✅ Verify data is fetched correctly
4. ✅ Remove console.log sau khi test xong

