# Enable Mock Mode for FPT AI
# This script automatically configures the project to use mock FPT AI services

Write-Host "🔧 Enabling Mock Mode for FPT AI..." -ForegroundColor Cyan
Write-Host ""

# 1. Add @Profile annotation to FptAiClient.java
Write-Host "1. Updating FptAiClient.java..." -ForegroundColor Yellow

$fptAiClientPath = "src\main\java\com\mentorx\api\feature\user\service\FptAiClient.java"

if (Test-Path $fptAiClientPath) {
    $content = Get-Content $fptAiClientPath -Raw
    
    # Check if already has @Profile
    if ($content -notmatch '@Profile\("!dev-mock"\)') {
        # Add import if not exists
        if ($content -notmatch 'import org.springframework.context.annotation.Profile;') {
            $content = $content -replace '(import org.springframework.stereotype.Service;)', "`$1`nimport org.springframework.context.annotation.Profile;"
        }
        
        # Add @Profile annotation
        $content = $content -replace '(@Service\s+@RequiredArgsConstructor\s+@Slf4j)', "`$1`n@Profile(`"!dev-mock`")"
        
        Set-Content $fptAiClientPath $content -NoNewline
        Write-Host "   ✅ Added @Profile annotation" -ForegroundColor Green
    } else {
        Write-Host "   ℹ️  @Profile annotation already exists" -ForegroundColor Gray
    }
} else {
    Write-Host "   ❌ FptAiClient.java not found!" -ForegroundColor Red
    exit 1
}

# 2. Check if MockFptAiClient.java exists
Write-Host "2. Checking MockFptAiClient.java..." -ForegroundColor Yellow

$mockClientPath = "src\main\java\com\mentorx\api\feature\user\service\MockFptAiClient.java"

if (Test-Path $mockClientPath) {
    Write-Host "   ✅ MockFptAiClient.java exists" -ForegroundColor Green
} else {
    Write-Host "   ❌ MockFptAiClient.java not found!" -ForegroundColor Red
    Write-Host "   Please create it manually or copy from the documentation" -ForegroundColor Yellow
}

# 3. Instructions
Write-Host ""
Write-Host "📝 Next Steps:" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Stop the backend if running (Ctrl+C)" -ForegroundColor White
Write-Host ""
Write-Host "2. Start with mock profile:" -ForegroundColor White
Write-Host "   .\mvnw spring-boot:run -Dspring-boot.run.profiles=dev,dev-mock" -ForegroundColor Green
Write-Host ""
Write-Host "3. Test eKYC - you should see:" -ForegroundColor White
Write-Host "   🔶 MOCK MODE: Performing OCR - returning fake CCCD data" -ForegroundColor Gray
Write-Host "   🔶 MOCK MODE: Checking liveness - returning LIVE with high score" -ForegroundColor Gray
Write-Host "   🔶 MOCK MODE: Matching faces - returning MATCH with high similarity" -ForegroundColor Gray
Write-Host ""
Write-Host "✅ Mock mode configuration complete!" -ForegroundColor Green
Write-Host ""
Write-Host "ℹ️  To use real FPT AI API:" -ForegroundColor Cyan
Write-Host "   1. Get API key from https://console.fpt.ai/" -ForegroundColor White
Write-Host "   2. Update .env: FPT_AI_API_KEY=your_key" -ForegroundColor White
Write-Host "   3. Restart without mock profile: .\mvnw spring-boot:run" -ForegroundColor White
Write-Host ""
