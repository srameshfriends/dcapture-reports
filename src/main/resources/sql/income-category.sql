--
--
--createIncomeCategory
CREATE TABLE IF NOT EXISTS entity.income_category (id INTEGER auto_increment, code VARCHAR(64),
name VARCHAR(128), status VARCHAR(32), currency VARCHAR(8), credit_account VARCHAR(64),
debit_account VARCHAR(64), description VARCHAR(512), PRIMARY KEY (id));
--loadAll
SELECT id, code, name, status, currency, credit_account, debit_account, description FROM entity.income_category
 ORDER BY code;
--findCodeList
SELECT code FROM entity.income_category;
--insertIncomeCategory
INSERT INTO entity.income_category (code, name, status, currency, credit_account, debit_account, description)
VALUES(?,?,?,?,?,?,?);
--deleteIncomeCategory
DELETE FROM entity.income_category WHERE code = ?;
--updateIncomeCategory
UPDATE entity.income_category SET code = ?, name = ?, currency = ?, credit_account = ?, debit_account = ?, description = ?
 WHERE code = ?;
--updateStatus
UPDATE entity.income_category SET status = ? WHERE code = ?;
--