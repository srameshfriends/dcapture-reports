--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.account (code VARCHAR(8),
name VARCHAR(128), account_type VARCHAR(32), status VARCHAR(32), currency VARCHAR(8), balance DECIMAL,
description VARCHAR(512), PRIMARY KEY (code));
--foreignKeyCurrency
ALTER TABLE entity.account ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--loadAll
SELECT code, name, account_type, status, currency, balance, description FROM entity.account
 ORDER BY code;
--findCodeList
SELECT code FROM entity.account;
--insertAccount
INSERT INTO entity.account (code, name, account_type, status, currency, balance, description)
VALUES(?,?,?,?,?,?,?);
--deleteAccount
DELETE FROM entity.account WHERE code = ?;
--updateStatus
UPDATE entity.account SET status = ? WHERE code = ?;
--updateCurrency
UPDATE entity.account SET currency = ? WHERE code = ?;
--updateAccountType
UPDATE entity.account SET account_type = ? WHERE code = ?;
--findByAccountTypes
SELECT code, name, account_type, status, currency, balance, description FROM entity.account
 WHERE account_type IN ($account_type) $searchText ORDER BY code;
--searchAccount
SELECT code, name, account_type, status, currency, balance, description FROM entity.account
 WHERE status IN ($status) AND account_type IN ($accountType) $searchText;
--findByCode
SELECT code, name, account_type, status, currency, balance, description FROM entity.account
 WHERE code = ?;
--