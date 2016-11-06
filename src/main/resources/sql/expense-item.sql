--
--
--createExpenseItem
CREATE TABLE IF NOT EXISTS entity.expense_item (code VARCHAR(8), expense_date DATE,
reference_number VARCHAR(128), description VARCHAR(512), currency VARCHAR(8), amount DECIMAL, status VARCHAR(16),
expense_category VARCHAR(8), expense_account VARCHAR(8), paid BOOLEAN, PRIMARY KEY (code));
--foreignKeyCurrency
ALTER TABLE entity.expense_item ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyExpenseCategory
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_category) REFERENCES entity.expense_category(code);
--foreignKeyExpenseAccount
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_account) REFERENCES entity.account(code);
--loadAll
SELECT code, expense_date, reference_number, description, currency, amount, status, expense_category,
 expense_account, paid FROM entity.expense_item ORDER BY expense_date, code;
--findExpenseCodeList
SELECT code FROM entity.expense_item;
--insertExpenseItem
INSERT INTO entity.expense_item (code, expense_date, reference_number, description, currency, amount, status,
 expense_category, expense_account, paid) VALUES(?,?,?,?,?,?,?,?,?,?);
--deleteExpenseItem
DELETE FROM entity.expense_item WHERE code = ?;
--updateStatus
UPDATE entity.expense_item SET status = ? WHERE code = ?;
--updateCurrency
UPDATE entity.expense_item SET currency = ? WHERE code = ?;
--updateExpenseAccount
UPDATE entity.expense_item SET expense_account = ? WHERE code = ?;
--updateExpenseCategory
UPDATE entity.expense_item SET expense_category = ? WHERE code = ?;
--findLastSequence
SELECT code FROM entity.expense_item ORDER BY code DESC limit 1;