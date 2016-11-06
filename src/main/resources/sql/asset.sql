--
--
--createAssetTable
CREATE TABLE IF NOT EXISTS entity.asset (code VARCHAR(8),
 name VARCHAR(64), description VARCHAR(512), asset_type VARCHAR(32), start_date DATE, end_date DATE,
 currency VARCHAR(8), cost DECIMAL, status VARCHAR(16), units DECIMAL, reference_number VARCHAR(32),
 category VARCHAR(32), PRIMARY KEY (code));
--foreignKeyCurrency
ALTER TABLE entity.asset ADD FOREIGN KEY (currency) REFERENCES entity.currency(code);
--loadAll
SELECT code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
 category FROM entity.asset;
--findCodeList
SELECT code FROM entity.asset;
--insertAsset
INSERT INTO entity.asset (code, name, description, asset_type, start_date, end_date, currency, cost, status, units,
reference_number, category) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);
--deleteAsset
DELETE FROM entity.asset WHERE code = ?;
--updateAsset
UPDATE entity.asset SET name = ?, description = ?, asset_type = ?, start_date = ?, end_date = ?,
 currency = ?, cost = ?, status = ?, units = ?, reference_number = ?, category = ? WHERE code = ?;
--updateStatus
UPDATE entity.asset SET status = ? WHERE code = ?;
--