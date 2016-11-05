--
--
--createSystemSettingTable
CREATE TABLE IF NOT EXISTS entity.system_setting (code VARCHAR(8),
group_code VARCHAR(8), name VARCHAR(128), text_value VARCHAR(256), decimal_value DECIMAL,
date_value DATE, bool_value BOOLEAN, PRIMARY KEY (code));
--loadAll
SELECT code, group_code, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 ORDER BY code;
--findByCode
SELECT code, group_code, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 WHERE code = ?;
--findByCodeArray
SELECT code, group_code, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 WHERE code IN ($code) ORDER BY code;
--findByGroupCode
SELECT code, group_code, name, text_value, decimal_value, date_value, bool_value FROM entity.system_setting
 WHERE group_code = ? ORDER BY code;
--insertSystemSetting
INSERT INTO entity.system_setting (code, group_code, name, text_value, decimal_value, date_value, bool_value)
VALUES(?,?,?,?,?,?,?);
--deleteSystemSetting
DELETE FROM entity.system_setting WHERE code = ?;
--updateValue
UPDATE entity.system_setting SET text_value = ?, decimal_value = ?, date_value = ?, bool_value = ? WHERE code = ?;
--