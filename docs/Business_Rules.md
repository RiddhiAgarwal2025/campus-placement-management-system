# Business Rules and Where They Are Enforced

Layers: **DB** = constraint in the schema, **TRG** = trigger, **SVC** = Java service layer (inside a JDBC
transaction where noted), **UI** = form validation message. Cross-table rules are never claimed to be
enforced by CHECK constraints.

| # | Rule | Enforced by |
|---|---|---|
| 1 | Student ID is unique | DB primary key; SVC pre-check with friendly message |
| 2 | Student email is unique | DB `uq_students_email`; SVC email format check |
| 3 | Every student belongs to a department | DB NOT NULL + FK; SVC "Department is required" |
| 4 | CGPA between 0 and 10 | DB `chk_students_cgpa`; SVC validation |
| 5 | Semester 1–10, SGPA 0–10, marks 0–100 | DB checks on academic_records; SVC validation |
| 6 | One academic record per student per semester | DB UNIQUE (student_id, semester) |
| 7 | Backlogs ≥ 0 | DB unsigned + `chk_students_backlogs`; SVC |
| 8 | Graduation year 2000–2100 | DB check; SVC |
| 9 | Skills stored as rows, not lists | Schema (student_skills, job_profile_skills, eligibility_skills) |
| 10 | Company names are unique | DB `uq_companies_name` |
| 11 | A company cannot have two job profiles with the same position | DB UNIQUE (company_id, position) |
| 12 | Package > 0 | DB checks on job_profiles and offers; SVC |
| 13 | Deadline on or before drive date | DB `chk_drive_dates`; SVC |
| 14 | Drive status ∈ UPCOMING, OPEN, CLOSED, COMPLETED | DB ENUM |
| 15 | Each drive has exactly one criteria row | DB UNIQUE drive_id; SVC creates drive and criteria in one transaction |
| 16 | Eligibility = CGPA ≥ min, backlogs ≤ max, batch matches (if set), department allowed (if restricted), all required skills held | SVC `EligibilityService.evaluate` (single engine), mirrored by view `vw_drive_eligible_students` |
| 17 | Apply only if drive is OPEN | SVC application transaction (drive row locked `FOR UPDATE`) |
| 18 | Apply only on or before the deadline | SVC application transaction |
| 19 | Apply only if eligible; reasons are shown | SVC application transaction (same engine) |
| 20 | One application per student per drive | DB UNIQUE (student_id, drive_id); SVC duplicate check in transaction |
| 21 | Students apply only for themselves | SVC session check |
| 22 | Withdraw only while APPLIED and before deadline of an open drive | SVC |
| 23 | Round sequence unique within a drive | DB UNIQUE (drive_id, sequence_no) |
| 24 | Result only for an applicant of the same drive | TRG `trg_round_results_bi`; SVC |
| 25 | Result in round N only if PASS in the previous round | TRG `trg_round_results_bi`; SVC |
| 26 | A PASS cannot change to FAIL while later results exist; earlier results cannot be deleted first | TRG `trg_round_results_bu`, `trg_round_results_bd` |
| 27 | Round order cannot change / round cannot be deleted once results exist | TRG `trg_selection_rounds_bu`, `trg_selection_rounds_bd` |
| 28 | Application status follows results: any FAIL → REJECTED, all rounds PASS → SELECTED, some PASS → SHORTLISTED | SVC `SelectionService` (same transaction as the result) |
| 29 | Manual SELECTED requires all rounds passed; no manual change after an offer | SVC `ApplicationService.changeStatus`; TRG `trg_applications_bu` |
| 30 | Offer only for SELECTED applications | TRG `trg_offers_bi`; SVC transaction |
| 31 | At most one offer per application | DB `uq_offer_application`; SVC check |
| 32 | Offer status ∈ PENDING, ACCEPTED, REJECTED; responded_at set iff responded | DB ENUM + `chk_offer_response` |
| 33 | Joining date ≥ offer date | DB `chk_offer_dates`; SVC |
| 34 | Only the owning student responds; a response is final | SVC transaction with `FOR UPDATE`; TRG `trg_offers_bu`; procedure `sp_respond_offer` |
| 35 | A student may accept only one offer | SVC transaction; TRG `trg_offers_bu` |
| 36 | Officers edit/withdraw only PENDING offers | SVC |
| 37 | Students cannot use officer functions | UI (separate navigation) and SVC `Session.requireOfficer()` |
| 38 | Passwords stored as PBKDF2-HMAC-SHA256 hashes | `PasswordUtil`; seed data generated with the same algorithm |
| 39 | Departments, skills, companies and job profiles in use cannot be deleted | DB FK RESTRICT; SVC pre-checks with explanations |
