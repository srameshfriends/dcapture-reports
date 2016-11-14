package excel.accounting.dao;

import excel.accounting.db.*;
import excel.accounting.entity.SystemSetting;
import excel.accounting.shared.DataConverter;

import java.math.BigDecimal;
import java.util.Arrays;
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
        systemSetting.setGroupCode((String) columns[1]);
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

    public SystemSetting findByCode(String code) {
        QueryBuilder builder = getQueryBuilder("findByCode");
        builder.add(1, code);
        return getDataReader().findSingleRow(builder, this);
    }

    public List<SystemSetting> findByCodeArray(String... codeArray) {
        SQLBuilder builder = createSQLQuery();
        builder.select(SystemSetting.class);
        builder.whereOrIn("code", codeArray);
        return getOrmReader().findAll(builder);
    }

    public List<SystemSetting> findByGroupCode(String groupCode) {
        QueryBuilder builder = getQueryBuilder("findByGroupCode");
        builder.add(1, groupCode);
        return getDataReader().findRowDataList(builder, this);
    }

    public List<SystemSetting> getSystemUser() {
        final String pf = "su";
        String[] codes = {pf + "u1", pf + "p1", pf + "u2", pf + "p2", pf + "u3", pf + "p3"};
        return findByCodeArray(codes);
    }

    public List<SystemSetting> getDefaultSystemUser() {
        final String pf = "su";
        SystemSetting u1 = createSystemSetting(pf + "u1", pf + "u1", "Abi", true);
        SystemSetting p1 = createSystemSetting(pf + "p1", pf + "u1", DataConverter.encode("password"), false);
        SystemSetting u2 = createSystemSetting(pf + "u2", pf + "u2", "Bha", true);
        SystemSetting p2 = createSystemSetting(pf + "p2", pf + "u2", DataConverter.encode("password"), false);
        SystemSetting u3 = createSystemSetting(pf + "u3", pf + "u3", "Cho", true);
        SystemSetting p3 = createSystemSetting(pf + "p3", pf + "u3", DataConverter.encode("password"), false);
        return Arrays.asList(u1, p1, u2, p2, u3, p3);
    }

    private SystemSetting createSystemSetting(String code, String groupCode, String textValue, boolean boolValue) {
        SystemSetting systemSetting = new SystemSetting();
        systemSetting.setCode(code);
        systemSetting.setGroupCode(groupCode);
        systemSetting.setTextValue(textValue);
        systemSetting.setBoolValue(boolValue);
        return systemSetting;
    }
}
