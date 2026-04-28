-- Initialize MentorX Database
-- This script runs when PostgreSQL container starts for the first time

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Create indexes for better performance
-- These will be created by Hibernate, but we can add custom ones here

-- Example: Create index for full-text search on users
-- CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_fulltext 
-- ON users USING gin(to_tsvector('english', full_name || ' ' || COALESCE(display_name, '')));

-- Insert default roles
INSERT INTO roles (id, role_name, description, created_at, updated_at) 
VALUES 
    (1, 'USER', 'Regular user role', NOW(), NOW()),
    (2, 'MENTOR', 'Mentor role with additional permissions', NOW(), NOW()),
    (3, 'ADMIN', 'Administrator role with full permissions', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Insert default admin user (password: admin123)
-- Password hash for 'admin123' using BCrypt
INSERT INTO users (
    id, email, password_hash, full_name, display_name, 
    status, is_email_verified, is_mentor, mentor_status,
    created_at, updated_at
) VALUES (
    uuid_generate_v4(),
    'admin@mentorx.com',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM5lE7u8LZKpP6pqULHu',
    'System Administrator',
    'Admin',
    'ACTIVE',
    true,
    false,
    'NONE',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id, granted_at, granted_by)
SELECT u.id, 3, NOW(), u.id
FROM users u 
WHERE u.email = 'admin@mentorx.com'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur 
    WHERE ur.user_id = u.id AND ur.role_id = 3
);

-- Create sample categories for jobs
-- This can be expanded based on your business requirements
CREATE TABLE IF NOT EXISTS job_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO job_categories (name, description) VALUES
    ('Web Development', 'Frontend and backend web development projects'),
    ('Mobile Development', 'iOS and Android mobile application development'),
    ('Data Science', 'Data analysis, machine learning, and AI projects'),
    ('DevOps', 'Infrastructure, deployment, and system administration'),
    ('UI/UX Design', 'User interface and user experience design'),
    ('Digital Marketing', 'SEO, social media, and online marketing'),
    ('Content Writing', 'Blog posts, articles, and copywriting'),
    ('Consulting', 'Business and technical consulting services')
ON CONFLICT (name) DO NOTHING;

-- Create sample skills
CREATE TABLE IF NOT EXISTS skills (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT NOW()
);

INSERT INTO skills (name, category) VALUES
    ('Java', 'Programming'),
    ('Spring Boot', 'Framework'),
    ('React', 'Frontend'),
    ('Node.js', 'Backend'),
    ('PostgreSQL', 'Database'),
    ('Docker', 'DevOps'),
    ('AWS', 'Cloud'),
    ('Python', 'Programming'),
    ('JavaScript', 'Programming'),
    ('TypeScript', 'Programming')
ON CONFLICT (name) DO NOTHING;

-- Add any other initialization data here
COMMIT;