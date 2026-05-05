# Script to add ErrorCode import to service implementations

$files = @(
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseSectionServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseLessonServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CourseEnrollmentServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/LessonProgressServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/LessonCommentServiceImpl.java",
    "src/main/java/com/mentorx/api/feature/course/service/impl/CartItemServiceImpl.java"
)

$importStatement = "import com.mentorx.api.common.exception.ErrorCode;"

foreach ($file in $files) {
    if (Test-Path $file) {
        Write-Host "Processing $file..."
        $content = Get-Content $file -Raw
        
        # Check if import already exists
        if ($content -notmatch "import com\.mentorx\.api\.common\.exception\.ErrorCode;") {
            # Find the package declaration and add import after it
            $content = $content -replace "(package com\.mentorx\.api\.feature\.course\.service\.impl;)", "`$1`n`n$importStatement"
            
            Set-Content $file $content -NoNewline
            Write-Host "  Added ErrorCode import to $file" -ForegroundColor Green
        } else {
            Write-Host "  ErrorCode import already exists in $file" -ForegroundColor Yellow
        }
    } else {
        Write-Host "  File not found: $file" -ForegroundColor Red
    }
}

Write-Host "`nAll files processed!" -ForegroundColor Green
