import sys

with open('src/main/java/com/mentorx/api/auth/controller/AuthController.java', 'r', encoding='utf-8') as f:
    content = f.read()

new_method = """    @PostMapping("/google")
    @Operation(summary = "Google login", description = "Authenticate user via Google ID Token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody com.mentorx.api.auth.dto.request.GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }

    @PostMapping("/refresh")"""

if 'public ResponseEntity<ApiResponse<AuthResponse>> googleLogin' not in content:
    content = content.replace('    @PostMapping("/refresh")', new_method)

with open('src/main/java/com/mentorx/api/auth/controller/AuthController.java', 'w', encoding='utf-8') as f:
    f.write(content)
