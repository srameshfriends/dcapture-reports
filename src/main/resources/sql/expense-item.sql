--
--
--createExpenseItem
CREATE TABLE IF NOT EXISTS entity.expense_item (id INTEGER auto_increment, expense_date DATE,
reference_number VARCHAR(128), description VARCHAR(512), currency VARCHAR(8), amount DECIMAL, status VARCHAR(32),
 PRIMARY KEY (id));
--loadAll
SELECT id, expense_date, reference_number, description, currency, amount, status FROM entity.expense_item ORDER BY expense_date;
--findIdList
SELECT id FROM entity.expense_item;
--insertExpenseItem
INSERT INTO entity.expense_item (expense_date, reference_number, description, currency, amount, status) VALUES(?,?,?,?,?,?);
--deleteExpenseItem
DELETE FROM entity.expense_item WHERE id = ?;
--updateExpenseItem
UPDATE entity.expense_item SET expense_date = ?, reference_number = ?, description = ?, currency = ?, amount = ? WHERE id = ?;
--updateStatus
UPDATE entity.expense_item SET status = ? WHERE id = ?;
--