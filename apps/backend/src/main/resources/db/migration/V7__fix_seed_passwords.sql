-- Passwords were seeded with an incorrect BCrypt hash in V2.
-- Correct hashes below (bcrypt cost 10):
--   student1/student2 → Student@123
--   vendor1/vendor2   → Vendor@123
--   admin1            → Admin@123

UPDATE users
SET password_hash = '$2b$10$Y0Cex/sUNIM75HjX1.nCOeLakqTEPT6hkk4ntfkxGIy/qVhpMWwb2'
WHERE username IN ('student1', 'student2');

UPDATE users
SET password_hash = '$2b$10$j5H4vYKn2Fl2vBVyPFugA.a3vlHzKq8afSaYc9Z8IrdDEiHZ/PdOm'
WHERE username IN ('vendor1', 'vendor2');

UPDATE users
SET password_hash = '$2b$10$kpdsI5lcJH6l4HWzYI7FLef/MT9.JFNOsNbxcKrBJRzhC5UZvcj46'
WHERE username = 'admin1';
