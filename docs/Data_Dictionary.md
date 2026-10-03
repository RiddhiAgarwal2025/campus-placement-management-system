# Data Dictionary

Generated from the live `campus_placement` schema (MySQL 8). PK = primary key, FK = foreign key.

## users

Login accounts for officers and students (PBKDF2 password hashes).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| user_id | int | no | — | PK, auto increment |
| username | varchar(40) | no | — | UNIQUE (username) |
| password_hash | varchar(255) | no | — | — |
| role | enum('OFFICER','STUDENT') | no | — | — |
| display_name | varchar(100) | no | — | — |
| is_active | tinyint(1) | no | 1 | — |
| created_at | timestamp | no | CURRENT_TIMESTAMP | — |
| last_login | timestamp | yes | — | — |

## departments

Academic departments.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| dept_id | int | no | — | PK, auto increment |
| dept_code | varchar(10) | no | — | UNIQUE (dept_code) |
| dept_name | varchar(100) | no | — | UNIQUE (dept_name) |

## students

Student master record; one login account per student.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| student_id | varchar(20) | no | — | PK |
| user_id | int | yes | — | FK → users(user_id), UNIQUE (user_id) |
| full_name | varchar(100) | no | — | — |
| email | varchar(120) | no | — | UNIQUE (email) |
| phone | varchar(15) | yes | — | — |
| dept_id | int | no | — | FK → departments(dept_id) |
| graduation_year | smallint | no | — | — |
| cgpa | decimal(4,2) | no | 0.00 | — |
| backlogs | tinyint unsigned | no | 0 | — |
| created_at | timestamp | no | CURRENT_TIMESTAMP | — |

CHECK constraints:

- `chk_students_backlogs`: ((backlogs >= 0) and (backlogs <= 60))
- `chk_students_cgpa`: (cgpa between 0 and 10)
- `chk_students_email`: (email like _utf8mb4\\'%_@_%._%\\')
- `chk_students_gradyear`: (graduation_year between 2000 and 2100)

## academic_records

Semester-wise SGPA and marks; one row per student per semester.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| record_id | int | no | — | PK, auto increment |
| student_id | varchar(20) | no | — | FK → students(student_id), UNIQUE (student_id,semester) |
| semester | tinyint | no | — | UNIQUE (student_id,semester) |
| sgpa | decimal(4,2) | no | — | — |
| marks_percentage | decimal(5,2) | no | — | — |

CHECK constraints:

- `chk_academic_marks`: (marks_percentage between 0 and 100)
- `chk_academic_semester`: (semester between 1 and 10)
- `chk_academic_sgpa`: (sgpa between 0 and 10)

## skills

Skills catalogue.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| skill_id | int | no | — | PK, auto increment |
| skill_name | varchar(60) | no | — | UNIQUE (skill_name) |
| category | varchar(40) | no | General | — |

## student_skills

Many-to-many: skills held by each student, with proficiency.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| student_id | varchar(20) | no | — | PK, FK → students(student_id) |
| skill_id | int | no | — | PK, FK → skills(skill_id) |
| proficiency | enum('BEGINNER','INTERMEDIATE','ADVANCED') | no | INTERMEDIATE | — |

## companies

Recruiting companies.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| company_id | int | no | — | PK, auto increment |
| company_name | varchar(100) | no | — | UNIQUE (company_name) |
| industry | varchar(60) | no | — | — |
| website | varchar(150) | yes | — | — |
| contact_person | varchar(100) | yes | — | — |
| contact_email | varchar(120) | yes | — | — |
| contact_phone | varchar(15) | yes | — | — |

## job_profiles

Positions offered by a company, with package and location.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| job_id | int | no | — | PK, auto increment |
| company_id | int | no | — | FK → companies(company_id), UNIQUE (company_id,position) |
| position | varchar(100) | no | — | UNIQUE (company_id,position) |
| description | varchar(1000) | yes | — | — |
| package_lpa | decimal(6,2) | no | — | — |
| location | varchar(100) | no | — | — |

CHECK constraints:

- `chk_job_package`: ((package_lpa > 0) and (package_lpa <= 500))

## job_profile_skills

Many-to-many: skills a job profile describes as required.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| job_id | int | no | — | PK, FK → job_profiles(job_id) |
| skill_id | int | no | — | PK, FK → skills(skill_id) |

## placement_drives

A recruitment drive for one job profile.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| drive_id | int | no | — | PK, auto increment |
| job_id | int | no | — | FK → job_profiles(job_id) |
| drive_date | date | no | — | — |
| application_deadline | date | no | — | — |
| venue | varchar(100) | no | Placement Cell | — |
| status | enum('UPCOMING','OPEN','CLOSED','COMPLETED') | no | UPCOMING | — |
| created_at | timestamp | no | CURRENT_TIMESTAMP | — |

CHECK constraints:

- `chk_drive_dates`: (application_deadline <= drive_date)

## eligibility_criteria

One criteria row per drive (CGPA, backlogs, batch).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| criteria_id | int | no | — | PK, auto increment |
| drive_id | int | no | — | FK → placement_drives(drive_id), UNIQUE (drive_id) |
| min_cgpa | decimal(4,2) | no | 0.00 | — |
| max_backlogs | tinyint unsigned | no | 0 | — |
| graduation_year | smallint | yes | — | — |

CHECK constraints:

- `chk_criteria_cgpa`: (min_cgpa between 0 and 10)
- `chk_criteria_gradyear`: ((graduation_year is null) or (graduation_year between 2000 and 2100))

## eligibility_departments

Departments allowed for a drive (none listed = all departments).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| criteria_id | int | no | — | PK, FK → eligibility_criteria(criteria_id) |
| dept_id | int | no | — | PK, FK → departments(dept_id) |

## eligibility_skills

Skills a student must hold to be eligible for a drive.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| criteria_id | int | no | — | PK, FK → eligibility_criteria(criteria_id) |
| skill_id | int | no | — | PK, FK → skills(skill_id) |

## applications

A student applying to a drive (unique per student and drive).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| application_id | int | no | — | PK, auto increment |
| student_id | varchar(20) | no | — | FK → students(student_id), UNIQUE (student_id,drive_id) |
| drive_id | int | no | — | FK → placement_drives(drive_id), UNIQUE (student_id,drive_id) |
| applied_at | timestamp | no | CURRENT_TIMESTAMP | — |
| status | enum('APPLIED','SHORTLISTED','SELECTED','REJECTED') | no | APPLIED | — |
| updated_at | timestamp | no | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |

## selection_rounds

Ordered rounds of a drive (unique sequence per drive).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| round_id | int | no | — | PK, auto increment |
| drive_id | int | no | — | FK → placement_drives(drive_id), UNIQUE (drive_id,sequence_no) |
| round_name | varchar(80) | no | — | — |
| sequence_no | tinyint | no | — | UNIQUE (drive_id,sequence_no) |
| round_date | date | yes | — | — |
| status | enum('UPCOMING','IN_PROGRESS','COMPLETED') | no | UPCOMING | — |

CHECK constraints:

- `chk_round_sequence`: (sequence_no between 1 and 20)

## round_results

PASS/FAIL of an application in a round.

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| result_id | int | no | — | PK, auto increment |
| round_id | int | no | — | FK → selection_rounds(round_id), UNIQUE (round_id,application_id) |
| application_id | int | no | — | FK → applications(application_id), UNIQUE (round_id,application_id) |
| result | enum('PASS','FAIL') | no | — | — |
| remarks | varchar(255) | yes | — | — |
| recorded_at | timestamp | no | CURRENT_TIMESTAMP | — |

## offers

Offer issued for a SELECTED application (at most one per application).

| Column | Type | Null | Default | Key / Notes |
|---|---|---|---|---|
| offer_id | int | no | — | PK, auto increment |
| application_id | int | no | — | FK → applications(application_id), UNIQUE (application_id) |
| package_lpa | decimal(6,2) | no | — | — |
| offer_date | date | no | — | — |
| joining_date | date | no | — | — |
| status | enum('PENDING','ACCEPTED','REJECTED') | no | PENDING | — |
| responded_at | timestamp | yes | — | — |

CHECK constraints:

- `chk_offer_dates`: (joining_date >= offer_date)
- `chk_offer_package`: ((package_lpa > 0) and (package_lpa <= 500))
- `chk_offer_response`: (((status = _utf8mb4\\'PENDING\\') and (responded_at is null)) or ((status <> _utf8mb4\\'PENDING\\') and (responded_at is not null)))

