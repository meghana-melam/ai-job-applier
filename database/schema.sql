-- AI Job Applier Database Schema
-- Database: MySQL

-- Jobs Table
CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    company VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    job_url VARCHAR(500) UNIQUE,
    source_platform VARCHAR(50),
    experience_level VARCHAR(100),
    employment_type VARCHAR(50),
    match_score DOUBLE,
    is_easy_apply BOOLEAN DEFAULT FALSE,
    status VARCHAR(50) DEFAULT 'NEW',
    posted_date DATETIME,
    scraped_date DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Job Skills Table (Many-to-Many)
CREATE TABLE IF NOT EXISTS job_skills (
    job_id BIGINT,
    skill VARCHAR(100),
    PRIMARY KEY (job_id, skill),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
);

-- Resumes Table
CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    file_path VARCHAR(500),
    years_of_experience INT,
    summary TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Resume Skills Table
CREATE TABLE IF NOT EXISTS resume_skills (
    resume_id BIGINT,
    skill VARCHAR(100),
    PRIMARY KEY (resume_id, skill),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

-- Resume Education Table
CREATE TABLE IF NOT EXISTS resume_education (
    resume_id BIGINT,
    degree TEXT,
    PRIMARY KEY (resume_id, degree(255)),
    FOREIGN KEY (resume_id) REFERENCES resumes(id) ON DELETE CASCADE
);

-- Applications Table
CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id BIGINT,
    resume_id BIGINT,
    cover_letter TEXT,
    status VARCHAR(50) DEFAULT 'PENDING',
    applied_date DATETIME,
    error_message TEXT,
    response_received BOOLEAN DEFAULT FALSE,
    response_date DATETIME,
    response_details TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (resume_id) REFERENCES resumes(id)
);

-- Indexes for better performance
CREATE INDEX idx_jobs_status ON jobs(status);
CREATE INDEX idx_jobs_platform ON jobs(source_platform);
CREATE INDEX idx_jobs_match_score ON jobs(match_score DESC);
CREATE INDEX idx_jobs_scraped_date ON jobs(scraped_date DESC);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_date ON applications(applied_date DESC);
CREATE INDEX idx_applications_job_id ON applications(job_id);

-- Note: MySQL handles auto-updating of updated_at via ON UPDATE CURRENT_TIMESTAMP
-- No additional triggers needed for timestamp updates

-- Sample data (optional)
-- INSERT INTO resumes (name, email, phone, years_of_experience, summary, is_active)
-- VALUES ('Meghana', 'meghana@example.com', '+91-1234567890', 3, 
--         'Experienced software developer with expertise in Java and Spring Boot', TRUE);
