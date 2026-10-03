# Functional Dependencies

Notation: `X → Y` means X determines Y. Only non-trivial dependencies are listed. For every table,
every determinant is a candidate key, which is why each table is in BCNF (and therefore 3NF).

| Table | Candidate keys | Functional dependencies |
|---|---|---|
| users | {user_id}, {username} | user_id → username, password_hash, role, display_name, is_active, created_at, last_login; username → user_id |
| departments | {dept_id}, {dept_code}, {dept_name} | dept_id → dept_code, dept_name; dept_code → dept_id; dept_name → dept_id |
| students | {student_id}, {email}, {user_id} | student_id → user_id, full_name, email, phone, dept_id, graduation_year, cgpa, backlogs, created_at; email → student_id; user_id → student_id |
| academic_records | {record_id}, {student_id, semester} | record_id → student_id, semester, sgpa, marks_percentage; {student_id, semester} → record_id, sgpa, marks_percentage |
| skills | {skill_id}, {skill_name} | skill_id → skill_name, category; skill_name → skill_id |
| student_skills | {student_id, skill_id} | {student_id, skill_id} → proficiency |
| companies | {company_id}, {company_name} | company_id → company_name, industry, website, contact_person, contact_email, contact_phone; company_name → company_id |
| job_profiles | {job_id}, {company_id, position} | job_id → company_id, position, description, package_lpa, location; {company_id, position} → job_id |
| job_profile_skills | {job_id, skill_id} | none beyond the key |
| placement_drives | {drive_id} | drive_id → job_id, drive_date, application_deadline, venue, status, created_at |
| eligibility_criteria | {criteria_id}, {drive_id} | criteria_id → drive_id, min_cgpa, max_backlogs, graduation_year; drive_id → criteria_id |
| eligibility_departments | {criteria_id, dept_id} | none beyond the key |
| eligibility_skills | {criteria_id, skill_id} | none beyond the key |
| applications | {application_id}, {student_id, drive_id} | application_id → student_id, drive_id, applied_at, status, updated_at; {student_id, drive_id} → application_id |
| selection_rounds | {round_id}, {drive_id, sequence_no} | round_id → drive_id, round_name, sequence_no, round_date, status; {drive_id, sequence_no} → round_id |
| round_results | {result_id}, {round_id, application_id} | result_id → round_id, application_id, result, remarks, recorded_at; {round_id, application_id} → result_id |
| offers | {offer_id}, {application_id} | offer_id → application_id, package_lpa, offer_date, joining_date, status, responded_at; application_id → offer_id |

## Dependencies deliberately not stored

These facts are derivable, so they are computed with joins/views instead of being stored:

- application → company, position, package (via drive → job profile → company)
- offer → student, company (via application)
- student → department name (via dept_id)
- drive → application count, eligible-student count (aggregates in `vw_drive_summary`, `vw_drive_eligible_students`)
- department → placement percentage (aggregate in `vw_department_placement`)
