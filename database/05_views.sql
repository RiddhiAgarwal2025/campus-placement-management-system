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
