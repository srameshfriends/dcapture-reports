--
--
--createExpenseItem
CREATE TABLE IF NOT EXISTS entity.expense_item (expense_code VARCHAR(8), expense_date DATE,
reference_number VARCHAR(128), description VARCHAR(512), currency VARCHAR(8), amount DECIMAL, status VARCHAR(32),
expense_category VARCHAR(8), expense_account VARCHAR(8), PRIMARY KEY (expense_code));
--foreignKeyCurrency
ALTER TABLE entity.expense_item ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyExpenseCategory
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_category) REFERENCES entity.expense_category(code);
--foreignKeyExpenseAccount
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_account) REFERENCES entity.account(account_number);
--loadAll
SELECT expense_code, expense_date, reference_number, description, currency, amount, status, expense_category,
 expense_account FROM entity.expense_item ORDER BY expense_date, expense_code;
--findExpenseCodeList
SELECT expense_code FROM entity.expense_item;
--insertExpenseItem
INSERT INTO entity.expense_item (expense_code, expense_date, reference_number, description, currency, amount, status)
 VALUES(?,?,?,?,?,?,?);
--deleteExpenseItem
DELETE FROM entity.expense_item WHERE expense_code = ?;
--updateStatus
UPDATE entity.expense_item SET status = ? WHERE expense_code = ?;
--updateExpenseAccount
UPDATE entity.expense_item SET expense_account = ? WHERE expense_code = ?;
--updateExpenseCategory
UPDATE entity.expense_item SET expense_category = ? WHERE expense_code = ?;
--