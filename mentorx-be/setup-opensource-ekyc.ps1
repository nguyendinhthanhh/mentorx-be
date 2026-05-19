# Setup Open-Source eKYC - Download required files
# This script downloads Tesseract language data and OpenCV Haar Cascades

Write-Host "Setting up Open-Source eKYC (100% FREE!)" -ForegroundColor Cyan
Write-Host ""

# Create directories
Write-Host "Creating directories..." -ForegroundColor Yellow
$tessdataDir = "src\main\resources\tessdata"
$resourcesDir = "src\main\resources"

if (!(Test-Path $tessdataDir)) {
    New-Item -ItemType Directory -Path $tessdataDir -Force | Out-Null
    Write-Host "  Created $tessdataDir" -ForegroundColor Green
} else {
    Write-Host "  $tessdataDir already exists" -ForegroundColor Gray
}

# Download Tesseract language data
Write-Host ""
Write-Host "Downloading Tesseract language data..." -ForegroundColor Yellow

$baseUrl = "https://github.com/tesseract-ocr/tessdata/raw/main"
$languages = @("vie", "eng")

foreach ($lang in $languages) {
    $filename = "$lang.traineddata"
    $filepath = Join-Path $tessdataDir $filename
    $url = "$baseUrl/$filename"
    
    if (Test-Path $filepath) {
        Write-Host "  $filename already exists, skipping..." -ForegroundColor Gray
    } else {
        Write-Host "  Downloading $filename..." -ForegroundColor Cyan
        try {
            Invoke-WebRequest -Uri $url -OutFile $filepath -UseBasicParsing
            Write-Host "  Downloaded $filename" -ForegroundColor Green
        } catch {
            Write-Host "  Failed to download ${filename}: $_" -ForegroundColor Red
        }
    }
}

# Download OpenCV Haar Cascade
Write-Host ""
Write-Host "Downloading OpenCV Haar Cascade..." -ForegroundColor Yellow

$cascadeFile = "haarcascade_frontalface_default.xml"
$cascadePath = Join-Path $resourcesDir $cascadeFile
$cascadeUrl = "https://raw.githubusercontent.com/opencv/opencv/master/data/haarcascades/$cascadeFile"

if (Test-Path $cascadePath) {
    Write-Host "  $cascadeFile already exists, skipping..." -ForegroundColor Gray
} else {
    Write-Host "  Downloading $cascadeFile..." -ForegroundColor Cyan
    try {
        Invoke-WebRequest -Uri $cascadeUrl -OutFile $cascadePath -UseBasicParsing
        Write-Host "  Downloaded $cascadeFile" -ForegroundColor Green
    } catch {
        Write-Host "  Failed to download ${cascadeFile}: $_" -ForegroundColor Red
    }
}

# Verify downloads
Write-Host ""
Write-Host "Verifying downloads..." -ForegroundColor Yellow

$allGood = $true

foreach ($lang in $languages) {
    $filepath = Join-Path $tessdataDir "$lang.traineddata"
    if (Test-Path $filepath) {
        $sizeMB = [math]::Round((Get-Item $filepath).Length / 1MB, 2)
        Write-Host "  OK: $lang.traineddata ($sizeMB MB)" -ForegroundColor Green
    } else {
        Write-Host "  MISSING: $lang.traineddata" -ForegroundColor Red
        $allGood = $false
    }
}

if (Test-Path $cascadePath) {
    $sizeKB = [math]::Round((Get-Item $cascadePath).Length / 1KB, 2)
    Write-Host "  OK: $cascadeFile ($sizeKB KB)" -ForegroundColor Green
} else {
    Write-Host "  MISSING: $cascadeFile" -ForegroundColor Red
    $allGood = $false
}

# Summary
Write-Host ""
if ($allGood) {
    Write-Host "Setup completed successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Next steps:" -ForegroundColor Cyan
    Write-Host "  1. Build project: .\mvnw clean install" -ForegroundColor White
    Write-Host "  2. Run with open-source eKYC:" -ForegroundColor White
    Write-Host "     .\mvnw spring-boot:run -Dspring-boot.run.profiles=dev,opensource-ekyc" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now use 100% FREE eKYC!" -ForegroundColor Cyan
    Write-Host "  - No API key required" -ForegroundColor Gray
    Write-Host "  - Unlimited requests" -ForegroundColor Gray
    Write-Host "  - Complete privacy" -ForegroundColor Gray
} else {
    Write-Host "Setup incomplete - some files are missing" -ForegroundColor Yellow
    Write-Host "  Please check the errors above and try again" -ForegroundColor White
}

Write-Host ""
Write-Host "Documentation: OPENSOURCE_EKYC_SETUP.md" -ForegroundColor Cyan
Write-Host ""
