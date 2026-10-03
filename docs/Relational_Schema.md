# Relational Schema

Primary keys are **bold**; foreign keys are marked with `→ table(column)`.

- **users**(**user_id**, username, password_hash, role, display_name, is_active, created_at, last_login)
  - UNIQUE `uq_users_username` (username)
- **departments**(**dept_id**, dept_code, dept_name)
  - UNIQUE `uq_departments_code` (dept_code)
  - UNIQUE `uq_departments_name` (dept_name)
- **students**(**student_id**, user_id → users(user_id), full_name, email, phone, dept_id → departments(dept_id), graduation_year, cgpa, backlogs, created_at)
  - UNIQUE `uq_students_email` (email)
  - UNIQUE `uq_students_user` (user_id)
- **academic_records**(**record_id**, student_id → students(student_id), semester, sgpa, marks_percentage)
  - UNIQUE `uq_academic_student_sem` (student_id,semester)
- **skills**(**skill_id**, skill_name, category)
  - UNIQUE `uq_skills_name` (skill_name)
- **student_skills**(**student_id** → students(student_id), **skill_id** → skills(skill_id), proficiency)
- **companies**(**company_id**, company_name, industry, website, contact_person, contact_email, contact_phone)
  - UNIQUE `uq_companies_name` (company_name)
- **job_profiles**(**job_id**, company_id → companies(company_id), position, description, package_lpa, location)
  - UNIQUE `uq_job_company_position` (company_id,position)
- **job_profile_skills**(**job_id** → job_profiles(job_id), **skill_id** → skills(skill_id))
- **placement_drives**(**drive_id**, job_id → job_profiles(job_id), drive_date, application_deadline, venue, status, created_at)
- **eligibility_criteria**(**criteria_id**, drive_id → placement_drives(drive_id), min_cgpa, max_backlogs, graduation_year)
  - UNIQUE `uq_criteria_drive` (drive_id)
- **eligibility_departments**(**criteria_id** → eligibility_criteria(criteria_id), **dept_id** → departments(dept_id))
- **eligibility_skills**(**criteria_id** → eligibility_criteria(criteria_id), **skill_id** → skills(skill_id))
- **applications**(**application_id**, student_id → students(student_id), drive_id → placement_drives(drive_id), applied_at, status, updated_at)
  - UNIQUE `uq_application_student_drive` (student_id,drive_id)
- **selection_rounds**(**round_id**, drive_id → placement_drives(drive_id), round_name, sequence_no, round_date, status)
  - UNIQUE `uq_round_drive_sequence` (drive_id,sequence_no)
- **round_results**(**result_id**, round_id → selection_rounds(round_id), application_id → applications(application_id), result, remarks, recorded_at)
  - UNIQUE `uq_result_round_application` (round_id,application_id)
- **offers**(**offer_id**, application_id → applications(application_id), package_lpa, offer_date, joining_date, status, responded_at)
  - UNIQUE `uq_offer_application` (application_id)

## Views

| View | Purpose |
|---|---|
| vw_drive_summary | Drive with company, job profile, criteria summary and application count |
| vw_student_applications | Applications joined with student, department, drive, job profile and company |
| vw_shortlisted_students | SHORTLISTED/SELECTED applications with rounds cleared and total rounds |
| vw_student_offers | Offers with student, department, company and position |
| vw_department_placement | Students, applicants, placed students, placement % and average accepted package per department |
| vw_company_recruitment | Drives, applications, selections, offers, acceptances and highest offer per company |
| vw_drive_eligible_students | Every (drive, student) pair satisfying all criteria; same rules as the Java eligibility engine |

## Triggers and procedures

| Object | Rule enforced |
|---|---|
| trg_round_results_bi | Result only for an applicant of the same drive who PASSED the previous round |
| trg_round_results_bu | A PASS cannot become FAIL while later-round results exist; results cannot move between rounds |
| trg_round_results_bd | Earlier-round results cannot be deleted while later-round results exist |
| trg_selection_rounds_bu | Round order cannot change after results are recorded |
| trg_selection_rounds_bd | Rounds with results cannot be deleted |
| trg_offers_bi | Offers only for SELECTED applications |
| trg_offers_bu | Responded offers are final; a student may accept only one offer |
| trg_applications_bi | New applications start as APPLIED |
| trg_applications_bu | An application holding an offer must stay SELECTED; applications cannot change student/drive |
| sp_respond_offer(offer, student, decision) | Atomic accept/reject with row lock, ownership and status checks |
| sp_drive_eligible_students(drive) | Eligible students of one drive |
