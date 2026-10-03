-- 06: Triggers and stored procedures for cross-table business rules
USE campus_placement;

DROP TRIGGER IF EXISTS trg_round_results_bi;
DROP TRIGGER IF EXISTS trg_round_results_bu;
DROP TRIGGER IF EXISTS trg_round_results_bd;
DROP TRIGGER IF EXISTS trg_selection_rounds_bu;
DROP TRIGGER IF EXISTS trg_selection_rounds_bd;
DROP TRIGGER IF EXISTS trg_offers_bi;
DROP TRIGGER IF EXISTS trg_offers_bu;
DROP TRIGGER IF EXISTS trg_applications_bi;
DROP TRIGGER IF EXISTS trg_applications_bu;
DROP PROCEDURE IF EXISTS sp_respond_offer;
DROP PROCEDURE IF EXISTS sp_drive_eligible_students;

DELIMITER $$

-- A result may only be recorded for a candidate of the same drive who PASSED the previous round.
CREATE TRIGGER trg_round_results_bi BEFORE INSERT ON round_results
FOR EACH ROW
BEGIN
    DECLARE v_round_drive INT;
    DECLARE v_seq INT;
    DECLARE v_app_drive INT;
    DECLARE v_prev_seq INT;
    DECLARE v_prev_result VARCHAR(4);

    SELECT drive_id, sequence_no INTO v_round_drive, v_seq FROM selection_rounds WHERE round_id = NEW.round_id;
    SELECT drive_id INTO v_app_drive FROM applications WHERE application_id = NEW.application_id;

    IF v_round_drive IS NULL OR v_app_drive IS NULL OR v_round_drive <> v_app_drive THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: the candidate did not apply to this drive.';
    END IF;

    SELECT MAX(sequence_no) INTO v_prev_seq FROM selection_rounds
     WHERE drive_id = v_round_drive AND sequence_no < v_seq;

    IF v_prev_seq IS NOT NULL THEN
        SELECT rr.result INTO v_prev_result
          FROM selection_rounds sr
          JOIN round_results rr ON rr.round_id = sr.round_id AND rr.application_id = NEW.application_id
         WHERE sr.drive_id = v_round_drive AND sr.sequence_no = v_prev_seq;
        IF v_prev_result IS NULL OR v_prev_result <> 'PASS' THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: the candidate has not passed the previous round.';
        END IF;
    END IF;
END$$

-- A PASS cannot become FAIL while later-round results exist.
CREATE TRIGGER trg_round_results_bu BEFORE UPDATE ON round_results
FOR EACH ROW
BEGIN
    IF NEW.round_id <> OLD.round_id OR NEW.application_id <> OLD.application_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: a result cannot be moved to another round or candidate.';
    END IF;
    IF NEW.result = 'FAIL' AND OLD.result = 'PASS' AND EXISTS (
        SELECT 1 FROM round_results rr
        JOIN selection_rounds later ON later.round_id = rr.round_id
        JOIN selection_rounds cur   ON cur.round_id = OLD.round_id
        WHERE rr.application_id = OLD.application_id
          AND later.drive_id = cur.drive_id AND later.sequence_no > cur.sequence_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: remove later-round results before changing this result to FAIL.';
    END IF;
END$$

CREATE TRIGGER trg_round_results_bd BEFORE DELETE ON round_results
FOR EACH ROW
BEGIN
    IF EXISTS (
        SELECT 1 FROM round_results rr
        JOIN selection_rounds later ON later.round_id = rr.round_id
        JOIN selection_rounds cur   ON cur.round_id = OLD.round_id
        WHERE rr.application_id = OLD.application_id
          AND later.drive_id = cur.drive_id AND later.sequence_no > cur.sequence_no) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Invalid round progression: remove later-round results first.';
    END IF;
END$$

-- Round order cannot be changed once results are recorded for that round.
CREATE TRIGGER trg_selection_rounds_bu BEFORE UPDATE ON selection_rounds
FOR EACH ROW
BEGIN
    IF (NEW.sequence_no <> OLD.sequence_no OR NEW.drive_id <> OLD.drive_id)
       AND EXISTS (SELECT 1 FROM round_results WHERE round_id = OLD.round_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Round order cannot be changed after results have been recorded.';
    END IF;
END$$

CREATE TRIGGER trg_selection_rounds_bd BEFORE DELETE ON selection_rounds
FOR EACH ROW
BEGIN
    IF EXISTS (SELECT 1 FROM round_results WHERE round_id = OLD.round_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This round has recorded results and cannot be deleted.';
    END IF;
END$$

-- Offers only for SELECTED applications.
CREATE TRIGGER trg_offers_bi BEFORE INSERT ON offers
FOR EACH ROW
BEGIN
    DECLARE v_status VARCHAR(12);
    SELECT status INTO v_status FROM applications WHERE application_id = NEW.application_id;
    IF v_status IS NULL OR v_status <> 'SELECTED' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An offer can only be issued to a SELECTED candidate.';
    END IF;
END$$

-- Once accepted or rejected, an offer's decision is final; a student can accept only one offer.
CREATE TRIGGER trg_offers_bu BEFORE UPDATE ON offers
FOR EACH ROW
BEGIN
    IF OLD.status <> 'PENDING' AND NEW.status <> OLD.status THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This offer has already been responded to.';
    END IF;
    IF NEW.application_id <> OLD.application_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An offer cannot be moved to another application.';
    END IF;
    IF NEW.status = 'ACCEPTED' AND OLD.status = 'PENDING' AND EXISTS (
        SELECT 1 FROM offers o
        JOIN applications a  ON a.application_id = o.application_id
        JOIN applications me ON me.application_id = NEW.application_id
        WHERE a.student_id = me.student_id AND o.status = 'ACCEPTED' AND o.offer_id <> NEW.offer_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'The student has already accepted another offer.';
    END IF;
END$$

-- New applications always start as APPLIED.
CREATE TRIGGER trg_applications_bi BEFORE INSERT ON applications
FOR EACH ROW
BEGIN
    IF NEW.status IS NULL THEN
        SET NEW.status = 'APPLIED';
    END IF;
END$$

-- An application holding an offer must stay SELECTED.
CREATE TRIGGER trg_applications_bu BEFORE UPDATE ON applications
FOR EACH ROW
BEGIN
    IF OLD.status = 'SELECTED' AND NEW.status <> 'SELECTED'
       AND EXISTS (SELECT 1 FROM offers WHERE application_id = OLD.application_id) THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This application already has an offer; its status must remain SELECTED.';
    END IF;
    IF NEW.student_id <> OLD.student_id OR NEW.drive_id <> OLD.drive_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'An application cannot be moved to another student or drive.';
    END IF;
END$$

-- Atomic offer response (the Java application performs the same steps in a JDBC transaction).
CREATE PROCEDURE sp_respond_offer(IN p_offer_id INT, IN p_student_id VARCHAR(20), IN p_decision VARCHAR(10))
BEGIN
    DECLARE v_owner VARCHAR(20);
    DECLARE v_status VARCHAR(10);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    IF p_decision NOT IN ('ACCEPTED','REJECTED') THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Decision must be ACCEPTED or REJECTED.';
    END IF;

    START TRANSACTION;
    SELECT a.student_id, o.status INTO v_owner, v_status
      FROM offers o JOIN applications a ON a.application_id = o.application_id
     WHERE o.offer_id = p_offer_id FOR UPDATE;

    IF v_owner IS NULL OR v_owner <> p_student_id THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Offer not found for this student.';
    END IF;
    IF v_status <> 'PENDING' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'This offer has already been responded to.';
    END IF;

    UPDATE offers SET status = p_decision, responded_at = CURRENT_TIMESTAMP WHERE offer_id = p_offer_id;
    COMMIT;
END$$

CREATE PROCEDURE sp_drive_eligible_students(IN p_drive_id INT)
BEGIN
    SELECT * FROM vw_drive_eligible_students WHERE drive_id = p_drive_id ORDER BY cgpa DESC;
END$$

DELIMITER ;
