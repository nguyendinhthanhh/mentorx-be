# Security Policy

## 🔐 Security Best Practices

### Environment Variables

**❌ NEVER commit these to git:**
```bash
# Real secrets
JWT_SECRET=abc123xyz789
POSTGRES_PASSWORD=mypassword123
WALLET_SECRET_KEY=secret123
AWS_ACCESS_KEY_ID=AKIA...
STRIPE_SECRET_KEY=sk_live_...
```

**✅ Always use environment variables:**
```bash
# In .env file (never committed)
JWT_SECRET=your-super-strong-32-character-secret
POSTGRES_PASSWORD=complex-database-password-here
WALLET_SECRET_KEY=wallet-hashing-secret-key
```

### Required Security Configuration

#### 1. JWT Secret (CRITICAL)
- **Minimum 32 characters**
- **Use cryptographically secure random string**
- **Different for each environment**

```bash
# Generate strong JWT secret
openssl rand -base64 32
```

#### 2. Database Security
```bash
# Strong password requirements
POSTGRES_PASSWORD=Min8Chars!WithSpecial@Characters#
```

#### 3. Wallet Security
```bash
# Used for transaction hash chain integrity
WALLET_SECRET_KEY=unique-secret-for-transaction-hashing
```

### File Security Checklist

#### ❌ Files that should NEVER be committed:
- `.env` - Real environment variables
- `application-local.yml` - Local configuration
- `application-secret.yml` - Secret configuration
- `*.log` - Log files may contain sensitive data
- `database.sql` - Database dumps with real data
- `backup.sql` - Database backups
- `*.key`, `*.pem` - Private keys and certificates
- `credentials.json` - Service account files

#### ✅ Files that are safe to commit:
- `.env.example` - Template without real values
- `application.yml` - Uses environment variables
- `docker-compose.yml` - Uses environment variables
- Source code files

### Docker Security

#### ❌ Insecure docker-compose.yml:
```yaml
environment:
  POSTGRES_PASSWORD: admin123  # Hardcoded password
  JWT_SECRET: mysecret         # Weak secret
```

#### ✅ Secure docker-compose.yml:
```yaml
environment:
  POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}  # From .env
  JWT_SECRET: ${JWT_SECRET}                # From .env
```

### Production Security Checklist

- [ ] Strong JWT secret (32+ characters)
- [ ] Strong database passwords
- [ ] HTTPS enabled
- [ ] CORS properly configured
- [ ] Rate limiting enabled
- [ ] Input validation on all endpoints
- [ ] SQL injection protection (using JPA)
- [ ] XSS protection headers
- [ ] CSRF protection (if needed)
- [ ] Secure session management
- [ ] Regular security updates
- [ ] Monitoring and logging
- [ ] Backup encryption

### Common Security Vulnerabilities

#### 1. Hardcoded Secrets
```java
// ❌ NEVER do this
String jwtSecret = "mySecretKey123";
String dbPassword = "admin123";

// ✅ Use environment variables
@Value("${jwt.secret}")
private String jwtSecret;
```

#### 2. SQL Injection
```java
// ❌ Vulnerable to SQL injection
String query = "SELECT * FROM users WHERE email = '" + email + "'";

// ✅ Use JPA/prepared statements
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);
```

#### 3. Weak Password Validation
```java
// ❌ Weak validation
if (password.length() > 6) { ... }

// ✅ Strong validation
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")
private String password;
```

### Incident Response

If you discover a security vulnerability:

1. **DO NOT** create a public GitHub issue
2. Email security concerns to: [security@mentorx.com]
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

### Security Updates

- Monitor dependencies for vulnerabilities
- Update Spring Boot and dependencies regularly
- Review security advisories
- Test security patches in staging first

### Monitoring and Alerting

Set up monitoring for:
- Failed login attempts
- Unusual API usage patterns
- Database connection failures
- High error rates
- Suspicious wallet transactions

### Compliance

This application handles:
- Personal data (GDPR compliance required)
- Financial transactions (PCI DSS considerations)
- User authentication data

Ensure compliance with relevant regulations in your jurisdiction.

---

**Remember**: Security is not a one-time setup but an ongoing process. Regularly review and update security measures.