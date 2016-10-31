--
--
--createExpenseItem
CREATE TABLE IF NOT EXISTS entity.expense_item (id INTEGER auto_increment, expense_date DATE,
reference_number VARCHAR(128), description VARCHAR(512), currency VARCHAR(8), amount DECIMAL, status VARCHAR(32),
expense_category VARCHAR(8), expense_account VARCHAR(8), income_account VARCHAR(8), PRIMARY KEY (id));
--foreignKeyCurrency
ALTER TABLE entity.expense_item ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyExpenseCategory
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_category) REFERENCES entity.expense_category(code);
--foreignKeyExpenseAccount
ALTER TABLE entity.expense_item ADD FOREIGN KEY (expense_account) REFERENCES entity.account(account_number);
--foreignKeyIncomeAccount
ALTER TABLE entity.expense_item ADD FOREIGN KEY (income_account) REFERENCES entity.account(account_number);
--loadAll
SELECT id, expense_date, reference_number, description, currency, amount, status, linked, expense_category,
 expense_account, income_account FROM entity.expense_item ORDER BY expense_date;
--findIdList
SELECT id FROM entity.expense_item;
--insertExpenseItem
INSERT INTO entity.expense_item (expense_date, reference_number, description, currency, amount, status) VALUES(?,?,?,?,?,?);
--deleteExpenseItem
DELETE FROM entity.expense_item WHERE id = ?;
--updateExpenseItem
UPDATE entity.expense_item SET expense_date = ?, reference_number = ?, description = ?, currency = ?, amount = ?
 WHERE id = ?;
--updateStatus
UPDATE entity.expense_item SET status = ? WHERE id = ?;
--assignAccounts
UPDATE entity.expense_item SET expense_account = ?, income_account = ?, linked = ? WHERE id = ?;
--unAssignAccounts
UPDATE entity.expense_item SET expense_account = ?, income_account = ? WHERE id = ?;
--assignExpenseCategory
UPDATE entity.expense_item SET expense_category = ? WHERE id = ?;
--unAssignExpenseCategory
UPDATE entity.expense_item SET expense_category = ? WHERE id = ?;
--