--
--
--createExpenseCategory
CREATE TABLE IF NOT EXISTS entity.expense_category (code VARCHAR(8), name VARCHAR(128),
 chartof_accounts VARCHAR(8), description VARCHAR(256), status VARCHAR(32), PRIMARY KEY (code));
--loadAll
SELECT code, name, chartof_accounts, description, status FROM entity.expense_category ORDER BY code;
--findCodeList
SELECT code FROM entity.expense_category ORDER BY code;
--insertExpenseCategory
INSERT INTO entity.expense_category (code, name, chartof_accounts, description, status) VALUES(?,?,?,?,?);
--deleteExpenseCategory
DELETE FROM entity.expense_category WHERE code = ?;
--updateChartOfAccounts
UPDATE entity.expense_category SET chartof_accounts = ? WHERE code = ?;
--updateStatus
UPDATE entity.expense_category SET status = ? WHERE code = ?;
--searchExpenseCategory
SELECT code, name, chartof_accounts, description, status FROM entity.expense_category
 WHERE status IN ($status) $searchText;
--