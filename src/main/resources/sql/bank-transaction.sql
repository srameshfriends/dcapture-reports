--
--
--createBankTransaction
CREATE TABLE IF NOT EXISTS entity.bank_transaction (id INTEGER auto_increment, bank VARCHAR(64),
transaction_date DATE, transaction_index INTEGER, transaction_code VARCHAR(64), description VARCHAR(512),
currency VARCHAR(8), credit_amount DECIMAL, debit_amount DECIMAL, status VARCHAR(32), PRIMARY KEY (id));
--loadAll
SELECT id, bank, transaction_date, transaction_index, transaction_code, description, currency,
credit_amount, debit_amount, status FROM entity.bank_transaction ORDER BY bank, transaction_date, transaction_index;
--findIdList
SELECT id FROM entity.bank_transaction;
--insertBankTransaction
INSERT INTO entity.bank_transaction (bank, transaction_date, transaction_index, transaction_code, description,
currency, credit_amount, debit_amount, status) VALUES(?,?,?,?,?,?,?,?,?);
--deleteBankTransaction
DELETE FROM entity.bank_transaction WHERE id = ?;
--updateBankTransaction
UPDATE entity.bank_transaction SET bank = ?, transaction_date = ?, transaction_index = ?, transaction_code = ?,
description = ?, currency = ?, credit_amount = ?, debit_amount = ? WHERE id = ?;
--updateStatus
UPDATE entity.bank_transaction SET status = ? WHERE id = ?;
--