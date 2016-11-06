--
--
--createExchangeRate
CREATE TABLE IF NOT EXISTS entity.exchange_rate (id INTEGER auto_increment, fetch_from VARCHAR(64), asof_date DATE,
currency VARCHAR(8), exchange_currency VARCHAR(8), unit INTEGER, selling_rate DECIMAL, buying_rate DECIMAL,
status VARCHAR(16), PRIMARY KEY (id));
--foreignKeyCurrency
ALTER TABLE entity.exchange_rate ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--foreignKeyExchangeCurrency
ALTER TABLE entity.exchange_rate ADD FOREIGN KEY (exchange_currency) REFERENCES entity.currency(code);
--loadAll
SELECT id, fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate, status
 FROM entity.exchange_rate ORDER BY asof_date, exchange_currency;
--findIdList
SELECT id FROM entity.exchange_rate;
--insertExchangeRate
INSERT INTO entity.exchange_rate (fetch_from, asof_date, currency, exchange_currency, unit, selling_rate, buying_rate,
 status) VALUES(?,?,?,?,?,?,?,?);
--deleteExchangeRate
DELETE FROM entity.exchange_rate WHERE id = ?;
--updateExchangeRate
UPDATE entity.exchange_rate SET fetch_from = ?, asof_date = ?, currency = ?, exchange_currency = ?, unit = ?,
 selling_rate = ?, buying_rate = ?, status = ? WHERE id = ?;
--updateStatus
UPDATE entity.exchange_rate SET status = ? WHERE id = ?;
--