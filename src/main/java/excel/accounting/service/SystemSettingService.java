package excel.accounting.service;

import excel.accounting.dao.SystemSettingDao;
import excel.accounting.db.*;
import excel.accounting.entity.SystemSetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * System Setting Service
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class SystemSettingService extends AbstractService implements EntityToRowColumns<SystemSetting> {
    private SystemSettingDao currencyDao;

    @Override
    protected String getSqlFileName() {
        return "system-setting";
    }

    private SystemSettingDao getSystemSettingDao() {
        if (currencyDao == null) {
            currencyDao = (SystemSettingDao) getBean("systemSettingDao");
        }
        return currencyDao;
    }

    private void insertSystemSetting(List<SystemSetting> settingList) {
       /* OrmTransaction transaction = createOrmTransaction();
        try {
            for (SystemSetting systemSetting : settingList) {
                transaction.insert(systemSetting);
            }
            transaction.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public void updateValue(List<SystemSetting> settingList) {
        QueryBuilder queryBuilder = getQueryBuilder("updateValue");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (SystemSetting systemSetting : settingList) {
            transaction.addBatch(getColumnsMap("updateValue", systemSetting));
        }
        executeBatch(transaction);
    }

    public void deleteSystemSetting(List<SystemSetting> settingList) {
        QueryBuilder queryBuilder = getQueryBuilder("deleteSystemSetting");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (SystemSetting systemSetting : settingList) {
            transaction.addBatch(getColumnsMap("deleteSystemSetting", systemSetting));
        }
        executeBatch(transaction);
    }

    /**
     * insertSystemSetting
     * code, group_code, name, text_value, decimal_value, date_value, bool_value
     * deleteSystemSetting
     * find by code
     * updateValue
     * set values find by code
     */
    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, SystemSetting entity) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertSystemSetting".equals(queryName)) {
            map.put(1, entity.getCode());
            map.put(2, entity.getGroupCode());
            map.put(3, entity.getName());
            map.put(4, entity.getTextValue());
            map.put(5, entity.getDecimalValue());
            map.put(6, entity.getDateValue());
            map.put(7, entity.getBoolValue());
        } else if ("deleteSystemSetting".equals(queryName)) {
            map.put(1, entity.getCode());
        } else if ("updateValue".equals(queryName)) {
            map.put(1, entity.getTextValue());
            map.put(2, entity.getDecimalValue());
            map.put(3, entity.getDateValue());
            map.put(4, entity.getBoolValue());
            map.put(5, entity.getCode());
        }
        return map;
    }

    public void insertDefaultSystemUser(boolean resetDefault) {
        List<SystemSetting> settingList = getSystemSettingDao().getSystemUser();
        if (resetDefault && !settingList.isEmpty()) {
            deleteSystemSetting(settingList);
        }
        if(settingList.isEmpty()) {
            insertSystemSetting(getSystemSettingDao().getDefaultSystemUser());
        }
    }
}
