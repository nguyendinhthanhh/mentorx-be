-- Create mentor_packages table
CREATE TABLE mentor_packages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_profile_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    package_type VARCHAR(50) NOT NULL,
    duration_hours INTEGER NOT NULL,
    price_mxc DECIMAL(10,2) NOT NULL,
    features TEXT[],
    is_active BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_packages_profile FOREIGN KEY (mentor_profile_id) 
        REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT check_duration_positive CHECK (duration_hours > 0),
    CONSTRAINT check_price_positive CHECK (price_mxc >= 0)
);

CREATE INDEX idx_mentor_packages_mentor ON mentor_packages(mentor_profile_id);
CREATE INDEX idx_mentor_packages_active ON mentor_packages(is_active);
CREATE INDEX idx_mentor_packages_display_order ON mentor_packages(display_order);

-- Create mentor_courses table
CREATE TABLE mentor_courses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_profile_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price_mxc DECIMAL(10,2) NOT NULL,
    duration_hours INTEGER,
    level VARCHAR(50),
    lessons_count INTEGER DEFAULT 0,
    thumbnail_url TEXT,
    status VARCHAR(50) DEFAULT 'DRAFT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_courses_profile FOREIGN KEY (mentor_profile_id) 
        REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT check_course_price_positive CHECK (price_mxc >= 0),
    CONSTRAINT check_lessons_count_positive CHECK (lessons_count >= 0)
);

CREATE INDEX idx_mentor_courses_mentor ON mentor_courses(mentor_profile_id);
CREATE INDEX idx_mentor_courses_status ON mentor_courses(status);
CREATE INDEX idx_mentor_courses_level ON mentor_courses(level);

-- Create mentor_availability table
CREATE TABLE mentor_availability (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_profile_id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_availability_profile FOREIGN KEY (mentor_profile_id) 
        REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT check_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT check_time_order CHECK (start_time < end_time)
);

CREATE INDEX idx_mentor_availability_mentor ON mentor_availability(mentor_profile_id);
CREATE INDEX idx_mentor_availability_day ON mentor_availability(day_of_week);
CREATE INDEX idx_mentor_availability_active ON mentor_availability(is_active);

-- Create mentor_blocked_dates table
CREATE TABLE mentor_blocked_dates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    mentor_profile_id UUID NOT NULL,
    blocked_date DATE NOT NULL,
    reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mentor_blocked_dates_profile FOREIGN KEY (mentor_profile_id) 
        REFERENCES mentor_profiles(id) ON DELETE CASCADE,
    CONSTRAINT unique_mentor_blocked_date UNIQUE (mentor_profile_id, blocked_date)
);

CREATE INDEX idx_mentor_blocked_dates_mentor ON mentor_blocked_dates(mentor_profile_id);
CREATE INDEX idx_mentor_blocked_dates_date ON mentor_blocked_dates(blocked_date);

-- Add comments for documentation
COMMENT ON TABLE mentor_packages IS 'Mentoring service packages offered by mentors';
COMMENT ON TABLE mentor_courses IS 'Courses created and offered by mentors';
COMMENT ON TABLE mentor_availability IS 'Weekly recurring availability schedule for mentors';
COMMENT ON TABLE mentor_blocked_dates IS 'Specific dates when mentor is unavailable';

COMMENT ON COLUMN mentor_packages.package_type IS 'Type: SINGLE_SESSION, PACKAGE_DEAL, or SUBSCRIPTION';
COMMENT ON COLUMN mentor_packages.features IS 'Array of feature strings describing what is included';
COMMENT ON COLUMN mentor_courses.status IS 'Status: DRAFT, PUBLISHED, or ARCHIVED';
COMMENT ON COLUMN mentor_courses.level IS 'Difficulty level: BEGINNER, INTERMEDIATE, or ADVANCED';
COMMENT ON COLUMN mentor_availability.day_of_week IS 'Day of week: 1=Monday, 2=Tuesday, ..., 7=Sunday';
