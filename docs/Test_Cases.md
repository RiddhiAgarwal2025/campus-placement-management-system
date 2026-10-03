# Test Cases

Run against a freshly loaded `complete_database.sql`. Logins: `officer` / `Officer@123`; students use
their student ID with `Student@123` (for example `21CSE003`).

Status of each case: **Verified** = executed against MySQL 8.0 through the service layer during
development (automated harness), **Manual** = UI step to perform yourself.

## Authentication and roles

| ID | Steps | Expected result | Status |
|---|---|---|---|
| A1 | Sign in as `officer` / `wrong` | "Invalid username or password." | Verified |
| A2 | Sign in as `officer` / `Officer@123` | Officer dashboard with sidebar sections Overview, People, Recruitment, Analytics | Verified |
| A3 | Sign in as `21CSE003` / `Student@123` | Student dashboard; only six student menu items | Verified |
| A4 | As a student, call an officer service (e.g. list all applications) | "Only placement officers can perform this action." | Verified |
| A5 | Student changes password, signs out, signs in with the new one | Login succeeds; old password fails | Verified |
| A6 | Stop MySQL, then sign in | "Cannot connect to the MySQL server…" | Verified |

## Master data

| ID | Steps | Expected result | Status |
|---|---|---|---|
| M1 | Add student with CGPA 11 | "CGPA must be between 0 and 10." | Verified |
| M2 | Add student with email `ananya.rao@university.edu` | "A student with this email address already exists." | Verified |
| M3 | Add student with ID `21CSE001` | "A student with ID 21CSE001 already exists." | Verified |
| M4 | Add student `24CSE099` | Saved; login `24CSE099` / `Student@123` created | Verified |
| M5 | Add semester 1 record twice | Second attempt: "An academic record for this semester already exists." | Verified |
| M6 | Add record with marks 170 | "Marks (%) must be between 0 and 100." | Verified |
| M7 | Assign the same skill twice | "This skill is already assigned to the student." | Verified |
| M8 | Delete department CSE | Refused: department still has students | Verified |
| M9 | Add company with an existing name | "A company with this name already exists." | Verified |
| M10 | Delete a company that has job profiles | Refused with explanation | Verified |
| M11 | Create a drive with deadline after drive date | "The application deadline must be on or before the drive date." | Verified |

## Eligibility and applications

| ID | Steps | Expected result | Status |
|---|---|---|---|
| E1 | Officer → Eligibility → drive #6 (Tessera Cloud) → Check | 3 eligible; others show reasons such as "Missing required skills: Cloud Computing, Linux" | Verified |
| E2 | Student `21CSE003` opens drive #9 | NOT ELIGIBLE with "Minimum CGPA required: 8.50 / Student CGPA: 7.64" | Verified |
| E3 | Eligible student applies to an OPEN drive | Application created with status APPLIED | Verified |
| E4 | Apply again to the same drive | "You have already applied to this drive." | Verified |
| E5 | Apply to a drive whose criteria are not met | "NOT ELIGIBLE" plus reasons; nothing inserted (rollback) | Verified |
| E6 | Apply to CLOSED drive #4 | "This drive is closed…" | Verified |
| E7 | Apply to UPCOMING drive #9 | "Applications for this drive have not opened yet." | Verified |
| E8 | Report 1 vs Eligibility screen for the same drive | Same set of eligible students (view and engine agree) | Verified |

## Selection and offers

| ID | Steps | Expected result | Status |
|---|---|---|---|
| S1 | Add rounds 1 and 2 to a drive; add another round with sequence 2 | "This drive already has a round with that sequence number." | Verified |
| S2 | Record round 2 result before round 1 | "Invalid round progression: … has not passed …" | Verified |
| S3 | PASS round 1 | Application becomes SHORTLISTED | Verified |
| S4 | Issue offer while SHORTLISTED | "Offers can only be issued to SELECTED candidates…" | Verified |
| S5 | PASS final round | Application becomes SELECTED | Verified |
| S6 | Change round 1 to FAIL after round 2 PASS | Refused by trigger | Verified |
| S7 | Issue offer to the SELECTED candidate; issue again | First succeeds; second "An offer has already been issued for this application." | Verified |
| S8 | Change status of an application that has an offer | Refused | Verified |
| S9 | Student accepts the offer | Status ACCEPTED, responded_at set | Verified |
| S10 | Respond again | "This offer has already been accepted." | Verified |
| S11 | Take a student who has already accepted an offer (e.g. `21CSE006`), select them in another drive, issue an offer, and try to accept it as that student | "You have already accepted another offer…" | Verified |

## Reports

| ID | Steps | Expected result | Status |
|---|---|---|---|
| R1 | Officer → Reports → each of the 16 reports | Every report runs and returns rows from the sample data | Verified |
| R2 | Report 16 | Drives #9 and #10 listed (no eligible applicants) | Verified |
| R3 | Export CSV | File opens in a spreadsheet with the same rows | Manual |

## End-to-end workflow (Manual)

1. Officer: add a student, an academic record and a skill (Students → Details).
2. Companies → Add company; Job Profiles → New job profile.
3. Drives → New drive (status OPEN, deadline in the future) with criteria that the new student meets.
4. Eligibility → select the drive → Check eligibility → the student appears as ELIGIBLE.
5. Sign out; sign in as the student → Placement Drives → select the drive → Apply.
6. My Applications shows APPLIED.
7. Officer: Applications shows the new row. Selection → Add round(s) → Mark PASS for each round → status SELECTED.
8. Offers → Issue offer.
9. Student: My Offers → Accept offer.
10. Officer: Dashboard and Reports 8, 9 and 11 include the new placement.
