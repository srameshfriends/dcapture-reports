--
--
--createExpenseCategory
CREATE TABLE IF NOT EXISTS entity.expense_category (id INTEGER auto_increment, code VARCHAR(64),
name VARCHAR(128), status VARCHAR(32), currency VARCHAR(8), debit_account VARCHAR(64),
credit_account VARCHAR(64), description VARCHAR(512), PRIMARY KEY (id));
--loadAll
SELECT id, code, name, status, currency, debit_account, credit_account, description FROM entity.expense_category
 ORDER BY code;
--findCodeList
SELECT code FROM entity.expense_category;
--insertExpenseCategory
INSERT INTO entity.expense_category (code, name, status, currency, debit_account, credit_account, description)
VALUES(?,?,?,?,?,?,?);
--deleteExpenseCategory
DELETE FROM entity.expense_category WHERE code = ?;
--updateExpenseCategory
UPDATE entity.expense_category SET code = ?, name = ?, currency = ?, debit_account = ?, credit_account = ?, description = ?
 WHERE code = ?;
--updateStatus
UPDATE entity.expense_category SET status = ? WHERE code = ?;
--