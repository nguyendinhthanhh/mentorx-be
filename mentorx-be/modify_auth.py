import sys

with open('src/main/java/com/mentorx/api/auth/service/serviceImpl/AuthServiceImpl.java', 'r', encoding='utf-8') as f:
    content = f.read()

if 'googleClientId' not in content:
    content = content.replace('private Long refreshTokenExpiryMs;', 'private Long refreshTokenExpiryMs;\n\n    @org.springframework.beans.factory.annotation.Value("${spring.security.oauth2.client.registration.google.client-id:default}")\n    private String googleClientId;')

if 'public AuthResponse googleLogin' not in content:
    new_method = """public AuthResponse googleLogin(com.mentorx.api.auth.dto.request.GoogleLoginRequest request) {
        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier verifier = new com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier.Builder(new com.google.api.client.http.javanet.NetHttpTransport(), com.google.api.client.json.gson.GsonFactory.getDefaultInstance())
                    .setAudience(java.util.Collections.singletonList(googleClientId))
                    .build();

            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken idToken = verifier.verify(request.getCredential());
            if (idToken != null) {
                com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String firstName = (String) payload.get("given_name");
                if (firstName == null) firstName = "User";
                String lastName = (String) payload.get("family_name");
                if (lastName == null) lastName = "";
                String subject = payload.getSubject();

                return handleOAuth2Success(email, firstName, lastName, "google", subject);
            } else {
                throw new AppException(ErrorCode.INVALID_CREDENTIALS);
            }
        } catch (Exception e) {
            log.error("Google login failed", e);
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken("""
    
    content = content.replace('public AuthResponse refreshToken(', new_method)

with open('src/main/java/com/mentorx/api/auth/service/serviceImpl/AuthServiceImpl.java', 'w', encoding='utf-8') as f:
    f.write(content)
