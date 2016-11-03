--
--
--createSystemSettingTable
CREATE TABLE IF NOT EXISTS entity.system_setting (code VARCHAR(8),
setting_type VARCHAR(8), name VARCHAR(128), text_value VARCHAR(256), decimal_value DECIMAL,
date_value DATE, bool_value BOOLEAN, PRIMARY KEY (code));
--loadAll
SELECT code, setting_type, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 ORDER BY code;
--findByCode
SELECT code, setting_type, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 WHERE code = ?;
--insertSystemSetting
INSERT INTO entity.system_setting (code, setting_type, name, text_value, decimal_value, date_value, bool_value)
VALUES(?,?,?,?,?,?,?);
--deleteSystemSetting
DELETE FROM entity.system_setting WHERE code = ?;
--updateValue
UPDATE entity.system_setting SET text_value = ?, decimal_value = ?, date_value = ?, bool_value = ? WHERE code = ?;
--