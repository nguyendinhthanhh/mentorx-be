# Troubleshooting Guide - MentorX

## 🔧 Common Issues and Solutions

### 1. ERR_CONNECTION_REFUSED - Backend Not Running

**Error:**
```
Failed to load resource: net::ERR_CONNECTION_REFUSED
:8080/api/...
```

**Cause:** Backend server is not running

**Solution:**
```bash
cd mentorx-be
./mvnw spring-boot:run
```

**Verify backend is running:**
```bash
# Windows
netstat -ano | findstr :8080

# Mac/Linux
lsof -i :8080
```

Should see output like:
```
TCP    0.0.0.0:8080    0.0.0.0:0    LISTENING    14740
```

---

### 2. Frontend Not Connecting to Backend

**Error:**
```
Network Error
AxiosError: Network Error
```

**Cause:** Frontend environment variable not set or dev server not restarted

**Solution:**

1. **Check `.env` file exists:**
```bash
cd mentorx-fe
cat .env
```

Should contain:
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_APP_NAME=MentorX
```

2. **Restart frontend dev server:**
```bash
# Stop current server (Ctrl+C)
npm run dev
```

3. **Clear browser cache:**
- Open DevTools (F12)
- Right-click refresh button
- Select "Empty Cache and Hard Reload"

---

### 3. KYC Submit 403 Forbidden

**Error:**
```
AxiosError: Request failed with status code 403
at async Object.submitKyc (kycApi.ts:17:22)
```

**Causes:**
1. Not logged in
2. Token expired
3. API response not unwrapped correctly

**Solutions:**

**A. Check if logged in:**
- Open DevTools → Application → Local Storage
- Check if `auth-storage` exists with valid token

**B. Re-login:**
- Go to `/login`
- Login again
- Try KYC submission

**C. Check API response format:**
Backend returns:
```json
{
  "success": true,
  "data": { ... },
  "timestamp": "..."
}
```

Frontend should access `response.data.data`, not just `response.data`

**Fixed in:** `kycApi.ts`
```typescript
return response.data.data || response.data
```

---

### 4. JSON Parse Error - "undefined" is not valid JSON

**Error:**
```
Uncaught (in promise) SyntaxError: "undefined" is not valid JSON
at JSON.parse (<anonymous>)
```

**Cause:** Trying to parse undefined or null value as JSON

**Common locations:**
- LocalStorage parsing
- API response parsing
- State initialization

**Solution:**

**Check localStorage:**
```typescript
// BAD
const data = JSON.parse(localStorage.getItem('key'))

// GOOD
const stored = localStorage.getItem('key')
const data = stored ? JSON.parse(stored) : null
```

**Check API response:**
```typescript
// BAD
return response.data

// GOOD
return response.data?.data || response.data
```

---

### 5. CORS Errors

**Error:**
```
Access to XMLHttpRequest at 'http://localhost:8080/api/...' 
from origin 'http://localhost:3000' has been blocked by CORS policy
```

**Cause:** Backend CORS not configured for frontend origin

**Solution:**

**Check backend `application.yml`:**
```yaml
app:
  cors:
    allowed-origins: http://localhost:3000,http://localhost:3001
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    allowed-headers: "*"
    allow-credentials: true
```

**Or check `.env`:**
```env
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
```

**Restart backend after changes!**

---

### 6. Video Recording Not Stopping

**Error:** Video records indefinitely, never stops

**Cause:** Countdown and recording logic conflict

**Solution:** Already fixed in `KycCamera.tsx`

**Flow:**
1. Countdown 3...2...1... (preparation)
2. Start recording
3. Auto-stop after 3 seconds

**Verify fix:**
- Go to `/profile` → Identity Verification
- Click "Bắt đầu ghi hình"
- Should see countdown, then recording, then auto-stop

---

### 7. VNPay "Sai chữ ký" (Invalid Signature)

**Error:**
```
Thông báo: Sai chữ ký
```

**Cause:** Hash data was URL encoded (should be raw values)

**Solution:** Already fixed in `VNPayServiceImpl.java`

**Correct approach:**
- Hash data: Raw values (no encoding)
- Query string: URL encoded

**Verify fix:**
- Go to `/wallet` → Deposit
- Create payment
- Should redirect to VNPay successfully

---

### 8. Port Already in Use

**Error:**
```
Port 8080 is already in use
```

**Solution:**

**Windows:**
```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

**Mac/Linux:**
```bash
# Find process
lsof -i :8080

# Kill process
kill -9 <PID>
```

---

### 9. Database Connection Failed

**Error:**
```
Connection refused: connect
Could not connect to database
```

**Cause:** PostgreSQL not running

**Solution:**

**Check if PostgreSQL is running:**
```bash
# Windows
sc query postgresql-x64-14

# Mac
brew services list | grep postgresql

# Linux
sudo systemctl status postgresql
```

**Start PostgreSQL:**
```bash
# Windows
net start postgresql-x64-14

# Mac
brew services start postgresql

# Linux
sudo systemctl start postgresql
```

**Check connection:**
```bash
psql -U postgres -d mentorx_db
```

---

### 10. Build Errors

**Error:**
```
BUILD FAILURE
Compilation error
```

**Solution:**

**Backend:**
```bash
cd mentorx-be
./mvnw clean install -DskipTests
```

**Frontend:**
```bash
cd mentorx-fe
rm -rf node_modules
npm install
npm run build
```

---

## 🚀 Quick Start Checklist

Before starting development, ensure:

- [ ] PostgreSQL is running
- [ ] Backend `.env` file exists with correct values
- [ ] Frontend `.env` file exists with correct values
- [ ] Backend is running on port 8080
- [ ] Frontend is running on port 3000
- [ ] No CORS errors in browser console
- [ ] Can login successfully
- [ ] API calls return data (check Network tab)

---

## 📝 Debugging Tips

### Check Backend Logs

```bash
cd mentorx-be
tail -f logs/mentorx-api.log
```

### Check Frontend Console

- Open DevTools (F12)
- Go to Console tab
- Look for errors (red text)
- Check Network tab for failed requests

### Check API Responses

- Open DevTools → Network tab
- Click on failed request
- Check:
  - Request URL
  - Request Headers (Authorization token?)
  - Response Status
  - Response Body

### Test API Directly

Use curl or Postman:

```bash
# Test backend health
curl http://localhost:8080/actuator/health

# Test login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password"}'

# Test authenticated endpoint
curl http://localhost:8080/api/v1/wallet/user/{userId} \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 🆘 Still Having Issues?

1. **Check logs:**
   - Backend: `mentorx-be/logs/mentorx-api.log`
   - Frontend: Browser DevTools Console

2. **Restart everything:**
   ```bash
   # Stop all servers
   # Restart PostgreSQL
   # Restart backend
   # Restart frontend
   ```

3. **Clear all caches:**
   - Browser cache
   - LocalStorage
   - npm cache: `npm cache clean --force`
   - Maven cache: `./mvnw clean`

4. **Check versions:**
   - Java 21
   - Node.js 18+
   - PostgreSQL 14+

5. **Review recent changes:**
   - Check git log
   - Revert if needed

---

## 📚 Useful Commands

```bash
# Backend
cd mentorx-be
./mvnw spring-boot:run          # Start backend
./mvnw clean compile            # Compile only
./mvnw test                     # Run tests
./mvnw clean install            # Full build

# Frontend
cd mentorx-fe
npm run dev                     # Start dev server
npm run build                   # Build for production
npm run preview                 # Preview production build
npm run lint                    # Lint code

# Database
psql -U postgres -d mentorx_db  # Connect to database
\dt                             # List tables
\d table_name                   # Describe table
```

---

**Last Updated:** May 11, 2026
**Version:** 1.0.0
