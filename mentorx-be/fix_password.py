import sys

path = 'src/main/java/com/mentorx/api/auth/service/serviceImpl/AuthServiceImpl.java'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('.password(user.getPasswordHash())', '.password(user.getPasswordHash() != null ? user.getPasswordHash() : "OAUTH2_USER")')

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
