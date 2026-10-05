-- 08: Least-privilege MySQL database user for the application client.
-- In production, the application should connect with a dedicated non-root user
-- that only has the exact privileges required for day-to-day operations.

-- Create the dedicated application user for local connections
CREATE USER IF NOT EXISTS 'placement_app'@'localhost' IDENTIFIED BY 'Placement@123';

-- Create the dedicated application user for remote/container connections
CREATE USER IF NOT EXISTS 'placement_app'@'%' IDENTIFIED BY 'Placement@123';

-- Grant DML (Data Manipulation) and EXECUTE permissions only on campus_placement database
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON campus_placement.* TO 'placement_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON campus_placement.* TO 'placement_app'@'%';

-- Flush privilege cache to apply changes immediately
FLUSH PRIVILEGES;
