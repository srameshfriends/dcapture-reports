--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.chartof_accounts (code VARCHAR(8),
name VARCHAR(128), account_type VARCHAR(32), status VARCHAR(32), currency VARCHAR(8), balance DECIMAL,
description VARCHAR(512), PRIMARY KEY (code));
--foreignKeyCurrency
ALTER TABLE entity.chartof_accounts ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--loadAll
SELECT code, name, account_type, status, currency, balance, description FROM entity.chartof_accounts
 ORDER BY code;
--findCodeList
SELECT code FROM entity.chartof_accounts;
--insertAccount
INSERT INTO entity.chartof_accounts (code, name, account_type, status, currency, balance, description)
VALUES(?,?,?,?,?,?,?);
--deleteAccount
DELETE FROM entity.chartof_accounts WHERE code = ?;
--updateStatus
UPDATE entity.chartof_accounts SET status = ? WHERE code = ?;
--updateCurrency
UPDATE entity.chartof_accounts SET currency = ? WHERE code = ?;
--updateAccountType
UPDATE entity.chartof_accounts SET account_type = ? WHERE code = ?;
--findByAccountTypes
SELECT code, name, account_type, status, currency, balance, description FROM entity.chartof_accounts
 WHERE account_type IN ($account_type) $searchText ORDER BY code;
--searchAccount
SELECT code, name, account_type, status, currency, balance, description FROM entity.chartof_accounts
 WHERE status IN ($status) AND account_type IN ($accountType) $searchText;
--findByCode
SELECT code, name, account_type, status, currency, balance, description FROM entity.chartof_accounts
 WHERE code = ?;
--