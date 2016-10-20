--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.account (id INTEGER auto_increment, account_number VARCHAR(64) NOT NULL,
name VARCHAR(256) NOT NULL, PRIMARY KEY (id));
--loadAll
SELECT id, account_number, name FROM entity.account;
--insertAccount
INSERT INTO entity.account (account_number, name) VALUES(?,?);
--
