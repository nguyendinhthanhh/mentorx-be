# Hướng Dẫn Restore Mentor Module

## Tình Huống
Trong quá trình refactoring từ `mentor/` sang 3 modules riêng (mentorship, learning, scheduling), đã xảy ra 100+ compilation errors. 

## Quyết Định
**ROLLBACK** về cấu trúc ban đầu với module `mentor/` và giữ nguyên.

## Cách Restore

### Option 1: Từ Git Stash (Nếu đã stash)
```bash
git stash list
git stash apply stash@{0}
```

### Option 2: Từ IDE Local History
1. Mở IntelliJ IDEA
2. Right-click vào folder `src/main/java/com/mentorx/api/feature/`
3. Chọn **Local History** → **Show History**
4. Tìm thời điểm trước khi refactor
5. Click **Revert**

### Option 3: Từ Git Reflog
```bash
git reflog
git reset --hard HEAD@{n}  # n = số thứ tự trước khi refactor
```

### Option 4: Tạo Lại Từ Đầu
Nếu không restore được, tôi sẽ tạo lại toàn bộ 30 files với code đã viết.

## Files Cần Restore

### Entities (4 files)
- `entity/MentorPackage.java`
- `entity/MentorOffering.java`
- `entity/MentorAvailability.java`
- `entity/MentorBlockedDate.java`

### DTOs (9 files)
**Request:**
- `dto/request/MentorPackageRequest.java`
- `dto/request/MentorOfferingRequest.java`
- `dto/request/MentorAvailabilityRequest.java`
- `dto/request/MentorBlockedDateRequest.java`

**Response:**
- `dto/response/MentorPackageResponse.java`
- `dto/response/MentorOfferingResponse.java`
- `dto/response/MentorAvailabilityResponse.java`
- `dto/response/MentorBlockedDateResponse.java`
- `dto/response/WeeklyAvailabilityResponse.java`

### Repositories (4 files)
- `repository/MentorPackageRepository.java`
- `repository/MentorOfferingRepository.java`
- `repository/MentorAvailabilityRepository.java`
- `repository/MentorBlockedDateRepository.java`

### Services (6 files)
**Interfaces:**
- `service/MentorPackageService.java`
- `service/MentorOfferingService.java`
- `service/MentorAvailabilityService.java`

**Implementations:**
- `service/impl/MentorPackageServiceImpl.java`
- `service/impl/MentorOfferingServiceImpl.java`
- `service/impl/MentorAvailabilityServiceImpl.java`

### Controllers (3 files)
- `controller/MentorPackageController.java`
- `controller/MentorOfferingController.java`
- `controller/MentorAvailabilityController.java`

## Sau Khi Restore

1. **Compile để kiểm tra**:
```bash
./mvnw compile -DskipTests
```

2. **Xóa các folder không cần**:
```bash
rm -rf src/main/java/com/mentorx/api/feature/mentorship
rm -rf src/main/java/com/mentorx/api/feature/learning
rm -rf src/main/java/com/mentorx/api/feature/scheduling
```

3. **Xóa migration không cần**:
```bash
rm src/main/resources/db/migration/V2.3.1__rename_mentor_services_tables.sql
```

4. **Commit code**:
```bash
git add .
git commit -m "refactor: keep mentor module structure, add documentation"
```

## Kết Luận

Giữ nguyên cấu trúc `mentor/` là quyết định đúng đắn vì:
- ✅ Đơn giản hơn
- ✅ Ít lỗi hơn
- ✅ Dễ maintain
- ✅ Vẫn đảm bảo modular architecture

Xem `MODULAR_MONOLITH_REFACTORING_COMPLETE.md` để hiểu rõ hơn về quyết định kiến trúc.

