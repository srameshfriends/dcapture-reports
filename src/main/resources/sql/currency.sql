--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.currency (id INTEGER auto_increment, code VARCHAR(8),
 name VARCHAR(128), status VARCHAR(32), decimal_precision INTEGER, symbol VARCHAR(8), PRIMARY KEY (id));
--loadAll
SELECT id, code, name, status, decimal_precision, symbol FROM entity.currency;
--findCodeList
SELECT code FROM entity.currency;
--insertCurrency
INSERT INTO entity.currency (code, name, status, decimal_precision, symbol) VALUES(?,?,?,?,?);
--deleteCurrency
DELETE FROM entity.currency WHERE code = ?;
--updateCurrency
UPDATE entity.currency SET code = ?, name = ?, decimal_precision = ?, symbol = ? WHERE code = ?;
--updateStatus
UPDATE entity.currency SET status = ? WHERE code = ?;
--