--
--
--create
CREATE TABLE IF NOT EXISTS entity.application_user (
code VARCHAR(8), full_name VARCHAR(128), password VARCHAR(512), PRIMARY KEY (code));
--loadAll
SELECT code, full_name, password FROM entity.application_user ORDER BY code;
--insert
INSERT INTO entity.application_user (code, full_name, password) VALUES(?,?,?);
--update
UPDATE entity.application_user SET code = ?, full_name = ?, password = ? WHERE code = ?;
--totalRowCount
SELECT COUNT(code) FROM entity.application_user;
--