-- 07: Placement reports. Each report starts with a "-- @report" marker line
-- (used by the desktop application, which loads these exact queries).
USE campus_placement;

-- @report Eligible students by drive | Students who satisfy every criterion of each drive (view with EXISTS / NOT EXISTS)
SELECT e.drive_id AS `Drive`, e.company_name AS `Company`, e.position AS `Position`,
       e.student_id AS `Student ID`, e.full_name AS `Student`, e.dept_code AS `Dept`,
       e.cgpa AS `CGPA`, e.backlogs AS `Backlogs`,
       CASE WHEN EXISTS (SELECT 1 FROM applications a WHERE a.drive_id = e.drive_id AND a.student_id = e.student_id)
            THEN 'Yes' ELSE 'No' END AS `Applied`
FROM vw_drive_eligible_students e
ORDER BY e.drive_id, e.cgpa DESC;

-- @report Applications by drive | Application counts per drive, split by status (LEFT JOIN keeps drives without applications)
SELECT d.drive_id AS `Drive`, c.company_name AS `Company`, j.position AS `Position`, d.status AS `Drive Status`,
       COUNT(a.application_id) AS `Applications`,
       SUM(a.status = 'APPLIED') AS `Applied`, SUM(a.status = 'SHORTLISTED') AS `Shortlisted`,
       SUM(a.status = 'SELECTED') AS `Selected`, SUM(a.status = 'REJECTED') AS `Rejected`
FROM placement_drives d
JOIN job_profiles j ON j.job_id = d.job_id
JOIN companies c ON c.company_id = j.company_id
LEFT JOIN applications a ON a.drive_id = d.drive_id
GROUP BY d.drive_id, c.company_name, j.position, d.status
ORDER BY d.drive_id;

-- @report Applications by company | Total drives and applications received by each company
SELECT c.company_name AS `Company`, c.industry AS `Industry`,
       COUNT(DISTINCT d.drive_id) AS `Drives`, COUNT(a.application_id) AS `Applications`,
       COUNT(DISTINCT a.student_id) AS `Distinct Students`
FROM companies c
LEFT JOIN job_profiles j ON j.company_id = c.company_id
LEFT JOIN placement_drives d ON d.job_id = j.job_id
LEFT JOIN applications a ON a.drive_id = d.drive_id
GROUP BY c.company_id, c.company_name, c.industry
ORDER BY `Applications` DESC, c.company_name;

-- @report Applications by student | Every student with their application, shortlist and offer counts
SELECT s.student_id AS `Student ID`, s.full_name AS `Student`, dp.dept_code AS `Dept`, s.cgpa AS `CGPA`,
       COUNT(a.application_id) AS `Applications`,
       SUM(a.status IN ('SHORTLISTED','SELECTED')) AS `Shortlisted/Selected`,
       (SELECT COUNT(*) FROM offers o JOIN applications a2 ON a2.application_id = o.application_id
         WHERE a2.student_id = s.student_id) AS `Offers`
FROM students s
JOIN departments dp ON dp.dept_id = s.dept_id
LEFT JOIN applications a ON a.student_id = s.student_id
GROUP BY s.student_id, s.full_name, dp.dept_code, s.cgpa
ORDER BY `Applications` DESC, s.full_name;

-- @report Shortlisted students | Candidates currently SHORTLISTED or SELECTED, with rounds cleared
SELECT v.student_id AS `Student ID`, v.full_name AS `Student`, v.dept_code AS `Dept`, v.company_name AS `Company`,
       v.position AS `Position`, v.status AS `Status`,
       CONCAT(v.rounds_cleared, ' / ', v.total_rounds) AS `Rounds Cleared`
FROM vw_shortlisted_students v
ORDER BY v.company_name, v.status DESC, v.full_name;

-- @report Round results | Result of every candidate in every selection round, in round order
SELECT c.company_name AS `Company`, j.position AS `Position`, sr.sequence_no AS `Round #`, sr.round_name AS `Round`,
       s.full_name AS `Student`, rr.result AS `Result`, rr.remarks AS `Remarks`
FROM round_results rr
JOIN selection_rounds sr ON sr.round_id = rr.round_id
JOIN applications a ON a.application_id = rr.application_id
JOIN students s ON s.student_id = a.student_id
JOIN placement_drives d ON d.drive_id = sr.drive_id
JOIN job_profiles j ON j.job_id = d.job_id
JOIN companies c ON c.company_id = j.company_id
ORDER BY d.drive_id, sr.sequence_no, s.full_name;

-- @report Offers | All offers issued, with current status
SELECT o.offer_id AS `Offer`, o.full_name AS `Student`, o.dept_code AS `Dept`, o.company_name AS `Company`,
       o.position AS `Position`, o.package_lpa AS `Package (LPA)`, o.offer_date AS `Offer Date`,
       o.joining_date AS `Joining Date`, o.status AS `Status`
FROM vw_student_offers o
ORDER BY o.offer_date DESC, o.offer_id;

-- @report Accepted offers | Offers accepted by students (placed students)
SELECT o.full_name AS `Student`, o.dept_code AS `Dept`, o.company_name AS `Company`, o.position AS `Position`,
       o.package_lpa AS `Package (LPA)`, o.joining_date AS `Joining Date`, o.responded_at AS `Accepted On`
FROM vw_student_offers o
WHERE o.status = 'ACCEPTED'
ORDER BY o.package_lpa DESC;

-- @report Department placement | Placement percentage and average accepted package per department
SELECT dept_code AS `Dept`, dept_name AS `Department`, total_students AS `Students`, students_applied AS `Applied`,
       students_placed AS `Placed`, COALESCE(placement_percentage, 0) AS `Placement %`,
       avg_accepted_package AS `Avg Package (LPA)`
FROM vw_department_placement
ORDER BY placement_percentage DESC, dept_code;

-- @report Company recruitment | Drives, selections, offers and acceptances per company
SELECT company_name AS `Company`, industry AS `Industry`, drives AS `Drives`, applications AS `Applications`,
       selected AS `Selected`, offers AS `Offers`, accepted_offers AS `Accepted`, highest_offer_lpa AS `Highest (LPA)`
FROM vw_company_recruitment
ORDER BY accepted_offers DESC, offers DESC, company_name;

-- @report Overall placement statistics | Institution-wide summary computed with scalar subqueries
SELECT (SELECT COUNT(*) FROM students) AS `Students`,
       (SELECT COUNT(DISTINCT student_id) FROM applications) AS `Students Applied`,
       (SELECT COUNT(DISTINCT a.student_id) FROM offers o JOIN applications a ON a.application_id = o.application_id
         WHERE o.status = 'ACCEPTED') AS `Students Placed`,
       ROUND(100 * (SELECT COUNT(DISTINCT a.student_id) FROM offers o JOIN applications a ON a.application_id = o.application_id
         WHERE o.status = 'ACCEPTED') / NULLIF((SELECT COUNT(*) FROM students), 0), 2) AS `Placement %`,
       (SELECT COUNT(*) FROM placement_drives) AS `Drives`,
       (SELECT COUNT(*) FROM applications) AS `Applications`,
       (SELECT COUNT(*) FROM offers) AS `Offers`,
       (SELECT COUNT(*) FROM offers WHERE status = 'ACCEPTED') AS `Accepted`,
       (SELECT COUNT(*) FROM offers WHERE status = 'PENDING') AS `Pending`,
       (SELECT COUNT(*) FROM offers WHERE status = 'REJECTED') AS `Declined`;

-- @report Average package | Average offered and accepted package per department, plus the institution-wide average
SELECT dp.dept_code AS `Dept`, COUNT(o.offer_id) AS `Offers`,
       ROUND(AVG(o.package_lpa), 2) AS `Avg Offered (LPA)`,
       ROUND(AVG(CASE WHEN o.status = 'ACCEPTED' THEN o.package_lpa END), 2) AS `Avg Accepted (LPA)`
FROM departments dp
JOIN students s ON s.dept_id = dp.dept_id
JOIN applications a ON a.student_id = s.student_id
JOIN offers o ON o.application_id = a.application_id
GROUP BY dp.dept_code
HAVING COUNT(o.offer_id) > 0
UNION ALL
SELECT 'ALL', COUNT(*), ROUND(AVG(package_lpa), 2),
       ROUND(AVG(CASE WHEN status = 'ACCEPTED' THEN package_lpa END), 2)
FROM offers;

-- @report Highest package | Offer(s) carrying the maximum package (nested query)
SELECT o.full_name AS `Student`, o.dept_code AS `Dept`, o.company_name AS `Company`, o.position AS `Position`,
       o.package_lpa AS `Package (LPA)`, o.status AS `Status`
FROM vw_student_offers o
WHERE o.package_lpa = (SELECT MAX(package_lpa) FROM offers);

-- @report Lowest package | Offer(s) carrying the minimum package (nested query)
SELECT o.full_name AS `Student`, o.dept_code AS `Dept`, o.company_name AS `Company`, o.position AS `Position`,
       o.package_lpa AS `Package (LPA)`, o.status AS `Status`
FROM vw_student_offers o
WHERE o.package_lpa = (SELECT MIN(package_lpa) FROM offers);

-- @report Students with multiple applications | Students who applied to more than one drive (GROUP BY ... HAVING)
SELECT s.student_id AS `Student ID`, s.full_name AS `Student`, dp.dept_code AS `Dept`,
       COUNT(*) AS `Applications`,
       GROUP_CONCAT(c.company_name ORDER BY a.applied_at SEPARATOR ', ') AS `Companies`
FROM applications a
JOIN students s ON s.student_id = a.student_id
JOIN departments dp ON dp.dept_id = s.dept_id
JOIN placement_drives d ON d.drive_id = a.drive_id
JOIN job_profiles j ON j.job_id = d.job_id
JOIN companies c ON c.company_id = j.company_id
GROUP BY s.student_id, s.full_name, dp.dept_code
HAVING COUNT(*) > 1
ORDER BY `Applications` DESC, s.full_name;

-- @report Drives with no eligible applicants | Drives where no eligible student has applied (NOT EXISTS)
SELECT d.drive_id AS `Drive`, c.company_name AS `Company`, j.position AS `Position`, d.status AS `Status`,
       d.application_deadline AS `Deadline`,
       (SELECT COUNT(*) FROM vw_drive_eligible_students e WHERE e.drive_id = d.drive_id) AS `Eligible Students`,
       (SELECT COUNT(*) FROM applications a WHERE a.drive_id = d.drive_id) AS `Applications`
FROM placement_drives d
JOIN job_profiles j ON j.job_id = d.job_id
JOIN companies c ON c.company_id = j.company_id
WHERE NOT EXISTS (SELECT 1 FROM applications a
                  JOIN vw_drive_eligible_students e ON e.drive_id = a.drive_id AND e.student_id = a.student_id
                  WHERE a.drive_id = d.drive_id)
ORDER BY d.drive_id;
