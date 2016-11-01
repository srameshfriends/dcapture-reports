--
--
--createIncomeCategory
CREATE TABLE IF NOT EXISTS entity.income_category (code VARCHAR(8),
name VARCHAR(128), description VARCHAR(512), status VARCHAR(32), PRIMARY KEY (code));
--loadAll
SELECT code, name, description, status FROM entity.income_category ORDER BY code;
--findCodeList
SELECT code FROM entity.income_category;
--insertIncomeCategory
INSERT INTO entity.income_category (code, name, description, status) VALUES(?,?,?,?);
--deleteIncomeCategory
DELETE FROM entity.income_category WHERE code = ?;
--updateIncomeCategory
UPDATE entity.income_category SET name = ?, description = ? WHERE code = ?;
--updateStatus
UPDATE entity.income_category SET status = ? WHERE code = ?;
--searchIncomeCategory
SELECT code, name, description, status FROM entity.income_category WHERE status IN ($status) $searchText;
--