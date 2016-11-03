package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.db.QueryBuilder;
import excel.accounting.db.RowColumnsToEntity;
import excel.accounting.entity.SystemSetting;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * System Setting Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class SystemSettingDao extends AbstractDao<SystemSetting> implements RowColumnsToEntity<SystemSetting> {
    @Override
    protected String getTableName() {
        return "entity.system_setting";
    }

    @Override
    protected String getSqlFileName() {
        return "system-setting";
    }

    @Override
    public SystemSetting getEntity(String queryName, Object[] columns) {
        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setCode((String) columns[0]);
        systemSetting.setSettingType((String) columns[1]);
        systemSetting.setName((String) columns[2]);
        systemSetting.setTextValue((String) columns[3]);
        systemSetting.setDecimalValue((BigDecimal) columns[4]);
        systemSetting.setDateValue((Date) columns[5]);
        systemSetting.setBoolValue((Boolean) columns[6]);
        return systemSetting;
    }

    @Override
    protected SystemSetting getReferenceRow(String primaryKay) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, primaryKay);
        return getDataReader().findSingleRow(builder, this);
    }

    public List<SystemSetting> loadAll() {
        QueryBuilder builder = getQueryBuilder("loadAll");
        return getDataReader().findRowDataList(builder, this);
    }
}
