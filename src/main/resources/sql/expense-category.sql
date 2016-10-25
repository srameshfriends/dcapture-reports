--
--
--createExpenseCategory
CREATE TABLE IF NOT EXISTS entity.expense_category (id INTEGER auto_increment, code VARCHAR(64),
name VARCHAR(128), status VARCHAR(32), currency VARCHAR(8), expense_account VARCHAR(64),
description VARCHAR(512), PRIMARY KEY (id));
--loadAll
SELECT id, code, name, status, currency, expense_account, description FROM entity.expense_category
 ORDER BY code;
--findCodeList
SELECT code FROM entity.expense_category;
--insertExpenseCategory
INSERT INTO entity.expense_category (code, name, status, currency, expense_account, description)
VALUES(?,?,?,?,?,?,?);
--deleteExpenseCategory
DELETE FROM entity.expense_category WHERE code = ?;
--updateExpenseCategory
UPDATE entity.expense_category SET code = ?, name = ?, currency = ?, expense_account = ?, description = ?
 WHERE code = ?;
--updateStatus
UPDATE entity.expense_category SET status = ? WHERE code = ?;
--