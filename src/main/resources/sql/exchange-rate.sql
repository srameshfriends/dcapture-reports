--
--
--createExchangeRate
CREATE TABLE IF NOT EXISTS entity.exchange_rate (code VARCHAR(8), fetch_from VARCHAR(4), asof_date DATE,
currency VARCHAR(8), exchange_currency VARCHAR(8), unit INTEGER, selling_rate DECIMAL, buying_rate DECIMAL,
status VARCHAR(16), PRIMARY KEY (code));
--foreignKeyCurrency
ALTER TABLE entity.exchange_rate ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyExchangeCurrency
ALTER TABLE entity.exchange_rate ADD FOREIGN KEY (exchange_currency) REFERENCES entity.currency(code);
--loadAll
SELECT code, fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate, status
 FROM entity.exchange_rate ORDER BY asof_date, exchange_currency;
--findCodeList
SELECT code FROM entity.exchange_rate;
--insertExchangeRate
INSERT INTO entity.exchange_rate (code, fetch_from, asof_date, currency, exchange_currency, unit, selling_rate,
buying_rate, status) VALUES(?,?,?,?,?,?,?,?,?);
--deleteExchangeRate
DELETE FROM entity.exchange_rate WHERE code = ?;
--updateExchangeRate
UPDATE entity.exchange_rate SET unit = ?, selling_rate = ? , buying_rate = ? WHERE code = ?;
--updateStatus
UPDATE entity.exchange_rate SET status = ? WHERE code = ?;
--findLastSequence
SELECT code FROM entity.exchange_rate ORDER BY code DESC limit 1;
--