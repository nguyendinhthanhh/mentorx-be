# Script to fix AppException calls in course service implementations

$files = @(
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseSectionServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseLessonServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseEnrollmentServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/LessonProgressServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/LessonCommentServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CartItemServiceImpl.java"
)

$replacements = @{
    'new AppException\(HttpStatus\.NOT_FOUND, "Course not found"\)' = 'new AppException(ErrorCode.COURSE_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Course section not found"\)' = 'new AppException(ErrorCode.COURSE_SECTION_NOT_FOUND)'
    'new AppException\(HttpStatus\.BAD_REQUEST, "Section order already exists for this course"\)' = 'new AppException(ErrorCode.SECTION_ORDER_EXISTS)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Lesson not found"\)' = 'new AppException(ErrorCode.LESSON_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Course lesson not found"\)' = 'new AppException(ErrorCode.LESSON_NOT_FOUND)'
    'new AppException\(HttpStatus\.BAD_REQUEST, "Lesson order already exists for this section"\)' = 'new AppException(ErrorCode.LESSON_ORDER_EXISTS)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Enrollment not found"\)' = 'new AppException(ErrorCode.ENROLLMENT_NOT_FOUND)'
    'new AppException\(HttpStatus\.BAD_REQUEST, "Student is already enrolled in this course"\)' = 'new AppException(ErrorCode.ALREADY_ENROLLED)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Student not found"\)' = 'new AppException(ErrorCode.USER_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "User not found"\)' = 'new AppException(ErrorCode.USER_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Lesson progress not found"\)' = 'new AppException(ErrorCode.LESSON_PROGRESS_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Comment not found"\)' = 'new AppException(ErrorCode.COMMENT_NOT_FOUND)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Comment has been deleted"\)' = 'new AppException(ErrorCode.COMMENT_DELETED)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Parent comment not found"\)' = 'new AppException(ErrorCode.COMMENT_NOT_FOUND)'
    'new AppException\(HttpStatus\.FORBIDDEN, "You can only update your own comments"\)' = 'new AppException(ErrorCode.COMMENT_UPDATE_FORBIDDEN)'
    'new AppException\(HttpStatus\.BAD_REQUEST, "Cannot update deleted comment"\)' = 'new AppException(ErrorCode.COMMENT_DELETED)'
    'new AppException\(HttpStatus\.FORBIDDEN, "You can only delete your own comments"\)' = 'new AppException(ErrorCode.COMMENT_DELETE_FORBIDDEN)'
    'new AppException\(HttpStatus\.BAD_REQUEST, "Course is already in cart"\)' = 'new AppException(ErrorCode.COURSE_ALREADY_IN_CART)'
    'new AppException\(HttpStatus\.NOT_FOUND, "Cart item not found"\)' = 'new AppException(ErrorCode.CART_ITEM_NOT_FOUND)'
    'import org\.springframework\.http\.HttpStatus;' = ''
}

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing $file..."
        $content = Get-Content $file -Raw
        
        foreach ($pattern in $replacements.Keys) {
            $replacement = $replacements[$pattern]
            $content = $content -replace $pattern, $replacement
        }
        
        Set-Content $file $content -NoNewline
        Write-Host "  Fixed $file"
    } else {
        Write-Host "  File not found: $file" -ForegroundColor Yellow
    }
}

Write-Host "`nAll files processed!" -ForegroundColor Green
Write-Host "Now run: mvn clean compile -DskipTests" -ForegroundColor Cyan
