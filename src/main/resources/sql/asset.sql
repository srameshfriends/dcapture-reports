--
--
--createAssetTable
CREATE TABLE IF NOT EXISTS entity.asset (id INTEGER auto_increment, code VARCHAR(32),
 name VARCHAR(64), description VARCHAR(512), asset_type VARCHAR(32), start_date DATE, end_date DATE,
 currency VARCHAR(8), cost DECIMAL, status VARCHAR(32), units DECIMAL, reference_number VARCHAR(32),
 category VARCHAR(32), PRIMARY KEY (id));
--loadAll
SELECT id, code, name, description, asset_type, start_date, end_date, currency, cost, status, units, reference_number,
 category FROM entity.asset;
--findCodeList
SELECT code FROM entity.asset;
--insertAsset
INSERT INTO entity.asset (code, name, description, asset_type, start_date, end_date, currency, cost, status, units,
reference_number, category) VALUES(?,?,?,?,?,?,?,?,?,?,?,?);
--deleteAsset
DELETE FROM entity.asset WHERE code = ?;
--updateAsset
UPDATE entity.asset SET code = ?, name = ?, description = ?, asset_type = ?, start_date = ?, end_date = ?,
 currency = ?, cost = ?, status = ?, units = ?, reference_number = ?, category = ? WHERE code = ?;
--updateStatus
UPDATE entity.asset SET status = ? WHERE code = ?;
--