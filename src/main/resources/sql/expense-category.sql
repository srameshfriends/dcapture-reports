--
--
--createExpenseCategory
CREATE TABLE IF NOT EXISTS entity.expense_category (code VARCHAR(8), name VARCHAR(128),
 description VARCHAR(512), status VARCHAR(32), PRIMARY KEY (code));
--loadAll
SELECT code, name, description, status FROM entity.expense_category ORDER BY code;
--findCodeList
SELECT code FROM entity.expense_category;
--insertExpenseCategory
INSERT INTO entity.expense_category (code, name, description, status) VALUES(?,?,?,?);
--deleteExpenseCategory
DELETE FROM entity.expense_category WHERE code = ?;
--updateExpenseCategory
UPDATE entity.expense_category SET name = ?, description = ? WHERE code = ?;
--updateStatus
UPDATE entity.expense_category SET status = ? WHERE code = ?;
--searchExpenseCategory
  SELECT code, name, description, status FROM entity.expense_category WHERE status IN ($status) $searchText;
--