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
public class SystemSettingService extends AbstractService {
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
       /* QueryBuilder queryBuilder = getQueryBuilder("updateValue");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (SystemSetting systemSetting : settingList) {
            transaction.addBatch(getColumnsMap("updateValue", systemSetting));
        }
        executeBatch(transaction);*/
    }

    public void deleteSystemSetting(List<SystemSetting> settingList) {
      /*  QueryBuilder queryBuilder = getQueryBuilder("deleteSystemSetting");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (SystemSetting systemSetting : settingList) {
            transaction.addBatch(getColumnsMap("deleteSystemSetting", systemSetting));
        }
        executeBatch(transaction);*/
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
