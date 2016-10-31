--
--
--createIncomeItem
CREATE TABLE IF NOT EXISTS entity.income_item (id INTEGER auto_increment, income_date DATE,
description VARCHAR(512), currency VARCHAR(8), amount DECIMAL, status VARCHAR(32),
income_category VARCHAR(8), income_account VARCHAR(8), PRIMARY KEY (id));
--foreignKeyCurrency
ALTER TABLE entity.income_item ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyIncomeCategory
ALTER TABLE entity.income_item ADD FOREIGN KEY (income_category) REFERENCES entity.income_category(code);
--foreignKeyIncomeAccount
ALTER TABLE entity.income_item ADD FOREIGN KEY (income_account) REFERENCES entity.account(account_number);
--loadAll
SELECT id, income_date, description, currency, amount, status FROM entity.income_item ORDER BY income_date;
--findIdList
SELECT id FROM entity.income_item;
--insertIncomeItem
INSERT INTO entity.income_item (income_date, description, currency, amount, status) VALUES(?,?,?,?,?);
--deleteIncomeItem
DELETE FROM entity.income_item WHERE id = ?;
--updateIncomeItem
UPDATE entity.income_item SET income_date = ?, description = ?, currency = ?, amount = ? WHERE id = ?;
--updateStatus
UPDATE entity.income_item SET status = ? WHERE id = ?;
--