-- Campus Placement and Recruitment Drive Management System
-- 01: Database creation (MySQL 8.0+)
DROP DATABASE IF EXISTS campus_placement;
CREATE DATABASE campus_placement CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE campus_placement;
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
-- 03: Foreign keys and secondary indexes
USE campus_placement;

ALTER TABLE students
    ADD CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_students_dept FOREIGN KEY (dept_id) REFERENCES departments(dept_id) ON DELETE RESTRICT;

ALTER TABLE academic_records
    ADD CONSTRAINT fk_academic_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE student_skills
    ADD CONSTRAINT fk_ss_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_ss_skill   FOREIGN KEY (skill_id)   REFERENCES skills(skill_id)     ON DELETE RESTRICT;

ALTER TABLE job_profiles
    ADD CONSTRAINT fk_job_company FOREIGN KEY (company_id) REFERENCES companies(company_id) ON DELETE RESTRICT;

ALTER TABLE job_profile_skills
    ADD CONSTRAINT fk_jps_job   FOREIGN KEY (job_id)   REFERENCES job_profiles(job_id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_jps_skill FOREIGN KEY (skill_id) REFERENCES skills(skill_id)     ON DELETE RESTRICT;

ALTER TABLE placement_drives
    ADD CONSTRAINT fk_drive_job FOREIGN KEY (job_id) REFERENCES job_profiles(job_id) ON DELETE RESTRICT;

ALTER TABLE eligibility_criteria
    ADD CONSTRAINT fk_criteria_drive FOREIGN KEY (drive_id) REFERENCES placement_drives(drive_id) ON DELETE CASCADE;

ALTER TABLE eligibility_departments
    ADD CONSTRAINT fk_ed_criteria FOREIGN KEY (criteria_id) REFERENCES eligibility_criteria(criteria_id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_ed_dept     FOREIGN KEY (dept_id)     REFERENCES departments(dept_id)              ON DELETE RESTRICT;

ALTER TABLE eligibility_skills
    ADD CONSTRAINT fk_es_criteria FOREIGN KEY (criteria_id) REFERENCES eligibility_criteria(criteria_id) ON DELETE CASCADE,
    ADD CONSTRAINT fk_es_skill    FOREIGN KEY (skill_id)    REFERENCES skills(skill_id)                  ON DELETE RESTRICT;

ALTER TABLE applications
    ADD CONSTRAINT fk_app_student FOREIGN KEY (student_id) REFERENCES students(student_id)       ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_app_drive   FOREIGN KEY (drive_id)   REFERENCES placement_drives(drive_id) ON DELETE RESTRICT;

ALTER TABLE selection_rounds
    ADD CONSTRAINT fk_round_drive FOREIGN KEY (drive_id) REFERENCES placement_drives(drive_id) ON DELETE RESTRICT;

ALTER TABLE round_results
    ADD CONSTRAINT fk_result_round FOREIGN KEY (round_id)       REFERENCES selection_rounds(round_id)   ON DELETE RESTRICT,
    ADD CONSTRAINT fk_result_app   FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE CASCADE;

ALTER TABLE offers
    ADD CONSTRAINT fk_offer_app FOREIGN KEY (application_id) REFERENCES applications(application_id) ON DELETE RESTRICT;

-- Secondary indexes for frequent filters and joins
CREATE INDEX idx_students_dept      ON students(dept_id);
CREATE INDEX idx_students_name      ON students(full_name);
CREATE INDEX idx_students_gradyear  ON students(graduation_year, cgpa);
CREATE INDEX idx_drives_status      ON placement_drives(status, application_deadline);
CREATE INDEX idx_drives_date        ON placement_drives(drive_date);
CREATE INDEX idx_apps_drive_status  ON applications(drive_id, status);
CREATE INDEX idx_apps_applied_at    ON applications(applied_at);
CREATE INDEX idx_results_app        ON round_results(application_id);
CREATE INDEX idx_offers_status      ON offers(status);
CREATE INDEX idx_companies_industry ON companies(industry);
-- 04: Sample data. Dates are relative to CURDATE() so OPEN drives stay open whenever the script is run.
USE campus_placement;

INSERT INTO departments (dept_id, dept_code, dept_name) VALUES
(1,'CSE','Computer Science and Engineering'),
(2,'IT','Information Technology'),
(3,'ECE','Electronics and Communication Engineering'),
(4,'EEE','Electrical and Electronics Engineering'),
(5,'MECH','Mechanical Engineering');

INSERT INTO skills (skill_id, skill_name, category) VALUES
(1,'Java','Programming'),
(2,'Python','Programming'),
(3,'SQL','Databases'),
(4,'C++','Programming'),
(5,'JavaScript','Web'),
(6,'Data Structures','Core CS'),
(7,'Machine Learning','Data Science'),
(8,'Cloud Computing','Infrastructure'),
(9,'Embedded C','Electronics'),
(10,'MATLAB','Engineering Tools'),
(11,'AutoCAD','Engineering Tools'),
(12,'Communication','Soft Skills'),
(13,'Networking','Infrastructure'),
(14,'Linux','Infrastructure');

-- Password for officer: Officer@123 ; password for every student: Student@123
INSERT INTO users (user_id, username, password_hash, role, display_name) VALUES
(1,'officer','pbkdf2_sha256$65536$a/tyX1Vi18C4tBRCKmmJaw==$PFdd9SUlT6RFh2x4wQ4Uqw3Lj0BNw6iLqt/3NayboiQ=','OFFICER','Dr. Revathi Menon'),
(2,'tpo.assistant','pbkdf2_sha256$65536$os7Hz8iPCBFgnVD0HfNECg==$/cTrwG0RTGpJC587gYmQmIXHS4+KkwqV8sPp67uIM3Y=','OFFICER','Suresh Varma'),
(3,'21CSE001','pbkdf2_sha256$65536$yNsPKN+ybDoWa0NwX8u6pQ==$EcIXrNbspuKJwnXZNO1kZr7t09juh7wXfrjsKVi4Pds=','STUDENT','Ananya Rao'),
(4,'21CSE002','pbkdf2_sha256$65536$UVrkiyiFj8g2KGCY9Xethg==$lwjgxaEiiYjlca4E/SGYhMi5JN3PSWQdAl9TryAl0eM=','STUDENT','Rahul Mehta'),
(5,'21CSE003','pbkdf2_sha256$65536$3H3rOgeU5z1UQmZQ5s3uTQ==$ONZ0mSKGNFUvaYDtZzQOpRJcw79hWPiuZev7Z5bD5vo=','STUDENT','Priya Sharma'),
(6,'21CSE004','pbkdf2_sha256$65536$Q8WWMlFtR6iVhg6vjfHl/Q==$Q34qXblvbgyzrTwpFyamRbnBUuHz0ouoFDcJc/5epkE=','STUDENT','Karthik Reddy'),
(7,'21CSE005','pbkdf2_sha256$65536$UpC0da/rwb2JX3F0RVFFQQ==$ZecB5+ohNm5dHYo9PmGYprX6ki0mUcjuFa8pQ5nknTk=','STUDENT','Sneha Iyer'),
(8,'21CSE006','pbkdf2_sha256$65536$G0Swrd0BlDfl96Qa3KV3Aw==$XpzLVJQNdtAOx7aOLslIGXaH2b7Y7JkBX8UeVDJsn+w=','STUDENT','Arjun Nair'),
(9,'21IT001','pbkdf2_sha256$65536$AMJXTacXH7NIuU4X83NHQg==$FO9EOKX2dU5t5MrzeckptMBlAKAa+gy9DzGX5OoX7to=','STUDENT','Meera Joshi'),
(10,'21IT002','pbkdf2_sha256$65536$pjczKX56yazCvzxzqRbtGA==$UgNxLX7ZlCy/09MZygDYA5amEmW69e/7MeiytqqzKT0=','STUDENT','Vikram Singh'),
(11,'21IT003','pbkdf2_sha256$65536$ZcX/f5iI22O9CZwOhWSZaw==$D6+J4x2DLmFjowQ4OyL+ow3pwF0ilc4M/8gKNk6RdNw=','STUDENT','Divya Menon'),
(12,'21IT004','pbkdf2_sha256$65536$njdnsJGGpDIinRw4Hu3cRA==$U2ntbNbt+K0HHmxfgycJdX/OIgZsmeuU9Mosg7qmVtw=','STUDENT','Rohan Gupta'),
(13,'21IT005','pbkdf2_sha256$65536$V9RaS3JQNIo7l9T/R8fvGQ==$w3KIUY2TjPpRwDEpUsEzoIQ3yEzkSWZ6lO1rnt7fw+U=','STUDENT','Kavya Pillai'),
(14,'21ECE001','pbkdf2_sha256$65536$10irqfnpYASIxlMyFohuzA==$XZw3jzIIyEEMpUxz3w/EZCdrTmvEu4nBpkSe7I/0pis=','STUDENT','Aditya Kulkarni'),
(15,'21ECE002','pbkdf2_sha256$65536$o95mNSlcQGeHWOcuRYvuVA==$zqRiix76UgodBfuFz92KB07f9WF12gKwUjR3Z0wBI5s=','STUDENT','Nisha Verma'),
(16,'21ECE003','pbkdf2_sha256$65536$Axkg6+dnA1T88Cp2X1mSEQ==$uz5JvNWLhokTAr5Y9dpibd5UOK0yy+8D/IFtiIt7FiY=','STUDENT','Siddharth Rao'),
(17,'21ECE004','pbkdf2_sha256$65536$du4XrTvMhntLdwURTzj0Fg==$zu4y2KExPAmPulh6BPhMJ9gp36O042XxO69D38+4Abg=','STUDENT','Pooja Desai'),
(18,'21ECE005','pbkdf2_sha256$65536$eh6yCzma1gaWABIQPR0s8g==$8SkAVjl/CC9lInEgJehX/Er+a4VwkyNCM3Qal01V3P8=','STUDENT','Harish Kumar'),
(19,'21EEE001','pbkdf2_sha256$65536$NnmjC7CRaS0q4jzuCkuMJQ==$3hYzk9LL1FMZqCaVIvVY6IgpL1l+9QvrFXYlGh6NibM=','STUDENT','Lakshmi Prasad'),
(20,'21EEE002','pbkdf2_sha256$65536$DP4d+azodXZ49u/3psWXKA==$CVNRl0gCAtgbgSZ4a/aWC658gpFgeMBVbHVBvs7RcaU=','STUDENT','Manoj Yadav'),
(21,'21EEE003','pbkdf2_sha256$65536$qJcLuWceVVt3YPF9WtYd7g==$eOrCrduo6aHt7L3KEDudMiNCmfD8sJlRsIFqkRnFASw=','STUDENT','Swathi Reddy'),
(22,'21ME001','pbkdf2_sha256$65536$C6AoYz4YixnCi8NFbFA0Jw==$AauKD/y1M6bpN/OvSqGJcgsrgG0yoF7sEjCDZyUYl8s=','STUDENT','Abhishek Patil'),
(23,'21ME002','pbkdf2_sha256$65536$nelGkezVBfM/+Io+mY2EHA==$nHTcmKz1GZof4pJccYR5FJFlBDRelMBGokLILU3E0G0=','STUDENT','Ishita Banerjee'),
(24,'21ME003','pbkdf2_sha256$65536$fHCaJgCLPAfyudfXNhnWbA==$1Wf/ar6+sPVB5jNBmm6StnD6dqIhv3BUiYixpAIVIdM=','STUDENT','Naveen Chandra'),
(25,'22CSE001','pbkdf2_sha256$65536$8PT4VuJo7b3/V4Wjb+eoDg==$uteAg3SRQOCSTghPX7qvKzLoXDh6IlADa/Z5SAC6snI=','STUDENT','Tanvi Kapoor'),
(26,'22IT001','pbkdf2_sha256$65536$sOzY/SaHjqwiJJHJWoA7WQ==$svRc/HWrRQ07yrmY1zsXaX8IwGDZhdQ5ufO9awECgxQ=','STUDENT','Farhan Ali');

INSERT INTO students (student_id, user_id, full_name, email, phone, dept_id, graduation_year, cgpa, backlogs) VALUES
('21CSE001',3,'Ananya Rao','ananya.rao@university.edu','9895822412',1,2027,9.12,0),
('21CSE002',4,'Rahul Mehta','rahul.mehta@university.edu','9824942603',1,2027,8.45,0),
('21CSE003',5,'Priya Sharma','priya.sharma@university.edu','9813356886',1,2027,7.64,1),
('21CSE004',6,'Karthik Reddy','karthik.reddy@university.edu','9846913810',1,2027,8.88,0),
('21CSE005',7,'Sneha Iyer','sneha.iyer@university.edu','9842868828',1,2027,6.95,2),
('21CSE006',8,'Arjun Nair','arjun.nair@university.edu','9839958838',1,2027,9.40,0),
('21IT001',9,'Meera Joshi','meera.joshi@university.edu','9828728463',2,2027,8.20,0),
('21IT002',10,'Vikram Singh','vikram.singh@university.edu','9823756669',2,2027,7.80,0),
('21IT003',11,'Divya Menon','divya.menon@university.edu','9883197857',2,2027,8.65,0),
('21IT004',12,'Rohan Gupta','rohan.gupta@university.edu','9821668732',2,2027,7.10,1),
('21IT005',13,'Kavya Pillai','kavya.pillai@university.edu','9889254563',2,2027,9.05,0),
('21ECE001',14,'Aditya Kulkarni','aditya.kulkarni@university.edu','9866629388',3,2027,8.30,0),
('21ECE002',15,'Nisha Verma','nisha.verma@university.edu','9814265799',3,2027,7.45,0),
('21ECE003',16,'Siddharth Rao','siddharth.rao@university.edu','9813999315',3,2027,8.95,0),
('21ECE004',17,'Pooja Desai','pooja.desai@university.edu','9822575562',3,2027,6.80,3),
('21ECE005',18,'Harish Kumar','harish.kumar@university.edu','9839345092',3,2027,7.90,0),
('21EEE001',19,'Lakshmi Prasad','lakshmi.prasad@university.edu','9841227216',4,2027,8.10,0),
('21EEE002',20,'Manoj Yadav','manoj.yadav@university.edu','9877827638',4,2027,7.25,1),
('21EEE003',21,'Swathi Reddy','swathi.reddy@university.edu','9890801586',4,2027,8.70,0),
('21ME001',22,'Abhishek Patil','abhishek.patil@university.edu','9813561597',5,2027,7.55,0),
('21ME002',23,'Ishita Banerjee','ishita.banerjee@university.edu','9885329037',5,2027,8.40,0),
('21ME003',24,'Naveen Chandra','naveen.chandra@university.edu','9836687537',5,2027,6.60,2),
('22CSE001',25,'Tanvi Kapoor','tanvi.kapoor@university.edu','9897226012',1,2028,8.75,0),
('22IT001',26,'Farhan Ali','farhan.ali@university.edu','9883140807',2,2028,8.05,0);

INSERT INTO academic_records (student_id, semester, sgpa, marks_percentage) VALUES
('21CSE001',1,9.06,84.57),
('21CSE001',2,9.08,84.76),
('21CSE001',3,8.93,83.33),
('21CSE001',4,9.46,88.37),
('21CSE001',5,9.36,87.42),
('21CSE001',6,8.82,82.29),
('21CSE002',1,8.51,79.34),
('21CSE002',2,8.38,78.11),
('21CSE002',3,8.32,77.54),
('21CSE002',4,8.82,82.29),
('21CSE002',5,8.22,76.59),
('21CSE002',6,8.47,78.97),
('21CSE003',1,7.72,71.84),
('21CSE003',2,7.71,71.75),
('21CSE003',3,7.64,71.08),
('21CSE003',4,7.44,69.18),
('21CSE003',5,7.81,72.69),
('21CSE003',6,7.51,69.84),
('21CSE004',1,9.23,86.19),
('21CSE004',2,8.47,78.97),
('21CSE004',3,8.66,80.77),
('21CSE004',4,8.97,83.72),
('21CSE004',5,9.20,85.90),
('21CSE004',6,8.73,81.44),
('21CSE005',1,6.59,61.10),
('21CSE005',2,6.48,60.06),
('21CSE005',3,7.02,65.19),
('21CSE005',4,7.12,66.14),
('21CSE005',5,7.31,67.94),
('21CSE005',6,7.19,66.81),
('21CSE006',1,9.72,90.84),
('21CSE006',2,9.28,86.66),
('21CSE006',3,9.35,87.33),
('21CSE006',4,9.69,90.55),
('21CSE006',5,9.09,84.86),
('21CSE006',6,9.26,86.47),
('21IT001',1,8.31,77.45),
('21IT001',2,8.34,77.73),
('21IT001',3,8.33,77.64),
('21IT001',4,7.77,72.31),
('21IT001',5,8.28,77.16),
('21IT001',6,8.19,76.30),
('21IT002',1,7.60,70.70),
('21IT002',2,7.80,72.60),
('21IT002',3,7.62,70.89),
('21IT002',4,8.21,76.50),
('21IT002',5,8.00,74.50),
('21IT002',6,7.58,70.51),
('21IT003',1,8.47,78.97),
('21IT003',2,8.87,82.76),
('21IT003',3,8.23,76.69),
('21IT003',4,8.92,83.24),
('21IT003',5,8.90,83.05),
('21IT003',6,8.54,79.63),
('21IT004',1,6.71,62.24),
('21IT004',2,7.47,69.47),
('21IT004',3,7.16,66.52),
('21IT004',4,7.30,67.85),
('21IT004',5,6.84,63.48),
('21IT004',6,7.10,65.95),
('21IT005',1,9.38,87.61),
('21IT005',2,9.16,85.52),
('21IT005',3,8.71,81.25),
('21IT005',4,8.71,81.25),
('21IT005',5,9.25,86.38),
('21IT005',6,9.07,84.67),
('21ECE001',1,8.48,79.06),
('21ECE001',2,8.20,76.40),
('21ECE001',3,8.34,77.73),
('21ECE001',4,8.14,75.83),
('21ECE001',5,8.71,81.25),
('21ECE001',6,7.93,73.83),
('21ECE002',1,7.43,69.08),
('21ECE002',2,7.67,71.36),
('21ECE002',3,7.76,72.22),
('21ECE002',4,7.13,66.23),
('21ECE002',5,7.13,66.23),
('21ECE002',6,7.60,70.70),
('21ECE003',1,9.06,84.57),
('21ECE003',2,8.87,82.76),
('21ECE003',3,9.06,84.57),
('21ECE003',4,8.94,83.43),
('21ECE003',5,8.75,81.62),
('21ECE003',6,9.02,84.19),
('21ECE004',1,7.03,65.28),
('21ECE004',2,6.79,63.00),
('21ECE004',3,6.28,58.16),
('21ECE004',4,6.98,64.81),
('21ECE004',5,6.86,63.67),
('21ECE004',6,6.87,63.77),
('21ECE005',1,7.88,73.36),
('21ECE005',2,7.83,72.89),
('21ECE005',3,7.71,71.75),
('21ECE005',4,7.57,70.42),
('21ECE005',5,8.21,76.50),
('21ECE005',6,8.21,76.50),
('21EEE001',1,8.40,78.30),
('21EEE001',2,8.22,76.59),
('21EEE001',3,7.99,74.41),
('21EEE001',4,7.63,70.98),
('21EEE001',5,8.09,75.36),
('21EEE001',6,8.29,77.25),
('21EEE002',1,7.24,67.28),
('21EEE002',2,6.96,64.62),
('21EEE002',3,7.12,66.14),
('21EEE002',4,6.93,64.33),
('21EEE002',5,7.64,71.08),
('21EEE002',6,7.61,70.80),
('21EEE003',1,9.01,84.09),
('21EEE003',2,8.72,81.34),
('21EEE003',3,8.62,80.39),
('21EEE003',4,8.28,77.16),
('21EEE003',5,8.51,79.34),
('21EEE003',6,9.07,84.67),
('21ME001',1,7.88,73.36),
('21ME001',2,7.37,68.52),
('21ME001',3,7.37,68.52),
('21ME001',4,7.66,71.27),
('21ME001',5,7.22,67.09),
('21ME001',6,7.81,72.69),
('21ME002',1,8.52,79.44),
('21ME002',2,8.67,80.86),
('21ME002',3,8.27,77.06),
('21ME002',4,7.90,73.55),
('21ME002',5,8.38,78.11),
('21ME002',6,8.64,80.58),
('21ME003',1,6.16,57.02),
('21ME003',2,6.48,60.06),
('21ME003',3,6.56,60.82),
('21ME003',4,6.88,63.86),
('21ME003',5,6.85,63.58),
('21ME003',6,6.69,62.06),
('22CSE001',1,8.81,82.20),
('22CSE001',2,8.83,82.39),
('22CSE001',3,8.55,79.73),
('22CSE001',4,8.79,82.00),
('22IT001',1,7.95,74.03),
('22IT001',2,8.42,78.49),
('22IT001',3,8.02,74.69),
('22IT001',4,7.83,72.89);

INSERT INTO student_skills (student_id, skill_id, proficiency) VALUES
('21CSE001',1,'INTERMEDIATE'),
('21CSE001',2,'ADVANCED'),
('21CSE001',3,'INTERMEDIATE'),
('21CSE001',6,'INTERMEDIATE'),
('21CSE001',8,'INTERMEDIATE'),
('21CSE001',14,'INTERMEDIATE'),
('21CSE002',1,'INTERMEDIATE'),
('21CSE002',3,'INTERMEDIATE'),
('21CSE002',6,'INTERMEDIATE'),
('21CSE002',5,'INTERMEDIATE'),
('21CSE003',2,'BEGINNER'),
('21CSE003',3,'INTERMEDIATE'),
('21CSE003',7,'BEGINNER'),
('21CSE004',1,'INTERMEDIATE'),
('21CSE004',4,'ADVANCED'),
('21CSE004',6,'ADVANCED'),
('21CSE004',14,'INTERMEDIATE'),
('21CSE004',8,'INTERMEDIATE'),
('21CSE005',5,'INTERMEDIATE'),
('21CSE005',3,'BEGINNER'),
('21CSE006',2,'ADVANCED'),
('21CSE006',7,'ADVANCED'),
('21CSE006',3,'INTERMEDIATE'),
('21CSE006',6,'INTERMEDIATE'),
('21CSE006',12,'INTERMEDIATE'),
('21IT001',1,'ADVANCED'),
('21IT001',3,'ADVANCED'),
('21IT001',5,'ADVANCED'),
('21IT001',12,'ADVANCED'),
('21IT002',2,'INTERMEDIATE'),
('21IT002',3,'BEGINNER'),
('21IT002',8,'BEGINNER'),
('21IT002',14,'BEGINNER'),
('21IT003',5,'ADVANCED'),
('21IT003',1,'ADVANCED'),
('21IT003',6,'INTERMEDIATE'),
('21IT004',3,'BEGINNER'),
('21IT004',12,'BEGINNER'),
('21IT005',2,'INTERMEDIATE'),
('21IT005',3,'ADVANCED'),
('21IT005',7,'INTERMEDIATE'),
('21IT005',8,'ADVANCED'),
('21IT005',14,'INTERMEDIATE'),
('21ECE001',9,'ADVANCED'),
('21ECE001',10,'ADVANCED'),
('21ECE001',4,'INTERMEDIATE'),
('21ECE002',9,'BEGINNER'),
('21ECE002',10,'INTERMEDIATE'),
('21ECE002',2,'BEGINNER'),
('21ECE003',4,'INTERMEDIATE'),
('21ECE003',9,'INTERMEDIATE'),
('21ECE003',13,'INTERMEDIATE'),
('21ECE003',2,'INTERMEDIATE'),
('21ECE004',10,'BEGINNER'),
('21ECE005',13,'INTERMEDIATE'),
('21ECE005',14,'INTERMEDIATE'),
('21ECE005',4,'INTERMEDIATE'),
('21EEE001',10,'INTERMEDIATE'),
('21EEE001',9,'ADVANCED'),
('21EEE001',12,'INTERMEDIATE'),
('21EEE002',10,'BEGINNER'),
('21EEE002',11,'INTERMEDIATE'),
('21EEE003',2,'INTERMEDIATE'),
('21EEE003',10,'ADVANCED'),
('21EEE003',3,'ADVANCED'),
('21ME001',11,'INTERMEDIATE'),
('21ME001',10,'INTERMEDIATE'),
('21ME002',11,'ADVANCED'),
('21ME002',10,'ADVANCED'),
('21ME002',12,'INTERMEDIATE'),
('21ME003',11,'BEGINNER'),
('22CSE001',1,'ADVANCED'),
('22CSE001',2,'INTERMEDIATE'),
('22CSE001',6,'INTERMEDIATE'),
('22IT001',3,'INTERMEDIATE'),
('22IT001',5,'ADVANCED'),
('22IT001',2,'INTERMEDIATE');

INSERT INTO companies (company_id, company_name, industry, website, contact_person, contact_email, contact_phone) VALUES
(1,'Northwind Technologies','Software Services','https://northwind.example.com','Anita Desai','campus@northwind.example.com','0801841248'),
(2,'Meridian Analytics','Data and Analytics','https://meridian.example.com','Rakesh Bhat','talent@meridian.example.com','0808999183'),
(3,'Helix Semiconductors','Semiconductors','https://helixsemi.example.com','Joseph Mathew','hr@helixsemi.example.com','0809436429'),
(4,'Kestrel Systems','IT Consulting','https://kestrel.example.com','Farida Khan','recruit@kestrel.example.com','0809910817'),
(5,'Aurelia Software','Product Engineering','https://aurelia.example.com','Neha Sood','careers@aurelia.example.com','0803641282'),
(6,'Tessera Cloud','Cloud Infrastructure','https://tessera.example.com','Gautam Rao','people@tessera.example.com','0801954280'),
(7,'Vantage Consulting','Management Consulting','https://vantage.example.com','Isabel Fernandes','campus@vantage.example.com','0809519948'),
(8,'Orion Motors','Automotive','https://orionmotors.example.com','Prakash Iyer','get@orionmotors.example.com','0802344047'),
(9,'Lumen Finance Labs','Financial Technology','https://lumenlabs.example.com','Shreya Ghosh','quant@lumenlabs.example.com','0804117625');

INSERT INTO job_profiles (job_id, company_id, position, description, package_lpa, location) VALUES
(1,1,'Software Engineer','Design, build and maintain enterprise Java services.',12.00,'Bengaluru'),
(2,1,'Data Engineer','Build batch and streaming data pipelines on modern platforms.',16.00,'Hyderabad'),
(3,2,'Data Analyst','Analyse business data and produce insight dashboards.',8.50,'Pune'),
(4,3,'VLSI Design Engineer','RTL design and verification for mixed-signal SoCs.',10.00,'Hyderabad'),
(5,4,'Systems Engineer','Support and modernise client infrastructure and applications.',6.50,'Chennai'),
(6,5,'Frontend Developer','Build accessible, performant interfaces for SaaS products.',9.00,'Bengaluru'),
(7,6,'Cloud Engineer','Operate and automate large multi-region cloud platforms.',14.00,'Hyderabad'),
(8,7,'Business Analyst','Translate client requirements into delivery plans.',7.00,'Mumbai'),
(9,8,'Graduate Engineer Trainee','Rotational programme across design and manufacturing.',5.50,'Pune'),
(10,9,'Quant Developer','Low-latency pricing and risk systems in C++.',18.00,'Mumbai'),
(11,2,'Business Intelligence Developer','Model data warehouses and build reporting layers.',9.50,'Pune');

INSERT INTO job_profile_skills (job_id, skill_id) VALUES
(1,1),
(1,6),
(1,3),
(2,3),
(2,2),
(2,7),
(3,3),
(3,2),
(4,9),
(4,4),
(6,5),
(7,8),
(7,14),
(8,12),
(8,3),
(9,10),
(9,11),
(10,4),
(10,6),
(11,3);

INSERT INTO placement_drives (drive_id, job_id, drive_date, application_deadline, venue, status) VALUES
(1,1,DATE_ADD(CURDATE(), INTERVAL -40 DAY),DATE_ADD(CURDATE(), INTERVAL -46 DAY),'Main Auditorium','COMPLETED'),
(2,3,DATE_ADD(CURDATE(), INTERVAL -32 DAY),DATE_ADD(CURDATE(), INTERVAL -38 DAY),'Seminar Hall A','COMPLETED'),
(3,4,DATE_ADD(CURDATE(), INTERVAL -25 DAY),DATE_ADD(CURDATE(), INTERVAL -30 DAY),'ECE Block Lab 3','COMPLETED'),
(4,5,DATE_ADD(CURDATE(), INTERVAL -2 DAY),DATE_ADD(CURDATE(), INTERVAL -6 DAY),'Placement Cell','CLOSED'),
(5,6,DATE_ADD(CURDATE(), INTERVAL 17 DAY),DATE_ADD(CURDATE(), INTERVAL 10 DAY),'Seminar Hall B','OPEN'),
(6,7,DATE_ADD(CURDATE(), INTERVAL 21 DAY),DATE_ADD(CURDATE(), INTERVAL 14 DAY),'Main Auditorium','OPEN'),
(7,8,DATE_ADD(CURDATE(), INTERVAL 12 DAY),DATE_ADD(CURDATE(), INTERVAL 7 DAY),'Seminar Hall A','OPEN'),
(8,9,DATE_ADD(CURDATE(), INTERVAL 27 DAY),DATE_ADD(CURDATE(), INTERVAL 20 DAY),'Mechanical Workshop Hall','OPEN'),
(9,10,DATE_ADD(CURDATE(), INTERVAL 38 DAY),DATE_ADD(CURDATE(), INTERVAL 30 DAY),'Placement Cell','UPCOMING'),
(10,2,DATE_ADD(CURDATE(), INTERVAL 19 DAY),DATE_ADD(CURDATE(), INTERVAL 12 DAY),'Seminar Hall B','OPEN');

INSERT INTO eligibility_criteria (criteria_id, drive_id, min_cgpa, max_backlogs, graduation_year) VALUES
(1,1,7.50,0,2027),
(2,2,7.00,1,2027),
(3,3,7.50,0,2027),
(4,4,6.50,2,2027),
(5,5,7.00,1,2027),
(6,6,8.00,0,2027),
(7,7,6.50,1,2027),
(8,8,6.00,2,2027),
(9,9,8.50,0,2027),
(10,10,9.50,0,2027);

INSERT INTO eligibility_departments (criteria_id, dept_id) VALUES
(1,1),
(1,2),
(3,3),
(3,4),
(5,1),
(5,2),
(6,1),
(6,2),
(8,4),
(8,5),
(9,1),
(9,2);

INSERT INTO eligibility_skills (criteria_id, skill_id) VALUES
(1,1),
(1,6),
(2,3),
(2,2),
(3,9),
(5,5),
(6,8),
(6,14),
(7,12),
(8,10),
(9,4),
(9,6),
(10,3),
(10,2),
(10,7);

INSERT INTO applications (application_id, student_id, drive_id, applied_at, status) VALUES
(1,'21CSE001',1,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -46 DAY), INTERVAL 1 DAY) + INTERVAL 10 HOUR,'SELECTED'),
(2,'21CSE002',1,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -46 DAY), INTERVAL 6 DAY) + INTERVAL 12 HOUR,'SELECTED'),
(3,'21CSE004',1,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -46 DAY), INTERVAL 4 DAY) + INTERVAL 10 HOUR,'REJECTED'),
(4,'21IT003',1,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -46 DAY), INTERVAL 5 DAY) + INTERVAL 12 HOUR,'REJECTED'),
(5,'21CSE003',2,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -38 DAY), INTERVAL 5 DAY) + INTERVAL 9 HOUR,'SELECTED'),
(6,'21CSE006',2,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -38 DAY), INTERVAL 5 DAY) + INTERVAL 10 HOUR,'SELECTED'),
(7,'21IT002',2,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -38 DAY), INTERVAL 4 DAY) + INTERVAL 17 HOUR,'REJECTED'),
(8,'21EEE003',2,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -38 DAY), INTERVAL 3 DAY) + INTERVAL 13 HOUR,'REJECTED'),
(9,'21ECE001',3,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -30 DAY), INTERVAL 2 DAY) + INTERVAL 14 HOUR,'SELECTED'),
(10,'21ECE003',3,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -30 DAY), INTERVAL 2 DAY) + INTERVAL 13 HOUR,'SELECTED'),
(11,'21EEE001',3,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -30 DAY), INTERVAL 4 DAY) + INTERVAL 11 HOUR,'REJECTED'),
(12,'21CSE005',4,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -6 DAY), INTERVAL 6 DAY) + INTERVAL 13 HOUR,'SHORTLISTED'),
(13,'21IT004',4,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -6 DAY), INTERVAL 4 DAY) + INTERVAL 14 HOUR,'SHORTLISTED'),
(14,'21ECE005',4,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -6 DAY), INTERVAL 1 DAY) + INTERVAL 9 HOUR,'SHORTLISTED'),
(15,'21EEE002',4,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -6 DAY), INTERVAL 4 DAY) + INTERVAL 10 HOUR,'REJECTED'),
(16,'21IT001',4,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL -6 DAY), INTERVAL 1 DAY) + INTERVAL 17 HOUR,'SHORTLISTED'),
(17,'21IT001',5,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 10 DAY), INTERVAL 13 DAY) + INTERVAL 17 HOUR,'APPLIED'),
(18,'21IT003',5,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 10 DAY), INTERVAL 12 DAY) + INTERVAL 11 HOUR,'APPLIED'),
(19,'21CSE004',6,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 14 DAY), INTERVAL 16 DAY) + INTERVAL 10 HOUR,'APPLIED'),
(20,'21IT005',6,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 14 DAY), INTERVAL 17 DAY) + INTERVAL 14 HOUR,'APPLIED'),
(21,'21IT004',7,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 7 DAY), INTERVAL 9 DAY) + INTERVAL 11 HOUR,'APPLIED'),
(22,'21ME002',7,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 7 DAY), INTERVAL 9 DAY) + INTERVAL 17 HOUR,'APPLIED'),
(23,'21EEE001',7,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 7 DAY), INTERVAL 8 DAY) + INTERVAL 13 HOUR,'APPLIED'),
(24,'21ME001',8,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 20 DAY), INTERVAL 21 DAY) + INTERVAL 17 HOUR,'APPLIED'),
(25,'21EEE002',8,DATE_SUB(DATE_ADD(CURDATE(), INTERVAL 20 DAY), INTERVAL 23 DAY) + INTERVAL 17 HOUR,'APPLIED');

INSERT INTO selection_rounds (round_id, drive_id, round_name, sequence_no, round_date, status) VALUES
(1,1,'Aptitude Test',1,DATE_ADD(CURDATE(), INTERVAL -40 DAY),'COMPLETED'),
(2,1,'Technical Interview',2,DATE_ADD(CURDATE(), INTERVAL -39 DAY),'COMPLETED'),
(3,1,'HR Interview',3,DATE_ADD(CURDATE(), INTERVAL -38 DAY),'COMPLETED'),
(4,2,'Online Assessment',1,DATE_ADD(CURDATE(), INTERVAL -32 DAY),'COMPLETED'),
(5,2,'Case Interview',2,DATE_ADD(CURDATE(), INTERVAL -31 DAY),'COMPLETED'),
(6,3,'Technical Test',1,DATE_ADD(CURDATE(), INTERVAL -25 DAY),'COMPLETED'),
(7,3,'Panel Interview',2,DATE_ADD(CURDATE(), INTERVAL -24 DAY),'COMPLETED'),
(8,4,'Aptitude Test',1,DATE_ADD(CURDATE(), INTERVAL -2 DAY),'COMPLETED'),
(9,4,'Technical Interview',2,DATE_ADD(CURDATE(), INTERVAL -1 DAY),'IN_PROGRESS'),
(10,4,'HR Interview',3,DATE_ADD(CURDATE(), INTERVAL 1 DAY),'UPCOMING');

-- Results are inserted in round order; triggers verify progression.
INSERT INTO round_results (round_id, application_id, result, remarks) VALUES
(1,1,'PASS','Cleared'),
(2,1,'PASS','Cleared'),
(3,1,'PASS','Cleared'),
(1,2,'PASS','Cleared'),
(2,2,'PASS','Cleared'),
(3,2,'PASS','Cleared'),
(1,3,'PASS','Cleared'),
(2,3,'FAIL','Did not meet the bar'),
(1,4,'FAIL','Did not meet the bar'),
(4,5,'PASS','Cleared'),
(5,5,'PASS','Cleared'),
(4,6,'PASS','Cleared'),
(5,6,'PASS','Cleared'),
(4,7,'PASS','Cleared'),
(5,7,'FAIL','Did not meet the bar'),
(4,8,'FAIL','Did not meet the bar'),
(6,9,'PASS','Cleared'),
(7,9,'PASS','Cleared'),
(6,10,'PASS','Cleared'),
(7,10,'PASS','Cleared'),
(6,11,'FAIL','Did not meet the bar'),
(8,12,'PASS','Cleared'),
(8,13,'PASS','Cleared'),
(8,14,'PASS','Cleared'),
(8,15,'FAIL','Did not meet the bar'),
(8,16,'PASS','Cleared'),
(9,14,'PASS','Cleared'),
(9,16,'PASS','Cleared');

INSERT INTO offers (application_id, package_lpa, offer_date, joining_date, status, responded_at) VALUES
(1,12.00,DATE_ADD(CURDATE(), INTERVAL -35 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'ACCEPTED',DATE_ADD(CURDATE(), INTERVAL -31 DAY) + INTERVAL 11 HOUR),
(2,12.00,DATE_ADD(CURDATE(), INTERVAL -35 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'REJECTED',DATE_ADD(CURDATE(), INTERVAL -31 DAY) + INTERVAL 11 HOUR),
(5,8.50,DATE_ADD(CURDATE(), INTERVAL -28 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'PENDING',NULL),
(6,8.50,DATE_ADD(CURDATE(), INTERVAL -28 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'ACCEPTED',DATE_ADD(CURDATE(), INTERVAL -24 DAY) + INTERVAL 11 HOUR),
(9,10.00,DATE_ADD(CURDATE(), INTERVAL -20 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'ACCEPTED',DATE_ADD(CURDATE(), INTERVAL -16 DAY) + INTERVAL 11 HOUR),
(10,10.00,DATE_ADD(CURDATE(), INTERVAL -20 DAY),DATE_ADD(CURDATE(), INTERVAL 240 DAY),'PENDING',NULL);

-- 05: Views
USE campus_placement;

CREATE OR REPLACE VIEW vw_drive_summary AS
SELECT d.drive_id, d.job_id, j.company_id, c.company_name, j.position, j.package_lpa, j.location,
       d.drive_date, d.application_deadline, d.venue, d.status,
       ec.criteria_id, ec.min_cgpa, ec.max_backlogs, ec.graduation_year,
       (SELECT COUNT(*) FROM applications a WHERE a.drive_id = d.drive_id) AS application_count
FROM placement_drives d
JOIN job_profiles j ON j.job_id = d.job_id
JOIN companies c    ON c.company_id = j.company_id
LEFT JOIN eligibility_criteria ec ON ec.drive_id = d.drive_id;

CREATE OR REPLACE VIEW vw_student_applications AS
SELECT a.application_id, a.student_id, s.full_name, s.dept_id, dp.dept_code, s.cgpa,
       a.drive_id, c.company_id, c.company_name, j.position, j.package_lpa, j.location,
       d.drive_date, d.application_deadline, d.status AS drive_status,
       a.status, a.applied_at, a.updated_at
FROM applications a
JOIN students s         ON s.student_id = a.student_id
JOIN departments dp     ON dp.dept_id = s.dept_id
JOIN placement_drives d ON d.drive_id = a.drive_id
JOIN job_profiles j     ON j.job_id = d.job_id
JOIN companies c        ON c.company_id = j.company_id;

CREATE OR REPLACE VIEW vw_shortlisted_students AS
SELECT va.application_id, va.student_id, va.full_name, va.dept_code, va.cgpa,
       va.drive_id, va.company_name, va.position, va.status,
       (SELECT COUNT(*) FROM round_results rr WHERE rr.application_id = va.application_id AND rr.result = 'PASS') AS rounds_cleared,
       (SELECT COUNT(*) FROM selection_rounds sr WHERE sr.drive_id = va.drive_id) AS total_rounds
FROM vw_student_applications va
WHERE va.status IN ('SHORTLISTED','SELECTED');

CREATE OR REPLACE VIEW vw_student_offers AS
SELECT o.offer_id, o.application_id, a.student_id, s.full_name, dp.dept_code,
       c.company_name, j.position, j.location, o.package_lpa, o.offer_date, o.joining_date,
       o.status, o.responded_at, a.drive_id
FROM offers o
JOIN applications a     ON a.application_id = o.application_id
JOIN students s         ON s.student_id = a.student_id
JOIN departments dp     ON dp.dept_id = s.dept_id
JOIN placement_drives d ON d.drive_id = a.drive_id
JOIN job_profiles j     ON j.job_id = d.job_id
JOIN companies c        ON c.company_id = j.company_id;

-- Placed student = student with at least one ACCEPTED offer
CREATE OR REPLACE VIEW vw_department_placement AS
SELECT dp.dept_id, dp.dept_code, dp.dept_name,
       COUNT(DISTINCT s.student_id) AS total_students,
       COUNT(DISTINCT a.student_id) AS students_applied,
       COUNT(DISTINCT CASE WHEN o.status = 'ACCEPTED' THEN a.student_id END) AS students_placed,
       ROUND(100 * COUNT(DISTINCT CASE WHEN o.status = 'ACCEPTED' THEN a.student_id END)
             / NULLIF(COUNT(DISTINCT s.student_id), 0), 2) AS placement_percentage,
       ROUND(AVG(CASE WHEN o.status = 'ACCEPTED' THEN o.package_lpa END), 2) AS avg_accepted_package
FROM departments dp
LEFT JOIN students s     ON s.dept_id = dp.dept_id
LEFT JOIN applications a ON a.student_id = s.student_id
LEFT JOIN offers o       ON o.application_id = a.application_id
GROUP BY dp.dept_id, dp.dept_code, dp.dept_name;

CREATE OR REPLACE VIEW vw_company_recruitment AS
SELECT c.company_id, c.company_name, c.industry,
       COUNT(DISTINCT d.drive_id) AS drives,
       COUNT(DISTINCT a.application_id) AS applications,
       COUNT(DISTINCT CASE WHEN a.status = 'SELECTED' THEN a.application_id END) AS selected,
       COUNT(DISTINCT o.offer_id) AS offers,
       COUNT(DISTINCT CASE WHEN o.status = 'ACCEPTED' THEN o.offer_id END) AS accepted_offers,
       MAX(o.package_lpa) AS highest_offer_lpa
FROM companies c
LEFT JOIN job_profiles j     ON j.company_id = c.company_id
LEFT JOIN placement_drives d ON d.job_id = j.job_id
LEFT JOIN applications a     ON a.drive_id = d.drive_id
LEFT JOIN offers o           ON o.application_id = a.application_id
GROUP BY c.company_id, c.company_name, c.industry;

-- Eligibility evaluated from criteria tables. Mirrors EligibilityService in Java:
-- CGPA >= min, backlogs <= max, matching graduation year (if set),
-- department in allowed list (if any listed), and every required skill held.
CREATE OR REPLACE VIEW vw_drive_eligible_students AS
SELECT d.drive_id, c.company_name, j.position, s.student_id, s.full_name, dp.dept_code,
       s.cgpa, s.backlogs, s.graduation_year
FROM placement_drives d
JOIN eligibility_criteria ec ON ec.drive_id = d.drive_id
JOIN job_profiles j          ON j.job_id = d.job_id
JOIN companies c             ON c.company_id = j.company_id
CROSS JOIN students s
JOIN departments dp          ON dp.dept_id = s.dept_id
WHERE s.cgpa >= ec.min_cgpa
  AND s.backlogs <= ec.max_backlogs
  AND (ec.graduation_year IS NULL OR s.graduation_year = ec.graduation_year)
  AND (NOT EXISTS (SELECT 1 FROM eligibility_departments ed WHERE ed.criteria_id = ec.criteria_id)
       OR EXISTS (SELECT 1 FROM eligibility_departments ed WHERE ed.criteria_id = ec.criteria_id AND ed.dept_id = s.dept_id))
  AND NOT EXISTS (SELECT 1 FROM eligibility_skills es
                  WHERE es.criteria_id = ec.criteria_id
                    AND NOT EXISTS (SELECT 1 FROM student_skills ss
                                    WHERE ss.student_id = s.student_id AND ss.skill_id = es.skill_id));
-- 06: Triggers and stored procedures for cross-table business rules
USE campus_placement;

DROP TRIGGER IF EXISTS trg_round_results_bi;
DROP TRIGGER IF EXISTS trg_round_results_bu;
DROP TRIGGER IF EXISTS trg_round_results_bd;
DROP TRIGGER IF EXISTS trg_selection_rounds_bu;
DROP TRIGGER IF EXISTS trg_selection_rounds_bd;
DROP TRIGGER IF EXISTS trg_offers_bi;
DROP TRIGGER IF EXISTS trg_offers_bu;
DROP TRIGGER IF EXISTS trg_applications_bi;
DROP TRIGGER IF EXISTS trg_applications_bu;
DROP PROCEDURE IF EXISTS sp_respond_offer;
DROP PROCEDURE IF EXISTS sp_drive_eligible_students;

DELIMITER $$

-- A result may only be recorded for a candidate of the same drive who PASSED the previous round.
CREATE TRIGGER trg_round_results_bi BEFORE INSERT ON round_results
FOR EACH ROW
BEGIN
    DECLARE v_round_drive INT;
    DECLARE v_seq INT;
    DECLARE v_app_drive INT;
    DECLARE v_prev_seq INT;
    DECLARE v_prev_result VARCHAR(4);

    SELECT drive_id, sequence_no INTO v_round_drive, v_seq FROM selection_rounds WHERE round_id = NEW.round_id;
    SELECT drive_id INTO v_app_drive FROM applications WHERE application_id = NEW.application_id;

    IF v_round_drive IS NULL OR v_app_drive IS NULL OR v_round_drive <> v_app_drive THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: the candidate did not apply to this drive.';
    END IF;

    SELECT MAX(sequence_no) INTO v_prev_seq FROM selection_rounds
     WHERE drive_id = v_round_drive AND sequence_no < v_seq;

    IF v_prev_seq IS NOT NULL THEN
        SELECT rr.result INTO v_prev_result
          FROM selection_rounds sr
          JOIN round_results rr ON rr.round_id = sr.round_id AND rr.application_id = NEW.application_id
         WHERE sr.drive_id = v_round_drive AND sr.sequence_no = v_prev_seq;
        IF v_prev_result IS NULL OR v_prev_result <> 'PASS' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: the candidate has not passed the previous round.';
        END IF;
    END IF;
END$$

-- A PASS cannot become FAIL while later-round results exist.
CREATE TRIGGER trg_round_results_bu BEFORE UPDATE ON round_results
FOR EACH ROW
BEGIN
    IF NEW.round_id <> OLD.round_id OR NEW.application_id <> OLD.application_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: a result cannot be moved to another round or candidate.';
    END IF;
    IF NEW.result = 'FAIL' AND OLD.result = 'PASS' AND EXISTS (
        SELECT 1 FROM round_results rr
        JOIN selection_rounds later ON later.round_id = rr.round_id
        JOIN selection_rounds cur   ON cur.round_id = OLD.round_id
        WHERE rr.application_id = OLD.application_id
          AND later.drive_id = cur.drive_id AND later.sequence_no > cur.sequence_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: remove later-round results before changing this result to FAIL.';
    END IF;
END$$

CREATE TRIGGER trg_round_results_bd BEFORE DELETE ON round_results
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM round_results rr
        JOIN selection_rounds later ON later.round_id = rr.round_id
        JOIN selection_rounds cur   ON cur.round_id = OLD.round_id
        WHERE rr.application_id = OLD.application_id
          AND later.drive_id = cur.drive_id AND later.sequence_no > cur.sequence_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: remove later-round results first.';
    END IF;
END$$

-- Round order cannot be changed once results are recorded for that round.
CREATE TRIGGER trg_selection_rounds_bu BEFORE UPDATE ON selection_rounds
FOR EACH ROW
BEGIN
    IF (NEW.sequence_no <> OLD.sequence_no OR NEW.drive_id <> OLD.drive_id)
       AND EXISTS (SELECT 1 FROM round_results WHERE round_id = OLD.round_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Round order cannot be changed after results have been recorded.';
    END IF;
END$$

CREATE TRIGGER trg_selection_rounds_bd BEFORE DELETE ON selection_rounds
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM round_results WHERE round_id = OLD.round_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This round has recorded results and cannot be deleted.';
    END IF;
END$$

-- Offers only for SELECTED applications.
CREATE TRIGGER trg_offers_bi BEFORE INSERT ON offers
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(12);
    SELECT status INTO v_status FROM applications WHERE application_id = NEW.application_id;
    IF v_status IS NULL OR v_status <> 'SELECTED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An offer can only be issued to a SELECTED candidate.';
    END IF;
END$$

-- Once accepted or rejected, an offer's decision is final; a student can accept only one offer.
CREATE TRIGGER trg_offers_bu BEFORE UPDATE ON offers
FOR EACH ROW
BEGIN
    IF OLD.status <> 'PENDING' AND NEW.status <> OLD.status THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This offer has already been responded to.';
    END IF;
    IF NEW.application_id <> OLD.application_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An offer cannot be moved to another application.';
    END IF;
    IF NEW.status = 'ACCEPTED' AND OLD.status = 'PENDING' AND EXISTS (
        SELECT 1 FROM offers o
        JOIN applications a  ON a.application_id = o.application_id
        JOIN applications me ON me.application_id = NEW.application_id
        WHERE a.student_id = me.student_id AND o.status = 'ACCEPTED' AND o.offer_id <> NEW.offer_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The student has already accepted another offer.';
    END IF;
END$$

-- New applications always start as APPLIED.
CREATE TRIGGER trg_applications_bi BEFORE INSERT ON applications
FOR EACH ROW
BEGIN
    IF NEW.status IS NULL THEN
        SET NEW.status = 'APPLIED';
    END IF;
END$$

-- An application holding an offer must stay SELECTED.
CREATE TRIGGER trg_applications_bu BEFORE UPDATE ON applications
FOR EACH ROW
BEGIN
    IF OLD.status = 'SELECTED' AND NEW.status <> 'SELECTED'
       AND EXISTS (SELECT 1 FROM offers WHERE application_id = OLD.application_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This application already has an offer; its status must remain SELECTED.';
    END IF;
    IF NEW.student_id <> OLD.student_id OR NEW.drive_id <> OLD.drive_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An application cannot be moved to another student or drive.';
    END IF;
END$$

-- Atomic offer response (the Java application performs the same steps in a JDBC transaction).
CREATE PROCEDURE sp_respond_offer(IN p_offer_id INT, IN p_student_id VARCHAR(20), IN p_decision VARCHAR(10))
BEGIN
    DECLARE v_owner VARCHAR(20);
    DECLARE v_status VARCHAR(10);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_decision NOT IN ('ACCEPTED','REJECTED') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Decision must be ACCEPTED or REJECTED.';
    END IF;

    START TRANSACTION;
    SELECT a.student_id, o.status INTO v_owner, v_status
      FROM offers o JOIN applications a ON a.application_id = o.application_id
     WHERE o.offer_id = p_offer_id FOR UPDATE;

    IF v_owner IS NULL OR v_owner <> p_student_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Offer not found for this student.';
    END IF;
    IF v_status <> 'PENDING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This offer has already been responded to.';
    END IF;

    UPDATE offers SET status = p_decision, responded_at = CURRENT_TIMESTAMP WHERE offer_id = p_offer_id;
    COMMIT;
END$$

CREATE PROCEDURE sp_drive_eligible_students(IN p_drive_id INT)
BEGIN
    SELECT * FROM vw_drive_eligible_students WHERE drive_id = p_drive_id ORDER BY cgpa DESC;
END$$

DELIMITER ;

-- 08: Least-privilege application client user
CREATE USER IF NOT EXISTS 'placement_app'@'localhost' IDENTIFIED BY 'Placement@123';
CREATE USER IF NOT EXISTS 'placement_app'@'%' IDENTIFIED BY 'Placement@123';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON campus_placement.* TO 'placement_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON campus_placement.* TO 'placement_app'@'%';
FLUSH PRIVILEGES;
