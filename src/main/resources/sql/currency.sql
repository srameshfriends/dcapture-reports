--
--
--createTable
CREATE TABLE IF NOT EXISTS entity.currency (code VARCHAR(8),
 name VARCHAR(128), decimal_precision INTEGER, symbol VARCHAR(8), status VARCHAR(32), PRIMARY KEY (code));
--loadAll
SELECT code, name, decimal_precision, symbol, status FROM entity.currency ORDER BY code;
--findByCode
SELECT code, name, decimal_precision, symbol, status FROM entity.currency WHERE code = ?;
--findCodeList
SELECT code FROM entity.currency ORDER BY code;
--insertCurrency
INSERT INTO entity.currency (code, name, decimal_precision, symbol, status) VALUES(?,?,?,?,?);
--deleteCurrency
DELETE FROM entity.currency WHERE code = ?;
--updateCurrency
UPDATE entity.currency SET name = ?, decimal_precision = ?, symbol = ? WHERE code = ?;
--updateStatus
UPDATE entity.currency SET status = ? WHERE code = ?;
--searchCurrency
SELECT code, name, decimal_precision, symbol, status FROM entity.currency WHERE status IN ($status) $searchText;
--