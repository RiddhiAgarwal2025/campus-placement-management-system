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
