# ER Diagram

Entity–relationship model of the `campus_placement` database. The diagram uses Mermaid notation
(renders on GitHub, GitLab, VS Code with a Mermaid extension, and https://mermaid.live).

```mermaid
erDiagram
    USERS ||--o| STUDENTS : "logs in as"
    DEPARTMENTS ||--o{ STUDENTS : "enrols"
    STUDENTS ||--o{ ACADEMIC_RECORDS : "has"
    STUDENTS ||--o{ STUDENT_SKILLS : "holds"
    SKILLS ||--o{ STUDENT_SKILLS : "held by"
    COMPANIES ||--o{ JOB_PROFILES : "offers"
    JOB_PROFILES ||--o{ JOB_PROFILE_SKILLS : "describes"
    SKILLS ||--o{ JOB_PROFILE_SKILLS : "described in"
    JOB_PROFILES ||--o{ PLACEMENT_DRIVES : "recruited through"
    PLACEMENT_DRIVES ||--|| ELIGIBILITY_CRITERIA : "governed by"
    ELIGIBILITY_CRITERIA ||--o{ ELIGIBILITY_DEPARTMENTS : "allows"
    DEPARTMENTS ||--o{ ELIGIBILITY_DEPARTMENTS : "allowed in"
    ELIGIBILITY_CRITERIA ||--o{ ELIGIBILITY_SKILLS : "requires"
    SKILLS ||--o{ ELIGIBILITY_SKILLS : "required in"
    STUDENTS ||--o{ APPLICATIONS : "submits"
    PLACEMENT_DRIVES ||--o{ APPLICATIONS : "receives"
    PLACEMENT_DRIVES ||--o{ SELECTION_ROUNDS : "has ordered"
    SELECTION_ROUNDS ||--o{ ROUND_RESULTS : "records"
    APPLICATIONS ||--o{ ROUND_RESULTS : "evaluated in"
    APPLICATIONS ||--o| OFFERS : "may receive"

    USERS {
        int user_id PK
        varchar username UK
        varchar password_hash
        enum role
        varchar display_name
        boolean is_active
    }
    DEPARTMENTS {
        int dept_id PK
        varchar dept_code UK
        varchar dept_name UK
    }
    STUDENTS {
        varchar student_id PK
        int user_id FK
        varchar full_name
        varchar email UK
        varchar phone
        int dept_id FK
        smallint graduation_year
        decimal cgpa
        tinyint backlogs
    }
    ACADEMIC_RECORDS {
        int record_id PK
        varchar student_id FK
        tinyint semester
        decimal sgpa
        decimal marks_percentage
    }
    SKILLS {
        int skill_id PK
        varchar skill_name UK
        varchar category
    }
    STUDENT_SKILLS {
        varchar student_id PK
        int skill_id PK
        enum proficiency
    }
    COMPANIES {
        int company_id PK
        varchar company_name UK
        varchar industry
        varchar website
        varchar contact_person
        varchar contact_email
        varchar contact_phone
    }
    JOB_PROFILES {
        int job_id PK
        int company_id FK
        varchar position
        varchar description
        decimal package_lpa
        varchar location
    }
    JOB_PROFILE_SKILLS {
        int job_id PK
        int skill_id PK
    }
    PLACEMENT_DRIVES {
        int drive_id PK
        int job_id FK
        date drive_date
        date application_deadline
        varchar venue
        enum status
    }
    ELIGIBILITY_CRITERIA {
        int criteria_id PK
        int drive_id FK
        decimal min_cgpa
        tinyint max_backlogs
        smallint graduation_year
    }
    ELIGIBILITY_DEPARTMENTS {
        int criteria_id PK
        int dept_id PK
    }
    ELIGIBILITY_SKILLS {
        int criteria_id PK
        int skill_id PK
    }
    APPLICATIONS {
        int application_id PK
        varchar student_id FK
        int drive_id FK
        timestamp applied_at
        enum status
    }
    SELECTION_ROUNDS {
        int round_id PK
        int drive_id FK
        varchar round_name
        tinyint sequence_no
        date round_date
        enum status
    }
    ROUND_RESULTS {
        int result_id PK
        int round_id FK
        int application_id FK
        enum result
        varchar remarks
    }
    OFFERS {
        int offer_id PK
        int application_id FK
        decimal package_lpa
        date offer_date
        date joining_date
        enum status
        timestamp responded_at
    }
```

## Cardinalities in words

| Relationship | Cardinality | Implementation |
|---|---|---|
| User – Student | 1 : 0..1 | `students.user_id` UNIQUE FK (officers have no student row) |
| Department – Student | 1 : N | `students.dept_id` NOT NULL FK |
| Student – Academic record | 1 : N | FK + UNIQUE (student_id, semester) |
| Student – Skill | M : N | `student_skills` |
| Company – Job profile | 1 : N | `job_profiles.company_id`, UNIQUE (company_id, position) |
| Job profile – Skill | M : N | `job_profile_skills` |
| Job profile – Drive | 1 : N | `placement_drives.job_id` |
| Drive – Eligibility criteria | 1 : 1 | `eligibility_criteria.drive_id` UNIQUE FK |
| Criteria – Department / Skill | M : N | `eligibility_departments`, `eligibility_skills` |
| Student – Drive (application) | M : N | `applications`, UNIQUE (student_id, drive_id) |
| Drive – Selection round | 1 : N | UNIQUE (drive_id, sequence_no) |
| Round – Application (result) | M : N | `round_results`, UNIQUE (round_id, application_id) |
| Application – Offer | 1 : 0..1 | `offers.application_id` UNIQUE FK |
