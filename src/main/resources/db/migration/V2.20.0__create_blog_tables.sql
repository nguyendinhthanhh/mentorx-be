CREATE TYPE blog_category AS ENUM (
    'CAREER_GROWTH',
    'MENTORING',
    'COURSES',
    'FREELANCE_JOBS',
    'TECHNOLOGY',
    'PLATFORM_SAFETY'
);

CREATE TYPE blog_audience AS ENUM (
    'FOR_LEARNERS',
    'FOR_MENTORS',
    'CAREER_GROWTH',
    'FREELANCE_JOBS',
    'COURSES',
    'PLATFORM_SAFETY'
);

CREATE TABLE blog_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug VARCHAR(300) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    excerpt TEXT,
    category blog_category NOT NULL,
    audience blog_audience NOT NULL,
    author VARCHAR(100),
    author_role VARCHAR(100),
    author_avatar VARCHAR(500),
    cover_image VARCHAR(500),
    content TEXT,
    read_time VARCHAR(50),
    featured BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE blog_post_tags (
    blog_post_id UUID NOT NULL REFERENCES blog_posts(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (blog_post_id, tag)
);

CREATE INDEX idx_blog_posts_category ON blog_posts(category);
CREATE INDEX idx_blog_posts_audience ON blog_posts(audience);
CREATE INDEX idx_blog_posts_featured ON blog_posts(featured);