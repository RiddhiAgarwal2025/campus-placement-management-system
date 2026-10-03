-- 02: Tables with primary keys, NOT NULL, UNIQUE, DEFAULT and single-row CHECK constraints
USE campus_placement;

CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(40)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           ENUM('OFFICER','STUDENT') NOT NULL,
    display_name   VARCHAR(100) NOT NULL,
    is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login     TIMESTAMP    NULL,
    CONSTRAINT uq_users_username UNIQUE (username)
);

CREATE TABLE departments (
    dept_id    INT AUTO_INCREMENT PRIMARY KEY,
    dept_code  VARCHAR(10)  NOT NULL,
    dept_name  VARCHAR(100) NOT NULL,
    CONSTRAINT uq_departments_code UNIQUE (dept_code),
    CONSTRAINT uq_departments_name UNIQUE (dept_name)
);

CREATE TABLE students (
    student_id       VARCHAR(20)  PRIMARY KEY,
    user_id          INT          NULL,
    full_name        VARCHAR(100) NOT NULL,
    email            VARCHAR(120) NOT NULL,
    phone            VARCHAR(15)  NULL,
    dept_id          INT          NOT NULL,
    graduation_year  SMALLINT     NOT NULL,
    cgpa             DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    backlogs         TINYINT UNSIGNED NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_students_email UNIQUE (email),
    CONSTRAINT uq_students_user  UNIQUE (user_id),
    CONSTRAINT chk_students_cgpa CHECK (cgpa BETWEEN 0 AND 10),
    CONSTRAINT chk_students_backlogs CHECK (backlogs >= 0 AND backlogs <= 60),
    CONSTRAINT chk_students_gradyear CHECK (graduation_year BETWEEN 2000 AND 2100),
    CONSTRAINT chk_students_email CHECK (email LIKE '%_@_%._%')
);

CREATE TABLE academic_records (
    record_id         INT AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(20)  NOT NULL,
    semester          TINYINT      NOT NULL,
    sgpa              DECIMAL(4,2) NOT NULL,
    marks_percentage  DECIMAL(5,2) NOT NULL,
    CONSTRAINT uq_academic_student_sem UNIQUE (student_id, semester),
    CONSTRAINT chk_academic_semester CHECK (semester BETWEEN 1 AND 10),
    CONSTRAINT chk_academic_sgpa  CHECK (sgpa BETWEEN 0 AND 10),
    CONSTRAINT chk_academic_marks CHECK (marks_percentage BETWEEN 0 AND 100)
);

CREATE TABLE skills (
    skill_id    INT AUTO_INCREMENT PRIMARY KEY,
    skill_name  VARCHAR(60) NOT NULL,
    category    VARCHAR(40) NOT NULL DEFAULT 'General',
    CONSTRAINT uq_skills_name UNIQUE (skill_name)
);

CREATE TABLE student_skills (
    student_id   VARCHAR(20) NOT NULL,
    skill_id     INT         NOT NULL,
    proficiency  ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL DEFAULT 'INTERMEDIATE',
    PRIMARY KEY (student_id, skill_id)
);

CREATE TABLE companies (
    company_id     INT AUTO_INCREMENT PRIMARY KEY,
    company_name   VARCHAR(100) NOT NULL,
    industry       VARCHAR(60)  NOT NULL,
    website        VARCHAR(150) NULL,
    contact_person VARCHAR(100) NULL,
    contact_email  VARCHAR(120) NULL,
    contact_phone  VARCHAR(15)  NULL,
    CONSTRAINT uq_companies_name UNIQUE (company_name)
);

CREATE TABLE job_profiles (
    job_id       INT AUTO_INCREMENT PRIMARY KEY,
    company_id   INT           NOT NULL,
    position     VARCHAR(100)  NOT NULL,
    description  VARCHAR(1000) NULL,
    package_lpa  DECIMAL(6,2)  NOT NULL,
    location     VARCHAR(100)  NOT NULL,
    CONSTRAINT uq_job_company_position UNIQUE (company_id, position),
    CONSTRAINT chk_job_package CHECK (package_lpa > 0 AND package_lpa <= 500)
);

CREATE TABLE job_profile_skills (
    job_id    INT NOT NULL,
    skill_id  INT NOT NULL,
    PRIMARY KEY (job_id, skill_id)
);

CREATE TABLE placement_drives (
    drive_id              INT AUTO_INCREMENT PRIMARY KEY,
    job_id                INT          NOT NULL,
    drive_date            DATE         NOT NULL,
    application_deadline  DATE         NOT NULL,
    venue                 VARCHAR(100) NOT NULL DEFAULT 'Placement Cell',
    status                ENUM('UPCOMING','OPEN','CLOSED','COMPLETED') NOT NULL DEFAULT 'UPCOMING',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_drive_dates CHECK (application_deadline <= drive_date)
);

CREATE TABLE eligibility_criteria (
    criteria_id      INT AUTO_INCREMENT PRIMARY KEY,
    drive_id         INT          NOT NULL,
    min_cgpa         DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    max_backlogs     TINYINT UNSIGNED NOT NULL DEFAULT 0,
    graduation_year  SMALLINT     NULL,
    CONSTRAINT uq_criteria_drive UNIQUE (drive_id),
    CONSTRAINT chk_criteria_cgpa CHECK (min_cgpa BETWEEN 0 AND 10),
    CONSTRAINT chk_criteria_gradyear CHECK (graduation_year IS NULL OR graduation_year BETWEEN 2000 AND 2100)
);

CREATE TABLE eligibility_departments (
    criteria_id  INT NOT NULL,
    dept_id      INT NOT NULL,
    PRIMARY KEY (criteria_id, dept_id)
);

CREATE TABLE eligibility_skills (
    criteria_id  INT NOT NULL,
    skill_id     INT NOT NULL,
    PRIMARY KEY (criteria_id, skill_id)
);

CREATE TABLE applications (
    application_id  INT AUTO_INCREMENT PRIMARY KEY,
    student_id      VARCHAR(20) NOT NULL,
    drive_id        INT         NOT NULL,
    applied_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          ENUM('APPLIED','SHORTLISTED','SELECTED','REJECTED') NOT NULL DEFAULT 'APPLIED',
    updated_at      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_application_student_drive UNIQUE (student_id, drive_id)
);

CREATE TABLE selection_rounds (
    round_id     INT AUTO_INCREMENT PRIMARY KEY,
    drive_id     INT          NOT NULL,
    round_name   VARCHAR(80)  NOT NULL,
    sequence_no  TINYINT      NOT NULL,
    round_date   DATE         NULL,
    status       ENUM('UPCOMING','IN_PROGRESS','COMPLETED') NOT NULL DEFAULT 'UPCOMING',
    CONSTRAINT uq_round_drive_sequence UNIQUE (drive_id, sequence_no),
    CONSTRAINT chk_round_sequence CHECK (sequence_no BETWEEN 1 AND 20)
);

CREATE TABLE round_results (
    result_id       INT AUTO_INCREMENT PRIMARY KEY,
    round_id        INT          NOT NULL,
    application_id  INT          NOT NULL,
    result          ENUM('PASS','FAIL') NOT NULL,
    remarks         VARCHAR(255) NULL,
    recorded_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_result_round_application UNIQUE (round_id, application_id)
);

CREATE TABLE offers (
    offer_id        INT AUTO_INCREMENT PRIMARY KEY,
    application_id  INT          NOT NULL,
    package_lpa     DECIMAL(6,2) NOT NULL,
    offer_date      DATE         NOT NULL,
    joining_date    DATE         NOT NULL,
    status          ENUM('PENDING','ACCEPTED','REJECTED') NOT NULL DEFAULT 'PENDING',
    responded_at    TIMESTAMP    NULL,
    CONSTRAINT uq_offer_application UNIQUE (application_id),
    CONSTRAINT chk_offer_package CHECK (package_lpa > 0 AND package_lpa <= 500),
    CONSTRAINT chk_offer_dates CHECK (joining_date >= offer_date),
    CONSTRAINT chk_offer_response CHECK ((status = 'PENDING' AND responded_at IS NULL) OR (status <> 'PENDING' AND responded_at IS NOT NULL))
);
