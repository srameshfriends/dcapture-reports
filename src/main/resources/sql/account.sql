--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.account (id INTEGER auto_increment, account_number VARCHAR(64),
name VARCHAR(128), category VARCHAR(64), status VARCHAR(32), currency VARCHAR(8), balance DECIMAL,
description VARCHAR(512), PRIMARY KEY (id));
--loadAll
SELECT id, account_number, name, category, status, currency, balance, description FROM entity.account;
--insertAccount
INSERT INTO entity.account (account_number, name, category, status, currency, balance, description)
VALUES(?,?,?,?,?,?,?);
--deleteAccount
DELETE FROM entity.account WHERE account_number = ?;
--updateStatus
UPDATE entity.account SET status = ? WHERE account_number = ?;
--