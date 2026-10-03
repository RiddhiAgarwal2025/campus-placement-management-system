# Normalization to 3NF

## Starting point (unnormalized)

A naive placement spreadsheet would keep one row per application with repeated and multi-valued data:

| student_id | name | dept | skills | sem1_sgpa…sem8_sgpa | company | industry | position | package | drive_date | round1 | round2 | offer_status |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| 21CSE001 | Ananya Rao | CSE, Computer Science… | Java, Python, SQL | 9.1 … | Northwind | Software | Software Engineer | 12 | … | PASS | PASS | ACCEPTED |

Problems: multi-valued `skills`, repeating semester and round columns, and company/drive facts
repeated for every applicant (update, insert and delete anomalies).

## 1NF — atomic values, no repeating groups

- `skills` list → separate rows in **student_skills** (student_id, skill_id).
- Semester columns → rows in **academic_records** (student_id, semester, sgpa, marks).
- Round columns → **selection_rounds** (ordered by sequence_no) and **round_results** (round, application, result).
- Drive eligibility lists (allowed departments, required skills) → **eligibility_departments**, **eligibility_skills**.

## 2NF — no partial dependency on part of a composite key

Composite-key tables keep only attributes that depend on the whole key:

- student_skills(student_id, skill_id) → proficiency depends on both; skill_name/category moved to **skills**.
- round_results: result depends on (round_id, application_id); round_name and date depend on round_id only → **selection_rounds**.
- eligibility_departments / eligibility_skills hold only the key; names live in departments / skills.

## 3NF — no transitive dependencies

| Transitive dependency in the flat design | Resolution |
|---|---|
| student_id → dept_id → dept_name | **departments** table; students keep dept_id |
| application → drive → job → company → industry | **placement_drives**, **job_profiles**, **companies** each hold their own facts |
| drive → job → package, location | package/location stored once in job_profiles |
| offer → application → student | offers reference application_id only |
| user → student details | users holds credentials only; students hold academic data |
| drive → min_cgpa, max_backlogs | **eligibility_criteria** (1:1 with drive) keeps criteria separate from scheduling |

## Result

All 17 tables are in 3NF and, as shown in `Functional_Dependencies.md`, every determinant is a candidate
key, so they also satisfy BCNF.

## Design notes

- `students.cgpa` is the official cumulative CGPA from the registrar. It is not recalculated from
  `academic_records`, because records may be incomplete (for example transfer students), and eligibility
  must use the official value. It is therefore an independent attribute, not a derived one.
- `offers.package_lpa` is stored separately from `job_profiles.package_lpa` because the final offered
  package can differ from the advertised package; it is a fact about the offer.
- Application status (APPLIED / SHORTLISTED / SELECTED / REJECTED) is a workflow state set by the officer
  and synchronised from round results by the service layer inside the same transaction.
