-- ============================================================
-- SAMPLE DATA FOR MENTORX PLATFORM
-- ============================================================

-- Insert sample roles (if not exists)
INSERT INTO roles (id, role_name, description) VALUES
(uuid_generate_v4(), 'ADMIN', 'Full system access and control'),
(uuid_generate_v4(), 'MODERATOR', 'Content moderation and user management'),
(uuid_generate_v4(), 'USER', 'Standard platform user'),
(uuid_generate_v4(), 'MENTOR', 'Approved mentor with course and job posting rights')
ON CONFLICT (role_name) DO NOTHING;

-- Insert sample permissions
INSERT INTO permissions (permission_key, description) VALUES
('user:view:any', 'View any user profile'),
('user:ban', 'Ban a user'),
('user:suspend', 'Suspend a user account'),
('user:delete', 'Delete a user account'),
('user:change_role', 'Change user roles'),
('mentor:approve', 'Approve mentor applications'),
('mentor:reject', 'Reject mentor applications'),
('job:view:any', 'View any job'),
('job:force_close', 'Force close a job'),
('course:approve', 'Approve a course for publishing'),
('course:reject', 'Reject a course'),
('payment:view:all', 'View all transactions'),
('payment:approve_withdraw', 'Approve withdrawal requests'),
('dispute:review', 'Review disputes'),
('dispute:resolve', 'Resolve disputes and force payouts'),
('report:review', 'Review user reports'),
('settings:view', 'View platform settings'),
('settings:edit', 'Edit platform settings')
ON CONFLICT (permission_key) DO NOTHING;

-- Assign all permissions to ADMIN
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Assign moderator permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.role_name = 'MODERATOR' AND p.permission_key IN (
    'user:view:any', 'user:suspend', 'mentor:approve', 'mentor:reject',
    'job:view:any', 'job:force_close', 'course:approve', 'course:reject',
    'dispute:review', 'report:review'
)
ON CONFLICT DO NOTHING;

-- Insert platform settings
INSERT INTO platform_settings (key, value, description) VALUES
('platform_fee_percent', '10.00', 'Platform commission % deducted from each job payment'),
('withdrawal_fee_percent', '2.00', 'Fee % charged on each withdrawal'),
('min_withdrawal_mxc', '100.00', 'Minimum MXC amount to request withdrawal'),
('max_withdrawal_mxc_per_day', '10000.00', 'Maximum MXC withdrawable per day per user'),
('auto_approve_withdrawal_mxc', '500.00', 'Withdrawals below this amount auto-approve'),
('mxc_to_vnd_rate', '1000.00', 'Conversion: 1 MXC = X VND'),
('mxc_to_usd_rate', '0.04', 'Conversion: 1 MXC = X USD'),
('new_user_bonus_mxc', '50.00', 'MXC gifted to new users on first email verification'),
('max_proposals_per_job', '20', 'Max proposals a job can receive')
ON CONFLICT (key) DO NOTHING;

-- Insert sample skills
INSERT INTO skills (slug, label_vi, label_en, label_zh, label_ja) VALUES
('java', 'Java', 'Java', 'Java编程', 'Java開発'),
('spring-boot', 'Spring Boot', 'Spring Boot', 'Spring Boot', 'Spring Boot'),
('python', 'Python', 'Python', 'Python编程', 'Python開発'),
('react', 'React.js', 'React.js', 'React.js', 'React.js'),
('nodejs', 'Node.js', 'Node.js', 'Node.js', 'Node.js'),
('javascript', 'JavaScript', 'JavaScript', 'JavaScript', 'JavaScript'),
('sql', 'SQL / Cơ sở dữ liệu', 'SQL / Databases', 'SQL数据库', 'SQLデータベース'),
('ui-ux', 'Thiết kế UI/UX', 'UI/UX Design', 'UI/UX设计', 'UI/UXデザイン'),
('design', 'Thiết kế', 'Design', '设计', 'デザイン'),
('data-science', 'Khoa học dữ liệu', 'Data Science', '数据科学', 'データサイエンス'),
('machine-learning', 'Machine Learning', 'Machine Learning', '机器学习', '機械学習'),
('devops', 'DevOps', 'DevOps', 'DevOps', 'DevOps'),
('docker', 'Docker', 'Docker', 'Docker', 'Docker'),
('kubernetes', 'Kubernetes', 'Kubernetes', 'Kubernetes', 'Kubernetes'),
('blockchain', 'Blockchain', 'Blockchain', '区块链', 'ブロックチェーン'),
('mobile', 'Lập trình Mobile', 'Mobile Development', '移动开发', 'モバイル開発'),
('business', 'Kinh doanh', 'Business', '商业', 'ビジネス'),
('marketing', 'Marketing', 'Marketing', '营销', 'マーケティング'),
('finance', 'Tài chính', 'Finance', '金融', 'ファイナンス')
ON CONFLICT (slug) DO NOTHING;

-- Insert sample categories
INSERT INTO categories (slug, label_vi, label_en, label_zh, label_ja, display_order) VALUES
('software-dev', 'Lập trình phần mềm', 'Software Development', '软件开发', 'ソフトウェア開発', 1),
('data-ai', 'Dữ liệu & AI', 'Data & AI', '数据与AI', 'データ＆AI', 2),
('design', 'Thiết kế', 'Design', '设计', 'デザイン', 3),
('business-finance', 'Kinh doanh & Tài chính', 'Business & Finance', '商业与金融', 'ビジネス＆金融', 4),
('marketing-seo', 'Marketing & SEO', 'Marketing & SEO', '营销与SEO', 'マーケティング', 5),
('language', 'Ngôn ngữ', 'Language', '语言', '言語', 6),
('personal-dev', 'Phát triển bản thân', 'Personal Development', '个人发展', '自己開発', 7)
ON CONFLICT (slug) DO NOTHING;

-- Insert system wallets
INSERT INTO wallets (account_type, balance_mxc) VALUES
('PLATFORM_REVENUE', 0),
('PLATFORM_FLOAT', 0),
('ESCROW', 0)
ON CONFLICT (user_id, account_type) DO NOTHING;

-- Insert sample admin user
INSERT INTO users (
    email, password_hash, full_name, display_name, avatar_url, bio, 
    country_code, preferred_language, status, is_email_verified, 
    is_mentor, mentor_status, profile_is_public
) VALUES (
    'admin@mentorx.demo', 
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', -- password
    'System Administrator', 
    'Admin', 
    'https://ui-avatars.com/api/?name=Admin&background=0d47a1&color=fff',
    'System administrator for MentorX platform',
    'VN', 'vi', 'ACTIVE', true, false, 'NONE', true
) ON CONFLICT (email) DO NOTHING;

-- Insert sample mentors
INSERT INTO users (
    email, password_hash, full_name, display_name, avatar_url, bio, 
    country_code, preferred_language, status, is_email_verified, 
    is_mentor, mentor_status, profile_is_public
) VALUES 
('mentor1@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Nguyễn Văn An', 'An Nguyen', 'https://ui-avatars.com/api/?name=An+Nguyen&background=random',
 'Senior Java Developer với 8 năm kinh nghiệm', 'VN', 'vi', 'ACTIVE', true, true, 'APPROVED', true),
('mentor2@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Trần Thị Bình', 'Binh Tran', 'https://ui-avatars.com/api/?name=Binh+Tran&background=random',
 'UI/UX Designer chuyên nghiệp', 'VN', 'vi', 'ACTIVE', true, true, 'APPROVED', true),
('mentor3@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Lê Minh Cường', 'Cuong Le', 'https://ui-avatars.com/api/?name=Cuong+Le&background=random',
 'Data Scientist và Machine Learning Engineer', 'VN', 'vi', 'ACTIVE', true, true, 'APPROVED', true),
('mentor4@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Phạm Thu Dung', 'Dung Pham', 'https://ui-avatars.com/api/?name=Dung+Pham&background=random',
 'Full-stack Developer (React + Node.js)', 'VN', 'vi', 'ACTIVE', true, true, 'APPROVED', true),
('mentor5@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Hoàng Văn Em', 'Em Hoang', 'https://ui-avatars.com/api/?name=Em+Hoang&background=random',
 'DevOps Engineer và Cloud Architect', 'VN', 'vi', 'ACTIVE', true, true, 'APPROVED', true)
ON CONFLICT (email) DO NOTHING;

-- Insert sample clients
INSERT INTO users (
    email, password_hash, full_name, display_name, avatar_url, bio, 
    country_code, preferred_language, status, is_email_verified, 
    is_mentor, mentor_status, profile_is_public
) VALUES 
('client1@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Công ty ABC', 'ABC Company', 'https://ui-avatars.com/api/?name=ABC+Company&background=random',
 'Sample client for testing purposes', 'VN', 'vi', 'ACTIVE', true, false, 'NONE', true),
('client2@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Startup XYZ', 'XYZ Startup', 'https://ui-avatars.com/api/?name=XYZ+Startup&background=random',
 'Sample client for testing purposes', 'VN', 'vi', 'ACTIVE', true, false, 'NONE', true),
('client3@mentorx.demo', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 
 'Doanh nghiệp DEF', 'DEF Enterprise', 'https://ui-avatars.com/api/?name=DEF+Enterprise&background=random',
 'Sample client for testing purposes', 'VN', 'vi', 'ACTIVE', true, false, 'NONE', true)
ON CONFLICT (email) DO NOTHING;

-- Assign roles to users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email = 'admin@mentorx.demo' AND r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email LIKE 'mentor%@mentorx.demo' AND r.role_name = 'MENTOR'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE u.email LIKE '%@mentorx.demo' AND r.role_name = 'USER'
ON CONFLICT DO NOTHING;

-- Create wallets for demo users
INSERT INTO wallets (user_id, account_type, balance_mxc)
SELECT u.id, 'USER_AVAILABLE', 1000.0000
FROM users u 
WHERE u.email LIKE '%@mentorx.demo'
ON CONFLICT (user_id, account_type) DO NOTHING;

INSERT INTO wallets (user_id, account_type, balance_mxc)
SELECT u.id, 'USER_PENDING', 0.0000
FROM users u 
WHERE u.email LIKE '%@mentorx.demo'
ON CONFLICT (user_id, account_type) DO NOTHING;

-- Create notification preferences for demo users
INSERT INTO notification_preferences (user_id)
SELECT id FROM users WHERE email LIKE '%@mentorx.demo'
ON CONFLICT (user_id) DO NOTHING;