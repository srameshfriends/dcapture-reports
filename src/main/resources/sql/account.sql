--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.account (id INTEGER auto_increment, account_number VARCHAR(64),
name VARCHAR(128), account_type VARCHAR(32), status VARCHAR(32), currency VARCHAR(8), balance DECIMAL,
description VARCHAR(512), PRIMARY KEY (id));
--loadAll
SELECT id, account_number, name, account_type, status, currency, balance, description FROM entity.account
 ORDER BY account_number;
--findAccountNumberList
SELECT account_number FROM entity.account;
--insertAccount
INSERT INTO entity.account (account_number, name, account_type, status, currency, balance, description)
VALUES(?,?,?,?,?,?,?);
--deleteAccount
DELETE FROM entity.account WHERE account_number = ?;
--updateAccount
UPDATE entity.account SET account_number = ?, name = ?, account_type = ?, currency = ?, description = ?
 WHERE account_number = ?;
--updateStatus
UPDATE entity.account SET status = ? WHERE account_number = ?;
--
--findAccountsByType
SELECT id, account_number, name, account_type, status, currency, balance, description FROM entity.account
 WHERE account_type IN ($account_type) $searchText ORDER BY account_number;
--